package Server.RMI.FollowService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import Server.FollowNotifierService;
import Server.ServerManager;

public class FollowService extends UnicastRemoteObject implements FollowNotifierService {

    public FollowService() throws RemoteException {
    }

    @Override
    public void register(String username, CallbackService user) throws RemoteException {
        ServerManager.addCallback(username, user);
    }

    @Override
    public void unregister(String username, CallbackService user) throws RemoteException {
        ServerManager.removeCallback(username, user);
    }

}