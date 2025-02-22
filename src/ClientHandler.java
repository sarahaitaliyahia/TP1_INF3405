import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

public class ClientHandler extends Thread {
    private static final int MAX_HISTORY_MESSAGES = 15;
    private static final String HISTORY_TXT = "history.txt";

    private static final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private static final HashMap<String, String> users = new HashMap<>();

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;

    static {
        loadUpUsers();
    }

    public ClientHandler(Socket socket) {
        this.socket = socket;
        System.out.println("************* Nouvelle connexion client à " + socket + " *************");
    }

    public void run() {
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            if (!checkCredentials()) {
                socket.close();
                return;
            }

            synchronized (clients) {
                clients.add(this);
            }

            displayMessageHistory();

            String message;
            while (true) {
                try {
                    message = in.readUTF();
                    String formattedMessage = buildMessageString(message);
                    saveSentMessage(formattedMessage);
                    broadcastMessage(formattedMessage);
                } catch (IOException error) {
                    System.out.println("************* Client " + username + " déconnecté *************");
                    break;
                }
            }
        } catch (IOException error) {
            System.out.println(" ! Erreur lors de la gestion du client : " + error.getMessage());
        } finally {
            terminate();
        }
    }

    private static void loadUpUsers() {
        File file = new File("accounts.txt");
        if (!file.exists()) return;

        try (BufferedReader buffReader = new BufferedReader(new FileReader(file))) {
            String user;
            while ((user = buffReader.readLine()) != null) {
                String[] parts = user.split(":");
                if (parts.length == 2) users.put(parts[0], parts[1]);
            }
        } catch (IOException error) {
            System.out.println(" ! Erreur du chargement de l'utilisateur : " + error.getMessage());
        }
    }

    private boolean checkCredentials() throws IOException {
        out.writeUTF(" > Veuillez entrer votre nom d'utilisateur : ");
        username = in.readUTF().trim();

        out.writeUTF(" > Veuillez entrer votre mot de passe : ");
        String password = in.readUTF().trim();

        if (username.isEmpty() || password.isEmpty()) {
            out.writeUTF(" ! Le nom d'utilisateur et le mot de passe ne doivent pas être vides");
            return false;
        }

        synchronized (users) {
            if (users.containsKey(username)) {
                if (!users.get(username).equals(password)) {
                    out.writeUTF(" ! Erreur dans la saisie du mot de passe");
                    return false;
                } else {
                    out.writeUTF("************* Authentification réussie, bienvenue " + username + "! *************");
                    return true;
                }
            } else {
                users.put(username, password);
                saveNewUser(username, password);
                out.writeUTF("************* Compte créé et authentification réussie, bienvenue " + username + "! *************");
                out.writeUTF(
                        "************* Veuillez envoyer des messages de moins de 200 caractères. Faites Ctrl+C pour quitter *************");
                return true;
            }
        }
    }

    private String buildMessageString(String rawMessage) {
        String clientIp = socket.getInetAddress().getHostAddress();
        int clientPort = socket.getPort();

        String timestamp = new SimpleDateFormat("yyyy-MM-dd@HH:mm:ss").format(new Date());
        return String.format("[%s - %s:%d - %s]: %s", username, clientIp, clientPort, timestamp, rawMessage);
    }


    private void broadcastMessage(String message) {
        synchronized (clients) {
            Iterator<ClientHandler> iterator = clients.iterator();
            while (iterator.hasNext()) {
                ClientHandler client = iterator.next();
                try {
                    client.out.writeUTF(message);
                } catch (IOException error) {
                    System.out.println("************* Client " + client.username + " déconnecté *************");
                    iterator.remove();
                }
            }
        }
    }

    private void saveSentMessage(String message) {
        try (BufferedWriter buffWriter = new BufferedWriter(new FileWriter(HISTORY_TXT, true))) {
            buffWriter.write(message);
            buffWriter.newLine();
            buffWriter.flush();
        } catch (IOException error) {
            System.out.println(" ! Erreur lors de l'enregistrement du message : " + error.getMessage());
        }
    }

    private void displayMessageHistory() throws IOException {
        List<String> lastMessages = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(HISTORY_TXT))) {
            String line;
            while ((line = br.readLine()) != null) {
                lastMessages.add(line);
            }
        } catch (FileNotFoundException error) {
            System.out.println(" ! Aucun fichier d'historique trouvé");
        }

        int start = Math.max(0, lastMessages.size() - MAX_HISTORY_MESSAGES);
        List<String> recentMessages = lastMessages.subList(start, lastMessages.size());

        for (String message : recentMessages) {
            out.writeUTF(message);
        }
    }

    private static void saveNewUser(String username, String password) {
        try (BufferedWriter buffWriter = new BufferedWriter(new FileWriter("accounts.txt", true))) {
            buffWriter.write(username + ":" + password);
            buffWriter.newLine();
            buffWriter.flush();
        } catch (IOException error) {
            System.out.println(" ! Erreur lors de l'enregistrement du nouvel utilisateur : " + error.getMessage());
        }
    }

    private void terminate() {
        try {
            socket.close();
            in.close();
            out.close();
        } catch (IOException error) {
            System.out.println(" ! Impossible de fermer le socket du client : " + error.getMessage());
        }
        synchronized (clients) {
            clients.remove(this);
        }
        System.out.println("************* Connexion du client " + username + " fermée *************");
    }
}
