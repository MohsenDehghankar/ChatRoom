package sample;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class Controller {
    private static Controller controller = new Controller();

    private Controller() {
    }

    public static Controller getInstance() {
        return controller;
    }

    public void showMainMenu(Stage stage) {
        Group root = new Group();
        Scene scene = new Scene(root, 400, 400);
        scene.setFill(Color.BLACK);
        scene.addEventHandler(KeyEvent.KEY_PRESSED, (key) -> {
            if (key.getCode() == KeyCode.ESCAPE)
                System.exit(0);
        });
        Label label = new Label("status");
        label.relocate(100, 100);
        TextField textField = new TextField("Enter User Name ...");
        textField.relocate(200, 200);
        textField.setOnAction(actionEvent -> {
            if (textField.getText().split(" ").length > 1) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Invalid User Name ( no Space )");
                alert.show();
            } else {
                //TODO
                connectToServer(textField.getText(), label, stage);
            }
        });
        root.getChildren().add(label);
        root.getChildren().add(textField);
        stage.setScene(scene);
        stage.setTitle("Chat Room");
        stage.show();
    }


    private void connectToServer(String name, Label status, Stage stage) {
        try {
            InetAddress ip = InetAddress.getByName("localhost");
            Socket s = new Socket(ip, 1234);
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            dos.writeUTF(name);
            status.setText("Connected");
            Group root = new Group();
            Scene scene = new Scene(root, 400, 400);
            TextField contactName = new TextField("Enter Contact Name");
            ListView<String> listView = new ListView<>();
            listView.relocate(100, 100);
            listView.addEventFilter(KeyEvent.KEY_PRESSED, (key) -> {
                if (key.getCode() == KeyCode.ESCAPE)
                    showMainMenu(stage);
            });
            listView.setOnMouseClicked(mouseEvent -> {
                String contact = listView.getSelectionModel().getSelectedItem();
                if (contact != null)
                    chat(name, contact, stage, dis, dos);
            });
            Button refresh = new Button("REFRESH");
            refresh.relocate(10, 200);
            refresh.setOnMouseClicked(mouseEvent -> {
                try {
                    dos.writeUTF("clients");
                    if (dis.available() > 0)
                        fillTheClientsList(dis, name,listView);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            fillTheClientsList(dis, name,listView);
            addToStage(stage, scene, root, listView, contactName, refresh);
        } catch (Exception e) {
            e.printStackTrace();
            status.setText("Not Connected :(");
        }
    }

    private void fillTheClientsList(DataInputStream dis, String name,ListView<String> listView) throws IOException {
        if (dis.available() > 0) {
            String st = dis.readUTF();
            if (st.substring(0, 4).equals("5780")) {
                st = st.substring(4);
                ArrayList<String> clients = getArrayList(st);
                listView.getItems().removeAll();
                for (String client : clients) {
                    if (!client.equals(name) && !listView.getItems().contains(client))
                        listView.getItems().add(client);
                }
            }
        }
    }

    private void chat(String name, String contactName, Stage stage, DataInputStream dis, DataOutputStream dos) {
        Group root = new Group();
        Scene scene = new Scene(root, 400, 400);
        scene.setFill(Color.GRAY);
        Label userName = new Label("You");
        Label contactNameLabel = new Label("Your Contact : " + contactName);
        userName.relocate(300, 10);
        contactNameLabel.relocate(20, 10);
        ListView<String>[] messageHistories = getChatHistoryLists();
        TextField message = getMessageBox(dos, contactName, messageHistories[1]);
        Thread thread = getRefreshingThread(dis, contactName, messageHistories[0]);
        thread.setDaemon(true);
        thread.start();
        addToStage(stage, scene, root, message, userName, contactNameLabel, messageHistories[0], messageHistories[1]);
    }

    private ListView<String>[] getChatHistoryLists() {
        ListView<String>[] lists = new ListView[2];
        ListView<String> firstClient = new ListView<>();
        ListView<String> secondClient = new ListView<>();
        firstClient.setPrefSize(150, 300);
        secondClient.setPrefSize(150, 300);
        firstClient.relocate(220, 30);
        secondClient.relocate(30, 30);
        lists[1] = firstClient;
        lists[0] = secondClient;
        return lists;
    }

    private Thread getRefreshingThread(DataInputStream dis, String contactName, ListView<String> contactMessage) {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Platform.runLater(() -> {
                    try {
                        if (dis.available() > 0) {
                            String received = dis.readUTF();
                            if (received.length() < 4
                                    || (!received.substring(0, 4).equals("5780"))) {
                                String contactSent = received.substring(received.lastIndexOf('.') + 1);
                                String messageSent = received.substring(0, received.lastIndexOf('.'));
                                if (contactSent.equals(contactName))
                                    contactMessage.getItems().add(messageSent);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
        return thread;
    }

    private TextField getMessageBox(DataOutputStream dos, String contactName, ListView<String> firstList) {
        TextField message = new TextField("");
        message.setPromptText("Type Message ...");
        message.relocate(170, 370);
        message.setPrefWidth(200);
        message.setOnAction(actionEvent -> {
            try {
                dos.writeUTF(message.getText() + "." + contactName);
                firstList.getItems().add(message.getText());
                message.setText("");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        /*message.addEventFilter(KeyEvent.KEY_PRESSED, (keyEvent -> {
            if(keyEvent.getCode() == KeyCode.ESCAPE)

        }));*/
        return message;
    }

    private void showAvailableClients(Stage stage,String currentClientName,DataInputStream dis){
        Group root = new Group();
        Scene scene = new Scene(root,400,400);


       /* addToStage(stage,scene,root,);*/
    }

    private void addToStage(Stage stage, Scene scene, Group root, Node... nodes) {
        for (Node node : nodes) {
            root.getChildren().add(node);
        }
        stage.setScene(scene);
        stage.show();
    }

    private ArrayList<String> getArrayList(String fullString) {
        String[] seperated = fullString.split("\\.");
        ArrayList<String> result = new ArrayList<>();
        for (String s : seperated) {
            result.add(s);
        }
        return result;
    }

    /*private void showChatMenu(Stage stage, Client loggedInClient) {
        Group root = new Group();
        Scene scene = new Scene(root, 400, 400);
        Label label = new Label(loggedInClient.getName());
        label.relocate(10, 10);
        ListView<String> otherClients = new ListView<>();
        otherClients.relocate(100, 10);
        otherClients.addEventHandler(KeyEvent.KEY_PRESSED, (key -> {
            if (key.getCode() == KeyCode.ESCAPE)
                showMainMenu(stage);
        }));

        for (Client client : Client.getClients()) {
            if (!client.getName().equals(loggedInClient.getName())) {
                otherClients.getItems().add(client.getName());
            }
        }
        otherClients.setOnMouseClicked(mouseEvent -> {
            Client contact = Client.searchClient(otherClients.getSelectionModel().getSelectedItem());
            if (contact != null)
                showChat(stage, loggedInClient,
                        contact);
            else
                System.out.println(otherClients.getSelectionModel().getSelectedItems());
        });
        root.getChildren().add(otherClients);
        root.getChildren().add(label);
        stage.setTitle("Chat Selection");
        stage.setScene(scene);
        stage.show();
    }

    private void showChat(Stage stage, Client firstClient, Client secondClient) {
        Group root = new Group();
        Scene scene = new Scene(root, 400, 400);
        scene.addEventHandler(KeyEvent.KEY_PRESSED, (key -> {
            if (key.getCode() == KeyCode.ESCAPE)
                showChatMenu(stage,firstClient);
        }));
        Label firstClientName = new Label("You : " + firstClient.getName());
        Label secondClientName = new Label("Your Contact : " + secondClient.getName());
        firstClientName.relocate(300,350);
        secondClientName.relocate(20,350);
        // TODO

        //
        root.getChildren().add(firstClientName);
        root.getChildren().add(secondClientName);
        stage.setTitle("Chat With :" + secondClient.getName());
        stage.setScene(scene);
        stage.show();
    }*/
}
