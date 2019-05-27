package sample;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class GroupChat {
    private String CODE = "5780";
    private String groupName;
    private String currentClient;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private String admin;

    public GroupChat(ServerGroup serverGroup, String currentClient
            , DataOutputStream dataOutputStream, DataInputStream dataInputStream) {
        this.groupName = serverGroup.getName();
        this.currentClient = currentClient;
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
        this.admin = serverGroup.getAdmin();
    }

    public void startChat() throws IOException {
        String received;
        while (true) {
            received = dataInputStream.readUTF();
            if (received.equals(CODE + "exit")) {
                break;
            } else if (received.contains(".") && received.split("\\.")[0].equals(CODE + "addmember")) {
                addMember(received);
            } else if (received.contains(".") && received.split("\\.").length == 2) {
                sendMessage(received);
            } else if (received.contains(".") && received.split("\\.")[0].equals("5780emoji")) {
                sendEmoji(received);
            }
        }
    }

    private void sendEmoji(String received) throws IOException {
        String[] split = received.split("\\.");
        String sender = split[2];
        String numberOfEmoji = split[1];
        sendMessage(sender + " : ." + sender);
        String toSend = "5780emoji" + "." + numberOfEmoji + "." + groupName;
        for (ServerGroup group : Server.getGroups()) {
            if (group.getName().equals(groupName)) {
                ArrayList<ClientHandler> members = group.getMembers();
                for (ClientHandler member : members) {
                    if (!member.getClientName().equals(sender)) {
                        member.getDataOutputStream().writeUTF(toSend);
                    }
                }
                break;
            }
        }
    }

    private void sendMessage(String received) throws IOException {
        String[] split = received.split("\\.");
        String sender = split[1];
        String message = split[0];
        for (ServerGroup group : Server.getGroups()) {
            if (group.getName().equals(groupName)) {
                ArrayList<ClientHandler> members = group.getMembers();
                for (ClientHandler member : members) {
                    if (!member.getClientName().equals(sender)) {
                        member.getDataOutputStream().writeUTF(message + "." + groupName);
                    }
                }
                break;
            }
        }
    }

    private void addMember(String received) {
        String[] split = received.split("\\.");
        String sender = split[1];
        String member = split[2];
        if (admin.equals(sender)) {
            ServerGroup group = Server.findGroupByName(groupName);
            for (ClientHandler client : Server.getClients()) {
                if (client.getClientName().equals(member))
                    group.addMember(client);
            }
        }
    }


}
