import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashSet;

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


    ClientThread(Socket clientSocket, AtlantisServer server, DatabaseHandler databaseHandler) {

        super();

        this.clientSocket = clientSocket;
        this.server = server;
        this.databaseHandler = databaseHandler;

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
                sendGuestNumber();
                sendGameList();

                while (running) {
                    receiveMessage();
                }

            } catch (IOException e) {
                System.out.println("Unable to create reader and/or writer");
                return;
            } finally {
                System.out.println("Thread " + currentThread().getName() + " ended");
            }
        }
    }

    private void sendGuestNumber() {
        try {
            sendMessage(new Message(MessageType.USERNAME, server.getGuestNumber()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendGameList() {

        //databaseHandler.getGameList();

        try {
            sendMessage(new Message(MessageType.GAMELIST, "send game List Object Here"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveMessage() throws IOException {
        try {
            Message message = (Message) inReader.readObject();
            System.out.println("Receiving from User: " + clientSocket.getRemoteSocketAddress() + " -> " + message.getMessageObject());

            switch (message.getMessageType()) {

                case DISCONNECT:
                    disconnectUser();
                    break;

                case CHAT:
                    handleChatMessage(message);
                    break;

                case CREATEPROFILE:
                    this.handleCreateProfile(message);
                    break;

                case LOGIN:
                    this.handleLogin(message);
                    break;

                case NEWGAME:
                    databaseHandler.newGame(message);
                    break;
            }
        } catch (IOException e) {
            System.out.println("Unable to receive message");
            disconnectUser();

        } catch (ClassNotFoundException e) {
            System.out.println("Class \"Message\" not found");
        }
    }

    private void handleLogin(Message message) {
        try {
            if (databaseHandler.userLogin(message)) {

                sendMessage(new Message(MessageType.LOGIN, true));

            } else {
                sendMessage(new Message(MessageType.LOGIN, false));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleCreateProfile(Message message) {
        try {
            if (databaseHandler.createProfile(message)) {

                sendMessage(new Message(MessageType.CREATEPROFILE, true));

            } else {
                sendMessage(new Message(MessageType.CREATEPROFILE, false));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleChatMessage(Message message) throws IOException {
        if (message.getMessageType() == MessageType.CHAT && message.getMessageObject().equals("QUIT")) {
            disconnectUser();
        } else if (message.getMessageType() == MessageType.CHAT && message.getMessageObject().toString().equalsIgnoreCase("HELP")) {
            sendHelpMessage();
        } else {
            sendMessageToAllClients(message);
        }
    }

    private void disconnectUser() throws IOException {

        server.removeThread(currentThread().getId());

        sendMessageToAllClients(new Message(MessageType.CHAT, "User " + clientSocket.getInetAddress().getCanonicalHostName() + " left the chat"));

        outputStreams.remove(this.outputStream);
        running = false;
        this.interrupt();
        inReader.close();
        outputStream.close();
        clientSocket.close();
    }

    private void sendWelcomeMessage() throws IOException {

        outputStream.writeObject(new Message(MessageType.CHAT, "*****************************************\n"
                + "Welcome to the Atlantis Game Server \n"
                + "*****************************************\n"
                + "Server IP Address: "
                + clientSocket.getLocalAddress()
                + "\nConnected to Server Port " + clientSocket.getLocalPort()
                + "\n*****************************************\n"
                + "\nFor help type \"HELP\" (....needs to be implemented)"));
    }

    public void sendMessage(Message message) throws IOException {
        outputStream.writeObject(message);
    }

    private void sendMessageToAllClients(Message message) {
        if (outputStreams.size() != 0) {
            for (ObjectOutputStream outputStream : outputStreams) {
                try {
                    System.out.println("Sending to User: " + clientSocket.getRemoteSocketAddress() + " -> " + message.getMessageObject());
                    outputStream.writeObject(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendHelpMessage() throws IOException {
        outputStream.writeObject(new Message(MessageType.CHAT, "I already said, it needs to be implemented!"));
    }
}
