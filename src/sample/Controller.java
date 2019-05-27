package sample;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


public class Controller {
    private static Controller controller = new Controller();
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String CODE = "5780";
    private Stage stage;
    private Client client;

    private Controller() {
    }

    public static Controller getInstance(Client client, Stage stage) {
        controller.client = client;
        controller.stage = stage;
        return controller;
    }

    public void showMainMenu() {
        javafx.scene.Group root = new javafx.scene.Group();
        Scene scene = new Scene(root, 200, 200);
        scene.setFill(Color.BLACK);
        scene.addEventHandler(KeyEvent.KEY_PRESSED, (key) -> {
            if (key.getCode() == KeyCode.ESCAPE)
                System.exit(0);
        });
        Label label = new Label(" ");
        label.setTextFill(Color.WHITE);
        label.relocate(10, 10);
        root.getChildren().add(label);
        root.getChildren().add(getUserNameTextField(label));
        stage.setScene(scene);
        stage.setTitle("Chat Room");
        stage.show();
    }

    private TextField getUserNameTextField(Label label) {
        TextField textField = new TextField("Enter User Name ...");
        textField.relocate(20, 70);
        textField.setOnAction(actionEvent -> {
            if (textField.getText().split(" ").length > 1) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Invalid User Name ( no Space )");
                alert.show();
            } else {
                boolean connectionState = client.connectToServer(textField.getText());
                handleConnectionState(connectionState, label);
            }
        });
        return textField;
    }


    private void handleConnectionState(boolean status, Label statusLabel) {
        if (status) {
            showActionChooseMenu();
        } else {
            statusLabel.setText("Not Connected :(");
        }
    }

    private void showActionChooseMenu() {
        javafx.scene.Group root = new javafx.scene.Group();
        Scene scene = new Scene(root, 300, 300);
        Button chat = new Button("Chat");
        chat.setOnMouseClicked(mouseEvent -> {
            showClientsList();
        });
        Button group = new Button("Group");
        group.setOnMouseClicked(mouseEvent -> {
            showGroupMenu();
        });
        Button exit = new Button("Exit");
        exit.relocate(70, 200);
        exit.setOnMouseClicked(mouseEvent -> {
            try {
                client.getDataOutputStream().writeUTF(CODE + "exit");
            } catch (IOException e) {
                e.printStackTrace();
            }
            showMainMenu();
        });
        chat.relocate(70, 50);
        group.relocate(70, 150);
        addToStage(stage, scene, root, chat, group, exit);
    }

    private void showGroupMenu() {
        javafx.scene.Group root = new javafx.scene.Group();
        Scene scene = new Scene(root, 400, 400);
        Button create = new Button("Create Group");
        create.relocate(10, 10);
        ListView<String> groups = new ListView<>();
        groups.relocate(150, 10);
        Thread thread = getClientListRefreshingThread(groups, true);
        thread.setDaemon(true);
        thread.start();
        TextField groupName = new TextField("enter name for group...");
        TextField members = new TextField("enter members by space");
        members.relocate(10, 250);
        members.setPrefWidth(170);
        groupName.relocate(10, 200);
        groups.setOnMouseClicked(mouseEvent -> {
            String selectedItem = groups.getSelectionModel().getSelectedItem();
            thread.interrupt();
            client.startGroup(CODE,selectedItem,"");
            showChatScene(selectedItem, true);
        });
        create.setOnMouseClicked(mouseEvent -> {
            thread.interrupt();
            String name = groupName.getText();
            //TODO
            client.startGroup(CODE, name, members.getText());
            showChatScene(name, true);
        });
        scene.addEventHandler(KeyEvent.KEY_PRESSED, (keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ESCAPE) {
                thread.interrupt();
                showActionChooseMenu();
            }
        }));
        addToStage(stage, scene, root, groupName, members, create, groups);
    }

    private void showClientsList() {
        String name = client.getName();
        javafx.scene.Group root = new javafx.scene.Group();
        Scene scene = new Scene(root, 400, 400);
        Label label = new Label("Active Clients : ");
        label.relocate(10, 15);
        ListView<String> listView = new ListView<>();
        listView.relocate(10, 30);
        listView.setPrefSize(200, 200);
        Thread listRefreshThread = getClientListRefreshingThread(listView, false);
        listRefreshThread.setDaemon(true);
        listRefreshThread.start();
        listView.addEventFilter(KeyEvent.KEY_PRESSED, (key) -> {
            if (key.getCode() == KeyCode.ESCAPE) {
                listRefreshThread.interrupt();
                showActionChooseMenu();
            }
        });
        listView.setOnMouseClicked(mouseEvent -> {
            String contact = listView.getSelectionModel().getSelectedItem();
            if (contact != null) {
                listRefreshThread.interrupt();
                client.startChat(CODE, contact);
                showChatScene(contact, false);
            }
        });
        Label userName = new Label("Your Name : " + name);
        userName.relocate(200, 10);
        try {
            fillTheClientsList(listView, client.requestClientsList(CODE));
        } catch (IOException e) {
            e.printStackTrace();
        }
        addToStage(stage, scene, root, listView, userName, label);
    }

    private Thread getClientListRefreshingThread(ListView<String> listView, boolean isGroup) {
        Thread thread = new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
                Platform.runLater(() -> {
                    try {
                        if (!isGroup)
                            fillTheClientsList(listView, client.requestClientsList(CODE));
                        else
                            fillTheClientsList(listView, client.requestGroupList(CODE));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
        return thread;
    }

    private void fillTheClientsList(ListView<String> listView, ArrayList<String> clients) {
        if (clients != null) {
            listView.getItems().clear();
            for (String client : clients) {
                if (!client.equals(this.client.getName()) && !listView.getItems().contains(client))
                    listView.getItems().add(client);
            }
        }
    }

    private void showChatScene(String contactName, boolean isGroup) {
        String name = client.getName();
        javafx.scene.Group root = new javafx.scene.Group();
        Scene scene = new Scene(root, 500, 500);
        scene.setFill(Color.GRAY);
        Label userName = new Label("You : " + name);
        Label contactNameLabel = new Label("Your Contact : " + contactName);
        userName.relocate(250, 10);
        Label replyTo = getReplyToLabel();
        contactNameLabel.relocate(5, 10);
        ListView[] messageHistories = getChatHistoryLists(replyTo);
        TextField message = getMessageBox(messageHistories, replyTo, isGroup, contactName);
        Thread thread = getChatRefreshingThread(messageHistories, isGroup, contactName);
        thread.setDaemon(true);
        thread.start();
        setMessageEvent(message, thread, isGroup);
        Button emojiButton = getEmojis(10, 440, messageHistories);
        addToStage(stage, scene, root, message, userName, contactNameLabel,
                messageHistories[0], messageHistories[1], replyTo, emojiButton);
    }

    private void setMessageEvent(TextField message, Thread thread, boolean isGroup) {
        message.addEventHandler(KeyEvent.KEY_PRESSED, (keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ESCAPE) {
                thread.interrupt();
                try {
                    client.sendExitToServer(CODE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (!isGroup) {
                    showClientsList();
                } else {
                    showActionChooseMenu();
                }
            }
        }));
    }

    private Button getEmojis(int x, int y, ListView[] list) {
        Stage stage = new Stage();
        javafx.scene.Group root = new javafx.scene.Group();
        Scene scene = new Scene(root, 300, 300);
        stage.setScene(scene);
        Button emoji = new Button("EMOJI");
        emoji.relocate(x, y);
        ListView<ImageView> listView = new ListView<>();
        for (int i = 1; i <= 4; i++) {
            listView.getItems().add(new ImageView(
                    new Image("images/" + i + ".jpg")));
        }
        listView.setOnMouseClicked(mouseEvent -> {
            ImageView imageView = listView.getSelectionModel().getSelectedItem();
            int index = listView.getItems().indexOf(imageView);
            index++; // number of emoji
            try {
                ImageView imageView1 = client.getCurrentChat().sendEmoji(index, CODE, false);
                list[1].getItems().add(imageView1);
                list[0].getItems().add(client.getCurrentChat().sendEmoji(1, CODE, true));
            } catch (IOException e) {
                e.printStackTrace();
            }
            stage.close();
        });
        root.getChildren().add(listView);
        emoji.setOnMouseClicked(mouseEvent -> {
            stage.show();
        });
        return emoji;
    }


    private Label getReplyToLabel() {
        Label replyTo = new Label("Reply To : No Message");
        replyTo.relocate(100, 450);
        return replyTo;
    }

    private ListView<String>[] getChatHistoryLists(Label replyTo) {
        ListView[] lists = new ListView[2];
        ListView firstClient = new ListView();
        ListView secondClient = new ListView();
        firstClient.setPrefSize(240, 400);
        secondClient.setPrefSize(240, 400);
        firstClient.relocate(250, 30);
        secondClient.relocate(5, 30);
        secondClient.setOnMouseClicked(mouseEvent -> {
            String replyingMessage = secondClient.getSelectionModel().getSelectedItem().toString();
            if (replyingMessage != null) {
                if (replyingMessage.length() < 11 || !replyingMessage.substring(0, 11).equals("Reply To : "))
                    replyTo.setText("Reply To : " + replyingMessage);
                else
                    replyTo.setText("Reply To : " + replyingMessage.substring(replyingMessage.lastIndexOf('>') + 2));
            }
        });
        lists[1] = firstClient;
        lists[0] = secondClient;
        return lists;
    }

    private Thread getChatRefreshingThread(ListView[] lists, boolean isGroup, String group) {
        Thread thread = new Thread(() -> {
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
                        } else if (isGroup) {
                            fillGroupMessages(client.requestGroupMessages(CODE), lists[0]);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
        return thread;
    }

    private void fillGroupMessages(ArrayList<String> messages, ListView listView) {
        if (messages != null) {
            listView.getItems().clear();
            for (String message : messages) {
                String[] split = message.split("\\.");
                if (split.length == 3) {
                    listView.getItems().add(split[2] + " sent :");
                    listView.getItems().add(new ImageView(ClientChat.getCopyOfEmoji("images/" + split[1]
                            + ".jpg")));
                } else if (split.length == 2) {
                    listView.getItems().add(split[0] + "( " + split[1] + " said )");
                }
            }
        }
    }

    private TextField getMessageBox(ListView<String>[] lists, Label replyTo, boolean isGroup, String groupName) {
        TextField message = new TextField("");
        message.setPromptText("Type Message ...");
        message.relocate(250, 440);
        message.setPrefWidth(200);
        message.setOnAction(actionEvent -> {
            try {
                String sent;
                if (isGroup) {
                    sent = new ClientChat(client, groupName).sendMessage(replyTo.getText(), message.getText());
                } else
                    sent = client.getCurrentChat().sendMessage(replyTo.getText(), message.getText());
                lists[1].getItems().add(sent);
                lists[0].getItems().add(" ");
                message.setText("");
                replyTo.setText("Reply To : No Message");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return message;
    }

    private void addToStage(Stage stage, Scene scene, javafx.scene.Group root, Node... nodes) {
        for (Node node : nodes) {
            root.getChildren().add(node);
        }
        stage.setScene(scene);
        stage.show();
    }
}
