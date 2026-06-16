import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EchoServer {

    private final int port;
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private final Set<ClientConnection> clients = ConcurrentHashMap.newKeySet();

    private EchoServer(int port) {
        this.port = port;
    }

    public static EchoServer bindToPort(int port) {
        return new EchoServer(port);
    }

    public void run() {
        try (ServerSocket server = new ServerSocket(port)) {
            while (!server.isClosed()) {
                Socket clientSocket = server.accept();
                pool.submit(() -> handle(clientSocket));
            }
        } catch (IOException e) {
            System.out.printf("Вероятнее всего порт %s занят.%n", port);
            e.printStackTrace();
        }
    }

    private void handle(Socket socket) {
        try {
            ClientConnection client = new ClientConnection(socket);
            clients.add(client);

            System.out.println("Connected: " + client.getUsername());
            client.sendMessage("Your name is " + client.getUsername());

            while (true) {
                String message = client.readMessage();

                if (message == null || message.isBlank() || "bye".equalsIgnoreCase(message)) {
                    clients.remove(client);
                    client.close();
                    return;
                }

                System.out.printf("%s: %s%n", client.getUsername(), message);

                broadcast(client.getUsername(), message, client);
            }
        } catch (Exception e) {
            System.out.println("Client disconnected");
        }
    }

    private void broadcast(String sender, String message, ClientConnection senderClient) {
        String result = sender + ": " + message;

        clients.forEach(client -> {
            if (client == senderClient) {
                return;
            }

            try {
                client.sendMessage(result);
            } catch (Exception ignored) {
            }
        });
    }

    private boolean isQuitMsg(String message) {
        return "bye".equalsIgnoreCase(message);
    }

    private boolean isEmptyMsg(String message) {
        return message == null || message.isBlank();
    }

    private void sendResponse(String response, PrintWriter writer) {
        writer.write(response);
        writer.write(System.lineSeparator());
        writer.flush();
    }

    private PrintWriter getWriter(Socket socket) throws IOException {
        OutputStream stream = socket.getOutputStream();
        return new PrintWriter(stream);
    }

    private Scanner getReader(Socket socket) throws IOException {
        InputStream stream = socket.getInputStream();
        return new Scanner(stream, "UTF-8");
    }
}