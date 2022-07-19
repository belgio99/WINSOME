package threads;

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
import java.util.concurrent.ConcurrentLinkedQueue;

import datastructures.Comment;
import datastructures.Post;
import datastructures.User;
import settings.ServerSettings;
import utils.ServerManager;
import utils.ServerUtils;

public class RewardCalculatorThread implements Runnable {

   private final ConcurrentHashMap<Integer, Post> postDB;
   private Instant lastDate;
   private double reward;
   private final ConcurrentLinkedQueue<Post> analyzeList;

   public RewardCalculatorThread(ConcurrentLinkedQueue<Post> analyzeList, ConcurrentHashMap<Integer, Post> postDB) {
      this.postDB = postDB;
      this.analyzeList = analyzeList;
   }

   public void run() { 
      try{

         synchronized (analyzeList) {      
         System.out.println("Calcolo dei rewards in corso...");
         while (!analyzeList.isEmpty()) {
            Post p = analyzeList.poll();
               HashSet<String> likers = analyzePost(p); //analizza il post per ottenere chi ha interagito con esso, per ottenere i rewards
               double authorReward = round((reward * ServerSettings.authorPercentage) / 100, 1);
               if (authorReward > 0)
                  ServerManager.findUserByUsername(p.getAuthor()).addToWincoinList(reward);
               if (likers.size()>0) { //evitare la divisione per zero!
                  double othersReward = round((reward - authorReward) / likers.size(), 1);
                  for (String username : likers) {
                     User u = ServerManager.findUserByUsername(username);
                     u.addToWincoinList(othersReward);
               
               }
               
                  }
         }
      }
      synchronized (postDB) {
         Iterator<Entry<Integer, Post>> it = postDB.entrySet().iterator();
         while (it.hasNext()) {
            it.next().getValue().increaseIterations(); //aumento il numero di iterazioni del post, anche se non ha subito modifiche
         }

      }
      ServerUtils.sendUDPMessage("Rewards Calcolati!");
      lastDate = Instant.now();
   }
   catch (Exception e) {
      e.printStackTrace();
   }
   }

   private boolean isAfterLastUpdate(Instant date) {
      if (lastDate == null)
         return true;
      return (Duration.between(lastDate, date).toNanos() >= 0); //Se date è maggiore o uguale a lastDate e minore o uguale a currentDate
   }

   private HashSet<String> analyzePost(Post p) {
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
         if (isAfterLastUpdate(p.getLikersList().get(key))) { // se l'utente ha messo like al post entro le date dell'ultimo
                                                           // aggiornamento lo aggiungo al set e aumento il contatore di
                                                           // likes
            likersCounter++;
            set.add(key);
         }
      }

      keys = p.getDislikersList().keySet(); // prendo tutti i disliker dell'post
      for (String key : keys) {
         if (isAfterLastUpdate(p.getDislikersList().get(key))) { // faccio la stessa cosa per i disliker, ma non li aggiungo
                                                              // al set perché i dislike non devono essere ricompensati
            dislikersCounter++;
         }
      }

      double n1 = getn1(likersCounter, dislikersCounter);
      HashMap<String, Integer> map = commentCounter(p, set); // conto quanti commenti ha fatto ogni utente
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
      //Metodo per contare quanti commenti ha fatto ogni utente in quel post
      HashMap<String, Integer> map = new HashMap<>(p.getCommentsList().size()); // creo una map per contenere l'username e
                                                                                // il numero di commenti
      for (Comment c : p.getCommentsList()) {
         if (isAfterLastUpdate(c.getTimestamp())) {

         String author = c.getAuthor();
         if (map.containsKey(author)) {
            int value = map.get(author);
            map.replace(author, value + 1);
         } else {
            set.add(author);
            map.put(author, 1);
         }
      }
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