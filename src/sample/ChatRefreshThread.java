package sample;

import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;

import java.io.IOException;

public class ChatRefreshThread extends Thread {
    private Client client;
    private boolean isGroup;
    private String CODE = "5780";
    private ListView[] lists;
    private String groupName;

    public ChatRefreshThread(Client client, boolean isGroup, ListView[] lists, String groupName) {
        this.client = client;
        this.isGroup = isGroup;
        this.lists = lists;
        this.groupName = groupName;
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
                    if (!isGroup && client.getDataInputStream().available() > 0) {
                        String received = client.getDataInputStream().readUTF();
                        if (received.length() >= 10 &&
                                received.substring(0, 10).equals(CODE + "emoji.")) {
                            ImageView view = client.getCurrentChat().receiveEmoji(received, false);
                            if (view != null) {
                                lists[0].getItems().add(view);
                                lists[1].getItems().add(client.getCurrentChat().receiveEmoji("", true));
                            }
                        } else if (received.length() < 4
                                || (!received.substring(0, 4).equals("5780"))) {
                            String message = client.getCurrentChat().receiveMessage(received);
                            if (message != null) {
                                lists[0].getItems().add(message);
                                lists[1].getItems().add(" ");
                            }
                        }
                    } else if (isGroup && client.getDataInputStream().available() > 0) {
                        String received = client.getDataInputStream().readUTF();
                        if (received.contains(".") && received.split("\\.").length == 2) {
                            String[] split = received.split("\\.");
                            String group = split[1];
                            if (group.equals(groupName)) {
                                lists[0].getItems().add(split[0]);
                                lists[1].getItems().add(" ");
                            }
                        } else if (received.contains(".") && received.split("\\.")[0].equals("5780emoji")) {
                            ClientChat clientChat = new ClientChat(client, groupName);
                            ImageView imageView = clientChat.receiveEmoji(received, false);
                            ImageView imageView2 = clientChat.receiveEmoji(received, true);
                            lists[0].getItems().add(imageView);
                            lists[1].getItems().add(imageView2);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
