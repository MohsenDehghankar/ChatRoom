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
            //
            System.out.println("client " + name + " connected");
            //
            Thread t = new Thread(newHandler);
            System.out.println("Adding this client to active client list");
            clients.add(newHandler);
            //TODO
            for (ClientHandler client : clients) {
                System.out.println(client.getName());
            }
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
    Scanner scn = new Scanner(System.in);
    private String name;
    final DataInputStream dis;
    final DataOutputStream dos;
    Socket s;
    boolean isloggedin;

    // constructor
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

                if (received.equals("logout")) {
                    this.isloggedin = false;
                    this.s.close();
                    break;
                }

                // break the string into message and recipient part
                String[] receiveds = received.split(" ");
                String MsgToSend = receiveds[0];
                String recipient = receiveds[1];

                // search for the recipient in the connected devices list.
                // clients is the vector storing client of active users
                for (ClientHandler mc : Server.getClients()) {
                    // if the recipient is found, write on its
                    // output stream
                    //
                    System.out.println(mc.name);
                    //
                    if (mc.name.equals(recipient) && mc.isloggedin == true) {
                        mc.dos.writeUTF(this.name + " : " + MsgToSend);
                        break;
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
        String clients = "";
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
