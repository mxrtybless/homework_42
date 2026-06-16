import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

public class ClientConnection {

    private final Socket socket;
    private final Scanner reader;
    private final PrintWriter writer;
    private final String username;

    public ClientConnection(Socket socket) throws IOException {

        this.socket = socket;

        InputStream input = socket.getInputStream();
        OutputStream output = socket.getOutputStream();

        reader = new Scanner(input, "UTF-8");
        writer = new PrintWriter(output);

        username = "user_" + (new Random().nextInt(9000) + 1000);
    }

    public String getUsername() {
        return username;
    }

    public String readMessage() {
        return reader.nextLine();
    }

    public void sendMessage(String message) {
        writer.write(message);
        writer.write(System.lineSeparator());
        writer.flush();
    }

    public void close() throws IOException {
        socket.close();
    }
}