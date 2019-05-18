// Java implementation of Server side
// It contains two classes : Server and ClientHandler
// Save file as Server.java
package sample;

import java.io.*;
import java.util.*;
import java.net.*;

// Server class
public class Server {

    // Vector to store active clients
    private static ArrayList<ClientHandler> clients = new ArrayList<>();

    // counter for clients
    static int i = 1;

    public static ClientHandler searchClients(String name) {
        for (ClientHandler client : clients) {
            if (client.getName().equals(name))
                return client;
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        // server is listening on port 1234
        ServerSocket ss = new ServerSocket(1234);
        Socket s;
        while (true) {
            s = ss.accept();
            System.out.println("New client request received : " + s);
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            System.out.println("Creating a new handler for this client...");
            String name = dis.readUTF();
            ClientHandler newHandler = new ClientHandler(s, name, dis, dos);
            System.out.println("client " + name + " connected");
            Thread t = new Thread(newHandler);
            System.out.println("Adding this client to active client list");
            clients.add(newHandler);
            t.start();
            i++;
        }
    }

    public static ArrayList<ClientHandler> getClients() {
        return clients;
    }
}

// ClientHandler class
class ClientHandler implements Runnable {
    private String name;
    private final DataInputStream dis;
    private final DataOutputStream dos;
    private Socket s;
    private boolean isloggedin;

    public ClientHandler(Socket s, String name,
                         DataInputStream dis, DataOutputStream dos) {
        this.dis = dis;
        this.dos = dos;
        this.name = name;
        this.s = s;
        this.isloggedin = true;
    }

    @Override
    public void run() {
        sendClientsArrayList();
        String received;
        while (true) {
            try {
                // receive the string
                received = dis.readUTF();
                System.out.println(received);
                if (received.equals("clients")) {
                    sendClientsArrayList();
                } else if (received.equals("exit"))
                    break;
                else {
                    String message = received.substring(0, received.lastIndexOf('.'));
                    String contact = received.substring(received.lastIndexOf('.') + 1);
                    for (ClientHandler client : Server.getClients()) {
                        if (client.getName().equals(contact))
                            client.dos.writeUTF(message + "." + name);
                    }
                }
            } catch (IOException e) {

                e.printStackTrace();
            }

        }
        try {
            // closing resources
            this.dis.close();
            this.dos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    private void sendClientsArrayList() {
        String clients = "5780";
        for (int i = 0; i < Server.getClients().size(); i++) {
            if (i != 0)
                clients += "." + Server.getClients().get(i).getName();
            else
                clients += Server.getClients().get(i).getName();
        }
        try {
            dos.writeUTF(clients);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
