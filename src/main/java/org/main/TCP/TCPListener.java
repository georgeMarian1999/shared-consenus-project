package org.main.TCP;

import org.main.process.Process;
import org.main.projectbase.CommunicationProtocol;
import org.main.system.ApplicationSystem;
import org.main.utilities.Utilities;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.logging.Logger;

import static org.main.utilities.Utilities.findProcessId;

public class  TCPListener implements Runnable {

    final static Logger logger = Logger.getLogger(String.valueOf(TCPListener.class));

    private Process process;

    public TCPListener(Process process) {
        this.process = process;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(process.getPort())) {
            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());
                    int readSize = inputStream.readInt();
                    byte[] messageBytes = inputStream.readNBytes(readSize);
                    CommunicationProtocol.Message message = CommunicationProtocol.Message.parseFrom(messageBytes);
                    CommunicationProtocol.NetworkMessage networkMessage = message.getNetworkMessage();
                    CommunicationProtocol.Message innerMessage = networkMessage.getMessage();

                    if (innerMessage.getType() != CommunicationProtocol.Message.Type.EPFD_INTERNAL_HEARTBEAT_REPLY
                        && innerMessage.getType() != CommunicationProtocol.Message.Type.EPFD_INTERNAL_HEARTBEAT_REQUEST)
                        logger.info(String.format("%s received from %s:%s message %s \n", Thread.currentThread().getName(), networkMessage.getSenderHost(), networkMessage.getSenderListeningPort(), networkMessage.getMessage().getType()));


                    if (innerMessage.getType() == CommunicationProtocol.Message.Type.PROC_INITIALIZE_SYSTEM) {
                        CommunicationProtocol.ProcInitializeSystem procInitializeSystem = innerMessage.getProcInitializeSystem();
                        List<CommunicationProtocol.ProcessId> processes = procInitializeSystem.getProcessesList();
                        CommunicationProtocol.ProcessId currentProcessId = findProcessId(processes, process.getIndex(), process.getOwner());
                        process.setSystem(new ApplicationSystem(innerMessage.getSystemId(), processes, currentProcessId, process));
                    } else if (innerMessage.getType() == CommunicationProtocol.Message.Type.PROC_DESTROY_SYSTEM) {
                        if (message.getSystemId().equals(process.getSystem().getId())) {
                            process.getSystem().addMessage(message);
                            process.setSystem(null);
                        }
                    } else {
                        //logger.info(String.format("Got message with toAbstractionId %s", message.getToAbstractionId()));
                        if (message.getSystemId().equals(process.getSystem().getId())) {
                            process.getSystem().addMessage(message);
                        }
                    }
                } catch (Exception ex) {
                    logger.info(String.format("Process TCP Listener %s error: %s", Thread.currentThread().getName(), ex.getMessage()));
                    ex.printStackTrace();
                }
            }
        } catch (IOException ex) {
            logger.info(String.format("Process TCP Listener %s error: %s", Thread.currentThread().getName(), ex.getMessage()));
            ex.printStackTrace();
        }
    }

    private void processMessage(CommunicationProtocol.Message message) {

        switch (message.getType()) {
            case PROC_INITIALIZE_SYSTEM:
                CommunicationProtocol.ProcInitializeSystem procInitializeSystem = message.getProcInitializeSystem();
                List<CommunicationProtocol.ProcessId> processes = procInitializeSystem.getProcessesList();
                CommunicationProtocol.ProcessId currentProccess = findProcessId(processes, process.getIndex(), process.getOwner());
                process.setSystem(new ApplicationSystem(message.getSystemId(), processes, currentProccess, process));
                break;
            case PROC_DESTROY_SYSTEM:
                if (message.getSystemId().equals(process.getSystem().getId())) {
                    process.getSystem().addMessage(message);
                    process.setSystem(null);
                }
                break;
            default:
                if (message.getSystemId().equals(process.getSystem().getId())) {
                    logger.info(String.format("TCP Listener of process %s got message %s from %s", Thread.currentThread().getName(), message.getType(), message.getNetworkMessage().getSenderListeningPort()));
                    process.getSystem().addMessage(message);
                }
        }
    }
}
