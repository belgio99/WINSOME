package Server.utils;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Post {

   private int IDPost;
   private String title;
   private String content;
   private transient User author;
   private String authorUsername;
   private ConcurrentLinkedQueue<Comment> commentsList;
   private LinkedHashMap<User, Instant> likersList;
   private LinkedHashMap<User, Instant> dislikersList;
   private Instant creationTimestamp;
   private Instant lastUpdateTimestamp;
   private ConcurrentLinkedQueue<User> rewinList;
   private int numIterations;
   
   

   public Post(int IDPost, User author, String title, String content) {
      this.IDPost = IDPost;
      //Da controllare se quell'IDpost è già preso
      this.title = title;
      this.content = content;
      this.author = author;
      authorUsername = author.getUsername();
      creationTimestamp = Instant.now();
      commentsList = new ConcurrentLinkedQueue<>();
      likersList = new LinkedHashMap<>();
      dislikersList = new LinkedHashMap<>();
      rewinList = new ConcurrentLinkedQueue<>();
      numIterations = 0;
      lastUpdateTimestamp = creationTimestamp;

      
      

   }

   public User getAuthor() {
      return this.author;
   }

   public void setAuthor(User author) {
      this.author = author;
   }

   public void increaseIterations () {
      numIterations++;
   }
   public int getNumIterations() {
      return numIterations+1;
   }

   public int getIDPost() {
      return this.IDPost;
   }

   public void setIDPost(int IDPost) {
      this.IDPost = IDPost;
   }

   public String getTitle() {
      return this.title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public String getContent() {
      return this.content;
   }

   public void setContent(String content) {
      this.content = content;
   }

   public String getAuthorUsername() {
      return this.authorUsername;
   }

   public void setAuthorUsername(String authorUsername) {
      this.authorUsername = authorUsername;
   }

   public ConcurrentLinkedQueue<Comment> getCommentsList() {
      return this.commentsList;
   }

   public void setCommentsList(ConcurrentLinkedQueue<Comment> commentsList) {
      this.commentsList = commentsList;
   }

   public LinkedHashMap<User,Instant> getLikersList() {
      return this.likersList;
   }

   public void setLikersList(LinkedHashMap<User,Instant> likersList) {
      this.likersList = likersList;
   }

   public LinkedHashMap<User,Instant> getDislikersList() {
      return this.dislikersList;
   }

   public void setDislikersList(LinkedHashMap<User,Instant> dislikersList) {
      this.dislikersList = dislikersList;
   }

   public Instant getCreationTimestamp() {
      return this.creationTimestamp;
   }

   public void setCreationTimestamp(Instant creationTimestamp) {
      this.creationTimestamp = creationTimestamp;
   }

   public Instant getLastUpdateTimestamp() {
      return this.lastUpdateTimestamp;
   }

   public void setLastUpdateTimestamp(Instant lastUpdateTimestamp) {
      this.lastUpdateTimestamp = lastUpdateTimestamp;
   }

   public ConcurrentLinkedQueue<User> getRewinList() {
      return this.rewinList;
   }

   public void setRewinList(ConcurrentLinkedQueue<User> rewinList) {
      this.rewinList = rewinList;
   }
   public void setNumIterations(int numIterations) {
      this.numIterations = numIterations;
   }
   
}
