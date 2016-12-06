package ch.atlantis.server;

import ch.atlantis.database.DatabaseHandler;
import ch.atlantis.game.GameManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * Created by Hermann Grieder on 16.07.2016.
 * <p>
 * Main class of the Server.
 * <p>
 * Accepts user connections and hands each of them off to a new clientThread.
 */

public class AtlantisServer {

    private static final int PORT = 9000;
    private static HashMap<Long, Socket> clientThreads = new HashMap<>();

    public static void main(String[] args) throws IOException {

        AtlantisServer server = new AtlantisServer();

        GameManager gameManager = new GameManager();

        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server is running");
        System.out.println("Listening on port: " + PORT);

        //Create Database connection
        DatabaseHandler databaseHandler = new DatabaseHandler();

        while (true) {

            printUserCount();

            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connection accepted: " + clientSocket.getRemoteSocketAddress());

                ClientThread clientThread = new ClientThread(clientSocket, server, databaseHandler, gameManager);
                clientThreads.put(clientThread.getId(), clientSocket);
                System.out.println("Starting Thread: " + clientThread.getId());
                clientThread.start();

            } catch (IOException e) {
                System.out.println("Server was unable to accept user connection");
            }
        }
    }

    private static void printUserCount() {
        if (clientThreads.size() == 0) {
            System.out.println("No user currently connected");
        } else if (clientThreads.size() == 1) {
            System.out.println(clientThreads.size() + " user currently connected");
        } else {
            System.out.println(clientThreads.size() + " users currently connected");
        }
    }

    void removeThread(long threadID) {
        System.out.println("Thread ID: " + threadID);
        System.out.println("Active Threads: " + clientThreads.size());
        clientThreads.remove(threadID);
        System.out.println("Thread# " + threadID + " removed");
        System.out.println("Active Threads: " + clientThreads.size());
    }

    private int createGuestNumber() {

        int guestNumber = 0;
        if (clientThreads.size() > 0) {
            guestNumber = clientThreads.size();
        }

        return guestNumber;
    }

    public int getGuestNumber() {
        return this.createGuestNumber();
    }
}
