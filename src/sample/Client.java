package sample;

import java.util.ArrayList;

public class Client {
    private static ArrayList<Client> clients = new ArrayList<>();
    private String name;

    public Client(String name){
        this.name = name;
        clients.add(this);
    }

    public String getName() {
        return name;
    }

    public static ArrayList<Client> getClients() {
        return clients;
    }

    public static Client searchClient(String name){
        for (Client client : clients) {
            if(client.getName().equals(name))
                return client;
        }
        return null;
    }
}
