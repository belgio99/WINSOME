package utils;

import static utils.ServerUtils.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import datastructures.Comment;
import datastructures.Post;
import datastructures.Transaction;
import datastructures.User;

public class ServerRequestHandler implements Runnable {

   private final SocketChannel clientChannel;
   private final ByteBuffer buffer;
   private final Selector selector;
   private final ConcurrentLinkedQueue<SocketChannel> registerQueue;
   static int columnSize = 15;

   public ServerRequestHandler(SocketChannel clientChannel, Selector selector,
         ConcurrentLinkedQueue<SocketChannel> registerQueue) {
      this.clientChannel = clientChannel;
      this.selector = selector;
      buffer = ByteBuffer.allocate(1024);
      this.registerQueue = registerQueue;
   }

   private String receiveRequest() throws IOException {
      StringBuilder clientRequest = new StringBuilder();
      int numBytes;
      while ((numBytes = clientChannel.read(buffer)) > 0) {
         clientRequest.append(new String(buffer.array(), 0, numBytes));
         buffer.clear();

      }
      return clientRequest.toString();
   }

   public void run() {
      String response = "";
      String[] requestSplit;
      try {
         String clientRequest = receiveRequest(); // ricevo la richiesta del client
         requestSplit = clientRequest.trim().split(" "); // splitto la richiesta del client in base al carattere di spazio
         if (requestSplit.length < 1 || requestSplit[0].trim().isEmpty()) { // se la richiesta è vuota o contiene solo spazi
            response = "L'input non è corretto!";
            sendString(clientChannel, response);
            registerKey(); // registro la chiave del client nel selector
            return;
         }
         requestSplit[0] = requestSplit[0].trim();
         if (requestSplit[0].equals("login")) {
            if (requestSplit.length != 3 || requestSplit[1].trim().isEmpty()
                  || requestSplit[2].trim().isEmpty()) { 
               sendString(clientChannel, "L'input non è corretto!");
               registerKey();
               return;
            }
            sendString(clientChannel, login(requestSplit));
            registerKey();
            return;
         }
         // Ora controllo se l'utente è loggato
         User u;
         if ((u = ServerManager.getUserLogged(clientChannel)) == null) {
            sendString(clientChannel, "Utente non loggato!");
            registerKey();
            return;
         }
         switch (requestSplit[0]) { // controllo la prima parte della richiesta
            case "help":
               response = printHelp();
            case "logout":
               response = logout();
               break;
            case "list":
               if (requestSplit.length < 2 || requestSplit[1].trim().isEmpty()) { // se la richiesta è vuota o contiene solo spazi
                  response = "L'input non è corretto!";
               } else
                  switch (requestSplit[1]) {
                     case "users":
                        response = listUsers(u);
                        break;
                     case "following":
                        response = listFollowing(u);
                        break;
                     case "followers":
                        response = listFollowers(u);
                        break;
                     default:
                        response = "Operazione non valida!"; 
                        break;
                  }
               break;
            case "show":
               if (requestSplit.length < 2 || requestSplit[1].trim().isEmpty()) {
                  response = "L'input non è corretto!";
               } else
                  switch (requestSplit[1]) {
                     case "feed":
                        response = showFeed(u);
                        break;
                     case "post":
                        response = showPost(requestSplit);
                        break;
                     default:
                        response = "Operazione non valida!";
                        break;
                  }
               break;
            case "post":
               response = createPost(u, clientRequest.replaceFirst("post ", ""));
               break;
            case "delete":
               response = deletePost(u, requestSplit);
               break;
            case "follow":
               response = followUser(u, requestSplit);
               break;
            case "unfollow":
               response = unfollowUser(u, requestSplit);
               break;
            case "comment":
               response = commentPost(u, clientRequest.replaceFirst("comment ", ""));
               break;
            case "rewin":
               response = rewinPost(u, requestSplit);
               break;
            case "rate":
               response = ratePost(u, requestSplit);
               break;
            case "blog":
               response = viewBlog(u);
               break;
            case "wallet":
               if (requestSplit.length == 1)
                  response = getWallet(u);
               else if (requestSplit[1].trim().isEmpty())
                  response = "L'input non è corretto!";
               else if (requestSplit[1].equals("btc"))
                  response = getWalletBTC(u);
               else
                  response = "L'input non è corretto!";
               break;
            default:
               response = "L'input non è corretto!";
               break;
         }
         sendString(clientChannel, response); // invio la risposta al client
         registerKey(); // registro la chiave del client nel selector
      } catch (IOException e) {
         System.out.println("Un client si è disconnesso!");
         ServerManager.logout(clientChannel);
      }

   }

   private void registerKey() {
      registerQueue.add(clientChannel); // Aggiungo il client alla coda di registrazione
      selector.wakeup(); // Risveglio il selector per far partire la registrazione
   }

