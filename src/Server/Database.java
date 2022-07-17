package Server;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import Server.Configs.ServerSettings;
import Server.utils.Post;
import Server.utils.User;

public class Database {

   private ConcurrentHashMap<String, User> userDB; // username -> user
   private ConcurrentHashMap<Integer, Post> postDB; // postID -> post
   private ConcurrentHashMap<String, LinkedList<String>> globalTagsMap;
   private final Gson gson;
   private static RewardCalculator r1;

   public Database() {
      postDB = new ConcurrentHashMap<>();
      globalTagsMap = new ConcurrentHashMap<>();
      userDB = new ConcurrentHashMap<>();
      gson = new GsonBuilder().setPrettyPrinting().create();
      r1 = new RewardCalculator(postDB);
      r1.startAnalyzing();

   }

   public void loadDatabaseFromFile() {
      File userDBFile = new File(ServerSettings.storagePath + "/userdb.json");
      File postDBFile = new File(ServerSettings.storagePath + "/postdb.json");
      File globalTagsMapFile = new File(ServerSettings.storagePath + "/globaltagslist.json");
      if (!userDBFile.exists() || !postDBFile.exists() || !globalTagsMapFile.exists()) {
         System.out.println("Database pre-esistente non trovato: Verrà creato un nuovo database e salvato in " + ServerSettings.storagePath);
         return;
      }
      try (FileReader reader = new FileReader(userDBFile)) {
         userDB.clear();
         Type type = new TypeToken<ConcurrentHashMap<String, User>>() {
         }.getType();
         userDB = gson.fromJson(reader, type);
      } catch (Exception e) {
         System.out.println("File del database non presente o non valido.");
         userDB = new ConcurrentHashMap<>();
      }
      try (FileReader reader = new FileReader((postDBFile))) {
         postDB.clear();
         Type type = new TypeToken<ConcurrentHashMap<Integer, Post>>() {
         }.getType();
         postDB = gson.fromJson(reader, type);
      } catch (Exception e) {
         System.out.println("Impossibile caricare la lista dei post!");
         postDB = new ConcurrentHashMap<>();
      }
      try (FileReader reader = new FileReader((globalTagsMapFile))) {
         globalTagsMap.clear();
         Type type = new TypeToken<ConcurrentHashMap<String, LinkedList<String>>>() {
         }.getType();
         globalTagsMap = gson.fromJson(reader, type);
      } catch (Exception e) {
         System.out.println("Impossibile caricare la lista dei tag!");
         globalTagsMap = new ConcurrentHashMap<String, LinkedList<String>>();
      }
      System.out.println("Database pre-esistente in "+ ServerSettings.storagePath + " trovato e caricato.");
   }

   public void saveDatabaseToFile() {
      File userDBFile = new File(ServerSettings.storagePath + "/userdb.json");
      File postDBFile = new File(ServerSettings.storagePath + "/postdb.json");
      File globalTagsListFile = new File(ServerSettings.storagePath + "/globaltagslist.json");
      try (FileWriter writer = new FileWriter(userDBFile)) {
         gson.toJson(userDB, writer);
      } catch (IOException e) {
         System.out.println("Impossibile salvare il database!");
         return;
      }
      try (FileWriter writer = new FileWriter(postDBFile)) {
         gson.toJson(postDB, writer);
      } catch (IOException e) {
         System.out.println("Impossibile salvare la lista dei post!");
         return;
      }
      try (FileWriter writer = new FileWriter(globalTagsListFile)) {
         gson.toJson(globalTagsMap, writer);
      } catch (IOException e) {
         System.out.println("Impossibile salvare la lista dei tag!");
         return;
      }
   }

   public boolean registerUser(String username, String password, LinkedList<String> tagsList) {
      // E' null se l'utente non esisteva e può essere registrato, altrimenti ritorna
      // un'username dunque il login sarà fallito.
      User newUser = new User(username, password, tagsList);
      Iterator<String> itr = tagsList.iterator();
      while (itr.hasNext()) {
         String newTag = itr.next();
         globalTagsMap.putIfAbsent(newTag, new LinkedList<String>());
         globalTagsMap.get(newTag).add(newUser.getUsername());
      }
      return userDB.putIfAbsent(username, newUser) == null ? true : false;
   }

   public boolean addPost(User author, Post post) throws NullPointerException {
      if (postDB.putIfAbsent(post.getId(), post) != null)
         return false; //returna false in caso si tratti di un rewin
      author.addToUserPostList((post.getId()));
      return true;
   }

   public void deletePost(Post post) throws NullPointerException {
      User author = ServerManager.findUserByUsername(post.getAuthor());
      author.removeFromUserPostList(post.getId());
      postDB.remove(post.getId());
      return;
   }

   public User findUserByUsername(String username) throws NullPointerException {
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
      return globalTagsMap.get(tag);
   }

   public void shutdownDatabase() {
      r1.shutdown();
      saveDatabaseToFile();
   }
}
