package Server.Configs;

import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;
import java.util.Properties;

public class ServerSettings {

   public static String serverAddress;
   public int TCPPort;
   public int UDPPort;

   public String multicastAddress;
   public int multicastPort;

   public String RMIAddress;
   public int RMIPort;
   public String RMIName;

   public int socketTimeout;
   public String storagePath;

   public int rewardDelayTime;
   public float authorPercentage;
   public int cacheSize;

   public int columnSize;
   
   
   

   public ServerSettings(String path) {
      Properties settings = new Properties();
      File configFile = new File(path);
      try {
         if (configFile.exists()) {
         settings.load(new FileReader(path));
         serverAddress = (String)settings.getOrDefault("serverAddress", InetAddress.getLoopbackAddress().toString());
         TCPPort = Integer.parseInt(String.valueOf(settings.getOrDefault("TCPPort", 50000)));
         TCPPort = checkValidPort(TCPPort, 50000);

         UDPPort = Integer.parseInt(String.valueOf(settings.getOrDefault("UDPPort", 50001)));
         UDPPort = checkValidPort(UDPPort, 50001);

         multicastAddress = (String)settings.getOrDefault("multicastAddress", "239.255.32.32");
         multicastPort = Integer.parseInt(String.valueOf(settings.getOrDefault("multicastPort", 50002)));
         multicastPort = checkValidPort(multicastPort, 50002);

         RMIAddress = (String)settings.getOrDefault("RMIAddress", InetAddress.getLoopbackAddress().toString());
         RMIPort = Integer.parseInt(String.valueOf(settings.getOrDefault("RMIPort", 50003)));
         RMIPort = checkValidPort(RMIPort, 50003);

         authorPercentage = Integer.parseInt(String.valueOf(settings.getOrDefault("authorPercentage", 50)));
         socketTimeout = Integer.parseInt(String.valueOf(settings.getOrDefault("socketTimeout", -1)));
         cacheSize = Integer.parseInt(String.valueOf(settings.getOrDefault("cacheSize", 10)));
         storagePath = (String) settings.getOrDefault("databasePath", "/tmp");
         rewardDelayTime = Integer.parseInt((String) settings.getOrDefault("rewardDelayTime", -1));
         columnSize = Integer.parseInt((String) settings.getOrDefault("columnSize", 10));
         }
         else {
            System.out.println("File di configurazione non trovato, uso valori di default");
            setDefaults();
         }
   }
   catch (Exception e)
   {
      System.out.println("Errore nel file di configurazione");
      setDefaults();
      return;
   }
   return;
}
   private void setDefaults() {
      serverAddress = InetAddress.getLoopbackAddress().toString();
      TCPPort = 50000;
      UDPPort = 50001;
      
      multicastAddress = "239.255.32.32";
      multicastPort = 50002;

      RMIAddress = InetAddress.getLoopbackAddress().toString();
      RMIPort = 50003;
      RMIName = "ServerRemoteInterface";

      authorPercentage = 50;
      socketTimeout = -1;
      cacheSize = 10;
      storagePath = "/tmp";
      rewardDelayTime = -1;
      columnSize = 10;
   }

   private int checkValidPort(int port, int defaultValue) {
      if (port < 0) {
         System.out.println("Numero di porta non valida, verrà usato il valore di default");
         return defaultValue;
      }
      if (port == 8080) System.err.println("Errore: la porta 8080 è riservata per l invio dei Post e delle transazioni");
      return port == 8080 ? defaultValue : port;
  }
}
