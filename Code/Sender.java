import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This Sender Class sends files over a server.
 * 
 * @author Jayden Abrahams – 23559322@sun.ac.za
 * @author Clinton Elves - 24007676@sun.ac.za
 * @author Cindi-Ann Jeffery – 24695823@sun.ac.za
 * @author Konanani Mathoho – 24774898@sun.ac.za
 * @author Langton Chikukwa – 23607769@sun.ac.za
 */
public class Sender {

	static String hostSender = "127.0.0.2";
	static int portUDPSender = 9876;
	static ObjectOutputStream outputStreamSender;
	static ObjectInputStream inputStreamSender;
	/* Maximum Segment Size */
	public static final int packetSize = 65400;

	// Probability of loss during packet sending
	public static final double pPacketLost = 0.0000001;

	/* Number of packets sent without acking */
	public static final int blastSize = 5;
	static int sqNumLS = 0;
	static int sqNumAck = 0;
	static int numSqPackets;
	static byte[] fileByteArraySender;
	static ArrayList<RDTPacket> packetsSent;
	static InetAddress receiverAddrSender;
	static DatagramSocket receiverDSSender;
	static boolean stop = false;

	public Sender(File f) {
		Socket socketTCP;
		try {
			/* waits for connection */
			socketTCP = (new ServerSocket(9999)).accept();
			System.out.println("ACCEPTED...");
			socketTCP.setSoTimeout(30);
			outputStreamSender = new ObjectOutputStream(socketTCP.getOutputStream());
			inputStreamSender = new ObjectInputStream(socketTCP.getInputStream());
			fileByteArraySender = fileToByteArray(f);
			System.out.println("Data size: " + fileByteArraySender.length + " bytes");
			numSqPackets = (int) Math.ceil((double) fileByteArraySender.length / packetSize);
			System.out.println("Number of packets to send: " + numSqPackets);
			packetsSent = new ArrayList<RDTPacket>();
			try {
				receiverDSSender = new DatagramSocket();
				receiverAddrSender = InetAddress.getByName(hostSender);
				while (!stop) {
					sending();
					ack();
				}
				System.out.println("Finished transmission");
				/* Closes the connection */
				try {
					if (socketTCP != null) {
						socketTCP.close();
					}
					if (outputStreamSender != null) {
						outputStreamSender.close();
					}
					if (inputStreamSender != null) {
						inputStreamSender.close();
					}
					if (receiverDSSender != null) {
						receiverDSSender.close();
					}
				} catch (Exception e) {

				}
			} catch (Exception e) {
			}

			// }

			// Socket socketTCP = (new ServerSocket(5555)).accept();

			// startUDP(f);

		} catch (Exception e) {
		}
	}

	public static void sending() throws IOException {
		while (sqNumLS - sqNumAck < blastSize && sqNumLS < numSqPackets) {

			byte[] filePacketBytes = new byte[packetSize];

			/* Copy segment of data bytes to array */
			filePacketBytes = Arrays.copyOfRange(fileByteArraySender, sqNumLS * packetSize,
					sqNumLS * packetSize + packetSize);

			/* Create RDTPacket object */
			RDTPacket rdtPacketObject = new RDTPacket(sqNumLS, filePacketBytes,
					(sqNumLS == numSqPackets - 1) ? true : false);

			byte[] sendData = Serializer.toBytes(rdtPacketObject);

			DatagramPacket packet = new DatagramPacket(sendData, sendData.length, receiverAddrSender, portUDPSender);

			System.out.println(
					"Sending packet with sequence number: " + sqNumLS);

			/*
			 * Add packet to the sent list
			 */
			packetsSent.add(rdtPacketObject);

			/* / Send with some probability of loss */
			if (Math.random() > pPacketLost) {
				receiverDSSender.send(packet);
			} else {
				System.out.println("[X] Lost packet with sequence number " + sqNumLS);
			}

			sqNumLS++;

		}

	}

	public static void ack() throws ClassNotFoundException, IOException {
		try {
			/* If an ACK was not received in the time specified */
			receiverDSSender.setSoTimeout(10);
			RDTAck ackObject = (RDTAck) inputStreamSender.readObject();
			System.out.println("Received ACK for " + ackObject.getPacket());
			/* If last ack, stop sender */
			if (ackObject.getPacket() == numSqPackets) {
				stop = true;
			}
			sqNumAck = Math.max(sqNumAck, ackObject.getPacket());

		} catch (SocketTimeoutException e) {

			for (int i = sqNumAck; i < sqNumLS; i++) {
				/* Serialize the RDTPacket object */
				byte[] sendData = Serializer.toBytes(packetsSent.get(i));
				// Create the packet
				DatagramPacket packet = new DatagramPacket(sendData, sendData.length, receiverAddrSender,
						portUDPSender);
				/* Send with some probability */
				if (Math.random() > pPacketLost) {
					receiverDSSender.send(packet);
				} else {
					System.out.println("[X] Lost packet with sequence number " + packetsSent.get(i).getSeq());
				}
			}
		}
	}

	public static byte[] fileToByteArray(File fileToSend) {
		FileInputStream fileInputStream = null;
		byte[] byteArray = new byte[(int) fileToSend.length()];
		try {
			fileInputStream = new FileInputStream(fileToSend);
			fileInputStream.read(byteArray);
			fileInputStream.close();
		} catch (IOException ioExp) {
			ioExp.printStackTrace();
		}
		return byteArray;
	}
}
