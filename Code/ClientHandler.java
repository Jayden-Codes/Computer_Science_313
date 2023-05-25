
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JTextArea;
import java.util.Collections;
import java.util.Comparator;

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
    static CopyOnWriteArrayList<Group> Groups = new CopyOnWriteArrayList<Group>();
    static int groupNumber = 0;
    private JTextArea textArea;
    private String userName;
    private boolean isInACall = false;
    private String ipAddress;
    public static int numUsers = 1;

    /**
     * The constructor
     * 
     * @param socket - socket
     * @param ta     - JTextArea of the server
     */
    public ClientHandler(Socket socket, JTextArea ta) {
        try {
            this.socket = socket;
            InputStream inputStream = this.socket.getInputStream();
            this.objectInputStream = new ObjectInputStream(inputStream);
            OutputStream outputStream = this.socket.getOutputStream();
            this.objectOutputStream = new ObjectOutputStream(outputStream);
            this.textArea = ta;
            /* get username */
            Packet packetReceived = (Packet) (this.objectInputStream.readObject());
            this.userName = packetReceived.from;
            if (packetReceived.type.equals("#CONNECTING")) {
                /* check if username is unique */
                if (!isUserNameUnique(this.userName) && this.userName != null) {
                    Packet pk1 = new Packet("SERVER", this.userName, "#USERNAMEINVALID");
                    this.objectOutputStream.writeObject(pk1);
                } else {

                    ipAddress = packetReceived.message;

                    Packet pk1 = new Packet("SERVER", this.userName, "#CONNECTIONVALID");
                    this.objectOutputStream.writeObject(pk1);
/* add user to list */
                    clientHandlers.add(this);

                    this.textArea.append("New user, " + this.userName + ", joined.\n");
                    for (ClientHandler clientHandler : clientHandlers) {
                        clientHandler.sendUpdateList();
                    }
                    /* send active user list */
                    sendUpdateList();

                    /* update server with active users */
                    String s = "";
                    for (ClientHandler clientHandler : clientHandlers) {
                        s += "-" + clientHandler.userName + "\n";
                    }
                    textArea.append("ACTIVE USERS:\n" + s);

                }

            }

        } catch (Exception e) {
        }

    }

    /**
     * Checks of username is unique
     * 
     * @param name - name provided
     * @return - returns true or false
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
     * Order Arraylist based on client ipAddress
     * 
     * @param ipAddressList - list provided
     * @return - ordered list
     */
    public static ArrayList<String> reOrderList(ArrayList<String> ipAddressList) {

        Collections.sort(ipAddressList, new Comparator<String>() {
            @Override
            public int compare(String IPAddress1, String IPAddress2) {
                /* split name from ip address */
                String[] IPAddress1Array = IPAddress1.split("-");
                String[] IPAddress2Array = IPAddress2.split("-");

                String ipAddress1 = IPAddress1Array[1];
                String ipAddress2 = IPAddress2Array[1];

                /* split octets */
                String[] ipAddress1Array = ipAddress1.split("\\.");
                String[] ipAddress2Array = ipAddress2.split("\\.");

                for (int i = 0; i < 4; i++) {
                    int octet1 = Integer.parseInt(ipAddress1Array[i]);
                    int octet2 = Integer.parseInt(ipAddress2Array[i]);

                    if (octet1 != octet2) {
                        return octet1 - octet2;
                    }
                }

                return 0;
            }
        });
        return ipAddressList;
    }

    /**
     * Send Active userlist
     */
    public void sendUpdateList() {
        for (ClientHandler clientHandler : clientHandlers) {
            Packet pk = new Packet("SERVER", userName, "#USERLIST", "hi:)");
            ArrayList<String> x = new ArrayList<String>();
            for (ClientHandler c : clientHandlers) {
                x.add(c.userName + "-" + c.ipAddress);
            }
            /*sort list by ip address */
            reOrderList(x);
            for (String u : x) {
                pk.addGroupMember(u);
            }

            clientHandler.writer(pk);
        }

    }

    /**
     * Sends available for call list
     * 
     * @param sk - socket
     */

    public void sendInCallList(Socket sk) {
        /* available for call list */
        Packet pk = new Packet("SERVER", this.userName, "#CALLLIST", "hi:)");
        for (ClientHandler c : clientHandlers) {
            if (!c.isInACall) {
                pk.addGroupMember(c.userName);
            }
        }

        try {
            this.objectOutputStream.writeObject(pk);
        } catch (Exception e) {
        }
    }

    /*
     * Listens for messages from client
     */
    @Override
    public void run() {
        while (this.socket.isConnected()) {
            try {
                Packet packetReceived = (Packet) (this.objectInputStream.readObject());

                if (packetReceived.type.equals("#MESSAGE")) {
                    broadForwardPacket(packetReceived);

                } else if (packetReceived.type.equals("#CREATEGROUP")) {

                    textArea.append("Group created by " + this.userName + ".\n");
                    ArrayList<String> x = packetReceived.getMembersList();
                    String gpip = ipGenerator();
                    int gppt = portGenerator();
                    Group gp = new Group(groupNumber + "", gpip, gppt);
                    gp.addMember(this.userName);
                    Packet pk1 = new Packet(this.userName, this.userName, "#GROUPINVITE", groupNumber + "", gpip, gppt);
                    this.objectOutputStream.writeObject(pk1);
                    /* add members to groups */
                    for (String person : x) {
                        gp.addMember(person);

                        Packet pk = new Packet(packetReceived.getFrom(), person, "#GROUPINVITE", groupNumber + "", gpip,
                                gppt);
                        directForwardPacket(pk, person.split("-")[0]);

                    }
                    Groups.add(gp);
                    groupNumber++;

                } else if (packetReceived.type.equals("#CALLERUPDATE")) {
                    /* send updated list */
                    sendInCallList(this.socket);
                } else if (packetReceived.type.equals("#CALL")) {
                    /* notify server call started */
                    textArea.append("Call started by " + this.userName + "\n");
                    isInACall = !isInACall;
                    String x[] = packetReceived.message.split("@");
                    for (int i = 1; i < x.length; i++) {
                        for (ClientHandler clientHandler : clientHandlers) {
                            if (clientHandler.userName.equals(x[i]) && !clientHandler.isInACall) {
                                clientHandler.isInACall = !clientHandler.isInACall;

                                Packet pk = new Packet("SERVER", x[i], "#CALLREQ", packetReceived.getCallIp1(),
                                        packetReceived.getCallPortx(), packetReceived.getGroupName(),
                                        packetReceived.getGroupIP(), packetReceived.getGroupPortNum(), this.userName);
                                        /* send call request */
                                directForwardPacket(pk, x[i]);

                            }

                        }
                    }

                } else if (packetReceived.type.equals("#ENDCALL")) {
                    this.isInACall = false;
                } else {
                    broadForwardPacket(packetReceived);
                }

            } catch (Exception e) {
                offTheSystem();
                break;
            }
        }
    }

    /**
     * Generates a multicast IP address
     * 
     * @return returns the ip as a string
     */
    public static String ipGenerator() {
        /* generate random octets in multicast socket range */
        Random rand = new Random();
        int firstOctet = rand.nextInt(16) + 224;
        int secondOctet = rand.nextInt(256);
        int thirdOctet = rand.nextInt(256);
        int fourthOctet = rand.nextInt(256);
        return firstOctet + "." + secondOctet + "." + thirdOctet + "." + fourthOctet;
    }

    /**
     * Generates a port number
     * 
     * @return returns the port number as an integer
     */
    public static int portGenerator() {
        /*
         * generate random numbers for multicast port number
         */
        Random rand = new Random();
        int firstOctet = rand.nextInt(5) + 5;
        int secondOctet = rand.nextInt(10);
        int thirdOctet = rand.nextInt(10);
        int fourthOctet = rand.nextInt(10);
        return Integer.parseInt(String.format("%d%d%d%d", firstOctet, secondOctet, thirdOctet, fourthOctet));
    }

    /**
     * Writes packets
     */
    public void writer(Packet pk) {
        try {
            this.objectOutputStream.writeObject(pk);
        } catch (IOException e) {
            offTheSystem();
        }
    }

    /**
     * Broadcasts packets to all
     * 
     * @param pk - packets to send
     */
    public void broadForwardPacket(Packet pk) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (!clientHandler.userName.equals(this.userName)) {
                clientHandler.writer(pk);
            }
        }
    }

    /**
     * Directly send packet to user
     * 
     * @param pk     - packet to send
     * @param person - person to send to
     */
    public void directForwardPacket(Packet pk, String person) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.userName.equals(person) && !clientHandler.userName.equals(this.userName)) {
                clientHandler.writer(pk);
            }
        }
    }

    /**
     * Remove the user
     */
    public void removeUser() {
        textArea.append("USER, " + this.userName + ", has left.\n");
        numUsers--;
        clientHandlers.remove(this);
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.sendUpdateList();
        }
        String s = "";
        for (ClientHandler clientHandler : clientHandlers) {
            s += "-" + clientHandler.userName + "\n";
        }
        textArea.append("ACTIVE USERS:\n" + s);

    }

    /**
     * Kill all the sockets and input and output streams
     */
    public void offTheSystem() {
        /* first remove user before closing socket and streams */
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
        }

    }
}
