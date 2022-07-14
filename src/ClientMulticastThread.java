import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ClientMulticastThread implements Runnable {
   private String address;
   private int port;
   private MulticastSocket multicastSocket;
   private NetworkInterface networkInterface;

   public ClientMulticastThread(String address, int port) {
      this.address = address;
      this.port = port;
      try {
      this.networkInterface = NetworkInterface.getByInetAddress(InetAddress.getByName(InetAddress.getLocalHost().getHostAddress()));
      }
      catch (SocketException | UnknownHostException e) {
         e.printStackTrace();
      }
   }

   @Override
   public void run() {
      try {
         System.out.println("Avvio thread client multicast...");
         multicastSocket = new MulticastSocket(port);
         multicastSocket.setSoTimeout(0);
         multicastSocket.setReuseAddress(true);
         multicastSocket.joinGroup(new InetSocketAddress(address, port),networkInterface);
         DatagramPacket p = new DatagramPacket(new byte[1024], 1024);
         while (!Thread.currentThread().isInterrupted()) {
            try {
               multicastSocket.receive(p);
            }
            catch (SocketException e) {
               e.printStackTrace();
               return;
            }
            String s = new String(p.getData(), 0, p.getLength());
            System.out.println(s);

         }
         System.out.println("Chiudo thread client multicast...");
         multicastSocket.close();
      }
      catch (Exception e) {
         e.printStackTrace();
      }

   }

  /*public void close() {
      multicastSocket.close();
  }*/


}