package org.main.register;

import org.main.projectbase.CommunicationProtocol;

import java.util.logging.Logger;

public class NNARObject {

    static final Logger logger = Logger.getLogger(String.valueOf(NNARObject.class));

    private int ts;
    private int wr;
    private CommunicationProtocol.Value val;

    public NNARObject() {
        this.ts = 0;
        this.wr = 0;
        this.val = CommunicationProtocol.Value.newBuilder()
                .setV(0)
                .setDefined(false)
                .build();
    }

    public NNARObject(int ts, int wr, CommunicationProtocol.Value val) {
        this.ts = ts;
        this.wr = wr;
        this.val = val;
    }

    public int getTs() {
        return ts;
    }

    public void setTs(int ts) {
        this.ts = ts;
    }

    public int getWr() {
        return wr;
    }

    public void setWr(int wr) {
        this.wr = wr;
    }

    public CommunicationProtocol.Value getVal() {
        return val;
    }

    public void setVal(CommunicationProtocol.Value val) {
        this.val = val;
    }
}
