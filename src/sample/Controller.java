package sample;

import javafx.application.Platform;
import javafx.collections.ObservableList;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


public class Controller {
    private static Controller controller = new Controller();
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String CODE = "5780";

    private Controller() {
    }

    public static Controller getInstance() {
        return controller;
    }

    public void showMainMenu(Stage stage) {
        Group root = new Group();
        Scene scene = new Scene(root, 200, 200);
        scene.setFill(Color.BLACK);
        scene.addEventHandler(KeyEvent.KEY_PRESSED, (key) -> {
            if (key.getCode() == KeyCode.ESCAPE)
                System.exit(0);
        });
        Label label = new Label(" ");
        label.setTextFill(Color.WHITE);
        label.relocate(10, 10);
        TextField textField = new TextField("Enter User Name ...");
        textField.relocate(20, 70);
        textField.setOnAction(actionEvent -> {
            if (textField.getText().split(" ").length > 1) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Invalid User Name ( no Space )");
                alert.show();
            } else {
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
            showClientsList(stage, name, dos, dis);
        } catch (Exception e) {
            //e.printStackTrace();
            status.setText("Not Connected :(");
        }
    }

    private void showClientsList(Stage stage, String name, DataOutputStream dos, DataInputStream dis) {
        Group root = new Group();
        Scene scene = new Scene(root, 400, 400);
        Label label = new Label("Active Clients : ");
        label.relocate(10, 15);
        ListView<String> listView = new ListView<>();
        listView.relocate(10, 30);
        listView.setPrefSize(200, 200);

        Thread listRefreshThread = getClientListRefreshingThread(name, dos, dis, listView);
        listRefreshThread.setDaemon(true);
        listRefreshThread.start();
        listView.addEventFilter(KeyEvent.KEY_PRESSED, (key) -> {
            if (key.getCode() == KeyCode.ESCAPE) {
                listRefreshThread.interrupt();
                try {
                    dos.writeUTF(CODE + "exit");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                showMainMenu(stage);
            }
        });
        listView.setOnMouseClicked(mouseEvent -> {
            String contact = listView.getSelectionModel().getSelectedItem();
            if (contact != null) {
                listRefreshThread.interrupt();
                chat(contact, name, stage, dis, dos);
            }
        });
        Label userName = new Label("Your Name : " + name);
        userName.relocate(200, 10);
        try {
            fillTheClientsList(dis, name, listView);
        } catch (IOException e) {
            e.printStackTrace();
        }
        addToStage(stage, scene, root, listView, userName, label);
    }

    private Thread getClientListRefreshingThread(String name, DataOutputStream dos, DataInputStream dis, ListView<String> listView) {
        Thread thread = new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
                Platform.runLater(() -> {
                    try {
                        dos.writeUTF(CODE + "clients");
                        fillTheClientsList(dis, name, listView);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
        return thread;
    }

    private void fillTheClientsList(DataInputStream dis, String name, ListView<String> listView) throws IOException {
        if (dis.available() > 0) {
            String st = dis.readUTF();
            if (st.substring(0, 4).equals("5780")) {
                st = st.substring(4);
                ArrayList<String> clients = getArrayList(st);
                listView.getItems().clear();
                for (String client : clients) {
                    if (!client.equals(name) && !listView.getItems().contains(client))
                        listView.getItems().add(client);
                }
            }
        }
    }

    private void chat(String contactName, String name, Stage stage, DataInputStream dis, DataOutputStream dos) {
        Group root = new Group();
        Scene scene = new Scene(root, 600, 600);
        scene.setFill(Color.GRAY);
        Label userName = new Label("You : " + name);
        Label contactNameLabel = new Label("Your Contact : " + contactName);
        userName.relocate(300, 10);
        Label replyTo = getReplyToLabel();
        contactNameLabel.relocate(30, 10);
        ListView<String>[] messageHistories = getChatHistoryLists(replyTo);
        TextField message = getMessageBox(dos, contactName, messageHistories, replyTo);
        Thread thread = getChatRefreshingThread(dis, contactName, messageHistories);
        thread.setDaemon(true);
        thread.start();
        message.addEventHandler(KeyEvent.KEY_PRESSED, (keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ESCAPE) {
                thread.interrupt();
                showClientsList(stage, name, dos, dis);
            }
        }));
        addToStage(stage, scene, root, message, userName, contactNameLabel,
                messageHistories[0], messageHistories[1], replyTo);
    }

    private Label getReplyToLabel() {
        Label replyTo = new Label("Reply To : No Message");
        replyTo.relocate(100, 550);
        return replyTo;
    }

    private ListView<String>[] getChatHistoryLists(Label replyTo) { // first and second reversed
        ListView<String>[] lists = new ListView[2];
        ListView<String> firstClient = new ListView<>();
        ListView<String> secondClient = new ListView<>();
        firstClient.setPrefSize(250, 500);
        secondClient.setPrefSize(250, 500);
        firstClient.relocate(300, 30);
        secondClient.relocate(30, 30);
        secondClient.setOnMouseClicked(mouseEvent -> {
            String replyingMessage = secondClient.getSelectionModel().getSelectedItem();
            if (replyingMessage.length() < 11 || !replyingMessage.substring(0, 11).equals("Reply To : "))
                replyTo.setText("Reply To : " + replyingMessage);
            else
                replyTo.setText("Reply To : " + replyingMessage.substring(replyingMessage.lastIndexOf('>') + 2));
        });
        lists[1] = firstClient;
        lists[0] = secondClient;
        return lists;
    }

    private Thread getChatRefreshingThread(DataInputStream dis, String contactName, ListView<String>[] lists) {
        Thread thread = new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
                Platform.runLater(() -> {
                    try {
                        if (dis.available() > 0) {
                            String received = dis.readUTF();
                            if (received.length() < 4
                                    || (!received.substring(0, 4).equals("5780"))) {
                                String contactSent = received.substring(received.lastIndexOf('.') + 1);
                                String messageSent = received.substring(0, received.lastIndexOf('.'));
                                if (contactSent.equals(contactName)) {
                                    lists[0].getItems().add(messageSent);
                                    lists[1].getItems().add(" ");
                                }
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

    private TextField getMessageBox(DataOutputStream dos, String contactName,
                                    ListView<String>[] lists, Label replyTo) {
        TextField message = new TextField("");
        message.setPromptText("Type Message ...");
        message.relocate(300, 552);
        message.setPrefWidth(200);
        message.setOnAction(actionEvent -> {
            try {
                String reply = replyTo.getText();
                String relation = " -> ";
                if (reply.equals("Reply To : No Message") || reply.equals("Reply To :  ")) {
                    reply = "";
                    relation = "";
                }
                String toSend = reply + relation + message.getText();
                dos.writeUTF(toSend + "." + contactName);
                lists[1].getItems().add(toSend);
                lists[0].getItems().add(" ");
                message.setText("");
                replyTo.setText("Reply To : No Message");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return message;
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

}