   private String login(String[] split) {
      if (split.length != 3 || split[1].trim().isEmpty() || split[2].trim().isEmpty())
         return "L'input non è corretto!";
      String username = split[1];
      String password = split[2];
      User u = ServerManager.login(username, password, clientChannel);
      if (u == null) {
         return "Errore!";
      } else
         return "Operazione completata";
   }

   private String logout() {
      if (ServerManager.logout(clientChannel) == -1)
         return "Utente non loggato";
      else
         return "Operazione completata";
   }

   private String createPost(User u, String clientRequest) throws IOException {
      String[] split = fixArray(clientRequest); // rimuovo gli spazi iniziali e finali, e gestisco le virgolette
      if (split.length != 3 || split[0].trim().isEmpty() || split[2].trim().isEmpty())
         return "L'input non è corretto!";
      String title = split[0];
      String content = split[2];
      Post post = ServerManager.createPost(title, content, u);
      if (post == null)
         return "Operazione non valida!";
      return "Operazione completata: ID del post: " + post.getId();
   }

   private String deletePost(User u, String[] requestSplit) {
      requestSplit[1] = requestSplit[1].trim();
      int postID;
      try {
         postID = Integer.parseInt(requestSplit[1]);
      } catch (IllegalArgumentException e) {
         return "L'input non è corretto!";
      }
      Post post = ServerManager.getPostByID(postID);
      if (post == null)
         return "Post non trovato!";
      if (!post.getAuthor().equals(u.getUsername()))
         return "Utente non autorizzato!";
      return ServerManager.deletePost(post) == 0 ? "Operazione completata" : "Operazione non permessa!";
   }

   private String followUser(User u, String[] requestSplit) {
      requestSplit[1] = requestSplit[1].trim();
      if (requestSplit[1].trim().isEmpty())
         return "L'input non è corretto!";

      switch (ServerManager.followUser(u, requestSplit[1])) {
         case 0:
            return "Operazione completata";
         case -1:
            return "Utente non trovato!";
         case -2:
            return "Non si può seguire se stessi!";
         default:
            return "Errore sconosciuto!";
      }
   }

   private String unfollowUser(User u, String[] requestSplit) {
      requestSplit[1] = requestSplit[1].trim();
      if (requestSplit[1].trim().isEmpty())
         return "L'input non è corretto!";

      switch (ServerManager.unfollowUser(u, requestSplit[1])) {
         case 0:
            return "Operazione completata";
         case -1:
            return "Utente non trovato!";
         case 1:
            return "L'azione non ha avuto alcun effetto!";
         default:
            return "Errore sconosciuto!";
      }
   }

   private String viewBlog(User u) {
      int numPostsToShow = 10;
      StringBuilder msg = new StringBuilder();
      Iterator<Post> itr = ServerManager.viewBlog(u).iterator();
      msg.append("< ID");
      for (int i = 0; i < columnSize - 2; i++)
         msg.append(" ");
      msg.append("Autore");
      for (int i = 0; i < columnSize - 6; i++)
         msg.append(" ");
      msg.append("Titolo")
            .append("\n")
            .append("< ");
      for (int i = 0; i < columnSize * 3; i++)
         msg.append("-");
      msg.append("\n");
      for (int i = 0; itr.hasNext() && i < numPostsToShow; i++) {
         Post currentPost = itr.next();
         String idAsString = String.valueOf(currentPost.getId());
         msg.append("< ")
               .append(idAsString.substring(0, Math.min(idAsString.length(), columnSize - 1))); // con math.min evito
                                                                                                // l'eccezione in caso
                                                                                                // la stringa sia già
                                                                                                // più corta e non ha
                                                                                                // bisogno di trimming
         for (int j = 0; j < columnSize - idAsString.length(); j++)
            msg.append(" ");
         msg.append("|" + currentPost.getAuthor().toString());
         for (int j = 0; j < columnSize - currentPost.getAuthor().length(); j++)
            msg.append(" ");
         msg.append("|" + currentPost.getTitle().toString());
      }
      return msg.toString();

   }

   private String commentPost(User u, String clientRequest) {
      String[] split = fixArray(clientRequest);
      String comment = split[1];
      int idPost = Integer.parseInt(split[0]);
      switch (ServerManager.addComment(u, ServerManager.getPostByID(idPost), comment)) {
         case 0:
            return "Operazione completata";
         case -1:
            return "Post non trovato!";
         default:
            return "Errore sconosciuto!";
      }
   }

