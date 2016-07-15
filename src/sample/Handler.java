package sample;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by LorisGrether on 15.07.2016.
 */
public class Handler extends Thread {

    private String name;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private static ArrayList<PrintWriter> writers = new ArrayList<PrintWriter>();

    public Handler(Socket socket) {

        this.socket = socket;
    }

    public void run() {

        try {

            // Create character streams for the socket.
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            while (true) {
                String input = in.readLine();
                if (input == null) {
                    return;
                }

                System.out.println(input + "/n");

                for (PrintWriter writer : writers) {
                    writer.println("MESSAGE " + name + ": " + input);
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        } finally {

            if (out != null) {
                writers.remove(out);
            }
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("blabal");
            }
        }
    }
}
