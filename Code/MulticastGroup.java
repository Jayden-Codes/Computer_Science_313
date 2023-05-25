import java.net.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.plaf.DimensionUIResource;

import java.util.Date;
import java.util.Random;

/**
 * This MulticastGroup Class creates the conference channels which will be used
 * for conference calls and messages.
 * 
 * @author Jayden Abrahams – 23559322@sun.ac.za
 * @author Clinton Elves - 24007676@sun.ac.za
 * @author Cindi-Ann Jeffery – 24695823@sun.ac.za
 * @author Konanani Mathoho – 24774898@sun.ac.za
 * @author Langton Chikukwa – 23607769@sun.ac.za
 */
public class MulticastGroup {

   /**
    * @param creator   the creator of the group
    * @param name      the name of the creator
    * @param ip        the IP address that the group will be using
    * @param portNum   the port number the group will be using
    * @param groupName the name of the group
    */

   public MulticastGroup(String creator, String name, String ip, int portNum, String groupName) {

      /* Assigning values to GUI attributes */
      JTextArea chatBoxTA = new JTextArea();
      chatBoxTA.setEditable(false);
      chatBoxTA.setFont(new Font("MONO", Font.TRUETYPE_FONT, 15));
      chatBoxTA.setLineWrap(true);
      JFrame chatFrame = new JFrame("Group-" + groupName + ": " + name);
      chatBoxTA.append("Created by " + creator + "\n");
      groupListen(name, ip, portNum, chatFrame, chatBoxTA);

      /* Opens a new Chat Window */
      chatWindow(name, chatFrame, "Group-" + groupName, ip, portNum, chatBoxTA);

   }

   /**
    * Generates a random IP address.
    * 
    * @return a random IP address
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
    * Generates a random port number.
    * 
    * @return a random port number
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
    * This method allows clients to send data via a multicast socket to the group
    * 
    * @param ipAddr  the group IP address
    * @param portNum the group port number
    * @param buf     the byte array to send data
    */
   public static void groupSend(String ipAddr, int portNum, byte[] buf) {
      try {

         /* Setting up streams */
         InetAddress group = InetAddress.getByName(ipAddr);
         MulticastSocket socket = new MulticastSocket();
         DatagramPacket packet = new DatagramPacket(buf, buf.length, group, portNum);

         /* Sending Packet */
         socket.send(packet);
         socket.close();
      } catch (Exception e) {
      }
   }

   /**
    * This method allows clients to receive data via a multicast socket to the
    * group
    * 
    * @param whoAmI       the sender of the packet
    * @param groupIpAddr  the group IP address
    * @param groupPortNum the group port number
    * @param frame        the group GUI frame
    * @param chatBoxTA    the group textbox
    */
   public static void groupListen(String whoAmI, String groupIpAddr, int groupPortNum, JFrame frame,
         JTextArea chatBoxTA) {
      new Thread(new Runnable() {
         @Override
         public void run() {
            try {
               /* Set up socket */
               InetAddress group = InetAddress.getByName(groupIpAddr);
               MulticastSocket socket = new MulticastSocket(groupPortNum);
               socket.joinGroup(group);
               while (true) {

                  /* Receive packet */
                  byte[] buf = new byte[1024];
                  DatagramPacket packet = new DatagramPacket(buf, buf.length);
                  socket.receive(packet);

                  /* Analyse packet  received */
                  Packet pk = (Packet) Serializer.toObject(packet.getData());
                  if (!pk.from.equals(whoAmI)) {
                     if (pk.type.equals("#MESSAGE")) {
                        chatBoxTA.append(pk.from + ": " + pk.message + "\n");
                     } else if (pk.type.equals("#USERLISTREQ")) {
                        Packet pk1 = new Packet(whoAmI, pk.from, "#USERLISTRES");
                        groupSend(groupIpAddr, groupPortNum, Serializer.toBytes(pk1));

                     } else if (pk.type.equals("#USERLISTRES")) {
                        if (pk.to.equals(whoAmI)) {
                           chatBoxTA.append("-" + pk.from + "\n");
                        }
                     } else if (pk.type.equals("#STARTSENDINGVN")) {
                        vnReceiver(whoAmI, pk.from, frame, chatBoxTA, pk.getGroupIP(), pk.getGroupPortNum(),
                              pk.getFileName(), pk.getFileLength());
                        Packet pk1 = new Packet(whoAmI, "ALL", "#START", pk.getGroupIP(), pk.getGroupPortNum(),
                              pk.getFileName(), pk.getFileLength());
                        groupSend(groupIpAddr, groupPortNum, Serializer.toBytes(pk1));
                     } else if (pk.type.equals("#START")) {
                        vnSender(pk.getGroupIP(), pk.getGroupPortNum(),
                              new File("./Voice_Notes_" + whoAmI + "/Sent/" + pk.getFileName()), chatBoxTA);
                     } else {
                        String received = new String(packet.getData(), 0, packet.getLength());
                        System.out.println("Received: " + received);
                        socket.leaveGroup(group);
                        socket.close();
                     }
                  }
               }

            } catch (Exception e) {
            }
         }
      }).start();

   }

