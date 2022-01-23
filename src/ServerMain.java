import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.rmi.AlreadyBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Server.ServerManager;
import Server.Configs.DefaultValues;
import Server.RMI.RegistrationService;
import Server.utils.Message;

public class ServerMain {

   
   
   private static ByteBuffer buffer;
   private static final int port = 10000;
   public static ServerSocketChannel serverSocketChannel;

   public static void main(String[] args) {
      System.out.println("Avvio server...");
      ExecutorService threadPool = Executors.newCachedThreadPool();
      Selector selector = ServerManager.getSelector();
      try {
         serverSocketChannel = ServerSocketChannel.open();
         serverSocketChannel.configureBlocking(false);
         serverSocketChannel.socket().bind(new InetSocketAddress(DefaultValues.serverval.TCPPort));
         
         serverSocketChannel.configureBlocking(false);
         serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
         buffer = ByteBuffer.allocate(1024);
         String multicastAddress = DefaultValues.serverval.multicastAddress + ":" + DefaultValues.serverval.multicastPort;
         ByteBuffer multicastBuffer = ByteBuffer.wrap(multicastAddress.getBytes());
         
         RegistrationService regService = new RegistrationService();
         Registry r1 = LocateRegistry.createRegistry(DefaultValues.serverval.RMIPort);
         r1.bind(DefaultValues.serverval.RMIName, regService);

         //Registry r2 = LocateRegistry.createRegistry(DefaultValues.client.RMIPort);
         //Mettere il follow service

         



         
      } catch (IOException e) {
         System.err.println("Errore di I/O! Impossibile avviare il server!");
         System.exit(1);
      }
       catch (AlreadyBoundException e) {
          System.err.println("Errore! non torna");
          e.printStackTrace();
          System.exit(1);
       }
      System.out.println("Server pronto");
      try {
         System.out.println("Attesa nel select...");
      while (selector.select() != 0) {
         System.out.println("Ricevuta nuova chiave!");
            Set<SelectionKey> selectedKS = selector.selectedKeys();
            for (SelectionKey key : selectedKS) {
               System.out.println("La chiave è di tipo" + key.toString());
               if (key.isAcceptable()) {
                  SocketChannel connectedClient = serverSocketChannel.accept();
                  connectedClient.configureBlocking(false);
                  connectedClient.register(selector, SelectionKey.OP_READ);
                  System.out.println("Nuovo client connesso a " + connectedClient.getRemoteAddress().toString());
               } else if (key.isReadable()) {
                  SocketChannel client = (SocketChannel) key.channel();
                  ServerRequestHandler request = new ServerRequestHandler(client,selector);
                  key.cancel();
                  updateKeySet(selector);
                  threadPool.submit(new Thread(request));
                  //int command = readCommand(client, buffer);
               } else if (key.isWritable()) {
                  SocketChannel client = (SocketChannel) key.channel();
                  Message response = (Message) key.attachment();
                  ServerRequestResponder writer = new ServerRequestResponder(client, response);
                  key.cancel();
                  updateKeySet(selector);
                  threadPool.submit(new Thread(writer));
                  // Invio la risposta
               }

               selectedKS.remove(key);
            }
            System.out.println("Torno al select...");
         }
         System.out.println("Esco!");
         selector.close();
         serverSocketChannel.close();
      }
      
         catch (IOException e) {
            System.err.println("Errore di I/O!");
            //continue;
         }
      }
      private static void updateKeySet(Selector selector) {
         try {
             selector.selectNow();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
      
   

   /*public static int readCommand(SocketChannel client, ByteBuffer buffer) {
      try {
         Commands command = receive(client, buffer).valueOf(Commands);
         //String command = receive(client,buffer);
         if (command == null)
            return -1;
         switch (command) {
            case login:
            case register:
            case logout:
            default: break;

         }

      }
      catch 

   }
   private static String receive(SocketChannel client, ByteBuffer buffer) throws IOException {
      buffer.clear();
      int length;
      StringBuilder string = new StringBuilder();
      while((length = client.read(buffer)) > 0){ 
          string.append(new String(buffer.array(), 0, length));
      }
      if (length == -1) { //In caso di lunghezza non valida
          System.err.println("Errore durante la ricezione!");
          throw new IOException();
      }
      return string.toString();
  }
  */
}
