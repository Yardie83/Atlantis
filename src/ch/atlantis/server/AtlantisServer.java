package ch.atlantis.server;

import ch.atlantis.database.DatabaseHandler;
import ch.atlantis.game.GameManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by Hermann Grieder on 16.07.2016.
 * <p>
 * Main class of the Server.
 * <p>
 * Accepts user connections and hands each of them off to a new clientThread.
 */

public class AtlantisServer {

    public static final String AtlantisLogger = AtlantisServer.class.getSimpleName();
    private static Logger logger;
    private static FileHandler fh;

    private static final int PORT = 9000;
    private static HashMap<Long, Socket> clientThreads = new HashMap<>();
    private int guestNumber;

    public static void main(String[] args) throws IOException {

        logger = Logger.getLogger(AtlantisLogger);

        configLogger();

        AtlantisServer server = new AtlantisServer();

        GameManager gameManager = new GameManager();

        ServerSocket serverSocket = new ServerSocket(PORT);
        logger.info("Server is running");
        logger.info("Listening on port: " + PORT);

        //Create Database connection
        DatabaseHandler databaseHandler = new DatabaseHandler();

        while (true) {

            printUserCount();

            try {
                Socket clientSocket = serverSocket.accept();
                logger.info("Connection accepted: " + clientSocket.getRemoteSocketAddress());

                ClientThread clientThread = new ClientThread(clientSocket, server, databaseHandler, gameManager);
                clientThreads.put(clientThread.getId(), clientSocket);
                logger.info("Starting Thread: " + clientThread.getId());
                clientThread.start();
            } catch (IOException e) {
                logger.info("Server was unable to accept user connection");
            }
        }
    }

    private static void configLogger() {
        try {

            // Configure logger with handler and formatter
            fh = new FileHandler("AtlantisServerLog.txt", 50000, 1);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();

            fh.setFormatter(formatter);

            logger.setLevel(Level.INFO);

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printUserCount() {
        if (clientThreads.size() == 0) {
            System.out.println("No user currently connected");
        } else if (clientThreads.size() == 1) {
            logger.info(clientThreads.size() + " user currently connected");
        } else {
            logger.info(clientThreads.size() + " users currently connected");
        }
    }

    void removeThread(long threadID) {
        logger.info("Thread ID: " + threadID);
        logger.info("Active Threads: " + clientThreads.size());
        clientThreads.remove(threadID);
        logger.info("Thread# " + threadID + " removed");
        logger.info("Active Threads: " + clientThreads.size());
    }

    public int getGuestNumber() {
        return ++guestNumber;
    }
}
