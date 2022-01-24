import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.LinkedList;
import java.util.Scanner;

import Server.Configs.DefaultValues;
import Server.RMI.RegistrationServerInterface;
import Server.utils.ResultCode;

import static Server.utils.ResultCode.*;

public class ClientMain {
    public static final String serverName = "localhost";
    // private final Gson gson = new Gson();
    private static ByteBuffer buffer;
    // private LinkedList<String> tags;
    private static SocketChannel clientSocketChannel;

    public static final int port = DefaultValues.serverval.TCPPort;

    public static void main(String[] args) throws Exception {

        try {
            Thread.sleep(2000);
            clientSocketChannel = SocketChannel.open(
                    new InetSocketAddress(DefaultValues.serverval.serverAddress, DefaultValues.serverval.TCPPort));
            clientSocketChannel.configureBlocking(true);
            buffer = ByteBuffer.allocate(1024);
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
        Scanner scanner = new Scanner(System.in);
        String input;
        System.out.println("Inserire l'input...");
        while (!(input = scanner.nextLine()).trim().equalsIgnoreCase("exit")) {
            if (input.isBlank())
                continue;
            //System.out.println("Sto per inviare: " + input);
            // String[] command =
            Boolean action = false;
            input = input.toLowerCase();
            String[] splitted = input.split(" ");
            switch (splitted[0]) {
            case "register":
                action=true;
                register(input);
                break;
            case "login":
                action=true;   
                login(input);
                int receive = receiveInt();
                System.out.println(ResultCode.values()[receive]);
                break;
            default: break;
            }
            if (!action) {

            send(input);
            int code = receiveInt();
            if (code != OK.getCode()) {
                System.out.println(ResultCode.values()[code]);
                continue;
            }
            else switch (splitted[0]) {
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
                    /*case "rewin":
                    case "rate":
                    case "wallet":
                    case "comment":
                    case "blog":
                    case "delete":
                    case "follow":
                    case "unfollow":
                    case "logout":*/
                    default:

                        break;

                }
                int receive = receiveInt();
                System.out.println(ResultCode.values()[receive]);
            }
            
            // System.out.println(receive);

            buffer.clear(); // Resetto il buffer
            /*
             * System.out.println("Messaggio ricevuto dal server: ");
             * System.out.println();
             */
            System.out.println("Inserire parole da inviare al server, o scrivere \"exit\" per uscire");
        
        }
        scanner.close();

    }
    private static void createPost(String input) throws IOException {
        if (receiveInt()==OK.getCode()) {
            System.out.println("Nuovo post creato: ID "+ receiveInt());
        }
        return;
    }

    private static void login(String input) {
        // String splittedInput[] = input.split(" ");
        // String op = splittedInput[0];
        // String username = splittedInput[1];
        // String password = splittedInput[2];
        send(input);
        // String response = receive();
        /*switch (receive()) {
            case "USER NOT FOUND":
            case "UNKNOWN COMMAND":
                System.out.println("errore!");
                break;
            default:
                System.out.println("Operazione completata");
                break;
        }*/
    }

    /*
     * if (!response.equals("Errore"))
     * return;
     * }
     */
    private static void register(String input) {
        String splittedInput[] = input.split(" ");
        String username = splittedInput[1];
        String password = splittedInput[2];
        LinkedList<String> tagsList = new LinkedList<>();
        // Da mettere se la pw è vuota o è di lunghezza 0.
        for (int i = 3; i < splittedInput.length; i++)
            tagsList.add(splittedInput[i]);
        try {
            Registry r1 = LocateRegistry.getRegistry(DefaultValues.client.RMIAddress, DefaultValues.serverval.RMIPort);
            RegistrationServerInterface registerService = (RegistrationServerInterface) r1
                    .lookup(DefaultValues.serverval.RMIName);
            if (!registerService.registerUser(username, password, tagsList))
                System.out.println("Registrazione Fallita!");
            else {
                System.out.println("Registrazione riuscita!");
            }

        }

        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void listUsers(String input) throws IOException{
        System.out.println("Lista di utenti con almeno un tag in comune:");
        System.out.println(receive());
        return;

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

    private static String receive() {
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

    private static void showPost(String input) throws IOException {
        if (receiveInt()!=OK.getCode())
            return;
        System.out.println("< Titolo: " + receive());
        System.out.println("< Contenuto: " + receive());
        System.out.println("< Voti: " + receiveInt() + " positivi, " + receiveInt() + " negativi");
        System.out.println("< Commenti:");
        int loop = receiveInt();
        for (int i=0; i<loop; i++)
            System.out.println("    " + receive() + ": " + receive());
        return;
    }
}
