package Server;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import Server.Configs.ServerSettings;
import Server.utils.Post;

public class RewardCalculator extends ScheduledThreadPoolExecutor {

   private ConcurrentLinkedQueue<Post> analyzeList;
   private int n;
   private TimeUnit unit;

   public RewardCalculator(ConcurrentLinkedQueue<Post> analyzeList) {
      super(1);
      this.analyzeList = analyzeList;
      setValues();
   }

   public void analyzePost(Post p) {
      analyzeList.add(p);
   }

   public void discardPost(Post p) {
      analyzeList.remove(p);
  }

  public void startAnalyzing() {
     scheduleAtFixedRate(new RewardCalculatorThread(analyzeList), n, n, unit);
  }

  //public void stopA

   private void setValues() {
        String delay = ServerSettings.rewardDelayTime;
        char u = delay.charAt(delay.length() - 1); // prendo l'ultimo carattere di REWARDINTERVAL

        String time = delay.substring(0, delay.indexOf(u));
        n = Integer.parseInt(time);
        if (n <= 0) {
            System.err.println("Errore nella conversione del tempo per il calcolo delle rimpense");
            System.err.println("Verrà usato il valore di default");

            n = 5;
        }
        switch (u) {
         case 's': unit = TimeUnit.SECONDS; break;
         case 'm': unit = TimeUnit.MINUTES; break;
         case 'h': unit = TimeUnit.HOURS; break;
         case 'd': unit = TimeUnit.DAYS; break;
         default: {
             System.err.println("Errore: usare s,m,h,d,w (tutto minuscolo)");
             System.err.println("Verrà usato il valore di default");
             unit = TimeUnit.SECONDS;
             n = 5;
         }
      }
   }

}