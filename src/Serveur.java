import java.util.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class Serveur {
    private static ServerSocket Listener;
    static String ipAddress;
    static int port;

    public static void main(String[] args) throws Exception {
        //serverAddress = "127.0.0.1"
        //serverPort = 5000

        int clientNumber = 0;
        configureServer();

        Listener = new ServerSocket();
        Listener.setReuseAddress(true);
        InetAddress serverIP = InetAddress.getByName(ipAddress);
        Listener.bind(new InetSocketAddress(serverIP, port));
        System.out.format("The server is running on %s:%d%n", ipAddress, port);

        try {
            while (true) {
                new ClientHandler(Listener.accept(), clientNumber++).start();
            }
        } finally {
            Listener.close();
        }
    }

    static void configureServer() {
        configureIpAdress();
        configurePort();
    }

    static void configureIpAdress() {
        System.out.println("Veuillez entrer une adresse IP valide (exemple du format requis : 192.168.1.1) : ");
        ipAddress = new Scanner(System.in).nextLine();

        while(!verifyIpAddress(ipAddress)){
            System.out.println("L'adresse IP entrée n'est pas valide, veuillez réessayer (exemple du format requis : 192.168.1.1) : ");
            ipAddress = new Scanner(System.in).nextLine();
        };
    }

    static boolean verifyIpAddress(String ipAddress) {
        if (ipAddress.isEmpty())
            return false;

        String[] parts = ipAddress.split( "\\." );
        if ( parts.length != 4 )
            return false;

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

    static void configurePort() {
        System.out.println("Veuillez entrer un port d'écoute compris entre 5000 et 5050 : ");
        port = new Scanner(System.in).nextInt();

        while(!verifyPort(port)){
            System.out.println("Le port entré n'est pas valide, veuillez réessayer (entre 5000 et 5050) : ");
            port = new Scanner(System.in).nextInt();
        };
    }

    static boolean verifyPort(int port) {
        return port >= 5000 && port <= 5050;
    }
}