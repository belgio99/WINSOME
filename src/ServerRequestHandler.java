import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

import Server.ServerManager;
import static Server.utils.ResultCode.*;

import Server.utils.Comment;
import Server.utils.Post;
import Server.utils.Transaction;
import Server.utils.User;
import static Server.utils.ServerUtils.*;

public class ServerRequestHandler implements Runnable {

   private final SocketChannel clientChannel;
   private final ByteBuffer buffer;
   private final Selector selector;
   //private final Selector selector = ServerManager.getSelector();

   public ServerRequestHandler(SocketChannel clientChannel, Selector selector) {
      this.clientChannel = clientChannel;
      this.selector = selector;
      buffer = ByteBuffer.allocate(1024);
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
      int result = OK.getCode();
      String[] requestSplitted;
      try {
         String clientRequest = receiveRequest();
         requestSplitted = clientRequest.trim().split(" ");
         if (requestSplitted.length < 1 || stringChecker(requestSplitted[0])) {
            sendInt(clientChannel, MALFORMED_INPUT.getCode());
            registerKey();
            return;
         }
         requestSplitted[0] = requestSplitted[0].trim();
         // fino a qua il comando deve essere buono
         if (requestSplitted[0].equals("login")) {
            if (requestSplitted.length != 3 || stringChecker(requestSplitted[1])
                  || stringChecker(requestSplitted[2])) {
               sendInt(clientChannel, MALFORMED_INPUT.getCode());
               registerKey();
               return;
            }
            sendInt(clientChannel, login(requestSplitted));
            registerKey();
            return;
         }
         // Ora controllo se l'utente è loggato
         User u;
         if ((u = ServerManager.getUserLogged(clientChannel)) == null) {
            sendInt(clientChannel, USER_NOT_LOGGED.getCode());
            registerKey();
            return;
         }
         else sendInt(clientChannel, OK.getCode());
         // Fino a qua l'utente è loggato e la stringa 0 è checked, ma le stringhe dalla
         // 0 in poi sono unchecked
         // Controllo anche la stringa 1 perché serve per alcuni comandi
         switch (requestSplitted[0]) {
            case "logout":
               result = logout(requestSplitted);
               break;
            case "list":
               if (requestSplitted.length < 2 || stringChecker(requestSplitted[1])) {
                  result = MALFORMED_INPUT.getCode();
               } else
                  switch (requestSplitted[1]) {
                     case "user":
                        result = listUsers(u);
                     case "following":
                        // result = listFollowing(requestSplitted);
                     default:
                        result = ILLEGAL_OPERATION.getCode();
                        break;
                  }
               break;
            case "show":
               if (requestSplitted.length < 2 || stringChecker(requestSplitted[1])) {
                  result = MALFORMED_INPUT.getCode();
               } else
                  switch (requestSplitted[1]) {
                     case "feed":
                        result = showFeed(u);
                        break;
                     case "post":
                        result = showPost(requestSplitted);
                        break;
                     default:
                        result = ILLEGAL_OPERATION.getCode();
                        break;
                  }
               break;
            case "post":
               result = createPost(u, clientRequest.replaceFirst("post ", ""));
               break;
            case "delete":
               result = deletePost(u, requestSplitted);
               break;
            case "follow":
               result = followUser(u, requestSplitted);
               break;
            case "unfollow":
               result = unfollowUser(u, requestSplitted);
               break;
            case "comment":
               result = commentPost(u, clientRequest.replaceFirst("comment ", ""));
               break;
            case "rewin":
               result = rewinPost(u, requestSplitted);
               break;
            case "rate":
               result = ratePost(u, requestSplitted);
               break;
            case "wallet":
               if (requestSplitted.length == 1)
                  result = getWallet(u);
               else if (stringChecker(requestSplitted[1]))
                  result = MALFORMED_INPUT.getCode();
               else if (requestSplitted[1].equals("btc"))
                  result = getWalletBTC(u);
               else
                  result = MALFORMED_INPUT.getCode();
               break;
            default:
               result = ILLEGAL_OPERATION.getCode();
               System.out.println("Richiesta non valida!");
               break;
         }
      sendInt(clientChannel, result);
      registerKey();
      } catch (IOException e) {
        ServerManager.logoutUser(clientChannel);
         e.printStackTrace();
      }
      
      // sendResult(result);

   }
   private void registerKey() {
      try {
         clientChannel.register(selector, SelectionKey.OP_READ);
         selector.wakeup();
         
         }
         catch (ClosedChannelException e) {
            ServerManager.logoutUser(clientChannel);
         }
   
   }
   private void registerKey(SocketChannel clientChannel, Object object) {
      try {
         clientChannel.register(selector, SelectionKey.OP_READ, object);
         selector.wakeup();
         }
         catch (ClosedChannelException e) {
            ServerManager.logoutUser(clientChannel);
         }
   
   }
   /*
    * private void sendResult(int result) {
    * try {
    * clientChannel.register(selector, SelectionKey.OP_WRITE, result);
    * selector.wakeup();
    * }
    * catch (Exception e)
    * {
    * e.printStackTrace();
    * }
    * }
    */
   private int login(String[] splitted) {
      if (splitted.length != 3 || stringChecker(splitted[1]) || stringChecker(splitted[2]))
         return MALFORMED_INPUT.getCode();
      String username = splitted[1];
      String password = splitted[2];
      User u = ServerManager.login(username, password, clientChannel);
      if (u == null)
         return USER_NOT_FOUND.getCode();
      else
         return OK.getCode();
   }

