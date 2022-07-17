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

   private ConcurrentHashMap<String, User> userDB; // username -> user
   private ConcurrentHashMap<Integer,Post> postDB; // postID -> post
   private ConcurrentHashMap<String, LinkedList<String>> globalTagsList;
   private final ConcurrentLinkedQueue<Post> analyzeList; //TODO
/*    private final ConcurrentHashMap<String, LinkedList<Post>> trash;
   private final ConcurrentHashMap<String, LinkedList<String>> tags; */
   private final Gson gson;



   public Database(ConcurrentLinkedQueue<Post> analyzeList) {
      postDB = new ConcurrentHashMap<>();
      globalTagsList = new ConcurrentHashMap<>();
      userDB = new ConcurrentHashMap<>();
      this.analyzeList = analyzeList;

      gson = new GsonBuilder().setPrettyPrinting().create();
      
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
         userDB.clear();
         Type type = new TypeToken<ConcurrentHashMap<String, User>>(){}.getType();
         userDB = gson.fromJson(reader, type);
      }
      catch (Exception e) {
         System.out.println("File del database non presente o non valido.");
         userDB = new ConcurrentHashMap<>();
      }
      try (FileReader reader = new FileReader((postDBFile))) {
         postDB.clear();
         Type type = new TypeToken<ConcurrentHashMap<Integer,Post>>(){}.getType();
         postDB = gson.fromJson(reader, type);
      }
      catch (Exception e) {
         System.out.println("Impossibile caricare la lista dei post!");
         userDB = new ConcurrentHashMap<>();
      }
      try (FileReader reader = new FileReader((globalTagsListFile))) {
         globalTagsList.clear();
         Type type = new TypeToken<ConcurrentHashMap<String, LinkedList<String>>>(){}.getType();
         globalTagsList = gson.fromJson(reader, type);
      }
      catch (Exception e) {
         System.out.println("Impossibile caricare la lista dei tag!");
         userDB = new ConcurrentHashMap<>();
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
         return userDB.putIfAbsent(username, newUser)==null ? true : false;
      }

   public boolean addPost(User author, Post post) throws NullPointerException {
      if (postDB.putIfAbsent(post.getId(), post)!=null)
         return false;
      author.addToUserPostList((post.getId()));
      return true;
      //updateJSON();
      //save new database to json
      
   }
   
   public void deletePost(Post post) throws NullPointerException {
      User author = ServerManager.findUserByUsername(post.getAuthor());
      author.removeFromUserPostList(post.getId());
      postDB.remove(post.getId());
      return;
   }
   


   /*public User loginUser(String username, String password) {
      return findUserByUsername(username);
   }*/
   public User findUserByUsername(String username) throws NullPointerException{
      return userDB.get(username); 
   }
   public Post getPostByID(int ID) {
      return postDB.get(ID);
   }
   public LinkedList<Post> getUserPosts(User u) throws NullPointerException {
      LinkedList<Post> posts = new LinkedList<>();
      for (Integer id : u.getUserPostList()) {
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
   
