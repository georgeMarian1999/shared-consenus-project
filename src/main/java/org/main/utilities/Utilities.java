package org.main.utilities;


import org.main.projectbase.CommunicationProtocol;

import java.util.List;
import java.util.logging.Logger;

public class Utilities {

    static Logger logger = Logger.getLogger(String.valueOf(Utilities.class));


    public static String getParentAbstraction(String abstractionId) {
        int lastIndex = abstractionId.lastIndexOf(".");
        if (lastIndex == -1) {
            lastIndex = abstractionId.length();
        }
        return abstractionId.substring(0, lastIndex);
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
        return processIds.stream().filter(p -> p.getOwner().equals(owner) && p.getIndex() == index).findFirst().orElse(null);
    }
}

