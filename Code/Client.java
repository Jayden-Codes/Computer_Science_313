
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.DimensionUIResource;

import java.net.*;

/**
 * This Client Class is the one with the methods for manipulating the GUI and
 * connecting to the server. As well as sending files over the network.
 * 
 * @author Jayden Abrahams – 23559322@sun.ac.za
 * @author Clinton Elves - 24007676@sun.ac.za
 * @author Cindi-Ann Jeffery – 24695823@sun.ac.za
 * @author Konanani Mathoho – 24774898@sun.ac.za
 * @author Langton Chikukwa – 23607769@sun.ac.za
 */

public class Client {
    private static Socket socketForSignaling;
    static ObjectOutputStream objectOutputStreamTCP;
    static ObjectInputStream objectInputStreamTCP;
    private static String whoAmI = "JAYDEN";
    // private static String directoryPathName;
    private static Client client;

    // private static String hostTCP = "127.0.0.1";/// Server ip
    private static String hostTCP = "25.46.71.1";

    private static int portTCP = 7777;
    private static JTextField userIPAdressTF;

    private static String myIPAddress = "127.0.0.1";
    private static boolean isInACall = false;

    /**
     * Constructor method for Client.
     * 
     * @param socket the socket used for connecting the client to the server
     */

    public Client(Socket socket) {
        try {
            /* Initial setup */
            OutputStream outputStream = socket.getOutputStream();
            objectOutputStreamTCP = new ObjectOutputStream(outputStream);
            InputStream inputStream = socket.getInputStream();
            objectInputStreamTCP = new ObjectInputStream(inputStream);

        } catch (Exception e) {
            offTheClient();

        }

    }

    /**
     * The main method that runs the Client.
     */

    public static void main(String[] args) throws IOException {
        new Thread(new Runnable() {
            /**
             * Run the start up gui in new thread
             */
            @Override
            public void run() {
                startUpWindow();

            }
        }).start();
    }

    private static JFrame startUpFrame = new JFrame("SENDER");
    private static JTextField serverIPTF = new JTextField(15);
    private static JTextField portNumTF = new JTextField(15);
    private static JTextField usernameTF;
    private static String username = "";

    /**
     * The method that creates the startup window GUI.
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
        startPanel.add(new JLabel("Your IP: "), constraints);
        constraints.gridx = 1;
        userIPAdressTF = new JTextField(15);
        startPanel.add(userIPAdressTF, constraints);

        constraints.gridy = 3;
        constraints.gridx = 0;
        startPanel.add(new JLabel("Enter a username:  "), constraints);
        constraints.gridx = 1;
        usernameTF = new JTextField(15);
        startPanel.add(usernameTF, constraints);

        constraints.gridy = 4;

        startPanel.add(warning, constraints);

        startUpFrame.add(startPanel, new BorderLayout().CENTER);

        connectBtn.addActionListener(new ActionListener() {
            /**
             * As required Action Listener to listen to input
             * that is during the setup
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                whoAmI = usernameTF.getText();
                myIPAddress = userIPAdressTF.getText();
                username = usernameTF.getText();
                if (username.strip().length() < 1) {
                    warning.setText("Username Not Valid");
                } else if (Integer.parseInt(portNumTF.getText()) < 1024 ||
                        !isInterger(portNumTF.getText())) {
                    warning.setText("Port Not Valid");
                } else if (!serverIPTF.getText().contains(".")
                        || serverIPTF.getText().length() < 7) {
                    warning.setText("IP Address Not Valid");
                } else {
                    try {
                        hostTCP = serverIPTF.getText();
                        portTCP = Integer.parseInt(portNumTF.getText());

                        socketForSignaling = new Socket(hostTCP, portTCP);
                        client = new Client(socketForSignaling);
                        createGroupMenuBtn.setEnabled(false);
                        callerBtn.setEnabled(false);
                        client.listenForMessages();

                        Packet pk = new Packet(whoAmI, "SERVER", "#CONNECTING", myIPAddress);
                        objectOutputStreamTCP.writeObject(pk);
                    } catch (Exception ex) {
                        warning.setText("SERVER NOT ACTIVE");
                        offTheClient();

                    }
                }
            }
        });

        startUpFrame.getRootPane().setDefaultButton(connectBtn);
        startUpFrame.add(connectBtn, new BorderLayout().SOUTH);
        startUpFrame.setMinimumSize(new DimensionUIResource(400, 300));
        startUpFrame.setVisible(true);
        startUpFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    }

    /**
     * @param x string that contains an integer
     * @return returns true if the x variable is an integer
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
     * This method listens for messages from the clients,
     * creates directories in the project directory
     * and sends files between clients.
     */

