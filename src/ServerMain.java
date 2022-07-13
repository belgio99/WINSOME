import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.rmi.AlreadyBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Server.ServerManager;
import Server.Configs.Settings;
import Server.RMI.Registration.RemoteService;

public class ServerMain {

   // private static ByteBuffer buffer;
   // private static final int port = 10000;
   public static ServerSocketChannel serverSocketChannel;

   public static void main(String[] args) {

      System.out.println("Avvio server...");
      ExecutorService threadPool = Executors.newCachedThreadPool();
      Selector selector = ServerManager.getSelector();
      ConcurrentLinkedQueue<SocketChannel> registerQueue = new ConcurrentLinkedQueue<>();
      ServerManager.startupServer();

      try {
         serverSocketChannel = ServerSocketChannel.open();
         serverSocketChannel.configureBlocking(false);
         serverSocketChannel.socket().bind(new InetSocketAddress(Settings.serverSettings.TCPPort));
         
         serverSocketChannel.configureBlocking(false);
         serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
         //buffer = ByteBuffer.allocate(1024);
         String multicastAddress = Settings.serverSettings.multicastAddress + ":" + Settings.serverSettings.multicastPort;
         ByteBuffer multicastBuffer = ByteBuffer.wrap(multicastAddress.getBytes());
         
         RemoteService regService = new RemoteService();
         Registry r1 = LocateRegistry.createRegistry(Settings.serverSettings.RMIPort);
         r1.bind(Settings.serverSettings.RMIName, regService);

         //Registry r2 = LocateRegistry.createRegistry(DefaultValues.client.RMIPort);
         //Mettere il follow service

         Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
               Selector selector = ServerManager.getSelector();
               
               try {
                  selector.close();
                  serverSocketChannel.close();
                  UnicastRemoteObject.unexportObject(regService, false);
                  r1.unbind(Settings.serverSettings.RMIName);
                  Thread.sleep(200);
                  ServerManager.shutdownServer();
    
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }
        });



         
      } catch (IOException e) {
         System.err.println("Errore di I/O! Impossibile avviare il server!");
         System.exit(1);
      }
       catch (AlreadyBoundException e) {
          System.err.println("Errore! L'indirizzo è già stato utilizzato!");
          System.exit(1);
       }
      System.out.println("Server pronto");
      try {
         System.out.println("Attesa nel select...");
      while (true) {
         while (selector.select()==0) {
            while (!registerQueue.isEmpty())
               registerQueue.poll().register(selector, SelectionKey.OP_READ);
            continue;
         }
         System.out.println("Ricevuta nuova chiave!");
            Set<SelectionKey> selectedKS = selector.selectedKeys();
            for (SelectionKey key : selectedKS) {
               //System.out.println("La chiave è di tipo" + key.toString());
               if (key.isAcceptable()) {
                  System.out.println("La chiave è di tipo ACCEPT");
                  SocketChannel connectedClient = serverSocketChannel.accept();
                  connectedClient.configureBlocking(false);
                  connectedClient.register(selector, SelectionKey.OP_READ);
                  System.out.println("Nuovo client connesso a " + connectedClient.getRemoteAddress().toString());
               } else if (key.isReadable()) {
                  System.out.println("La chiave è di tipo READ");
                  SocketChannel client = (SocketChannel) key.channel();
                  ServerRequestHandler request = new ServerRequestHandler(client,selector, registerQueue);
                  key.cancel();
                  updateKeySet(selector);
                  threadPool.submit(new Thread(request));
                  //int command = readCommand(client, buffer);
               } /*else if (key.isWritable()) {
                  SocketChannel client = (SocketChannel) key.channel();
                  Message response = (Message) key.attachment();
                  ServerRequestResponder writer = new ServerRequestResponder(client, response);
                  key.cancel();
                  updateKeySet(selector);
                  threadPool.submit(new Thread(writer));
                  // Invio la risposta
               }*/

               selectedKS.remove(key);
            }
            System.out.println("Torno al select...");
         }
         
      }
      
         catch (IOException e) {
            System.err.println("Errore di I/O!");
            //continue;
         } catch (ClosedSelectorException e) {
            System.out.println("Spegnimento del server...");
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