   private String rewinPost(User u, String[] requestSplit) {
      int postID;
      requestSplit[1] = requestSplit[1].trim();
      if (requestSplit[1].trim().isEmpty())
         return "L'input non è corretto!";
      try {
         postID = Integer.parseInt(requestSplit[1]);
      } catch (NumberFormatException e) {
         return "L'input non è corretto!";
      }
      Post post = ServerManager.getPostByID(postID);
      if (post == null)
         return "Post non trovato!";
      if (post.getAuthor().equals(u.getUsername()))
         return "Non si può fare il rewin di un proprio post!";
      switch (ServerManager.rewinPost(u, post)) {
         case 0:
            return "Operazione completata";
         case -1:
            return "Utente non autorizzato!";
         default:
            return "Errore sconosciuto!";
      }

   }

   private String ratePost(User u, String[] requestSplit) {
      int idPost, vote;
      if (requestSplit.length < 3)
         return "L'input non è corretto!";
      try {
         idPost = Integer.parseInt(requestSplit[1]);
         vote = Integer.parseInt(requestSplit[2]);
      } catch (NumberFormatException e) {
         return "L'input non è corretto!";
      }
      if (vote < -1 || vote > 1)
         return "Il voto non è valido!";
      Post post = ServerManager.getPostByID(idPost);
      if (post == null)
         return "Post non trovato!";
      switch (ServerManager.ratePost(u, post, vote)) {
         case 0:
            return "Operazione completata";
         case -1:
            return "Operazione non valida!";
         case -2:
            return "Non si può votare un proprio post!";
         case -3:
            return "Post già votato!";
         case -4:
            return "Prima di votare questo post, devi effettuare il rewin!";
         default:
            return "Errore sconosciuto!";
      }

   }

   private String getWallet(User u) throws IOException {
      double sum = 0;
      StringBuilder msg = new StringBuilder();
      for (Transaction t : u.getWincoinList()) {
         sum = sum + t.getWinCoin();
         LocalDateTime datetime = LocalDateTime.ofInstant(t.getTimestamp(), ZoneOffset.UTC);
         String formatted = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").format(datetime);
         msg.append("Transazione: " + t.getWinCoinAsString() + " WinCoin, in data: " + formatted + "\n");
      }
      msg.append("Totale: " + sum + " WinCoin");
      return msg.toString();
   }

   private String getWalletBTC(User u) throws IOException {
      Double btcValue = ServerManager.getBTCValue();
      double sum = 0;
      for (Transaction t : u.getWincoinList()) {
         sum = sum + t.getWinCoin();
      }
      return "Totale: " + sum + " WinCoin, equivalente a " + sum * btcValue + " BTC";
   }

   private String listUsers(User u) throws IOException {
      HashMap<String, LinkedList<String>> usersWithCommonTag = ServerManager.listUsers(u);
      StringBuilder msg = new StringBuilder();
      // creazione della tabella da restituire al client
      msg.append("Utente");
      for (int i = 0; i < columnSize - 6; i++)
         msg.append(" ");
      msg.append("|");
      msg.append("Tag");
      msg.append("\n");
      for (int i = 0; i < columnSize * 3; i++)
         msg.append("-");
      msg.append("\n");
      for (Map.Entry<String, LinkedList<String>> entry : usersWithCommonTag.entrySet()) {
         msg.append("< ");
         msg.append(entry.getKey().substring(0, Math.min(entry.getKey().length(), columnSize - 1)));
         for (int i = 0; i < columnSize - entry.getKey().length(); i++)
            msg.append(" ");
         msg.append("|");
         for (String tag : entry.getValue()) {
            msg.append(tag);
            if (!tag.equals(entry.getValue().getLast()))
               msg.append(", ");
         }
         msg.append("\n");
      }
      return msg.toString();
   }

   private String listFollowing(User u) throws IOException {
      HashMap<String, LinkedList<String>> usersWithCommonTag = ServerManager.listFollowing(u);
      StringBuilder msg = new StringBuilder();
      // creazione della tabella da restituire al client
      msg.append("Utente");
      for (int i = 0; i < columnSize - 6; i++)
         msg.append(" ");
      msg.append("|");
      msg.append("Tag");
      msg.append("\n");
      for (int i = 0; i < columnSize * 3; i++)
         msg.append("-");
      msg.append("\n");
      for (Map.Entry<String, LinkedList<String>> entry : usersWithCommonTag.entrySet()) {
         msg.append("< ");
         msg.append(entry.getKey().substring(0, Math.min(entry.getKey().length(), columnSize - 1))); //uso math.min perchè se la stringa è troppo lunga la taglio, ma senza che lanci l'eccezione se non è troppo lunga
         for (int i = 0; i < columnSize - entry.getKey().length(); i++)
            msg.append(" ");
         msg.append("|");
         for (String tag : entry.getValue()) {
            msg.append(tag);
            if (!tag.equals(entry.getValue().getLast()))
               msg.append(", ");
         }
         msg.append("\n");
      }
      return msg.toString();
   }
   
