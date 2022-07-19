package utils;


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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import RMI.CallbackService;
import datastructures.Comment;
import datastructures.Post;
import datastructures.User;

public class ServerManager {
   private static final Database database = new Database();
   private static Selector selector;
   private static final ConcurrentHashMap<User, CallbackService> callbacksMap = new ConcurrentHashMap<>(); //mappa degli utenti registrati alle callbacks
   private static final ConcurrentHashMap<SocketChannel, User> usersLogged = new ConcurrentHashMap<>();
   private static ConcurrentLinkedQueue<Post> analyzeQueue = new ConcurrentLinkedQueue<>();
   private static Scheduler r1;

   static {
      r1 = new Scheduler(analyzeQueue, database.getPostDB());
      r1.start();
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
      if (username == null || password == null || clientChannel == null)
         return null; 
      User user = database.findUserByUsername(username); // cerco l'utente nel database
      if (user == null)
         return null;
      if (!User.hashEncrypt(password).equals(user.getPassword())) // se la password non è corretta
         return null;
      if (usersLogged.get(clientChannel) == null) { // se l'utente non è già loggato
         usersLogged.put(clientChannel, user); // lo loggo
         return user;
      } else
         return null;

   }

   public static int logout(SocketChannel clientChannel) {
      if (clientChannel == null)
         return 0;
      try {
         return usersLogged.remove(clientChannel) == null ? -1 : 0; // se l'utente non è loggato, ritorna -1, altrimenti 0
      } catch (NullPointerException e) {
         return -1;
      }

   }

   public static Post createPost(String title, String content, User author) {
      if (author == null || title == null || content == null)
         return null;
      Post post = new Post(database.getPreviousMaxPostID() + 1, author.getUsername(), title, content); // creo il post
      database.addPost(author, post); // lo aggiungo al database
      return post;
   }

   public static int deletePost(Post post) {
      if (post == null)
         return -1;
      try {
         database.deletePost(post); // lo elimino dal database
         analyzeQueue.remove(post); // lo elimino dalla lista dei post da analizzare
         return 0;
      } catch (Exception e) {
         e.printStackTrace();
         return -1;
      }

   }

   public static Post getPostByID(int ID) {
      if (ID < 0)
         return null;
      return database.getPostByID(ID); // cerco il post nel database
   }

   public static User findUserByUsername(String username) {
      if (username == null)
         return null;
      return database.findUserByUsername(username); // cerco l'utente nel database
   }

   public static Selector getSelector() {
      return selector;
   }

   public static User getUserLogged(SocketChannel clientChannel) {
      if (clientChannel == null)
         return null;
      return usersLogged.get(clientChannel); // cerco l'utente loggato
   }

   public static int followUser(User u, String usernameToFollow) {
      if (u == null || usernameToFollow == null)
         return -1;
      if (u.getUsername().equals(usernameToFollow)) // se l'utente sta cercando di seguire se stesso
         return -2;
      User userToFollow = database.findUserByUsername(usernameToFollow); // cerco l'utente da seguire nel database
      if (userToFollow == null)
         return -1;
      u.getFollowing().add(userToFollow.getUsername()); // seguo l'utente
      userToFollow.getFollowers().add(u.getUsername());
      CallbackService service = callbacksMap.get(userToFollow); // cerco la callback dell'utente da seguire
      if (service != null) {
         try {
            service.notifyNewFollower(u.getUsername()); // notifico l'utente da seguire che c'è un nuovo follower
         } catch (Exception e) {
            return 0;
         }
      }
      return 0;
   }

