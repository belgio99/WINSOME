package Server.RMI.FollowService;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentLinkedDeque;

import Server.utils.User;

public interface CallbackService extends Remote {
    void setFollowers(ConcurrentLinkedDeque<User> followers) throws RemoteException;
    void setFollowing(ConcurrentLinkedDeque<User> following) throws RemoteException;
}