package ch.atlantis.server;

import ch.atlantis.database.DatabaseHandler;
import ch.atlantis.game.*;
import ch.atlantis.util.Message;
import ch.atlantis.util.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Hermann Grieder on 16.07.2016.
 * <p>
 * Individual Thread for each client that connects to the server.
 * Receives and sends messages to the clients. Handles login, new user profiles, chat messages
 * and new games that have been created.
 */
class ClientThread extends Thread {

    private Socket clientSocket;
    private AtlantisServer server;
    private ObjectInputStream inReader;
    private ObjectOutputStream outputStream;
    private boolean running;
    private static HashMap<Player, Socket> playerSockets = new HashMap<>();
    private static Map<Socket, ObjectOutputStream> outputStreams = new HashMap<>();
    private DatabaseHandler databaseHandler;
    private GameHandler gameHandler;
    private boolean loggedIn;
    private String currentPlayerName;
    private Player player;
    private GameController gameController;
    private Card card;
    private GamePiece gamePiece;

    private long gameTime;

    ClientThread(Socket clientSocket, AtlantisServer server, DatabaseHandler databaseHandler, GameHandler
            gameHandler) {

        this.clientSocket = clientSocket;
        this.server = server;
        this.databaseHandler = databaseHandler;
        this.gameHandler = gameHandler;
        loggedIn = false;
        running = true;
    }

    @Override
    public void run() {
        while (running) {
            try {
                this.inReader = new ObjectInputStream(clientSocket.getInputStream());
                this.outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                outputStreams.put(clientSocket, outputStream);
                System.out.println("OutputStreams: " + outputStreams.size());

                sendWelcomeMessage();
                sendPlayerName(null);
                sendGameList();

                while (running) {
                    receiveMessage();
                }
            } catch (IOException e) {
                System.out.println("Unable to create reader and/or writer");
                return;
            } finally {
                System.out.println("Thread " + currentThread().getName() + " terminated");
            }
        }
    }

    private void sendWelcomeMessage() throws IOException {

        outputStream.writeObject(new Message(MessageType.CHAT, "*****************************************\n"
                + "Welcome to the Atlantis Game Server \n"
                + "*****************************************\n"
                + "Server IP Address: "
                + clientSocket.getLocalAddress()
                + "\nConnected to Server Port " + clientSocket.getLocalPort()
                + "\n*****************************************"));
    }

    /**
     * Sends a message to the same client from which a message has been received.
     * <p>
     * Hermann Grieder
     *
     * @param message Message Object to be sent
     * @throws IOException Throws IOException
     */
    public void sendMessage(Message message) throws IOException {
        outputStream.writeObject(message);
        System.out.println("Sending to User:     " + clientSocket.getRemoteSocketAddress() + " -> " + message
                .getMessageObject());
    }

    /**
     * Sends a message to all clients currently connected. If the messageType is Chat, the
     * current DateTime is added to ensure uniqueness of the message to be sent.
     * <p>
     * Hermann Grieder
     *
     * @param messageType   Type of message
     * @param messageString The actual message to be sent
     */
    public void sendMessageToAllClients(MessageType messageType, String messageString) {

        // If there are no users connected anymore, don't do anything

        if (outputStreams.size() <= 0) {
            return;
        }

        Message message = new Message(messageType, messageString);

        // In order to ensure uniqueness of a ChatMessage we add the LocalDateTime to the message.
        // This way a user can enter the same chat message twice in a row. The counterpart can be found
        // in the client application in the GameLobbyController in the addListeners method.
        // Author: Hermann Grieder

        if (messageType == MessageType.CHAT) {
            messageString = LocalDateTime.now().toString() + " " + messageString;
            message = new Message(messageType, messageString);
        }

        for (HashMap.Entry<Socket, ObjectOutputStream> entry : outputStreams.entrySet()) {
            Socket clientSocket = entry.getKey();
            ObjectOutputStream outputStream = entry.getValue();
            {
                try {
                    System.out.println("Sending to User:     " + clientSocket.getRemoteSocketAddress() + " -> " +
                            messageString);
                    outputStream.writeObject(message);
                } catch (IOException e) {
                    System.out.println("Could not write to " + clientSocket.getRemoteSocketAddress());
                }
            }
        }
    }

