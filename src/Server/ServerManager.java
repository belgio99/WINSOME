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

import Server.RMI.CallbackService;
import Server.utils.Comment;
import Server.utils.Post;
import Server.utils.User;

public class ServerManager {
   // private static final Gson gson = new Gson();
   //private static ConcurrentLinkedQueue<Post> analyzeList = new ConcurrentLinkedQueue<>();
   private static final Database database = new Database();
   private static final ConcurrentHashMap<User, CallbackService> callbacksMap = new ConcurrentHashMap<>(); //mappa degli utenti registrati alle callbacks

   private static Selector selector;
   private static final ConcurrentHashMap<SocketChannel, User> usersLogged = new ConcurrentHashMap<>();

   static {
      try {
         selector = Selector.open();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }



   public static int register(String username, String password, LinkedList<String> tagsList) {
      if (!database.registerUser(username, password, tagsList))
         return -1;
      int score = 0;
      // se la password è lunga più di 8 caratteri, aumenta il punteggio di 1
      if (password.length() > 8) {
         score += 1;
      } // se la password contiene dei caratteri minuscoli, aumenta il punteggio di 1
      if (password.matches(".*[a-z].*")) {
         score += 1;
      } // se la password contiene dei caratteri maiuscoli, aumenta il punteggio di 1
      if (password.matches(".*[A-Z].*")) {
         score += 1;
      }
      // se la password contiene dei numeri, aumenta il punteggio di 1
      if (password.matches(".*[0-9].*")) {
         score += 1;
      } // se la password contiene un carattere speciale, aumenta il punteggio di 1
      if (password.matches(".*[^a-zA-Z0-9].*")) {
         score += 1;
      }

      return score;
   }

   public static User login(String username, String password, SocketChannel clientChannel) {
      User user = database.findUserByUsername(username);
      if (user == null)
         return null;
      if (!password.equals(user.getPassword()))
         return null;
      if (usersLogged.get(clientChannel) == null) {
         usersLogged.put(clientChannel, user);
         return user;
      } else
         return null;

   }

   public static int logout(SocketChannel clientChannel) {
      try {
         return usersLogged.remove(clientChannel) == null ? -1 : 0;
      } catch (NullPointerException e) {
         return -1;
      }

   }

   public synchronized static Post createPost(String title, String content, User author) {
      Post post = new Post(database.getPreviousMaxPostID() + 1, author.getUsername(), title, content);
      database.addPost(author, post);
      // database.saveDatabase();
      return post;
   }

   public synchronized static int deletePost(Post post) {
      try {
         database.deletePost(post);
         return 0;
      } catch (Exception e) {
         return -1;
      }

   }

   public static Post getPostByID(int ID) {
      if (ID < 0)
         return null;
      return database.getPostByID(ID);
   }

   public static User findUserByUsername(String username) {
      if (username == null)
         return null;
      return database.findUserByUsername(username);
   }

   public static Selector getSelector() {
      return selector;
   }

   public static User getUserLogged(SocketChannel clientChannel) {
      if (clientChannel == null)
         return null;
      return usersLogged.get(clientChannel);
   }

   public synchronized static int followUser(User u, String usernameToFollow) {
      User userToFollow = database.findUserByUsername(usernameToFollow);
      if (userToFollow == null)
         return -1;
      u.getFollowing().add(userToFollow);
      userToFollow.getFollowers().add(u);
      CallbackService service = callbacksMap.get(userToFollow);
      if (service != null) {
         try {
            service.notifyNewFollower(u.getUsername());
         } catch (Exception e) {
            return 0;
         }
      }
      return 0;
   }

   public synchronized static int unfollowUser(User u, String usernameToUnfollow) {
      User userToUnfollow = database.findUserByUsername(usernameToUnfollow);
      if (userToUnfollow == null)
         return -1;
      if (u.getFollowing().remove(userToUnfollow) && userToUnfollow.getFollowers().remove(u)) {
         CallbackService service = callbacksMap.get(userToUnfollow);
         if (service != null) {
            try {
               service.notifyNewUnfollower(u.getUsername());
            } catch (Exception e) {
               return 0;
            }
         }
         return 0;
      }
      return 1;
   }

   public static LinkedList<Post> viewBlog(User u) {
      if (u == null)
         return null;
      return database.getUserPosts(u);
   }

   public static int addComment(User u, Post post, String content) {
      if (post == null)
         return -1;
      Comment comment = new Comment(u, content);
      post.getCommentsList().add(comment);
      return 0;
   }

   public static int rewinPost(User u, Post post) {
      if (post == null)
         return -1;
      database.addPost(u, post);
      post.getRewinList().add(u.getUsername());
      return 0;
   }

   public static int ratePost(User u, Post post, int vote) {
      if (post.getAuthor().equals(u.getUsername()))
         return -2; // non puoi votare il tuo post
      if (post.getLikersList().containsKey(u.getUsername()) || post.getDislikersList().containsKey(u.getUsername()))
         return -3; // non puoi votare un post già votato
      if (!post.getRewinList().contains(u.getUsername()))
         return -4; // non puoi votare un post di cui non hai effettuato il rewin
      switch (vote) {
         case 1:
            post.getLikersList().put(u.getUsername(), Instant.now());
            break;

         case -1:
            post.getDislikersList().put(u.getUsername(), Instant.now());
            break;
         default:
            return -1;
      }
      return 0;
   }

   public static double getWalletAmount(User u) {
      if (u == null)
         return -1;
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
      } catch (Exception e) {
         e.printStackTrace();
      }
      return Double.parseDouble(stringBuilder.toString());

   }

