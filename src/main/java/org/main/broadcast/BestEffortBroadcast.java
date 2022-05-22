package org.main.broadcast;

import org.main.projectbase.CommunicationProtocol;

import java.util.List;
import java.util.Queue;

public class BestEffortBroadcast {

    private Queue<CommunicationProtocol.Message> messageQueue;
    private List<CommunicationProtocol.ProcessId> processes;
    private String id;

    public BestEffortBroadcast(Queue<CommunicationProtocol.Message> messageQueue, List<CommunicationProtocol.ProcessId> processes, String id) {
        this.messageQueue = messageQueue;
        this.processes = processes;
        this.id = id;
    }

    public Queue<CommunicationProtocol.Message> getMessageQueue() {
        return messageQueue;
    }

    public void setMessageQueue(Queue<CommunicationProtocol.Message> messageQueue) {
        this.messageQueue = messageQueue;
    }

    public List<CommunicationProtocol.ProcessId> getProcesses() {
        return processes;
    }

    public void setProcesses(List<CommunicationProtocol.ProcessId> processes) {
        this.processes = processes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public void Handle(CommunicationProtocol.Message message) throws Exception {

        switch (message.getType()) {
            case BEB_BROADCAST:
                for (CommunicationProtocol.ProcessId process: this.processes) {
                    CommunicationProtocol.Message messageToSend = CommunicationProtocol.Message.newBuilder()
                            .setType(CommunicationProtocol.Message.Type.PL_SEND)
                            .setFromAbstractionId(this.id)
                            .setToAbstractionId(this.id + ".pl")
                            .setPlSend(CommunicationProtocol.PlSend.newBuilder()
                                            .setDestination(process)
                                            .setMessage(message.getBebBroadcast().getMessage())
                                            .build()
                                      )
                            .build();
                    messageQueue.add(messageToSend);
                }
                break;
            case PL_DELIVER:
                CommunicationProtocol.Message messageToSend = CommunicationProtocol.Message.newBuilder()
                        .setType(CommunicationProtocol.Message.Type.BEB_DELIVER)
                        .setFromAbstractionId(this.id)
                        .setToAbstractionId(message.getPlDeliver().getMessage().getToAbstractionId())
                        .setBebDeliver(
                                CommunicationProtocol.BebDeliver.newBuilder()
                                        .setSender(message.getPlDeliver().getSender())
                                        .setMessage(message.getPlDeliver().getMessage())
                                        .build()
                        )
                        .build();
                messageQueue.add(messageToSend);
                break;
            default:
                throw new Exception("Message not supported");
        }
    }
}
