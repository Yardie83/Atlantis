import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Hermann Grieder on 16.07.2016.
 */

public class AtlantisServer {

    private static final int PORT = 9000;
    private static Socket clientSocket;
    private static int clientNumber;
    private static ArrayList<ClientThread> clientThreads = new ArrayList<>();

    public static void main(String[] args) throws IOException {

        AtlantisServer server = new AtlantisServer();

        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server is running");
        System.out.println("Listening on port: " + PORT);


        while (true) {
            try {
                System.out.println(clientThreads.size() + " user connected");
                clientSocket = serverSocket.accept();
                System.out.println("Connection accepted: " + clientSocket.getInetAddress().getCanonicalHostName());
                ClientThread chatServerThread = new ClientThread(clientSocket, ++clientNumber, server);
                clientThreads.add(chatServerThread);
                chatServerThread.start();
            } catch (IOException e) {
                System.out.println("User: " + clientSocket.getInetAddress() + " disconnected");
            } finally {
            }
        }
    }

    public void removeThread(int clientNumber) {
        ClientThread chatServerThreadToRemove;
        boolean found = false;
        while (!found) {
            for (ClientThread thread : clientThreads) {
                if (thread.getClientNumber() == clientNumber-1) {
                    chatServerThreadToRemove = thread;
                    clientThreads.remove(chatServerThreadToRemove.getClientNumber() - 1);

                    for (ClientThread chatServerThread : clientThreads) {
                        chatServerThread.sendMessage(String.valueOf("Client #: " + clientNumber + " left"));
                    }
                    found = true;
                }
            }
        }
        System.out.println("Client #: " + clientNumber + "disconnected from the server");
        if (clientThreads.size() < 2) {
            System.out.println(clientThreads.size() + " client connected");
        } else {
            System.out.println(clientThreads.size() + " clients connected");
        }
    }

    public ArrayList<ClientThread> getClientThreads() {
        return clientThreads;
    }
}



