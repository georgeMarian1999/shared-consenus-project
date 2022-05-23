package org.main.utilities;


import org.main.projectbase.CommunicationProtocol;

import java.util.List;
import java.util.logging.Logger;

public class Utilities {

    static Logger logger = Logger.getLogger(String.valueOf(Utilities.class));


    public static String getParentAbstraction(String abstractionId) {
        return abstractionId.substring(0, abstractionId.lastIndexOf("."));
    }

    public static String getAbstractionIdNnarRegister(String abstractionId, String register) {
        return abstractionId + ".nnar[" + register + "]";
    }

    public static String getAbstractionIdUc(String abstractionId, String topic) {
        return abstractionId + ".uc[" + topic + "]";
    }

    public static String getAbstractionIdEp(String abstractionId, int timestamp) {
        return abstractionId + ".ep[" + timestamp + "]";
    }

    public static String getRegisterName(String abstractionId) {
        int openParenthesis = abstractionId.indexOf('[');
        int closeParenthesis = abstractionId.indexOf(']');
        return abstractionId.substring(openParenthesis + 1, closeParenthesis);
    }

    public static String getRegisterAbstraction(String abstractionId) {
        int closeParenthesis = abstractionId.indexOf(']');
        return abstractionId.substring(0, closeParenthesis + 1);
    }

    public static String getUniformConsensusAbstraction(String abstractionId) {
        int closeParenthesis = abstractionId.indexOf(']');
        return abstractionId.substring(0, closeParenthesis + 1);
    }

    public static CommunicationProtocol.ProcessId findProcessId(List<CommunicationProtocol.ProcessId> processIds, int index, String owner) {
        for (CommunicationProtocol.ProcessId processId: processIds
             ) {
            if (processId.getIndex() == index && processId.getOwner().equals(owner)) {
                return processId;
            }
        }
        return null;
    }
}

