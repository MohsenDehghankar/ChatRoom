package sample;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {

    private static final String CODE = "5780";
    private String clientName;
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;
    private Socket s;
    private boolean isLoggedIn;
    // private ArrayList<String> groupsNames = new ArrayList<>();

    public ClientHandler(Socket s, String name,
                         DataInputStream dis, DataOutputStream dos) {
        this.dataInputStream = dis;
        this.dataOutputStream = dos;
        this.clientName = name;
        this.s = s;
        this.isLoggedIn = true;
    }

    @Override
    public void run() {
        sendClientsAndGroupArrayList();
        String received;
        mainLoop:
        while (true) {
            try {
                received = dataInputStream.readUTF();
                if (received.equals(CODE + "list")) {
                    sendClientsAndGroupArrayList();
                } else if (received.equals(CODE + "exit")) {
                    isLoggedIn = false;
                    Server.checkOnlineClients();
                    break;
                } else if (received.equals(CODE + "chat")) {
                    Chat chat = new Chat(dataInputStream, dataOutputStream, clientName);
                    chat.startChat();
                } else if (received.contains(".") && received.split("\\.")[0].equals(CODE + "group")) {
                    //CODEgroup.clientName.groupName(.members)
                    handleGroup(received);

                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

        }
        try {
            this.dataInputStream.close();
            this.dataOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleGroup(String received) throws IOException {
        String[] split = received.split("\\.");
        if (split.length == 3) {
            //entering a group
            String groupName = split[2];
            String clientName = split[1];
            ServerGroup serverGroup = Server.findGroupByName(groupName);
            if (serverGroup != null && serverGroup.isMember(clientName)) {
                new GroupChat(serverGroup, clientName, dataOutputStream, dataInputStream).startChat();
            } else {
                //not entered
                dataOutputStream.writeUTF("5780error");
            }
        } else if (split.length == 4) {
            //creating a group
            String admin = split[1];
            String groupName = split[2];
            String members = split[3];
            ServerGroup serverGroup = new ServerGroup(members, groupName, admin);
            Server.addGroup(serverGroup);
            //group made , now enter
        }
    }

    public String getClientName() {
        return clientName;
    }

    private void sendClientsAndGroupArrayList() {
        String clients = CODE + "list";
        String send2 = " change";
        for (ServerGroup group : Server.getGroups()) {
            if (group.isMember(clientName))
                send2 += " " + group.getName();
        }
        for (int i = 0; i < Server.getClients().size(); i++) {
            if (i != 0)
                clients += " " + Server.getClients().get(i).getClientName();
            else
                clients += Server.getClients().get(i).getClientName();
        }
        try {
            dataOutputStream.writeUTF(clients + send2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    public boolean getLoggedInStatus() {
        return isLoggedIn;
    }

    /*public void addGroup(String groupName) {
        groupsNames.add(groupName);
    }*/
}


