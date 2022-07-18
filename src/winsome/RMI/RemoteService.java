package winsome.RMI;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;

import winsome.ServerManager;


public class RemoteService extends UnicastRemoteObject implements ServerRemoteInterface {
   public RemoteService() throws RemoteException {

   }
   public int registerUser(String username, String password, LinkedList<String> tagsList) {
      return ServerManager.register(username, password, tagsList);
   }
   public LinkedList<String> receiveFollowersList(String username) {
      return ServerManager.receiveFollowersList(username);
   }
   public void registerForCallback(String username, CallbackService service) {
      ServerManager.registerForCallback(username, service);
      return;
   }
   public void unregisterForCallback(String username, CallbackService service) {
      ServerManager.unregisterForCallback(username, service);
      return;
   }
}