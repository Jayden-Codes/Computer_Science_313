
/**
 * Compilation: javac Client.java Execution: java Client Dependencies: none
 * 
 * This Client.java file is what allow you to communicate with other clients on the same socket and
 * also have broadcast and direct messages as options of communication.
 */
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.*;
import javax.swing.plaf.DimensionUIResource;

import java.io.IOException;
import java.net.*;

/**
 * This Client Class is the one with the methods for manipulating the GUI and
 * connecting to the the
 * server and keeping state of the chat on the client side. All necessary client
 * side needs for I/O
 * and Net operations are handled here.
 * 
 * @author Jayden Abrahams – 23559322@sun.ac.za
 * @author Clinton Elves - 24007676@sun.ac.za
 * @author Cindi-Ann Jeffery – 24695823@sun.ac.za
 * @author Konanani Mathoho – 24774898@sun.ac.za
 * @author Langton Chikukwa – 23607769@sun.ac.za
 */
public class Client {
    
    private static Socket socket;
    private static BufferedReader bufferedReader;
    private static BufferedWriter bufferedWriter;
    private static String username;
    private static Client client;
    //GUI variables
    private static JTextField usernameTF;
    private static JButton sendMessageBtn;
    private static JTextField messageBoxTF;
    private static JTextArea chatBoxTA;
    private static String programName = "CHAT";
    private static JFrame chatFrame = new JFrame(programName);
    private static JTextArea onlineTA = new JTextArea("*****ONLINE USERS*****\n");
    private static JButton commandsBtn;
    private static JLabel warning = new JLabel("");
    private static JFrame startUpFrame = new JFrame("CLIENT");
    private static JTextField serverIPTF = new JTextField(15);
    private static JTextField portNumTF = new JTextField(15);

