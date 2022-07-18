package utils;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketException;

public class ClientMulticastThread implements Runnable {
   private MulticastSocket multicastSocket;

   public ClientMulticastThread(MulticastSocket multicastSocket) {
      this.multicastSocket = multicastSocket;
   }

   @Override
   public void run() {
      try {
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
            System.out.println("");
            System.out.println("< " + s);
            System.out.print("> ");

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