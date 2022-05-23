package org.main;


import org.main.consensus.EventuallyPerfectFailureDetector;
import org.main.process.Hub;
import org.main.process.Process;
import org.main.projectbase.CommunicationProtocol;

import java.util.logging.Logger;

public class Main {

    final static Logger logger = Logger.getLogger(String.valueOf(Main.class));

    public static void main(String[] args) throws Exception {

        try {
//            String hubHost = args[0];
//            int hubPort = Integer.parseInt(args[1]);
//            String processHost = args[3];
//            int firstProcessPort = Integer.parseInt(args[3]);
//            int secondProcessPort = Integer.parseInt(args[4]);
//            int thirdProcessPort = Integer.parseInt(args[5]);
//            String owner = args[6];

            String hubHost = "127.0.0.1";
            int hubPort = 5000;
            String processHost = "127.0.0.1";
            int firstProcessPort = 6001;
            int secondProcessPort = 6002;
            int thirdProcessPort = 6003;
            String owner = "george";
            Hub hub = new Hub(hubHost, hubPort);

            Thread process1 = new Thread(new Process(hub, processHost, firstProcessPort, 1, owner), owner + "_1");
            Thread process2 = new Thread(new Process(hub, processHost, secondProcessPort, 2, owner), owner + "_2");
            Thread process3 = new Thread(new Process(hub, processHost, thirdProcessPort, 3, owner), owner + "_3");

            process1.start();
            process2.start();
            process3.start();

            process1.join();
            process2.join();
            process3.join();

        } catch (Exception ex) {
            logger.info(String.format("%s process error", Thread.currentThread().getName()));
            ex.printStackTrace();
        }

    }
}