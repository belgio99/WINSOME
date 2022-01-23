package Server.utils;

import java.time.Instant;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Post {

   private int IDPost;
   private String title;
   private String content;
   private User author;
   private ConcurrentLinkedQueue<Comment> commentsList;
   private ConcurrentLinkedQueue<User> likersList;
   private ConcurrentLinkedQueue<User> dislikersList;
   private Instant timestamp;
   private ConcurrentLinkedQueue<User> rewinList;
   
   

   public Post(int IDPost, User author, String title, String content) {
      this.IDPost = IDPost;
      //Da controllare se quell'IDpost è già preso
      this.title = title;
      this.content = content;
      this.author = author;
      timestamp = Instant.now();
      commentsList = new ConcurrentLinkedQueue<>();
      likersList = new ConcurrentLinkedQueue<>();
      dislikersList = new ConcurrentLinkedQueue<>();
      rewinList = new ConcurrentLinkedQueue<>();

      

   }

   public ConcurrentLinkedQueue<User> getRewinList() {
      return this.rewinList;
   }

   public void setRewinList(ConcurrentLinkedQueue<User> rewinList) {
      this.rewinList = rewinList;
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

   public User getAuthor() {
      return this.author;
   }

   public void setAuthor(User author) {
      this.author = author;
   }

   public ConcurrentLinkedQueue<Comment> getCommentsList() {
      return this.commentsList;
   }

   public void setCommentsList(ConcurrentLinkedQueue<Comment> commentsList) {
      this.commentsList = commentsList;
   }

   public ConcurrentLinkedQueue<User> getLikersList() {
      return this.likersList;
   }

   public void setLikersList(ConcurrentLinkedQueue<User> likersList) {
      this.likersList = likersList;
   }

   public ConcurrentLinkedQueue<User> getDislikersList() {
      return this.dislikersList;
   }

   public void setDislikersList(ConcurrentLinkedQueue<User> dislikersList) {
      this.dislikersList = dislikersList;
   }

   public Instant getTimestamp() {
      return this.timestamp;
   }

   public void setTimestamp(Instant timestamp) {
      this.timestamp = timestamp;
   }

}
