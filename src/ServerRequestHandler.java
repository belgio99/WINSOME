

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import Server.ServerManager;
import Server.Configs.Settings;
import Server.utils.Comment;
import Server.utils.Post;
import Server.utils.Transaction;
import Server.utils.User;
import static Server.utils.ServerUtils.*;

public class ServerRequestHandler implements Runnable {

   private final SocketChannel clientChannel;
   private final ByteBuffer buffer;
   private final Selector selector;
   private final ConcurrentLinkedQueue<SocketChannel> registerQueue;

   // private final Selector selector = ServerManager.getSelector();

   public ServerRequestHandler(SocketChannel clientChannel, Selector selector, ConcurrentLinkedQueue<SocketChannel> registerQueue) {
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
      System.out.println(clientRequest);
      return clientRequest.toString();
   }

   public void run() {
      String response = "";
      String[] requestSplitted;
      try {
         String clientRequest = receiveRequest();
         requestSplitted = clientRequest.trim().split(" ");
         if (requestSplitted.length < 1 || requestSplitted[0].trim().isEmpty()) {
            response = "L'input non è corretto!";
            sendString(clientChannel, response);
            registerKey();
            return;
         }
         requestSplitted[0] = requestSplitted[0].trim();
         if (requestSplitted[0].equals("login")) {
            if (requestSplitted.length != 3 || requestSplitted[1].trim().isEmpty()
                  || requestSplitted[2].trim().isEmpty()) {
               sendString(clientChannel, "L'input non è corretto!");
               registerKey();
               return;
            }
            sendString(clientChannel, login(requestSplitted));
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
         // else sendInt(clientChannel, OK.getCode());
         // Fino a qua l'utente è loggato e la stringa 0 è checked, ma le stringhe dalla
         // 0 in poi sono unchecked
         // Controllo anche la stringa 1 perché serve per alcuni comandi
         switch (requestSplitted[0]) {
            case "help":
               response = printHelp();
            case "logout":
               response = logout();
               break;
            case "list":
               if (requestSplitted.length < 2 || requestSplitted[1].trim().isEmpty()) {
                  response = "L'input non è corretto!";
               } else
                  switch (requestSplitted[1]) {
                     case "user":
                        response = listUsers(u);
                     case "following":
                        response = listFollowing(u);
                     default:
                        response = "Operazione non valida!";
                        break;
                  }
               break;
            case "show":
               if (requestSplitted.length < 2 || requestSplitted[1].trim().isEmpty()) {
                  response = "L'input non è corretto!";
               } else
                  switch (requestSplitted[1]) {
                     case "feed":
                        response = showFeed(u);
                        break;
                     case "post":
                        response = showPost(requestSplitted);
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
               response = deletePost(u, requestSplitted);
               break;
            case "follow":
               response = followUser(u, requestSplitted);
               break;
            case "unfollow":
               response = unfollowUser(u, requestSplitted);
               break;
            case "comment":
               response = commentPost(u, clientRequest.replaceFirst("comment ", ""));
               break;
            case "rewin":
               response = rewinPost(u, requestSplitted);
               break;
            case "rate":
               response = ratePost(u, requestSplitted);
               break;
            case "blog":
               response = viewBlog(u);
               break;
            case "wallet":
               if (requestSplitted.length == 1)
                  response = getWallet(u);
               else if (requestSplitted[1].trim().isEmpty())
                  response = "L'input non è corretto!";
               else if (requestSplitted[1].equals("btc"))
                  response = getWalletBTC(u);
               else
                  response = "L'input non è corretto!";
               break;
            default:
               response = "L'input non è corretto!";
               break;
         }
         sendString(clientChannel,response);
         registerKey();
      } catch (IOException e) {
         System.out.println("Un client si è disconnesso!");
         ServerManager.logout(clientChannel);
      }



   }

   private void registerKey() {
         registerQueue.add(clientChannel);
         selector.wakeup();
   }


   private String login(String[] splitted) {
      if (splitted.length != 3 || splitted[1].trim().isEmpty() || splitted[2].trim().isEmpty())
         return "L'input non è corretto!";
      String username = splitted[1];
      String password = splitted[2];
      User u = ServerManager.login(username, password, clientChannel);
      if (u == null) {
         StringBuilder msg = new StringBuilder();
         msg.append("Errore in fase di login! Le cause possono essere:\n")
            .append("< - Account non registrato\n")
            .append("< - Password non corretta\n")
            .append("< - Si sta provando ad accedere ad un altro account senza prima essersi sloggati\n")
            .append("< - Si sta tentando di accedere, ma si è già loggati");
         return msg.toString();
      }
      else
         return "Operazione completata";
   }

   private String logout() {
      if (ServerManager.logout(clientChannel)==-1)
         return "Utente non loggato";
      else
         return "Operazione completata";
   }

   private String createPost(User u, String clientRequest) throws IOException {
      String[] splitted = fixArray(clientRequest);
      if (splitted.length != 3 || splitted[0].trim().isEmpty() || splitted[2].trim().isEmpty())
         return "L'input non è corretto!";
      String title = splitted[0];
      String content = splitted[2];
      Post post = ServerManager.createPost(title, content, u);
      if (post == null)
         return "Operazione non valida!";
      return "Operazione completata: ID del post: " + post.getId();
   }

   private String deletePost(User u, String[] requestSplitted) {
      requestSplitted[1] = requestSplitted[1].trim();
      int postID;
      if (requestSplitted.length < 2)
         return "L'input non è corretto!";
      try {
         postID = Integer.parseInt(requestSplitted[1]);
      }
      catch (IllegalArgumentException e) {
         return "L'input non è corretto!";
      }
      Post post = ServerManager.getPostByID(postID);
      if (post == null)
         return "Post non trovato!";
      if (!post.getAuthor().equals(u.getUsername()))
         return "Utente non autorizzato!";
      return ServerManager.deletePost(post) == 0 ? "Operazione completata" : "Operazione non permessa!";
   }

   private String followUser(User u, String[] requestSplitted) {
      requestSplitted[1] = requestSplitted[1].trim();
      if (requestSplitted.length < 2 || requestSplitted[1].trim().isEmpty())
         return "L'input non è corretto!";

      switch (ServerManager.followUser(u, requestSplitted[1])) {
         case 0:
            return "Operazione completata";
         case -1:
            return "Utente non trovato!";
         default:
            return "Errore sconosciuto!";
      }
   }

   private String unfollowUser(User u, String[] requestSplitted) {
      requestSplitted[1] = requestSplitted[1].trim();
      if (requestSplitted.length < 2 || requestSplitted[1].trim().isEmpty()) //TODO
         return "L'input non è corretto!";

      switch (ServerManager.unfollowUser(u, requestSplitted[1])) {
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
      int columnSize = Settings.serverSettings.columnSize;
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
            .append(idAsString.substring(0,Math.min(idAsString.length(), columnSize - 1))); //con math.min evito l'eccezione in caso la stringa sia già più corta, e non ha bisogno di trimming
         for (int j=0; j<columnSize-idAsString.length(); j++)
            msg.append(" ");
         msg.append("|" + currentPost.getAuthor().toString());
         for (int j=0; j<columnSize-currentPost.getAuthor().length(); j++)
            msg.append(" ");
         msg.append("|" + currentPost.getTitle().toString());
      }
      return msg.toString();


   }

   private String commentPost(User u, String clientRequest) {
      String[] splitted = fixArray(clientRequest);
      String comment = splitted[1];
      int idPost = Integer.parseInt(splitted[0]);
      switch (ServerManager.addComment(u, ServerManager.getPostByID(idPost), comment)) {
         case 0:
            return "Operazione completata";
         case -1:
            return "Post non trovato!";
         default:
            return "Errore sconosciuto!";
      }
   }

   private String rewinPost(User u, String[] requestSplitted) {
      requestSplitted[1] = requestSplitted[1].trim();
      if (requestSplitted.length < 2   || requestSplitted[1].trim().isEmpty()) //TODO //flippo i byte 1 e 2 (funziona da 1 a n-1), ovvero controllo se il numero è positivo o !=0
         return "L'input non è corretto!";
      Post post = ServerManager.getPostByID(Integer.parseInt(requestSplitted[1]));
      if (post == null)
         return "Post non trovato!";
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
      if (requestSplit.length < 3) //TODO
         return "L'input non è corretto!";
      try {
      idPost = Integer.parseInt(requestSplit[1]);
      vote = Integer.parseInt(requestSplit[2]);
      }
      catch (NumberFormatException e) {
         return "L'input non è corretto!";
      }
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
      StringBuilder msg = new StringBuilder();
      HashSet<String> users = ServerManager.listUsers(u);
      Iterator<String> itr1 = users.iterator();
      while (itr1.hasNext()) {
         msg.append(itr1.next());
         if (itr1.hasNext())
            msg.append("\n");
      }
      return msg.toString();
   }
   private String listFollowing(User u) throws IOException {
      StringBuilder msg = new StringBuilder();
      HashSet<String> users = ServerManager.listFollowing(u);
      Iterator<String> itr1 = users.iterator();
      while (itr1.hasNext()) {
         msg.append(itr1.next());
         if (itr1.hasNext())
            msg.append("\n");
      }
      return msg.toString();
   }
   private String showFeed(User u) throws IOException {
      int numPostsToShow = 10;
      StringBuilder msg = new StringBuilder();
      Iterator<Post> itr = ServerManager.showFeed(u).iterator();
      int columnSize = Settings.serverSettings.columnSize;
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
            .append(idAsString.substring(0,Math.min(idAsString.length(), columnSize - 1))); //con math.min evito l'eccezione in caso la stringa sia già più corta, e non ha bisogno di trimming
         for (int j=0; j<columnSize-idAsString.length(); j++)
            msg.append(" ");
         msg.append("|" + currentPost.getAuthor().toString());
         for (int j=0; j<columnSize-currentPost.getAuthor().length(); j++)
            msg.append(" ");
         msg.append("|" + currentPost.getTitle().toString());
      }
      return msg.toString();
   }

   private String showPost(String[] requestSplitted) throws IOException {
      if (requestSplitted.length < 3) //TODO
         return "L'input non è corretto";
      int idPost = Integer.parseInt(requestSplitted[2]);
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
         .append("Voti: " + post.getLikersList().size() + " positivi, " + post.getDislikersList().size() + " negativi")
         .append("\n")
         .append("< ")
         .append("Commenti: " + post.getCommentsList().size())
         .append("\n");
      Iterator<Comment> itr = post.getCommentsList().iterator();
      while (itr.hasNext()) {
         Comment curr = itr.next();
         msg.append("<      " + curr.getAuthor().getUsername().toString() + ": \"" + curr.getContent().toString() + "\" \n");
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
