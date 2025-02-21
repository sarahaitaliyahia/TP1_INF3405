import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;


public class ClientHandler extends Thread { //pour traiter la demande de chaque client sur un socket particulier
    private static final int MAX_HISTORY_MESSAGES = 15;
    private static String HISTORY_TXT = "history.txt";
    private Socket socket;
    private int clientNumber;
     DataInputStream in;
     DataOutputStream out;
    private static final List<ClientHandler> clients = new ArrayList<>();
    private static final HashMap<String, String> users = new HashMap<>();

    static {
        loadUsers();
    }

    private static void loadUsers() {
        File file = new File("accounts.txt");
        if (!file.exists()) return; // No accounts file exists yet

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    users.put(parts[0], parts[1]); // Load username and password
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
    }

    public ClientHandler(Socket socket, int clientNumber) {
        this.socket = socket;
        this.clientNumber = clientNumber;
        addClient(this);

        System.out.println("New connection with client #" + clientNumber + " at " + socket);
    }

    public void run() {
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            out.writeUTF("Veuillez entrer votre nom d'utilisateur :");
            String username = in.readUTF().trim();

            out.writeUTF("Veuillez entrer votre mot de passe :");
            String password = in.readUTF().trim();

            if (username.isEmpty() || password.isEmpty()) {
                out.writeUTF("Nom d'utilisateur et mot de passe ne doivent pas être vides.");
                socket.close();
                return;
            }

            synchronized (users) { // Ensure thread safety
                if (users.containsKey(username)) {
                    if (!users.get(username).equals(password)) {
                        out.writeUTF("Erreur dans la saisie du mot de passe.");
                        System.out.println("Client#" + clientNumber + " a échoué l'authentification.");
                        socket.close();
                        return;
                    } else {
                        out.writeUTF("Authentification réussie. Bienvenue, " + username + "!");
                        System.out.println("Client#" + clientNumber + " authentifié en tant que " + username);
                    }
                } else {
                    // **New Account Creation + Save to File**
                    users.put(username, password);
                    saveNewUser(username, password); // Save to accounts.txt
                    out.writeUTF("Compte créé et authentification réussie. Bienvenue, " + username + "!");
                    System.out.println("Nouveau compte créé pour " + username + " par client#" + clientNumber);
                }
            }

            this.displayMessageHistory();

            String message;
            while ((message = in.readUTF()) != null) {
                String formattedMessage = this.formatMessage(message);
                this.saveSentMessage(formattedMessage);
                this.broadcastMessage(formattedMessage);
            }
    } catch(IOException e) {
        System.out.println("Erreur lors de la gestion du client #" + clientNumber + " : " + e);
    } finally {
        removeClient(this);
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Impossible de fermer le socket du client #" + clientNumber);
        }
        System.out.println("Connexion avec le client #" + clientNumber + " fermée.");
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
        synchronized (clients) {
            for (ClientHandler client : clients) {
                try {
                    client.out.writeUTF(message);
                } catch (IOException e) {
                    System.out.println("Erreur dans l'envoi du message");
                }
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
    private static void saveNewUser(String username, String password) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("accounts.txt", true))) {
            bw.write(username + ":" + password);
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            System.out.println("Erreur lors de l'enregistrement du nouvel utilisateur: " + e.getMessage());
        }
    }

    private synchronized void addClient(ClientHandler client) {
        clients.add(client);
    }

    private synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
    }
}