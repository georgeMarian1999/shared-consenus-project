package org.main.process;

import org.main.projectbase.CommunicationProtocol;

public class Hub {

    private String address;

    private int port;

    private CommunicationProtocol.ProcessId processId;

    public Hub(String address, int port) {
        this.address = address;
        this.port = port;
        this.processId = CommunicationProtocol.ProcessId
                .newBuilder()
                .setHost(address)
                .setPort(port)
                .setOwner("hub")
                .build();
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

    public CommunicationProtocol.ProcessId getProcessId() {
        return processId;
    }

    public void setProcessId(CommunicationProtocol.ProcessId processId) {
        this.processId = processId;
    }
}
