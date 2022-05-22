package org.main.consensus;

import org.main.projectbase.CommunicationProtocol;

import java.util.logging.Logger;

public class EventuallyPerfectFailureDetector {

    static Logger logger = Logger.getLogger(String.valueOf(EventuallyPerfectFailureDetector.class));


    public EventuallyPerfectFailureDetector() {

    }

    public void Handle(CommunicationProtocol.Message message) throws Exception  {
        logger.info("[epfd] handling message " + message.toString());
    }
}
