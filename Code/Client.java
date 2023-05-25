
import java.util.ArrayList;
import java.util.Arrays;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.FlowLayout;
import java.io.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.DimensionUIResource;
import java.awt.Desktop;
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
    private static String whoAmI = "USERX";
    private static String directoryPathName;
    private static Client client;

    private static String hostTCP = "127.0.0.1";
    private static int portTCP = 7777;

    /**
     * Constructor method for Client.
     * 
     * @param socket the socket used for connecting the client to the server
     */
    public Client(Socket socket) {
        try {

            OutputStream outputStream = socket.getOutputStream();
            objectOutputStreamTCP = new ObjectOutputStream(outputStream);

            InputStream inputStream = socket.getInputStream();
            objectInputStreamTCP = new ObjectInputStream(inputStream);

        } catch (Exception e) {
            e.printStackTrace();
            offTheClient();

        }

    }

    /**
     * The main method that runs the Client.
     */
    public static void main(String[] args) throws IOException {

        new Thread(new Runnable() {
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
        startPanel.add(new JLabel("Enter a username:  "), constraints);
        constraints.gridx = 1;
        usernameTF = new JTextField(15);
        startPanel.add(usernameTF, constraints);

        constraints.gridy = 4;

        startPanel.add(warning, constraints);

        startUpFrame.add(startPanel, new BorderLayout().CENTER);

        connectBtn.addActionListener(new ActionListener() {
            /**
             * As required Action Lister to listen to input during the setup
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                whoAmI = usernameTF.getText();
                hostTCP = serverIPTF.getText();
                /* Checking if inputs are valid */
                if (whoAmI.strip().length() < 1) {
                    warning.setText("Username Not Valid");
                } else if (!isInteger(portNumTF.getText())) {
                    warning.setText("Port Not Valid");
                } else if (!serverIPTF.getText().contains(".")
                        || serverIPTF.getText().length() < 7) {
                    warning.setText("IP Address Not Valid");
                } else {
                    try {

                        portTCP = Integer.parseInt(portNumTF.getText());
                        socketForSignaling = new Socket(hostTCP, portTCP);
                        /* Attempts connection */
                        client = new Client(socketForSignaling);
                        client.listenForMessages();
                        Packet pk = new Packet(whoAmI, "SERVER", "#CONNECTING", null, null, 0);
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
    private static boolean isInteger(String x) {
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
                    /* While client is still connected to the Server */
                    while (socketForSignaling.isConnected()) {

                        Packet packetReceived = (Packet) (objectInputStreamTCP.readObject());
                        if (packetReceived.type.equals("#CONNECTIONVALID")) {
                            directoryPathName = "./FILES_RECEIVED_" + whoAmI + "/";
                            File theDir = new File(directoryPathName);
                            if (!theDir.exists()) {
                                theDir.mkdirs();
                            }
                            directory = new File(directoryPathName);
                            startUpFrame.dispose();
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
                        } else if (packetReceived.type.equals("#TCPFILEREQ")) {
                            int result = JOptionPane.showConfirmDialog(chatFrame,
                                    "DOWNLOAD \"" + packetReceived.fileName + "\" from " + packetReceived.from + " ?",
                                    "INCOMING FILE",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.QUESTION_MESSAGE);
                            if (result == JOptionPane.YES_OPTION) {

                                receivedFileLength = packetReceived.getFileLength();
                                receivedFileName = packetReceived.getFileName();

                                receiveFileTCP();

                                Packet pk = new Packet(whoAmI, null, "#TCPFILERES", null, "YES", 0);
                                client.writer(pk);

                            } else {
                                Packet pk = new Packet(whoAmI, null, "#TCPFILERES", null, "NO", 0);
                                client.writer(pk);

                            }
                        } else if (packetReceived.type.equals("#RBUDPFILEREQ")) {

                            int result = JOptionPane.showConfirmDialog(chatFrame,
                                    "DOWNLOAD \"" + packetReceived.fileName + "\" from " + packetReceived.from + " ?",
                                    "INCOMING FILE",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.QUESTION_MESSAGE);
                            if (result == JOptionPane.YES_OPTION) {
                                receivedFileName = packetReceived.getFileName();

                                totalNumberOfPacketsToReceive = packetReceived.numberOfPackets;
                                packetSizeToReceive = packetReceived.packetSize;

                                Packet pk = new Packet(whoAmI, null, "#RBUDPFILERES", null, "YES", 0);
                                client.writer(pk);
                                chatBoxTA.append("START RECEIVING.....\n");
                                receiveFileRBUDP();
                            } else {
                                Packet pk = new Packet(whoAmI, null, "#RBUDPFILERES", null, "NO", 0);
                                client.writer(pk);
                            }

                        } else if (packetReceived.type.equals("#RBUDPFILERES")) {
                            if (packetReceived.fileName.equals("YES")) {
                                chatBoxTA.append("SENDING...\n");
                                sendFileRBUDP(fileToSend);

                            } else {
                                chatBoxTA.append("NOT SENDING...\n");
                                chatBoxTA.append("RECEIVER DOES NOT WANT IT.\n");
                            }
                        } else if (packetReceived.type.equals("#TCPFILERES")) {
                            if (packetReceived.fileName.equals("YES")) {
                                chatBoxTA.append("SENDING...\n");
                                sendFileTCP();
                            } else {
                                chatBoxTA.append("RECEIVER DOES NOT WANT IT.\n");
                            }
                        }
                    }
                } catch (Exception e) {
                    chatBoxTA.append("SERVER DISCONNECTED.\n");
                    offTheClient();

                }
            }
        }).start();
    }

    /**
     * This method updates the file list that is displayed on the GUI for the
     * client.
     */
    public static void updateFileList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (socketForSignaling.isConnected()) {
                    boolean hasFile = false;
                    model = new DefaultListModel<>();
                    File[] files = directory.listFiles();
                    for (File file : files) {
                        if (file.isFile()) {
                            model.addElement(file.getName());
                            hasFile = true;
                        }
                    }

                    list.setModel(model);
                    list.repaint();
                    list.validate();
                    if (hasFile) {
                        chatFrame.repaint();
                        chatFrame.validate();
                    }
                    /* Happens every second */
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();

                    }
                }
            }
        }).start();
    }

    private static JButton sendMessageBtn;
    private static JTextField messageBoxTF;
    private static JTextArea chatBoxTA;
    private static String programName = "CHAT";
    private static JFrame chatFrame = new JFrame(programName + ": " + whoAmI);
    private static JButton sendChatWindowBtn;
    private static JLabel warning = new JLabel("");
    private static JTextArea onlineTA = new JTextArea("     FILES     ");
    private static JPanel sidePanel = new JPanel(new GridBagLayout());

    private static DefaultListModel<String> model = new DefaultListModel<>();
    private static JList<String> list = new JList<>(model);
    private static File directory;

    private static JProgressBar receiverPB;

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
                    /* Don't do anything */
                } else if (messageBoxTF.getText().equals("#CLEAR")) {
                    /* Clears the TestArea */
                    chatBoxTA.setText("Cleared all messages\n");
                    messageBoxTF.setText("");
                } else if (messageBoxTF.getText().equals("#EXIT")) {
                    /* Disconnects the client */
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

        sendChatWindowBtn = new JButton("Send File");
        sendChatWindowBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendChatWindowBtn.setEnabled(false);
                sendOverGUI();
            }
        });
        chatBoxTA = new JTextArea();
        chatBoxTA.setEditable(false);
        chatBoxTA.setFont(new Font("MONO", Font.TRUETYPE_FONT, 15));
        chatBoxTA.setLineWrap(true);

        JMenuBar menu = new JMenuBar();
        JMenuItem m1 = new JMenuItem("   ");
        JButton commmandsMenuBtn = new JButton("Commands");

        m1.add(commmandsMenuBtn);
        commmandsMenuBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chatBoxTA.append(
                        "********************COMMANDS*************************\n" +
                                "* Clear chat: #CLEAR                                                    *\n" +
                                "* Exit chat: #EXIT                                                           *\n" +
                                "***********************************************************\n");
            }
        });
        menu.add(m1);

        onlineTA.setEditable(false);
        onlineTA.setAutoscrolls(true);
        onlineTA.setSize(100, 200);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        sidePanel.add(onlineTA, c);

        updateFileList();
        /* Allows list files to open on click */
        list.setFixedCellWidth(210);
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        // list.setVisibleRowCount(-1);
        list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                String selectedFileName = list.getSelectedValue();
                File selectedFile = new File(directory, selectedFileName);
                try {
                    Desktop.getDesktop().open(selectedFile);
                } catch (Exception ex) {

                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(list);
        c.gridy++;
        sidePanel.add(scrollPane, c);

        chatFrame.add(menu, new BorderLayout().NORTH);
        chatboxPanel.add(new JScrollPane(chatBoxTA), BorderLayout.CENTER);
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.weightx = 550.0D;
        bottomPanel.add(messageBoxTF, constraint);
        bottomPanel.add(sendMessageBtn, new GridBagConstraints());
        bottomPanel.add(sendChatWindowBtn, new GridBagConstraints());
        chatFrame.add(chatboxPanel, BorderLayout.CENTER);
        chatFrame.add(BorderLayout.SOUTH, bottomPanel);

        chatFrame.add(new JScrollPane(sidePanel), BorderLayout.EAST);
        chatFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chatFrame.getRootPane().setDefaultButton(sendMessageBtn);
        chatFrame.setMinimumSize(new DimensionUIResource(505, 300));
        chatFrame.setVisible(true);

    }

    static JSlider sliderBlastSize = new JSlider();
    static JLabel sliderLabelBlastSize;

    static JSlider sliderPacketSize = new JSlider();
    static JLabel sliderLabelPacketSize;

    static JFrame senderFrame = new JFrame("SENDER");
    static JButton sendPopUpBtn = new JButton("SEND");

    /**
     * This method creates the GUI for the file selection process.
     */
    public static void sendOverGUI() {

        warning.setText("");
        fileToSend = null;
        JDialog dialog = new JDialog(chatFrame, "SEND OVER: " + whoAmI);

        JPanel tPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        tPanel.add(new JLabel("SEND OVER"), constraints);
        constraints.gridy++;

        JPanel tempPanel = new JPanel(new FlowLayout());
        JRadioButton tcpRBtn = new JRadioButton("TCP");
        tcpRBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sliderPacketSize.setVisible(false);
                sliderBlastSize.setVisible(false);
                sliderLabelBlastSize.setVisible(false);
                sliderLabelPacketSize.setVisible(false);
            }
        });
        JRadioButton rbudpRBtn = new JRadioButton("RBUDP");
        rbudpRBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sliderPacketSize.setVisible(true);
                sliderBlastSize.setVisible(true);
                sliderLabelBlastSize.setVisible(true);
                sliderLabelPacketSize.setVisible(true);
            }
        });
        ButtonGroup bg = new ButtonGroup();
        bg.add(tcpRBtn);
        bg.add(rbudpRBtn);
        tcpRBtn.setActionCommand("TCP");
        rbudpRBtn.setActionCommand("RBUDP");
        tempPanel.add(tcpRBtn);
        tempPanel.add(rbudpRBtn);

        tPanel.add(tempPanel, constraints);

        constraints.gridy++;

        sliderPacketSize = new JSlider(JSlider.HORIZONTAL, 10240, 65400, 65400);
        sliderPacketSize.setVisible(false);
        sliderPacketSize.setMinorTickSpacing(10000);
        sliderPacketSize.setMajorTickSpacing(15000);
        sliderPacketSize.setPaintTicks(true);
        sliderPacketSize.setPaintLabels(true);

        sliderPacketSize.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                sliderLabelPacketSize.setText("Packet Size: " + sliderPacketSize.getValue());
            }
        });
        sliderLabelPacketSize = new JLabel("Packet Size: " + sliderPacketSize.getValue());
        sliderLabelPacketSize.setVisible(false);

        tPanel.add(sliderLabelPacketSize, constraints);
        constraints.gridy++;
        tPanel.add(sliderPacketSize, constraints);

        constraints.gridy++;

        tPanel.add(new JLabel(" "), constraints);
        constraints.gridy++;

        sliderBlastSize = new JSlider(JSlider.HORIZONTAL, 2, 10, 5);
        sliderBlastSize.setVisible(false);
        sliderBlastSize.setMinorTickSpacing(1);
        sliderBlastSize.setMajorTickSpacing(2);
        sliderBlastSize.setPaintTicks(true);
        sliderBlastSize.setPaintLabels(true);

        sliderBlastSize.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                sliderLabelBlastSize.setText("Blast Size: " + sliderBlastSize.getValue());
            }
        });
        sliderLabelBlastSize = new JLabel("Blast Size: " + sliderBlastSize.getValue());
        sliderLabelBlastSize.setVisible(false);
        tPanel.add(sliderLabelBlastSize, constraints);
        constraints.gridy++;
        tPanel.add(sliderBlastSize, constraints);

        constraints.gridy++;
        tPanel.add(new JLabel(" "), constraints);
        constraints.gridy++;

        JButton fileBtn = new JButton("FILE TO SEND");
        fileBtn.setEnabled(false);
        /* Enables the file button whenever one of the radio buttons are selected */
        tcpRBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileBtn.setEnabled(true);
            }
        });
        rbudpRBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileBtn.setEnabled(true);
            }
        });

        tPanel.add(fileBtn, constraints);
        constraints.gridy++;
        tPanel.add(warning, constraints);
        constraints.gridy++;
        tPanel.add(new JLabel(" "), constraints);

        sendPopUpBtn.setEnabled(false);
        dialog.add(tPanel, new BorderLayout().CENTER);
        dialog.getRootPane().setDefaultButton(sendPopUpBtn);
        dialog.add(sendPopUpBtn, new BorderLayout().SOUTH);
        dialog.setMinimumSize(new DimensionUIResource(500, 400));
        dialog.setVisible(true);
        dialog.setLocationRelativeTo(chatFrame);
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                sendChatWindowBtn.setEnabled(true);
            }
        });

        /* Allows Sender to choose a file */
        fileBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fChooser = new JFileChooser();
                if (fChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    warning.setText(fChooser.getSelectedFile().getName());
                    sendPopUpBtn.setEnabled(true);
                    fileToSend = fChooser.getSelectedFile();

                } else {
                    warning.setText("File selection cancelled");
                    sendPopUpBtn.setEnabled(false);
                }
            }
        });
        sendPopUpBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                blastSize = sliderBlastSize.getValue();
                packetSize = sliderPacketSize.getValue();
                /* Notifies the receiver that there is an incoming file */
                if (tcpRBtn.isSelected()) {
                    Packet pk = new Packet(whoAmI, null, "#TCPFILEREQ", null, fileToSend.getName(),
                            (int) fileToSend.length());
                    client.writer(pk);
                } else if (rbudpRBtn.isSelected()) {

                    chatBoxTA.append("RBUDP_BlastSize" + blastSize + "_PacketSize" + packetSize + "\n");
                    Packet pk = new Packet(whoAmI, null, "#RBUDPFILEREQ", fileToSend.getName(),
                            (int) fileToSend.length(), packetSize,
                            (int) Math.ceil((double) fileToByteArray(fileToSend).length / packetSize));
                    client.writer(pk);

                }

            }
        });

    }

    /**
     * This method writes packets to an OutputStream.
     * 
     * @param pk the packet object
     */
    public void writer(Packet pk) {
        try {
            this.objectOutputStreamTCP.writeObject(pk);
        } catch (IOException e) {
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
     * This method creates the progress bar on GUI.
     */
    public static void myProgressBar() {
        JDialog dialog = new JDialog(chatFrame, "Progress");
        receiverPB = new JProgressBar();
        receiverPB.setStringPainted(true);
        receiverPB.setBorder(new LineBorder(Color.DARK_GRAY));
        receiverPB.setForeground(Color.green);
        dialog.add(receiverPB);
        dialog.setSize(200, 100);
        dialog.setLocationRelativeTo(chatFrame);
        dialog.setVisible(true);
    }

    private static File fileToSend;

    /**
     * This method sends the selected file using TCP.
     */
    public static void sendFileTCP() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    FileInputStream fIS = new FileInputStream(fileToSend.getAbsolutePath());
                    Socket sendSocket = null;
                    /* Tries to connect to Receiver */
                    while (true) {
                        try {
                            sendSocket = new Socket("127.0.0.1", 1234);
                            break;
                        } catch (Exception e) {
                            try {
                                Thread.sleep(1000);
                            } catch (Exception ignore) {
                            }
                        }
                    }

                    DataOutputStream dOS = new DataOutputStream(sendSocket.getOutputStream());

                    byte[] bSendFile = new byte[(int) fileToSend.length()];
                    fIS.read(bSendFile);
                    dOS.write(bSendFile);
                    dOS.close();
                    fIS.close();
                    sendSocket.close();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
                // }
            }
        }).start();
    }

    private static String receivedFileName;
    private static int receivedFileLength;

    /**
     * This method receives the selected file using TCP.
     */
    public static void receiveFileTCP() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ServerSocket serverSocket = null;
                try {
                    serverSocket = new ServerSocket(1234);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                /* Calculates total number of packets */
                int packTotal = receivedFileLength / 512;
                try {
                    Socket socket1 = serverSocket.accept();
                    myProgressBar();
                    DataInputStream dIS = new DataInputStream(socket1.getInputStream());
                    int progress = 0;
                    int pack = 0;
                    int remaining = 0;
                    OutputStream fileOPstream = new FileOutputStream(directoryPathName + receivedFileName);
                    byte[] bFile = new byte[receivedFileLength];

                    if (receivedFileLength > 0) {
                        remaining = receivedFileLength;
                        while (remaining > 0) {
                            pack = Math.min(512, remaining);
                            dIS.read(bFile, 0, pack);
                            fileOPstream.write(bFile, 0, pack);
                            progress += 1;
                            remaining -= pack;
                            /* Updates progress bar */
                            receiverPB.setValue((int) ((progress * 100) / packTotal));
                            receiverPB.repaint();
                            receiverPB.validate();
                        }
                        /* Sends file stream */
                        fileOPstream.flush();
                        fileOPstream.close();
                        System.out.println("DONE");
                        dIS.close();
                        serverSocket.close();
                    }
                } catch (Exception ignore) {
                }
            }
        }).start();
    }

    static int totalNumberOfPacketsToReceive;
    static int packetSizeToReceive;

    /**
     * This method receives the selected file using RBUDP.
     */
    public static void receiveFileRBUDP() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                myProgressBar();
                new Receiver(whoAmI, receivedFileName, totalNumberOfPacketsToReceive, packetSizeToReceive, receiverPB);
            }
        }).start();

    }

    static String hostReceiverIP = "127.0.0.1";
    static int portUDPReceiver = 9876;
    static ObjectOutputStream outputStreamSenderTCP;
    static ObjectInputStream inputStreamSenderTCP;

    public static int packetSize = 65400;
    public static final double pPacketLost = 0.0000001;
    public static int blastSize = 5;

    static int sqNumLS = 0;
    static int sqNumAck = 0;
    static int numSqPackets;
    static byte[] fileByteArraySender;
    static ArrayList<RDTPacket> packetsSent;
    static InetAddress receiverAddrSender;
    static DatagramSocket receiverDSSender;
    static boolean stop = false;

    /**
     * This method sends the file using RBUDP
     * 
     * @param f the selected file to send
     */
    public void sendFileRBUDP(File f) {
        Socket socketTCP;
        try {
            /* Waits for receiver to connect */
            socketTCP = (new ServerSocket(9999)).accept();
            socketTCP.setSoTimeout(30);
            outputStreamSenderTCP = new ObjectOutputStream(socketTCP.getOutputStream());
            inputStreamSenderTCP = new ObjectInputStream(socketTCP.getInputStream());
            fileByteArraySender = fileToByteArray(f);
            System.out.println("Data size: " + fileByteArraySender.length + " bytes");
            numSqPackets = (int) Math.ceil((double) fileByteArraySender.length / packetSize);
            System.out.println("Number of packets to send: " + numSqPackets);
            packetsSent = new ArrayList<RDTPacket>();
            try {
                receiverDSSender = new DatagramSocket();
                receiverAddrSender = InetAddress.getByName(hostReceiverIP);
                while (!stop) {
                    sending();
                    ack();
                }
                System.out.println("Finished Transmission");
                /* Closes the sockets and Streams */
                try {
                    if (socketTCP != null) {
                        socketTCP.close();
                    }
                    if (outputStreamSenderTCP != null) {
                        outputStreamSenderTCP.close();
                    }
                    if (inputStreamSenderTCP != null) {
                        inputStreamSenderTCP.close();
                    }
                    if (receiverDSSender != null) {
                        receiverDSSender.close();
                    }
                } catch (Exception e) {
                }
            } catch (Exception e) {

            }
        } catch (Exception e) {
            chatBoxTA.setText("FAILED TO SEND FAIL");
        }
    }

    /**
     * This method sends packets using UDP.
     */
    public static void sending() throws IOException {
        /* Sends through the packets */
        while (sqNumLS - sqNumAck < blastSize && sqNumLS < numSqPackets) {
            byte[] filePacketBytes = new byte[packetSize];
            filePacketBytes = Arrays.copyOfRange(fileByteArraySender, sqNumLS * packetSize,
                    sqNumLS * packetSize + packetSize);
            RDTPacket rdtPacketObject = new RDTPacket(sqNumLS, filePacketBytes,
                    (sqNumLS == numSqPackets - 1) ? true : false);
            byte[] sendData = Serializer.toBytes(rdtPacketObject);
            DatagramPacket packet = new DatagramPacket(sendData, sendData.length, receiverAddrSender, portUDPReceiver);
            System.out.println("Sending packet with sequence number: " + sqNumLS);
            packetsSent.add(rdtPacketObject);
            if (Math.random() > pPacketLost) {
                receiverDSSender.send(packet);
            } else {
                System.out.println("XXX Lost packet: " + sqNumLS);
            }
            sqNumLS++;
        }
    }

    /**
     * This method waits for acknowledgements from sent packets.
     */
    public static void ack() throws ClassNotFoundException, IOException {
        /* Gets acknowledgements */
        try {
            receiverDSSender.setSoTimeout(20);
            RDTAck ackObject = (RDTAck) inputStreamSenderTCP.readObject();
            System.out.println("Received ACK: " + ackObject.getPacket());
            if (ackObject.getPacket() == numSqPackets) {
                stop = true;
            }
            sqNumAck = Math.max(sqNumAck, ackObject.getPacket());
        } catch (SocketTimeoutException e) {
            for (int i = sqNumAck; i < sqNumLS; i++) {
                byte[] sendData = Serializer.toBytes(packetsSent.get(i));
                DatagramPacket packet = new DatagramPacket(sendData, sendData.length, receiverAddrSender,
                        portUDPReceiver);
                if (Math.random() > pPacketLost) {
                    receiverDSSender.send(packet);
                } else {
                    System.out.println("XXX Lost packet: " + packetsSent.get(i).getSeq());
                }
            }
        }
    }

    /**
     * This method reads in bytes from the file from an InputStream.
     * 
     * @param fileToSend the selected file to send
     * @return byte array
     */

    public static byte[] fileToByteArray(File fileToSend) {
        /* Converts file to byte array */
        FileInputStream fileInputStream = null;
        byte[] byteArray = new byte[(int) fileToSend.length()];
        try {
            fileInputStream = new FileInputStream(fileToSend);
            fileInputStream.read(byteArray);
            fileInputStream.close();
        } catch (IOException ioExp) {
        }
        return byteArray;
    }

}
