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
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import Server.RMI.FollowService.CallbackService;
import Server.utils.Comment;
import Server.utils.Post;
import Server.utils.User;

import static Server.utils.ResultCode.*;

public class ServerManager {
   //private static final Gson gson = new Gson();
   private static ConcurrentLinkedQueue<Post> analyzeList = new ConcurrentLinkedQueue<>();
   private static final Database database = new Database(analyzeList);
   private static final ConcurrentHashMap<User, CallbackService> callbacksMap = new ConcurrentHashMap<>();
   private static final RewardCalculator r1;
   
   private static Selector selector;
   private static final ConcurrentHashMap<SocketChannel, User> usersLogged = new ConcurrentHashMap<>();
   

   static {
      try {
         selector = Selector.open();
      }
      catch (IOException e) {
         e.printStackTrace();
      }
      r1 = new RewardCalculator(analyzeList);
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
   public static Post createPost(String title, String content, User author) {
      Post post = new Post(database.getPreviousMaxPostID()+1, author, title, content);
      database.addPost(author, post);
      analyzeList.add(post);
      //database.saveDatabase();
      return post;

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
   public static int followUser(User u, String usernameToFollow) {
      User userToFollow = database.findUserByUsername(usernameToFollow);
      if (userToFollow == null) return USER_NOT_FOUND.getCode();
      u.getFollowing().add(userToFollow);
      userToFollow.getFollowers().add(u);
      return OK.getCode();
   }
   public static int unfollowUser(User u, String usernameToUnfollow) {
      User userToUnfollow = database.findUserByUsername(usernameToUnfollow);
      if (userToUnfollow == null) return USER_NOT_FOUND.getCode();
      if (u.getFollowing().remove(userToUnfollow) && userToUnfollow.getFollowers().remove(u))
         return OK.getCode();
      return NO_EFFECT.getCode();
   }

   public static int addComment(User u, Post post, String content) {
      if (post == null) return POST_NOT_FOUND.getCode();

      Comment comment = new Comment(u, content);
      post.getCommentsList().add(comment);
      analyzeList.add(post);
      return OK.getCode();
   }

   public static int rewinPost(User u, Post post) {
      if (post == null) return POST_NOT_FOUND.getCode();

      database.addPost(u, post);
      post.getRewinList().add(u);
      return OK.getCode();
   }

   public static int ratePost(User u, Post post, int vote) {
      if (post.getAuthor().equals(u))
         return RATE_OWN_POST.getCode();
      if (post.getLikersList().containsKey(u) || post.getDislikersList().containsKey(u))
         return ALREADY_RATED.getCode();
      if (!database.getUserPosts(u).contains(post))
         return RATE_BEFORE_REWIN.getCode();
      switch (vote) {
      case 1:
         post.getLikersList().put(u, Instant.now()); break;

      case -1:
         post.getDislikersList().put(u, Instant.now()); break;
      default:
         return ILLEGAL_OPERATION.getCode();
         }
      analyzeList.add(post); 
      return OK.getCode();
      }

   public static double getWalletAmount(User u) {
      return u.getCurrentCompensation();
      
   }

   public static double getBTCValue() {
      String randomURL = "https://www.random.org/decimal-fractions/?num=1&dec=5&col=2&format=plain&rnd=new";
      StringBuilder stringBuilder = new StringBuilder();
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
          while ((c = r.read()) != -1) {
              stringBuilder.append((char) c);
          }
          r.close();
         }
         catch (Exception e) {
            e.printStackTrace();
         }
          return Double.parseDouble(stringBuilder.toString());

   }
   public static HashSet<String> listUsers(User u) {
      LinkedList<String> userTags = u.getTags();
      HashSet<String> returnSet = new HashSet<>();
      Iterator<String> itr1 = userTags.iterator();
      while (itr1.hasNext()) {
         LinkedList<User> userTagList = database.getUsersOfTag(itr1.next());
         Iterator<User> itr2 = userTagList.iterator();
         while (itr2.hasNext())
            returnSet.add(itr2.next().getUsername());
      }
         
      return returnSet;

   }
   public static LinkedList<Post> showFeed(User u) {
      LinkedList<Post> feedList = new LinkedList<>();
      for (User followingUser: u.getFollowing())
      feedList.addAll(database.getUserPosts(followingUser));
      Collections.shuffle(feedList);
      return feedList;
   }

   public static void shutdown() {
      for (SocketChannel cc : usersLogged.keySet())
         logoutUser(cc);
      r1.shutdown();
      database.saveDatabase();
   }
   public static void addCallback(String username, CallbackService service) {
      callbacksMap.put(username, service);
  }

  public static void removeCallback(String username, CallbackService service) {
      if (username == null) return;

      callbacksMap.remove(username, service);
  }


}
