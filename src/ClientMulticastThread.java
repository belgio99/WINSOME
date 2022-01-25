import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

public class ClientMulticastThread implements Runnable {
   private String address;
   private int port;
   private MulticastSocket multicastSocket;

   public ClientMulticastThread(String address, int port) {
      this.address = address;
      this.port = port;
   }

   @Override
   public void run() {
      try {
         multicastSocket = new MulticastSocket(port);
         multicastSocket.joinGroup(InetAddress.getByName(address));
         multicastSocket.setSoTimeout(0);
         DatagramPacket p = new DatagramPacket(new byte[1024], 1024);
         while (Thread.currentThread().isInterrupted()) {
            try {
               multicastSocket.receive(p);
            }
            catch (SocketException e) {
               break;
            }
            if (multicastSocket.isClosed()) break;
            String s = new String(p.getData(), 0, p.getLength());
            System.out.println(s);

         }

      }
      catch (Exception e) {
         e.printStackTrace();
      }

   }

  public void close() {
      multicastSocket.close();
  }


}