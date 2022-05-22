package org.main.application;

import org.main.projectbase.CommunicationProtocol;

import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {

    private Queue<CommunicationProtocol.Message> messageQueue;


    public App(Queue<CommunicationProtocol.Message> messageQueue) {
        this.messageQueue = messageQueue;
    }

    public String getRegister(String value) {
        Pattern pattern = Pattern.compile("[(.*)]");
        Matcher matcher = pattern.matcher(value);

        return matcher.toString();
    }

    public void Handle(CommunicationProtocol.Message message) throws Exception {
        CommunicationProtocol.Message messageToSend = null;

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
                        break;
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
                        break;
                    case APP_WRITE:
                        messageToSend = CommunicationProtocol.Message.newBuilder()
                                .setType(CommunicationProtocol.Message.Type.NNAR_WRITE)
                                .setFromAbstractionId("app")
                                .setToAbstractionId("app.nnar[" + message.getPlDeliver().getMessage().getAppWrite().getRegister() + "]")
                                .setNnarWrite(CommunicationProtocol.NnarWrite.newBuilder()
                                        .setValue(message.getPlDeliver().getMessage().getAppWrite().getValue())
                                        .build())
                                .build();
                        break;
                    case APP_READ:
                        messageToSend = CommunicationProtocol.Message.newBuilder()
                                .setType(CommunicationProtocol.Message.Type.NNAR_READ)
                                .setFromAbstractionId("app")
                                .setToAbstractionId("app.nnar[" + message.getPlDeliver().getMessage().getAppRead().getRegister() + "]")
                                .setNnarRead(CommunicationProtocol.NnarRead.newBuilder()
                                        .build())
                                .build();
                        break;
                    default:
                        throw new Exception("Message not supported");
                }
                break;
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
                break;
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
                break;
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
                break;
            default:
                throw new Exception("Message not supported");
        }

        if (messageToSend != null){
            messageQueue.add(messageToSend);
        }

    }
}
