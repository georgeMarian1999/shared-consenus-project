package org.main.abstraction;

import org.main.projectbase.CommunicationProtocol;
import org.main.system.ApplicationSystem;

import java.io.IOException;

public abstract class Abstraction {
    protected String abstractionId;
    protected ApplicationSystem system;

    public Abstraction(String abstractionId, ApplicationSystem system) {
        this.abstractionId = abstractionId;
        this.system = system;
    }

    public abstract String handleMessage(CommunicationProtocol.Message message) throws IOException;

    public String getAbstractionId() {
        return abstractionId;
    }

    public void setAbstractionId(String abstractionId) {
        this.abstractionId = abstractionId;
    }

    public ApplicationSystem getSystem() {
        return system;
    }

    public void setSystem(ApplicationSystem system) {
        this.system = system;
    }


}
