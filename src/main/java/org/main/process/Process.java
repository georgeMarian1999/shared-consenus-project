package org.main.process;

import org.main.TCP.TCP;
import org.main.TCP.TCPListener;
import org.main.projectbase.CommunicationProtocol;
import org.main.system.ApplicationSystem;
import org.main.utilities.Utilities;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

public class Process implements Runnable{

    final static Logger logger = Logger.getLogger(String.valueOf(Process.class));

    private Hub hub;

    private String address;

    private int port;

    private int index;

    private String owner;

    private ApplicationSystem system;

    public Process(Hub hub, String address, int port, int index, String owner) {
        this.hub = hub;
        this.address = address;
        this.port = port;
        this.index = index;
        this.owner = owner;
    }


    @Override
    public void run() {
        try {
            registerProcess();

            // Starting the TCP listener for this process
            logger.info(String.format("Starting the tcp listener for process with owner %s", owner));
            TCPListener tcpListener = new TCPListener(this);
            Thread tcpListenerThread = new Thread(tcpListener, Thread.currentThread().getName());
            tcpListenerThread.start();
            tcpListenerThread.join();
        } catch (IOException | InterruptedException exception) {
            logger.info(String.format("Process %s run error: %s", Thread.currentThread().getName(), exception.getMessage()));
            exception.printStackTrace();
        }
    }

    private void registerProcess() throws IOException {
        CommunicationProtocol.ProcRegistration processRegistration = CommunicationProtocol.ProcRegistration.newBuilder()
                .setOwner(owner)
                .setIndex(index)
                .build();

        CommunicationProtocol.Message registrationMessage = CommunicationProtocol.Message.newBuilder()
                .setType(CommunicationProtocol.Message.Type.PROC_REGISTRATION)
                .setProcRegistration(processRegistration)
                .setMessageUuid(String.valueOf(UUID.randomUUID()))
                .setToAbstractionId("app")
                .build();

        CommunicationProtocol.NetworkMessage networkMessage = CommunicationProtocol.NetworkMessage.newBuilder()
                .setSenderHost(address)
                .setSenderListeningPort(port)
                .setMessage(registrationMessage)
                .build();

        CommunicationProtocol.Message message = CommunicationProtocol.Message.newBuilder()
                .setType(CommunicationProtocol.Message.Type.NETWORK_MESSAGE)
                .setNetworkMessage(networkMessage)
                .setMessageUuid(String.valueOf(UUID.randomUUID()))
                .setToAbstractionId("app")
                .build();

        TCP.sendMessage(message, hub.getAddress(), hub.getPort());
    }

    public Hub getHub() {
        return hub;
    }

    public void setHub(Hub hub) {
        this.hub = hub;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public ApplicationSystem getSystem() {
        return system;
    }

    public void setSystem(ApplicationSystem system) {
        this.system = system;
    }
}
