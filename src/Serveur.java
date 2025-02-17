import java.io.IOException;
import java.util.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class Serveur {
    private static String ipAddress;
    private static int port;
    private static ServerSocket listener;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        int clientNumber = 0;

        configureServer();
        establishConnection();

        try {
            while (true) {
                new ClientHandler(listener.accept(), clientNumber++).start();
            }
        } finally {
            listener.close();
        }
    }

    private static void establishConnection() throws IOException {
        listener = new ServerSocket();
        listener.setReuseAddress(true);
        InetAddress serverIP = InetAddress.getByName(ipAddress);
        listener.bind(new InetSocketAddress(serverIP, port));
        System.out.format("The server is running on %s:%d%n", ipAddress, port);
    }

    private static void configureServer() {
        configureIpAdress();
        configurePort();
    }

    private static void configureIpAdress() {
        System.out.println("Veuillez entrer une adresse IP valide (exemple du format requis : 192.168.1.1) : ");
        ipAddress = scanner.nextLine();

        while(!verifyIpAddress(ipAddress)){
            System.out.println("L'adresse IP entrée n'est pas valide, veuillez réessayer (exemple du format requis : 192.168.1.1) : ");
            ipAddress = scanner.nextLine();
        };
    }

    private static boolean verifyIpAddress(String ipAddress) {
        if (ipAddress.isEmpty()) return false;

        String[] parts = ipAddress.split( "\\." );
        if ( parts.length != 4 )  return false;

        try {
            for (String part : parts) {
                int i = Integer.parseInt(part);
                if ((i < 0) || (i > 255))
                    return false;
            }
        }
        catch (Exception e) {
            return !ipAddress.endsWith(".");
        }

        return true;
    }

    private static void configurePort() {
        System.out.println("Veuillez entrer un port d'écoute compris entre 5000 et 5050 : ");
        port = scanner.nextInt();

        while(!verifyPort(port)){
            System.out.println("Le port entré n'est pas valide, veuillez réessayer (entre 5000 et 5050) : ");
            port = scanner.nextInt();
        };
    }

    private static boolean verifyPort(int port) {
        return port >= 5000 && port <= 5050;
    }
}