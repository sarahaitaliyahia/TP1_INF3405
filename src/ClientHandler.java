import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;


public class ClientHandler extends Thread { //pour traiter la demande de chaque client sur un socket particulier
    private static final int MAX_HISTORY_MESSAGES = 15;
    private static String HISTORY_TXT = "history.txt";
    private Socket socket;
    private int clientNumber;
    static DataInputStream in;
    static DataOutputStream out;
    private static final List<ClientHandler> clients = new ArrayList<>();
    private static final HashMap<String, String> users = new HashMap<>();

    static {
        // Utilisateurs et mots de passe stockés en dur
        users.put("user1", "password1");
        users.put("user2", "password2");
    }

    public ClientHandler(Socket socket, int clientNumber) {
        this.socket = socket;
        this.clientNumber = clientNumber;
        System.out.println("New connection with client #" + clientNumber + " at " + socket);
    }

    public void run() { //Création de thread qui envoi un message à un client
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream()); //création de canal d’envoi
            clients.add(this);

//            out.writeUTF("Veuillez entrer votre nom d'utilisateur :");
//            String username = in.readUTF();
//
//            out.writeUTF("Veuillez entrer votre mot de passe :");
//            String password = in.readUTF();
//
//            if (authenticate(username, password)) {
//                out.writeUTF("Authentification réussie. Bienvenue, " + username + "!");
//                System.out.println("Client#" + clientNumber + " authentifié en tant que " + username);
//            } else {
//                out.writeUTF("Échec de l'authentification. Connexion refusée.");
//                System.out.println("Client#" + clientNumber + " a échoué l'authentification.");
//                socket.close();
//                return;
//            }

            this.displayMessageHistory();

            String message;
            while((message = in.readUTF()) != null) {
                String formattedMessage = this.formatMessage(message);
                this.saveSentMessage(formattedMessage);
                this.broadcastMessage(formattedMessage);
            }
        }
        catch (IOException e) {
            System.out.println("Error handling client # " + clientNumber + ": " + e);
            throw new RuntimeException(e);
        }

//        try{
//              clientAuthentification();

//              out.writeUTF("Utilisez la commande Ctrl+C pour quitter la conversation et vous déconnecter.");
//              out.writeUTF("Veuillez respecter la limite de 200 caractères par message envoyé.");

//              displayMessageHistory();
//              broadcastMessage();
//              saveSentMessage();
//        }
//        catch (Exception e) {
//        }

        finally {
            clients.remove(this);
            try {
                socket.close();
            }
            catch (IOException e) {
                System.out.println("Couldn't close a socket, what's going on?");}
                System.out.println("Connection with client #" + clientNumber + " closed");
            }
    }

    private boolean authenticate(String username, String password) {
        return users.containsKey(username) && users.get(username).equals(password);
    }

    private String formatMessage(String rawMessage) {
        String user = String.format("%s:%d", socket.getInetAddress().getHostName(), socket.getPort());
        String time = new SimpleDateFormat("yyyy-MM-dd'@'HH:mm:ss").format(new Date());
        return String.format("[%s - %s]: %s", user, time, rawMessage);
    }

    private void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            try {
                client.out.writeUTF(message);
            } catch (IOException e) {
                System.out.println("Erreur dans l'envoi du message");
            }
        }

    }

    private void saveSentMessage(String message) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(HISTORY_TXT, true))) {
            bw.write(message);
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            System.out.println("Error saving message: " + e.getMessage());
        }
    }

    private void displayMessageHistory() throws IOException {
        List<String> lastMessages = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(HISTORY_TXT))) {
            String line;
            while ((line = br.readLine()) != null) {
                lastMessages.add(line);
            }
        } catch (FileNotFoundException e) {
            System.out.println("History file not found, starting fresh.");
        }

        int start = Math.max(0, lastMessages.size() - MAX_HISTORY_MESSAGES);
        List<String> recentMessages = lastMessages.subList(start, lastMessages.size());

        for (String message : recentMessages) {
            out.writeUTF(message);
        }
    }

}