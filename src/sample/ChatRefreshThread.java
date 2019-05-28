package sample;

import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class ChatRefreshThread extends Thread {
    private Client client;
    private boolean isGroup;
    private String CODE = "5780";
    private ListView[] lists;
    private String groupName;
    private static int uniqueID = 1;

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
                        } else if (received.length() >= 9 && received.substring(0, 9).equals("5780image")) {
                            String contact = received.substring(9, received.lastIndexOf('.'));
                            long length = Integer.parseInt(received.substring(received.lastIndexOf('.') + 1));
                            System.out.println(client.getName());
                            if (contact.equals(client.getName())) {
                                FileOutputStream f =
                                        new FileOutputStream("src/images/clientTemp/"
                                                + client.getName() + uniqueID++ + "image" + ".jpg");
                                int count;
                                byte[] buffer = new byte[70000]; // or 4096, or more
                                while ((count = client.getDataInputStream().read(buffer)) > 0) {
                                    f.write(buffer, 0, count);
                                    if (count == length)
                                        break;
                                }
                                f.flush();
                                f.close();
                                FileInputStream fileInputStream =
                                        new FileInputStream("src/images/clientTemp/"
                                                + client.getName() + (uniqueID - 1) + "image" + ".jpg");
                                Image image = new Image(fileInputStream);
                                lists[0].getItems().add(new ImageView(image));
                                lists[1].getItems().add("\n\n\n\n\n\n\n");
                            }

                        }
                    } else if (isGroup && client.getDataInputStream().available() > 0) {
                        String received = client.getDataInputStream().readUTF();
                        if (received.contains(".") && received.split("\\.")[0].equals("5780emoji")) {
                            ClientChat clientChat = new ClientChat(client, groupName);
                            ImageView imageView = clientChat.receiveEmoji(received, false);
                            ImageView imageView2 = clientChat.receiveEmoji(received, true);
                            lists[0].getItems().add(imageView);
                            lists[1].getItems().add(imageView2);
                        } else if (received.length() >= 9 && received.substring(0, 9).equals("5780image")) {
                            //TODO
                            System.out.println("salammmmmmmm");
                            System.out.println(groupName);
                            String group = received.substring(9, received.lastIndexOf('.'));
                            System.out.println(group);
                            long length = Integer.parseInt(received.substring(received.lastIndexOf('.') + 1));
                            if (group.equals(groupName)) {
                                //TODO
                                System.out.println("salam");
                                FileOutputStream f =
                                        new FileOutputStream("src/images/clientTemp/"
                                                + client.getName() + uniqueID++ + "image" + ".jpg");
                                int count;
                                byte[] buffer = new byte[70000]; // or 4096, or more
                                while ((count = client.getDataInputStream().read(buffer)) > 0) {
                                    f.write(buffer, 0, count);
                                    if (count == length)
                                        break;
                                }
                                f.flush();
                                f.close();
                                FileInputStream fileInputStream =
                                        new FileInputStream("src/images/clientTemp/"
                                                + client.getName() + (uniqueID - 1) + "image" + ".jpg");
                                Image image = new Image(fileInputStream);
                                lists[0].getItems().add(new ImageView(image));
                                lists[1].getItems().add("\n\n\n\n\n\n\n");
                            }
                        } else if (received.contains(".") && received.split("\\.").length == 2) {
                            String[] split = received.split("\\.");
                            String group = split[1];
                            if (group.equals(groupName)) {
                                lists[0].getItems().add(split[0]);
                                lists[1].getItems().add(" ");
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
