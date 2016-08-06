import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Hermann Grieder on 16.07.2016.
 *
 */

public class AtlantisServer {

    private static final int PORT = 9000;
    private static int clientNumber;
    private static HashMap<Long, Socket> clientThreads = new HashMap<>();

    public static void main(String[] args) throws IOException {

        AtlantisServer server = new AtlantisServer();

        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server is running");
        System.out.println("Listening on port: " + PORT);

        while (true) {

            printUserCount();

            try {

                //Create Database connection
                System.out.println("Create Database Handler");
                DatabaseHandler databaseHandler = new DatabaseHandler();
                System.out.println("Done");

                Socket clientSocket = serverSocket.accept();
                System.out.println("Connection accepted: " + clientSocket.getInetAddress().getCanonicalHostName());

                ClientThread chatServerThread = new ClientThread(clientSocket, ++clientNumber, server, databaseHandler);
                clientThreads.put(chatServerThread.getId(), clientSocket);
                chatServerThread.start();

            } catch (IOException e) {
                System.out.println("Server was unable to accept user connection");
            }
        }
    }

    private static void printUserCount(){
        if (clientThreads.size() == 0) {
            System.out.println("No user currently connected");
        } else if (clientThreads.size() == 1){
            System.out.println(clientThreads.size() + " user currently connected");
        }else{
            System.out.println(clientThreads.size() + " users currently connected");
        }
    }

    void removeThread(long threadID) {
        System.out.println("Thread ID: " + threadID);
        System.out.println(clientThreads.size());

        clientThreads.remove(threadID);
        System.out.println(clientThreads.size());
    }
}