package org.main.broadcast;

import org.main.abstraction.Abstraction;
import org.main.projectbase.CommunicationProtocol;
import org.main.system.ApplicationSystem;
import org.main.utilities.Utilities;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class BestEffortBroadcast extends Abstraction {

    static final Logger logger = Logger.getLogger(String.valueOf(BestEffortBroadcast.class));

    public BestEffortBroadcast(String abstractionId, ApplicationSystem system) {
        super(abstractionId, system);
    }

    @Override
    public String handleMessage(CommunicationProtocol.Message message) throws IOException {
        // logger.info(String.format("%s:%s from %s to %s\n", Thread.currentThread().getName(), message.getType(), message.getFromAbstractionId(), message.getToAbstractionId()));
        if(!abstractionId.equals(message.getToAbstractionId())) {
            return "Unsupported message";
        }
        switch (message.getType()) {
            case BEB_BROADCAST:
                sendToAll(message);
                return "Beb processed message. BEB_BROADCAST";
            case PL_DELIVER:
                perfectLinkBebDeliver(message);
                return "Beb processed message. PL_DELIVER";

        }
        return "Beb message not supported";
    }

    private void sendToAll(CommunicationProtocol.Message message) {
        CommunicationProtocol.Message internalMessage = message.getBebBroadcast().getMessage();
        List<CommunicationProtocol.ProcessId> processes = system.getProcessIds();
        processes.forEach(p -> {
            CommunicationProtocol.PlSend plSend = CommunicationProtocol.PlSend.newBuilder()
                    .setDestination(p)
                    .setMessage(internalMessage)
                    .build();
            CommunicationProtocol.Message messageToSend = CommunicationProtocol.Message.newBuilder()
                    .setType(CommunicationProtocol.Message.Type.PL_SEND)
                    .setPlSend(plSend)
                    .setMessageUuid(String.valueOf(UUID.randomUUID()))
                    .setToAbstractionId(this.abstractionId + ".pl")
                    .setFromAbstractionId(this.abstractionId)
                    .setSystemId(system.getId())
                    .build();
            system.addMessage(messageToSend);
        });
    }

    private void perfectLinkBebDeliver(CommunicationProtocol.Message message) {
        CommunicationProtocol.Message internalMessage = message.getPlDeliver().getMessage();
        CommunicationProtocol.BebDeliver bebDeliver;
        if (message.getPlDeliver().getSender() != null) {
            bebDeliver = CommunicationProtocol.BebDeliver.newBuilder()
                    .setMessage(internalMessage)
                    .setSender(message.getPlDeliver().getSender())
                    .build();
        } else {
            bebDeliver = CommunicationProtocol.BebDeliver.newBuilder()
                    .setMessage(internalMessage)
                    .build();
        }
        CommunicationProtocol.Message messageToSend = CommunicationProtocol.Message.newBuilder()
                .setType(CommunicationProtocol.Message.Type.BEB_DELIVER)
                .setBebDeliver(bebDeliver)
                .setMessageUuid(String.valueOf(UUID.randomUUID()))
                .setToAbstractionId(Utilities.getParentAbstraction(message.getToAbstractionId()))
                .setFromAbstractionId(this.abstractionId)
                .setSystemId(system.getId())
                .build();
        system.addMessage(messageToSend);
    }
}