    /**
     * Assigns a guestName to the client upon connection and informs other clients that a new client
     * joined the chat. When the client logs in or creates a profile the method informs all the
     * currently connected clients about the name change.
     * <p>
     * Hermann Grieder
     *
     * @param playerName The new username of the client
     */
    private void sendPlayerName(String playerName) {
        try {
            if (!loggedIn) {
                currentPlayerName = "Guest" + server.getGuestNumber();
                sendMessageToAllClients(MessageType.CHAT, currentPlayerName + " entered the chat");
            } else {
                String oldUserName = currentPlayerName;
                currentPlayerName = playerName;
                sendMessageToAllClients(MessageType.CHAT, oldUserName + " is now known as: " + currentPlayerName);
            }

            sendMessage(new Message(MessageType.USERNAME, currentPlayerName));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends the gameList to the client
     * <p>
     * Hermann Grieder
     */
    private void sendGameList() {
        // Check for empty games and remove them from the list
        if (gameHandler.getGames() != null) {
            String gameToRemove = null;
            for (HashMap.Entry<String, Game> entry : gameHandler.getGames().entrySet()) {
                Game g = entry.getValue();
                if (g.getPlayers().size() == 0) {
                    System.out.println("Players in Game: " + g.getGameName() + " : " + g.getPlayers().size());
                    gameToRemove = g.getGameName();
                }
            }
            if (gameToRemove != null) {
                gameHandler.getGames().remove(gameToRemove);
            }
        }
        // Todo: (loris) databaseHandler getGameList....need to talk about what to do with this.
        //databaseHandler.getGameList();

        // Send each game from the Games array in the gameHandler class to all the clients
        if (gameHandler.getGames().size() > 0) {
            for (HashMap.Entry<String, Game> entry : gameHandler.getGames().entrySet()) {
                Game g = entry.getValue();
                String nameOfGame = g.getGameName();
                int numberOfPlayers = g.getNumberOfPlayers();
                int currentJoinedUsers = g.getPlayers().size();

                sendMessageToAllClients(MessageType.GAMELIST, nameOfGame + "," + numberOfPlayers + "," +
                        currentJoinedUsers);
            }
        }
    }

    /**
     * Receives messages from the clients and handles them according to their MessageType.
     * <p>
     * Hermann Grieder
     *
     * @throws IOException Throws IOException if it can not read from the client.
     */
    private void receiveMessage() throws IOException {
        try {
            Message message = (Message) inReader.readObject();
            System.out.println("Receiving from User: " + clientSocket.getRemoteSocketAddress() + " -> " + message
                    .getMessageObject());

            switch (message.getMessageType()) {
                case LOGIN:
                    this.handleLogin(message);
                    break;
                case CREATEPROFILE:
                    this.handleCreateProfile(message);
                    break;
                case CHAT:
                    sendMessageToAllClients(MessageType.CHAT, message.getMessageObject().toString());
                    break;
                case DISCONNECT:
                    this.handleDisconnectUser(currentPlayerName);
                    break;
                case NEWGAME:
                    this.handleNewGame(message);
                    break;
                case JOINGAME:
                    this.handleJoinGame(message);
                    break;
                case STARTGAME:
                    this.initGame(this.player);
                    break;
                case MOVE:
                    this.handleMove(message);
                    break;
            }
        } catch (IOException e) {
            System.out.println("User disconnected");
            e.printStackTrace();
            handleDisconnectUser(currentPlayerName);

        } catch (ClassNotFoundException e) {
            System.out.println("Class \"Message\" not found");
            e.printStackTrace();
        }
    }

    private void initGame(Player hostPlayer) throws IOException {
        HashMap<String, ArrayList> initGame = gameHandler.initGame(hostPlayer);
        Game game = gameHandler.getGames().get(hostPlayer.getGameName());
        for (Player player : game.getPlayers()) {
            Socket socket = playerSockets.get(player);
            outputStreams.get(socket).writeObject(new Message(MessageType.GAMEINIT, initGame));
        }
    }

    private void handleNewGame(Message message) throws IOException {
        if (gameHandler.handleNewGame(message, currentPlayerName)) {
            handleJoinGame(message);
        }
    }

    private void handleJoinGame(Message message) throws IOException {
        // Extract the information from the message
        String[] gameInformation = message.getMessageObject().toString().split(",");
        String gameName = gameInformation[0];

        Player player = gameHandler.addPlayer(gameName, currentPlayerName);
        if (player != null) {
            this.player = player;
            playerSockets.put(player, clientSocket);
            sendMessage(new Message(MessageType.JOINGAME, player.getPlayerID() + "," + gameName));
        }
        sendGameList();

        for (HashMap.Entry<String, Game> entry : gameHandler.getGames().entrySet()) {
            Game g = entry.getValue();
            Boolean gameIsReady = gameHandler.isGameFull(g.getGameName());
            sendMessageToAllClients(MessageType.GAMEREADY, g.getGameName() + "," + gameIsReady.toString());
        }
    }

    /**
     * Handles the Move message by calling the checkMove method in the game..........(model / controller?)
     * Informs the players about the new state of the game. If the game is not over, the information about
     * the move of the player and who the next player is will be shared with all the players. If the game is over
     * the game over message will be sent.
     *
     * Fabian Witschi
     * @param message
     * @throws IOException
     */
    private void handleMove(Message message) throws IOException {

        if (message.getMessageObject() instanceof HashMap) {

        }
    }

    /**
     * @param message Message received from the client
     */
    private void handleLogin(Message message) {
        String[] credentials = splitMessage(message);
        String playerName = credentials[0];
        try {
            if (databaseHandler.userLogin(message)) {

                //Start Timer
                gameTime = System.currentTimeMillis();

                loggedIn = true;
                sendMessage(new Message(MessageType.LOGIN, true));
                sendPlayerName(playerName);
            } else if (!(databaseHandler.userLogin(message))) {
                sendMessage(new Message(MessageType.LOGIN, false));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param message Message received from the client
     */
    private void handleCreateProfile(Message message) {
        String[] credentials = splitMessage(message);
        String playerName = credentials[0];
        try {
            if (databaseHandler.createProfile(message)) {
                loggedIn = true;
                sendMessage(new Message(MessageType.CREATEPROFILE, true));
                sendPlayerName(playerName);
            } else {
                sendMessage(new Message(MessageType.CREATEPROFILE, false));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Splits a message at the "," sign.
     *
     * @param message Message received from the client
     * @return String[]
     */
    private String[] splitMessage(Message message) {
        return message.getMessageObject().toString().split(",");
    }

    /**
     * @param currentPlayerName Current name of the player
     * @throws IOException If if was not possible to close all resources
     */
    private void handleDisconnectUser(String currentPlayerName) throws IOException {
        server.removeThread(currentThread().getId());

        //End Timer
        gameTime = System.currentTimeMillis() - gameTime;

        databaseHandler.enterGameTime(gameTime, this.currentPlayerName);

        outputStreams.remove(this.outputStream);
        running = false;
        this.interrupt();
        inReader.close();
        outputStream.close();
        clientSocket.close();
        sendMessageToAllClients(MessageType.CHAT, "User " + currentPlayerName + " left the chat");
    }
}
