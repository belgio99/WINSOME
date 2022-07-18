package winsome.utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SocketChannel;

import winsome.Configs.ServerSettings;

public class ServerUtils {

    public static void sendString(SocketChannel clientChannel, String msg) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
        sendInt(clientChannel, msg.getBytes().length);
        while (buffer.hasRemaining())
            clientChannel.write(buffer);
    }

    public static void sendInt(SocketChannel clientChannel, int n) throws IOException {
        ByteBuffer buffLen = ByteBuffer.allocate(4);
        IntBuffer view = buffLen.asIntBuffer();
        view.put(n);
        view.flip();
        while (buffLen.hasRemaining())
            clientChannel.write(buffLen);

    }

    public static String[] fixArray(String clientRequest) {
        String[] split = clientRequest.split("(?=\"[^\"].*\")");
        if (split.length == 1)
            split = clientRequest.split(" ");
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].replaceAll("\"", "");
            split[i] = split[i].trim();
        }
        return split;
    }

    public synchronized static void sendUDPMessage(String message) {
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName(ServerSettings.multicastAddress);
            int port = ServerSettings.multicastPort;
            DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), address, port);
            socket.send(packet);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}