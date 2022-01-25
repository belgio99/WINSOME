package Server.RMI.Registration;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;

import Server.ServerManager;


public class RegistrationService extends UnicastRemoteObject implements RegistrationServerInterface {
   public RegistrationService() throws RemoteException {

   }
   public boolean registerUser(String username, String password, LinkedList<String> tagsList) {
      return ServerManager.register(username, password, tagsList);
   }
}