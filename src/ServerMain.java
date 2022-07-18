
import java.io.IOException;
import java.net.InetSocketAddress;
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

import RMI.RemoteService;
import settings.ServerSettings;
import utils.ServerManager;
import utils.ServerRequestHandler;
import utils.ServerUtils;

public class ServerMain {

   public static ServerSocketChannel serverSocketChannel;
   public static ServerSettings settings;

   public static void main(String[] args) {
      System.out.println("Avvio server...");
      ExecutorService threadPool = Executors.newCachedThreadPool();
      Selector selector = ServerManager.getSelector();
      ConcurrentLinkedQueue<SocketChannel> registerQueue = new ConcurrentLinkedQueue<>();
      try {
         serverSocketChannel = ServerSocketChannel.open();
         serverSocketChannel.configureBlocking(false);
         serverSocketChannel.socket().bind(new InetSocketAddress(ServerSettings.TCPPort));

         serverSocketChannel.configureBlocking(false);
         serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
         RemoteService regService = new RemoteService();
         Registry r1 = LocateRegistry.createRegistry(ServerSettings.RMIPort);
         r1.bind(ServerSettings.RMIName, regService);

         Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
               Selector selector = ServerManager.getSelector();

               try {
                  selector.close();
                  serverSocketChannel.close();
                  UnicastRemoteObject.unexportObject(regService, false);
                  r1.unbind(ServerSettings.RMIName);
                  threadPool.shutdown();
                  ServerManager.shutdown();
                  ServerManager.saveServerState();

               } catch (Exception e) {
                  Thread.currentThread().interrupt();
                  e.printStackTrace();
                  System.exit(1);
               }
            }
         });
      } catch (IOException e) {
         System.err.println("Errore di I/O! Impossibile avviare il server!");
         System.exit(1);
      } catch (AlreadyBoundException e) {
         System.err.println("Errore! L'indirizzo è già stato utilizzato!");
         System.exit(1);
      }
      System.out.println("Server pronto");
      try {
         while (true) {
            while (selector.select() == 0) {
               while (!registerQueue.isEmpty())
                  registerQueue.poll().register(selector, SelectionKey.OP_READ); //prendo il primo elemento della coda e lo registro nel selector
               continue;
            }
            Set<SelectionKey> selectedKS = selector.selectedKeys();
            for (SelectionKey key : selectedKS) {
               if (key.isAcceptable()) {
                  SocketChannel clientChannel = serverSocketChannel.accept();
                  clientChannel.configureBlocking(false);
                  clientChannel.register(selector, SelectionKey.OP_READ);
                  System.out.println("Nuovo client connesso a " + clientChannel.getRemoteAddress().toString());
                  ServerUtils.sendString(clientChannel, ServerSettings.RMIAddress + ";" + ServerSettings.RMIPort + ";"
                  + ServerSettings.RMIName); //invio i dettagli RMI del server al client
                  ServerUtils.sendString(clientChannel,
                        ServerSettings.multicastAddress + ":" + ServerSettings.multicastPort); //invio i dettagli multicast del server al client
               } else if (key.isReadable()) {
                  SocketChannel clientChannel = (SocketChannel) key.channel();
                  ServerRequestHandler request = new ServerRequestHandler(clientChannel, selector, registerQueue); //creo una nuova istanza di ServerRequestHandler per gestire la richiesta del client
                  key.cancel();
                  threadPool.submit(new Thread(request)); //invio la richiesta al thread pool
               }
               selectedKS.remove(key);
            }
         }

      }

      catch (IOException e) {
         System.err.println("Errore di I/O!");
      } catch (ClosedSelectorException e) {
         System.out.println("Spegnimento del server...");
      }
   }

}
