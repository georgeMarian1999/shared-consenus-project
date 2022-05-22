package org.main.consensus;

import org.main.projectbase.CommunicationProtocol;

import java.util.logging.Logger;

public class EpochChange {

    static Logger logger = Logger.getLogger(String.valueOf(EpochChange.class));



    public void Handle(CommunicationProtocol.Message message) throws Exception {
        logger.info("[ep] handling message " + message.toString());
    }
}
