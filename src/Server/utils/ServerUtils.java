package Server.utils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SocketChannel;
import java.util.BitSet;

import Server.ServerManager;

public class ServerUtils {

    //Array di boolean per flag se int è positivo, negativo, o se deve essere diverso da zero


    public static void sendString(SocketChannel clientChannel, String msg) throws IOException{
        ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());

        sendInt(clientChannel, msg.getBytes().length);
        while(buffer.hasRemaining())
            clientChannel.write(buffer);

  }

  public static void sendInt(SocketChannel clientChannel ,int n) throws IOException {
      ByteBuffer buffLen = ByteBuffer.allocate(4);
      IntBuffer view = buffLen.asIntBuffer();
      view.put(n);
      view.flip();
      while (buffLen.hasRemaining())
          clientChannel.write(buffLen);

      
      }
      public static boolean intToStringChecker(String str, BitSet bs) {
          int i;
        try {
            i = Integer.parseInt(str);
        }
        catch (NumberFormatException e) {
            return false;
        }
        if (bs.get(0) && i>0) //controlla se è negativo
            return false;
        if (bs.get(1) && i == 0) //controlla se è diverso da zero
            return false;
        if (bs.get(2) && i<0) //controlla se è positivo
            return false;
        return true;


  }
    public static boolean stringChecker(String str) {
        if (str.isEmpty() || str.trim().length()==0 || str.isBlank())
            return true;
        return false;
    }
    public static String[] fixArray(String clientRequest) {
        String[] splitted = clientRequest.split("(?=\"[^\"].*\")");
        if (splitted.length==1)
           splitted = clientRequest.split(" ");
        for (int i=0;i<splitted.length;i++) {
           splitted[i] = splitted[i].replaceAll("\"", "");
           splitted[i] = splitted[i].trim();
        }
        return splitted;
     }
}
