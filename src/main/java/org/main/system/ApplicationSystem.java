package org.main.system;

import org.main.abstraction.Abstraction;
import org.main.application.App;
import org.main.broadcast.BestEffortBroadcast;
import org.main.perfectlink.PerfectLink;
import org.main.process.Hub;
import org.main.process.Process;
import org.main.projectbase.CommunicationProtocol;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class ApplicationSystem {

    final static Logger logger = Logger.getLogger(String.valueOf(ApplicationSystem.class));

    private String id;
    private Process process;
    private CommunicationProtocol.ProcessId currentProcessId;
    private List<CommunicationProtocol.ProcessId> processIds;
    private BlockingQueue<CommunicationProtocol.Message> queue = new LinkedBlockingQueue<>();
    private MessageProcessor messageProcessor;
    private List<Abstraction> abstractions = new CopyOnWriteArrayList<>();

    public ApplicationSystem(String id, List<CommunicationProtocol.ProcessId> processIds, CommunicationProtocol.ProcessId currentProcessId, Process process) {
        this.id = id;
        this.process = process;
        this.currentProcessId = currentProcessId;
        this.processIds = processIds;

        // Starting the message processor

        messageProcessor = new MessageProcessor(this);
        Thread messageProcessorThread = new Thread(messageProcessor, Thread.currentThread().getName());
        messageProcessorThread.start();


        // Adding the system abstractions
        abstractions.add(new App("app", this));
        abstractions.add(new BestEffortBroadcast("app.beb", this));
        abstractions.add(new PerfectLink("app.pl", this));
        abstractions.add(new PerfectLink("app.beb.pl", this));
    }

    public void addMessage(CommunicationProtocol.Message message) {
        queue.add(message);
    }

    public CommunicationProtocol.Message getMessage() throws InterruptedException {
        return queue.take();
    }

    public void addAbstraction(Abstraction abstraction) {
        abstractions.add(abstraction);
    }

    public boolean isAbstraction(String abstractionId) {
        for (Abstraction abs: abstractions
             ) {
            if (abs.getAbstractionId().equals(abstractionId)) {
                return true;
            }
        }
        return false;
    }

    public CommunicationProtocol.ProcessId getSenderProcessId(String host, int port) {
        Hub hub = process.getHub();
        if (Objects.equals(hub.getAddress(), host) && hub.getPort() == port) {
            return hub.getProcessId();
        }
        for (CommunicationProtocol.ProcessId processId: processIds
             ) {
            if (processId.getHost().equals(host) && processId.getPort() == port) {
                return processId;
            }
        }
        return null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public CommunicationProtocol.ProcessId getCurrentProcessId() {
        return currentProcessId;
    }

    public void setCurrentProcessId(CommunicationProtocol.ProcessId currentProcessId) {
        this.currentProcessId = currentProcessId;
    }

    public List<CommunicationProtocol.ProcessId> getProcessIds() {
        return processIds;
    }

    public void setProcessIds(List<CommunicationProtocol.ProcessId> processIds) {
        this.processIds = processIds;
    }

    public BlockingQueue<CommunicationProtocol.Message> getQueue() {
        return queue;
    }

    public void setQueue(BlockingQueue<CommunicationProtocol.Message> queue) {
        this.queue = queue;
    }

    public List<Abstraction> getAbstractions() {
        return abstractions;
    }

    public void setAbstractions(List<Abstraction> abstractions) {
        this.abstractions = abstractions;
    }
}