    public void listenForMessages() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    /* While socket is active */
                    while (socketForSignaling.isConnected()) {

                        Packet packetReceived = (Packet) (objectInputStreamTCP.readObject());
                        if (packetReceived.type.equals("#CONNECTIONVALID")) {
                            startUpFrame.dispose();
                            makeFolders(whoAmI);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    chatWindow();
                                }
                            }).start();
                        } else if (packetReceived.type.equals("#USERNAMEINVALID")) {
                            warning.setText("USERNAME NOT UNIQUE");
                        } else if (packetReceived.type.equals("#MESSAGE")) {
                            chatBoxTA.append(packetReceived.from + ": " + new String(packetReceived.getData()) + "\n");
                        } else if (packetReceived.type.equals("#GROUPINVITE")) {
                            String groupName = packetReceived.getGroupName();
                            String groupIp = packetReceived.getGroupIP();
                            int groupPort = packetReceived.getGroupPortNum();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    new MulticastGroup(packetReceived.getFrom(), whoAmI, groupIp, groupPort, groupName);
                                }
                            }).start();

                        } else if (packetReceived.type.equals("#VNSENDINGREQ")) {
                            /* Prepare receiver to receive file */

                            vnReceiver(whoAmI, packetReceived.from, packetReceived.getGroupIP(),
                                    packetReceived.getGroupPortNum());

                            /* Inform the sender */
                            Packet pk1 = new Packet(whoAmI, "ALL", "#VNSENDINGRES", packetReceived.getGroupIP(),
                                    packetReceived.getGroupPortNum(),
                                    packetReceived.getFileName(), packetReceived.getFileLength());
                            client.writer(pk1);

                        } else if (packetReceived.type.equals("#VNSENDINGRES")) {
                            vnSender(packetReceived.getGroupIP(), packetReceived.getGroupPortNum(),
                                    new File("./Voice_Notes_" + whoAmI + "/Sent/" + packetReceived.fileName));
                            chatBoxTA.append("VOICE NOTE SENT.\n");
                        }

                        else if (packetReceived.type.equals("#CALLREQ")) {

                            new Thread(new Runnable() {
                                /**
                                 * Start new group window
                                 */
                                @Override
                                public void run() {
                                    new MulticastGroup(packetReceived.getFrom(), whoAmI, packetReceived.getGroupIP(),
                                            packetReceived.getGroupPortNum(), packetReceived.getGroupName());
                                }
                            }).start();

                            isInACall = true;
                            endCall = false;
                            callerBtn.setText("END");
                            chatBoxTA.append("CALL STARTED BY " + packetReceived.getMessage() + "...\n");
                            /* Start sending audio on call */
                            sendVoice3("238.0.0.1");
                            /* listen to incoming audio */
                            listenVoice3("239.0.0.1");

                        } else if (packetReceived.type.equals("#USERLIST")) {

                            boolean hasFile = false;
                            model = new DefaultListModel<>();
                            ArrayList<String> uStrList = packetReceived.getMembersList();
                            for (String user : uStrList) {
                                if (!user.split("-")[0].equals(whoAmI)) {
                                    model.addElement(user);
                                    hasFile = true;
                                }
                            }
                            /* Update list */
                            list.setModel(model);
                            list.repaint();
                            list.validate();

                            list.addListSelectionListener(new ListSelectionListener() {
                                public void valueChanged(ListSelectionEvent e) {
                                    if (!e.getValueIsAdjusting()) {
                                        List<String> selectedItems = list.getSelectedValuesList();
                                        messageBoxTF.setText("");
                                        for (String item : selectedItems) {
                                            messageBoxTF
                                                    .setText(messageBoxTF.getText() + "#@" + item.split("-")[0] + " ");
                                        }
                                    }
                                }
                            });
                            cgList.setModel(model);
                            cgList.repaint();
                            cgList.validate();
                            if (hasFile) {
                                createGroupMenuBtn.setEnabled(true);
                                callerBtn.setEnabled(true);
                                chatFrame.repaint();
                                chatFrame.validate();
                                cgDialog.repaint();
                                cgDialog.validate();
                            } else {
                                createGroupMenuBtn.setEnabled(false);
                                callerBtn.setEnabled(false);
                            }
                        } else if (packetReceived.type.equals("#CALLLIST")) {
                            callerModel = new DefaultListModel<String>();
                            ArrayList<String> x = packetReceived.getGroupMemberNames();
                            for (String caller : x) {
                                if (!caller.split("-")[0].equals(whoAmI)) {
                                    callerModel.addElement(caller);
                                }
                            }
                            callerList.setModel(callerModel);
                            callerList.repaint();
                            callerList.validate();
                            callGUI();
                        }
                    }
                } catch (Exception e) {
                    chatBoxTA.append("SERVER DISCONNECTED\n");
                    sendMessageBtn.setEnabled(false);
                    recordVNCWBtn.setEnabled(false);
                    /* kill the client */
                    offTheClient();

                }
            }
        }).start();
    }

    private static JButton sendMessageBtn;
    private static JTextField messageBoxTF;
    private static JTextArea chatBoxTA;
    private static String programName = "GENERAL";
    private static JFrame chatFrame = new JFrame(programName + ": " + whoAmI);
    private static JButton recordVNCWBtn;
    private static JButton callerBtn = new JButton("Call");
    private static JLabel warning = new JLabel("");
    private static JTextArea onlineTA = new JTextArea("     ACTIVE USERS     \n");
    private static JPanel sidePanel = new JPanel(new GridBagLayout());

    private static DefaultListModel<String> model = new DefaultListModel<>();
    private static JList<String> list = new JList<>(model);
    private static JList<String> cgList = new JList<>(model);
    private static DefaultListModel<String> callerModel = new DefaultListModel<>();
    private static JList<String> callerList = new JList<>(callerModel);

    private static JMenuItem createGroupMenuBtn = new JMenuItem("Create Group");

    /**
     * This method creates the GUI for the chat window between clients.
     */

    public static void chatWindow() {
        GridBagConstraints constraint = new GridBagConstraints();
        JPanel chatboxPanel = new JPanel();
        JPanel bottomPanel = new JPanel();
        chatFrame.setTitle(programName + ": " + whoAmI);
        chatboxPanel.setLayout(new BorderLayout());
        bottomPanel.setBackground(Color.LIGHT_GRAY);
        bottomPanel.setLayout(new GridBagLayout());
        messageBoxTF = new JTextField(30);
        messageBoxTF.requestFocusInWindow();
        sendMessageBtn = new JButton("Send Message");
        sendMessageBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (messageBoxTF.getText().length() < 1) {
                    // don't do anything
                } else if (messageBoxTF.getText().equals("#CLEAR")) {
                    chatBoxTA.setText("Cleared all messages\n");
                    messageBoxTF.setText("");
                } else if (messageBoxTF.getText().equals("#EXIT")) {
                    chatBoxTA.append("You left.\n");
                    offTheClient();
                } else {
                    String message = messageBoxTF.getText();
                    chatBoxTA.append("YOU: " + message + "\n");
                    messageBoxTF.setText("");

                    Packet pk1 = new Packet(whoAmI, null, "#MESSAGE", message.getBytes(), null, 0);
                    client.writer(pk1);

                }
                messageBoxTF.requestFocusInWindow();
            }
        });

        recordVNCWBtn = new JButton("Record");
        recordVNCWBtn.addActionListener(new ActionListener() {
            /* Start recording voice note */
            @Override
            public void actionPerformed(ActionEvent e) {
                recordVN();

            }
        });

        callerBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (!isInACall) {

                    Packet pk1 = new Packet(whoAmI, "SERVER", "#CALLERUPDATE", "hi:)");
                    client.writer(pk1);
                } else {
                    callerBtn.setText("Call");
                    isInACall = !isInACall;

                    /* Notify server you ended the call */
                    Packet pk1 = new Packet(whoAmI, "SERVER", "#ENDCALL", "hi:)");
                    client.writer(pk1);

                    chatBoxTA.append("CALL ENDED.");
                    endCall = true;
                }

            }
        });

        chatBoxTA = new JTextArea();
        chatBoxTA.setEditable(false);
        chatBoxTA.setFont(new Font("MONO", Font.TRUETYPE_FONT, 15));
        chatBoxTA.setLineWrap(true);

        JMenuBar menuBar = new JMenuBar();

        JMenu menu = new JMenu("Menu");

        createGroupMenuBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chatFrame.repaint();
                chatFrame.validate();
                cgDialog.repaint();
                cgDialog.validate();
                cgList.repaint();
                cgList.validate();
                createGroupGUI();
            }
        });
        createGroupMenuBtn.setIconTextGap(10);
        menu.add(createGroupMenuBtn);

        JMenuItem commandsItem = new JMenuItem("Commands");
        commandsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chatBoxTA.append(
                        "********************COMMANDS*************************\n" +
                                "* Clear chat: #CLEAR                                                    *\n" +
                                "* Exit chat: #EXIT                                                           *\n" +
                                "***********************************************************\n");
            }
        });
        commandsItem.setIconTextGap(10);
        menu.add(commandsItem);
        menu.addSeparator();

        JMenu vnMenu = new JMenu("Voice Notes");
        JMenuItem receiveItem = new JMenuItem("Received");
        receiveItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileExplorer("Voice Notes Received", "./Voice_Notes_" + whoAmI + "/Received");
            }
        });
        receiveItem.setIconTextGap(10);
        vnMenu.add(receiveItem);
        JMenuItem sentItem = new JMenuItem("Sent");
        sentItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileExplorer("Voice Notes Sent", "./Voice_Notes_" + whoAmI + "/Sent");
            }
        });
        sentItem.setIconTextGap(10);
        vnMenu.add(sentItem);
        menu.add(vnMenu);

        menuBar.add(menu);
        onlineTA.setEditable(false);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        sidePanel.add(onlineTA, c);

        list.setFixedCellWidth(210);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        cgList.setFixedCellWidth(210);
        cgList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        cgList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        callerList.setFixedCellWidth(210);
        callerList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        callerList.setLayoutOrientation(JList.HORIZONTAL_WRAP);

        callerList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                // Check the number of selected elements and restrict it to 4
                if (callerList.getSelectedIndices().length > 2) {
                    int[] selectedIndices = callerList.getSelectedIndices();
                    int lastIndex = selectedIndices[selectedIndices.length - 1];
                    callerList.removeSelectionInterval(lastIndex, lastIndex);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(list);
        c.gridy++;
        sidePanel.add(scrollPane, c);

        chatFrame.add(menuBar, new BorderLayout().NORTH);
        chatboxPanel.add(new JScrollPane(chatBoxTA), BorderLayout.CENTER);
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.weightx = 550.0D;
        bottomPanel.add(messageBoxTF, constraint);
        bottomPanel.add(sendMessageBtn, new GridBagConstraints());
        bottomPanel.add(recordVNCWBtn, new GridBagConstraints());
        bottomPanel.add(callerBtn, new GridBagConstraints());
        chatFrame.add(chatboxPanel, BorderLayout.CENTER);
        chatFrame.add(BorderLayout.SOUTH, bottomPanel);

        chatFrame.add(new JScrollPane(sidePanel), BorderLayout.EAST);
        chatFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chatFrame.getRootPane().setDefaultButton(sendMessageBtn);
        chatFrame.setMinimumSize(new DimensionUIResource(505, 300));
        chatFrame.setVisible(true);

    }

    /**
     * File viewing GUI
     * 
     * @param frameName - chatframe
     * @param dirPath   - directory path
     */
    public static void fileExplorer(String frameName, String dirPath) {

        JFrame fileVFrame = new JFrame(frameName);
        DefaultListModel<String> fmodel = new DefaultListModel<String>();
        File directory = new File(dirPath);

        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                fmodel.addElement(file.getName());
            }
        }
        /* Update list of files */
        JList<String> list = new JList<String>(fmodel);
        list.setFixedCellWidth(300);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL_WRAP);
        list.setVisibleRowCount(-1);
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int index = list.getSelectedIndex();
                    String fileName = (String) fmodel.get(index);
                    try {
                        Desktop.getDesktop().open(new File(dirPath + File.separator + fileName));
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(null, "Error File Not Supported: " + fileName,
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        fileVFrame.add(list);
        fileVFrame.setVisible(true);
        fileVFrame.setMaximumSize(new Dimension(300, 300));
        fileVFrame.setMinimumSize(new Dimension(300, 300));
        fileVFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                fileVFrame.dispose();
            }
        });
    }

    private static JDialog callerDialog = new JDialog(chatFrame, whoAmI + ", who do you want to call?");

    /**
     * Available callers list GUI
     */
    public static void callGUI() {
        callerDialog = new JDialog(chatFrame, whoAmI + ", who do you want to call?");

        JPanel tPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;

        JScrollPane scrollPane = new JScrollPane(callerList);

        tPanel.add(scrollPane, constraints);

        JButton callBtn = new JButton("Call");
        callBtn.addActionListener(new ActionListener() {
            /*
             * Listens to when call button is click
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                String x = "#CALL";
                int cNum = 1;
                for (String personToCall : callerList.getSelectedValuesList()) {
                    x += "@" + personToCall;
                    cNum++;
                }
                System.out.println("cNum =" + cNum);

                if (cNum > 1) {
                    isInACall = true;

                    callerBtn.setText("End");
                    String groupName = whoAmI + "-" + portGenerator();
                    String groupIp = ipGenerator();
                    int groupPort = portGenerator();

                    String callIp = ipGenerator();
                    int callPort = portGenerator();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            new MulticastGroup(whoAmI, whoAmI, groupIp, groupPort, groupName);
                        }
                    }).start();

                    chatBoxTA.append("CALLING...\n");

                    endCall = false;
                    /*
                     * Start Sending audio on call
                     */
                    sendVoice3("239.0.0.1");
                    /*
                     * listens for incoming audio
                     */
                    listenVoice3("238.0.0.1");
                    Packet pk = new Packet(whoAmI, "SERVER", "#CALL", callIp, callPort, groupName, groupIp,
                            groupPort,
                            x);
                    client.writer(pk);
                }

                callerDialog.dispose();
            }
        });

        callerDialog.add(tPanel, new BorderLayout().CENTER);
        callerDialog.getRootPane().setDefaultButton(callBtn);
        callerDialog.add(callBtn, new BorderLayout().SOUTH);
        callerDialog.setMinimumSize(new DimensionUIResource(500, 400));
        callerDialog.setVisible(true);
        callerDialog.setLocationRelativeTo(chatFrame);
        callerDialog.addWindowListener(new WindowAdapter() {
            /*
             * Kill the Jdialog
             */
            public void windowClosing(WindowEvent e) {
                callerDialog.dispose();
            }
        });
    }

    private static JDialog cgDialog = new JDialog(chatFrame, "Select Participants: " + whoAmI);

    /**
     * Group creation gui.
     */
    public static void createGroupGUI() {

        cgDialog = new JDialog(chatFrame, "Select Participants: " + whoAmI);
        JPanel tPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;

        JScrollPane scrollPane = new JScrollPane(cgList);

        tPanel.add(scrollPane, constraints);

        JButton createBtn = new JButton("Create");
        createBtn.addActionListener(new ActionListener() {
            /*
             * Action listener for create group menu button
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Creating a group: {" + (cgList.getSelectedValuesList().size() + 1) + "}");
                if (cgList.getSelectedValuesList().size() > 0) {
                    ArrayList<String> x = new ArrayList<>();
                    for (String item : cgList.getSelectedValuesList()) {
                        x.add(item);
                    }

                    Packet pk = new Packet(whoAmI, "SERVER", "#CREATEGROUP", x);
                    client.writer(pk);
                    cgDialog.dispose();
                }
            }
        });

        cgDialog.add(tPanel, new BorderLayout().CENTER);
        cgDialog.getRootPane().setDefaultButton(createBtn);
        cgDialog.add(createBtn, new BorderLayout().SOUTH);
        cgDialog.setMinimumSize(new DimensionUIResource(500, 400));
        cgDialog.setVisible(true);
        cgDialog.setLocationRelativeTo(chatFrame);
        cgDialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                cgDialog.dispose();
            }
        });
    }

    /**
     * Handles the writing of packets
     * 
     * @param pk - Packet to send
     */
    public void writer(Packet pk) {
        try {
            /*
             * writes packet
             */
            this.objectOutputStreamTCP.writeObject(pk);
        } catch (IOException e) {
            e.printStackTrace();
            offTheClient();
        }
    }

    /**
     * Terminates the client by closing sockets and output/input streams.
     */

    public static void offTheClient() {
        try {
            if (objectInputStreamTCP != null) {
                objectInputStreamTCP.close();
            }
            if (objectOutputStreamTCP != null) {
                objectOutputStreamTCP.close();
            }
            if (socketForSignaling != null) {
                socketForSignaling.close();
            }
        } catch (Exception e) {
        }
    }

    /**
     * Makes folders to save voice notes into
     * 
     * @param whoAmI - username
     */
    public static void makeFolders(String whoAmI) {
        String dirPathSent = "./Voice_Notes_" + whoAmI + "/Sent/";
        if (!(new File(dirPathSent)).exists()) {
            new File(dirPathSent).mkdirs();
        }
        String dirPathReceived = "./Voice_Notes_" + whoAmI + "/Received/";
        if (!(new File(dirPathReceived)).exists()) {
            new File(dirPathReceived).mkdirs();
        }
    }

    private static File output;

    /**
     * Generates a multicast IP address
     * 
     * @return returns the ip as a string
     */
    public static String ipGenerator() {
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
        Random rand = new Random();
        int firstOctet = rand.nextInt(5) + 5;
        int secondOctet = rand.nextInt(10);
        int thirdOctet = rand.nextInt(10);
        int fourthOctet = rand.nextInt(10);
        return Integer.parseInt(String.format("%d%d%d%d", firstOctet, secondOctet, thirdOctet, fourthOctet));
    }

    /**
     * Handles the recording of voice notes
     */
    public static void recordVN() {
        try {
            AudioFormat af = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44000, 16, 2, 4, 22000, false);
            DataLine.Info datainfo = new DataLine.Info(TargetDataLine.class, af);
            /*
             * Prompt the user to record
             */
            int option = JOptionPane.showOptionDialog(chatFrame, "Press OK to record.", "Record", 2, 1, null, null,
                    null);
            if (option == 0) {
                /* Make folders to save recordings */
                makeFolders(whoAmI);
                TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(datainfo);
                targetLine.open();
                targetLine.start();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        /* Getting audio stream */
                        AudioInputStream recordingStream = new AudioInputStream(targetLine);
                        output = new File("./Voice_Notes_" + whoAmI + "/Sent/VN_" + programName + "_" + whoAmI + "_"
                                + new Date().getTime() + ".wav");
                        try {
                            /* write to file */
                            AudioSystem.write(recordingStream, AudioFileFormat.Type.WAVE, output);
                        } catch (Exception e) {

                        }
                    }
                }).start();

                /* Prompt user to stop recording */
                JOptionPane.showMessageDialog(chatFrame, "Press OK to stop.");
                targetLine.stop();
                targetLine.close();
                String ip = ipGenerator();
                int pt = portGenerator();
                Packet pk1 = new Packet(whoAmI, "ALL", "#VNSENDINGREQ", ip, pt, output.getName(),
                        (int) output.length());

                client.writer(pk1);

            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Sends the voice note using udp multicast
     * 
     * @param ipAddr  - multicast group ip address
     * @param portNum - multicast group port number
     * @param file    - .wav file that is being sent.
     */
    public static void vnSender(String ipAddr, int portNum, File file) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    /*
                     * Setting up socket
                     */
                    InetAddress group = InetAddress.getByName(ipAddr);
                    MulticastSocket socket = new MulticastSocket();
                    socket.joinGroup(group);

                    String fileName = file.getName();
                    byte[] fileNameBytes = fileName.getBytes();
                    DatagramPacket fileNamePacket = new DatagramPacket(fileNameBytes, fileNameBytes.length, group,
                            portNum);
                    /* sending packet */
                    socket.send(fileNamePacket);

                    FileInputStream fis = new FileInputStream(file);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        DatagramPacket packet = new DatagramPacket(buffer, bytesRead, group, portNum);
                        socket.send(packet);
                    }

                    /* closing socket and Stream */
                    fis.close();
                    socket.leaveGroup(group);
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    /**
     * Receives the voice note
     * 
     * @param whoAmI  - username
     * @param from    - who sent the file
     * @param ipAddr  - multicast group ip address
     * @param portNum - multicast group port number
     */
    public static void vnReceiver(String whoAmI, String from, String ipAddr,
            int portNum) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        /* Setting up socket */
                        InetAddress group = InetAddress.getByName(ipAddr);
                        MulticastSocket socket = new MulticastSocket(portNum);
                        socket.joinGroup(group);
                        makeFolders(whoAmI);

                        byte[] fileNameBuffer = new byte[1024];
                        DatagramPacket fileNamePacket = new DatagramPacket(fileNameBuffer, fileNameBuffer.length);
                        socket.receive(fileNamePacket);
                        String fileName = new String(fileNamePacket.getData(), 0, fileNamePacket.getLength());

                        /* Creating file */
                        File file = new File("./Voice_Notes_" + whoAmI + "/Received/" + fileName);
                        FileOutputStream fos = new FileOutputStream(file);

                        byte[] buffer = new byte[1024];
                        while (true) {
                            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                            socket.receive(packet);
                            /* Writing to file */
                            fos.write(packet.getData(), 0, packet.getLength());
                            if (packet.getLength() < 1024) {
                                break;
                            }
                        }

                        /* Close all streams and sockets */
                        fos.close();
                        socket.leaveGroup(group);
                        socket.close();

                        chatBoxTA.append("VOICE NOTE RECEIVED FROM " + from + ".\n");
                        /* Prompt user to play audio */
                        int option = JOptionPane.showOptionDialog(chatFrame, "Press OK to listen to VN from " + from,
                                "PLAY", 2, 1,
                                null, null, null);
                        if (option == 0) {
                            playVN(file);
                        }

                    }
                } catch (IOException e) {
                }
            }
        }).start();

    }

    /**
     * Plays the voice note
     * 
     * @param file - the .wav file that was received
     */
    public static void playVN(File file) {
        try {
            
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);

            Clip clip = (Clip) AudioSystem.getLine(new DataLine.Info(Clip.class, audioInputStream.getFormat()));
            clip.open(audioInputStream);
            clip.start();
            /* Plays recording */
            Thread.sleep(clip.getMicrosecondLength() / 1000);
            clip.stop();
            clip.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static boolean endCall = false;
    private static TargetDataLine streamLine;
    private static SourceDataLine sDLine;

    /**
     * Handles the calling
     * 
     * @param xip - multicast call ip address
     */
    public static void sendVoice3(String xip) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
                int rate = 24000;
                int sampleSize = 16;
                int channels = 1;
                boolean bigE = false;
                int port = 50005;
/* Initial set up */
                AudioFormat sendAF = new AudioFormat(encoding, rate, sampleSize, channels, (sampleSize / 8) * channels,
                        rate, bigE);
                DataLine.Info sendDLI = new DataLine.Info(TargetDataLine.class, sendAF);
                DatagramPacket sendDGP;
                InetAddress sendAddress;

                try {
                    byte[] data = new byte[4096];
                    streamLine = (TargetDataLine) AudioSystem.getLine(sendDLI);
                    streamLine.open(sendAF);
                    streamLine.start();

                    sendAddress = InetAddress.getByName(xip);
                    MulticastSocket sendMCS = new MulticastSocket();
                    while (!endCall) {
                        /*Sending audio until call is ended */
                        streamLine.read(data, 0, data.length);
                        sendDGP = new DatagramPacket(data, data.length, sendAddress, port);
                        sendMCS.send(sendDGP);
                    }
                    streamLine.stop();
                    streamLine.close();
                    sendMCS.close();
                } catch (Exception e) {
                    // System.out.println("Error in sending voice");
                }
            }
        }).start();
    }

    /**
     * Handles the receiving of audio from caller
     * 
     * @param xip - multicast group call ip address
     */
    public static void listenVoice3(String xip) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DataLine.Info dLineInfo;
                AudioFormat audioFormat;
                int port = 50005;
                int sampleRate = 24000;
                try {
                    byte[] receiveData = new byte[4096];
                    InetAddress groupAddr = InetAddress.getByName(xip);
                    MulticastSocket mSocket = new MulticastSocket(port);
                    mSocket.joinGroup(groupAddr);
                    audioFormat = new AudioFormat(sampleRate, 16, 1, true, false);
                    dLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
                    sDLine = (SourceDataLine) AudioSystem.getLine(dLineInfo);
                    sDLine.open(audioFormat);
                    sDLine.start();
                    /* creating packet to receive */
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    while (!endCall) {
                        mSocket.receive(receivePacket);
                        try {
                            /* Playing audio  */
                            sDLine.write(receivePacket.getData(), 0, receivePacket.getData().length);
                        } catch (Exception e) {
                        }
                    }
                    /* closing all streams */
                    sDLine.stop();
                    sDLine.close();
                    mSocket.close();
                } catch (Exception e) {
                }
            }
        }).start();
    }

}
