import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;

/**
 * Created by Hermann Grieder on 16.07.2016.
 */
public class ClientThread extends Thread {

    private Socket clientSocket;
    private int clientNumber;
    private AtlantisServer server;
    private BufferedReader inReader;
    private PrintWriter outWriter;
    private boolean running = true;
    private static HashSet<PrintWriter> printWriters = new HashSet<>();

    public ClientThread(Socket clientSocket, int clientNumber, AtlantisServer server) {

        this.clientSocket = clientSocket;
        this.clientNumber = clientNumber;
        this.server = server;
    }

    @Override
    public void run() {
        while (running) {
            try {
                this.inReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                this.outWriter = new PrintWriter(clientSocket.getOutputStream(), true);
                printWriters.add(outWriter);

                System.out.println("PrintWriters: " + printWriters.size());

                sendWelcomeMessage();

                String fromClientMessage, toClientMessage;
                while (true) {
                    fromClientMessage = inReader.readLine();
                    System.out.println("User " + clientNumber + ": " + fromClientMessage);
                    toClientMessage = fromClientMessage;
                    for (PrintWriter outWriter : printWriters) {
                        outWriter.println(toClientMessage);
                        System.out.println("Message sent: " + toClientMessage + " to User" + clientSocket.getRemoteSocketAddress());
                    }
                    if (fromClientMessage.equals("QUIT")) {
                        sendGoodBye();
                        printWriters.remove(this.outWriter);
                        System.out.println(printWriters.size());
                        server.removeThread(clientNumber);
                        running = false;
                    }
                }
            } catch (IOException e) {
                System.out.println("User #: " + clientNumber + " disconnected \n");
                printWriters.remove(this.outWriter);
                server.removeThread(clientNumber);
                e.printStackTrace();
                running = false;
            }
        }
    }

    private void sendWelcomeMessage() {

        outWriter.println("*****************************************\n"
                + "Welcome to the Atlantis Game Server \n"
                + "*****************************************\n"
                + "Server IP Address: "
                + clientSocket.getLocalAddress()
                + "\nConnected to Server Port " + clientSocket.getLocalPort()
                + "\n*****************************************\n");
    }

    private void sendGoodBye() {
        outWriter.println("GoodBye");
    }

    public void sendShutDownMessage() {
        outWriter.println("Server is shutting down");
    }

    public int getClientNumber() {
        return clientNumber;
    }

    public void sendMessage(String message) {
        outWriter.println(message);
    }
}
