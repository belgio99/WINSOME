package winsome.datastructures;

import java.time.Instant;

public class Comment {
   private String author;
   private String content;
   private Instant timestamp;

   public Comment(String author, String content) {
      this.author = author;
      this.content = content;
      timestamp = Instant.now();
   }

   public String getContenuto() {
      return this.content;
   }

   public void setContenuto(String contenuto) {
      this.content = contenuto;
   }

   public Instant getTimestamp() {
      return this.timestamp;
   }

   public void setTimestamp(Instant timestamp) {
      this.timestamp = timestamp;
   }

   public String getAuthor() {
      return this.author;
   }

   public void setAuthor(String author) {
      this.author = author;
   }

   public String getContent() {
      return this.content;
   }

   public void setContent(String content) {
      this.content = content;
   }

}
