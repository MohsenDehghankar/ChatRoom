package sample;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


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
        TextField textField = new TextField("Enter User Name ...");
        textField.relocate(200, 200);
        textField.setOnAction(actionEvent -> {
            if (textField.getText().split(" ").length > 1) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Invalid User Name ( no Space )");
                alert.show();
            } else {
                Client client = new Client(textField.getText());
                showChatMenu(stage, client);
            }
        });
        root.getChildren().add(textField);
        stage.setScene(scene);
        stage.setTitle("Main Menu");
        stage.show();
    }

    private void showChatMenu(Stage stage, Client loggedInClient) {
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
    }
}
