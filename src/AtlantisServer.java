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

            printUserCount();

            try {

                clientSocket = serverSocket.accept();
                System.out.println("Connection accepted: " + clientSocket.getInetAddress().getCanonicalHostName());

                ClientThread chatServerThread = new ClientThread(clientSocket, ++clientNumber, server);
                clientThreads.add(chatServerThread);
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


    public void removeThread(long threadID) {

    }

}



