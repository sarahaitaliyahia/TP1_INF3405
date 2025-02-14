import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static Socket socket;

    public static void main(String[] args) throws Exception {
        String serverAddress = "127.0.0.1";
        int port = 5000;

        socket = new Socket(serverAddress, port);
        System.out.println("Connected to server");

        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        Scanner scanner = new Scanner(System.in);

        System.out.print(in.readUTF());
        String username = scanner.nextLine();
        out.writeUTF(username);

        System.out.print(in.readUTF());
        String password = scanner.nextLine();
        out.writeUTF(password);

        System.out.println(in.readUTF());

        socket.close();
    }
}
