package Server;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import Server.Configs.ServerSettings;
import Server.utils.Comment;
import Server.utils.Post;
import Server.utils.ServerUtils;
import Server.utils.User;

public class RewardCalculatorThread implements Runnable {

   private final ConcurrentHashMap<Integer, Post> postDB;
   private Instant currentDate;
   private Instant lastDate = null;
   private double reward;

   public RewardCalculatorThread(ConcurrentHashMap<Integer, Post> postDB) {
      this.postDB = postDB;
   }

   public void run() {
      synchronized (postDB) {
         System.out.println("Calcolo dei rewards in corso...");
         if (postDB.isEmpty())
            return;
         Iterator<Entry<Integer, Post>> it = postDB.entrySet().iterator();
         while (it.hasNext()) {
            Post p = (Post) it.next();
            if (isBetweenDates(p.getLastRewardTimestamp())) {
               HashSet<String> personAnalyzed = analyze(p);
               p.increaseIterations();
               double authorReward = round((reward * ServerSettings.authorPercentage) / 100, 1);
               ServerManager.findUserByUsername(p.getAuthor()).addToWincoinList(reward);
               double othersReward = round((reward - authorReward) / personAnalyzed.size(), 1);
               for (String username : personAnalyzed) {
                  ServerManager.findUserByUsername(username).addToWincoinList(othersReward);
               }

            }
               ServerUtils.sendUDPMessage("Rewards Calcolati!");
         }
      }
   }

   private boolean isBetweenDates(Instant date) {
      return (Duration.between(lastDate, date).toNanos() >= 0 && Duration.between(date, currentDate).toNanos() >= 0);
   }

   private HashSet<String> analyze(Post p) {
      HashSet<String> set = new HashSet<>(
            p.getCommentsList().size() + p.getLikersList().size() + p.getDislikersList().size()); // creo un set per
                                                                                                  // contenere tutti gli
                                                                                                  // utenti che hanno
                                                                                                  // commentato, likeato
                                                                                                  // o dislikeato il
                                                                                                  // post
      int likersCounter = 0, dislikersCounter = 0;
      Set<String> keys = p.getLikersList().keySet(); // prendo tutti i liker dell'post
      for (String key : keys) {
         if (isBetweenDates(p.getLikersList().get(key))) { // se l'utente ha likeato il post entro le date dell'ultimo
                                                           // aggiornamento lo aggiungo al set e aumento il contatore di
                                                           // likes
            likersCounter++;
            set.add(key);
         }
      }
      System.out.println("Ci sono likes: " + likersCounter);

      keys = p.getDislikersList().keySet(); // prendo tutti i disliker dell'post
      for (String key : keys) {
         if (isBetweenDates(p.getDislikersList().get(key))) { // faccio la stessa cosa per i disliker
            dislikersCounter++;
            set.add(key);
         }
      }
      System.out.println("Ci sono dislikes: " + dislikersCounter);

      double n1 = getn1(likersCounter, dislikersCounter);
      HashMap<String, Integer> map = commentCounter(p, set);
      double n2 = 0;
      for (String user : map.keySet()) {
         n2 = getn2(map.get(user));
      }
      n2 = Math.log(n2 + 1);
      n1 = round(n1, 1);

      reward = round((n1 + n2) / p.getNumIterations(), 1);

      return set;

   }

   public double round(double value, int places) {
      if (places < 0)
         throw new IllegalArgumentException();

      BigDecimal bd = BigDecimal.valueOf(value);
      bd = bd.setScale(places, RoundingMode.HALF_UP);
      return bd.doubleValue();
   }

   private HashMap<String, Integer> commentCounter(Post p, HashSet<String> set) {
      HashMap<String, Integer> map = new HashMap<>(p.getCommentsList().size());
      for (Comment c : p.getCommentsList()) {
         if (!isBetweenDates(c.getTimestamp()))
            break;

         User author = c.getAuthor();
         set.add(author.getUsername());
         if (map.containsKey(author.getUsername())) {
            int value = map.get(author.getUsername());
            map.replace(author.getUsername(), value + 1);
         } else
            map.put(author.getUsername(), 1);
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