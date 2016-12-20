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
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static ch.atlantis.server.AtlantisServer.AtlantisLogger;

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
    private GameManager gameManager;
    private boolean loggedIn;
    private String currentPlayerName;
    private Player player;
    private long gameTime;

    private Logger logger;

    ClientThread(Socket clientSocket, AtlantisServer server, DatabaseHandler databaseHandler, GameManager
            gameManager) {

        logger = Logger.getLogger(AtlantisServer.AtlantisLogger);

        this.clientSocket = clientSocket;
        this.server = server;
        this.databaseHandler = databaseHandler;
        this.gameManager = gameManager;
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
                logger.info("OutputStreams: " + outputStreams.size());

                sendWelcomeMessage();
                sendPlayerName(null);
                sendGameList();

                while (running) {
                    receiveMessage();
                }
            } catch (IOException e) {
                logger.info("Unable to create reader and/or writer");
                return;
            } finally {
                logger.info("Thread " + currentThread().getName() + " terminated");
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
        logger.info("Sending to User:     " + clientSocket.getRemoteSocketAddress() + " -> " + message
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
                    logger.info("Sending to User:     " + clientSocket.getRemoteSocketAddress() + " -> " +
                            messageString);
                    outputStream.writeObject(message);
                } catch (IOException e) {
                    logger.info("Could not write to " + clientSocket.getRemoteSocketAddress());
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
        gameManager.updateGameList();
        // Send each game from the Games list in the gameManager to all the clients
        for (HashMap.Entry<String, Game> entry : gameManager.getGames().entrySet()) {
            Game g = entry.getValue();
            String nameOfGame = g.getGameName();
            int numberOfPlayers = g.getNumberOfPlayers();
            int currentJoinedUsers = g.getPlayers().size();

            sendMessageToAllClients(MessageType.GAMELIST, nameOfGame + "," + numberOfPlayers + "," +
                    currentJoinedUsers);
            // + "," + currentPlayerName);
            logger.info("ClientThread -> GameList sent");
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
            logger.info("Receiving from User: " + clientSocket.getRemoteSocketAddress() + " -> " + message
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
                    this.initGame();
                    break;
                case MOVE:
                    this.handleMove(message);
                    break;
                case BUYCARD:
                    this.handleBuyCards(message);
                    break;
                case CANTMOVE:
                    this.handleCantMove(message);
                    break;
                case NEWTURN:
                    break;
            }
        } catch (IOException e) {
            logger.info("User disconnected.");
            handleDisconnectUser(currentPlayerName);

        } catch (ClassNotFoundException e) {
            logger.info("Class \"Message\" not found.");
        }
    }

    /**
     * Fabian Witschi
     *
     * @param message
     */

    private void handleCantMove(Message message) throws IOException {
        String gameName = (String) message.getMessageObject();
        Game game = gameManager.getGames().get(gameName);
        ArrayList<Card> twoCardsForNotMoving = game.getGameController().handleCantMove();
        sendMessage(new Message(MessageType.CANTMOVE, twoCardsForNotMoving));
        Integer currentTurnToSend = new Integer(game.getGameController().handleNewTurn());
        Player currentPlayer = this.player;
        sendMessageToAllPlayers(currentPlayer, new Message(MessageType.NEWTURN, currentTurnToSend));
    }

    /**
     * Can Heval Cokyasar
     *
     * @param message
     * @throws IOException
     */

    private void handleBuyCards(Message message) throws IOException {
        if (message.getMessageObject() instanceof HashMap) {
            HashMap<String, Object> hashToBuyCards = (HashMap<String, Object>) message.getMessageObject(); // Downcast to HashMap -> (...)
            Game game = gameManager.findGame(hashToBuyCards); // Find specific game
            int indexOfCard = (Integer) hashToBuyCards.get("Index");
            ArrayList<Card> arrayListOfPurchasedCards = game.getGameController().handleUserCardPurchase(indexOfCard); // Return an AL with purchased cards
            sendMessage(new Message(MessageType.BUYCARD, arrayListOfPurchasedCards)); // Send message back to client
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
                startGameTimer();
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
                startGameTimer();
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

    private void startGameTimer() {
        //Start Timer
        gameTime = System.currentTimeMillis();
        System.out.println("Start Game Time: " + gameTime);
    }

    private void handleNewGame(Message message) throws IOException {
        if (gameManager.handleNewGame(message)) {
            handleJoinGame(message);
        }
    }

    private void handleJoinGame(Message message) throws IOException {
        // Extract the information from the message
        String[] gameInformation = message.getMessageObject().toString().split(",");
        String gameName = gameInformation[0];

        Player player = gameManager.addPlayer(gameName, currentPlayerName);
        if (player != null) {
            this.player = player;
            playerSockets.put(player, clientSocket);
            sendMessage(new Message(MessageType.JOINGAME, player.getPlayerID() + "," + gameName));
        }
        sendGameList();

        for (HashMap.Entry<String, Game> entry : gameManager.getGames().entrySet()) {
            Game g = entry.getValue();
            Boolean gameIsReady = gameManager.isGameFull(g.getGameName());
            sendMessageToAllClients(MessageType.GAMEREADY, g.getGameName() + "," + gameIsReady.toString());
        }
    }

    /**
     * After receiving the game start message, some final initialization steps need to be taken
     * in order to produce all the needed information to play the game. After initialization it send
     * the finished game state map to all the players in that game.
     *
     * @throws IOException If the message could not be sent
     */
    private void initGame() throws IOException {
        Player currentPlayer = this.player;
        HashMap<String, Object> initialGameStateMap = gameManager.initGame(currentPlayer);
        Message message = new Message(MessageType.GAMEINIT, initialGameStateMap);
        sendMessageToAllPlayers(currentPlayer, message);
    }

    /**
     * Handles the Move message by calling the handleMove method in the gameManager.
     * Informs the players about the new state of the game. Checks if the game is over and
     * informs every player after each turn if it is or not.
     * <p>
     * Hermann Grieder
     *
     * @param message The incoming message
     * @throws IOException If the message could not be sent
     */
    @SuppressWarnings("unchecked")
    private void handleMove(Message message) throws IOException {
        if (message.getMessageObject() instanceof HashMap) {
            HashMap<String, Object> incomingGameState = (HashMap<String, Object>) message.getMessageObject();
            Player currentPlayer = this.player;
            Game game = gameManager.findGame(incomingGameState);
            // Check if the move is valid
            if (gameManager.handleMove(game, incomingGameState)) {
                // The move is valid, now inform all the players of the changes.
                HashMap<String, Object> newGameState = gameManager.writeGameState(game);
                sendMessageToAllPlayers(currentPlayer, new Message(MessageType.MOVE, newGameState));
            }
            // Check if the game is over and inform all the players if it is or not
            boolean isGameOver = gameManager.isGameOver(game);

            sendMessageToAllPlayers(currentPlayer, new Message(MessageType.GAMEOVER, isGameOver));
            if (isGameOver) {
                gameManager.removeGame(game);
                sendGameList();
            }
        }
    }

    /**
     * Sends a message to all players.
     *
     * @param currentPlayer To find the game and its player to send the message to
     * @param message       Message to be sent
     * @throws IOException If the message could not be sent
     */
    private void sendMessageToAllPlayers(Player currentPlayer, Message message) throws IOException {
        Game game = gameManager.getGames().get(currentPlayer.getGameName());
        for (Player player : game.getPlayers()) {
            Socket socket = playerSockets.get(player);
            outputStreams.get(socket).writeObject(message);
            outputStreams.get(socket).flush();
        }
    }


    /**
     * Splits a message at the delimiter sign into individual strings and returns them in an string array.
     *
     * @param message Message received from the client
     * @return String[] with the individual split strings
     */
    private String[] splitMessage(Message message) {
        return message.getMessageObject().toString().split(",");
    }

    /**
     * @param currentPlayerName Current name of the player
     * @throws IOException If if was not possible to close all resources
     */
    private void handleDisconnectUser(String currentPlayerName) throws IOException {
        gameManager.removePlayer(currentPlayerName);
        sendGameList();
        server.removeThread(currentThread().getId());

        //End Timer
        gameTime = System.currentTimeMillis() - gameTime;

        long time = TimeUnit.MILLISECONDS.toMinutes(gameTime);

        databaseHandler.enterGameTime(TimeUnit.MILLISECONDS.toMinutes(gameTime), this.currentPlayerName);

        outputStreams.remove(clientSocket);
        running = false;
        this.interrupt();
        inReader.close();
        outputStream.close();
        clientSocket.close();
        sendMessageToAllClients(MessageType.CHAT, "User " + currentPlayerName + " left the chat");
    }
}
