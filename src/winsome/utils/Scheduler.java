package winsome.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import winsome.Configs.ServerSettings;
import winsome.datastructures.Post;
import winsome.threads.AutosaveThread;
import winsome.threads.RewardCalculatorThread;

public class Scheduler extends ScheduledThreadPoolExecutor {

   private ConcurrentHashMap<Integer, Post> postDB;
   private ConcurrentLinkedQueue<Post> analyzeList;

   public Scheduler(ConcurrentLinkedQueue<Post> analyzeList, ConcurrentHashMap<Integer, Post> postDB) {
      super(2); //creo un thread pool con due thread
      this.postDB = postDB;
      this.analyzeList = analyzeList;
   }


  public void start() {
     scheduleAtFixedRate(new RewardCalculatorThread(analyzeList, postDB), parseDelayTime(ServerSettings.rewardDelayTime), parseDelayTime(ServerSettings.rewardDelayTime), parseTimeUnit(ServerSettings.rewardDelayTime));
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
         System.err.println("Errore nella conversione del tempo");
         System.err.println("Verrà usato il valore di default");
         return 5;
        }
        if (delayTime <= 0) {
            System.err.println("Errore nella conversione del tempo");
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
