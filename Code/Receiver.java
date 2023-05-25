import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JProgressBar;

/**
 * This Receiver Class receives files sent over a server.
 * 
 * @author Jayden Abrahams – 23559322@sun.ac.za
 * @author Clinton Elves - 24007676@sun.ac.za
 * @author Cindi-Ann Jeffery – 24695823@sun.ac.za
 * @author Konanani Mathoho – 24774898@sun.ac.za
 * @author Langton Chikukwa – 23607769@sun.ac.za
 */

public class Receiver {
	static String hostUDP = "127.0.0.1";
	static String fileName = "file";
	static int filePackets;
	static Socket socketTCP;
	static int packetLoss = 0;
	static ObjectInputStream inputStream;
	static ObjectOutputStream outputStream;

	private static String whoAmI;

	/**
	 * Constructor method for Receiver
	 * 
	 * @param whoAmI                        the client username
	 * @param fName                         the file name
	 * @param totalNumberOfPacketsToReceive the number of packets to receive
	 * @param packetSizeToReceive           the packet size to receive
	 * @param receiverPB                    the progress bar
	 */
	public Receiver(String whoAmI, String fName, int totalNumberOfPacketsToReceive, int packetSizeToReceive,
			JProgressBar receiverPB) {
		fileName = fName;
		this.whoAmI = whoAmI;
		/* Tries to connect */
		while (true) {
			try {
				socketTCP = new Socket(InetAddress.getByName(hostUDP), 9999);
				outputStream = new ObjectOutputStream(socketTCP.getOutputStream());
				inputStream = new ObjectInputStream(socketTCP.getInputStream());
				break;
			} catch (Exception e) {
				System.out.println("Waiting for connection...");
			}
		}
		try {
			DatagramSocket fromSender = new DatagramSocket(9876);
			byte[] receivedData = new byte[Sender.packetSize + 83];
			int waitingFor = 0;
			ArrayList<RDTPacket> received = new ArrayList<RDTPacket>();
			boolean end = false;
			int count = 0;
			while (!end) {
				System.out.println("Waiting for packet");
				DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
				fromSender.receive(receivedPacket);
				RDTPacket packet = (RDTPacket) Serializer.toObject(receivedPacket.getData());
				System.out.println("Packet with sequence number " + packet.getSeq() + " received.");
				if (packet.getSeq() == waitingFor && packet.isLast()) {

					waitingFor++;
					received.add(packet);
					System.out.println("Last Packet RECEIVED");
					end = true;

				} else if (packet.getSeq() == waitingFor) {
					waitingFor++;
					received.add(packet);
					System.out.println("Packed stored in buffer");
					count++;
					receiverPB.setValue((count * 100) / totalNumberOfPacketsToReceive);
				} else {
					System.out.println("Packet discarded (not in order)");
					packetLoss++;
				}
				RDTAck ackObject = new RDTAck(waitingFor);
				byte[] ackBytes = Serializer.toBytes(ackObject);
				outputStream.writeObject(ackObject);
				outputStream.flush();
				System.out.println("Sending ACK to seq " + waitingFor + " with " + ackBytes.length + " bytes");
			}
			/* Writes data to file */
			writeFile(received, receiverPB);
			System.out.println("Packets loss: " + packetLoss);
			try {
				if (socketTCP != null) {
					socketTCP.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
				if (inputStream != null) {
					inputStream.close();
				}
				if (fromSender != null) {
					fromSender.close();
				}
			} catch (Exception e) {
			}
		} catch (Exception e) {
		}

	}

	/**
	 * This method writes to an output file
	 * 
	 * @param received   The packets that have been received
	 * @param receiverPB The progress bar
	 */
	public static void writeFile(ArrayList<RDTPacket> received, JProgressBar receiverPB) {
		/* Checks if theres a receiving directory, if not the it makes one */
		if (!(new File("./FILES_RECEIVED_" + whoAmI + "/")).exists()) {
			new File("./FILES_RECEIVED_" + whoAmI + "/").mkdirs();
		}
		/* writes the file to the directory */
		try {
			File f = new File("./FILES_RECEIVED_" + whoAmI + "/" + fileName);
			FileOutputStream outToFile = new FileOutputStream(f);
			for (RDTPacket p : received) {
				outToFile.write(p.getData());
			}
			outToFile.close();
			receiverPB.setValue(100);
		} catch (Exception e) {
			System.out.println("Failed to write file");
		}
	}
}
