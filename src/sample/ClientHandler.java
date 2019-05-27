package sample;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private static final String CODE = "5780";
    private String clientName;
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;
    private Socket s;
    private boolean isLoggedIn;

    public static String getCODE() {
        return CODE;
    }

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
        sendClientsArrayList();
        String received;
        mainLoop:
        while (true) {
            try {
                received = dataInputStream.readUTF();

                if (received.equals(CODE + "clients")) {
                    sendClientsArrayList();
                } else if (received.equals(CODE + "groups")) {
                    String send = CODE + "groups ";
                    for (Group group : Server.getGroups()) {
                        send += group.getName() + " ";
                    }
                    send = send.substring(0, send.length() - 1);
                    dataOutputStream.writeUTF(send);
                } else if (received.equals(CODE + "exit")) {
                    isLoggedIn = false;
                    Server.checkOnlineClients();
                    break;
                } else if (received.equals(CODE + "chat")) {
                    Chat chat = new Chat(dataInputStream, dataOutputStream, clientName);
                    chat.startChat();
                } else if (received.equals(CODE + "group")) {
                    String clients = dataInputStream.readUTF();
                    Group group1 = Server.getGroupByName(clients.substring(clients.indexOf('.') + 1));
                    if (group1 == null) {
                        Group group = new Group(clients.substring(0, clients.lastIndexOf('.'))
                                , clients.substring(clients.indexOf('.') + 1), dataOutputStream, dataInputStream);
                        Server.addGroup(group);
                        group.startGroup();
                    } else {
                        group1.startGroup();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        try {
            this.dataInputStream.close();
            this.dataOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getClientName() {
        return clientName;
    }

    private void sendClientsArrayList() {
        String clients = CODE;
        for (int i = 0; i < Server.getClients().size(); i++) {
            if (i != 0)
                clients += "." + Server.getClients().get(i).getClientName();
            else
                clients += Server.getClients().get(i).getClientName();
        }
        try {
            dataOutputStream.writeUTF(clients);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DataInputStream getDataInputStream() {
        return dataInputStream;
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    public boolean getLoggedInStatus() {
        return isLoggedIn;
    }
}


