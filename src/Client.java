import java.io.*;
import java.net.Socket;
import java.util.Scanner;

// Application client
public class Client {
    private static Socket socket;
    static String ipAddress;
    static int port;
    static Scanner scanner = new Scanner(System.in);
    static DataInputStream in;
    static DataOutputStream out;


    public static void main(String[] args) throws Exception {
        try {
            ipAddress = configureIpAddress();
            port = configurePort();
            socket = new Socket(ipAddress, port);
            System.out.format("Connecté au serveur [%s:%d]%n", ipAddress, port);

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            System.out.print(in.readUTF());
            String username = scanner.nextLine();
            out.writeUTF(username);

            System.out.print(in.readUTF());
            String password = scanner.nextLine();
            out.writeUTF(password);

            String response = in.readUTF();
            System.out.println(response);
            if (!response.contains("authentification réussie")) {
                System.out.println("Connexion refusée.");
                socket.close();
                scanner.close();
                return;
            }

//            System.out.println(in.readUTF());
//            String helloMessageFromServer = in.readUTF();
//            System.out.println(helloMessageFromServer);


            new Thread(() -> {
                try {
                    String receivedMessage;
                    while ((receivedMessage = in.readUTF()) != null) {
                        System.out.println(receivedMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Server connection lost.");
                }
            }).start();

            sendMessage();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                socket.close();
                scanner.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void sendMessage() throws IOException {
        while (true) {
            String newMessage = scanner.nextLine().trim();

            if (newMessage.isEmpty()) {
                continue;
            }
            if (newMessage.length() > 200) {
                System.out.println("Le message est trop long, veuillez respecter la limite de 200 caractères.");
                continue;
            }
            out.writeUTF(newMessage);
        }
    }

    private static String configureIpAddress() {
        System.out.println("Veuillez entrer une adresse IP valide (exemple du format requis : 192.168.1.1) : ");
        ipAddress = scanner.nextLine();
        while (!verifyIpAddress(ipAddress)) {
            System.out.println("L'adresse IP entrée n'est pas valide, veuillez réessayer (exemple du format requis : 192.168.1.1) : ");
            ipAddress = scanner.nextLine();
        };
        return ipAddress;
    }

    private static boolean verifyIpAddress (String ipAddress){
            if (ipAddress.isEmpty()) return false;

            String[] parts = ipAddress.split("\\.");
            if (parts.length != 4) return false;

            try {
                for (String part : parts) {
                    int i = Integer.parseInt(part);
                    if ((i < 0) || (i > 255)) return false;
                }
            } catch (Exception e) {
                return !ipAddress.endsWith(".");
            }

            return true;
        }

    private static int configurePort() {
        System.out.println("Veuillez entrer un port d'écoute compris entre 5000 et 5050 : ");
        port = scanner.nextInt();
        while (!verifyPort(port)) {
            System.out.println("Le port entré n'est pas valide, veuillez réessayer (entre 5000 et 5050) : ");
            port = scanner.nextInt();
        };
        scanner.nextLine();
        return port;
    }

    private static boolean verifyPort ( int port){
            return port >= 5000 && port <= 5050;
        }

}