package org.main.system;

import org.main.abstraction.Abstraction;
import org.main.projectbase.CommunicationProtocol;
import org.main.register.Register;
import org.main.utilities.Utilities;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class MessageProcessor implements Runnable {

    final static Logger logger = Logger.getLogger(String.valueOf(MessageProcessor.class));

    private ApplicationSystem system;

    public MessageProcessor(ApplicationSystem system) {
        this.system = system;
    }

    public ApplicationSystem getSystem() {
        return system;
    }

    public void setSystem(ApplicationSystem system) {
        this.system = system;
    }

    @Override
    public void run() {

        while (true) {
            try {
                CommunicationProtocol.Message messageToHandle = system.getMessage();
                CommunicationProtocol.Message innerMessage = messageToHandle.getNetworkMessage().getMessage();

                if (innerMessage.getType() == CommunicationProtocol.Message.Type.PROC_DESTROY_SYSTEM) {
                    logger.info(String.format("%s destroyed system", Thread.currentThread().getName()));
                    break;
                }

                if (!system.isAbstraction(messageToHandle.getToAbstractionId())) {
                    if (messageToHandle.getToAbstractionId().contains(".nnar[")) {
                        String registerAbstraction = Utilities.getRegisterAbstraction(messageToHandle.getToAbstractionId());
                        system.addAbstraction(new Register(registerAbstraction, system));
                    } else if (messageToHandle.getToAbstractionId().contains(".uc[")) {
                        //String ucAbstraction = Utils.getUcAbstraction(message.getToAbstractionId());
                        //appSystem.addAbstraction(new Uc(ucAbstraction, appSystem));
                    }


                }
                List<Abstraction> abstractions = system.getAbstractions();

                for (Abstraction abstraction: abstractions
                ) {
                    try {
                        abstraction.handleMessage(messageToHandle);
                    }catch (IOException ex) {
                        logger.info(String.format("%s abstraction could not process message %s", abstraction.getAbstractionId(), messageToHandle.getMessageUuid()));
                    }
                }
            } catch (InterruptedException exception) {
                logger.info(String.format("Message processor process %s error %s ", Thread.currentThread().getName(), exception.getMessage()));
                exception.printStackTrace();
            }
        }
    }
}
