
import java.io.Serializable;

/**
 * This class creates the packet to be sent over UDP.
 * @author Jayden Abrahams – 23559322@sun.ac.za
 * @author Clinton Elves - 24007676@sun.ac.za
 * @author Cindi-Ann Jeffery – 24695823@sun.ac.za
 * @author Konanani Mathoho – 24774898@sun.ac.za
 * @author Langton Chikukwa – 23607769@sun.ac.za
 */
public class Packet implements Serializable {
    String from;
    String to;
    String type;
    byte[] data;
    String fileName;
    int fileLength;

    /**
     * Constructor method for Packet
     * @param from the sender of the packet
     * @param to the receiver of the packet
     * @param type the type of packet to be sent
     * @param data byte array
     * @param fileName the file name
     * @param fileLength the file length
     */
    public Packet(String from, String to, String type, byte[] data, String fileName, int fileLength) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.data = data;
        this.fileName = fileName;
        this.fileLength = fileLength;
    }

int packetSize;
int numberOfPackets;
    /**
     * Constructor method for Packet
     * @param from the sender of the packet
     * @param to the receiver of the packet
     * @param type the type of packet to be sent
     * @param fileName the file name
     * @param fileLength the file length
     * @param packetSize the size of the packet to be sent
     * @param numberOfPackets the number of packets to be sent
     */
    public Packet(String from, String to, String type, String fileName, int fileLength,  int packetSize, int numberOfPackets) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.fileName = fileName;
        this.fileLength = fileLength;
        this.packetSize = packetSize;
        /* Numebr of packets to be sent over. */
        this.numberOfPackets = numberOfPackets;
    }

    /**
     * @return the sender of the packet
     */
    public String getFrom() {
        return from;
    }

     /**
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @return the file length
     */
    public int getFileLength() {
        return fileLength;
    }

    /**
     * @return the receiver of the packet
     */
    public String getTo() {
        return to;
    }

    /**
     * @return the type of packet being sent
     */
    public String getType() {
        return type;
    }

    /**
     * @return the byte array that contains data
     */
    public byte[] getData() {
        return data;
    }

}
