import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import javax.swing.JFrame;

public class sender {

    private static File fileToSend;
    private int port = 7575;
    private String ipAddress = "127.0.0.1";
    private String title = "SENDER";
    private String filePath;
    private JFrame parent;

    /**
     * Sender Constructor that initializes variables and calls sendFileTCP.
     * 
     * @param chatBoxFrame - Jframe chatBoxFrame.
     * @param title        - Name of Frame.
     * @param ipAddress    - IP address of receiver.
     * @param portNum      - Port connection is taking place on.
     * @param filePath     - Path to file being sent.
     * @param client       - Client object.
     */
    public sender(JFrame chatBoxFrame, String title, String ipAddress, int portNum, String filePath, Client client) {
        parent = chatBoxFrame;
        this.port = portNum;
        this.filePath = filePath;
        this.title = title;
        this.ipAddress = ipAddress;
        this.filePath = filePath;
        fileToSend = new File(this.filePath);
        if (fileToSend.exists()) {
            sendFileTCP(client);
        } else {
            System.out.println("File path does not exist");
        }

    }

    /**
     * Sends the file over TCP.
     * 
     * @param client - Client Object.
     */
    public void sendFileTCP(Client client) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FileInputStream fIS = new FileInputStream(fileToSend.getAbsolutePath());
                    Socket sendSocket = null;
                    /* Tries to connect to Receiver */
                    while (true) {
                        try {
                            sendSocket = new Socket(ipAddress, port);
                            break;
                        } catch (Exception e) {
                            try {
                                /* Waits a while before retrying */
                                Thread.sleep(1000);
                            } catch (Exception ignore) {
                            }
                        }
                    }
                    /* Creating the progress bar */
                    ProgressBar pb = new ProgressBar(parent, title);
                    DataOutputStream dOS = new DataOutputStream(sendSocket.getOutputStream());
                    /* Send the file name as a separate message */
                    dOS.writeUTF(fileToSend.getName());
                    dOS.writeInt((int) fileToSend.length());

                    long fileSize = fileToSend.length();

                    byte[] bSendFile = new byte[(int) 512];
                    int bytesRead = 0;
                    long totalBytesRead = 0;
                    /* Reading from file and then writing to DataOutputStream */
                    while ((bytesRead = fIS.read(bSendFile)) != -1) {
                        dOS.write(bSendFile, 0, bytesRead);
                        totalBytesRead += bytesRead;
                        int progress = (int) ((totalBytesRead * 100) / fileSize);
                        /* Update progress bar */
                        pb.setProgress(progress);
                    }

                    pb.setProgress(100);
                    /* Close Connection */
                    dOS.close();
                    fIS.close();
                    sendSocket.close();
                    client.setUpload(true);
                } catch (IOException exception) {
                }
            }
        }).start();
    }

}
