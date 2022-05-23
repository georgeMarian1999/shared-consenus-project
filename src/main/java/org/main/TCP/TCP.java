package org.main.TCP;

import org.main.projectbase.CommunicationProtocol;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Logger;

public class TCP {

    static Logger logger = Logger.getLogger(String.valueOf(TCP.class));


    public static void sendMessage(CommunicationProtocol.Message message, String address, int port) throws IOException {
        try (Socket socket = new Socket(InetAddress.getByName(address), port)) {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            byte[] messageBytes = message.toByteArray();
            out.writeInt(messageBytes.length);
            out.write(messageBytes);
            if (message.getNetworkMessage().getMessage().getType() != CommunicationProtocol.Message.Type.EPFD_INTERNAL_HEARTBEAT_REQUEST
                    && message.getNetworkMessage().getMessage().getType() != CommunicationProtocol.Message.Type.EPFD_INTERNAL_HEARTBEAT_REPLY) {
                logger.info(String.format("%s sent to %s:%s message %s", Thread.currentThread().getName(), address, port, message.getNetworkMessage().getMessage().getType()));
            }
        }
    }

}
