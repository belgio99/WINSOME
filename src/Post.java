

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
   private ConcurrentLinkedQueue<String> rewinList;
   private int numIterations;

   public Post(int ID, String author, String title, String content) {
      this.ID = ID;
      // Da controllare se quell'ID è già preso
      this.title = title;
      this.content = content;
      this.author = author;
      creationTimestamp = Instant.now();
      commentsList = new ConcurrentLinkedQueue<>();
      likersList = new LinkedHashMap<>();
      dislikersList = new LinkedHashMap<>();
      rewinList = new ConcurrentLinkedQueue<>();
      numIterations = 0;

   }

   public String getAuthor() {
      return this.author;
   }

   public void setAuthor(String author) {
      this.author = author;
   }

   public void increaseIterations() {
      numIterations++;
   }

   public int getNumIterations() {
      return numIterations + 1;
   }

   public int getId() {
      return this.ID;
   }

   public String getTitle() {
      return this.title;
   }

   public String getContent() {
      return this.content;
   }

   public ConcurrentLinkedQueue<Comment> getCommentsList() {
      return this.commentsList;
   }

   public LinkedHashMap<String, Instant> getLikersList() {
      return this.likersList;
   }

   public LinkedHashMap<String, Instant> getDislikersList() {
      return this.dislikersList;
   }

   public Instant getCreationTimestamp() {
      return this.creationTimestamp;
   }

   public ConcurrentLinkedQueue<String> getRewinList() {
      return this.rewinList;
   }

   public void setNumIterations(int numIterations) {
      this.numIterations = numIterations;
   }

}
