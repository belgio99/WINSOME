package Server.utils;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class User{
   private String username;
   private String password;
   private LinkedList<String> tags;
   private ConcurrentLinkedQueue<User> following;
   private ConcurrentLinkedQueue<User> followers;
   private LinkedList<Transaction> wincoinList;
   private double currentCompensation;
   private LinkedList<Post> blog;


   public User(String username, String password, LinkedList<String> tags) {
      this.username = username;
      this.password = password;
      this.tags = tags;
      this.following = new ConcurrentLinkedQueue<>();
      this.followers = new ConcurrentLinkedQueue<>();
      this.wincoinList = new LinkedList<>();
      this.currentCompensation = 0;
      this.blog = new LinkedList<>();
      
   }
   

   public double getCurrentCompensation() {
      return this.currentCompensation;
   }

   public void setCurrentCompensation(double currentCompensation) {
      this.currentCompensation = currentCompensation;
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

   public LinkedList<Transaction> getWincoinList() {
      return this.wincoinList;
   }

   public void setWincoinList(LinkedList<Transaction> wincoinList) {
      this.wincoinList = wincoinList;
   }

   public LinkedList<Post> getBlog() {
      return this.blog;
   }

   public void setBlog(LinkedList<Post> blog) {
      this.blog = blog;
   }

   public User username(String username) {
      setUsername(username);
      return this;
   }

   public User password(String password) {
      setPassword(password);
      return this;
   }

   public User tags(LinkedList<String> tags) {
      setTags(tags);
      return this;
   }

   public User following(ConcurrentLinkedQueue<User> following) {
      setFollowing(following);
      return this;
   }

   public User followers(ConcurrentLinkedQueue<User> followers) {
      setFollowers(followers);
      return this;
   }

   public User wincoinList(LinkedList<Transaction> wincoinList) {
      setWincoinList(wincoinList);
      return this;
   }

   public User blog(LinkedList<Post> blog) {
      setBlog(blog);
      return this;
   }


}