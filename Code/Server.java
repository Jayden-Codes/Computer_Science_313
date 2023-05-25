
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.plaf.DimensionUIResource;

/**
 * This Server Class runs the server to allow connection between multiple
 * clients.
 * 
 * @author Jayden Abrahams – 23559322@sun.ac.za
 * @author Clinton Elves - 24007676@sun.ac.za
 * @author Cindi-Ann Jeffery – 24695823@sun.ac.za
 * @author Konanani Mathoho – 24774898@sun.ac.za
 * @author Langton Chikukwa – 23607769@sun.ac.za
 */
public class Server {
    private ServerSocket serverSocket;

    /**
     * This method receives a ServerSocket.
     * 
     * @param ss The ServerSocket to be used
     */
    public Server(ServerSocket ss) {
        serverSocket = ss;
    }

    /**
     * This method starts the server which allows clients to communicate with each
     * other.
     */
    public void start() {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket, textArea)).start();
            }
        } catch (Exception e) {
            textArea.append("DISCONNECTED.\n");
            offTheServer();
        }
    }

    static JFrame serverframe = new JFrame("SERVER");
    static JTextArea textArea;

    /**
     * This method creates the GUI.
     */
    public static void gui() {

        /* setting all the GUI attributes */
        textArea = new JTextArea("SERVER running on port " + p + "!\n");
        textArea.setEditable(false);
        serverframe.add(new JScrollPane(textArea));
        serverframe.setMinimumSize(new DimensionUIResource(300, 300));
        serverframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        serverframe.setVisible(true);
    }

    /**
     * This method terminates the server.
     */
    public void offTheServer() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (Exception e) {
        }
    }

    /**
     * This method determines if the parameter given is an integer.
     * 
     * @param x the given String
     * @return true if it is an integer otherwise false
     */
    private static boolean isInterger(String x) {
        try {
            Integer.parseInt(x);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static String p;

    /**
     * The main method that runs the server.
     */
    public static void main(String[] args) {
        try {
            p = JOptionPane.showInputDialog(null, "Enter Server Port Number:", "Server", 1, null, null, "7777") + "";
            if (isInterger(p)) {

                /* Set up GUI */
                ServerSocket ss = new ServerSocket(Integer.parseInt(p));
                Server s = new Server(ss);


                /* run the Server start() method */
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        gui();
                    }
                }).start();
                s.start();
            } else {

                /* Server breaks */
                System.out.println("Server start stopped.");
                System.exit(0);
            }
        } catch (Exception e) {
            serverframe.dispose();
            System.out.println(e.getMessage());
        }

    }
}
