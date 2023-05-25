
import java.io.Serializable;
import java.math.BigInteger;
import java.security.Key;
import java.security.PublicKey;
import java.util.ArrayList;

import javax.swing.plaf.basic.BasicBorders.RolloverButtonBorder;

/**
 * This class creates the packet to be sent to the server.
 * 
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
     * Empty constructor
     */
    public Packet() {
    }

    private String ipAddress;
    private PublicKey pubKey;

    /**
     * Constructor method for Packet object.
     * 
     * @param from      - who the packet originated from.
     * @param to        - who the packet is going to.
     * @param type      - type of Packet.
     * @param ipAddress - ip Address of receiver.
     * @param message   - Message.
     * @param key       - public key.
     */
    public Packet(String from, String to, String type, String ipAddress, String message, PublicKey key) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.ipAddress = ipAddress;
        this.pubKey = key;
        this.message = message;
    }

    /**
     * Constructor method for Packet object.
     * 
     * @param from    - who the packet originated from.
     * @param to      - who the packet is going to.
     * @param type    - type of Packet.
     * @param message - Message.
     * @param key     - public key.
     */

    public Packet(String from, String to, String type, String message, PublicKey key) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.pubKey = key;
        this.message = message;
    }

    private BigInteger bigP;
    private BigInteger bigG;

    private BigInteger bigN;

    /*
     * @returns BigInteger N.
     */
    public BigInteger getBigN() {
        return bigN;
    }

    /**
     * Set BigInteger P.
     * 
     * @param n - BigInteger.
     */
    public void setBigN(BigInteger n) {
        bigN = new BigInteger(n.toString());
    }

    private BigInteger bigR;

    /*
     * @returns BigInteger R.
     */
    public BigInteger getBigR() {
        return bigR;
    }

    /**
     * Set BigInteger R.
     * 
     * @param r - BigInteger.
     */
    public void setBigR(BigInteger r) {
        bigR = new BigInteger(r.toString());
    }

    /*
     * @returns BigInteger P.
     */
    public BigInteger getBigP() {
        return bigP;
    }

    /**
     * Get BigInteger G.
     * 
     * @return - BigInteger G.
     */
    public BigInteger getBigG() {
        return bigG;
    }

    /**
     * Set BigInteger P.
     * 
     * @param p - BigInteger.
     */
    public void setBigP(BigInteger p) {
        bigP = new BigInteger(p.toString());
    }

    /**
     * Set BigInteger G.
     * 
     * @param g - BigInteger.
     */
    public void setBigG(BigInteger g) {
        bigG = new BigInteger(g.toString());
    }

    private byte[] encryptedMessage;
    private byte[] hashKey;

    /**
     * Constructor method for Packet object.
     * 
     * @param from      - the sender of the packet.
     * @param to        - the receiver of the packet.
     * @param type      - the type of packet to be sent.
     * @param eMessage  - encryted message byte[].
     * @param hashedKey - hashed key.
     */
    public Packet(String from, String to, String type, byte[] eMessage, byte[] hashedKey) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.encryptedMessage = eMessage;
        this.hashKey = hashedKey;
    }

    /**
     * The function returns a byte array representing the hash key.
     * 
     * @return A byte array named "hashKey" is being returned.
     */
    public byte[] getHashKey() {
        return this.hashKey;
    }

    /**
     * The function returns a byte array containing an encrypted message.
     * 
     * @return A byte array containing the encrypted message.
     */
    public byte[] getEMessage() {
        return this.encryptedMessage;
    }

    /**
     * The function returns the IP address.
     * 
     * @return The method `getIpAddress()` is returning a `String` which represents the IP address.
     */
    public String getIpAddress() {
        return this.ipAddress;
    }

    /**
     * This function sets the IP address of an object.
     * 
     * @param ip The parameter "ip" is a String variable that represents an IP address. The method
     * "setIpAddress" sets the value of the instance variable "ipAddress" to the value of the "ip"
     * parameter.
     */
    public void setIpAddress(String ip) {
        this.ipAddress = ip;
    }

 
    /**
     * The function returns the public key.
     * 
     * @return The method `getKey()` is returning a `PublicKey` object.
     */
    public PublicKey getKey() {
        return this.pubKey;
    }

    public int fileSharePort;

    /**
     * This function sets the file sharing port to a specified value.
     * 
     * @param port The "port" parameter is an integer value that represents the port number used for
     * file sharing. This method sets the value of the "fileSharePort" instance variable to the
     * specified port number.
     */
    public void setFileSharePort(int port) {
        this.fileSharePort = port;
    }

    /**
     * This function returns the file share port.
     * 
     * @return The method `getFileSharePort()` is returning an integer value which represents the file
     * share port.
     */
    public int getFileSharePort() {
        return this.fileSharePort;
    }

    String dirPath;

    /**
     * The function returns the directory path as a string.
     * 
     * @return The method `getDir()` is returning a `String` variable named `dirPath`.
     */
    public String getDir() {
        return dirPath;
    }

    /**
     * This Java function sets the value of a variable called "dirPath" to the value of the input
     * parameter "dir".
     * 
     * @param dir The parameter "dir" is a String that represents the directory path that we want to
     * set. This method sets the value of the class variable "dirPath" to the value of the "dir"
     * parameter.
     */
    public void setDir(String dir) {
        dirPath = dir;
    }

    /**
     * Constructor method for Packet object
     * 
     * @param from       the sender of the packet
     * @param to         the receiver of the packet
     * @param type       the type of packet to be sent
     * @param data       byte array
     * @param fileName   the file name
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

    /**
     * Constructor method for Packet object
     * 
     * @param from         the sender of the packet
     * @param to           the receiver of the packet
     * @param type         the type of packet to be sent
     * @param ip           the IP address
     * @param groupPortNum the port number for the group
     * @param fileName     the file name
     * @param fileLength   the file length
     */
    public Packet(String from, String to, String type, String ip, int groupPortNum, String fileName, int fileLength) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.groupIP = ip;
        this.groupPortNum = groupPortNum;
        this.fileName = fileName;
        this.fileLength = fileLength;
    }

    /**
     * Constructor method for Packet object
     * 
     * @param from the sender of the packet
     * @param to   the receiver of the packet
     * @param type the type of packet to be sent
     */
    public Packet(String from, String to, String type) {
        this.from = from;
        this.to = to;
        this.type = type;
    }

    /**
     * Constructor method for Packet object
     * 
     * @param from             the sender of the packet
     * @param to               the receiver of the packet
     * @param type             the type of packet to be sent
     * @param groupMemberNames the names of the group members
     */
    ArrayList<String> groupMemberNames = new ArrayList<>();

    public Packet(String from, String to, String type, ArrayList<String> groupMemberNames) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.groupMemberNames = groupMemberNames;
    }

    public Packet(String from, String to, String type, ArrayList<String> groupMemberNames, int id) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.groupMemberNames = groupMemberNames;
        this.id = id;
    }

    private String message;

    /**
     * Constructor method for Packet object
     * 
     * @param from    the sender of the packet
     * @param to      the receiver of the packet
     * @param type    the type of packet to be sent
     * @param message the message to be sent
     */
    public Packet(String from, String to, String type, String message) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.message = message;
    }

    private int id;

    /**
     * Constructor method for Packet object
     * 
     * @param from    the sender of the packet
     * @param to      the receiver of the packet
     * @param type    the type of packet to be sent
     * @param message the message to be sent.
     * @param id      search id.
     */
    public Packet(String from, String to, String type, String message, int id) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.message = message;
        this.id = id;
    }

    public int getID() {
        return this.id;
    }

    private String groupName;
    private String groupIP;
    private int groupPortNum;

    /**
     * Constructor method for Packet object
     * 
     * @param from         the sender of the packet
     * @param to           the receiver of the packet
     * @param type         the type of packet to be sent
     * @param groupName    the names of the group
     * @param groupIP      the ip address of the group
     * @param groupPortNum the port number of the group
     */
    public Packet(String from, String to, String type, String groupName, String groupIP, int groupPortNum) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.groupName = groupName;
        this.groupIP = groupIP;
        this.groupPortNum = groupPortNum;

    }

    private String callIp1;
    private int callPortx;

    /**
     * Constructor method for Packet object
     * 
     * @param from         the sender of the packet
     * @param to           the receiver of the packet
     * @param type         the type of packet to be sent
     * @param callIp       the IP address of the caller
     * @param callPort     the port number of the caller
     * @param groupName    the names of the group
     * @param groupIP      the ip address of the group
     * @param groupPortNum the port number of the group
     */
    public Packet(String from, String to, String type, String callIp, int callPort, String groupName, String groupIP,
            int groupPortNum, String message) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.callIp1 = callIp;
        this.callPortx = callPort;
        this.groupName = groupName;
        this.groupIP = groupIP;
        this.groupPortNum = groupPortNum;
        this.message = message;
    }

    private String callIp2;

    /**
     * Constructor method for Packet object
     * 
     * @param from         the sender of the packet
     * @param to           the receiver of the packet
     * @param type         the type of packet to be sent
     * @param callIp1      the IP address of the caller
     * @param callIp2      the IP address of the second caller
     * @param callPortx    the port number of the caller
     * @param groupName    the names of the group
     * @param groupIP      the ip address of the group
     * @param groupPortNum the port number of the group
     * @param message      the message to be sent
     */
    public Packet(String from, String to, String type, String callIp1, String callIp2, int callPortx, String groupName,
            String groupIP, int groupPortNum, String message) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.callIp1 = callIp1;
        this.callPortx = callPortx;
        this.callIp2 = callIp2;
        // this.callPort2 = callPort2;
        this.groupName = groupName;
        this.groupIP = groupIP;
        this.groupPortNum = groupPortNum;
        this.message = message;
    }

    public Packet(String from, String to, String type, String fileName, String message) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.fileName = fileName;
        this.message = message;
    }

    /**
     * This method gets the caller 1 IP address
     * 
     * @return IP address as a String
     */
    public String getCallIp1() {
        return this.callIp1;
    }

    /**
     * This method gets the caller port number
     * 
     * @return the port number
     */
    public int getCallPortx() {
        return this.callPortx;
    }

    /**
     * This method gets the caller 2 IP address
     * 
     * @return IP address as a String
     */
    public String getCallIp2() {
        return this.callIp2;
    }

    /**
     * This method adds the group members to a group
     * 
     * @param name the user to be added
     */
    public void addGroupMember(String name) {
        groupMemberNames.add(name);
    }

    /**
     * This method removes the group members to a group
     * 
     * @param name the user to be removed
     */
    public void removeGroupMember(String name) {
        groupMemberNames.remove(name);
    }

    /**
     * This method returns all the group members connected
     * 
     * @return ArrayList of group members
     */
    public ArrayList<String> getMembersList() {
        return groupMemberNames;
    }

    /**
     * This method returns who the sender is
     * 
     * @return the sender
     */
    public String getFrom() {
        return from;
    }

    /**
     * This method returns the filename
     * 
     * @return the filename
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * This method returns the filename length
     * 
     * @return the filename length
     */
    public int getFileLength() {
        return fileLength;
    }

    /**
     * This method returns who the receiver is
     * 
     * @return the receiver
     */
    public String getTo() {
        return to;
    }

    /**
     * This method returns the type
     * 
     * @return the type
     */

    public String getType() {
        return type;
    }

    /**
     * This method returns the byte array data
     * 
     * @return byte array
     */
    public byte[] getData() {
        return data;
    }

    /**
     * This method returns the group member's names
     * 
     * @return The group member's names
     */
    public ArrayList<String> getGroupMemberNames() {
        return groupMemberNames;
    }

    /**
     * This method returns the message
     * 
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * This method returns the group name
     * 
     * @return the group name
     */
    public String getGroupName() {
        return this.groupName;
    }

    /**
     * This method returns the group IP address
     * 
     * @return the group IP
     */
    public String getGroupIP() {
        return this.groupIP;
    }

    /**
     * This method returns the group IP port number
     * 
     * @return the group port number
     */
    public int getGroupPortNum() {
        return this.groupPortNum;
    }

    /**
     * This method sets who the sender is
     * 
     * @param from the sender
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * This method sets who the receiver is
     * 
     * @param to the receiver
     */
    public void setTo(String to) {
        this.to = to;
    }

    /**
     * This method sets the type
     * 
     * @param type the type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * This method sets the byte array's data
     * 
     * @param data the byte array
     */
    public void setData(byte[] data) {
        this.data = data;
    }

    /**
     * This method sets the file name
     * 
     * @param fileName the file name
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * This method sets the file length
     * 
     * @param fileLength the file length
     */
    public void setFileLength(int fileLength) {
        this.fileLength = fileLength;
    }

    /**
     * This method sets the groupMemberNames
     * 
     * @param groupMemberNames the groupMemberNames arraylist
     */
    public void setGroupMemberNames(ArrayList<String> groupMemberNames) {
        this.groupMemberNames = groupMemberNames;
    }

    /**
     * This method sets the message.
     */

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * This method sets the group name
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * This method sets the group IP address
     */
    public void setGroupIP(String groupIP) {
        this.groupIP = groupIP;
    }

    /**
     * This method sets the group IP port number
     */
    public void setGroupPortNum(int groupPortNum) {
        this.groupPortNum = groupPortNum;
    }

}
