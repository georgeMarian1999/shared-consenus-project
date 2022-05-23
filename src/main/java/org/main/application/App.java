package org.main.application;

import org.main.abstraction.Abstraction;
import org.main.consensus.UniformConsensus;
import org.main.projectbase.CommunicationProtocol;
import org.main.system.ApplicationSystem;
import org.main.utilities.Utilities;

import java.io.IOException;
import java.util.Queue;
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
        CommunicationProtocol.Message messageToSend = null;

        if(!abstractionId.equals(message.getToAbstractionId())) {
            return "Unsupported abstraction for this message";
        }

        switch (message.getType())  {
            case PL_DELIVER:
                CommunicationProtocol.Message nestedMessage = message.getPlDeliver().getMessage();
                switch (nestedMessage.getType()) {
                    case APP_BROADCAST:
                        APP_BROADCAST(message);
                    case APP_WRITE:
                        APP_WRITE(message);
                    case APP_READ:
                        APP_READ(message);
                    case APP_PROPOSE:
                        APP_PROPOSE(message);
                }
                return "Message handled in PL_DELIVER";
            case BEB_DELIVER:
                BEB_DELIVER(message);
                return "Message handled App beb_deliver";
            case NNAR_WRITE_RETURN:
                NNAR_WRITE_RETURN(message);
                return "Message handled App NNAR_WRITE_RETURN";
            case NNAR_READ_RETURN:
                NNAR_READ_RETURN(message);
                return "Message handled App NNAR_READ_RETURN";
            case UC_DECIDE:
                UC_DECIDE(message);
                return "Message handled App UC_DECIDE";

        }
        return "Unsupported message";
    }


    private void APP_PROPOSE(CommunicationProtocol.Message message) {
        CommunicationProtocol.AppPropose appPropose = message.getPlDeliver().getMessage().getAppPropose();

        String uniformConsenusAbstractionId = Utilities.getAbstractionIdUc(abstractionId, appPropose.getTopic());

        if (!system.isAbstraction(uniformConsenusAbstractionId)) {
            system.addAbstraction(new UniformConsensus(uniformConsenusAbstractionId, system));
        }
        CommunicationProtocol.UcPropose uniformConsenusPropose = CommunicationProtocol.UcPropose.newBuilder()
                .setValue(appPropose.getValue())
                .build();

        CommunicationProtocol.Message messageToPropose = CommunicationProtocol.Message.newBuilder()
                .setType(CommunicationProtocol.Message.Type.APP_PROPOSE)
                .setUcPropose(uniformConsenusPropose)
                .setMessageUuid(String.valueOf(UUID.randomUUID()))
                .setToAbstractionId(uniformConsenusAbstractionId)
                .setFromAbstractionId(abstractionId)
                .setSystemId(system.getId())
                .build();

        system.addMessage(messageToPropose);
    }


    private void APP_BROADCAST(CommunicationProtocol.Message message) {
        CommunicationProtocol.Message internalMessage = message.getPlDeliver().getMessage();
        CommunicationProtocol.Value messageValue = internalMessage.getAppBroadcast().getValue();
        CommunicationProtocol.AppValue appValue = CommunicationProtocol.AppValue.newBuilder()
                .setValue(messageValue)
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

    private void APP_VALUE(CommunicationProtocol.Message message) {

    }

    private void APP_WRITE(CommunicationProtocol.Message message) {
        CommunicationProtocol.Message internalMessage = message.getPlDeliver().getMessage();
        CommunicationProtocol.AppWrite appWriteFromMessage = internalMessage.getAppWrite();

        String registerAbstractionId = Utilities.getAbstractionIdNnarRegister(abstractionId, appWriteFromMessage.getRegister());
        if (!system.isAbstraction(registerAbstractionId)) {
            // system.addAbstraction(new Register(registerAbstractionId, system));
        }

        CommunicationProtocol.NnarWrite nnarWrite = CommunicationProtocol.NnarWrite.newBuilder()
                .setValue(appWriteFromMessage.getValue())
                .build();

        CommunicationProtocol.Message messageToSend = CommunicationProtocol.Message.newBuilder()
                .setType(CommunicationProtocol.Message.Type.NNAR_WRITE)
                .setNnarWrite(nnarWrite)
                .setMessageUuid(String.valueOf(UUID.randomUUID()))
                .setToAbstractionId(registerAbstractionId)
                .setFromAbstractionId(abstractionId)
                .setSystemId(system.getId())
                .build();

        system.addMessage(messageToSend);
    }

    private void APP_READ(CommunicationProtocol.Message message) {
        CommunicationProtocol.Message internalMessage = message.getPlDeliver().getMessage();
        CommunicationProtocol.AppRead appReadFromMessage = internalMessage.getAppRead();
        String registerAbstractionId = Utilities.getAbstractionIdNnarRegister(abstractionId, appReadFromMessage.getRegister());

        if (!system.isAbstraction(registerAbstractionId)) {
            // system.addAbstraction(new Register(registerAbstractionId, system));
        }

        CommunicationProtocol.NnarRead nnarRead = CommunicationProtocol.NnarRead.newBuilder()
                .build();

        CommunicationProtocol.Message messageToSend = CommunicationProtocol.Message.newBuilder()
                .setType(CommunicationProtocol.Message.Type.APP_READ)
                .setNnarRead(nnarRead)
                .setMessageUuid(String.valueOf(UUID.randomUUID()))
                .setToAbstractionId(registerAbstractionId)
                .setFromAbstractionId(abstractionId)
                .setSystemId(system.getId())
                .build();

        system.addMessage(messageToSend);
    }

    private void BEB_DELIVER(CommunicationProtocol.Message message) {
        // Send value to hub after beb broadcast
        CommunicationProtocol.Message internalMessage = message.getBebDeliver().getMessage();
        CommunicationProtocol.PlSend plSend = CommunicationProtocol.PlSend.newBuilder()
                .setMessage(internalMessage)
                .setDestination(system.getProcess().getHub().getProcessId())
                .build();

        CommunicationProtocol.Message messageToSend = CommunicationProtocol.Message.newBuilder()
                .setType(CommunicationProtocol.Message.Type.PL_SEND)
                .setPlSend(plSend)
                .setMessageUuid(String.valueOf(UUID.randomUUID()))
                .setToAbstractionId(abstractionId + ".pl")
                .setFromAbstractionId(abstractionId)
                .setSystemId(system.getId())
                .build();

        system.addMessage(messageToSend);
    }

    private void NNAR_WRITE_RETURN(CommunicationProtocol.Message message) {
        CommunicationProtocol.AppWriteReturn appWriteReturn = CommunicationProtocol.AppWriteReturn.newBuilder()
                .setRegister(Utilities.getRegisterName(message.getFromAbstractionId()))
                .build();

        CommunicationProtocol.Message writeMessage = CommunicationProtocol.Message.newBuilder()
                .setType(CommunicationProtocol.Message.Type.APP_WRITE_RETURN)
                .setAppWriteReturn(appWriteReturn)
                .setMessageUuid(String.valueOf(UUID.randomUUID()))
                .setToAbstractionId(abstractionId)
                .setFromAbstractionId(abstractionId)
                .setSystemId(system.getId())
                .build();

        CommunicationProtocol.PlSend plSend = CommunicationProtocol.PlSend.newBuilder()
                .setDestination(system.getProcess().getHub().getProcessId())
                .setMessage(writeMessage)
                .build();

        CommunicationProtocol.Message messageToSend = CommunicationProtocol.Message.newBuilder()
                .setType(CommunicationProtocol.Message.Type.PL_SEND)
                .setPlSend(plSend)
                .setFromAbstractionId(abstractionId)
                .setToAbstractionId(abstractionId + ".pl")
                .setSystemId(system.getId())
                .build();

        system.addMessage(messageToSend);
    }

    private void NNAR_READ_RETURN(CommunicationProtocol.Message message) {
        CommunicationProtocol.AppReadReturn appReadReturn = CommunicationProtocol.AppReadReturn.newBuilder()
                .setRegister(Utilities.getRegisterName(message.getFromAbstractionId()))
                .setValue(message.getNnarReadReturn().getValue())
                .build();

        CommunicationProtocol.Message readMessage = CommunicationProtocol.Message.newBuilder()
                .setType(CommunicationProtocol.Message.Type.APP_READ_RETURN)
                .setAppReadReturn(appReadReturn)
                .setMessageUuid(String.valueOf(UUID.randomUUID()))
                .setFromAbstractionId(abstractionId)
                .setToAbstractionId(abstractionId)
                .setSystemId(system.getId())
                .build();

        CommunicationProtocol.PlSend plSend = CommunicationProtocol.PlSend.newBuilder()
                .setDestination(system.getProcess().getHub().getProcessId())
                .setMessage(readMessage)
                .build();

        CommunicationProtocol.Message messageToSend = CommunicationProtocol.Message.newBuilder()
                .setType(CommunicationProtocol.Message.Type.PL_SEND)
                .setPlSend(plSend)
                .setFromAbstractionId(abstractionId)
                .setToAbstractionId(abstractionId + ".pl")
                .setSystemId(system.getId())
                .build();

        system.addMessage(messageToSend);
    }

    private void UC_DECIDE(CommunicationProtocol.Message message) {
        CommunicationProtocol.Value messageValue = message.getUcDecide().getValue();
        CommunicationProtocol.AppDecide appDecide = CommunicationProtocol.AppDecide.newBuilder()
                .setValue(messageValue)
                .build();

        CommunicationProtocol.Message decisionMessage = CommunicationProtocol.Message.newBuilder()
                .setType(CommunicationProtocol.Message.Type.APP_DECIDE)
                .setAppDecide(appDecide)
                .setMessageUuid(String.valueOf(UUID.randomUUID()))
                .setFromAbstractionId(abstractionId)
                .setToAbstractionId(abstractionId)
                .setSystemId(system.getId())
                .build();

        CommunicationProtocol.PlSend plSend = CommunicationProtocol.PlSend.newBuilder()
                .setDestination(system.getProcess().getHub().getProcessId())
                .setMessage(decisionMessage)
                .build();

        CommunicationProtocol.Message messageToSend = CommunicationProtocol.Message.newBuilder()
                .setType(CommunicationProtocol.Message.Type.PL_SEND)
                .setPlSend(plSend)
                .setFromAbstractionId(abstractionId)
                .setToAbstractionId(abstractionId + ".pl")
                .setSystemId(system.getId())
                .build();

        system.addMessage(messageToSend);
    }


}
