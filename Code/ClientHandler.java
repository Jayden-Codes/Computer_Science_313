
/**
 * Compilation : upon compilation of Server.java
 * Execution : background execution by Server.java
 * Dependencies: none
 * 
 * This ClientHandler.java file has a class with is used by the Server.java
 * to communicate with the client and handle client requests as well
 * as communicating with the client. 
 */
import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

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
    static CopyOnWriteArrayList<ClientHandler> clientHandlers = new CopyOnWriteArrayList<ClientHandler>();

    private Socket socket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private String clientUN;
    private JTextArea serverTA;

    /**
     * Constructor
     * 
     * Sets up reader and writer to the socket wich is
     * passed by the caller upon object creation
     * 
     * @param socket         socket
     * @param textAreaServer textAreaServer
     */
    public ClientHandler(Socket socket, JTextArea textAreaServer) {
        try {
            this.socket = socket;
            this.serverTA = textAreaServer;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.clientUN = bufferedReader.readLine();
            //used to check validy of username on the whole list of users then if 
            //invalid sends that information to the buffered writer.
            if (!isUserNameUnique(this.clientUN) && this.clientUN != null) {
                writer("#USERNAMEINVALID");
            } 
            //handles the addition of a new user to the list of users stored. 
            else {

                clientHandlers.add(this);
                writer("#USERNAMEVALID " + getUserList());
                serverTA.append("NEW USER JOINED!\n");

                broadcastMessage("#SERVER: " + this.clientUN + " joined");
                String temp = getUserList();
                String[] x = temp.split("-");
                String listStr = "";
                //adjust the displayed list to add the new user. 
                for (int i = 1; i < x.length; i++) {
                    if (i + 1 == x.length) {
                        listStr += "-" + x[i].split(" ", 2)[0] + "\n";
                    } else {
                        listStr += "-" + x[i] + "\n";
                    }
                }
                serverTA.append("ACTIVE USERS:\n" + listStr);

            }

        } catch (Exception e) {
            killAll(socket, bufferedReader, bufferedWriter);
        }
    }

    /**
     * Method to assist in writing to the buffer
     * i.e sending a message to the client.
     * 
     * @param message message
     */
    public void writer(String message) {
        try {
            if (this.socket.isConnected()) {
                this.bufferedWriter.write(message);
                this.bufferedWriter.newLine();
                this.bufferedWriter.flush();
            }
        } catch (IOException e) {
            killAll(socket, bufferedReader, bufferedWriter);
        }
    }

    /**
     * method for assisting with checking if a username is unique
     * on the server side from stored list of Clients
     * 
     * @param name name
     * @return boolean
     */
    public static boolean isUserNameUnique(String name) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.clientUN.equals(name)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Run method required by the threading of the project.
     */
    @Override
    public void run() {
        String messageFromGroup;
        try {
            //always checks that the sokcket is still active before running the code again.
            while (socket.isConnected()) {
                try {
                    //awaits for messages from the clients 
                    messageFromGroup = bufferedReader.readLine();
                    //case:whisper message handle accordingly between the parties. 
                    if (messageFromGroup.contains("#@")) {
                        String[] x = messageFromGroup.split("#@");
                        String secretMess = x[x.length - 1].split(" ", 2)[1];
                        for (int i = 1; i < x.length; i++) {
                            if (i + 1 == x.length) {
                                secretMessage(secretMess, x[i].split(" ", 2)[0]);
                            } else
                                secretMessage(secretMess, x[i].strip());
                        }
                    
                    }
                    //case:user wants to leave handle accordingly and tell the writer the user left.  
                    else if (messageFromGroup.contains("#Exit")) {
                        writer("YOU LEFT!");
                        killAll(socket, bufferedReader, bufferedWriter);

                        break;
                    } 
                    //case: when it's a regular message and broadcast it to everyone else. 
                    else {
                        broadcastMessage(messageFromGroup);
                    }

                } catch (IOException e) {
                    killAll(socket, bufferedReader, bufferedWriter);
                    break;
                }
            }
        } catch (Exception e) {
            killAll(socket, bufferedReader, bufferedWriter);
        }
    }

    /**
     * Method to assist with broadcasting the message to the clients.
     * 
     * @param messageToSend messageToSend
     */
    public void broadcastMessage(String messageToSend) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (!clientHandler.clientUN.equals(clientUN)) {
                clientHandler.writer(messageToSend);
            }
        }
    }

    /**
     * Method to assist me sending direct messages to
     * from client to client
     * 
     * @param messageToSend messageToSend
     * @param whoToSend     whoToSend
     */
    public void secretMessage(String messageToSend, String whoToSend) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.clientUN.equals(whoToSend) && !clientHandler.clientUN.equals(this.clientUN)) {
                clientHandler.writer(this.clientUN + " [PRIVATE]: " + messageToSend);
            }
        }
    }

    /**
     * This to handle when a client leaves the chat.
     */
    public void kickClient() {
        clientHandlers.remove(this);
        serverTA.append(clientUN + " left.\n");
        broadcastMessage("#SERVER: " + clientUN + " left the chat" + getUserList());

    }

    /**
     * Method for assisting in returning all the currently
     * active clients
     * 
     * @return userListStr
     */
    public String getUserList() {
        String userListStr = "";
        for (ClientHandler clientHandler : clientHandlers) {
            userListStr += "-" + clientHandler.clientUN;
        }
        return userListStr;
    }

    /**
     * Method for updating when active user list has changed.
     */
    public void UpdateOnlineStatus() {
        for (ClientHandler clientHandler : clientHandlers) {

            String list = getUserList();
            clientHandler.writer(list);
        }
    }

    /**
     * Method for killing the communication channel
     * as well as killing the buffered reader and writer.
     * 
     * @param socket
     * @param bufferedReader
     * @param bufferedWriter
     */
    public void killAll(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        //first removes the client and sends that message across to all
        kickClient();
        String temp = getUserList();
        String[] x = temp.split("-");
        String listStr = "";
        //adjust the list on the client handler side. 
        for (int i = 1; i < x.length; i++) {
            if (i + 1 == x.length) {
                listStr += "-" + x[i].split(" ", 2)[0] + "\n";
            } else {
                listStr += "-" + x[i] + "\n";
            }
        }
        //sets the updated list
        serverTA.append("ACTIVE USERS:\n" + listStr);
        //closes the buffered reader and writer for that particular client. 
        try {
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("FAILED TO KILL");
        }
    }
}