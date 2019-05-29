package sample;

import javafx.application.Platform;
import javafx.scene.control.ListView;


import java.io.IOException;

public class ListRefreshThread extends Thread {
    private static final String CODE = "5780";
    private ListView[] listView;
    private Client client;
    private boolean isGroup;

    public ListRefreshThread(ListView[] listView, Client client, boolean isGroup) {
        this.client = client;
        this.isGroup = isGroup;
        this.listView = listView;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                break;
            }
            Platform.runLater(() -> {
                try {
                    Controller.fillTheClientsList(listView, client.requestGroupAndClientList(CODE), client);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