   private int logout(String[] request) {
      return ServerManager.logoutUser(clientChannel) ? 0 : 3;
   }

   private int createPost(User u, String clientRequest) throws IOException {
      String[] splitted = fixArray(clientRequest);
      if (splitted.length != 3 || stringChecker(splitted[0]) || stringChecker(splitted[2]))
         return MALFORMED_INPUT.getCode();
      String title = splitted[0];
      String content = splitted[2];
      Post post = ServerManager.createPost(title, content, u);
      if (post == null) {
         sendInt(clientChannel, ILLEGAL_OPERATION.getCode());
         return ILLEGAL_OPERATION.getCode();
      }
      else sendInt(clientChannel, OK.getCode());
      sendInt(clientChannel, post.getIDPost());
      return OK.getCode();
   }

   private int deletePost(User u, String[] requestSplitted) {
      requestSplitted[1] = requestSplitted[1].trim();
      if (requestSplitted.length < 2 || !intToStringChecker(requestSplitted[1], new BitSet(3) {
         {
            flip(1, 3);
         }
      }))
         return MALFORMED_INPUT.getCode();

      Post post = ServerManager.getPostByID(Integer.parseInt(requestSplitted[1]));
      if (post == null)
         return POST_NOT_FOUND.getCode();
      if (!post.getAuthor().equals(u))
         return UNAUTHORIZED_USER.getCode();
      return ServerManager.deletePost(post) ? OK.getCode() : ILLEGAL_OPERATION.getCode();
   }

   private int followUser(User u, String[] requestSplitted) {
      requestSplitted[1] = requestSplitted[1].trim();
      if (requestSplitted.length < 2 || !intToStringChecker(requestSplitted[1], new BitSet(3) {
         {
            flip(1, 3);
         }
      }))
         return MALFORMED_INPUT.getCode();

      return ServerManager.followUser(u, requestSplitted[1]);
   }

   private int unfollowUser(User u, String[] requestSplitted) {
      requestSplitted[1] = requestSplitted[1].trim();
      if (requestSplitted.length < 2 || !intToStringChecker(requestSplitted[1], new BitSet(3) {
         {
            flip(1, 3);
         }
      }))
         return MALFORMED_INPUT.getCode();

      return ServerManager.unfollowUser(u, requestSplitted[1]);
   }

