package Server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.LinkedList;

import Server.RMI.FollowService.CallbackService;

public interface ServerRemoteInterface extends Remote {
   public int registerUser(String username, String password, LinkedList<String> tagsList) throws RemoteException;
   public void registerCallback(String username, CallbackService user) throws RemoteException;
   public void unregisterCallback(String username, CallbackService user) throws RemoteException;
   
}
