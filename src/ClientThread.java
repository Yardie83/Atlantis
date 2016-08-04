import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashSet;

/**
 * Created by Hermann Grieder on 16.07.2016.
 *
 */
class ClientThread extends Thread {

    private Socket clientSocket;
    private int clientNumber;
    private AtlantisServer server;
    private ObjectInputStream inReader;
    private ObjectOutputStream outputStream;
    private boolean running = true;
    private static HashSet<ObjectOutputStream> outputStreams = new HashSet<>();


    ClientThread(Socket clientSocket, int clientNumber, AtlantisServer server) {

        super();

        this.clientSocket = clientSocket;
        this.clientNumber = clientNumber;
        this.server = server;

    }

    @Override
    public void run() {
        while (true) {
            try {
                this.inReader = new ObjectInputStream(clientSocket.getInputStream());
                this.outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
                outputStreams.add(outputStream);
                System.out.println("OutputStreams: " + outputStreams.size());

                sendWelcomeMessage();

                while (running) {
                    receiveMessage();
                }

            } catch (IOException e) {
                System.out.println("Unable to create reader and/or writer. Client connection status: " + clientSocket.isConnected());
                return;
            } finally {
                System.out.println("Thread " + currentThread().getName() + " ended");
            }
        }
    }

    private void receiveMessage() throws IOException {
        try {
            Message message = (Message) inReader.readObject();
            System.out.println("User " + clientNumber + "-> " + message.getMessage());

            if (message.getMessageType() == MessageType.DISCONNECT) {
                disconnectUser();
            } else if (message.getMessageType() == MessageType.CHAT) {
                handleChatMessage(message);
            } else if (message.getMessageType() == MessageType.CHAT) {
                getGameList();
            }
        } catch (IOException e) {
            System.out.println("Unable to receive message");
            disconnectUser();

        } catch (ClassNotFoundException e) {
            System.out.println("Class \"Message\" not found");
        }
    }


    private void handleChatMessage(Message message) throws IOException {
        if (message.getMessageType() == MessageType.CHAT && message.getMessage().equals("QUIT")) {
            disconnectUser();
        } else if (message.getMessageType() == MessageType.CHAT && message.getMessage().equals("HELP")) {
            sendHelpMessage();
        } else {
            sendMessageToAllClients(message);
        }

    }

    private void getGameList() {
    }

    private void disconnectUser() throws IOException {

        server.removeThread(currentThread().getId());

        //sendMessageToAllClients(new Message(MessageType.CHAT, "User " + clientSocket.getInetAddress().getCanonicalHostName() + " left"));

        outputStreams.remove(this.outputStream);
        this.interrupt();
        inReader.close();
        outputStream.close();
        clientSocket.close();

        running = false;
    }

    private void sendWelcomeMessage() throws IOException {

        outputStream.writeObject(new Message(MessageType.CHAT, "*****************************************\n"
                + "Welcome to the Atlantis Game Server \n"
                + "*****************************************\n"
                + "Server IP Address: "
                + clientSocket.getLocalAddress()
                + "\nConnected to Server Port " + clientSocket.getLocalPort()
                + "\n*****************************************\n"
                + "\n For help type \"HELP\" (....needs to be implemented)"));
    }

    public void sendMessage(Message message) throws IOException {
        outputStream.writeObject(message);
    }

    private void sendMessageToAllClients(Message message) {
        for (ObjectOutputStream outputStream : outputStreams) {
            try {
                System.out.println("Sending to User: " + clientSocket.getRemoteSocketAddress() + " -> " + message.getMessage());
                outputStream.writeObject(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendHelpMessage() throws IOException {
        outputStream.writeObject(new Message(MessageType.CHAT, "I already said, it needs to be implemented!"));
    }
}
