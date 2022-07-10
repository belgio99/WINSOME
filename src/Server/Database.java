package Server;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import Server.Configs.DefaultValues;
import Server.utils.Post;
import Server.utils.User;


public class Database {
   private final ConcurrentHashMap<User, LinkedList<Post>> database;
   private final ConcurrentHashMap<String, User> userDB;
   private final ConcurrentLinkedDeque<Post> postList;
   private final ConcurrentHashMap<String, LinkedList<User>> globalTagsList;
   private final ConcurrentLinkedQueue<Post> analyzeList;
/*    private final ConcurrentHashMap<String, LinkedList<Post>> trash;
   private final ConcurrentHashMap<String, LinkedList<String>> tags; */
   private final Gson gson;



   public Database(ConcurrentLinkedQueue<Post> analyzeList) {
      database = new ConcurrentHashMap<>();
      postList = new ConcurrentLinkedDeque<>();
      globalTagsList = new ConcurrentHashMap<>();
      userDB = new ConcurrentHashMap<>();
      this.analyzeList = analyzeList;
      //trash = new ConcurrentHashMap<>();
      //usersLogged = new ConcurrentHashMap<>();

      gson = new GsonBuilder().setPrettyPrinting().create();
      //loadDatabase();
      

      File dbfolder = new File(DefaultValues.serverval.databasePath);
      if (!dbfolder.exists())
         if (!dbfolder.mkdir()) {
            System.out.println("Impossibile creare il database!");
            System.exit(1);
         }
      
      

   }
   public void loadDatabaseFromFile() {
      File dbFile = new File(DefaultValues.serverval.databasePath + "/" + DefaultValues.serverval.databaseFile);
      if (!dbFile.exists()) {
         try {
            dbFile.createNewFile();
         }
         catch (IOException e) {
            e.printStackTrace();
         }
      }
      try {
         FileReader fr = new FileReader(dbFile);
         database.clear();
         postList.clear();
         globalTagsList.clear();
         userDB.clear();
         //trash.clear();
         //tags.clear();
         database.putAll(gson.fromJson(fr, ConcurrentHashMap.class));
         fr.close();
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void saveDatabaseToFile() {
      File dbFile = new File(DefaultValues.serverval.databasePath + "/" + DefaultValues.serverval.databaseFile);
      try {
         FileWriter fw = new FileWriter(dbFile);
         fw.write(gson.toJson(database));
         fw.close();
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }
   
   public synchronized void updatePost(Post post) {
      User author = post.getAuthor();

      if (database.containsKey(author)) {
          LinkedList<Post> posts = database.get(author);
          int index;
          if ((index = posts.indexOf(post)) >= 0) {
              posts.set(index, post);
              return;
          }
      }

  }
   public boolean registerUser(String username, String password, LinkedList<String> tagsList) {
         //E' null se l'utente non esisteva e può essere registrato, altrimenti ritorna un'username dunque il login sarà fallito.
         User newUser = new User(username, password, tagsList);
         Iterator<String> itr = tagsList.iterator();
         while (itr.hasNext()) {
            String newTag = itr.next();
            globalTagsList.putIfAbsent(newTag, new LinkedList<User>());
            globalTagsList.get(newTag).add(newUser);
         }
         userDB.putIfAbsent(username, newUser);
         saveDatabase();
         return (database.putIfAbsent(newUser, new LinkedList<Post>()) == null) ? true: false;
      }
   public boolean addPost(User author, Post post) {
      LinkedList<Post> oldPostList = database.get(author);
      if (!oldPostList.offerFirst(post)) return false;
      if (!postList.offerFirst(post)) return false;
      return (database.replace(author, oldPostList) == null) ? false : true;
      //updateJSON();
      //save new database to json
      

   }
   
   public boolean deletePost(Post post) {
      database.get(post.getAuthor()).remove(post);
      postList.remove(post);
      return true;
   }
   


   public boolean saveDatabase() {
      try (FileWriter writer = new FileWriter(DefaultValues.serverval.databasePath+"/database.json")){
         gson.toJson(database, writer);
         writer.flush();
      } 
      catch (Exception e)
      {
         e.printStackTrace();
         return false;
      }
      try (FileWriter writer2 = new FileWriter(DefaultValues.serverval.databasePath+"/userdb.json")) {
         gson.toJson(userDB, writer2);
         writer2.flush();
      }
      catch (Exception e)
      {
         e.printStackTrace();
         return false;
      }
      try (FileWriter writer3 = new FileWriter(DefaultValues.serverval.databasePath+"/analyzelist.json")) {
         gson.toJson(analyzeList, writer3);
         return true;
      }
      catch (Exception e)
      {
         e.printStackTrace();
         return false;
      }
   }
   /*public User loginUser(String username, String password) {
      return findUserByUsername(username);
   }*/
   public User findUserByUsername(String username) {
      return database.keySet()
                     .stream()
                     .parallel()
                     .filter(p -> p.getUsername().equals(username))
                     .findFirst()
                     .orElse(null);
   }
   public Post getPostByID(int ID) {
      Iterator<Post> itr = postList.iterator();
      while (itr.hasNext()) {
         Post temp = itr.next();
         if (temp.getIDPost() == ID)
            return temp;
      }
      return null;
   }
   public LinkedList<Post> getUserPosts(User u) {
      return database.get(u);
   }
   public int getPreviousMaxPostID() {
      try {
         return postList.getFirst().getIDPost();
      }
      catch (NoSuchElementException e) {
         return 0;
      }
   }
   public LinkedList<User> getUsersOfTag (String tag) {
      return globalTagsList.get(tag);
   }

   /*public static boolean addUserLogged(User user, SocketChannel client) {
      return usersLogged.putIfAbsent(client, user) == null ? true : false;
   }
   public static boolean logoutUser(User user, SocketChannel client) {
      return usersLogged.remove(client, user);
   }
   public User getUserLogged(SocketChannel clientChannel) {
      return usersLogged.get(clientChannel);*/
   }
   
