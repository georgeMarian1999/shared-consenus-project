package org.main.broadcast;

import org.main.abstraction.Abstraction;
import org.main.projectbase.CommunicationProtocol;
import org.main.system.ApplicationSystem;
import org.main.utilities.Utilities;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class BestEffortBroadcast extends Abstraction {


    public BestEffortBroadcast(String abstractionId, ApplicationSystem system) {
        super(abstractionId, system);
    }

    @Override
    public String handleMessage(CommunicationProtocol.Message message) throws IOException {
        switch (message.getType()) {
            case BEB_BROADCAST:
                sendToAll(message);
                return "Beb processed message. BEB_BROADCAST";
            case PL_DELIVER:
                plBebDeliver(message);
                return "Beb processed message. PL_DELIVER";
            default:
                return "Beb message not supported";
        }
    }

    private void sendToAll(CommunicationProtocol.Message message) {
        CommunicationProtocol.Message innerMessage = message.getBebBroadcast().getMessage();
        List<CommunicationProtocol.ProcessId> processIds = system.getProcessIds();

        for (CommunicationProtocol.ProcessId process: processIds
             ) {
            CommunicationProtocol.PlSend plSend = CommunicationProtocol.PlSend.newBuilder()
                    .setDestination(process)
                    .setMessage(innerMessage)
                    .build();
            CommunicationProtocol.Message messageToSend = CommunicationProtocol.Message.newBuilder()
                    .setType(CommunicationProtocol.Message.Type.PL_SEND)
                    .setPlSend(plSend)
                    .setMessageUuid(String.valueOf(UUID.randomUUID()))
                    .setFromAbstractionId(this.abstractionId)
                    .setToAbstractionId(this.abstractionId + ".pl")
                    .setSystemId(system.getId())
                    .build();

            system.addMessage(messageToSend);
        }
    }

    private void plBebDeliver(CommunicationProtocol.Message message) {
        CommunicationProtocol.Message innerMessage = message.getPlDeliver().getMessage();
        CommunicationProtocol.BebDeliver bebDeliver;
        if (message.getPlDeliver().getSender() != null) {
            bebDeliver = CommunicationProtocol.BebDeliver.newBuilder()
                    .setSender(message.getPlDeliver().getSender())
                    .setMessage(innerMessage)
                    .build();
        }
        else  {
            bebDeliver = CommunicationProtocol.BebDeliver.newBuilder()
                    .setMessage(innerMessage)
                    .build();
        }

        CommunicationProtocol.Message messageToSend = CommunicationProtocol.Message.newBuilder()
                .setType(CommunicationProtocol.Message.Type.BEB_DELIVER)
                .setBebDeliver(bebDeliver)
                .setToAbstractionId(Utilities.getParentAbstraction(message.getToAbstractionId()))
                .setFromAbstractionId(this.abstractionId)
                .setMessageUuid(String.valueOf(UUID.randomUUID()))
                .setToAbstractionId(innerMessage.getToAbstractionId())
                .setSystemId(system.getId())
                .build();

        system.addMessage(messageToSend);
    }
}