   private int commentPost(User u, String clientRequest) {
      String[] splitted = fixArray(clientRequest);
      String comment = splitted[1];
      int idPost = Integer.parseInt(splitted[0]);
      return ServerManager.addComment(u, ServerManager.getPostByID(idPost), comment);
   }


   private int rewinPost(User u, String[] requestSplitted) {
      requestSplitted[1] = requestSplitted[1].trim();
      if (requestSplitted.length < 2 || !intToStringChecker(requestSplitted[1], new BitSet(3) {
         {
            flip(1, 3);
         }
      }))
         return MALFORMED_INPUT.getCode();
      Post post = ServerManager.getPostByID(Integer.parseInt(requestSplitted[1]));
      if (post == null)
         return POST_NOT_FOUND.getCode();
      return ServerManager.rewinPost(u, post);
   }

   private int ratePost(User u, String[] requestSplitted) {
      if (requestSplitted.length < 3 || !intToStringChecker(requestSplitted[1], new BitSet(3) {
         {
            flip(1, 3);
         }
      }) || !intToStringChecker(requestSplitted[2], new BitSet(3) {
         {
            flip(1, 3);
         }
      }))
         return MALFORMED_INPUT.getCode();
      int idPost = Integer.parseInt(requestSplitted[1]);
      int vote = Integer.parseInt(requestSplitted[2]);
      Post post = ServerManager.getPostByID(idPost);
      if (post == null)
         return POST_NOT_FOUND.getCode();
      return ServerManager.ratePost(u, post, vote);
   }

   private int getWallet(User u) {
      Double amount = u.getCurrentCompensation();
      LinkedList<Transaction> transactionList = u.getWincoinList();
      return 0;
   }

   private int getWalletBTC(User u) {
      Double amount = ServerManager.getBTCValue();
      return 0;
   }

   private int listUsers(User u) throws IOException {
      HashSet<String> users = ServerManager.listUsers(u);
      int size = users.size();
      sendInt(clientChannel, size);
      Iterator<String> itr1 = users.iterator();
      while (itr1.hasNext())
         sendString(clientChannel, itr1.next());
      return OK.getCode();
   }

   private int showFeed(User u) throws IOException {
      Iterator<Post> itr = ServerManager.showFeed(u).iterator();
      int numPostsToSend = 10;
      sendInt(clientChannel, numPostsToSend);
      for (int i = 0; itr.hasNext() && i < 10; i++) {
         Post currentPost = itr.next();
         sendInt(clientChannel, currentPost.getIDPost());
         sendString(clientChannel, currentPost.getAuthor().toString());
         sendString(clientChannel, currentPost.getTitle());
      }
      return OK.getCode();
   }
   private int showPost(String[] requestSplitted) throws IOException {
      if (requestSplitted.length < 3 || !intToStringChecker(requestSplitted[2], new BitSet(3) {
         {
            flip(1, 3);
         }
      }))
         return MALFORMED_INPUT.getCode();
      int idPost = Integer.parseInt(requestSplitted[2]);
      Post post = ServerManager.getPostByID(idPost);
      if (post==null) {
         sendInt(clientChannel, POST_NOT_FOUND.getCode());
         return POST_NOT_FOUND.getCode();
      }
      sendInt(clientChannel, OK.getCode());

      sendString(clientChannel, post.getTitle());
      sendString(clientChannel, post.getContent());
      sendInt(clientChannel, post.getLikersList().size());
      sendInt(clientChannel, post.getDislikersList().size());
      ConcurrentLinkedQueue<Comment> commentsList = post.getCommentsList();
      sendInt(clientChannel, commentsList.size());
      Iterator<Comment> itr = commentsList.iterator();
      while (itr.hasNext()) {
         Comment curr = itr.next();
         sendString(clientChannel, curr.getAuthor().getUsername().toString());
         sendString(clientChannel, curr.getContent());
      }
      return OK.getCode();
   }

}
