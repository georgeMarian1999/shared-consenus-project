package org.main.perfectlink;

import org.main.TCP.TCP;
import org.main.abstraction.Abstraction;
import org.main.projectbase.CommunicationProtocol;
import org.main.system.ApplicationSystem;
import org.main.utilities.Utilities;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

public class PerfectLink extends Abstraction {

    static final Logger logger = Logger.getLogger(String.valueOf(PerfectLink.class));

    public PerfectLink(String abstractionId, ApplicationSystem system) {
        super(abstractionId, system);
    }


    @Override
    public String handleMessage(CommunicationProtocol.Message message) throws IOException {
        if(!abstractionId.equals(message.getToAbstractionId())) {
            return "Unsupported message for this abstraction";
        }
        switch (message.getType()) {
            case NETWORK_MESSAGE:
                if (message.getNetworkMessage().getMessage().getType() != CommunicationProtocol.Message.Type.EPFD_INTERNAL_HEARTBEAT_REQUEST
                        && message.getNetworkMessage().getMessage().getType() != CommunicationProtocol.Message.Type.EPFD_INTERNAL_HEARTBEAT_REPLY) {
                    logger.info(String.format("%s%s: %s from %s to %s\n", Thread.currentThread().getName(), message.getType(), message.getFromAbstractionId(), message.getToAbstractionId()));
                }
                deliverNetworkMessage(message);
                return "Message handled. Case NETWORK_MESSAGE";
            case PL_DELIVER:
                if (message.getNetworkMessage().getMessage().getType() != CommunicationProtocol.Message.Type.EPFD_INTERNAL_HEARTBEAT_REQUEST
                        && message.getNetworkMessage().getMessage().getType() != CommunicationProtocol.Message.Type.EPFD_INTERNAL_HEARTBEAT_REPLY) {
                    logger.info(String.format("%s%s: %s from %s to %s\n", Thread.currentThread().getName(), message.getType(), message.getFromAbstractionId(), message.getToAbstractionId()));
                }
                sendNetworkMessage(message);
                return "Message handled. Case PL_DELIVER";
            default:
                return "Unsupported message";
        }
    }

    private void sendNetworkMessage(CommunicationProtocol.Message message) throws IOException {
        CommunicationProtocol.PlSend plSend = message.getPlSend();
        CommunicationProtocol.Message innerMessage = plSend.getMessage();

        CommunicationProtocol.NetworkMessage networkMessage = CommunicationProtocol.NetworkMessage.newBuilder()
                .setSenderHost(system.getProcess().getAddress())
                .setSenderListeningPort(system.getProcess().getPort())
                .setMessage(innerMessage)
                .build();

        CommunicationProtocol.Message forwardMessage = CommunicationProtocol.Message.newBuilder()
                .setType(CommunicationProtocol.Message.Type.NETWORK_MESSAGE)
                .setMessageUuid(String.valueOf(UUID.randomUUID()))
                .setNetworkMessage(networkMessage)
                .setToAbstractionId(abstractionId)
                .setFromAbstractionId(abstractionId)
                .setSystemId(system.getId())
                .build();
        TCP.sendMessage(forwardMessage, plSend.getDestination().getHost(), plSend.getDestination().getPort());
    }

    private void deliverNetworkMessage(CommunicationProtocol.Message message) throws IOException{
        CommunicationProtocol.NetworkMessage networkMessage = message.getNetworkMessage();
        CommunicationProtocol.ProcessId senderProcessId = system.getSenderProcessId(networkMessage.getSenderHost(), networkMessage.getSenderListeningPort());
        CommunicationProtocol.Message innerMessage = networkMessage.getMessage();

        CommunicationProtocol.PlDeliver plDeliver;
        if (senderProcessId == null) {
             plDeliver = CommunicationProtocol.PlDeliver.newBuilder()
                    .setMessage(innerMessage)
                    .build();
        }
        else {
            plDeliver = CommunicationProtocol.PlDeliver.newBuilder()
                    .setMessage(innerMessage)
                    .setSender(senderProcessId)
                    .build();
        }

        CommunicationProtocol.Message forwardMessage = CommunicationProtocol.Message.newBuilder()
                .setType(CommunicationProtocol.Message.Type.PL_DELIVER)
                .setPlDeliver(plDeliver)
                .setMessageUuid(String.valueOf(UUID.randomUUID()))
                .setToAbstractionId(Utilities.getParentAbstraction(message.getToAbstractionId()))
                .setFromAbstractionId(abstractionId)
                .setSystemId(system.getId())
                .build();
        system.addMessage(forwardMessage);
    }
}
