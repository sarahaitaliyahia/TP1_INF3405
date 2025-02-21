import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final Scanner scanner = new Scanner(System.in);
    private static Socket socket;
    private static DataInputStream in;
    private static DataOutputStream out;
    private static volatile boolean userIsConnected = true;

    public static void main(String[] args) {
        try {
            String ipAddress = NetworkValidation.configureIpAddress();
            int port = NetworkValidation.configurePort();
            socket = new Socket(ipAddress, port);
            System.out.format("Connecté au serveur [%s:%d]%n", ipAddress, port);

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            authenticateUser();

            listeningThread();
            sendingMessages();

        } catch (IOException e) {
            System.out.println("Erreur : " + e.getMessage());
        } finally {
            closeResources();
        }
    }

    private static void authenticateUser() throws IOException {
        System.out.print(in.readUTF());
        String username = scanner.nextLine();
        out.writeUTF(username);

        System.out.print(in.readUTF());
        String password = scanner.nextLine();
        out.writeUTF(password);

        String response = in.readUTF();
        System.out.println(response);

        if (!response.toLowerCase().contains("authentification réussie")) {
            System.out.println("Connexion refusée, veuillez relancer pour réessayer.");
            userIsConnected = false;
            closeResources();
        }
    }

    private static void listeningThread() {
        new Thread(() -> {
            try {
                while (userIsConnected) {
                    String receivedMessage = in.readUTF();
                    System.out.println(receivedMessage);
                }
            } catch (IOException e) {
                System.out.println("Connexion avec le serveur perdue.");
                userIsConnected = false;
                closeResources();
            }
        }).start();
    }

    private static void sendingMessages() throws IOException {
        while (userIsConnected) {
            String message = scanner.nextLine().trim();
            if (message.isEmpty()) continue;

            if (message.length() > 200) {
                System.out.println("Le message est trop long, veuillez respecter la limite de 200 caractères.");
                continue;
            }

            try {
                out.writeUTF(message);
            } catch (IOException e) {
                System.out.println("Impossible d'envoyer le message. Connexion perdue.");
                userIsConnected = false;
                closeResources();
                break;
            }
        }
    }

    private static void closeResources() {
        try {
            if (socket != null) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
        } catch (IOException e) {
            System.out.println("Erreur lors de la fermeture des ressources.");
        }
    }
}
