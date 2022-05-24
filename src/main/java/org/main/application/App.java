package org.main.application;

import org.main.abstraction.Abstraction;
import org.main.projectbase.CommunicationProtocol;
import org.main.register.Register;
import org.main.system.ApplicationSystem;
import org.main.utilities.Utilities;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App extends Abstraction {

    static final Logger logger = Logger.getLogger(String.valueOf(App.class));

    public App(String abstractionId, ApplicationSystem system) {
        super(abstractionId, system);
    }

    public String getRegister(String value) {
        Pattern pattern = Pattern.compile("[(.*)]");
        Matcher matcher = pattern.matcher(value);

        return matcher.toString();
    }

    @Override
    public String handleMessage(CommunicationProtocol.Message message) throws IOException {
        if (!abstractionId.equals(message.getToAbstractionId())) {
            return "invalid";
        }

        // logger.info(String.format("%s: %s from %s to %s\n", Thread.currentThread().getName(), message.getType().toString(), message.getFromAbstractionId(), message.getToAbstractionId()));
        switch (message.getType()) {
            case PL_DELIVER:
                // Process messages received from hub
                if (message.getPlDeliver().getMessage().getType() == CommunicationProtocol.Message.Type.APP_BROADCAST) {
                    APP_BROADCAST(message);
                } else if (message.getPlDeliver().getMessage().getType() == CommunicationProtocol.Message.Type.APP_WRITE) {
                    APP_WRITE(message);
                } else if (message.getPlDeliver().getMessage().getType() == CommunicationProtocol.Message.Type.APP_READ) {
                    APP_READ(message);
                } else if (message.getPlDeliver().getMessage().getType() == CommunicationProtocol.Message.Type.APP_PROPOSE) {
                    APP_PROPOSE(message);
                }
                return "processed";
            case BEB_DELIVER:
                // sends the value back to the hub after broadcast
                perfectLinkSendValueToHub(message);
                return "processed";
            case NNAR_READ_RETURN:
                // sends the value back to the hub after read
                perfectLinkSendReadReturn(message);
                return "processed";
            case NNAR_WRITE_RETURN:
                // sends the value back to the hub after write
                perfectLinkSendWriteReturn(message);
                return "processed";
            case UC_DECIDE:
                perfectLinkSendAppDecide(message);
                return "processed";
        }
        return "invalid";
    }


    private void perfectLinkSendAppDecide(CommunicationProtocol.Message message) {

    }

    private void APP_PROPOSE(CommunicationProtocol.Message message) {
//        CommunicationProtocol.AppPropose proposition = message.getPlDeliver().getMessage().getAppPropose();
//        String ucAbstractionId = Utilities.getAbstractionIdUc(abstractionId, proposition.getTopic());
//        if (!system.isAbstraction(ucAbstractionId)) {
//            // system.addAbstraction(new Uc(ucAbstractionId, system));
//        }
//        CommunicationProtocol.UcPropose ucPropose = CommunicationProtocol.UcPropose.newBuilder()
//                .setValue(proposition.getValue())
//                .build();
//        CommunicationProtocol.Message proposeMessage = CommunicationProtocol.Message.newBuilder()
//                .setType(CommunicationProtocol.Message.Type.UC_PROPOSE)
//                .setUcPropose(ucPropose)
//                .setMessageUuid(String.valueOf(UUID.randomUUID()))
//                .setToAbstractionId(ucAbstractionId)
//                .setFromAbstractionId(this.abstractionId)
//                .setSystemId(system.getId())
//                .build();
//        system.addMessage(proposeMessage);
    }

    private void perfectLinkSendWriteReturn(CommunicationProtocol.Message message) {
        CommunicationProtocol.AppWriteReturn appWriteReturn = CommunicationProtocol.AppWriteReturn.newBuilder()
                .setRegister(Utilities.getRegisterName(message.getFromAbstractionId()))
                .build();
        CommunicationProtocol.Message writeMessage = CommunicationProtocol.Message.newBuilder()
                .setType(CommunicationProtocol.Message.Type.APP_WRITE_RETURN)
                .setAppWriteReturn(appWriteReturn)
                .setMessageUuid(String.valueOf(UUID.randomUUID()))
                .setToAbstractionId(this.abstractionId)
                .setFromAbstractionId(this.abstractionId)
                .setSystemId(system.getId())
                .build();
        CommunicationProtocol.PlSend plSend = CommunicationProtocol.PlSend.newBuilder()
                .setDestination(system.getProcess().getHub().getProcessId())
                .setMessage(writeMessage)
                .build();
        CommunicationProtocol.Message messageToSend = CommunicationProtocol.Message.newBuilder()
                .setType(CommunicationProtocol.Message.Type.PL_SEND)
                .setPlSend(plSend)
                .setFromAbstractionId(this.abstractionId)
                .setToAbstractionId(this.abstractionId + ".pl")
                .setSystemId(system.getId())
                .build();
        system.addMessage(messageToSend);
    }

    private void perfectLinkSendReadReturn(CommunicationProtocol.Message message) {
        CommunicationProtocol.AppReadReturn appReadReturn = CommunicationProtocol.AppReadReturn.newBuilder()
                .setRegister(Utilities.getRegisterName(message.getFromAbstractionId()))
                .setValue(message.getNnarReadReturn().getValue())
                .build();
        CommunicationProtocol.Message readMessage = CommunicationProtocol.Message.newBuilder()
                .setType(CommunicationProtocol.Message.Type.APP_READ_RETURN)
                .setAppReadReturn(appReadReturn)
                .setMessageUuid(String.valueOf(UUID.randomUUID()))
                .setToAbstractionId(this.abstractionId)
                .setFromAbstractionId(this.abstractionId)
                .setSystemId(system.getId())
                .build();
        CommunicationProtocol.PlSend plSend = CommunicationProtocol.PlSend.newBuilder()
                .setDestination(system.getProcess().getHub().getProcessId())
                .setMessage(readMessage)
                .build();
        CommunicationProtocol.Message messageToSend = CommunicationProtocol.Message.newBuilder()
                .setType(CommunicationProtocol.Message.Type.PL_SEND)
                .setPlSend(plSend)
                .setFromAbstractionId(this.abstractionId)
                .setToAbstractionId(this.abstractionId + ".pl")
                .setSystemId(system.getId())
                .build();
        system.addMessage(messageToSend);
    }

    private void APP_WRITE(CommunicationProtocol.Message message) {
        CommunicationProtocol.Message internalMessage = message.getPlDeliver().getMessage();
        CommunicationProtocol.AppWrite appWriteMessage = internalMessage.getAppWrite();
        String registerAbstractionId = Utilities.getAbstractionIdNnarRegister(abstractionId, appWriteMessage.getRegister());
        if (!system.isAbstraction(registerAbstractionId)) {
            system.addAbstraction(new Register(registerAbstractionId, system));
        }
        CommunicationProtocol.NnarWrite NNARWrite = CommunicationProtocol.NnarWrite.newBuilder()
                .setValue(appWriteMessage.getValue())
                .build();
        CommunicationProtocol.Message messageToSend = CommunicationProtocol.Message.newBuilder()
                .setType(CommunicationProtocol.Message.Type.NNAR_WRITE)
                .setNnarWrite(NNARWrite)
                .setMessageUuid(String.valueOf(UUID.randomUUID()))
                .setToAbstractionId(registerAbstractionId)
                .setFromAbstractionId(this.abstractionId)
                .setSystemId(system.getId())
                .build();
        system.addMessage(messageToSend);
    }

    private void APP_READ(CommunicationProtocol.Message message) {
        CommunicationProtocol.Message internalMessage = message.getPlDeliver().getMessage();
        CommunicationProtocol.AppRead appReadMessage = internalMessage.getAppRead();
        String registerAbstractionId = Utilities.getAbstractionIdNnarRegister(abstractionId, appReadMessage.getRegister());
        if (!system.isAbstraction(registerAbstractionId)) {
            system.addAbstraction(new Register(registerAbstractionId, system));
        }
        CommunicationProtocol.NnarRead nnarRead = CommunicationProtocol.NnarRead.newBuilder()
                .build();
        CommunicationProtocol.Message messageToSend = CommunicationProtocol.Message.newBuilder()
                .setType(CommunicationProtocol.Message.Type.NNAR_READ)
                .setNnarRead(nnarRead)
                .setMessageUuid(String.valueOf(UUID.randomUUID()))
                .setToAbstractionId(registerAbstractionId)
                .setFromAbstractionId(this.abstractionId)
                .setSystemId(system.getId())
                .build();
        system.addMessage(messageToSend);
    }

    private void perfectLinkSendValueToHub(CommunicationProtocol.Message message) {
        CommunicationProtocol.Message internalMessage = message.getBebDeliver().getMessage();
        CommunicationProtocol.PlSend plSend = CommunicationProtocol.PlSend.newBuilder()
                .setMessage(internalMessage)
                .setDestination(system.getProcess().getHub().getProcessId())
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

    private void APP_BROADCAST(CommunicationProtocol.Message message) {
        CommunicationProtocol.Message internalMessage = message.getPlDeliver().getMessage();
        CommunicationProtocol.Value broadCastValue = internalMessage.getAppBroadcast().getValue();
        CommunicationProtocol.AppValue appValue = CommunicationProtocol.AppValue.newBuilder()
                .setValue(broadCastValue)
                .build();
        CommunicationProtocol.Message appValueMessage = CommunicationProtocol.Message.newBuilder()
                .setType(CommunicationProtocol.Message.Type.APP_VALUE)
                .setAppValue(appValue)
                .setMessageUuid(String.valueOf(UUID.randomUUID()))
                .setToAbstractionId(this.abstractionId)
                .setFromAbstractionId(this.abstractionId)
                .setSystemId(system.getId())
                .build();
        CommunicationProtocol.BebBroadcast bebBroadcast = CommunicationProtocol.BebBroadcast.newBuilder()
                .setMessage(appValueMessage)
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

}
