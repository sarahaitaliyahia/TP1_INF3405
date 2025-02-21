import java.io.*;
import java.net.*;

public class ClientHandler extends Thread {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String clientName;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            // Authentification
            authenticate();

            // Recevoir et envoyer des messages
            String message;
            while ((message = in.readUTF()) != null) {
                System.out.println(clientName + ": " + message);
                broadcastMessage(message);  // Diffuser à tous les autres clients
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Méthode pour authentifier un client
    private void authenticate() throws IOException {
        out.writeUTF("Veuillez entrer votre nom d'utilisateur : ");
        clientName = in.readUTF();

        out.writeUTF("Veuillez entrer votre mot de passe : ");
        String password = in.readUTF();

        if ("password1".equals(password)) {  // Supposons que le mot de passe soit hardcodé
            out.writeUTF("Authentification réussie ! Bienvenue, " + clientName);
        } else {
            out.writeUTF("Authentification échouée.");
            socket.close();
        }
    }

    // Diffuser un message à tous les clients
    private void broadcastMessage(String message) {
        // Implémentez la logique pour envoyer ce message à tous les clients connectés
        // Vous pouvez ajouter une liste statique des clients dans le serveur pour cela
    }
}
