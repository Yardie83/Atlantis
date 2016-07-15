package sample;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created by LorisGrether on 15.07.2016.
 */
public class Model {

    private static final int PORT = 9000;

    public Model() {

        try {
            this.waitForConnections();
        } catch (IOException e) {
            System.out.println("Error kei anig");
        }
    }

    private void waitForConnections() throws IOException {
        System.out.println("The server is running");

        ServerSocket listener = new ServerSocket(PORT);


        try {
            while (true) {
                new Handler(listener.accept()).run();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            listener.close();
        }


    }


}
