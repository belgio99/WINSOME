package Server;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import Server.Configs.Settings;
import Server.utils.Post;
import Server.utils.User;


public class Database {
   private final ConcurrentHashMap<User, LinkedList<Integer>> database;
   private final ConcurrentHashMap<String, User> userDB; // username -> user
   private final ConcurrentHashMap<Integer,Post> postDB; // postID -> post
   private final ConcurrentHashMap<String, LinkedList<String>> globalTagsList;
   private final ConcurrentLinkedQueue<Post> analyzeList;
/*    private final ConcurrentHashMap<String, LinkedList<Post>> trash;
   private final ConcurrentHashMap<String, LinkedList<String>> tags; */
   private final Gson gson;



   public Database(ConcurrentLinkedQueue<Post> analyzeList) {
      database = new ConcurrentHashMap<>();
      postDB = new ConcurrentHashMap<>();
      globalTagsList = new ConcurrentHashMap<>();
      userDB = new ConcurrentHashMap<>();
      this.analyzeList = analyzeList;
      //trash = new ConcurrentHashMap<>();
      //usersLogged = new ConcurrentHashMap<>();

      gson = new GsonBuilder().setPrettyPrinting().create();
      //loadDatabase();
      
   }
   public void loadDatabaseFromFile() {
      File userDBFile = new File(Settings.serverSettings.storagePath + "/userdb.json");
      File postDBFile = new File(Settings.serverSettings.storagePath + "/postdb.json");
      File globalTagsListFile = new File(Settings.serverSettings.storagePath + "/globaltagslist.json");
      if (!userDBFile.exists() || !postDBFile.exists() || !globalTagsListFile.exists()) {
         System.out.println("Impossibile caricare il database!");
         return;
      }
      try (FileReader reader = new FileReader(userDBFile)) {
         database.clear();
         Type type = new TypeToken<ConcurrentHashMap<User, LinkedList<Integer>>>(){}.getType();
         database.putAll(gson.fromJson(reader, type));
      }
      catch (Exception e) {
         System.out.println("File del database non presente o non valido.");
         return;
      }
      try (FileReader reader = new FileReader((postDBFile))) {
         postDB.clear();
         Type type = new TypeToken<ConcurrentHashMap<Integer,Post>>(){}.getType();
         postDB.putAll(gson.fromJson(reader, type));
      }
      catch (Exception e) {
         System.out.println("Impossibile caricare la lista dei post!");
         return;
      }
      try (FileReader reader = new FileReader((globalTagsListFile))) {
         globalTagsList.clear();
         Type type = new TypeToken<ConcurrentHashMap<String, LinkedList<String>>>(){}.getType();
         globalTagsList.putAll(gson.fromJson(reader, type));
      }
      catch (Exception e) {
         System.out.println("Impossibile caricare la lista dei tag!");
         return;
      }
   }

   public void saveDatabaseToFile() {
      File userDBFile = new File(Settings.serverSettings.storagePath + "/userdb.json");
      File postDBFile = new File(Settings.serverSettings.storagePath + "/postdb.json");
      File globalTagsListFile = new File(Settings.serverSettings.storagePath + "/globaltagslist.json");
      try (FileWriter writer = new FileWriter(userDBFile)) {
         gson.toJson(userDB, writer);
      }
      catch (IOException e) {
         System.out.println("Impossibile salvare il database!");
         return;
      }
      try (FileWriter writer = new FileWriter(postDBFile)) {
         gson.toJson(postDB, writer);
      }
      catch (IOException e) {
         System.out.println("Impossibile salvare la lista dei post!");
         return;
      }
      try (FileWriter writer = new FileWriter(globalTagsListFile)) {
         gson.toJson(globalTagsList, writer);
      }
      catch (IOException e) {
         System.out.println("Impossibile salvare la lista dei tag!");
         return;
      }
   }
   
   /*public synchronized void updatePost(Post post) {
      User author = ServerManager.findUserByUsername(post.getAuthor());
      if (database.containsKey(author)) {
         for (Integer id : database.get(author)) {
            postDB.get(id)
            }
         }
         postDB.get(key)
          LinkedList<Post> posts = database.get(author);
          int index;
          if ((index = posts.indexOf(post)) >= 0) {
              posts.set(index, post);
              return;
          }
      }

  }*/
   public boolean registerUser(String username, String password, LinkedList<String> tagsList) {
         //E' null se l'utente non esisteva e può essere registrato, altrimenti ritorna un'username dunque il login sarà fallito.
         User newUser = new User(username, password, tagsList);
         Iterator<String> itr = tagsList.iterator();
         while (itr.hasNext()) {
            String newTag = itr.next();
            globalTagsList.putIfAbsent(newTag, new LinkedList<String>());
            globalTagsList.get(newTag).add(newUser.getUsername());
         }
         userDB.putIfAbsent(username, newUser);
         return (database.putIfAbsent(newUser, new LinkedList<Integer>()) == null) ? true: false;
      }

   public boolean addPost(User author, Post post) throws NullPointerException {
      //this needs to be done in a thread-safe way
      if (postDB.putIfAbsent(post.getId(), post)==null) {
         database.get(author).addFirst(post.getId());
         return true;
      }
      return false;
      //updateJSON();
      //save new database to json
      

   }
   
   public void deletePost(Post post) throws NullPointerException {
      User author = ServerManager.findUserByUsername(post.getAuthor());
      database.get(author).remove(post.getId());
      postDB.remove(post.getId());
      return;
   }
   


   public boolean saveDatabase() {
      try (FileWriter writer = new FileWriter(Settings.serverSettings.storagePath+"/database.json")){
         gson.toJson(database, writer);
         writer.flush();
      } 
      catch (Exception e)
      {
         e.printStackTrace();
         return false;
      }
      try (FileWriter writer2 = new FileWriter(Settings.serverSettings.storagePath+"/userdb.json")) {
         gson.toJson(userDB, writer2);
         writer2.flush();
      }
      catch (Exception e)
      {
         e.printStackTrace();
         return false;
      }
      try (FileWriter writer3 = new FileWriter(Settings.serverSettings.storagePath+"/analyzelist.json")) {
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
      return postDB.get(ID);
   }
   public LinkedList<Post> getUserPosts(User u) throws NullPointerException {
      LinkedList<Post> posts = new LinkedList<>();
      for (Integer id : database.get(u)) {
         posts.add(postDB.get(id));
      }
      return posts;
   }

   public int getPreviousMaxPostID() {
         return postDB.size();
   }
   public LinkedList<String> getUsersOfTag(String tag) throws NullPointerException {
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
   
