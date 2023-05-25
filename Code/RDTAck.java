import java.io.Serializable;

/**
 * This class creates the acknowledgement to be sent over UDP.
 * @author Jayden Abrahams – 23559322@sun.ac.za
 * @author Clinton Elves - 24007676@sun.ac.za
 * @author Cindi-Ann Jeffery – 24695823@sun.ac.za
 * @author Konanani Mathoho – 24774898@sun.ac.za
 * @author Langton Chikukwa – 23607769@sun.ac.za
 */

public class RDTAck implements Serializable{
	
	private int packet;

	/**
	 * Constructor for RDTAck
	 * @param packet packet number
	 */
	public RDTAck(int packet) {
		super();
		this.packet = packet;
	}

	/**
	 * @return the packet number
	 */
	public int getPacket() {
		return packet;
	}
}
