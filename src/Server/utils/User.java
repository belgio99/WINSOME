package Server.utils;
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
   private double currentCompensation;
   private LinkedList<Post> blog;

   public User(String username, String password, LinkedList<String> tags) {
      this.username = username;
      this.password = password;
      this.tags = tags;
      this.following = new ConcurrentLinkedQueue<>();
      this.followers = new ConcurrentLinkedQueue<>();
      this.wincoinList = new LinkedBlockingQueue<>();
      this.currentCompensation = 0;
      this.blog = new LinkedList<>();
      
   }
   public void addToWincoinList(double reward) {
      wincoinList.add(new Transaction(reward));
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

   public double getCurrentCompensation() {
      return this.currentCompensation;
   }

   public void setCurrentCompensation(double currentCompensation) {
      this.currentCompensation = currentCompensation;
   }

   public LinkedList<Post> getBlog() {
      return this.blog;
   }

   public void setBlog(LinkedList<Post> blog) {
      this.blog = blog;
   }
   


}