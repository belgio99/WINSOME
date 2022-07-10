package Server.RMI.Registration;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;

import Server.ServerRemoteInterface;
import Server.ServerManager;


public class RemoteService extends UnicastRemoteObject implements ServerRemoteInterface {
   public RemoteService() throws RemoteException {

   }
   public int registerUser(String username, String password, LinkedList<String> tagsList) {
      return ServerManager.register(username, password, tagsList);
   }
}