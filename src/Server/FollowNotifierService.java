package Server;

import java.rmi.Remote;
import java.rmi.RemoteException;

import Server.RMI.FollowService.CallbackService;

public interface FollowNotifierService extends Remote {
    void register(String username, CallbackService user) throws RemoteException;
    void unregister(String username, CallbackService user) throws RemoteException;
}