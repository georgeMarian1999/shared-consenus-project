package org.main.consensus;

import org.main.projectbase.CommunicationProtocol;

import java.util.logging.Logger;

public class EpochConsensus {

    static Logger logger = Logger.getLogger(String.valueOf(EpochConsensus.class));


    public EpochConsensus() {

    }

    public void Handle(CommunicationProtocol.Message message) throws Exception {
        logger.info("[ep] handling message " + message.toString());
    }
}
