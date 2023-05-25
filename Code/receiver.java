import java.awt.Desktop;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class receiver {
    private int port = 7575;
    private String title = "RECEIVER";

    /**
     * Receiver constructor initializing variables.
     * 
     * @param title         - Name of Frame.
     * @param chatFrame     - JFrame component.
     * @param portNum       - Port connection takes place on.
     * @param directoryPath -Directory file being saved in to.
     */
    public receiver(String title, JFrame chatFrame, int portNum, String directoryPath) {
        this.port = portNum;
        this.directoryPathName = directoryPath;
        this.title = title;
        parentF = chatFrame;

    }

    private JFrame parentF;
    private String directoryPathName = "./";

    /**
     * Receives file information along with file.
     * 
     * @param client -Client object.
     */
    public void receiveFileTCP(Client client) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ServerSocket serverSocket = null;
                try {
                    serverSocket = new ServerSocket(port);
                } catch (IOException e) {

                }

                try {
                    Socket socket1 = serverSocket.accept();
                    ProgressBar pb = new ProgressBar(parentF, title);
                    DataInputStream dIS = new DataInputStream(socket1.getInputStream());
                    /* Read the file name as a separate message */
                    String receivedFileName = dIS.readUTF();
                    int receivedFileLength = dIS.readInt();

                    /* Calculates total number of packets */
                    int packTotal = receivedFileLength / 512;
                    int progress = 0;
                    int pack = 0;
                    int remaining = 0;

                    OutputStream fileOPstream = new FileOutputStream(directoryPathName + receivedFileName);
                    byte[] bFile = new byte[receivedFileLength];

                    if (receivedFileLength > 0) {
                        remaining = receivedFileLength;
                        while (remaining > 0) {
                            /* Check if paused */
                            if (pb.getPaused()) {
                                while (pb.getPaused()) {
                                    Thread.sleep(1);
                                }
                            } else {
                                pack = Math.min(512, remaining);
                                dIS.readFully(bFile, 0, pack);
                                fileOPstream.write(bFile, 0, pack);
                                fileOPstream.flush();
                                progress += 1;
                                remaining -= pack;
                                /* Updates progress bar */
                                pb.setProgress((int) ((progress * 100) / packTotal));
                            }
                        }
                        pb.setProgress(100);
                        client.setCanDownload(true);

                        /* Close connection */
                        fileOPstream.close();
                        dIS.close();
                        socket1.close();
                        serverSocket.close();

                        /* Promt user to open file */
                        int reply = JOptionPane.showConfirmDialog(parentF, "Open " + receivedFileName, "Viewer",
                                JOptionPane.YES_NO_OPTION);
                        if (reply == JOptionPane.YES_OPTION) {
                            opener(parentF, directoryPathName, receivedFileName);
                        }
                    }
                } catch (Exception ignore) {
                }
            }
        }).start();

    }

    /**
     * Opens the file.
     * 
     * @param parentF  - Parent componet.
     * @param dirPath  - Path to directory.
     * @param fileName - File name.
     */
    public static void opener(JFrame parentF, String dirPath, String fileName) {
        try {
            File f = new File(dirPath + File.separator + fileName);
            Desktop.getDesktop().open(f);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parentF, "Error File Not Supported: " + fileName,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

}
