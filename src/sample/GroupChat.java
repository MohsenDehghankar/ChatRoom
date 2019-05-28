package sample;

import java.io.*;
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
            } else if (received.length() >= 9 && received.substring(0, 9).equals("5780image")) {
                String group = received.substring(9, received.lastIndexOf("."));
                long length = Integer.parseInt(received.substring(received.lastIndexOf('.') + 1));
                FileOutputStream f = new FileOutputStream("src/images/serverTemp/1.jpg");
                int count;
                byte[] buffer = new byte[70000]; // or 4096, or more
                while ((count = dataInputStream.read(buffer)) > 0) {
                    f.write(buffer, 0, count);
                    if (count == length)
                        break;
                }
                f.close();
                main:
                for (ServerGroup serverGroup : Server.getGroups()) {
                    if (serverGroup.getName().equals(groupName)) {
                        for (ClientHandler client : serverGroup.getMembers()) {
                            if (!client.getClientName().equals(currentClient)) {
                                FileInputStream fileInputStream = new FileInputStream("src/images/serverTemp/1.jpg");
                                client.getDataOutputStream().writeUTF("5780image" + groupName + "." + length);
                                byte[] bytes = new byte[70000];
                                int count2;
                                while ((count2 = fileInputStream.read(bytes)) > 0) {
                                    client.getDataOutputStream().write(bytes, 0, count);
                                }
                                fileInputStream.close();
                                break main;
                            }
                        }
                    }
                }
            } else if (received.contains(".") && received.split("\\.")[0].equals("5780emoji")) {
                sendEmoji(received);
            } else if (received.contains(".") && received.split("\\.").length == 2) {
                sendMessage(received);
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
