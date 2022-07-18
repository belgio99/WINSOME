
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.Scanner;

import RMI.CallbackService;
import RMI.NotifyClient;
import RMI.ServerRemoteInterface;
import settings.ClientSettings;
import utils.ClientMulticastThread;

public class ClientMain {
    private static ByteBuffer buffer;
    private static SocketChannel clientSocketChannel;
    public static ServerRemoteInterface remote;
    private static Boolean isUserLoggedIn = false;
    private static LinkedList<String> followersList;
    private static CallbackService service;
    private static CallbackService stub;
    private static String username;

    public static void main(String[] args) throws Exception {
        try {
            clientSocketChannel = SocketChannel.open(
                    new InetSocketAddress(ClientSettings.serverAddress, ClientSettings.TCPPort));
            clientSocketChannel.configureBlocking(true);
            buffer = ByteBuffer.allocate(1024);
            String RMIInfoString = receiveString(); // ricevo dal server la stringa di configurazione dell'RMI
            String[] RMIInfo = RMIInfoString.split(";");
            RMIInfo[0] = RMIInfo[0].split("/")[0];
            Registry r1 = LocateRegistry.getRegistry(RMIInfo[0], Integer.parseInt(RMIInfo[1]));
            remote = (ServerRemoteInterface) r1.lookup(RMIInfo[2]);
        } catch (UnknownHostException e) {
            System.err.println("Non ho trovato l'host a cui connettermi!");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Errore di I/O! Chiusura...");
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Errore nella connessione al server!");
            System.exit(1);
        }
        System.setProperty("java.net.preferIPv4Stack" , "true"); // imposta lo stack IPv4 come preferito (necessario su Mac OS). Potrebbe non essere applicato su determinati sistemi operativi, e potrebbe richiedere il flag manuale
        Thread multicastThread = null;
        try {
            String mcastInfoString = receiveString(); // ricevo dal server la stringa di configurazione del multicast
            String[] mcastInfo = mcastInfoString.split(":");
        MulticastSocket multicastSocket = new MulticastSocket(Integer.parseInt(mcastInfo[1]));
        InetAddress mcastAddr = InetAddress.getByName(mcastInfo[0]);
        multicastSocket.setSoTimeout(0);
        multicastSocket.setReuseAddress(true);
        multicastSocket.joinGroup(mcastAddr);
        multicastThread = new Thread(new ClientMulticastThread(multicastSocket));
        multicastThread.setDaemon(true);
        multicastThread.start();
        }
        catch (SocketException e) {
                System.err.println("Errore nell'avvio del thread relativo al multicast!");
                System.err.println("Ci sono degli OS (es. Mac OS) che necessitano che la JVM sia avviata con il flag -Djava.net.preferIPv4Stack=true");
                System.err.println("Riavviare il programma con questo flag per disporre delle funzioni di notifica multicast");
            }
        followersList = new LinkedList<>();
        service = new NotifyClient(followersList);
        stub = (CallbackService) UnicastRemoteObject.exportObject(service, 0);
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
                String[] split = input.split(" ");
                split[0] = split[0].toLowerCase();
                if (split[0].equals("help"))
                    send(input);
                if (!isUserLoggedIn) {
                    switch (split[0]) {
                        case "register":
                            register(input);
                            break;
                        case "login":
                            send(input);
                            if (receiveString().equals("Operazione completata")) {
                                isUserLoggedIn = true;
                                System.out.println("< Operazione completata");
                                followersList.clear();
                                followersList.addAll(remote.receiveFollowersList(split[1]));
                                username = split[1];
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
                    switch (split[0]) {
                        case "logout":
                            logout();
                            break;
                            case "login":
                            System.out.println("Operazione non consentita al momento! Eseguire prima il logout!");
                            break;
                        case "register":
                            register(input);
                            break;
                        default:
                            send(input);
                            System.out.println("< " + receiveString());

                    }

                }

                System.out.print("> ");
                buffer.clear();
            }
        } catch (IOException | BufferUnderflowException e) {
            System.err.println("Errore di I/O! Probabilmente non si è più connessi al server. Chiusura...");
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        if (isUserLoggedIn) {
            logout();
            isUserLoggedIn = false;
        }
        scanner.close();
        clientSocketChannel.close();
        if (multicastThread != null)
            multicastThread.interrupt();
        System.exit(0);
    }

    private static void register(String input) throws Exception {
        String splitInput[] = input.split(" ");
        String regUsername = splitInput[1].trim().toLowerCase();
        String password = splitInput[2].trim();
        LinkedList<String> tagsList = new LinkedList<>();
        // Da mettere se la user e pass sono vuoti o è di lunghezza 0 o superiore a 20.
        if (regUsername.isEmpty() || password.isEmpty() || regUsername.length() > 20 || password.length() > 20) {
            System.out.println("< Username o password non validi! Lunghezza massima ammessa: 20 caratteri");
            return;
        }
        for (int i = 3; i < splitInput.length; i++) {
            tagsList.add(splitInput[i].trim().toLowerCase());
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
            System.out.println("< Logout fallito!");
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
