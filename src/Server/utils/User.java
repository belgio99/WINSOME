package Server.utils;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class User {
   private String username;
   private String password;
   private LinkedList<String> tags;
   private ConcurrentLinkedQueue<User> following;
   private ConcurrentLinkedQueue<User> followers;
   private LinkedBlockingQueue<Transaction> wincoinList;
   private ArrayList<Integer> userPostList;
   private double currentCompensation;

   public User(String username, String password, LinkedList<String> tags) {
      this.username = username;
      this.password = password;
      this.tags = tags;
      this.following = new ConcurrentLinkedQueue<>();
      this.followers = new ConcurrentLinkedQueue<>();
      this.wincoinList = new LinkedBlockingQueue<>();
      this.userPostList = new ArrayList<>();
      this.currentCompensation = 0;
      
   }
   public void addToWincoinList(double reward) {
      wincoinList.add(new Transaction(reward));
   }
   public void addToUserPostList(int postId) {
      userPostList.add(postId);
   }
   public void addToFollowing(User user) {
      following.add(user);
   }
   public void addToFollowers(User user) {
      followers.add(user);
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
   public void removeFromWincoinList(double reward) {
      wincoinList.remove(new Transaction(reward));
   }
   public void removeFromUserPostList(int postId) {
      userPostList.remove(postId);
   }
   public void removeFromFollowing(User user) {
      following.remove(user);
   }
   public void removeFromFollowers(User user) {
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

   public ConcurrentLinkedQueue<User> getFollowing() {
      return this.following;
   }

   public void setFollowing(ConcurrentLinkedQueue<User> following) {
      this.following = following;
   }

   public ConcurrentLinkedQueue<User> getFollowers() {
      return this.followers;
   }

   public void setFollowers(ConcurrentLinkedQueue<User> followers) {
      this.followers = followers;
   }

   public LinkedBlockingQueue<Transaction> getWincoinList() {
      return this.wincoinList;
   }

   public void setWincoinList(LinkedBlockingQueue<Transaction> wincoinList) {
      this.wincoinList = wincoinList;
   }

   public ArrayList<Integer> getUserPostList() {
      return this.userPostList;
   }



}