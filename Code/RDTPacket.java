import java.io.Serializable;

/**
 * This class creates the packet to be sent over UDP.
 * @author Jayden Abrahams – 23559322@sun.ac.za
 * @author Clinton Elves - 24007676@sun.ac.za
 * @author Cindi-Ann Jeffery – 24695823@sun.ac.za
 * @author Konanani Mathoho – 24774898@sun.ac.za
 * @author Langton Chikukwa – 23607769@sun.ac.za
 */
public class RDTPacket implements Serializable {

	public int seq;
	
	public byte[] data;
	
	public boolean last;

	/**
	 * Constructor for RDTPacket
	 * @param seq sequence number for packet
	 * @param data data of the packet
	 * @param last determines whether it is last packet or not
	 */
	public RDTPacket(int seq, byte[] data, boolean last) {
		super();
		this.seq = seq;
		this.data = data;
		this.last = last;
	}

	/**
	 * @return the sequence number
	 */
	public int getSeq() {
		return seq;
	}

	/**
	 * @return the data of the packet
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * @return returns true if the last packet was received
	 */
	public boolean isLast() {
		return last;
	}
	
}
