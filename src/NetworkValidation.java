import java.util.Scanner;

public class NetworkValidation {
    private static final Scanner scanner = new Scanner(System.in);

    public static String configureIpAddress() {
        System.out.println(
                " > Veuillez entrer une adresse IP valide (exemple du format requis : 192.168.1.1) : ");
        String ipAddress = scanner.nextLine();

        while (!verifyIpAddress(ipAddress)) {
            System.out.println(
                    " ! L'adresse IP entrée n'est pas valide, veuillez réessayer (exemple du format requis : 192.168.1.1) : ");
            ipAddress = scanner.nextLine();
        }
        return ipAddress;
    }

    public static boolean verifyIpAddress(String ipAddress) {
        if (ipAddress.isEmpty()) return false;

        String[] parts = ipAddress.split("\\.");
        if (parts.length != 4) return false;

        try {
            for (String part : parts) {
                int i = Integer.parseInt(part);
                if (i < 0 || i > 255) return false;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public static int configurePort() {
        System.out.println(" > Veuillez entrer un port d'écoute compris entre 5000 et 5050 : ");
        int port = Integer.parseInt(scanner.nextLine());

        while (!verifyPort(port)) {
            System.out.println(" ! Le port entré n'est pas valide, veuillez réessayer (entre 5000 et 5050) : ");
            port = Integer.parseInt(scanner.nextLine());
        }

        return port;
    }

    public static boolean verifyPort(int port) {
        return port >= 5000 && port <= 5050;
    }
}