   public static HashSet<String> listUsers(User u) {
      LinkedList<String> userTags = u.getTags();
      HashSet<String> returnSet = new HashSet<>();
      Iterator<String> itr1 = userTags.iterator();
      while (itr1.hasNext()) {
         LinkedList<String> userTagList = database.getUsersOfTag(itr1.next());
         Iterator<String> itr2 = userTagList.iterator();
         while (itr2.hasNext())
            returnSet.add(itr2.next());
      }
      return returnSet;

   }

   public static HashSet<String> listFollowing(User u) {
      HashSet<String> returnSet = new HashSet<>();
      Iterator<User> itr = u.getFollowing().iterator();
      while (itr.hasNext()) {
         returnSet.add(itr.next().getUsername());
      }
      return returnSet;
   }

   public static LinkedList<Post> showFeed(User u) {
      LinkedList<Post> feedList = new LinkedList<>();
      for (User followingUser : u.getFollowing())
         feedList.addAll(database.getUserPosts(followingUser));
      Collections.shuffle(feedList);
      return feedList;
   }

   public static void shutdown() {
      for (SocketChannel cc : usersLogged.keySet())
         logout(cc);
      database.shutdownDatabase();
   }

   public static LinkedList<String> receiveFollowersList(String username) {
      User u = database.findUserByUsername(username);
      if (u == null)
         return null;
      ConcurrentLinkedQueue<User> queue = u.getFollowers();
      LinkedList<String> returnList = new LinkedList<>();
      Iterator<User> itr = queue.iterator();
      while (itr.hasNext()) {
         returnList.add(itr.next().getUsername());
      }
      return returnList;

   }

   public static void registerForCallback(String username, CallbackService service) throws NullPointerException {
      User u = database.findUserByUsername(username);
      if (u == null)
         throw new NullPointerException();
      callbacksMap.putIfAbsent(u, service);

   }

   public static void unregisterForCallback(String username, CallbackService service) throws NullPointerException {
      if (username == null)
         throw new NullPointerException();
      callbacksMap.remove(username, service);
   }

   public static void saveServerState() {
      database.saveDatabaseToFile();
   }
  

}
