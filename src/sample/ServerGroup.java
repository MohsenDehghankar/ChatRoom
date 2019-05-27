package sample;

import java.util.ArrayList;
import java.util.Arrays;

public class ServerGroup {
    private static final String CODE = "5780";
    private ArrayList<ClientHandler> members = new ArrayList<>();
    private String groupName;
    private ArrayList<String> messages = new ArrayList<>();
    private String admin;

    public ServerGroup(String memberString, String groupName, String admin) {
        this.admin = admin;
        this.groupName = groupName;
        String[] member = memberString.split(" ");
        ArrayList<String> membersArray = new ArrayList<>();
        for (String s : member) {
            membersArray.add(s);
        }
        membersArray.add(admin);
        for (ClientHandler client : Server.getClients()) {
            if (membersArray.contains(client.getClientName()))
                members.add(client);
        }
    }

    public String getName() {
        return groupName;
    }

    public boolean isMember(String name) {
        for (ClientHandler member : members) {
            if (member.getClientName().equals(name))
                return true;
        }
        return false;
    }

    public ArrayList<ClientHandler> getMembers() {
        return members;
    }

    public String getAdmin() {
        return admin;
    }

    public void addMember(ClientHandler member) {
        members.add(member);
    }
}
