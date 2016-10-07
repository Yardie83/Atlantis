package ch.atlantis.server;

import ch.atlantis.database.DatabaseHandler;
import ch.atlantis.game.Game;
import ch.atlantis.game.GameHandler;
import ch.atlantis.game.Player;
import ch.atlantis.util.Message;
import ch.atlantis.util.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.HashSet;

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
    private static HashSet<ObjectOutputStream> outputStreams = new HashSet<>();
    private DatabaseHandler databaseHandler;
    private GameHandler gameHandler;
    private boolean loggedIn;
    private String currentPlayerName;

    private long gameTime;

    ClientThread( Socket clientSocket, AtlantisServer server, DatabaseHandler databaseHandler, GameHandler
            gameHandler ) {

        this.clientSocket = clientSocket;
        this.server = server;
        this.databaseHandler = databaseHandler;
        this.gameHandler = gameHandler;
        loggedIn = false;
        running = true;
    }

    @Override
    public void run() {
        while ( running ) {
            try {
                this.inReader = new ObjectInputStream( clientSocket.getInputStream() );
                this.outputStream = new ObjectOutputStream( clientSocket.getOutputStream() );
                outputStreams.add( outputStream );
                System.out.println( "OutputStreams: " + outputStreams.size() );

                sendWelcomeMessage();
                sendPlayerName( null );
                sendGameList();
                sendLanguages();

                while ( running ) {
                    receiveMessage();
                }
            } catch ( IOException e ) {
                System.out.println( "Unable to create reader and/or writer" );
                return;
            } finally {
                System.out.println( "Thread " + currentThread().getName() + " terminated" );
            }
        }
    }

    private void sendWelcomeMessage() throws IOException {

        outputStream.writeObject( new Message( MessageType.CHAT, "*****************************************\n"
                + "Welcome to the Atlantis Game Server \n"
                + "*****************************************\n"
                + "Server IP Address: "
                + clientSocket.getLocalAddress()
                + "\nConnected to Server Port " + clientSocket.getLocalPort()
                + "\n*****************************************" ) );
    }

    /**
     * Sends a message to the same client from which a message has been received.
     * <p>
     * Hermann Grieder
     *
     * @param message Message Object to be sent
     * @throws IOException Throws IOException
     */
    private void sendMessage( Message message ) throws IOException {
        outputStream.writeObject( message );
        System.out.println( "Sending to User:     " + clientSocket.getRemoteSocketAddress() + " -> " + message
                .getMessageObject() );
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
    private void sendMessageToAllClients( MessageType messageType, String messageString ) {

        // If there are no users connected anymore, don't do anything

        if ( outputStreams.size() <= 0 ) {
            return;
        }

        Message message = new Message( messageType, messageString );

        // In order to ensure uniqueness of a ChatMessage we add the LocalDateTime to the message.
        // This way a user can enter the same chat message twice in a row. The counterpart can be found
        // in the client application in the GameLobbyController in the addListeners method.
        // Author: Hermann Grieder

        if ( messageType == MessageType.CHAT ) {
            messageString = LocalDateTime.now().toString() + " " + messageString;
            message = new Message( messageType, messageString );
        }
        for ( ObjectOutputStream outputStream : outputStreams ) {
            try {
                System.out.println( "Sending to User:     " + clientSocket.getRemoteSocketAddress() + " -> " +
                        messageString );
                outputStream.writeObject( message );
            } catch ( IOException e ) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sends the list of languages to the client
     * <p>
     * Loris Grether
     */
    private void sendLanguages() {
        try {
            sendMessage( new Message( MessageType.LANGUAGELIST, server.getLanguageListFromServer() ) );
        } catch ( IOException e ) {
            e.printStackTrace();
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
    private void sendPlayerName( String playerName ) {
        try {
            if ( !loggedIn ) {
                currentPlayerName = "Guest" + server.getGuestNumber();
                sendMessageToAllClients( MessageType.CHAT, currentPlayerName + " entered the chat" );
            } else {
                String oldUserName = currentPlayerName;
                currentPlayerName = playerName;
                sendMessageToAllClients( MessageType.CHAT, oldUserName + " is now known as: " + currentPlayerName );
            }

            sendMessage( new Message( MessageType.USERNAME, currentPlayerName ) );

        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    /**
     * Sends the gameList to the client
     * <p>
     * Hermann Grieder
     */
    private void sendGameList() {

        //databaseHandler.getGameList();

        if ( gameHandler.getGames().size() > 0 ) {
            for ( Game g : gameHandler.getGames() ) {
                String nameOfGame = g.getGameName();
                int numberOfPlayers = g.getNumberOfPlayers();
                int currentJoinedUsers = g.getPlayers().size();

                sendMessageToAllClients( MessageType.GAMELIST, nameOfGame + "," + numberOfPlayers + "," +
                        currentJoinedUsers );
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
            System.out.println( "Receiving from User: " + clientSocket.getRemoteSocketAddress() + " -> " + message
                    .getMessageObject() );

            switch (message.getMessageType()) {
                case LOGIN:
                    this.handleLogin( message );
                    break;
                case CREATEPROFILE:
                    this.handleCreateProfile( message );
                    break;
                case CHAT:
                    sendMessageToAllClients( MessageType.CHAT, message.getMessageObject().toString() );
                    break;
                case DISCONNECT:
                    this.handleDisconnectUser( currentPlayerName );
                    break;
                case NEWGAME:
                    this.handleNewGame( message );
                    break;
                case JOINGAME:
                    this.handleJoinGame( message );
                    break;
            }
        } catch ( IOException e ) {
            System.out.println( "User disconnected" );
            handleDisconnectUser(currentPlayerName);

        } catch ( ClassNotFoundException e ) {
            System.out.println( "Class \"Message\" not found" );
            e.printStackTrace();
        }
    }

    /**
     * Extracts the information from the message. Creates a new game and
     * adds it to the gameHandler. Informs all the clients about this
     * newly created game.
     * <p>
     * Hermann Grieder
     *
     * @param message Message received from the client
     */
    private void handleNewGame( Message message ) {

        // Extract the information from the message
        String[] gameInformation = splitMessage( message );
        String gameName = gameInformation[ 0 ];
        Integer nrOfPlayers = Integer.parseInt( gameInformation[ 1 ] );

        // Create and add a new game to the games ArrayList in
        // the GameHandler with the above game information
        gameHandler.addGame( new Game( gameName, nrOfPlayers, new Player( currentPlayerName ) ) );

        // Send the newly created game to all clients, so they can display it in their List
        if ( gameHandler.getGames() != null ) {
            sendGameList();
        }
    }

    /**
     * @param message Message received from the client
     */
    private void handleLogin( Message message ) {
        String[] credentials = splitMessage( message );
        String playerName = credentials[ 0 ];
        try {
            if ( databaseHandler.userLogin( message ) ) {

                //Start Timer
                gameTime = System.currentTimeMillis();

                loggedIn = true;
                sendMessage( new Message( MessageType.LOGIN, true ) );
                sendPlayerName( playerName );
            } else if ( !( databaseHandler.userLogin( message ) ) ) {
                sendMessage( new Message( MessageType.LOGIN, false ) );
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    /**
     * @param message Message received from the client
     */
    private void handleCreateProfile( Message message ) {
        String[] credentials = splitMessage( message );
        String playerName = credentials[ 0 ];
        try {
            if ( databaseHandler.createProfile( message ) ) {
                loggedIn = true;
                sendMessage( new Message( MessageType.CREATEPROFILE, true ) );
                sendPlayerName( playerName );
            } else {
                sendMessage( new Message( MessageType.CREATEPROFILE, false ) );
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }

    private void handleJoinGame( Message message ) {
        String gameName = message.getMessageObject().toString();
        if (gameHandler.addPlayer(gameName, currentPlayerName )){
            try {
                sendMessage( new Message( MessageType.JOINGAME, true ) );
                sendGameList();
            } catch ( IOException e ) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Splits a message at the "," sign.
     *
     * @param message Message received from the client
     * @return String[]
     */
    private String[] splitMessage( Message message ) {
        return message.getMessageObject().toString().split( "," );
    }

    /**
     * @throws IOException
     * @param currentPlayerName Current name of the player
     */
    private void handleDisconnectUser( String currentPlayerName ) throws IOException {
        server.removeThread( currentThread().getId() );

        //End Timer
        gameTime = System.currentTimeMillis() - gameTime;

        databaseHandler.enterGameTime( gameTime, this.currentPlayerName );

        outputStreams.remove( this.outputStream );
        running = false;
        this.interrupt();
        inReader.close();
        outputStream.close();
        clientSocket.close();
        sendMessageToAllClients( MessageType.CHAT, "User " + currentPlayerName + " left the chat" );
    }
}