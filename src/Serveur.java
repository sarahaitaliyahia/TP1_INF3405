import java.util.*;
import java.net.*;
import java.io.*;

public class Serveur {

    public static void main(String[] args) throws Exception {
        String ipAddress = NetworkValidation.configureIpAddress();
        int port = NetworkValidation.configurePort();

        ServerSocket listener = new ServerSocket();
        listener.setReuseAddress(true);

        InetAddress serverIP = InetAddress.getByName(ipAddress);
        listener.bind(new InetSocketAddress(serverIP, port));
        System.out.format("Le serveur fonctionne sur %s:%d%n", ipAddress, port);

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