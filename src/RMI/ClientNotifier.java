package RMI;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ClientNotifier extends RemoteObject implements CallbackService {
   private LinkedList<String> followers;
   private Lock lock;

   public ClientNotifier(LinkedList<String> followers) {
       this.followers = followers;
       lock = new ReentrantLock();
   }

   // Aggiornamento della lista dei follower (inserimento e rimozione dalla lista)
   public void notifyNewFollower(String username) throws RemoteException {
       try {
           lock.lock();
           followers.add(username);
           System.out.println("Nuovo follower! " + username);
           System.out.println("< ");
       } finally {
           lock.unlock();
       }
   }

   public void notifyNewUnfollower(String username) throws RemoteException {
       try {
           lock.lock();
           followers.remove(username);
           System.out.println(username + " non ti sta più seguendo!");
           System.out.println("< ");
       } finally {
           lock.unlock();
       }
   }

}