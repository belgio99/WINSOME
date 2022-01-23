package Server.RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.LinkedList;

public interface RegistrationServerInterface extends Remote {
   boolean registerUser(String username, String password, LinkedList<String> tagsList) throws RemoteException;
   
}