   /**
    * The chatbox displayed in the GUI
    * 
    * @param whoAmI       the sender of the packet
    * @param chatFrame    the group GUI frame
    * @param programName  the name of the program
    * @param groupIpAddr  the group IP address
    * @param groupPortNum the group port number
    * @param chatBoxTA    the group textbox
    */

   public void chatWindow(String whoAmI, JFrame chatFrame, String programName, String groupIpAddr, int groupPortNum,
         JTextArea chatBoxTA) {

      /* Set attributes for the chat window GUI */
      chatBoxTA.append("Group IP: " + groupIpAddr + "\nGroup Port: " + groupPortNum + "\n");
      GridBagConstraints constraint = new GridBagConstraints();
      JPanel chatboxPanel = new JPanel();
      JPanel bottomPanel = new JPanel();
      chatFrame.setTitle(programName + ": " + whoAmI);
      chatboxPanel.setLayout(new BorderLayout());
      bottomPanel.setBackground(Color.LIGHT_GRAY);
      bottomPanel.setLayout(new GridBagLayout());
      JTextField messageBoxTF = new JTextField(30);
      messageBoxTF.requestFocusInWindow();
      JButton sendMessageBtn = new JButton("Send Message");

      /* Implement all the button functionality */
      sendMessageBtn.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {   /* Clearing all messages */
            if (messageBoxTF.getText().length() < 1) {
               // don't do anything
            } else if (messageBoxTF.getText().equals("#CLEAR")) {
               chatBoxTA.setText("Cleared all messages\n");
               messageBoxTF.setText("");
            } else {
               String message = messageBoxTF.getText();
               chatBoxTA.append("YOU: " + message + "\n");
               messageBoxTF.setText("");

               Packet pk1 = new Packet(whoAmI, "ALL", "#MESSAGE", message);

               groupSend(groupIpAddr, groupPortNum, Serializer.toBytes(pk1));

            }
            messageBoxTF.requestFocusInWindow();
         }

      });

      JButton recordBtn = new JButton("Record");    /* recording voice note */
      recordBtn.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            recordVN(whoAmI, programName, groupIpAddr, groupPortNum, chatFrame, chatBoxTA);
         }
      });

      JMenuBar menuBar = new JMenuBar();
      JMenu menu = new JMenu("Menu");

      JMenuItem commmandsMenuBtn = new JMenuItem("Commands");
      commmandsMenuBtn.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            chatBoxTA.append(

                  "********************COMMANDS*************************\n" +
                        "* Clear chat: #CLEAR                                                    *\n" +

                        "***********************************************************\n");
         }
      });
      menu.add(commmandsMenuBtn);

      JMenuItem userListBtn = new JMenuItem("Active Users");
      userListBtn.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {    /* Get update user list */
            chatBoxTA.append("**********ACTIVE USERS**********\n");
            Packet pk1 = new Packet(whoAmI, "ALL", "#USERLISTREQ");
            groupSend(groupIpAddr, groupPortNum, Serializer.toBytes(pk1));

         }
      });
      menu.add(userListBtn);

      /* Setting more GUI attributes for chat window */
      menuBar.add(menu);
      chatFrame.add(menuBar, new BorderLayout().NORTH);
      chatboxPanel.add(new JScrollPane(chatBoxTA), BorderLayout.CENTER);
      constraint.fill = GridBagConstraints.HORIZONTAL;
      constraint.weightx = 550.0D;
      bottomPanel.add(messageBoxTF, constraint);
      bottomPanel.add(sendMessageBtn, new GridBagConstraints());
      bottomPanel.add(recordBtn, new GridBagConstraints());
      chatFrame.add(chatboxPanel, BorderLayout.CENTER);
      chatFrame.add(BorderLayout.SOUTH, bottomPanel);

      chatFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      chatFrame.addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent e) {
            chatFrame.dispose();
         }
      });
      chatFrame.getRootPane().setDefaultButton(sendMessageBtn);
      chatFrame.setMinimumSize(new DimensionUIResource(505, 300));
      chatFrame.setVisible(true);

   }

   /**
    * This method generates folders to be used for voicenotes.
    * 
    * @param whoAmI The current client
    */
   public static void makeFolders(String whoAmI) {

      /* If path does not exist make directories */
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
    * This method allows voicenotes to be recorded to be sent via the group.
    * 
    * @param whoAmI       the sender of the packet
    * @param chatFrame    the group GUI frame
    * @param programName  the name of the program
    * @param groupIpAddr  the group IP address
    * @param groupPortNum the group port number
    * @param chatBoxTA    the group textbox
    */
   public static void recordVN(String whoAmI, String programName, String groupIpAddr, int groupPortNum, JFrame frame,
         JTextArea chatBoxTA) {
      try {
         /* Formatting Audio */
         AudioFormat af = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44000, 16, 2, 4, 22000, false);
         DataLine.Info datainfo = new DataLine.Info(TargetDataLine.class, af);

         int option = JOptionPane.showOptionDialog(frame, "Press OK to record.", "Record", 2, 1, null, null, null);
         if (option == 0) {

            /* Initialise the target line */
            makeFolders(whoAmI);
            TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(datainfo);
            targetLine.open();
            targetLine.start();
            new Thread(new Runnable() { /* Opens thread for listening to audio for recording */
               @Override
               public void run() {
                  AudioInputStream recordingStream = new AudioInputStream(targetLine);
                  output = new File("./Voice_Notes_" + whoAmI + "/Sent/VN_" + programName + "_" + whoAmI + "_"
                        + new Date().getTime() + ".wav");
                  try {
                     AudioSystem.write(recordingStream, AudioFileFormat.Type.WAVE, output);
                  } catch (IOException exc) {
                  }
               }
            }).start();

            /* Closing the targetline when stop recording */
            JOptionPane.showMessageDialog(frame, "Press OK to stop.");
            targetLine.stop();
            targetLine.close();

            /* Indicate to start sending voice note */
            String ip = ipGenerator();
            int pt = portGenerator();

            Packet pk1 = new Packet(whoAmI, "ALL", "#STARTSENDINGVN", ip, pt, output.getName(), (int) output.length());

            groupSend(groupIpAddr, groupPortNum, Serializer.toBytes(pk1));

         }

      } catch (Exception e) {
      }
   }

   /**
    * This method allows a voicenote to be sent to group members
    * 
    * @param ipAddr    the group IP address
    * @param portNum   the port number of the group
    * @param file      the voicenote file
    * @param chatBoxTA the group textbox
    */
   public static void vnSender(String ipAddr, int portNum, File file, JTextArea chatBoxTA) {
      new Thread(new Runnable() {
         @Override
         public void run() {
            try {
               /* set up socket to send over */
               InetAddress group = InetAddress.getByName(ipAddr);
               MulticastSocket socket = new MulticastSocket();
               FileInputStream fis = new FileInputStream(file);

               /* Convert byte array audio into packets and send */
               byte[] buffer = new byte[1024];
               int bytesRead;
               while ((bytesRead = fis.read(buffer)) != -1) {
                  DatagramPacket packet = new DatagramPacket(buffer, bytesRead, group, portNum);
                  socket.send(packet);
               }

               /* close socket */
               fis.close();
               socket.close();
               chatBoxTA.append("VOICE NOTE SENT\n");

            } catch (IOException e) {
               chatBoxTA.append("Failed to send.\n");
            }
         }
      }).start();

   }

   /**
    * This method allows a voicenote to be received by group members.
    * @param whoAmI The current client
    * @param from  The sender of the vn
    * @param frame The GUI frame
    * @param chatBoxTA the group textbox
    * @param ipAddr The group IP address
    * @param portNum The group port number
    * @param fileName The name of the file
    * @param fileLength The length of the file
    */
   public static void vnReceiver(String whoAmI, String from, JFrame frame, JTextArea chatBoxTA, String ipAddr,
         int portNum, String fileName, int fileLength) {
      new Thread(new Runnable() {
         @Override
         public void run() {
            try {
               /* Set up receiver socket */
               InetAddress group = InetAddress.getByName(ipAddr);
               MulticastSocket socket = new MulticastSocket(portNum);
               socket.joinGroup(group);
               makeFolders(whoAmI);
               File file = new File("./Voice_Notes_" + whoAmI + "/Received/" + fileName);
               FileOutputStream fos = new FileOutputStream(file);

               /* Receive packets to convert into byte array audio */
               byte[] buffer = new byte[1024];
               while (true) {
                  DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                  socket.receive(packet);
                  fos.write(packet.getData(), 0, packet.getLength());
                  if (packet.getLength() < 1024) {
                     break;
                  }
               }

               /* close socket */
               fos.close();
               socket.leaveGroup(group);
               socket.close();

               /* Notify that voice note has been received */
               chatBoxTA.append("VOICE NOTE RECEIVED FROM " + from + ".\n");
               int option = JOptionPane.showOptionDialog(frame, "Press OK to listen to VN from " + from, "PLAY", 2, 1,
                     null, null, null);
               if (option == 0) {
                  playVN(file);
               }
            } catch (IOException e) {
            }
         }
      }).start();

   }
     /**
    * This method allows the voicenote to be played by the group members
    * @param file The voicenote sent as a .wav file
    */

   public static void playVN(File file) {

      /* Use built-in Clip class to play audio from audio input stream*/
      try {
         AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
        
         Clip clip = (Clip) AudioSystem.getLine(new DataLine.Info(Clip.class, audioInputStream.getFormat()));
         clip.open(audioInputStream);
         clip.start();
         Thread.sleep(clip.getMicrosecondLength() / 1000);
         clip.stop();
         clip.close();
      } catch (Exception e) {
         System.out.println("Error: " + e.getMessage());
      }
   }

  
}
