package org.main.consensus;

import org.main.projectbase.CommunicationProtocol;

import java.util.logging.Logger;

public class EventualLeaderDetector {

    static Logger logger = Logger.getLogger(String.valueOf(EventualLeaderDetector.class));

    public EventualLeaderDetector() {
    }


    public void Handle(CommunicationProtocol.Message message) throws Exception {
        logger.info("[eld] handling message " + message.toString());
    }

}
