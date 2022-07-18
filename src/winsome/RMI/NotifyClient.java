package winsome.RMI;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NotifyClient extends RemoteObject implements CallbackService {
   private LinkedList<String> followers;
   private Lock lock;

   public NotifyClient(LinkedList<String> followers) {
       this.followers = followers;
       lock = new ReentrantLock();
   }

   // Aggiornamento della lista dei follower (inserimento e rimozione dalla lista)
   public void notifyNewFollower(String username) throws RemoteException {
       try {
           lock.lock();
           followers.add(username);
           System.out.println("Nuovo follower! " + username);
       } finally {
           lock.unlock();
       }
   }

   public void notifyNewUnfollower(String username) throws RemoteException {
       try {
           lock.lock();
           followers.remove(username);
           System.out.println("Unfollower! " + username);
       } finally {
           lock.unlock();
       }
   }

}