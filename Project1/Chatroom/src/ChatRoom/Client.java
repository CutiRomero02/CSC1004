package ChatRoom;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.time.*;
import java.time.format.DateTimeFormatter;

import static java.nio.charset.StandardCharsets.UTF_8;

// Client Side Code
public class Client {
    private PrintWriter writer;
    private BufferedReader reader;
    private Scanner scanner =  new Scanner(System.in);

    // Client side stored ID and username.
    public String username;
    public int id;

    private void go() {
        setUpConnection();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new IncomingReader());
        sendMessage();
    }

    // Establishing connection with the server.
    private void setUpConnection() {
        try {
            InetSocketAddress serverAddress = new InetSocketAddress("127.0.0.1", 5432);
            SocketChannel socketChannel = SocketChannel.open(serverAddress);
            reader = new BufferedReader(Channels.newReader(socketChannel, UTF_8));
            writer = new PrintWriter(Channels.newWriter(socketChannel, UTF_8));
            System.out.println("Connection established.");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Send message to the Server.
    private void sendMessage() {
        while (scanner.hasNextLine()) {
            String text = scanner.nextLine();
            if (username == null) {
                username = text;
                writer.println("@" + id + "@" + username);
                writer.flush();
            }

            // Sending commands.
            else if (text.startsWith("/")) {

                // Exit the chatroom
                if (text.equals("/exit")) {
                    writer.println("/exit" + id);
                    writer.flush();
                }

                // View the list of commands
                else if (text.equals("/help")) {
                    writer.println("/help" + id);
                    writer.flush();
                }

                // Search for specific keywords.
                else if (text.startsWith("/search") && text.endsWith(")")) {
                    writer.println(text + id);
                    writer.flush();
                }

                // Print the receivers of the last message the user sent.
                else if (text.equals("/printReceiver")) {
                    writer.println("/printReceiver" + id);
                    writer.flush();
                }
                else {
                    System.out.println("!!! Invalid command. Please try again.");
                }
            }

            // Sending messages.
            else {
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedDate = now.format(dtf);
                writer.println(formattedDate + " [" + id + "]" + username + " : " + text);
                writer.flush();
            }
        }
    }

    public static void main(String[] args) {
        new Client().go();
    }

    // Read messages sent from the server.
    public class IncomingReader implements Runnable {
        public void run() {
            String message;
            try {
                while ((message = reader.readLine()) != null) {
                    if (message.charAt(0) == '#') {
                        if (message.startsWith("#Your ID is : ")) {
                            id = Integer.parseInt(message.substring(14));
                        }
                        System.out.println(message);
                    }
                    else {
                        System.out.println(message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
