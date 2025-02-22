import java.net.*;

public class Serveur {
    public static void main(String[] args) throws Exception {
        System.out.println("************* Bienvenue dans le système de clavardage *************");
        String serverIpAddress = NetworkValidation.configureIpAddress();
        int serverPort = NetworkValidation.configurePort();

        ServerSocket listener = new ServerSocket();
        listener.setReuseAddress(true);

        InetAddress serverIP = InetAddress.getByName(serverIpAddress);
        listener.bind(new InetSocketAddress(serverIP, serverPort));
        System.out.format("************* Connexion du serveur réussie, le serveur fonctionne sur : [%s:%d] *************%n",
                serverIpAddress, serverPort);

        try {
            while (true) {
                Socket clientSocket = listener.accept();
                new ClientHandler(clientSocket).start();
            }
        } finally {
            listener.close();
        }
    }
}