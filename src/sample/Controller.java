package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
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
            showFirstMenu();
        } else {
            statusLabel.setText("Not Connected :(");
        }
    }

    /*private void showActionChooseMenu() {
        javafx.scene.Group root = new javafx.scene.Group();
        Scene scene = new Scene(root, 300, 300);
        Button chat = new Button("Chat");
        chat.setOnMouseClicked(mouseEvent -> {
            showFirstMenu();
        });
        Button group = new Button("ServerGroup");
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
    }*/

    /*private void showGroupMenu() {
        javafx.scene.Group root = new javafx.scene.Group();
        Scene scene = new Scene(root, 400, 400);
        Button create = new Button("Create ServerGroup");
        create.relocate(10, 10);
        ListView<String> groups = new ListView<>();
        groups.relocate(150, 10);
        Thread thread = new ListRefreshThread(groups, client, true);
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
            client.startGroup(CODE, selectedItem, "");
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
    }*/

    private void showFirstMenu() {
        String name = client.getName();
        Group root = new Group();
        Scene scene = new Scene(root, 400, 400);
        Label label = new Label("Active Clients : ");
        label.relocate(10, 10);
        Label groupsLabel = new Label("Your Groups :");
        groupsLabel.relocate(120, 10);
        Label userName = new Label("Your Name : " + name);
        userName.relocate(250, 10);
        ListView<String> groupsList = new ListView<>();
        groupsList.relocate(120, 30);
        groupsList.setPrefSize(100, 200);
        ListView<String> listView = new ListView<>();
        listView.relocate(10, 30);
        listView.setPrefSize(100, 200);
        Thread listRefreshThread = new ListRefreshThread(new ListView[]{listView, groupsList}, client, false);
        listRefreshThread.setDaemon(true);
        listRefreshThread.start();
        setListViewEvent(listView, listRefreshThread);
        setGroupsListEvent(groupsList, listRefreshThread);
        try {
            fillTheClientsList(new ListView[]{listView, groupsList}, client.requestGroupAndClientList(CODE), client);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Node[] nodes = getCreateGroupNodes();
        addToStage(stage, scene, root, listView, userName, label,
                groupsLabel, groupsList, nodes[0], nodes[1], nodes[2]);
    }

    private void setGroupsListEvent(ListView<String> groupsList, Thread thread) {
        groupsList.setOnMouseClicked(mouseEvent -> {
            String groupName = groupsList.getSelectionModel().getSelectedItem();
            try {
                if (groupName != null) {
                    boolean b = client.enterAGroup(groupName);
                    if (b) {
                        thread.interrupt();
                        showChatScene(groupName, true);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private Node[] getCreateGroupNodes() {
        Button button = new Button("Create Group");
        button.relocate(260, 80);
        TextField textField = new TextField();
        textField.setPromptText("members");
        TextField groupName = new TextField();
        groupName.setPromptText("group name");
        groupName.relocate(240, 100);
        textField.relocate(240, 40);
        button.setOnAction(mouseEvent -> {
            try {
                client.createGroup(groupName.getText(), textField.getText());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return new Node[]{textField, groupName, button};
    }

    private void setListViewEvent(ListView<String> listView, Thread thread) {
        listView.setOnMouseClicked(mouseEvent -> {
            String contact = listView.getSelectionModel().getSelectedItem();
            if (contact != null) {
                thread.interrupt();
                client.startChat(CODE, contact);
                showChatScene(contact, false);
            }
        });
        listView.addEventFilter(KeyEvent.KEY_PRESSED, (key) -> {
            if (key.getCode() == KeyCode.ESCAPE) {
                try {
                    client.getDataOutputStream().writeUTF(CODE + "exit");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                thread.interrupt();
                showMainMenu();
            }
        });
    }


    public static void fillTheClientsList(ListView[] listView, ArrayList<String> clientsAndGroups, Client currentClient) {
        if (clientsAndGroups != null) {
            listView[0].getItems().clear();
            listView[1].getItems().clear();
            int i = 0;
            for (String clientsAndGroup : clientsAndGroups) {
                if (i == 0) {
                    if (clientsAndGroup.equals("change"))
                        i = 1;
                    else if (!currentClient.getName().equals(clientsAndGroup)
                            && !listView[0].getItems().contains(currentClient))
                        listView[0].getItems().add(clientsAndGroup);
                } else {
                    listView[1].getItems().add(clientsAndGroup);
                }
            }
            /*listView.getItems().clear();
            for (String client : clients) {
                if (!client.equals(currentClient.getName()) && !listView.getItems().contains(client))
                    listView.getItems().add(client);
            }*/
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
        Thread thread = new ChatRefreshThread(client, isGroup, messageHistories, contactName);
        thread.setDaemon(true);
        thread.start();
        setMessageEvent(message, thread, isGroup);
        Button emojiButton = getEmojis(10, 440, messageHistories, isGroup, contactName);
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
                    System.out.println("salam");
                    e.printStackTrace();
                }
                showFirstMenu();
            }
        }));
    }

    private Button getEmojis(int x, int y, ListView[] list, boolean isGroup, String groupName) {
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
                if (!isGroup) {
                    ImageView imageView1 = client.getCurrentChat().sendEmoji(index, CODE, false, false);
                    list[1].getItems().add(imageView1);
                    list[0].getItems().add(client.getCurrentChat().sendEmoji(1, CODE, true, false));
                } else {
                    ImageView imageView1 = new ClientChat(client, groupName)
                            .sendEmoji(index, CODE, false, true);
                    list[1].getItems().add(imageView1);
                    list[0].getItems().add(new ClientChat(client, groupName)
                            .sendEmoji(1, CODE, true, true));
                }
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

    private TextField getMessageBox(ListView<String>[] lists, Label replyTo, boolean isGroup, String groupName) {
        TextField message = new TextField("");
        message.setPromptText("Type Message ...");
        message.relocate(250, 440);
        message.setPrefWidth(200);
        message.setOnAction(actionEvent -> {
            try {
                String sent;
                if (isGroup) {
                    sent = new ClientChat(client, groupName)
                            .sendMessage(replyTo.getText(), message.getText(), true);
                } else
                    sent = client.getCurrentChat()
                            .sendMessage(replyTo.getText(), message.getText(), false);
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
