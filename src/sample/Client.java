package sample;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Client extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception{
        Controller controller = Controller.getInstance();
        controller.showMainMenu(primaryStage);
    }


    public static void main(String[] args) {
        launch(args);
    }


}
