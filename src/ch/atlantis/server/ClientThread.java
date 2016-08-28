package ch.atlantis.server;

import ch.atlantis.database.DatabaseHandler;
import ch.atlantis.game.GameHandler;
import ch.atlantis.util.Message;
import ch.atlantis.util.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by Hermann Grieder on 16.07.2016.
 */
class ClientThread extends Thread {

    private Socket clientSocket;
    private AtlantisServer server;
    private ObjectInputStream inReader;
    private ObjectOutputStream outputStream;
    private boolean running = true;
    private static HashSet<ObjectOutputStream> outputStreams = new HashSet<>();
    private DatabaseHandler databaseHandler;
    private GameHandler gameHandler;
    private boolean loggedIn;
    private String newUserName;
    private String currentUserName;
    private String oldUserName;


    ClientThread(Socket clientSocket, AtlantisServer server, DatabaseHandler databaseHandler, GameHandler gameHandler) {

        super();

        this.clientSocket = clientSocket;
        this.server = server;
        this.databaseHandler = databaseHandler;
        this.gameHandler = gameHandler;
        loggedIn = false;
    }

    @Override
    public void run() {
        while (running) {
            try {
                this.inReader = new ObjectInputStream(clientSocket.getInputStream());
                this.outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                outputStreams.add(outputStream);
                System.out.println("OutputStreams: " + outputStreams.size());

                sendWelcomeMessage();
                sendUserName(null);
                sendGameList();
                sendLanguages();

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

    public void sendMessage(Message message) throws IOException {
        outputStream.writeObject(message);
        System.out.println("Sending to User:     " + clientSocket.getRemoteSocketAddress() + " -> " + message.getMessageObject());
    }

    private void sendMessageToAllClients(Message message) {
        if (outputStreams.size() > 0) {
            for (ObjectOutputStream outputStream : outputStreams) {
                try {
                    System.out.println("Sending to User:     " + clientSocket.getRemoteSocketAddress() + " -> " + message.getMessageObject());
                    outputStream.writeObject(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendLanguages() {
        try {
            sendMessage(new Message(MessageType.LANGUAGELIST, server.getLanguageListFromServer()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendUserName(String name) {
        newUserName = name;
        try {
            if (!loggedIn) {
                newUserName = "Guest" + server.getGuestNumber();
                currentUserName = newUserName;
                sendMessage(new Message(MessageType.USERNAME, currentUserName));
            } else if (loggedIn){
                oldUserName = currentUserName;
                currentUserName = newUserName;
                sendMessage(new Message(MessageType.USERNAME, newUserName ));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (loggedIn){
                sendMessageToAllClients(new Message(MessageType.CHAT, oldUserName + " is now known as: " +  newUserName));
            }else{
                sendMessageToAllClients(new Message(MessageType.CHAT, "User " + currentUserName + " entered the chat"));
            }
        }
    }

    private void sendGameList() {

        //databaseHandler.getGameList();

        try {
            // send the ArrayList gameList from the gameHandler class to the client
            for (Map.Entry<String, Integer> entry : gameHandler.getGameList().entrySet())
                sendMessage(new Message(MessageType.GAMELIST, entry.getKey() + "," + entry.getValue()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveMessage() throws IOException {
        try {
            Message message = (Message) inReader.readObject();
            System.out.println("Receiving from User: " + clientSocket.getRemoteSocketAddress() + " -> " + message.getMessageObject());

            switch (message.getMessageType()) {
                case LOGIN:
                    this.handleLogin(message);
                    break;
                case CREATEPROFILE:
                    this.handleCreateProfile(message);
                    break;
                case CHAT:
                    this.handleChatMessage(message);
                    break;
                case DISCONNECT:
                    this.handleDisconnectUser();
                    break;
                case NEWGAME:
                    this.handleNewGame(message);
                    break;
            }
        } catch (IOException e) {
            System.out.println("Unable to receive message");
            handleDisconnectUser();

        } catch (ClassNotFoundException e) {
            System.out.println("Class \"Message\" not found");
            e.printStackTrace();
        }
    }

    private void handleNewGame(Message message) {
        String[] gameInformation = splitMessage(message);
        String gameName = gameInformation[0];
        Integer nrOfPlayers = Integer.parseInt(gameInformation[1]);
        gameHandler.addGame(gameName, nrOfPlayers);
        for (Map.Entry<String, Integer> entry : gameHandler.getGameList().entrySet()) {
            sendMessageToAllClients(new Message(MessageType.GAMELIST, entry.getKey() + "," + entry.getValue()));
        }
    }

    private void handleLogin(Message message) {
        String[] credentials = splitMessage(message);
        String userName = credentials[0];
        try {
            if (databaseHandler.userLogin(message)) {
                loggedIn = true;
                sendMessage(new Message(MessageType.LOGIN, true));
                sendUserName(userName);
            } else if (!(databaseHandler.userLogin(message))) {
                sendMessage(new Message(MessageType.LOGIN, false));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleCreateProfile(Message message) {
        String[] credentials = splitMessage(message);
        String userName = credentials[0];
        try {
            if (databaseHandler.createProfile(message)) {
                loggedIn = true;
                sendMessage(new Message(MessageType.CREATEPROFILE, true));
                sendUserName(userName);
            } else {
                sendMessage(new Message(MessageType.CREATEPROFILE, false));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String[] splitMessage(Message message) {
        return message.getMessageObject().toString().split(",");
    }

    private void handleChatMessage(Message message) throws IOException {
        if (message.getMessageType() == MessageType.CHAT && message.getMessageObject().equals("QUIT")) {
            handleDisconnectUser();
        } else {
            sendMessageToAllClients(message);
        }
    }

    private void handleDisconnectUser() throws IOException {
        server.removeThread(currentThread().getId());
        outputStreams.remove(this.outputStream);
        running = false;
        this.interrupt();
        inReader.close();
        outputStream.close();
        clientSocket.close();
        sendMessageToAllClients(new Message(MessageType.CHAT, "User "
                + clientSocket.getInetAddress().getCanonicalHostName()
                + " left the chat"));
    }
}
