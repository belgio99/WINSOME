package Server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientRemoteInterface extends Remote {
   public void notifyNewFollower(String username) throws RemoteException;

   public void notifyNewUnfollower(String username) throws RemoteException;
}
