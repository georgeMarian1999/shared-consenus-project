package org.main;


import org.main.consensus.EventuallyPerfectFailureDetector;
import org.main.projectbase.CommunicationProtocol;

public class Main {
    public static void main(String[] args) throws Exception {

        CommunicationProtocol.Message message = CommunicationProtocol.Message.newBuilder().setMessageUuid("4").build();

        EventuallyPerfectFailureDetector epfd = new EventuallyPerfectFailureDetector();


    }
}