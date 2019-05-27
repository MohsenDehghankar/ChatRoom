
package sample;

import java.io.*;
import java.util.*;
import java.net.*;


public class Server {

    private static ArrayList<ClientHandler> clients = new ArrayList<>();
    private static ArrayList<Group> groups = new ArrayList<>();
    private static int i = 1;

    public static void main(String[] args) throws IOException {
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

    public synchronized static void checkOnlineClients() {
        ArrayList<ClientHandler> toDelete = new ArrayList<>();
        for (ClientHandler client : clients) {
            if (!client.getLoggedInStatus())
                toDelete.add(client);
        }
        for (ClientHandler handler : toDelete) {
            System.out.println("Client : " + handler.getClientName() + " Disconnected.");
            clients.remove(handler);
        }
    }

    public static ArrayList<ClientHandler> getClients() {
        return clients;
    }

    public static void addGroup(Group group) {
        groups.add(group);
    }

    public static ArrayList<Group> getGroups() {
        return groups;
    }


    public static Group getGroupByName(String name) {
        for (Group group : groups) {
            if (group.getName().equals(name))
                return group;
        }
        return null;
    }
}
