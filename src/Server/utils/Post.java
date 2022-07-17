package Server.utils;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Post {

   private int ID;
   private String title;
   private String content;
   private String author;
   private ConcurrentLinkedQueue<Comment> commentsList;
   private LinkedHashMap<String, Instant> likersList;
   private LinkedHashMap<String, Instant> dislikersList;
   private Instant creationTimestamp;
   private Instant lastRewardTimestamp;
   private ConcurrentLinkedQueue<String> rewinList;
   private int numIterations;
   
   

   public Post(int ID, String author, String title, String content) {
      this.ID = ID;
      //Da controllare se quell'ID è già preso
      this.title = title;
      this.content = content;
      this.author = author;
      creationTimestamp = Instant.now();
      commentsList = new ConcurrentLinkedQueue<>();
      likersList = new LinkedHashMap<>();
      dislikersList = new LinkedHashMap<>();
      rewinList = new ConcurrentLinkedQueue<>();
      numIterations = 0;
      lastRewardTimestamp = creationTimestamp;

      
      

   }

   public String getAuthor() {
      return this.author;
   }

   public void setAuthor(String author) {
      this.author = author;
   }

   public void increaseIterations () {
      numIterations++;
   }
   public int getNumIterations() {
      return numIterations+1;
   }

   public int getId() {
      return this.ID;
   }

   public void setID(int ID) {
      this.ID = ID;
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
   public ConcurrentLinkedQueue<Comment> getCommentsList() {
      return this.commentsList;
   }

   public void setCommentsList(ConcurrentLinkedQueue<Comment> commentsList) {
      this.commentsList = commentsList;
   }

   public LinkedHashMap<String,Instant> getLikersList() {
      return this.likersList;
   }

   public void setLikersList(LinkedHashMap<String,Instant> likersList) {
      this.likersList = likersList;
   }

   public LinkedHashMap<String,Instant> getDislikersList() {
      return this.dislikersList;
   }

   public void setDislikersList(LinkedHashMap<String,Instant> dislikersList) {
      this.dislikersList = dislikersList;
   }

   public Instant getCreationTimestamp() {
      return this.creationTimestamp;
   }

   public void setCreationTimestamp(Instant creationTimestamp) {
      this.creationTimestamp = creationTimestamp;
   }

   public Instant getLastRewardTimestamp() {
      return this.lastRewardTimestamp;
   }

   public void setLastRewardTimestamp(Instant lastUpdateTimestamp) {
      this.lastRewardTimestamp = lastUpdateTimestamp;
   }

   public ConcurrentLinkedQueue<String> getRewinList() {
      return this.rewinList;
   }

   public void setNumIterations(int numIterations) {
      this.numIterations = numIterations;
   }
   
}
