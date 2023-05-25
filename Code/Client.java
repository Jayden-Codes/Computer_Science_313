
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.math.BigInteger;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;

import javax.crypto.Cipher;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.DimensionUIResource;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;

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
    private static String whoAmI;
    private static Client client;
    private static String hostTCP = "127.0.0.1";
    private static int portTCP = 7777;

    private static String myIPAddress = "127.0.0.5";

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
    public static void main(String[] args) {
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

    /**
     * Finds users Hamachi ip address.
     * 
     * @return - ip address.
     */
    public static String findIP() {
        String ip = "127.0.0.1";
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface netIface = interfaces.nextElement();
                if (netIface.isLoopback() || !netIface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> addresses = netIface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    String temp = addresses.nextElement().getHostAddress();
                    /* Look for Hamachi adapter */
                    if (netIface.getDisplayName().contains("LogMeIn Hamachi Virtual Ethernet Adapter")) {
                        ip = temp;
                        break;
                    }
                }
            }
        } catch (Exception e) {
        }
        return ip;
    }

    private static JFrame startUpFrame = new JFrame("Client");
    private static JTextField serverIPTF = new JTextField(15);
    private static JTextField portNumTF = new JTextField(15);
    private static JTextField userIPAdressTF = new JTextField(15);

    private static JTextField usernameTF;

    /**
     * The method that creates the startup window GUI.
     */
    public static void startUpWindow() {
        serverIPTF.setText("127.0.0.1");
        portNumTF.setText("7777");
        userIPAdressTF.setText(findIP());
        JPanel startPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        JButton connectBtn = new JButton("Connect!");
        constraints.gridy = 0;
        constraints.gridx = 0;
        startPanel.add(new JLabel("Server IP: "), constraints);
        constraints.gridx = 1;
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
                if (whoAmI.strip().length() < 1) {
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
        usernameTF.requestFocusInWindow();
        startUpFrame.setLocationRelativeTo(null);
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

    private static BigInteger sxKey;
    private static BigInteger G;
    private static BigInteger P;
    private static BigInteger N;
    private static BigInteger R;

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

                        Packet packetReceived = (Packet) objectInputStreamTCP.readObject();
                        if (packetReceived.type.equals("#CONNECTIONVALID")) {
                            P = packetReceived.getBigP();
                            G = packetReceived.getBigG();
                            sxKey = new BigInteger(4096, new Random());
                            N = G.modPow(sxKey, P);
                            whoAmI = usernameTF.getText();
                            makeFolders();
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

                            String raw = new String(packetReceived.getEMessage());
                            byte[] privBytes = packetReceived.getHashKey();
                            byte[] unhashed = hasher(privBytes);
                            KeyFactory kf = KeyFactory.getInstance("RSA");
                            EncodedKeySpec eks = new PKCS8EncodedKeySpec(unhashed);
                            PrivateKey privKey = kf.generatePrivate(eks);
                            String decrypt = decryptString(raw, privKey);
                            chatBoxTA.append(packetReceived.from + ": " + decrypt + "\n");
                        }  else if (packetReceived.type.equals("#SEARCH")) {

                            File sFile = new File("./" + whoAmI + "/Shared");
                            File[] sFiles = sFile.listFiles();
                            ArrayList<String> filestr = new ArrayList<String>();
                            if (sFiles != null) {
                                ArrayList<String> fileNamesFound = new Search(packetReceived.getMessage(), sFiles)
                                        .searcher();
                                for (String f : fileNamesFound) {
                                    filestr.add("@" + whoAmI + " " + f + " /"
                                            + (new File(dirPathShared + f)).length());
                                }
                            }
                            if (filestr.size() > 0) {
                                Packet pkS = new Packet(whoAmI, packetReceived.getFrom(), "#SEARCHRES", filestr,
                                        packetReceived.getID());
                                client.writer(pkS);
                            }
                        } else if (packetReceived.type.equals("#SEARCHRES")) {
                            if (searchId == packetReceived.getID()) {
                                ArrayList<String> files = packetReceived.getGroupMemberNames();
                                for (String f : files) {
                                    fileModel.addElement(f);
                                }
                                searchList.setModel(fileModel);
                                searchList.repaint();
                                searchList.validate();
                            }
                        } else if (packetReceived.type.equals("#DOWNLOAD")) {

                            if (packetReceived.getMessage().equals("YES")) {

                                chatBoxTA.append("Sending on port: " + packetReceived.getFileSharePort() + "\n");
                                new sender(chatFrame, "SENDER", packetReceived.getIpAddress(),
                                        packetReceived.getFileSharePort(),
                                        packetReceived.getDir(), client);
                            }

                        } else if (packetReceived.type.equals("#DOWNLOADREQ")) {
                            if (canUpload) {
                                File ftos = new File(dirPathShared + packetReceived.getMessage());
                                if (ftos.exists()) {
                                    R = (packetReceived.getBigN()).modPow(sxKey, P);
                                    canUpload = false;
                                    Packet pk = new Packet(whoAmI, packetReceived.from, "#DOWNLOADRES",
                                            "CANSEND");
                                    pk.setBigR(R);
                                    pk.setBigN(N);
                                    pk.setDir(dirPathShared + packetReceived.getMessage());
                                    pk.setIpAddress(packetReceived.getIpAddress());
                                    client.writer(pk);
                                } else {
                                    Packet pk = new Packet(whoAmI, packetReceived.from, "#DOWNLOADRES",
                                            "FILEMISSING");
                                    client.writer(pk);
                                }

                            } else {
                                Packet pk = new Packet(whoAmI, packetReceived.from, "#DOWNLOADRES", "CANTSEND");
                                client.writer(pk);
                            }

                        } else if (packetReceived.type.equals("#DOWNLOADRES")) {
                            if (packetReceived.getMessage().equals("CANSEND")) {
                                BigInteger myR = packetReceived.getBigN().modPow(sxKey, P);
                                if (myR.compareTo(packetReceived.getBigR()) == 0) {
                                    System.out.println("they are equal");
                                    int pNum = portGenerator();
                                    chatBoxTA.append("Receiving on port: " + pNum+ "\n");
                                    Packet pk = new Packet(whoAmI, packetReceived.from, "#DOWNLOAD",
                                            "YES");
                                    pk.setFileSharePort(pNum);
                                    pk.setDir(packetReceived.getDir());
                                    pk.setIpAddress(packetReceived.getIpAddress());
                                    client.writer(pk);
                                    new receiver("RECEIVER", chatFrame, pNum,
                                            dirPathDownloads).receiveFileTCP(client);
                                    ;
                                } else {
                                    chatBoxTA.append("ABORT: CONNECTION NOT SECURE\n");
                                }
                            } else if (packetReceived.getMessage().equals("FILEMISSING")) {
                                chatBoxTA.append("FILE NOT AVAILABLE\n");
                            } else {
                                chatBoxTA.append("CAN'T SEND\n");

                            }
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
                                    list.clearSelection();
                                }
                            });
                            cgList.setModel(model);
                            cgList.repaint();
                            cgList.validate();
                            if (hasFile) {
                                chatFrame.repaint();
                                chatFrame.validate();
                            }
                        }
                    }
                } catch (Exception e) {
                    chatBoxTA.append("SERVER DISCONNECTED\n");
                    sendMessageBtn.setEnabled(false);
                    searchBtn.setEnabled(false);
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
    private static JLabel warning = new JLabel("");
    private static JTextField onlineTF = new JTextField("ACTIVE USERS");
    private static JPanel sidePanel = new JPanel(new BorderLayout());

    private static DefaultListModel<String> model = new DefaultListModel<>();
    private static JList<String> list = new JList<>(model);
    private static JList<String> cgList = new JList<>(model);

    private static JButton searchBtn;
    private static JTextField searchTF;

    private static DefaultListModel<String> fileModel = new DefaultListModel<>();

    private static int searchId = 0;

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
                    /* don't do anything */
                } else {
                    String message = messageBoxTF.getText();
                    chatBoxTA.append("YOU: " + message + "\n");
                    messageBoxTF.setText("");

                    PublicKey pub;
                    PrivateKey priv;
                    String encrypted;

                    try {
                        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
                        gen.initialize(2048);
                        KeyPair pair = gen.generateKeyPair();
                        pub = pair.getPublic();
                        priv = pair.getPrivate();
                        encrypted = encryptString(message, pub);
                        byte[] privBytes = priv.getEncoded();
                        byte[] hashed = hasher(privBytes);

                        Packet pk1 = new Packet(whoAmI, "SERVER", "#MESSAGE", encrypted.getBytes(), hashed);
                        client.writer(pk1);

                    } catch (Exception ex) {
                    }
                }
                messageBoxTF.requestFocusInWindow();
            }
        });

     
        chatBoxTA = new JTextArea();
        chatBoxTA.setEditable(false);
        chatBoxTA.setFont(new Font("MONO", Font.TRUETYPE_FONT, 15));
        chatBoxTA.setLineWrap(true);

        JMenuBar menuBar = new JMenuBar();

        /* Menu items */
        JMenu menu = new JMenu("Menu");

        JMenuItem fileViewItem = new JMenuItem("Shared Viewer");
        fileViewItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileExplorer("Shared File Viewer -" + whoAmI, dirPathShared);
            }
        });
        fileViewItem.setIconTextGap(10);
        menu.add(fileViewItem);

        JMenuItem fileViewDownloadsItem = new JMenuItem("Downloads Viewer");
        fileViewDownloadsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileExplorer("Downloads File Viewer -" + whoAmI, dirPathDownloads);
            }
        });
        fileViewDownloadsItem.setIconTextGap(10);
        menu.add(fileViewDownloadsItem);

        /* launches upload gui */
        JMenuItem uploadMenuBtn = new JMenuItem("Upload");
        uploadMenuBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                uploadGUI();
            }
        });
        uploadMenuBtn.setIconTextGap(10);
        menu.add(uploadMenuBtn);

        /* clears the chat */
        JMenuItem clearChatItem = new JMenuItem("Clear Chat");
        clearChatItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chatBoxTA.setText("Cleared all messages\n");
                messageBoxTF.setText("");
            }
        });
        clearChatItem.setIconTextGap(10);
        menu.add(clearChatItem);
        menu.addSeparator();

        /* OFF CLIENT */
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chatBoxTA.append("You left.\n");
                offTheClient();
            }
        });
        exitItem.setIconTextGap(10);
        menu.add(exitItem);

        menuBar.add(menu);

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchTF = new JTextField("");

        /* Search TextField hint */
        searchTF.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchTF.getText().equals("Search for something...")) {
                    searchTF.setText("");
                    searchTF.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchTF.getText().isEmpty()) {
                    searchTF.setForeground(Color.GRAY);
                    searchTF.setText("Search for something...");
                }
            }
        });
        searchBtn = new JButton("Search");
        searchBtn.setEnabled(false);

        searchTF.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkEnabled();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkEnabled();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkEnabled();
            }

            /*
             * Check search input length.
             */
            private void checkEnabled() {
                if (searchTF.getText().trim().length() > 1) {
                    searchBtn.setEnabled(true);
                } else {
                    searchBtn.setEnabled(false);
                }
            }
        });
        searchPanel.add(searchTF, BorderLayout.CENTER);
        searchBtn.addActionListener(new ActionListener() {
            /* Perform search when clicked */
            @Override
            public void actionPerformed(ActionEvent e) {
                fileModel = new DefaultListModel<>();
                searchList.repaint();
                searchList.validate();
                searchGui(searchTF);
                searchId = portGenerator();
                Packet pk2 = new Packet(whoAmI, "SERVER", "#SEARCH", searchTF.getText(), searchId);
                client.writer(pk2);
            }

        });
        searchPanel.add(searchBtn, BorderLayout.EAST);

        onlineTF.setEditable(false);
        onlineTF.setHorizontalAlignment(0);
        onlineTF.setBorder(
                BorderFactory.createCompoundBorder(onlineTF.getBorder(), BorderFactory.createEmptyBorder(2, 0, 3, 0)));
        sidePanel.add(onlineTF, BorderLayout.NORTH);

        list.setFixedCellWidth(186);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        cgList.setFixedCellWidth(186);
        cgList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        cgList.setLayoutOrientation(JList.VERTICAL);

        searchList.setFixedCellWidth(186);
        searchList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        searchList.setLayoutOrientation(JList.VERTICAL);

        JScrollPane scrollPane = new JScrollPane(list);
        sidePanel.add(scrollPane, BorderLayout.CENTER);

        chatFrame.add(menuBar, new BorderLayout().NORTH);
        chatboxPanel.add(new JScrollPane(chatBoxTA), BorderLayout.CENTER);
        constraint.fill = GridBagConstraints.HORIZONTAL;
        constraint.weightx = 550.0D;
        chatboxPanel.add(searchPanel, BorderLayout.NORTH);
        bottomPanel.add(messageBoxTF, constraint);
        bottomPanel.add(sendMessageBtn, new GridBagConstraints());
        chatFrame.add(chatboxPanel, BorderLayout.CENTER);
        chatFrame.add(BorderLayout.SOUTH, bottomPanel);

        chatFrame.add(new JScrollPane(sidePanel), BorderLayout.EAST);
        chatFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chatFrame.getRootPane().setDefaultButton(sendMessageBtn);
        chatFrame.setMinimumSize(new DimensionUIResource(505, 300));
        chatFrame.setVisible(true);
        chatFrame.setLocationRelativeTo(null);
        messageBoxTF.requestFocus();
    }

    private static JDialog searchDialog;
    static JList<String> searchList = new JList<String>();

    private static JButton downloadBtn;

    /**
     * Group creation gui.
     */
    public static void searchGui(JTextField searchtf) {

        searchDialog = new JDialog(chatFrame, "Select file to download: " + whoAmI);

        JPanel sPanel = new JPanel(new BorderLayout());
        JTextField sTF = new JTextField(searchtf.getText());
        sPanel.add(sTF, BorderLayout.CENTER);

        JButton sBtn = new JButton("Search");
        sBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileModel = new DefaultListModel<>();
                searchList.setModel(fileModel);
                searchDialog.repaint();
                searchDialog.validate();
                searchId = portGenerator();
                Packet pk2 = new Packet(whoAmI, "SERVER", "#SEARCH", sTF.getText(), searchId);
                client.writer(pk2);

            }
        });
        sPanel.add(sBtn, BorderLayout.EAST);

        sTF.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkEnabled();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkEnabled();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkEnabled();
            }

            /* 
             * Check search input length.
             */
            private void checkEnabled() {
                if (sTF.getText().trim().length() > 1) {
                    sBtn.setEnabled(true);
                } else {
                    sBtn.setEnabled(false);
                }
            }
        });
        searchDialog.add(sPanel, BorderLayout.NORTH);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;

        JScrollPane scrollPane = new JScrollPane(searchList);
        searchDialog.repaint();
        searchDialog.validate();
        searchList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int[] selectedIndices = searchList.getSelectedIndices();
                if (selectedIndices.length > 0) {
                    downloadBtn.setEnabled(true);
                } else {
                    downloadBtn.setEnabled(false);
                }
            }
        });
        downloadBtn = new JButton("Download");
        downloadBtn.addActionListener(new ActionListener() {
            /*
             * Action listener for create group menu button
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                if (searchList.getSelectedValuesList().size() > 0) {
                    if (canDownload) {
                        String to = searchList.getSelectedValuesList().get(0).split("@")[1].split(" ", 2)[0];
                        String fStr = searchList.getSelectedValuesList().get(0).split(" ", 2)[1].split(" /")[0];
                        /* Generate key pair */
                        KeyPairGenerator keyPairGenerator;
                        try {
                            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                            keyPairGenerator.initialize(2048);
                            KeyPair keyPair = keyPairGenerator.generateKeyPair();
                            /* Get public and private keys */
                            PrivateKey privateKey = keyPair.getPrivate();
                            PublicKey publicKey = keyPair.getPublic();
                            Packet pk = new Packet(whoAmI, to, "#DOWNLOADREQ", fStr, publicKey); /* +key */
                            pk.setBigN(N);
                            System.out.println("N res:" + N.toString());

                            client.writer(pk);
                        } catch (Exception e1) {
                        }
                        searchDialog.dispose();
                        searchList.clearSelection();
                    } else {
                        chatBoxTA.append("CAN'T DOWNLOAD.");
                    }

                }
            }
        });

        searchDialog.add(scrollPane, BorderLayout.CENTER);
        searchDialog.getRootPane().setDefaultButton(downloadBtn);
        searchDialog.add(downloadBtn, BorderLayout.SOUTH);
        searchDialog.setMinimumSize(new DimensionUIResource(400, 200));
        searchDialog.setVisible(true);
        searchDialog.setLocationRelativeTo(chatFrame);
        searchDialog.addWindowListener(new WindowAdapter() {
            /*
             * Dispose of search GUI
             */
            public void windowClosing(WindowEvent e) {
                searchDialog.repaint();
                searchDialog.validate();
                searchDialog.dispose();
                searchtf.setText(sTF.getText());
                searchTF.repaint();
                searchTF.validate();
                chatFrame.repaint();
                chatFrame.validate();
            }
        });
        sBtn.doClick();
    }

    /**
     * set canDownload
     * 
     * @param b - Boolean to set.
     */
    public void setCanDownload(boolean b) {
        canDownload = b;
    }

    /**
     * set canUpload
     * 
     * @param b - Boolean to set.
     */
    public void setUpload(boolean b) {
        canUpload = b;
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
        fileVFrame.setLocationRelativeTo(chatFrame);

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
                        /* Open file */
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
            /**
             * Window frame closer.
             * 
             * @param e - Event.
             */
            public void windowClosing(WindowEvent e) {
                fileVFrame.dispose();
            }
        });
    }

    public static boolean canUpload = true;
    public static boolean canDownload = true;

    /**
     * Move file over to folder.
     * 
     * @param srcPath  - Directory Path of source.
     * @param destPath - Directory Path of destination.
     * @param fileName - File name
     * @param dialog   - JDialog
     */
    public static void mover(String srcPath, String destPath, String fileName, JDialog dialog) {
        File src = new File(srcPath);

        File dest = new File(destPath + File.separator + fileName);
        if (dest.exists()) {
            JOptionPane.showMessageDialog(dialog, "File Already Present");
        } else {
            try {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int fSize = (int) src.length();
                        int remaining = fSize;
                        double progress = 0;
                        double packTotal = fSize / 10024;
                        while (remaining > 0) {
                            int pack = Math.min(10024, remaining);
                            progress += 1;
                            remaining -= pack;
                            /* Updates progress bar */
                            uploadPB.setValue((int) ((progress * 100) / packTotal));

                        }

                        uploadPB.setValue(100);
                        JOptionPane.showMessageDialog(dialog, fileName + " added!");
                        uploadPB.setValue(0);
                    }
                }).start();
                /* Perform copy of file */
                Files.copy(src.toPath(), dest.toPath());

            } catch (Exception e) {
                JOptionPane.showMessageDialog(dialog, "UPLOAD FAILED: " + fileName + " NOT added!");
            }
        }

    }

    static JProgressBar uploadPB;
    static JDialog dialogUploadGUI;

    /**
     * GUI for Upload Area
     */
    public static void uploadGUI() {
        dialogUploadGUI = new JDialog(chatFrame, "Upload -" + whoAmI);
        dialogUploadGUI.setLocationRelativeTo(chatFrame);

        uploadPB = new JProgressBar();
        uploadPB.setStringPainted(true);
        uploadPB.setBorder(new LineBorder(Color.DARK_GRAY));
        uploadPB.setForeground(Color.green);
        uploadPB.setValue(0);
        dialogUploadGUI.add(uploadPB, BorderLayout.NORTH);

        JPanel centrePanel = new JPanel(new BorderLayout());
        JLabel txt = new JLabel("Drag and drop files");
        txt.setHorizontalAlignment(0);
        JPanel sidePanel = new JPanel(new BorderLayout());
        JTextArea fileTA = new JTextArea(5, 20);
        fileTA.setText(updateFileList(dirPathShared));

        fileTA.setFont(new Font("Source Code Pro", Font.BOLD, 12));
        fileTA.setLineWrap(true);
        fileTA.setWrapStyleWord(true);
        fileTA.setEditable(false);
        sidePanel.add(new JScrollPane(fileTA));
        dialogUploadGUI.add(sidePanel, BorderLayout.EAST);
        centrePanel.add(txt, new BorderLayout().CENTER);
        centrePanel.setBackground(new Color(0, 134, 145, 123));
        /* For drag and drop functionality */
        centrePanel.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> dropFiles = (List<File>) evt.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);
                    for (File file : dropFiles) {
                        uploadPB.setValue(0);
                        mover(file.getAbsolutePath(), dirPathShared, file.getName(), dialogUploadGUI);
                        /* Update file list */
                        fileTA.setText(updateFileList(dirPathShared));
                        dialogUploadGUI.repaint();
                        dialogUploadGUI.validate();
                    }
                } catch (Exception ex) {
                }

                sidePanel.validate();
                sidePanel.repaint();
                dialogUploadGUI.validate();
                dialogUploadGUI.repaint();

            }
        });
        dialogUploadGUI.add(centrePanel);
        JButton uploadBtn = new JButton("Upload");
        uploadBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setMultiSelectionEnabled(false);
                if (chooser.showOpenDialog(dialogUploadGUI) == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    mover(file.getAbsolutePath(), dirPathShared, file.getName(), dialogUploadGUI);
                    fileTA.setText(updateFileList(dirPathShared));
                }
            }
        });
        dialogUploadGUI.add(uploadBtn, BorderLayout.SOUTH);
        dialogUploadGUI.setVisible(true);
        dialogUploadGUI.setMinimumSize(new Dimension(450, 300));

    }

    /**
     * The function takes a directory path as input and returns a string containing
     * the names of all
     * the files in that directory.
     * 
     * @param dir The parameter "dir" is a String representing the directory path
     *            for which we want to
     *            list the files.
     * @return The method `updateFileList` returns a string containing the names of
     *         all the files in
     *         the directory specified by the input parameter `dir`. The names are
     *         separated by a newline
     *         character and a line of asterisks.
     */
    public static String updateFileList(String dir) {
        File directory = new File(dir);
        File[] files = directory.listFiles();
        String fileNames = "";
        for (File file : files) {
            if (file.isFile()) {
                fileNames += file.getName() + "\n**********\n";
            }
        }
        return fileNames;
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

   
    private static String dirPathDownloads;
    private static String dirPathShared;

    /**
     * Makes folders to save voice notes into
     * 
     * @param whoAmI - username
     */
    public static void makeFolders() {
        dirPathDownloads = "./" + whoAmI + "/Downloads/";
        dirPathShared = "./" + whoAmI + "/Shared/";
   
        if (!(new File(dirPathDownloads)).exists()) {
            new File(dirPathDownloads).mkdirs();
        }
        if (!(new File(dirPathShared)).exists()) {
            new File(dirPathShared).mkdirs();
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
     * Simple encrypt String method
     * Takes string and public key as input
     *
     * @return returns encrypted string of different length of input
     */
    public static String encryptString(String str, PublicKey key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] bytes = cipher.doFinal(str.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
        }
        return null;

    }

    /**
     * Simple decrypt String method
     * Takes string and private key as input
     *
     * @return returns decrypted string of different length of input
     */
    public static String decryptString(String str, PrivateKey key) {
        try {
            byte[] bytes = Base64.getDecoder().decode(str);
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            bytes = cipher.doFinal(bytes);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * Hasher function.
     * 
     * @param key - byte array to be hashed.
     * @return - hashed byte array.
     */
    public static byte[] hasher(byte[] key) {
        int num_of_changes = 1;
        float change_point = 0.5f;
        float last_change = 0.5f;
        int len = key.length;
        int f1 = 1;
        int f2 = 1;
        for (int i = 0; i < key.length; i++) {
            if (i > (int) (change_point * (float) len)) {
                last_change = last_change / 2.f;
                change_point = change_point + last_change;
                num_of_changes++;
            }
            if (num_of_changes % 2 == 1) {
                key[i] = (byte) (key[i] ^ (byte) 1);
            }
            if (i == f2) {
                key[i] = (byte) (key[i] ^ (byte) 1);
                f2 = f2 + f1;
                f1 = f2 - f1;
            }
        }
        return key;
    }

}
