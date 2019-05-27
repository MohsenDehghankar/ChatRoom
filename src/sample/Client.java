package sample;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;


public class Client extends Application {

    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
    private String name;
    private ClientChat currentChat;


    @Override
    public void start(Stage primaryStage) {
        Controller controller = Controller.getInstance(this, primaryStage);
        controller.showMainMenu();
    }


    public static void main(String[] args) {
        launch(args);
    }

    public boolean connectToServer(String name) {
        try {
            this.name = name;
            InetAddress ip = InetAddress.getByName("localhost");
            Socket s = new Socket(ip, 1234);
            dataInputStream = new DataInputStream(s.getInputStream());
            dataOutputStream = new DataOutputStream(s.getOutputStream());
            dataOutputStream.writeUTF(name);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public DataInputStream getDataInputStream() {
        return dataInputStream;
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    public String getName() {
        return name;
    }

    public void startChat(String CODE, String contact) {
        try {
            dataOutputStream.writeUTF(CODE + "chat");
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentChat = new ClientChat(this, contact);
    }

    public ClientChat getCurrentChat() {
        return currentChat;
    }

    public void sendExitToServer(String CODE) throws IOException {
        getDataOutputStream().writeUTF(CODE + "exit");
    }

    public ArrayList<String> requestGroupAndClientList(String CODE) throws IOException {
        getDataOutputStream().writeUTF(CODE + "list");
        if (getDataInputStream().available() > 0) {
            String st = getDataInputStream().readUTF();
            if (st.length() > 10 && st.substring(0, 8).equals("5780list")) {
                st = st.substring(8);
                String[] groups = st.split(" ");
                return convertArrayToArrayList(groups);
            }
        }
        return null;
    }

    private ArrayList<String> convertArrayToArrayList(String[] strings) {
        ArrayList<String> result = new ArrayList<>();
        for (String message : strings) {
            result.add(message);
        }
        return result;
    }


    public void createGroup(String groupName, String members) throws IOException {
        String toSend = "5780group." + name + "." + groupName + "." + members;
        dataOutputStream.writeUTF(toSend);
    }

    public boolean enterAGroup(String groupName) throws IOException {
        String toSend = "5780group." + name + "." + groupName;
        dataOutputStream.writeUTF(toSend);
        if (dataInputStream.available() > 0) {
            if (dataInputStream.readUTF().equals("5780error"))
                return false;
        }
        return true;
    }

    public void sendAddMemberRequest(String member) throws IOException {
        dataOutputStream.writeUTF("5780addmember." + name + "." + member);
    }

}
