package sample;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private static final String CODE = "5780";
    private String name;
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
        this.name = name;
        this.s = s;
        this.isLoggedIn = true;
    }

    @Override
    public void run() {
        sendClientsArrayList();
        String received;
        while (true) {
            try {
                received = dataInputStream.readUTF();
                if (received.equals(CODE + "clients")) {
                    sendClientsArrayList();
                } else if (received.equals(CODE + "exit")) {
                    isLoggedIn = false;
                    Server.checkOnlineClients();
                    break;
                } else {
                    String message = received.substring(0, received.lastIndexOf('.'));
                    String contact = received.substring(received.lastIndexOf('.') + 1);
                    for (sample.ClientHandler client : Server.getClients()) {
                        if (client.getName().equals(contact))
                            client.dataOutputStream.writeUTF(message + "." + name);
                    }
                }
            } catch (IOException e) {
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

    public String getName() {
        return name;
    }

    private void sendClientsArrayList() {
        String clients = CODE;
        for (int i = 0; i < Server.getClients().size(); i++) {
            if (i != 0)
                clients += "." + Server.getClients().get(i).getName();
            else
                clients += Server.getClients().get(i).getName();
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

    public boolean getLoggedInStatus(){
        return isLoggedIn;
    }
}


