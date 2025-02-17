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

    public ClientHandler(Socket socket, int clientNumber) {
        this.socket = socket;
        this.clientNumber = clientNumber;
        System.out.println("New connection with client #" + clientNumber + " at " + socket);
    }

    public void run() { //Création de thread qui envoi un message à un client
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream()); //création de canal d’envoi
            out.writeUTF("Hello from server - you are client #" + clientNumber); //envoi de message
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
            try {
                socket.close();
            }
            catch (IOException e) {
                System.out.println("Couldn't close a socket, what's going on?");}
                System.out.println("Connection with client #" + clientNumber + " closed");
            }
    }


    private String formatMessage(String rawMessage) {
        String user = String.format("%s:%d", socket.getInetAddress().getHostName(), socket.getPort());
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        return String.format("[%s - %s - %s] : %s", user, time, rawMessage, rawMessage);
    }

    private static void displayMessageHistory() throws IOException {
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

    private void saveSentMessage(String message) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(HISTORY_TXT, true))) {
            bw.write(message);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error saving message: " + e.getMessage());
        }
    }

}