   public String listFollowers(User u) throws IOException {
      HashMap<String, LinkedList<String>> usersWithCommonTag = ServerManager.listFollowers(u);
      StringBuilder msg = new StringBuilder();
      msg.append("Utente");
      for (int i = 0; i < columnSize - 6; i++)
         msg.append(" ");
      msg.append("|");
      msg.append("Tag");
      msg.append("\n");
      for (int i = 0; i < columnSize * 3; i++)
         msg.append("-");
      msg.append("\n");
      for (Map.Entry<String, LinkedList<String>> entry : usersWithCommonTag.entrySet()) {
         msg.append("< ");
         msg.append(entry.getKey().substring(0, Math.min(entry.getKey().length(), columnSize - 1)));
         for (int i = 0; i < columnSize - entry.getKey().length(); i++)
            msg.append(" ");
         msg.append("|");
         for (String tag : entry.getValue()) {
            msg.append(tag);
            if (!tag.equals(entry.getValue().getLast()))
               msg.append(", ");
         }
         msg.append("\n");
      }
      return msg.toString();
   }

   private String showFeed(User u) throws IOException {
      int numPostsToShow = 10;
      StringBuilder msg = new StringBuilder();
      Iterator<Post> itr = ServerManager.showFeed(u).iterator();
      msg.append("ID");
      for (int i = 0; i < columnSize - 2; i++)
         msg.append(" ");
      msg.append("Autore");
      for (int i = 0; i < columnSize - 6; i++)
         msg.append(" ");
      msg.append("Titolo")
            .append("\n")
            .append("< ");
      for (int i = 0; i < columnSize * 3; i++)
         msg.append("-");
      msg.append("\n");
      for (int i = 0; itr.hasNext() && i < numPostsToShow; i++) {
         Post currentPost = itr.next();
         String idAsString = String.valueOf(currentPost.getId());
         msg.append("< ")
               .append(idAsString.substring(0, Math.min(idAsString.length(), columnSize - 1))); // con math.min evito
                                                                                                // l'eccezione in caso
                                                                                                // la stringa sia già
                                                                                                // più corta, e non ha
                                                                                                // bisogno di trimming
         for (int j = 0; j < columnSize - idAsString.length(); j++)
            msg.append(" ");
         msg.append("|" + currentPost.getAuthor().toString());
         for (int j = 0; j < columnSize - currentPost.getAuthor().length(); j++)
            msg.append(" ");
         msg.append("|" + currentPost.getTitle().toString());
         msg.append("\n");
      }
      return msg.toString();
   }

   private String showPost(String[] requestSplit) throws IOException {
      if (requestSplit.length < 3)
         return "L'input non è corretto";
      int idPost = Integer.parseInt(requestSplit[2]);
      Post post = ServerManager.getPostByID(idPost);
      if (post == null) {
         return "Post non trovato!";
      }
      StringBuilder msg = new StringBuilder();
      msg.append("Titolo: " + post.getTitle())
            .append("\n")
            .append("< ")
            .append("Autore: " + post.getAuthor())
            .append("\n")
            .append("< ")
            .append("Contenuto: " + post.getContent())
            .append("\n")
            .append("< ")
            .append(
                  "Voti: " + post.getLikersList().size() + " positivi, " + post.getDislikersList().size() + " negativi")
            .append("\n")
            .append("< ")
            .append("Commenti: " + post.getCommentsList().size())
            .append("\n");
      Iterator<Comment> itr = post.getCommentsList().iterator();
      while (itr.hasNext()) {
         Comment curr = itr.next();
         msg.append(
               "<      " + curr.getAuthor() + ": \"" + curr.getContent().toString() + "\" \n");
      }
      return msg.toString();
   }

   private String printHelp() {
      StringBuilder msg = new StringBuilder();
      msg.append("< Utilizzo:\n")
            .append("<      register <username> <password> <tags>\n")
            .append("<      login <username> <password>\n")
            .append("<      logout\n")
            .append("<      list user\n")
            .append("<      list followers\n")
            .append("<      list following\n")
            .append("<      follow <user>\n")
            .append("<      unfollow <user>\n")
            .append("<      blog\n")
            .append("<      post \"<title>\" \"<content>\"\n")
            .append("<      show feed\n")
            .append("<      show post <idPost>\n")
            .append("<      delete <idPost>\n")
            .append("<      rewin <idPost>\n")
            .append("<      rate <idPost> <vote>\n")
            .append("<      comment <idPost> \"<comment>\"\n")
            .append("<      wallet\n")
            .append("<      wallet btc\n")
            .append("<      help\n")
            .append("<      exit\n");

      return msg.toString();
   }
}
