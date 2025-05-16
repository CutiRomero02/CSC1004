package ChatRoom;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import static java.nio.charset.StandardCharsets.UTF_8;

public class Server {

    // The list of the client writers.
    private final List<PrintWriter> clientWriters = new ArrayList<>();

    // The list of the clients using the User class to store and deal with the client's information.
    private List<User> clientList = new ArrayList<>();

    static final int minID = 10000;


    // Establishing connection and initializing basic information of the clients.
    public void go() {
        ExecutorService threadPool = Executors.newCachedThreadPool();
        try {
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(5432));

            while (serverChannel.isOpen()) {

                // Connecting the users via SocketChannel.
                SocketChannel clientSocket = serverChannel.accept();
                PrintWriter writer = new PrintWriter(Channels.newWriter(clientSocket, UTF_8));
                clientWriters.add(writer);
                writer.println("#Welcome to the Chat Room!");

                // Show the current users in the chatroom to the new client.
                String currentUser = "Current Users: ";
                for (User user : clientList) {
                    currentUser = currentUser + "[" + user.id + "]" + user.name + " ";
                }
                writer.println(currentUser);

                // Assign the ID to the new client.
                int id = getID(minID);
                clientList.add(new User(null, id));

                writer.println("#Your ID is : " + (id));
                writer.println("#Please enter your name:");
                writer.flush();

                threadPool.submit(new ClientHandler(clientSocket));
                System.out.println("Client connected.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper method to get the index in the ClientList via the user's ID.
    private int getUserIndex(int id) {
        for(int i = 0; i < clientList.size(); i++) {
            if (clientList.get(i).id == id) {
                return i;
            }
        }
        return 0;
    }

    // Send a message only to a specific client via ID.
    private void sendToUser(int id, String message) {
        int index = getUserIndex(id);
        PrintWriter writer = clientWriters.get(index);
        writer.println(message);
        writer.flush();
    }

    // Send a message only to a specific client via the index of the ClientList.
    private void sendToIndexUser(int index, String message) {
        PrintWriter writer = clientWriters.get(index);
        writer.println(message);
        writer.flush();
    }

    // Send a message to all users in the chatroom.
    private void broadcast(String message) {
        for (PrintWriter writer : clientWriters) {
            writer.println(message);
            writer.flush();
        }
    }

    // Helper method to get all the receiver of a certain message.
    private String getMessageReceiver() {
        String result = "Last Message Receivers:";
        for (User user : clientList) {
            result += " [" + user.id + "]" + user.name + ",";
        }
        return result.substring(0, result.length() - 1);
    }

    // Helper method to get a unique ID for a new user.
    private int getID(int start) {
        for (User user : clientList) {
            if (user.id == start) {
                return getID(start + 1);
            }
        }
        return start;
    }

    public static void main(String[] args) {
        new Server().go();
    }

    // ClientHandler to handle the communication with clients.
    private class ClientHandler implements Runnable {
        BufferedReader reader;
        SocketChannel socket;

        public ClientHandler(SocketChannel clientSocket) {
            socket = clientSocket;
            reader = new BufferedReader(Channels.newReader(socket, UTF_8));
        }

        public void run() {
            String message;
            try {
                while ((message = reader.readLine()) != null) {
                    System.out.println("read " + message);

                    // Special communication to assign the username to the User representation on the server side.
                    if (message.charAt(0) == '@') {
                        String[] split = message.split("@");
                        int userID =  Integer.parseInt(split[1]);
                        String userName = split[2];
                        for (int i = clientList.size() - 1; i >= 0; i--) {
                            if (clientList.get(i).id == userID) {
                                clientList.get(i).setName(userName);
                                break;
                            }
                        }

                        // Display the chat records to the new client.
                        File file = new  File("ChatRecord.txt");
                        if (file.exists()) {
                            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                                String line;
                                while ((line = br.readLine()) != null) {
                                    sendToUser(userID, line);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            try {
                                file.createNewFile();
                                try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                                    bw.write("[CHAT RECORD]\n");
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        // Welcome the new user.
                        broadcast("[" + userID + "]" + userName +  " has entered the chatroom!");

                        // Display the list of built commands to the new user.
                        sendToUser(userID, "Some useful commands:");
                        sendToUser(userID, "'/exit': exit the chatroom.");
                        sendToUser(userID, "'/printReceiver': print the names of the receivers of the last message you sent.");
                        sendToUser(userID, "'/search(replace_this_with_the_keyword_you_want_to_search)': search for chat logs that contain specific keywords or username.");
                        sendToUser(userID, "'/help': display the list of available commands.");
                    }

                    // Handle commands.
                    else if (message.charAt(0) == '/') {
                        String cmdUserName;

                        // When user asks to exit, delete it from the clientList and cut of the connection.
                        if (message.startsWith("/exit")) {
                            int cmdUserID = Integer.parseInt(message.substring(5));
                            for (int i = 0; i < clientList.size(); i++) {
                                if (clientList.get(i).id == cmdUserID) {
                                    cmdUserName = clientList.get(i).name;
                                    broadcast("[" + cmdUserID + "]" + cmdUserName + " has left the chatroom!");
                                    clientList.remove(i);
                                    clientWriters.get(i).close();
                                    clientWriters.remove(i);
                                    break;
                                }
                            }
                        }

                        // Search for a certain keyword in the chat record.
                        else if (message.startsWith("/search")) {
                            int cmdUserID = Integer.parseInt(message.substring(message.length() - 5));
                            File myFile = new File("ChatRecord.txt");
                            FileReader fr = new FileReader(myFile);
                            BufferedReader br = new BufferedReader(fr);
                            String line;
                            String pattern = message.substring(8, message.length() - 6);
                            boolean empty = true;
                            while((line = br.readLine()) != null) {
                                if (line.contains(pattern)) {
                                    sendToUser(cmdUserID, line);
                                    empty = false;
                                }
                            }
                            if (empty) {
                                sendToUser(cmdUserID, "No records found!");
                            }
                        }

                        // Display the list of commands to the user.
                        else if (message.startsWith("/help")) {
                            int cmdUserID = Integer.parseInt(message.substring(5));
                            sendToUser(cmdUserID, "Some useful commands:");
                            sendToUser(cmdUserID, "'/exit': exit the chatroom.");
                            sendToUser(cmdUserID, "'/printReceiver': print the names of the receivers of the last message you sent.");
                            sendToUser(cmdUserID, "'/search(replace_this_with_the_keyword_you_want_to_search)': search chat logs for specific keywords or username.");
                            sendToUser(cmdUserID, "'/help': display the list of available commands.");
                        }

                        // Print the receivers of the client's last message.
                        else if (message.startsWith("/printReceiver")) {
                            int cmdUserID = Integer.parseInt(message.substring(14));
                            int cmdUserIndex = getUserIndex(cmdUserID);
                            sendToIndexUser(cmdUserIndex, clientList.get(cmdUserIndex).lastMessageReceiver);
                        }
                    }

                    // Ordinary chatting and chat logs storage.
                    else {
                        broadcast(message);

                        // Save the receivers of the message.
                        int msgID = Integer.parseInt(message.substring(21, 26));
                        int msgIndex = getUserIndex(msgID);
                        clientList.get(msgIndex).lastMessageReceiver = getMessageReceiver();

                        // Save the chat logs into a server side text file.
                        try {
                            Files.write(Paths.get("ChatRecord.txt"), Arrays.asList(message), StandardOpenOption.APPEND);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    // A server side helper class to represent the clients and their basic information.
    private class User {
        private String name;
        private int id;
        private String lastMessageReceiver;

        public User(String name, int id) {
            this.name = name;
            this.id = id;
            lastMessageReceiver = "No message found";
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}


