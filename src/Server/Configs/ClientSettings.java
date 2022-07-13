package Server.Configs;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

public class ClientSettings {
   public String serverAddress;
   public int TCPPort;
   public int UDPPort;

   public String multicastAddress;
   public int multicastPort;

   public String RMIAddress;
   public int RMIPort;
   public String RMIName;

   public ClientSettings(String path) {
      Properties settings = new Properties();
      File configFile = new File(path);
      try {
         if (configFile.exists()) {
            settings.load(new FileReader(path));
            serverAddress = (String) settings.getOrDefault("SERVER", "localhost");
            TCPPort = Integer.parseInt(String.valueOf(settings.getOrDefault("TCPPORT", 50000)));
            TCPPort = checkValidPort(TCPPort, 50000);

            UDPPort = Integer.parseInt(String.valueOf(settings.getOrDefault("UDPPORT", 50001)));
            UDPPort = checkValidPort(UDPPort, 50001);

            multicastAddress = (String) settings.getOrDefault("MULTICAST", "239.255.32.32");
            multicastPort = Integer.parseInt(String.valueOf(settings.getOrDefault("MCASTPORT", 50002)));
            multicastPort = checkValidPort(multicastPort, 50002);

            RMIAddress = (String) settings.getOrDefault("RMIADDR", "localhost");
            RMIPort = Integer.parseInt(String.valueOf(settings.getOrDefault("RMIPORT", 50003)));
            RMIPort = checkValidPort(RMIPort, 50003);

            RMIName = (String) settings.getOrDefault("RMINAME", "ServerRemoteInterface");
            System.out.println("File di configurazione caricato");
         }
         else {
            System.out.println("File di configurazione non trovato, uso valori di default");
            setDefaults();
         }
      } catch (Exception e) {
         e.printStackTrace();
         setDefaults();
      }
      return;
   }
   public void setDefaults() {
      serverAddress = "localhost";
      TCPPort = 50000;
      UDPPort = 50001;

      multicastAddress = "239.255.32.32";
      multicastPort = 50002;

      RMIAddress = "localhost";
      RMIPort = 50003;
      RMIName = "ServerRemoteInterface";
      return;
   }

   private int checkValidPort(int port, int defaultValue) {
      if (port < 0 || port > 65535) {
         System.out.println("Numero di porta non valida, verrà usato il valore di default");
         return defaultValue;
      }
      if (port == 8080)
         System.out.println("Errore: la porta 8080 è riservata per l invio dei Post e delle transazioni");
      return port == 8080 ? defaultValue : port;
   }
}
