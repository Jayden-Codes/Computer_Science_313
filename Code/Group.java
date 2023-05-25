import java.util.ArrayList;

public class Group {
    private String groupName;
    private ArrayList<String> members = new ArrayList<>();
    private int numMembers = 0;
    private String ipAddr;
    private int portNumber;

    public Group(String groupName, String ipAddr, int portNumber) {
        this.groupName = groupName;
        this.ipAddr = ipAddr;
        this.portNumber = portNumber;
    }
    public String getName() {
        return this.groupName;
    }
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    public String getIpAddress() {
        return this.ipAddr;
    }
    public int getPortNumber() {
        return this.portNumber;
    }

    public void addMember(String person) {
        this.members.add(person);
        this.numMembers++;
    }

    public void removeMember(String person) {
        this.members.remove(person);
        this.numMembers--;
    }
    public int getNumberMembers() {
        return this.numMembers;
    }
    
}
