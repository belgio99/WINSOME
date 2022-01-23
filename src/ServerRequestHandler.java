import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

import javax.naming.spi.DirStateFactory.Result;

import Server.Database;
import Server.ServerManager;
import Server.utils.Post;
import Server.utils.ResultCode;
import Server.utils.Transaction;
import Server.utils.User;
import static Server.utils.ResultCode.*;

public class ServerRequestHandler implements Runnable{

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
   while ((numBytes = clientChannel.read(buffer))>0) {
      clientRequest.append(new String(buffer.array(), 0, numBytes));
      buffer.clear();

   }
   System.out.println(clientRequest);
   return clientRequest.toString();
}
   public void run() {
      String result = OK;
      String[] requestSplitted;
      try {
         String clientRequest = receiveRequest();
         requestSplitted = clientRequest.split(":");
         if (requestSplitted[0].equals("login")) {
               result = login(requestSplitted);
               sendResult(result);
               return;
            }
         if (!checkLoggedIn(clientChannel)) {
            sendResult(USER_NOT_LOGGED);
            return;
         }
         User u = ServerManager.getUserLogged(clientChannel);
         switch (requestSplitted[0]) {
            case "logout":
               result = logout(requestSplitted);
               break;
            case "list":
               switch (requestSplitted[1]) {
                  case "user":
                  result = listUser(requestSplitted);
                  case "following":
                  result = listFollowing(requestSplitted);
                  default:
                  result = ILLEGAL_OPERATION;
                  break;
               }
               break;
            case "post":
               result = createPost(u, clientRequest);
            case "delete":
               result = deletePost(u, requestSplitted[1]);
            case "follow":
               result = followUser(u, requestSplitted[1]);
            case "unfollow":
               result = unfollowUser(u, requestSplitted[1]);
            case "comment":
               result = commentPost(u, clientRequest);
            case "rewin":
               result = rewinPost(u, requestSplitted);
            case "rate":
               result = ratePost(u, requestSplitted);
            case "wallet":
               if (requestSplitted.length==1)
                  result = getWallet(u);
               else if (requestSplitted[1].equals("btc"))
                  result = getWalletBTC(u);
               else result = MALFORMED_INPUT;
               break;

            /*
            case "DISCOVER":
               discover(requestSplitted[1]);
               break;
            case "GETUSERS":
               getProfileUsers(requestSplitted[1]);
               break;
            case "GETPOST":
               getSinglePost(requestSplitted[1]);
               break;*/
            default:
               result = ILLEGAL_OPERATION;
               System.out.println("Richiesta non valida!");
               break;
            }
         }
         catch (Exception e) {
            System.out.println("Boh");
            e.printStackTrace();
         }
         sendResult(result);
         
      }

      private void sendResult(String result) {
         try {
            clientChannel.register(selector, SelectionKey.OP_WRITE, result);
            selector.wakeup();
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }
      private String login(String[] splitted) {
         String username = splitted[1];
         String password = splitted[2];
         User u = ServerManager.login(username, password, clientChannel);
         if (u==null) return USER_NOT_FOUND;
         else return OK;

         //if (u!=null)  Database.addUserLogged(u, clientChannel);
         /*
         Message message = new Message(MESSAGE_TYPE.STRING, u.getUsername());
         
         */
         
      }
      private String logout(String[] request) {
         return ServerManager.logoutUser(clientChannel) ? OK : ILLEGAL_OPERATION;
      }
      private boolean checkLoggedIn(SocketChannel clientChannel) {
         return ServerManager.getUserLogged(clientChannel) == null ? false : true;
      }

      private String createPost(User u, String clientRequest) {
         String[] splitted = clientRequest.split(" \"");
         String title = splitted[1];
         String content = splitted[2];
         return ServerManager.createPost(title, content, u) ? OK : ILLEGAL_OPERATION;
      }
      private String deletePost(User u, String idPost) {
         int ID = Integer.parseInt(idPost);
         return ServerManager.deletePost(ServerManager.getPostByID(ID)) ? OK : ILLEGAL_OPERATION;
      }
      private String followUser(User u, String userToFollow) {
         return ServerManager.followUser(u,userToFollow) ? OK : ILLEGAL_OPERATION;
      }
      private String unfollowUser(User u, String userToUnfollow) {
         return ServerManager.followUser(u,userToUnfollow) ? OK : ILLEGAL_OPERATION;
      }
      private String commentPost(User u, String clientRequest) {
         String[] splitted = clientRequest.split(" \"");
         String comment = splitted[1];
         int idPost = Integer.parseInt(splitted[0].split(" ")[1]);
         return ServerManager.addComment(u, ServerManager.getPostByID(idPost), comment) ? OK : ILLEGAL_OPERATION;
      }
      private String rewinPost(User u, String[] requestSplitted) {
         int idPost = Integer.parseInt(requestSplitted[1]);
         return ServerManager.rewinPost(u, ServerManager.getPostByID(idPost)) ? OK : ILLEGAL_OPERATION;

      }
      private String ratePost(User u, String[] requestSplitted) {
         int idPost = Integer.parseInt(requestSplitted[1]);
         int vote = Integer.parseInt(requestSplitted[2]);
         return ServerManager.ratePost(u, ServerManager.getPostByID(idPost), vote);
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
      private String listUsers(User u) {
         return ServerManager.listUsers(u);
         
      }

}
