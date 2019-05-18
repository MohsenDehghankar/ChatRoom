package sample;

import java.util.ArrayList;

public class Chat {
    private static ArrayList<Chat> chats = new ArrayList<>();

    private ClientHandler firstClient;
    private ClientHandler secondClient;

    public Chat(ClientHandler firstClient, ClientHandler secondClient) {
        this.firstClient = firstClient;
        this.secondClient = secondClient;
        chats.add(this);
    }
}
