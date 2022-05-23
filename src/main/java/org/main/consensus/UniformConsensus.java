package org.main.consensus;

import org.main.abstraction.Abstraction;
import org.main.projectbase.CommunicationProtocol;
import org.main.system.ApplicationSystem;

import java.io.IOException;
import java.util.logging.Logger;

public class UniformConsensus extends Abstraction {

    static Logger logger = Logger.getLogger(String.valueOf(UniformConsensus.class));


    public UniformConsensus(String abstractionId, ApplicationSystem system) {
        super(abstractionId, system);
    }


    public void Handle(CommunicationProtocol.Message message) throws Exception {
        logger.info("[uc] handling message " + message.toString());
    }


    @Override
    public String handleMessage(CommunicationProtocol.Message message) throws IOException {
        return null;
    }
}
