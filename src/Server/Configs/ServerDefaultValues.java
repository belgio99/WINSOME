package Server.Configs;

import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;
import java.util.Properties;

public class ServerDefaultValues {

   public InetAddress serverAddress;
   public int TCPPort;
   public int UDPPort;

   public String multicastAddress;
   public int multicastPort;

   public InetAddress RMIAddress;
   public int RMIPort;
   public String RMIName;

   public int socketTimeout;
   public String databasePath;

   public int rewardDelayTime;
   public float authorPercentage;
   public int cacheSize;
   
   

   public ServerDefaultValues() {
      Properties properties = new Properties();
      setDefaults();
      File configFile = new File("properties.txt");
      try {
         if (configFile.exists())
         properties.load(new FileReader("properties.txt"));
         serverAddress = InetAddress.getByName((String)properties.getOrDefault("serverAddress", serverAddress));
         TCPPort = Integer.parseInt(String.valueOf(properties.getOrDefault("TCPPort", TCPPort)));
         TCPPort = checkValidPort(TCPPort, 50000);

         UDPPort = Integer.parseInt(String.valueOf(properties.getOrDefault("UDPPort", UDPPort)));
         UDPPort = checkValidPort(UDPPort, 50001);

         multicastAddress = (String)properties.getOrDefault("serverAddress", serverAddress);
         multicastPort = Integer.parseInt(String.valueOf(properties.getOrDefault("multicastPort", multicastPort)));
         multicastPort = checkValidPort(multicastPort, 50002);

         RMIAddress = (InetAddress.getByName((String) properties.getOrDefault("RMIAddress", RMIAddress)));
         RMIPort = Integer.parseInt(String.valueOf(properties.getOrDefault("RMIPort", RMIPort)));
         RMIPort = checkValidPort(RMIPort, 50003);

         authorPercentage = Integer.parseInt(String.valueOf(properties.getOrDefault("authorPercentage", authorPercentage)));
         socketTimeout = Integer.parseInt(String.valueOf(properties.getOrDefault("socketTimeout", socketTimeout)));
         cacheSize = Integer.parseInt(String.valueOf(properties.getOrDefault("cacheSize", cacheSize)));
         databasePath = (String) properties.getOrDefault("databasePath", databasePath);
         rewardDelayTime = Integer.parseInt((String) properties.getOrDefault("rewardDelayTime", rewardDelayTime));

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
      try {
      serverAddress = InetAddress.getLoopbackAddress();
      TCPPort = 50000;
      UDPPort = 50001;
      multicastAddress = "239.255.32.32";
      multicastPort = 50002;
      RMIAddress = InetAddress.getLoopbackAddress();
      RMIPort = 50003;
      socketTimeout = -1;
      rewardDelayTime = -1;
      databasePath = "/tmp/db";
      RMIName  = "WinsomeRegService";
      authorPercentage = 50;
      cacheSize = 10;
      }
      catch (Exception e)
      {
         System.out.println("Questo indirizzo non esiste.");
      }
   }

   private int checkValidPort(int port, int defaultValue) {
      if(port == 8080) System.err.println("Errore: la porta 8080 è riservata per l invio dei Post e delle transizioni");
      return port == 8080 ? defaultValue : port;
  }
}
