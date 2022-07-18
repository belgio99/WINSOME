package winsome.datastructures;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class User {
   private String username;
   private String password;
   private LinkedList<String> tags;
   private ConcurrentLinkedQueue<String> following;
   private ConcurrentLinkedQueue<String> followers;
   private LinkedBlockingQueue<Transaction> wincoinList;
   private LinkedList<Integer> userPostList;
   private double currentCompensation;

   public User(String username, String password, LinkedList<String> tags) {
      this.username = username;
      this.password = hashEncrypt(password);
      this.tags = tags;
      this.following = new ConcurrentLinkedQueue<>();
      this.followers = new ConcurrentLinkedQueue<>();
      this.wincoinList = new LinkedBlockingQueue<>();
      this.userPostList = new LinkedList<>();
      this.currentCompensation = 0;

   }

   public static String hashEncrypt(String password) {
      String encryptedPassword = "";
      try {
          encryptedPassword = toHexString(getSHA(password));
      } catch (NoSuchAlgorithmException e) {
          System.out.println("Algoritmo di crittografia non trovato!");
      }
      return encryptedPassword;
  }
  public static byte[] getSHA(String input) throws NoSuchAlgorithmException {
        // Creo un'istanza di messagedigest per calcolare l'hash, utilizzando l'algoritmo SHA-256
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        // faccio il vero e proprio digest, che ritorna un array di byte
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }
    public static String toHexString(byte[] hash) {
        // Converto l'array di byte in un BigInteger 
        BigInteger number = new BigInteger(1, hash);
        // Converto poi in una stringa di caratteri esadecimali, utilizzando StringBuilder in modo da poter inserire gli zeri iniziali
        StringBuilder hexString = new StringBuilder(number.toString(16));
        while (hexString.length() < 32)
            hexString.insert(0, '0');
        return hexString.toString();
    }

   public void addToWincoinList(double reward) {
      if (reward > 0) {
         wincoinList.add(new Transaction(reward));
      }
   }

   public void addToUserPostList(int postId) {
      userPostList.add(postId);
   }

   public void addToFollowing(String username) {
      following.add(username);
   }

   public void addToFollowers(String username) {
      followers.add(username);
   }

   public void addToCompensation(double compensation) {
      currentCompensation += compensation;
   }

   public void removeFromCompensation(double compensation) {
      currentCompensation -= compensation;
   }

   public double getCurrentCompensation() {
      return currentCompensation;
   }

   public void removeFromUserPostList(int postId) {
      Iterator<Integer> it = userPostList.iterator();
      while (it.hasNext()) {
         if (it.next() == postId) {
            it.remove();
         }
      }
   }

   public void removeFromFollowing(String user) {
      following.remove(user);
   }

   public void removeFromFollowers(String user) {
      followers.remove(user);
   }

   public String getUsername() {
      return this.username;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   public String getPassword() {
      return this.password;
   }

   public void setPassword(String password) {
      this.password = password;
   }

   public LinkedList<String> getTags() {
      return this.tags;
   }

   public void setTags(LinkedList<String> tags) {
      this.tags = tags;
   }

   public ConcurrentLinkedQueue<String> getFollowing() {
      return this.following;
   }

   public void setFollowing(ConcurrentLinkedQueue<String> following) {
      this.following = following;
   }

   public ConcurrentLinkedQueue<String> getFollowers() {
      return this.followers;
   }

   public void setFollowers(ConcurrentLinkedQueue<String> followers) {
      this.followers = followers;
   }

   public LinkedBlockingQueue<Transaction> getWincoinList() {
      return this.wincoinList;
   }

   public void setWincoinList(LinkedBlockingQueue<Transaction> wincoinList) {
      this.wincoinList = wincoinList;
   }

   public LinkedList<Integer> getUserPostList() {
      return this.userPostList;
   }

}