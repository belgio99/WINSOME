import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public class ClientMain {
    public static final String serverName = "localhost";
    public static final int port = 10000;
    public static void main(String[] args) throws Exception {
        try {
        Socket socket  = new Socket();
        socket.connect(new InetSocketAddress(serverName, port));
        System.out.println("Sto provando a connettermi al server");
        System.out.println("Server connesso" + socket.getInetAddress() + socket.getLocalAddress() + socket.getLocalPort());
        socket.close();
        }
        catch (UnknownHostException e) {
            System.err.println("Non ho trovato l'host a cui connettermi!");
        }
        catch (IOException e) {
            System.err.println("non trovo il server!");
        }
    }
}
