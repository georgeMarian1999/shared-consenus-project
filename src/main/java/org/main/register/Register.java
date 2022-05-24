package org.main.register;

import org.main.abstraction.Abstraction;
import org.main.broadcast.BestEffortBroadcast;
import org.main.perfectlink.PerfectLink;
import org.main.projectbase.CommunicationProtocol;
import org.main.system.ApplicationSystem;
import org.main.utilities.Utilities;

import java.io.IOException;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class Register extends Abstraction {

    static final Logger logger = Logger.getLogger(String.valueOf(Register.class));


    private boolean reading;
    private int acks;
    private int rid;
    private CommunicationProtocol.Value writeval;
    private CommunicationProtocol.Value readval;
    private NNARObject nnarObject;
    private ConcurrentHashMap<String, NNARObject> readlist = new ConcurrentHashMap<>();

    public Register(String abstractionId, ApplicationSystem system) {
        super(abstractionId, system);

        logger.info(String.format("%s create: %s", Thread.currentThread().getName(), abstractionId));
        system.addAbstraction(new PerfectLink(abstractionId + ".pl", system));
        system.addAbstraction(new PerfectLink(abstractionId + ".beb.pl", system));
        system.addAbstraction(new BestEffortBroadcast(abstractionId + ".beb", system));
        writeval = CommunicationProtocol.Value.newBuilder()
                .setDefined(false)
                .build();
        readval = CommunicationProtocol.Value.newBuilder()
                .setDefined(false)
                .build();
        rid = 0;
        acks = 0;
        reading = false;
        nnarObject = new NNARObject();
    }



    @Override
    public String handleMessage(CommunicationProtocol.Message message) throws IOException {
        if (!abstractionId.equals(message.getToAbstractionId())) {
            return "Unsupported message";
        }

        logger.info(String.format("%s: %s from %s to %s\n", Thread.currentThread().getName(), message.getType(), message.getFromAbstractionId(), message.getToAbstractionId()));
        switch (message.getType()) {
            case NNAR_WRITE:
                // received from app
                handleWrite(message);
                return "Message handled";
            case NNAR_READ:
                // received from app
                handleRead(message);
                return "Message handled";
            case BEB_DELIVER:
                if (message.getBebDeliver().getMessage().getType() == CommunicationProtocol.Message.Type.NNAR_INTERNAL_READ) {
                    // send current value to sender
                    perfectLinkSendNnarInternalValue(message);
                    return "Message handled";
                }
                if (message.getBebDeliver().getMessage().getType() == CommunicationProtocol.Message.Type.NNAR_INTERNAL_WRITE) {
                    // update value and send acknowledgement to sender
                    perfectLinkSendAck(message);
                    return "Message handled";
                }
            case PL_DELIVER:
                // in the initiator
                perfectLinkDeliver(message);
               
        }
        return "Unsupported message";
    }



    private void handleWrite(CommunicationProtocol.Message message) {
        CommunicationProtocol.NnarWrite nnarWriteMessage = message.getNnarWrite();
        rid++;
        writeval = nnarWriteMessage.getValue();
        acks = 0;
        readlist.clear();

        CommunicationProtocol.NnarInternalRead internalRead = CommunicationProtocol.NnarInternalRead.newBuilder()
                .setReadId(rid)
                .build();
        CommunicationProtocol.Message internalREadMessage = CommunicationProtocol.Message.newBuilder()
                .setType(CommunicationProtocol.Message.Type.NNAR_INTERNAL_READ)
                .setNnarInternalRead(internalRead)
                .setMessageUuid(String.valueOf(UUID.randomUUID()))
                .setToAbstractionId(this.abstractionId)
                .setFromAbstractionId(this.abstractionId)
                .setSystemId(system.getId())
                .build();
        CommunicationProtocol.BebBroadcast bebBroadcast = CommunicationProtocol.BebBroadcast.newBuilder()
                .setMessage(internalREadMessage)
                .build();

        CommunicationProtocol.Message messageToSend = CommunicationProtocol.Message.newBuilder()
                .setType(CommunicationProtocol.Message.Type.BEB_BROADCAST)
                .setBebBroadcast(bebBroadcast)
                .setMessageUuid(String.valueOf(UUID.randomUUID()))
                .setToAbstractionId(this.abstractionId + ".beb")
                .setFromAbstractionId(this.abstractionId)
                .setSystemId(system.getId())
                .build();
        system.addMessage(messageToSend);
    }
    private void handleRead(CommunicationProtocol.Message message) {
        rid++;
        acks = 0;
        readlist.clear();
        reading = true;
        CommunicationProtocol.NnarInternalRead internalRead = CommunicationProtocol.NnarInternalRead.newBuilder()
                .setReadId(rid)
                .build();
        CommunicationProtocol.Message internalREadMessage = CommunicationProtocol.Message.newBuilder()
                .setType(CommunicationProtocol.Message.Type.NNAR_INTERNAL_READ)
                .setNnarInternalRead(internalRead)
                .setMessageUuid(String.valueOf(UUID.randomUUID()))
                .setToAbstractionId(this.abstractionId)
                .setFromAbstractionId(this.abstractionId)
                .setSystemId(system.getId())
                .build();
        CommunicationProtocol.BebBroadcast bebBroadcast = CommunicationProtocol.BebBroadcast.newBuilder()
                .setMessage(internalREadMessage)
                .build();

        CommunicationProtocol.Message messageToSend = CommunicationProtocol.Message.newBuilder()
                .setType(CommunicationProtocol.Message.Type.BEB_BROADCAST)
                .setBebBroadcast(bebBroadcast)
                .setMessageUuid(String.valueOf(UUID.randomUUID()))
                .setToAbstractionId(this.abstractionId + ".beb")
                .setFromAbstractionId(this.abstractionId)
                .setSystemId(system.getId())
                .build();
        system.addMessage(messageToSend);
    }
    private void perfectLinkSendNnarInternalValue(CommunicationProtocol.Message message) {
        CommunicationProtocol.BebDeliver bebDeliver = message.getBebDeliver();
        CommunicationProtocol.ProcessId sender = bebDeliver.getSender();
        CommunicationProtocol.NnarInternalValue internalValue = CommunicationProtocol.NnarInternalValue.newBuilder()
                .setReadId(bebDeliver.getMessage().getNnarInternalRead().getReadId())
                .setTimestamp(nnarObject.getTs())
                .setWriterRank(nnarObject.getWr())
                .setValue(nnarObject.getVal())
                .build();
        CommunicationProtocol.Message internalValueMessage = CommunicationProtocol.Message.newBuilder()
                .setType(CommunicationProtocol.Message.Type.NNAR_INTERNAL_VALUE)
                .setNnarInternalValue(internalValue)
                .setMessageUuid(String.valueOf(UUID.randomUUID()))
                .setToAbstractionId(this.abstractionId)
                .setFromAbstractionId(this.abstractionId)
                .setSystemId(system.getId())
                .build();
        CommunicationProtocol.PlSend plSend = CommunicationProtocol.PlSend.newBuilder()
                .setDestination(sender)
                .setMessage(internalValueMessage)
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
    }
    private void perfectLinkSendAck(CommunicationProtocol.Message message) {
        CommunicationProtocol.ProcessId sender = message.getBebDeliver().getSender();
        CommunicationProtocol.NnarInternalWrite internalWrite = message.getBebDeliver().getMessage().getNnarInternalWrite();
        int readId = internalWrite.getReadId();
        int timestamp = internalWrite.getTimestamp();
        int writerRank = internalWrite.getWriterRank();
        CommunicationProtocol.Value value = internalWrite.getValue();
        if (timestamp > nnarObject.getTs()) {
            nnarObject = new NNARObject(timestamp, writerRank, value);
        } else if (timestamp == nnarObject.getTs()) {
            if (writerRank > nnarObject.getWr()) {
                nnarObject = new NNARObject(timestamp, writerRank, value);
            }
        }
        CommunicationProtocol.NnarInternalAck internalAck = CommunicationProtocol.NnarInternalAck.newBuilder()
                .setReadId(readId)
                .build();
        CommunicationProtocol.Message ackMessage = CommunicationProtocol.Message.newBuilder()
                .setType(CommunicationProtocol.Message.Type.NNAR_INTERNAL_ACK)
                .setNnarInternalAck(internalAck)
                .setMessageUuid(String.valueOf(UUID.randomUUID()))
                .setToAbstractionId(this.abstractionId)
                .setFromAbstractionId(this.abstractionId)
                .setSystemId(system.getId())
                .build();
        CommunicationProtocol.PlSend plSend = CommunicationProtocol.PlSend.newBuilder()
                .setDestination(sender)
                .setMessage(ackMessage)
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
    }

    private void perfectLinkDeliver(CommunicationProtocol.Message message) {
        CommunicationProtocol.Message internalMessage = message.getPlDeliver().getMessage();
        switch (internalMessage.getType()) {
            case NNAR_INTERNAL_VALUE:
                CommunicationProtocol.NnarInternalValue nnarInternalValue = message.getPlDeliver().getMessage().getNnarInternalValue();
                int r_NNAR_INTERNAL_VALUE = nnarInternalValue.getReadId();
                if (rid == r_NNAR_INTERNAL_VALUE) {
                    CommunicationProtocol.ProcessId sender = message.getPlDeliver().getSender();
                    readlist.put(sender.getOwner() + "-" + sender.getIndex(), new NNARObject(nnarInternalValue.getTimestamp(), nnarInternalValue.getWriterRank(), nnarInternalValue.getValue()));
                    if (readlist.size() > system.getProcessIds().size() / 2) {
                        NNARObject maxValue = readlist.values().stream()
                                .sorted(Comparator.comparing(NNARObject::getTs).reversed().thenComparing(NNARObject::getWr).reversed())
                                .findFirst().get();
                        readval = maxValue.getVal();
                        readlist.clear();
                        if (reading) {
                            CommunicationProtocol.Message resultMessage = createBebBroadcastWriteInternalMessage(rid, maxValue.getTs(), maxValue.getWr(), readval);
                            system.addMessage(resultMessage);
                        } else {
                            CommunicationProtocol.Message resultMessage = createBebBroadcastWriteInternalMessage(rid, maxValue.getTs() + 1, system.getCurrentProcessId().getRank(), writeval);
                            system.addMessage(resultMessage);
                        }
                    }
                }
            case NNAR_INTERNAL_ACK:
                CommunicationProtocol.NnarInternalAck nnarInternalAck = message.getPlDeliver().getMessage().getNnarInternalAck();
                int r_NNAR_INTERNAL_ACK = nnarInternalAck.getReadId();
                if (rid == r_NNAR_INTERNAL_ACK) {
                    acks++;
                    if (acks > system.getProcessIds().size() / 2) {
                        acks = 0;
                        if (reading) {
                            reading = false;
                            CommunicationProtocol.NnarReadReturn nnarReadReturn = CommunicationProtocol.NnarReadReturn.newBuilder()
                                    .setValue(readval)
                                    .build();
                            CommunicationProtocol.Message readMessage = CommunicationProtocol.Message.newBuilder()
                                    .setType(CommunicationProtocol.Message.Type.NNAR_READ_RETURN)
                                    .setNnarReadReturn(nnarReadReturn)
                                    .setMessageUuid(String.valueOf(UUID.randomUUID()))
                                    .setToAbstractionId(Utilities.getParentAbstraction(this.abstractionId))
                                    .setFromAbstractionId(this.abstractionId)
                                    .setSystemId(system.getId())
                                    .build();
                            system.addMessage(readMessage);
                        } else {
                            CommunicationProtocol.NnarWriteReturn nnarWriteReturn = CommunicationProtocol.NnarWriteReturn.newBuilder().build();
                            CommunicationProtocol.Message writeMessage = CommunicationProtocol.Message.newBuilder()
                                    .setType(CommunicationProtocol.Message.Type.NNAR_WRITE_RETURN)
                                    .setNnarWriteReturn(nnarWriteReturn)
                                    .setMessageUuid(String.valueOf(UUID.randomUUID()))
                                    .setToAbstractionId(Utilities.getParentAbstraction(this.abstractionId))
                                    .setFromAbstractionId(this.abstractionId)
                                    .setSystemId(system.getId())
                                    .build();
                            system.addMessage(writeMessage);
                        }
                    }
        }
        }
    }
    private CommunicationProtocol.Message createBebBroadcastWriteInternalMessage(int rid, int ts, int wr, CommunicationProtocol.Value val) {
        CommunicationProtocol.NnarInternalWrite nnarInternalWrite = CommunicationProtocol.NnarInternalWrite.newBuilder()
                .setReadId(rid)
                .setTimestamp(ts)
                .setWriterRank(wr)
                .setValue(val)
                .build();
        CommunicationProtocol.Message nnarInternalWriteMessage = CommunicationProtocol.Message.newBuilder()
                .setType(CommunicationProtocol.Message.Type.NNAR_INTERNAL_WRITE)
                .setNnarInternalWrite(nnarInternalWrite)
                .setMessageUuid(String.valueOf(UUID.randomUUID()))
                .setToAbstractionId(this.abstractionId)
                .setFromAbstractionId(this.abstractionId)
                .setSystemId(system.getId())
                .build();
        CommunicationProtocol.BebBroadcast bebBroadcast = CommunicationProtocol.BebBroadcast.newBuilder()
                .setMessage(nnarInternalWriteMessage)
                .build();
        return CommunicationProtocol.Message.newBuilder()
                .setType(CommunicationProtocol.Message.Type.BEB_BROADCAST)
                .setBebBroadcast(bebBroadcast)
                .setMessageUuid(String.valueOf(UUID.randomUUID()))
                .setToAbstractionId(this.abstractionId + ".beb")
                .setFromAbstractionId(this.abstractionId)
                .setSystemId(system.getId())
                .build();
    }
}
