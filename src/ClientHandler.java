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
        loadUsers();
    }

    private static void loadUsers() {
        File file = new File("accounts.txt");
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String user;
            while ((user = br.readLine()) != null) {
                String[] parts = user.split(":");
                if (parts.length == 2) {
                    users.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
    }

    public ClientHandler(Socket socket) {
        this.socket = socket;
        System.out.println("New connection with client at " + socket);
    }

    public void run() {
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            if (!authenticateUser()) {
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
                    String formattedMessage = formatMessage(message);
                    saveSentMessage(formattedMessage);
                    broadcastMessage(formattedMessage);
                } catch (IOException e) {
                    System.out.println("Client " + username + " disconnected.");
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Erreur lors de la gestion du client : " + e);
        } finally {
            disconnectClient();
        }
    }

    private boolean authenticateUser() throws IOException {
        out.writeUTF("Veuillez entrer votre nom d'utilisateur :");
        username = in.readUTF().trim();

        out.writeUTF("Veuillez entrer votre mot de passe :");
        String password = in.readUTF().trim();

        if (username.isEmpty() || password.isEmpty()) {
            out.writeUTF("Nom d'utilisateur et mot de passe ne doivent pas être vides.");
            return false;
        }

        synchronized (users) {
            if (users.containsKey(username)) {
                if (!users.get(username).equals(password)) {
                    out.writeUTF("Erreur dans la saisie du mot de passe.");
                    return false;
                } else {
                    out.writeUTF("Authentification réussie. Bienvenue, " + username + "!");
                    return true;
                }
            } else {
                users.put(username, password);
                saveNewUser(username, password);
                out.writeUTF("Compte créé et authentification réussie. Bienvenue, " + username + "!");
                return true;
            }
        }
    }

    private String formatMessage(String rawMessage) {
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
                } catch (IOException e) {
                    System.out.println("Client " + client.username + " déconnecté.");
                    iterator.remove(); // Remove disconnected client
                }
            }
        }
    }

    private void saveSentMessage(String message) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(HISTORY_TXT, true))) {
            bw.write(message);
            bw.newLine();
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

    private static void saveNewUser(String username, String password) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("accounts.txt", true))) {
            bw.write(username + ":" + password);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Erreur lors de l'enregistrement du nouvel utilisateur: " + e.getMessage());
        }
    }

    private void disconnectClient() {
        try {
            socket.close();
            in.close();
            out.close();
        } catch (IOException e) {
            System.out.println("Impossible de fermer le socket du client.");
        }
        synchronized (clients) {
            clients.remove(this);
        }
        System.out.println("Connexion avec le client " + username + " fermée.");
    }
}
