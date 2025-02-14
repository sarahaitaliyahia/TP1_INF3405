import java.io.*;
import java.net.Socket;
import java.util.HashMap;

public class ClientHandler extends Thread {
    private Socket socket;
    private int clientNumber;
    private static final HashMap<String, String> users = new HashMap<>();

    static {
        // Utilisateurs et mots de passe stockés en dur
        users.put("user1", "password1");
        users.put("user2", "password2");
    }

    public ClientHandler(Socket socket, int clientNumber) {
        this.socket = socket;
        this.clientNumber = clientNumber;
        System.out.println("New connection with client#" + clientNumber + " at " + socket);
    }

    public void run() {
        try (
                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream())
        ) {
            out.writeUTF("Veuillez entrer votre nom d'utilisateur :");
            String username = in.readUTF();

            out.writeUTF("Veuillez entrer votre mot de passe :");
            String password = in.readUTF();

            if (authenticate(username, password)) {
                out.writeUTF("Authentification réussie. Bienvenue, " + username + "!");
                System.out.println("Client#" + clientNumber + " authentifié en tant que " + username);
            } else {
                out.writeUTF("Échec de l'authentification. Connexion refusée.");
                System.out.println("Client#" + clientNumber + " a échoué l'authentification.");
                socket.close();
                return;
            }
        } catch (IOException e) {
            System.out.println("Error handling client# " + clientNumber + ": " + e);  //messages erreurs en francais ?
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Couldn't close a socket");  //messages erreurs en francais ?
            }
            System.out.println("Connection with client# " + clientNumber + " closed"); //messages erreurs en francais ?
        }
    }

    private boolean authenticate(String username, String password) {
        return users.containsKey(username) && users.get(username).equals(password);
    }
}