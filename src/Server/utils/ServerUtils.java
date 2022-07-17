package Server.utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SocketChannel;

import Server.Configs.ServerSettings;

public class ServerUtils {

    // Array di boolean per flag se int è positivo, negativo, o se deve essere
    // diverso da zero

    public static void sendString(SocketChannel clientChannel, String msg) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
        System.out.println(msg.getBytes().length);
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

    // Questa funzione controlla se un int è positivo, negativo, o se deve essere
    // diverso da zero, in base al valore di flag
    /*public static boolean intToStringChecker(String str, BitSet bs) {
        int i;
        try {
            i = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return false;
        }
        if (bs.get(0) && i > 0) // controlla se è negativo
            return false;
        if (bs.get(1) && i == 0) // controlla se è diverso da zero
            return false;
        if (bs.get(2) && i < 0) // controlla se è positivo
            return false;
        return true;

    }*/

    public static String[] fixArray(String clientRequest) {
        String[] splitted = clientRequest.split("(?=\"[^\"].*\")");
        if (splitted.length == 1)
            splitted = clientRequest.split(" ");
        for (int i = 0; i < splitted.length; i++) {
            splitted[i] = splitted[i].replaceAll("\"", "");
            splitted[i] = splitted[i].trim();
        }
        return splitted;
    }

    public synchronized static void sendUDPMessage(String message) {
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName(ServerSettings.serverAddress);
            int port = ServerSettings.multicastPort;
            DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), address, port);
            socket.send(packet);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}