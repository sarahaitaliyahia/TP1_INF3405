import java.io.*;
import java.net.*;
import java.util.*;

public class Serveur {
    private static final int port = 5020;
    private static ServerSocket listener;
    private static final List<ClientHandler> clients = new ArrayList<>();
    private static final Map<String, String> users = new HashMap<>();  // Utilisateurs et mots de passe

    public static void main(String[] args) throws IOException {
        // Ajouter des utilisateurs avec leurs mots de passe
        users.put("user1", "password1");
        users.put("user2", "password2");

        listener = new ServerSocket(port);
        System.out.println("Serveur démarré sur le port " + port);

        while (true) {
            Socket clientSocket = listener.accept();  // Le serveur accepte une nouvelle connexion client
            new ClientHandler(clientSocket).start();  // Crée un thread pour chaque client
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private DataInputStream in;
        private DataOutputStream out;
        private String clientName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());

                // Authentification du client
                authenticate();

                // Ajouter ce client à la liste des clients connectés
                synchronized (clients) {
                    clients.add(this);
                }

                // Recevoir et diffuser les messages à tous les autres clients
                String message;
                while ((message = in.readUTF()) != null) {
                    System.out.println("Message reçu: " + message);
                    broadcastMessage(message);  // Diffuser le message à tous les clients
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Nettoyage et déconnexion du client
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (clients) {
                    clients.remove(this);
                }
                System.out.println(clientName + " s'est déconnecté.");
            }
        }

        // Méthode pour authentifier un client
        private void authenticate() throws IOException {
            out.writeUTF("Veuillez entrer votre nom d'utilisateur : ");
            clientName = in.readUTF();

            out.writeUTF("Veuillez entrer votre mot de passe : ");
            String password = in.readUTF();

            if (users.containsKey(clientName) && users.get(clientName).equals(password)) {
                out.writeUTF("Authentification réussie ! Bienvenue, " + clientName);
            } else {
                out.writeUTF("Authentification échouée. Connexion fermée.");
                socket.close();
                return;
            }
        }

        // Diffuser le message à tous les clients
        private void broadcastMessage(String message) {
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    try {
                        client.out.writeUTF(message);  // Envoie le message à chaque client
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
