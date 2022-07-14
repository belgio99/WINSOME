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

import org.omg.CosNaming._BindingIteratorImplBase;

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
            // Thread.sleep(2000);
            clientSocketChannel = SocketChannel.open(
                    new InetSocketAddress(Settings.clientSettings.serverAddress, Settings.clientSettings.TCPPort));
            clientSocketChannel.configureBlocking(true);
            buffer = ByteBuffer.allocate(1024);
            Registry r1 = LocateRegistry.getRegistry(Settings.clientSettings.RMIAddress,
                    Settings.clientSettings.RMIPort);
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
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Non trovo il server! Chiusura...");
            System.exit(1);
        }
        service = new NotifyClient(followersList);
        stub = (CallbackService) UnicastRemoteObject.exportObject(service, 0);
        String mcastInfos = receiveString();
        String mcastAddress = mcastInfos.split(":")[0];
        int mcastPort = Integer.parseInt(mcastInfos.split(":")[1]);
        Thread multicastThread = new Thread(new ClientMulticastThread(mcastAddress, mcastPort));
        multicastThread.start();
        followersList = new LinkedList<>();
        Scanner scanner = new Scanner(System.in);
        String input;
        System.out.println("Connesso a " + clientSocketChannel.getRemoteAddress());
        System.out.println("Inserire parole da inviare al server, o scrivere \"exit\" per uscire");
        System.out.print("> ");
        try {
            while (!(input = scanner.nextLine()).trim().equalsIgnoreCase("exit")) {
                if (input.trim().isEmpty())
                    continue;
                // System.out.println("Sto per inviare: " + input);
                // String[] command =
                input = input.toLowerCase().trim();
                String[] splitted = input.split(" ");
                if (splitted[0].equals("help"))
                    send(input);
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
                                System.out.println("< Login fallito!"); // Ricevo la stringa con il messaggio di errore
                            }
                            break;
                        default:
                            System.out.println(
                                    "< Comando non disponibile: attualmente è possibile solo il login o registrarsi");
                            break;
                    }
                } else {
                    switch (splitted[0]) {
                        case "logout":
                            logout(input);
                        default:
                            send(input);
                            System.out.println("< " + receiveString());

                    }

                }

                System.out.print("> ");

                /*
                 * send(input);
                 * int code = receiveInt();
                 * if (code != OK.getCode()) {
                 * System.out.println(ResultCode.values()[code]);
                 * continue;
                 * } else
                 * switch (splitted[0]) {
                 * case "list":
                 * switch (splitted[1]) {
                 * case "user":
                 * listUsers(input);
                 * case "following":
                 * // listFollowing(input);
                 * default:
                 * // ILLEGAL_OPERATION;
                 * break;
                 * }
                 * case "show":
                 * switch (splitted[1]) {
                 * case "post":
                 * showPost(input);
                 * case "following":
                 * // listFollowing(input);
                 * default:
                 * // ILLEGAL_OPERATION;
                 * break;
                 * }
                 * break;
                 * case "post":
                 * createPost(input);
                 * break;
                 * /*
                 * case "rewin":
                 * case "rate":
                 * case "wallet":
                 * case "comment":
                 * case "blog":
                 * case "delete":
                 * case "follow":
                 * case "unfollow":
                 * case "logout":
                 * 
                 * default:
                 * 
                 * break;
                 * 
                 * }
                 * int receive = receiveInt();
                 * System.out.println(ResultCode.values()[receive]);
                 * 
                 * // System.out.println(receive);
                 */
                buffer.clear(); // Resetto il buffer
                /*
                 * System.out.println("Messaggio ricevuto dal server: ");
                 * System.out.println();
                 */

            }
        } catch (IOException e) {
            System.err.println("Errore di I/O! Probabilmente non si è più connessi al server. Chiusura...");
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            try {
                clientSocketChannel.close();
            } catch (IOException e) {
                System.err.println("Errore di I/O! Probabilmente non si è più connessi al server. Chiusura...");
                System.exit(1);
            }
        }
        if (isUserLoggedIn) {
            logout("logout");
            isUserLoggedIn = false;
        }
        scanner.close();
        multicastThread.interrupt();
    }

    private static void register(String input) throws Exception {
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
        int score = remote.registerUser(username, password, tagsList);
        if (score == -1)
            System.out.println("Registrazione Fallita!");
        else
            System.out.println("Registrazione riuscita! Il punteggio di sicurezza della tua password è: " + score);

    }

    private static void logout(String input) throws IOException{
        send(input);
        if (receiveString().equals("Operazione completata")) {
            isUserLoggedIn = false;
            remote.unregisterForCallback(input.split(" ")[1], stub);
            System.out.println("< Operazione completata");
        } else {
            System.out.println("< Logout fallito!"); // Ricevo la stringa con il messaggio di errore
        }
    }

    private static void send(String s) throws IOException {
        buffer = ByteBuffer.wrap(s.getBytes());
        while (buffer.hasRemaining())
            clientSocketChannel.write(buffer);
    }

    private static String receiveString() throws IOException {
        int numBytes = 0;
        numBytes = receiveInt();
        buffer = ByteBuffer.allocate(numBytes);
        clientSocketChannel.read(buffer);
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
