

import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;
import java.util.Properties;

public class ServerSettings {

   public static String serverAddress;
   public static int TCPPort;

   public static String multicastAddress;
   public static int multicastPort;

   public static String RMIAddress;
   public static int RMIPort;
   public static String RMIName;

   public static String storagePath;

   public static String rewardDelayTime;
   public static float authorPercentage;

   public static String autoSaveTime;

   
   static {
      String path = "./serverConfig.txt";
      Properties settings = new Properties();
      File configFile = new File(path);
      try {
         if (configFile.exists()) {
         settings.load(new FileReader(path));
         serverAddress = (String)settings.getOrDefault("SERVER", "localhost");
         TCPPort = Integer.parseInt(String.valueOf(settings.getOrDefault("TCPPORT", 50000)));
         TCPPort = checkValidPort(TCPPort, 50000);

         multicastAddress = (String)settings.getOrDefault("MCASTADDR", "239.255.32.32");
         multicastPort = Integer.parseInt(String.valueOf(settings.getOrDefault("MCASTPORT", 50001)));
         multicastPort = checkValidPort(multicastPort, 50001);

         RMIAddress = (String)settings.getOrDefault("RMIADDR", "localhost");
         RMIPort = Integer.parseInt(String.valueOf(settings.getOrDefault("RMIPORT", 50003)));
         RMIPort = checkValidPort(RMIPort, 50002);

         RMIName = (String)settings.getOrDefault("RMINAME", "Winsome");

         authorPercentage = Integer.parseInt(String.valueOf(settings.getOrDefault("AUTHORPERCENT", 50)));
        
         storagePath = (String) settings.getOrDefault("STORAGEPATH", "tmp");
         rewardDelayTime = (String) settings.getOrDefault("REWARDINTERVAL", -1);       
         
         autoSaveTime = (String) settings.getOrDefault("AUTOSAVE", "30s");
         System.out.println("Impostazioni caricate dal file di configurazione");
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
   }
   }
   

   private static void setDefaults() {
      serverAddress = InetAddress.getLoopbackAddress().toString();
      TCPPort = 50000;
      
      multicastAddress = "239.255.32.32";
      multicastPort = 50001;

      RMIAddress = InetAddress.getLoopbackAddress().toString();
      RMIPort = 50002;
      RMIName = "ServerRemoteInterface";

      authorPercentage = 50;
      storagePath = "tmp";
      rewardDelayTime = "30s";
      autoSaveTime = "30s";
   }

   private static int checkValidPort(int port, int defaultValue) {
      if (port < 0) {
         System.out.println("Numero di porta non valida, verrà usato il valore di default");
         return defaultValue;
      }
      if (port == 8080) System.err.println("Errore: la porta 8080 è riservata per l invio dei Post e delle transazioni");
      return port == 8080 ? defaultValue : port;
  }
}
