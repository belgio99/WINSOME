package utils;


import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import datastructures.Post;
import datastructures.User;
import settings.ServerSettings;

public class Database {

   private ConcurrentHashMap<String, User> userDB; // username -> user
   private ConcurrentHashMap<Integer, Post> postDB; // postID -> post
   private ConcurrentHashMap<String, LinkedList<String>> globalTagsMap;
   private final Gson gson;

   public Database() {
      postDB = new ConcurrentHashMap<>();
      globalTagsMap = new ConcurrentHashMap<>();
      userDB = new ConcurrentHashMap<>();
      gson = new GsonBuilder().setPrettyPrinting().create();
      loadDatabaseFromFile();
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
         userDB = gson.fromJson(reader, new TypeToken<ConcurrentHashMap<String, User>>(){}.getType());
         if (userDB == null)
            userDB = new ConcurrentHashMap<>();
      } catch (Exception e) {
         System.out.println("File del database non presente o non valido.");
         userDB = new ConcurrentHashMap<>();
      }
      try (FileReader reader = new FileReader((postDBFile))) {
         postDB.clear();
         postDB = gson.fromJson(reader, new TypeToken<ConcurrentHashMap<Integer, Post>>() {}.getType());
         if (postDB == null)
            postDB = new ConcurrentHashMap<>();
      } catch (Exception e) {
         System.out.println("Impossibile caricare la lista dei post!");
         postDB = new ConcurrentHashMap<>();
      }
      try (FileReader reader = new FileReader((globalTagsMapFile))) {
         globalTagsMap.clear();
         globalTagsMap = gson.fromJson(reader, new TypeToken<ConcurrentHashMap<String, LinkedList<String>>>() {}.getType());
         if (globalTagsMap == null)
            globalTagsMap = new ConcurrentHashMap<>();
      } catch (Exception e) {
         System.out.println("Impossibile caricare la lista dei tag!");
         globalTagsMap = new ConcurrentHashMap<>();
      }
      System.out.println("Database pre-esistente in "+ ServerSettings.storagePath + " trovato e caricato.");
   }

   public synchronized void saveDatabaseToFile() {
      File userDBFile = new File(ServerSettings.storagePath + "/userdb.json");
      File postDBFile = new File(ServerSettings.storagePath + "/postdb.json");
      File globalTagsListFile = new File(ServerSettings.storagePath + "/globaltagslist.json");
      synchronized (userDB) {
      try (FileWriter writer = new FileWriter(userDBFile)) {
         gson.toJson(userDB, writer);
      } catch (IOException e) {
         System.out.println("Impossibile salvare il database!");
         return;
      }
   }
      synchronized (postDB) {
      try (FileWriter writer = new FileWriter(postDBFile)) {
         gson.toJson(postDB, writer);
      } catch (IOException e) {
         System.out.println("Impossibile salvare la lista dei post!");
         return;
      }
   }
      synchronized (globalTagsMap) {
      try (FileWriter writer = new FileWriter(globalTagsListFile)) {
         gson.toJson(globalTagsMap, writer);
      } catch (IOException e) {
         System.out.println("Impossibile salvare la lista dei tag!");
         return;
      }
   }
   }

   public boolean registerUser(String username, String password, LinkedList<String> tagsList) {
      // E' null se l'utente non esisteva e può essere registrato, altrimenti ritorna
      // un'username dunque il login sarà fallito.
      User newUser = new User(username, password, tagsList);
      Iterator<String> itr = tagsList.iterator();
      while (itr.hasNext()) {
         String newTag = itr.next().toLowerCase();
         globalTagsMap.putIfAbsent(newTag, new LinkedList<>());
         globalTagsMap.get(newTag).add(newUser.getUsername());
      }
      return userDB.putIfAbsent(username, newUser) == null ? true : false;
   }

   public boolean addPost(User u, Post post) throws NullPointerException {
      if (postDB.putIfAbsent(post.getId(), post) != null)
         return false;
      u.addToUserPostList((post.getId()));
      return true;
   }

   public void deletePost(Post post) throws NullPointerException {
      User author = ServerManager.findUserByUsername(post.getAuthor());
      ConcurrentLinkedQueue<String> rewinList = post.getRewinList();
      Iterator<String> itr = rewinList.iterator();
      while (itr.hasNext()) {
         String username = itr.next();
         User u = ServerManager.findUserByUsername(username);
         u.removeFromUserRewinList(post.getId());
      }
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

   public LinkedList<Post> getUserPosts(User u) throws NullPointerException { //Prende anche i post del rewin
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

   public ConcurrentHashMap<Integer, Post> getPostDB() {
      return postDB;
   }
}
