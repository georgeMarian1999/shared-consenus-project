package org.main.consensus;

import org.main.projectbase.CommunicationProtocol;

import java.util.logging.Logger;

public class UniformConsensus {

    static Logger logger = Logger.getLogger(String.valueOf(UniformConsensus.class));


    public UniformConsensus() {

    }


    public void Handle(CommunicationProtocol.Message message) throws Exception {
        logger.info("[uc] handling message " + message.toString());
    }


}