    /**
     * Constructor
     * 
     * Sets up the socket, sets up ability to read from socket and write to the
     * socket.
     * 
     * @param socket socket
     * @param host   host
     * @param port   port
     * @throws IOException e
     */
    public Client(Socket socket, String host, int port) {
        //setting up a connection to the socket and also the reader and writer to the socket
        try {
            this.socket = new Socket(host, port);
            this.bufferedReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));

        } catch (IOException e) {
            killAll(socket, bufferedReader, bufferedWriter);
        }
    }

    /**
     * This method is called to setup the GUI for the client side of the whole
     * program stack
     * This section is for the intial variables required to establish a connection
     * This also has actions that shoul performed on the GUI
     */
    public static void startUpWindow() {

        JPanel startPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        JButton connectBtn = new JButton("Connect!");

        constraints.gridy = 0;
        constraints.gridx = 0;
        startPanel.add(new JLabel("Server IP: "), constraints);
        constraints.gridx = 1;
        serverIPTF.requestFocusInWindow();
        startPanel.add(serverIPTF, constraints);
        constraints.gridy = 1;
        constraints.gridx = 0;
        startPanel.add(new JLabel("Port: "), constraints);
        constraints.gridx = 1;
        startPanel.add(portNumTF, constraints);
        constraints.gridy = 2;
        constraints.gridx = 0;

        startPanel.add(new JLabel("Enter a username:  "), constraints);
        constraints.gridx = 1;
        usernameTF = new JTextField(15);
        startPanel.add(usernameTF, constraints);
        constraints.gridy = 3;
        constraints.gridx = 1;
        startPanel.add(warning, constraints);

        startUpFrame.add(startPanel, new BorderLayout().CENTER);

        connectBtn.addActionListener(new ActionListener() {
            /**
             * As required Action Lister to listen to input
             * that is during the setup
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                username = usernameTF.getText();
                //checking validity options for username
                //case: is none
                if (username.strip().length() < 1) {
                    warning.setText("Username Not Valid");
                //checking validity of port number
                //case: must be reater than or equal to 1024
                } else if (Integer.parseInt(portNumTF.getText()) < 1024 || !isInterger(portNumTF.getText())) {
                    warning.setText("Port Not Valid");
                //validity of ip-address
                } else if (!serverIPTF.getText().contains(".")
                        || serverIPTF.getText().length() < 7) {
                    warning.setText("IP Address Not Valid");
                //This is where the new instance of Client is created if error then server is inactive
                } else {
                    try {
                        client = new Client(socket, serverIPTF.getText(),
                                Integer.parseInt(portNumTF.getText()));
                        client.listenForMessages();
                        writer(username);
                    } catch (Exception ex) {
                        warning.setText("SERVER NOT ACTIVE");
                    }

                }
            }
        });
        //the window for the startup section of the GUI is now displayed here. 
        startUpFrame.getRootPane().setDefaultButton(connectBtn);
        startUpFrame.add(connectBtn, new BorderLayout().SOUTH);
        startUpFrame.setMinimumSize(new DimensionUIResource(400, 300));
        startUpFrame.setVisible(true);
        startUpFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    }

    /**
     * Sets up the GUI elements of the program chat section. 
     */
    public static void chatWindow() {
        JPanel sidePanel = new JPanel();
        GridBagConstraints constraint = new GridBagConstraints();
        JPanel chatboxPanel = new JPanel();
        JPanel bottomPanel = new JPanel();
        chatFrame.setTitle(programName + ": " + username);
        chatboxPanel.setLayout(new BorderLayout());
        bottomPanel.setBackground(Color.LIGHT_GRAY);
        bottomPanel.setLayout(new GridBagLayout());
        messageBoxTF = new JTextField(30);
        messageBoxTF.requestFocusInWindow();
        sendMessageBtn = new JButton("Send Message");
        sendMessageBtn.addActionListener(new ActionListener() {
            @Override
            /**
             * Upon receiving a message, the message is checked for it's validity
             * and then handled here. 
             */
            public void actionPerformed(ActionEvent e) {
                //checkigng validity of message received in the text field. 
                if (messageBoxTF.getText().length() < 1) {
                    // don't do anything
                } else if (messageBoxTF.getText().equals("#Clear")) {
                    chatBoxTA.setText("Cleared all messages\n");
                    messageBoxTF.setText("");
                } else {
                    String message = messageBoxTF.getText();
                    String text = username + ": " + message;
                    chatBoxTA.append(text + "\n");
                    messageBoxTF.setText("");
                    writer(text);
                }
                messageBoxTF.requestFocusInWindow();
            }
        });

        commandsBtn = new JButton("Commands");
        commandsBtn.addActionListener(new ActionListener() {
            /**
             * This section displays the options for the possible commands one can enter and receive some information
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                chatBoxTA.append(
                        "********************COMMANDS********************\n Whisper: @user1name @user2name message\n Clear chat: #Clear\n Exit chat: #Exit\n******************************************************\n");
            }
        });
        //This section deals with now creating the elements with the configurations above
        //for the GUI for the chat section and displaying it. 
        onlineTA.setEditable(false);
        onlineTA.setAutoscrolls(true);
        onlineTA.setSize(100, 200);
        sidePanel.add(onlineTA);
        chatBoxTA = new JTextArea();
        chatBoxTA.setEditable(false);
        chatBoxTA.setFont(new Font("MONO", Font.TRUETYPE_FONT, 15));
        chatBoxTA.setLineWrap(true);
        chatboxPanel.add(new JScrollPane(chatBoxTA), BorderLayout.CENTER);
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.weightx = 550.0D;
        bottomPanel.add(messageBoxTF, constraint);
        bottomPanel.add(sendMessageBtn, new GridBagConstraints());
        bottomPanel.add(commandsBtn, new GridBagConstraints());
        chatFrame.add(chatboxPanel, BorderLayout.CENTER);
        chatFrame.add(new JScrollPane(sidePanel), BorderLayout.EAST);
        chatFrame.add(BorderLayout.SOUTH, bottomPanel);
        chatFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chatFrame.getRootPane().setDefaultButton(sendMessageBtn);
        chatFrame.setMinimumSize(new DimensionUIResource(505, 300));
        chatFrame.setVisible(true);

    }

    /**
     * Method for assiting in the listening of messages
     */
    public void listenForMessages() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String messageFromGroup;
                try {
                    //remains checking for the validity of a message
                    while (socket.isConnected()) {
                        try {
                            //waits to read and receive some message from the buffered reader. 
                            messageFromGroup = bufferedReader.readLine();
                            //checking different options for that message. 
                            //case: username is valid so it sets up a chat windows for that user. 
                            if (messageFromGroup.contains("#USERNAMEVALID")) {
                                warning.setText("UserName Unique!");
                                startUpFrame.dispose();
                                String[] tempList = messageFromGroup.split("#USERNAMEVALID")[1].split("-");
                                for (int i = 1; i < tempList.length; i++) {
                                    onlineTA.append("- " + tempList[i] + "\n");
                                }
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        chatWindow();
                                    }
                                }).start();
                             
                            } 
                            // case: username is invalid therefore tells user that and requests
                            // another username.
                            else if (messageFromGroup.contains("#USERNAMEINVALID")) {
                                warning.setText("UserName Not Unique!");
                              
                            }
                            // case : server is valid so now joined to the server 
                            else if (messageFromGroup.contains("#SERVER:")
                                    && messageFromGroup.contains("joined")) {
                                chatBoxTA.append(messageFromGroup + "\n");
                                onlineTA.append("-"
                                        + messageFromGroup.split("#SERVER: ")[1].split(" joined")[0]
                                        + "\n");

                            } 
                            //case : when someone leaves the group then updates are amde on the client side. 
                            else if (messageFromGroup.contains("#SERVER:")
                                    && messageFromGroup.contains(" left the chat")) {
                                chatBoxTA.append(messageFromGroup.split("-")[0] + "\n");

                                String[] members = messageFromGroup.split("left the chat")[1].split("-");
                                onlineTA.setText("*****ONLINE USERS*****\n");

                                for (int i = 1; i < members.length; i++) {

                                    if (i + 1 == members.length) {
                                        onlineTA.append("-" + members[i].split(" ", 2)[0] + "\n");
                                    } else {
                                        onlineTA.append("-" + members[i] + "\n");
                                    }
                                }
                            } 
                            //if the current client leaves then case handled
                            else if (messageFromGroup.equals("YOU LEFT!")) {
                                sendMessageBtn.setEnabled(false);
                                messageBoxTF.setEnabled(false);
                                commandsBtn.setEnabled(false);
                                chatBoxTA.append(messageFromGroup + "\n");
                            }
                            //else it's a regular message 
                            else {

                                chatBoxTA.append(messageFromGroup + "\n");
                            }
                        } catch (IOException e) {
                            killAll(socket, bufferedReader, bufferedWriter);
                        }
                    }
                } catch (Exception e) {
                    warning.setText("SERVER NOT ACTIVE");
                }
            }
        }).start();
    }

    /**
     * Method for writing a given message to the buffered writer and
     * if it fails it will kill all operations
     * 
     * @param message message
     */
    public static void writer(String message) {
        try {
            if (socket.isConnected()) {
                bufferedWriter.write(message);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            killAll(socket, bufferedReader, bufferedWriter);
        }
    }

    /**
     * Called when things have gone wrong of when the sockets and readers
     * are to closed even due to logging out.
     * 
     * @param socket         socket
     * @param bufferedReader bufferedReader
     * @param bufferedWriter bufferedWriter
     */
    public static void killAll(Socket socket, BufferedReader bufferedReader,
            BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("FAILED TO KILL CONNECTION");
        }
    }
    /**
     * Method for checking if a string value is an Integer
     * 
     * @param x x
     * @return
     */
    private static boolean isInterger(String x) {
        try {
            Integer.parseInt(x);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Begins running the threads for the program
     * as well as in the thread opening the GUI
     * 
     * @param args args
     */
    public static void main(String[] args) {
        new Thread(new Runnable() {
            /**
             * Begin by starting up the main window. 
             */
            @Override
            public void run() {
                startUpWindow();
            }
        }).start();
    }
}
