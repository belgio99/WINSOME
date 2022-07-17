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

import Server.Configs.ClientSettings;
import Server.RMI.CallbackService;
import Server.RMI.NotifyClient;
import Server.RMI.ServerRemoteInterface;

public class ClientMain {
    private static ByteBuffer buffer;
    private static SocketChannel clientSocketChannel;
    public static ServerRemoteInterface remote;
    private static Boolean isUserLoggedIn = false;
    private static LinkedList<String> followersList;
    private static CallbackService service;
    private static CallbackService stub;
    private static String username;
    //private static ClientSettings settings;

    public static void main(String[] args) throws Exception {
        try {
            clientSocketChannel = SocketChannel.open(
                    new InetSocketAddress(ClientSettings.serverAddress, ClientSettings.TCPPort));
            clientSocketChannel.configureBlocking(true);
            buffer = ByteBuffer.allocate(1024);
            String RMIInfoString = receiveString(); //ricevo dal server la stringa di configurazione dell'RMI
            String[] RMIInfo = RMIInfoString.split(";");
            Registry r1 = LocateRegistry.getRegistry(RMIInfo[0],
            Integer.parseInt(RMIInfo[1]));
            remote = (ServerRemoteInterface) r1.lookup(RMIInfo[2]);
        } catch (UnknownHostException e) {
            System.err.println("Non ho trovato l'host a cui connettermi!");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Non trovo il server! Chiusura...");
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Errore nella connessione al server!");
            System.exit(1);
        }
        String mcastInfoString = receiveString(); //ricevo dal server la stringa di configurazione del multicast
        String[] mcastInfo = mcastInfoString.split(":");
        Thread multicastThread = new Thread(new ClientMulticastThread(mcastInfo[0], Integer.parseInt(mcastInfo[1])));
        multicastThread.start();
        service = new NotifyClient(followersList);
        stub = (CallbackService) UnicastRemoteObject.exportObject(service, 0);
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
                input = input.trim();
                String[] splitted = input.split(" ");
                splitted[0] = splitted[0].toLowerCase();
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
                                username = splitted[1];
                                remote.registerForCallback(username, stub);
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
                            logout();
                            break;
                        default:
                            send(input);
                            System.out.println("< " + receiveString());

                    }

                }

                System.out.print("> ");
                buffer.clear();
            }
        } catch (IOException e) {
            System.err.println("Errore di I/O! Probabilmente non si è più connessi al server. Chiusura...");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (isUserLoggedIn) {
            logout();
            isUserLoggedIn = false;
        }
        scanner.close();
        clientSocketChannel.close();
        multicastThread.interrupt();
        System.exit(0);
    }

    private static void register(String input) throws Exception {
        String splittedInput[] = input.split(" ");
        String regUsername = splittedInput[1].trim().toLowerCase();
        String password = splittedInput[2].trim();
        LinkedList<String> tagsList = new LinkedList<>();
        // Da mettere se la user e pass sono vuoti o è di lunghezza 0 o superiore a 20.
        if (regUsername.isEmpty() || password.isEmpty() || regUsername.length() > 20 || password.length() > 20) {
            System.out.println("< Username o password non validi! Lunghezza massima ammessa: 20 caratteri");
            return;
        }
        for (int i = 3; i < splittedInput.length; i++) {
            tagsList.add(splittedInput[i].trim().toLowerCase());
        }
        int score = remote.registerUser(regUsername, password, tagsList);
        if (score == -1)
            System.out.println("Registrazione fallita! Forse l'utente esiste già?");
        else
            System.out.println("Registrazione riuscita! Il punteggio di sicurezza della tua password è: " + score);

    }

    private static void logout() throws IOException {
        send("logout");
        if (receiveString().equals("Operazione completata")) {
            isUserLoggedIn = false;
            remote.unregisterForCallback(username, stub);
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
