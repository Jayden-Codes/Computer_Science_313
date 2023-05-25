
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JLabel;
import javax.swing.JTextArea;

/**
 * Has methods required to assist Server.java in it's interaction with
 * the clients.
 * 
 * @author Jayden Abrahams – 23559322@sun.ac.za
 * @author Clinton Elves - 24007676@sun.ac.za
 * @author Cindi-Ann Jeffery – 24695823@sun.ac.za
 * @author Konanani Mathoho – 24774898@sun.ac.za
 * @author Langton Chikukwa – 23607769@sun.ac.za
 */
public class ClientHandler implements Runnable {

    private Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    static CopyOnWriteArrayList<ClientHandler> clientHandlers = new CopyOnWriteArrayList<ClientHandler>();
    private JTextArea textArea;
    private String userName;

    /**
     * Constructor method for ClientHandler
     * 
     * @param socket sets up connection between server and client
     * @param tA     displays information about the clients joining and file sharing
     */
    public ClientHandler(Socket socket, JTextArea ta) throws IOException {
        this.socket = socket;
        InputStream inputStream = this.socket.getInputStream();
        this.objectInputStream = new ObjectInputStream(inputStream);
        OutputStream outputStream = this.socket.getOutputStream();
        this.objectOutputStream = new ObjectOutputStream(outputStream);
        this.textArea = ta;

        try {
            /* Gets first packet */
            Packet packetReceived = (Packet) (this.objectInputStream.readObject());
            this.userName = packetReceived.from;
            if (packetReceived.type.equals("#CONNECTING")) {
                if (!isUserNameUnique(this.userName) && this.userName != null) {
                    Packet pk1 = new Packet("SERVER", this.userName, "#USERNAMEINVALID", null, null, 0);
                    this.objectOutputStream.writeObject(pk1);
                } else {

                    /* Adds Client */
                    Packet pk1 = new Packet("SERVER", this.userName, "#CONNECTIONVALID", null, null, 0);
                    this.objectOutputStream.writeObject(pk1);
                    ta.append("NEW USER JOINED: " + this.userName + "\n");
                    clientHandlers.add(this);

                }

            }

        } catch (ClassNotFoundException e) {
        }

    }

    /**
     * 
     * @param name the username to test for uniqueness
     * @return returns true if the username is unique
     * 
     */
    public static boolean isUserNameUnique(String name) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.userName.equals(name)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Run method required by the threading of the project.
     * This method sends packets over the server to the receiver.
     */
    @Override
    public void run() {
        String messageFromGroup;
        while (this.socket.isConnected()) {
            try {
                Packet packetReceived = (Packet) (this.objectInputStream.readObject());

                if (packetReceived.type.equals("#MESSAGE")) {
                    messageFromGroup = new String(packetReceived.data);
                    forwardPacket(packetReceived);
                } else if (packetReceived.type.equals("#TCPFILEREQ")) {
                    this.textArea.append(this.userName + " wants to send \"" + new String(packetReceived.fileName)
                            + "\" over TCP.\n");
                    forwardPacket(packetReceived);
                } else if (packetReceived.type.equals("#RBUDPFILEREQ")) {
                    this.textArea.append(this.userName + " wants to send \"" + new String(packetReceived.fileName)
                            + "\" over RBUDP.\n");
                    forwardPacket(packetReceived);
                } else {
                    forwardPacket(packetReceived);
                }

            } catch (Exception e) {
                offTheSystem();
                break;
            }
        }
    }

    /**
     * This method writes packet objects to an OutputStream
     * 
     * @param pk the packet object
     */
    public void writer(Packet pk) {
        try {
            this.objectOutputStream.writeObject(pk);
        } catch (Exception e) {
            offTheSystem();
        }
    }

    /**
     * This method forwards packets to the client.
     * 
     * @param pk the packet object
     */
    public void forwardPacket(Packet pk) {
        /* Forwards the packet to the recepient */
        for (ClientHandler clientHandler : clientHandlers) {
            if (!clientHandler.userName.equals(this.userName)) {
                clientHandler.writer(pk);
            }
        }
    }

    /**
     * This method removes users from the server.
     */
    public void removeUser() {
        textArea.append(this.userName + " left.\n");
        clientHandlers.remove(this);
    }

    /**
     * This method terminates the server connects and removes the user.
     */
    public void offTheSystem() {
        removeUser();
        try {
            if (this.objectInputStream != null) {
                this.objectInputStream.close();
            }
            if (this.objectOutputStream != null) {
                this.objectOutputStream.close();
            }
            if (this.socket != null) {
                this.socket.close();
            }
        } catch (Exception e) {
            this.textArea.append("FAILED TO KILL");
        }

    }
}
