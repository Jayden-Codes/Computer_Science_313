
/**
 * Compilation : javac Server.java 
 * Execution : java Server
 * Dependencies: ClientHandler.java
 * 
 * The server.java file is concerned with all things server and has all
 * the necessary tools to form a server to allow other clients to communicate.
 */
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Classes containing all the necessary method to handle server related
 * processes as
 * well as defining a GUI for the server.
 * 
 * @author Jayden Abrahams – 23559322@sun.ac.za
 * @author Clinton Elves - 24007676@sun.ac.za
 * @author Cindi-Ann Jeffery – 24695823@sun.ac.za
 * @author Konanani Mathoho – 24774898@sun.ac.za
 * @author Langton Chikukwa – 23607769@sun.ac.za
 */
public class Server {
    private ServerSocket serverSocket;
    static JTextArea textArea;

    /**
     * Constructor
     * 
     * Basically sets the instance variable to the object
     * passed which is the ServerSocket
     * 
     * @param serverSocket serverSocket
     */
    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    /**
     * Begins the server socket and essentiallly starting the server
     */
    public void startServer() {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket, textArea)).start();
            }
        } catch (IOException e) {
            closeSocket();
        }
    }

    /**
     * Sets up the GUI for teh server side and also sets it to be visible
     */
    public static void gui() {
        JFrame serverframe = new JFrame("SERVER:8005");
        textArea = new JTextArea("SERVER running!\n");
        textArea.setEditable(false);
        serverframe.add(new JScrollPane(textArea));
        serverframe.setMinimumSize(new Dimension(300, 300));
        serverframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        serverframe.setVisible(true);
    }

    /**
     * Closes the server socket for communication
     */
    public void closeSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Main method defined socket which will be used for communication
     * with the client side and allows threads which also sets up the gui
     * 
     * @param args args
     */
    public static void main(String[] args) {

        try {
            //establish a new server connection on the server side
            ServerSocket serverSocket = new ServerSocket(8005);
            Server server = new Server(serverSocket);
            //start a GUI on the server side as well.
            new Thread(new Runnable() {
                @Override
                public void run() {
                    gui();
                }
            }).start();
            server.startServer();
        } catch (IOException e) {
            System.out.println("PORT already in use!");
        }

    }

}
