import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

public class ServerMain {

   private static Selector selector;
   private static ServerSocketChannel serverSocketChannel;
   private static ByteBuffer buffer;
   private static final int port = 10000;

   public static void main(String[] args) {
      System.out.println("Avvio server...");
      try {
         serverSocketChannel = ServerSocketChannel.open();
         serverSocketChannel.socket().bind(new InetSocketAddress(port));
         serverSocketChannel.configureBlocking(false);
         selector = Selector.open();
         serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
         buffer = ByteBuffer.allocate(1024);
      } catch (IOException e) {
         System.err.println("Errore di I/O! Impossibile avviare il server!");
         System.exit(1);
      }
      System.out.println("Server pronto");
      while (selector.select() != 0) {
         try {
            Set<SelectionKey> selectedKS = selector.selectedKeys();
            for (SelectionKey key : selectedKS) {
               if (key.isAcceptable()) {
                  SocketChannel connectedClient = serverSocketChannel.accept();
                  connectedClient.configureBlocking(false);
                  connectedClient.register(selector, SelectionKey.OP_READ);
                  System.out.println("Nuovo client connesso a " + connectedClient.getRemoteAddress().toString());
               } else if (key.isReadable()) {
                  SocketChannel client = (SocketChannel) key.channel();
                  int command = readCommand(client, buffer);
               } else if (key.isWritable()) {
                  SocketChannel client = (SocketChannel) key.channel();
                  String request = (String) key.attachment();
                  // Invio la risposta
               }

               selectedKS.remove(key);
            }
         }
         catch (IOException e) {
            System.err.println("Errore di I/O!");
            continue;
         }

         //selector.close();
         //serverSocketChannel.close();
      }
   }

   public static int readCommand(SocketChannel client, ByteBuffer buffer) {
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
}
