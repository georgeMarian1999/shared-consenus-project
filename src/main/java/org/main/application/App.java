package org.main.application;

import org.main.abstraction.Abstraction;
import org.main.projectbase.CommunicationProtocol;
import org.main.system.ApplicationSystem;
import org.main.utilities.Utilities;

import java.io.IOException;
import java.util.Queue;
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

        switch (message.getType()) {
            case PL_DELIVER:
                switch (message.getPlDeliver().getMessage().getType()) {
                    case APP_BROADCAST:
                        messageToSend = CommunicationProtocol.Message.newBuilder()
                                .setType(CommunicationProtocol.Message.Type.BEB_BROADCAST)
                                .setFromAbstractionId("app")
                                .setToAbstractionId("app.beb")
                                .setBebBroadcast(CommunicationProtocol.BebBroadcast.newBuilder()
                                        .setMessage(CommunicationProtocol.Message.newBuilder()
                                                .setType(CommunicationProtocol.Message.Type.APP_VALUE)
                                                .setFromAbstractionId("app")
                                                .setToAbstractionId("app")
                                                .setAppValue(CommunicationProtocol.AppValue.newBuilder()
                                                        .setValue(message.getPlDeliver().getMessage().getAppBroadcast().getValue())
                                                        .build())
                                                .build())
                                        .build())
                                .build();
                        return "Message handled App broadcast";
                    case APP_VALUE:
                        messageToSend = CommunicationProtocol.Message.newBuilder()
                                .setType(CommunicationProtocol.Message.Type.PL_SEND)
                                .setFromAbstractionId("app")
                                .setToAbstractionId("app.pl")
                                .setPlSend(CommunicationProtocol.PlSend.newBuilder()
                                        .setMessage(CommunicationProtocol.Message.newBuilder()
                                                .setType(CommunicationProtocol.Message.Type.APP_VALUE)
                                                .setAppValue(message.getPlDeliver().getMessage().getAppValue())
                                                .build())
                                        .build())
                                .build();
                        return "Message handled App value";
                    case APP_WRITE:
                        messageToSend = CommunicationProtocol.Message.newBuilder()
                                .setType(CommunicationProtocol.Message.Type.NNAR_WRITE)
                                .setFromAbstractionId("app")
                                .setToAbstractionId("app.nnar[" + message.getPlDeliver().getMessage().getAppWrite().getRegister() + "]")
                                .setNnarWrite(CommunicationProtocol.NnarWrite.newBuilder()
                                        .setValue(message.getPlDeliver().getMessage().getAppWrite().getValue())
                                        .build())
                                .build();
                        return "Message handled App write";
                    case APP_READ:
                        messageToSend = CommunicationProtocol.Message.newBuilder()
                                .setType(CommunicationProtocol.Message.Type.NNAR_READ)
                                .setFromAbstractionId("app")
                                .setToAbstractionId("app.nnar[" + message.getPlDeliver().getMessage().getAppRead().getRegister() + "]")
                                .setNnarRead(CommunicationProtocol.NnarRead.newBuilder()
                                        .build())
                                .build();
                        return "Message handled App broadcast";
                    default:
                        throw new IOException("Message not supported");
                }
            case BEB_DELIVER:
                messageToSend = CommunicationProtocol.Message.newBuilder()
                        .setType(CommunicationProtocol.Message.Type.PL_SEND)
                        .setFromAbstractionId("app")
                        .setToAbstractionId("app.pl")
                        .setPlSend(CommunicationProtocol.PlSend.newBuilder()
                                .setMessage(CommunicationProtocol.Message.newBuilder()
                                        .setType(CommunicationProtocol.Message.Type.APP_VALUE)
                                        .setAppValue(message.getBebDeliver().getMessage().getAppValue())
                                        .build())
                                .build())
                        .build();
                return "Message handled App beb_deliver";
            case NNAR_WRITE_RETURN:
                messageToSend = CommunicationProtocol.Message.newBuilder()
                        .setType(CommunicationProtocol.Message.Type.PL_SEND)
                        .setFromAbstractionId("app")
                        .setToAbstractionId("app.pl")
                        .setPlSend(CommunicationProtocol.PlSend.newBuilder()
                                .setMessage(CommunicationProtocol.Message.newBuilder()
                                        .setType(CommunicationProtocol.Message.Type.APP_WRITE_RETURN)
                                        .setAppWriteReturn(CommunicationProtocol.AppWriteReturn.newBuilder()
                                                .setRegister(getRegister(message.getFromAbstractionId()))
                                                .build())
                                        .build())
                                .build())
                        .build();
                return "Message handled App NNAR_WRITE_RETURN";
            case NNAR_READ_RETURN:
                messageToSend = CommunicationProtocol.Message.newBuilder()
                        .setType(CommunicationProtocol.Message.Type.PL_SEND)
                        .setFromAbstractionId("app")
                        .setToAbstractionId("app.pl")
                        .setPlSend(CommunicationProtocol.PlSend.newBuilder()
                                .setMessage(CommunicationProtocol.Message.newBuilder()
                                        .setType(CommunicationProtocol.Message.Type.APP_READ_RETURN)
                                        .setAppReadReturn(CommunicationProtocol.AppReadReturn.newBuilder()
                                                .setRegister(getRegister(message.getFromAbstractionId()))
                                                .setValue(message.getNnarReadReturnOrBuilder().getValue())
                                                .build())
                                        .build())
                                .build())
                        .build();
                return "Message handled App broadcast";

        }
        return "Unsupported message";
    }

    private void APP_BROADCAST(CommunicationProtocol.Message message) {

    }
}
