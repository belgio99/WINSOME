package Server.utils;

import java.time.Instant;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Post {

   private int IDPost;
   private String title;
   private String content;
   private transient User author;
   private String authorUsername;
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
      authorUsername = author.getUsername();
      timestamp = Instant.now();
      commentsList = new ConcurrentLinkedQueue<>();
      likersList = new ConcurrentLinkedQueue<>();
      dislikersList = new ConcurrentLinkedQueue<>();
      rewinList = new ConcurrentLinkedQueue<>();

      

   }

   public Post() {
   }

   public Post(int IDPost, String title, String content, User author, ConcurrentLinkedQueue<Comment> commentsList, ConcurrentLinkedQueue<User> likersList, ConcurrentLinkedQueue<User> dislikersList, Instant timestamp, ConcurrentLinkedQueue<User> rewinList) {
      this.IDPost = IDPost;
      this.title = title;
      this.content = content;
      this.author = author;
      this.commentsList = commentsList;
      this.likersList = likersList;
      this.dislikersList = dislikersList;
      this.timestamp = timestamp;
      this.rewinList = rewinList;
   }

   public Post IDPost(int IDPost) {
      setIDPost(IDPost);
      return this;
   }

   public Post title(String title) {
      setTitle(title);
      return this;
   }

   public Post content(String content) {
      setContent(content);
      return this;
   }

   public Post author(User author) {
      setAuthor(author);
      return this;
   }

   public Post commentsList(ConcurrentLinkedQueue<Comment> commentsList) {
      setCommentsList(commentsList);
      return this;
   }

   public Post likersList(ConcurrentLinkedQueue<User> likersList) {
      setLikersList(likersList);
      return this;
   }

   public Post dislikersList(ConcurrentLinkedQueue<User> dislikersList) {
      setDislikersList(dislikersList);
      return this;
   }

   public Post timestamp(Instant timestamp) {
      setTimestamp(timestamp);
      return this;
   }

   public Post rewinList(ConcurrentLinkedQueue<User> rewinList) {
      setRewinList(rewinList);
      return this;
   }



   @Override
   public String toString() {
      return "{" +
         " ID Post='" + getIDPost() + "'" +
         ", Titolo :'" + getTitle() + "'" +
         ", Contenuto :'" + getContent() + "'" +
         ", Autore ='" + getAuthor() + "'" +
         ", commentsList='" + getCommentsList() + "'" +
         ", likersList='" + getLikersList() + "'" +
         ", dislikersList='" + getDislikersList() + "'" +
         ", timestamp='" + getTimestamp() + "'" +
         ", rewinList='" + getRewinList() + "'" +
         "}";
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
