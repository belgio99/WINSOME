package Server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import Server.utils.Comment;
import Server.utils.Post;
import Server.utils.User;
import static Server.utils.ResultCode.*;

public class ServerManager {
   //private static final Gson gson = new Gson();

   private static final Database database = new Database();
   private static Selector selector;
   private static final ConcurrentHashMap<SocketChannel, User> usersLogged = new ConcurrentHashMap<>();
   

   static {
      try {
         selector = Selector.open();
      }
      catch (IOException e) {
         e.printStackTrace();
      }
   }

   public static boolean register(String username, String password, LinkedList<String> tagsList) {
      return database.registerUser(username, password, tagsList);

   }
   public static User login(String username, String password, SocketChannel clientChannel) {
       User user = database.findUserByUsername(username);
       if (user == null) return null;
       if (!password.equals(user.getPassword())) return null;
       addUserLogged(user, clientChannel);
       return user;
      
   }
   public static boolean logoutUser(SocketChannel clientChannel) {
      //database.logoutUser(u);
      return removeUserLogged(clientChannel);

   }
   public static boolean createPost(String title, String content, User author) {
      Post post = new Post(database.getPreviousMaxPostID()+1, author, title, content);
      database.addPost(author, post);
      return true;

   }
   public static boolean deletePost(Post post) {
      return database.deletePost(post);

   }

   public static Post getPostByID(int ID) {
      return database.getPostByID(ID);
   }


   public static User findUserByUsername(String username) {
      return database.findUserByUsername(username);
   }
   public static Selector getSelector() {
      return selector;
   }
   public static boolean addUserLogged(User user, SocketChannel clientChannel) {
      return usersLogged.putIfAbsent(clientChannel, user) == null ? true : false;
   }
   public static boolean removeUserLogged(SocketChannel clientChannel) {
      return usersLogged.remove(clientChannel) == null ? false : true;
   }
   public static User getUserLogged(SocketChannel clientChannel) {
      return usersLogged.get(clientChannel);
   }
   public static boolean followUser(User u, String usernameToFollow) {
      User userToFollow = database.findUserByUsername(usernameToFollow);
      u.getFollowing().add(userToFollow);
      userToFollow.getFollowers().add(u);
      return true;
   }
   public static boolean unfollowUser(User u, String usernameToUnfollow) {
      User userToUnfollow = database.findUserByUsername(usernameToUnfollow);
      u.getFollowing().remove(userToUnfollow);
      userToUnfollow.getFollowers().remove(u);
      return true;
   }

   public static boolean addComment(User u, Post post, String content) {
      Comment comment = new Comment(u, content);
      post.getCommentsList().add(comment);
      return true;
   }

   public static boolean rewinPost(User u, Post post) {
      database.addPost(u, post);
      post.getRewinList().add(u);
      return true;
   }

   public static String ratePost(User u, Post post, int vote) {
      if (post.getAuthor().equals(u))
         return RATE_OWN_POST;
      if (post.getLikersList().contains(u) || post.getDislikersList().contains(u))
         return ALREADY_RATED;
      if (!database.getUserPosts(u).contains(post))
         return RATE_BEFORE_REWIN;
      switch (vote) {
      case 1:
         post.getLikersList().add(u);
      case -1:
         post.getDislikersList().add(u);
      default:
         return OK;
      }
   }

   public static double getWalletAmount(User u) {
      return u.getCurrentCompensation();
      
   }

   public static double getBTCValue() {
      String randomURL = "https://www.random.org/decimal-fractions/?num=1&dec=5&col=2&format=plain&rnd=new";
      try {
          String encoding = "ISO-8859-1";
          URL u = new URL(randomURL);
          URLConnection uc = u.openConnection();
          String contentType = uc.getContentType();
          int encodingStart = contentType.indexOf("charset=");
          if (encodingStart != -1) {
              encoding = contentType.substring(encodingStart + 8);
          }
          InputStream in = new BufferedInputStream(uc.getInputStream());
          Reader r = new InputStreamReader(in, encoding);
          int c;
          StringBuilder stringBuilder = new StringBuilder();
          while ((c = r.read()) != -1) {
              stringBuilder.append((char) c);
          }
          r.close();
          return Double.parseDouble(stringBuilder.toString());
         }
         catch (Exception e) {
            e.printStackTrace();
            return 0;
         }

   }
   public static String listUsers(User u) {
      LinkedList<String> userTags = u.getTags();
      HashSet<String> returnSet = new HashSet<>();
      Iterator<String> itr1 = userTags.iterator();
      while (itr1.hasNext()) {
         LinkedList<User> userTagList = database.getUsersOfTag(itr1.next());
         Iterator<User> itr2 = userTagList.iterator();
         while (itr2.hasNext())
            returnSet.add(itr2.next().getUsername());
      }
      StringBuilder string = new StringBuilder();
      Iterator<String> itr3 = returnSet.iterator();
      while (itr3.hasNext()) {
         string.append(itr3.next() + "\n");
      }
         
      return string.toString();

   }


}
