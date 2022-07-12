import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.Scanner;

import Server.ServerRemoteInterface;
import Server.Configs.Settings;
import Server.utils.CallbackService;
import Server.utils.NotifyClient;

public class ClientMain {
    public static final String serverName = "localhost";
    // private final Gson gson = new Gson();
    private static ByteBuffer buffer;
    // private LinkedList<String> tags;
    private static SocketChannel clientSocketChannel;
    public static ServerRemoteInterface remote;
    private static Boolean isUserLoggedIn = false;
    private static LinkedList<String> followersList;
    private static CallbackService service;
    private static CallbackService stub;

    public static void main(String[] args) throws Exception {
        try {
            Thread.sleep(2000);
            clientSocketChannel = SocketChannel.open(new InetSocketAddress(Settings.clientSettings.serverAddress, Settings.clientSettings.TCPPort));
            clientSocketChannel.configureBlocking(true);
            buffer = ByteBuffer.allocate(1024);
            Registry r1 = LocateRegistry.getRegistry(Settings.clientSettings.RMIAddress, Settings.clientSettings.RMIPort);
            remote = (ServerRemoteInterface) r1.lookup(Settings.clientSettings.RMIName);
            


            // Registry r1 = LocateRegistry.getRegistry(DefaultValues.client.RMIAddress,
            // DefaultValues.serverval.RMIPort);
            
            /*
             * socket.connect(new InetSocketAddress(serverName, port));
             * System.out.println("Sto provando a connettermi al server");
             * System.out.println("Server connesso" + socket.getInetAddress() +
             * socket.getLocalAddress() + socket.getLocalPort());
             * socket.close();
             */
        } catch (UnknownHostException e) {
            System.err.println("Non ho trovato l'host a cui connettermi!");
        } catch (IOException e) {
            System.err.println("non trovo il server!");
        }
        service = new NotifyClient(followersList);
        stub = (CallbackService) UnicastRemoteObject.exportObject(service, 0);
        //ClientMulticastThread clientMulticastThread = new ClientMulticastThread("1.0.0.0");
        followersList = new LinkedList<>();
        Scanner scanner = new Scanner(System.in);
        String input;
        System.out.println("Inserire l'input...");
        System.out.println("Inserire parole da inviare al server, o scrivere \"exit\" per uscire");
        System.out.print("> ");
        while (!(input = scanner.nextLine()).trim().equalsIgnoreCase("exit")) {
            if (input.trim().isEmpty())
                continue;
            // System.out.println("Sto per inviare: " + input);
            // String[] command =
            input = input.toLowerCase().trim();
            String[] splitted = input.split(" ");
            if (!isUserLoggedIn) {
                switch (splitted[0]) {
                    case "register":
                        register(input);
                        break;
                    case "login":
                        send(input);
                        if (receiveString().equals("Operazione completata")) {
                            isUserLoggedIn = true;
                            System.out.println("< Operazione completata");
                            followersList.clear();
                            followersList.addAll(remote.receiveFollowersList(splitted[1]));
                            remote.registerForCallback(splitted[1], stub);
                        } else {
                            System.out.println("< Login fallito!"); //Ricevo la stringa con il messaggio di errore
                        }
                        break;
                    default:
                            System.out.println("< Comando non disponibile: attualmente è possibile solo il login o registrarsi");
                        break;
                }
            }
            else {
                switch (splitted[0]) {
                    case "logout":
                        send(input);
                        if (receiveString().equals("Operazione completata")) {
                            isUserLoggedIn = false;
                            remote.unregisterForCallback(splitted[1], stub);
                            System.out.println("< Operazione completata");
                        } else {
                            System.out.println("< Logout fallito!"); //Ricevo la stringa con il messaggio di errore
                        }
                        break;
                    default:
                    send(input);
                    System.out.println("< " + receiveString());
                    

            }
        }
        System.out.print("> ");
            
/*
            send(input);
            int code = receiveInt();
            if (code != OK.getCode()) {
                System.out.println(ResultCode.values()[code]);
                continue;
            } else
                switch (splitted[0]) {
                    case "list":
                        switch (splitted[1]) {
                            case "user":
                                listUsers(input);
                            case "following":
                                // listFollowing(input);
                            default:
                                // ILLEGAL_OPERATION;
                                break;
                        }
                    case "show":
                        switch (splitted[1]) {
                            case "post":
                                showPost(input);
                            case "following":
                                // listFollowing(input);
                            default:
                                // ILLEGAL_OPERATION;
                                break;
                        }
                        break;
                    case "post":
                        createPost(input);
                        break;
                    /*
                     * case "rewin":
                     * case "rate":
                     * case "wallet":
                     * case "comment":
                     * case "blog":
                     * case "delete":
                     * case "follow":
                     * case "unfollow":
                     * case "logout":
                     
                    default:

                        break;

                }
            int receive = receiveInt();
            System.out.println(ResultCode.values()[receive]);

            // System.out.println(receive);
*/
            buffer.clear(); // Resetto il buffer
            /*
             * System.out.println("Messaggio ricevuto dal server: ");
             * System.out.println();
             */
            

        }
        scanner.close();

    }

    private static void register(String input) {
        String splittedInput[] = input.split(" ");
        String username = splittedInput[1].trim().toLowerCase();
        String password = splittedInput[2].trim();
        LinkedList<String> tagsList = new LinkedList<>();
        // Da mettere se la user e pass sono vuoti o è di lunghezza 0 o superiore a 20.
        if (username.isEmpty() || password.isEmpty() || username.length() > 20 || password.length() > 20) {
            System.out.println("< Username o password non validi! Lunghezza massima ammessa: 20 caratteri");
            return;
        }
        for (int i = 3; i < splittedInput.length; i++) {
            tagsList.add(splittedInput[i].trim().toLowerCase());
        }
        try {
            int score = remote.registerUser(username, password, tagsList);
            if (score == -1)
                System.out.println("Registrazione Fallita!");
            else 
                System.out.println("Registrazione riuscita! Il punteggio di sicurezza della tua password è: " + score);
        }

        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void send(String s) {
        buffer = ByteBuffer.wrap(s.getBytes());

        try {
            while (buffer.hasRemaining())
                clientSocketChannel.write(buffer);
        } catch (Exception e) {
            System.out.println("Impossibile contattare il Server");
        }
    }

    private static String receiveString() {
        int numBytes = 0;
        try {
            numBytes = receiveInt();
            buffer = ByteBuffer.allocate(numBytes);
            clientSocketChannel.read(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        buffer.flip();
        return new String(buffer.array(), 0, numBytes);
    }

    private static int receiveInt() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        clientSocketChannel.read(buffer);
        buffer.flip();
        return buffer.asIntBuffer().get();
    }

   
}
