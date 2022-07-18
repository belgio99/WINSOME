package src.datastructures;

import java.time.Instant;

public class Comment {
   private User author;
   private String content;
   private Instant timestamp;

   public Comment(User author, String content) {
      this.author = author;
      this.content = content;
      timestamp = Instant.now();
   }

   public User getAutore() {
      return this.author;
   }

   public void setAutore(User autore) {
      this.author = autore;
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

   public User getAuthor() {
      return this.author;
   }

   public void setAuthor(User author) {
      this.author = author;
   }

   public String getContent() {
      return this.content;
   }

   public void setContent(String content) {
      this.content = content;
   }

}
