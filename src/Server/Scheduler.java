package Server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import Server.Configs.ServerSettings;
import Server.utils.AutosaveThread;
import Server.utils.Post;
import Server.utils.RewardCalculatorThread;

public class Scheduler extends ScheduledThreadPoolExecutor {

   private ConcurrentHashMap<Integer, Post> postDB;

   public Scheduler(ConcurrentHashMap<Integer, Post> postDB) {
      super(2); //creo un thread pool con due thread
      this.postDB = postDB;
   }


  public void start() {
     scheduleAtFixedRate(new RewardCalculatorThread(postDB), parseDelayTime(ServerSettings.rewardDelayTime), parseDelayTime(ServerSettings.rewardDelayTime), parseTimeUnit(ServerSettings.rewardDelayTime));
     scheduleAtFixedRate(new AutosaveThread(), parseDelayTime(ServerSettings.autoSaveTime), parseDelayTime(ServerSettings.autoSaveTime), parseTimeUnit(ServerSettings.autoSaveTime));
  }

  public void stop() {
     shutdown();
  }

   private int parseDelayTime(String delay) {
      int delayTime;
        String time = delay.substring(0, delay.length() - 1);
        try {
        delayTime = Integer.parseInt(time);
        }
        catch (NumberFormatException e) {
         System.err.println("Errore nella conversione del tempo per il calcolo delle ricompense");
         System.err.println("Verrà usato il valore di default");
         return 5;
        }
        if (delayTime <= 0) {
            System.err.println("Errore nella conversione del tempo per il calcolo delle ricompense");
            System.err.println("Verrà usato il valore di default");
            return 5;
        }
         return delayTime;

   }
   private TimeUnit parseTimeUnit(String delay) {
      char u = delay.charAt(delay.length() - 1);
      switch (u) {
         case 's': return TimeUnit.SECONDS;
         case 'm': return TimeUnit.MINUTES;
         case 'h': return TimeUnit.HOURS; 
         case 'd': return TimeUnit.DAYS;
         default: {
             System.err.println("Errore: usare s,m,h,d (tutto minuscolo)");
             System.err.println("Verrà usato il valore di default (i secondi)");
             return TimeUnit.SECONDS;
   }

}
   }
}