   public static int unfollowUser(User u, String usernameToUnfollow) {
      if (u == null || usernameToUnfollow == null)
         return -1;
      User userToUnfollow = database.findUserByUsername(usernameToUnfollow); // cerco l'utente da smettere di seguire nel database
      if (userToUnfollow == null)
         return -1;
      if (u.getFollowing().remove(userToUnfollow.getUsername()) && userToUnfollow.getFollowers().remove(u.getUsername())) { 
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
      return database.getUserPosts(u); // cerco i post dell'utente
   }

   public static int addComment(User u, Post post, String content) {
      if (post == null)
         return -1;
      Comment comment = new Comment(u.getUsername(), content); // creo il commento
      post.getCommentsList().add(comment); // aggiungo il commento al post
      analyzeQueue.add(post); // aggiungo il post alla lista dei post da analizzare
      return 0;
   }

   public static int rewinPost(User u, Post post) {
      if (post == null)
         return -1;
      u.addToUserPostList(post.getId()); // aggiungo il post alla lista dei post dell'utente
      post.getRewinList().add(u.getUsername()); // aggiungo l'utente alla lista dei rewin del post
      return 0;
   }

   public static int ratePost(User u, Post post, int vote) {
      if (post == null || u == null)
         return -1;
      if (post.getAuthor().equals(u.getUsername()))
         return -2; // non puoi votare il tuo post
      if (post.getLikersList().containsKey(u.getUsername()) || post.getDislikersList().containsKey(u.getUsername()))
         return -3; // non puoi votare un post già votato
      if (!post.getRewinList().contains(u.getUsername()))
         return -4; // non puoi votare un post di cui non hai prima effettuato il rewin
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
      analyzeQueue.add(post);
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
         int encodingStart = contentType.indexOf("charset="); // cerco l'encoding, che è dopo "charset=" 
         if (encodingStart != -1) {  // se ho trovato l'encoding
            encoding = contentType.substring(encodingStart + 8); // estraggo l'encoding dal content type
         }
         InputStream in = new BufferedInputStream(uc.getInputStream()); 
         Reader reader = new InputStreamReader(in, encoding); // creo un reader per l'input stream
         int c;
         while ((c = reader.read()) != -1) {
            stringBuilder.append((char) c); 
         }
         reader.close();
      } catch (Exception e) {
         e.printStackTrace();
      }
      return Double.parseDouble(stringBuilder.toString());

   }

   public static HashMap<String,LinkedList<String>> listUsers(User u) {
      if (u == null)
         return null;
      HashMap<String,LinkedList<String>> returnMap = new HashMap<>(); // mappa che contiene l'elenco degli utenti e i loro tags
      for (String tag : u.getTags())
         for (String username : database.getUsersOfTag(tag))
            returnMap.put(username, database.findUserByUsername(username).getTags());
      returnMap.remove(u.getUsername());
      return returnMap;
   }

   public static HashMap<String, LinkedList<String>> listFollowing(User u) {
      if (u == null)
         return null;
      HashMap<String, LinkedList<String>> returnMap = new HashMap<>(); // TODO mappa che contiene l'elenco degli utenti e i loro followings
      for (String username : u.getFollowing())
         returnMap.put(username, database.findUserByUsername(username).getTags());
      return returnMap;
   }
   public static HashMap<String, LinkedList<String>> listFollowers(User u) {
      if (u == null)
         return null;
      HashMap<String, LinkedList<String>> returnMap = new HashMap<>(); // TODO mappa che contiene l'elenco degli utenti e i loro followers
      for (String username : u.getFollowers())
         returnMap.put(username, database.findUserByUsername(username).getTags());
      return returnMap;
   }

   public static LinkedList<Post> showFeed(User u) {
      if (u == null)
         return null;
      LinkedList<Post> feedList = new LinkedList<>();
      for (String followingUser : u.getFollowing()) {
         feedList.addAll(database.getUserPosts(database.findUserByUsername(followingUser)));
      }
      Collections.shuffle(feedList); // ordino casualmente i post in modo da non avere una sequenza di post in ordine di data (molto più simile ad un vero feed)
      Iterator<Post> iterator = feedList.iterator();
      while (iterator.hasNext()) {
         Post post = iterator.next();
         if (post.getAuthor().equals(u.getUsername())) // se l'autore del post è l'utente corrente, lo rimuovo dalla lista
            iterator.remove();
      }
      return feedList;
   }

   public static void shutdown() {
      for (SocketChannel cc : usersLogged.keySet())
         logout(cc);
      r1.stop();
      database.saveDatabaseToFile();
   }

   public static LinkedList<String> receiveFollowersList(String username) {
      if (username == null)
         return null;
      User u = database.findUserByUsername(username); // cerco l'utente
      if (u == null)
         return null;
      ConcurrentLinkedQueue<String> queue = u.getFollowers(); // prendo la lista dei followers dell'utente
      LinkedList<String> returnList = new LinkedList<>();
      returnList.addAll(queue); // copio la lista dei followers dell'utente in una linkedlist (per il client)
      return returnList;

   }

   public static void registerForCallback(String username, CallbackService service) throws NullPointerException {
      User u = database.findUserByUsername(username);
      if (u == null)
         throw new NullPointerException();
      callbacksMap.putIfAbsent(u, service); //TODO

   }

   public static void unregisterForCallback(String username, CallbackService service) throws NullPointerException {
      if (username == null)
         throw new NullPointerException();
      callbacksMap.remove(username, service); // rimuovo il servizio dalla mappa
   }

   public static void saveServerState() {
      database.saveDatabaseToFile(); // salvo lo stato del server (necessario solo per l'autosalvataggio)
   }
  

}
