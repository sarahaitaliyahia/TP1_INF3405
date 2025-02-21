import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static Socket socket;
    static String ipAddress;
    static int port = 5020;
    static Scanner scanner = new Scanner(System.in);
    static DataInputStream in;
    static DataOutputStream out;

    public static void main(String[] args) throws Exception {
        try {
            // Demande à l'utilisateur l'adresse IP du serveur et le port
            ipAddress = configureIpAddress();
            socket = new Socket(ipAddress, port);  // Connexion au serveur
            System.out.println("Connecté au serveur à " + ipAddress + ":" + port);

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            // Appeler la méthode d'authentification
            authenticate();

            // Lancer le thread pour recevoir les messages
            new Thread(() -> {
                try {
                    String receivedMessage;
                    while ((receivedMessage = in.readUTF()) != null) {
                        System.out.println(receivedMessage);  // Affiche les messages reçus
                    }
                } catch (IOException e) {
                    System.out.println("Erreur de connexion au serveur.");
                }
            }).start();

            // Méthode pour envoyer des messages après authentification
            sendMessage();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
            scanner.close();
        }
    }

    // Méthode pour envoyer un message au serveur
    private static void sendMessage() throws IOException {
        while (true) {
            String newMessage = scanner.nextLine().trim();
            if (newMessage.isEmpty()) {
                continue;
            }
            out.writeUTF(newMessage);  // Envoie le message au serveur
        }
    }

    // Méthode pour configurer l'adresse IP du serveur
    private static String configureIpAddress() {
        System.out.println("Entrez l'adresse IP du serveur : ");
        ipAddress = scanner.nextLine();
        return ipAddress;
    }

    // Méthode pour authentifier l'utilisateur
    private static void authenticate() throws IOException {
        // Demander le nom d'utilisateur
        System.out.print(in.readUTF());  // Message du serveur pour le nom d'utilisateur
        String username = scanner.nextLine();
        out.writeUTF(username);  // Envoyer le nom d'utilisateur au serveur

        // Demander le mot de passe
        System.out.print(in.readUTF());  // Message du serveur pour le mot de passe
        String password = scanner.nextLine();
        out.writeUTF(password);  // Envoyer le mot de passe au serveur

        // Attendre la réponse du serveur
        String response = in.readUTF();
        System.out.println(response);
        if (response.contains("échec")) {
            System.out.println("Connexion fermée.");
            System.exit(0);  // Fermer la connexion si l'authentification échoue
        }
    }
}
