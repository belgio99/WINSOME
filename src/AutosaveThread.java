

public class AutosaveThread implements Runnable {

      public void run() {
         System.out.println("Autosalvataggio in corso...");
         ServerManager.saveServerState();
      }
}
