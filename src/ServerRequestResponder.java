import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import Server.ServerManager;
import Server.utils.Message;

public class ServerRequestResponder implements Runnable {
   private final SocketChannel client;
   private final Selector selector = ServerManager.getSelector();
   private final Message message;

   public ServerRequestResponder(SocketChannel client, Message message) {
       this.client = client;
       this.message = message;
   }

   @Override
   public void run() {
       try {

           if (message.getMessageType() == Message.MESSAGE_TYPE.STRING) {
               sendString();
           }

           client.register(selector, SelectionKey.OP_READ);
           selector.wakeup();
       }catch (Exception e){
           e.printStackTrace();
           System.err.println("Impossibile inviare dati al Client " + e.getMessage());
           try {
               client.close();
           } catch (IOException ex) {
               ex.printStackTrace();
           }
       }
   }
   private void sendString() throws IOException {
      String msg = (String) message.getMessage();
      ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());

      sendInt(msg.length());

      while(buffer.hasRemaining())
          client.write(buffer);
  }

  private void sendInt(int n) throws IOException {
      ByteBuffer buffLen = ByteBuffer.allocate(4);
      IntBuffer view = buffLen.asIntBuffer();
      view.put(n);
      view.flip();
      while (buffLen.hasRemaining())
          client.write(buffLen);
  }
   
}
