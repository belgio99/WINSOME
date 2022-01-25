package Server;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import Server.Configs.DefaultValues;
import Server.utils.Comment;
import Server.utils.Post;
import Server.utils.ServerUtils;
import Server.utils.User;

public class RewardCalculatorThread implements Runnable{

   private final ConcurrentLinkedQueue<Post> analyzeList;
   private Instant currentDate;
   private Instant lastDate = null;
   private double reward;

   public RewardCalculatorThread(ConcurrentLinkedQueue<Post> analyzeList) {
      this.analyzeList = analyzeList;
   }
   
   public void run() {
      if (analyzeList.isEmpty()) return;

      for (Post p: analyzeList) {
         if (isBetweenDates(p.getLastUpdateTimestamp())) {
            HashSet<User> personAnalyzed = analyze(p);
         p.increaseIterations();
         double authorReward = round((reward * DefaultValues.serverval.authorPercentage)/100,1);
         p.getAuthor().addToWincoinList(reward);
         double othersReward = round((reward - authorReward)/ personAnalyzed.size(),1);
         for (User user : personAnalyzed)
            user.addToWincoinList(othersReward);
         
         }

         analyzeList.remove(p);
         try {
            String msg = "RewardsCalcolati!";
            ServerUtils.sendUDPMessage(msg);
         }
         catch (Exception e) {
            e.printStackTrace();
         }

      }
   }
   
   private boolean isBetweenDates(Instant date) {
      return (Duration.between(lastDate, date).toNanos()>=0 && Duration.between(date, currentDate).toNanos()>=0);
  }


  private HashSet<User> analyze(Post p) {
      HashSet<User> set = new HashSet<>(p.getCommentsList().size() + p.getLikersList().size() + p.getDislikersList().size());
      int likesCounter = 0, diskLikersCounter = 0;
      Set<User> keys = p.getLikersList().keySet();
      for (User key : keys) {
         if (isBetweenDates(p.getLikersList().get(key))) {
            likesCounter++;
            set.add(key);
         }
      }
      System.out.println("Ci sono likes: " + likesCounter);
      for (User key : keys) {
         if (isBetweenDates(p.getDislikersList().get(key))) {
            likesCounter++;
            set.add(key);
         }
      }
      System.out.println("Ci sono dislikes: " + diskLikersCounter);
      double n1 = getn1(likesCounter, diskLikersCounter);
      HashMap<User, Integer> map = commentHandler(p, set);
      double n2 = 0;
      for (User user : map.keySet()) {
         n2 = getn2(map.get(user));
      }
      n2 = Math.log(n2 + 1);
      n1 = round(n1, 1);
      
      reward = round((n1+n2)/p.getNumIterations(),1);


      return set;



}
public double round(double value, int places) {
   if (places < 0) throw new IllegalArgumentException();

   BigDecimal bd = BigDecimal.valueOf(value);
   bd = bd.setScale(places, RoundingMode.HALF_UP);
   return bd.doubleValue();
}
private HashMap<User, Integer> commentHandler(Post p, HashSet<User> set) {
   HashMap<User, Integer> map = new HashMap<>(p.getCommentsList().size());
   for (Comment c : p.getCommentsList()) {
      if (!isBetweenDates(c.getTimestamp())) break;

      User author = c.getAuthor();
      set.add(author);
      if (map.containsKey(author)) {
         int value = map.get(author);
         map.replace(author, value+1);

      }
      else
         map.put(author, 1);
   }
   return map;
}
private double getn1(int num_likes, int num_dislikes) {
   return Math.log(Math.max(num_likes - num_dislikes, 0) + 1);
}

private double getn2(int cp) {
   return 2 / (1 + Math.pow(Math.E, -(cp - 1)));
}

}