

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.LinkedList;


public interface ServerRemoteInterface extends Remote {
   public int registerUser(String username, String password, LinkedList<String> tagsList) throws RemoteException;
   public LinkedList<String> receiveFollowersList(String username) throws RemoteException;
   public void registerForCallback(String username, CallbackService clientInterface) throws RemoteException;
   public void unregisterForCallback(String username, CallbackService clientInterface) throws RemoteException;
}
