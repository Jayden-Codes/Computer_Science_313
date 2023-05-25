

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

import javax.print.attribute.standard.Severity;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.plaf.DimensionUIResource;

public class Server {
    private ServerSocket serverSocket;

    public Server(ServerSocket ss) {
        serverSocket = ss;
    }

    public void start() {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                textArea.append("Address: "+socket.getRemoteSocketAddress().toString().replace("/", "") + "\n");
                new Thread(new ClientHandler(socket, textArea)).start();

            }

        } catch (Exception e) {
            e.printStackTrace();
            offTheServer();
        }
    }

    static JTextArea textArea;

    public static void gui() {
        JFrame serverframe = new JFrame("SERVER:7777");
        textArea = new JTextArea("SERVER running!\n");
        textArea.setEditable(false);
        serverframe.add(new JScrollPane(textArea));
        serverframe.setMinimumSize(new DimensionUIResource(300, 300));
        serverframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        serverframe.setVisible(true);
    }

    public void offTheServer() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (Exception e) {
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                gui();
            }
        }).start();
        ServerSocket ss = new ServerSocket(7777);
        Server s = new Server(ss);
        s.start();
    }
}
