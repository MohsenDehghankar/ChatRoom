package sample;

import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Group {
    private static final String CODE = "5780";
    private ArrayList<String> members;
    private String name;
    private ArrayList<String> messages = new ArrayList<>();
    private DataOutputStream dos;
    private DataInputStream dis;
    private static final Object o = new Object();

    public Group(String client1, String groupName, DataOutputStream dos, DataInputStream dis) {
        /*group = new Client();
        group.connectToServer(groupName);*/
        name = groupName;
        String[] clients = client1.split(" ");
        members = new ArrayList<>();
        for (String client : clients) {
            members.add(client);
        }
        this.dos = dos;
        this.dis = dis;
    }


    public void startGroup() throws IOException, InterruptedException {
        String received;
        while (true) {
            synchronized (o) {
                received = dis.readUTF();
                if (received.equals(CODE + "exit")) {
                    break;
                } else if (received.equals(CODE + "messages")) {
                    String messages = CODE;
                    for (Group group : Server.getGroups()) {
                        if (group.name.equals(name)) {
                            for (String message : group.getMessages()) {
                                messages += message + " ";
                            }
                        }
                    }
                    if (messages.length() > 1)
                        messages = messages.substring(0, messages.length() - 1);
                    //TODO
                    System.out.println("sent back : " + messages);
                    dos.writeUTF(CODE + messages);
                } else if (received.length() >= 9 && received.substring(0, 9).matches(CODE + "emoji")) {
                    String[] s = received.split("\\.");//5780emoji.number.contactName.sender
                    String toAdd = s[0] + "." + s[1] + "." + s[3];
                    messages.add(toAdd);
                } else if (received.contains(".")
                        && received.split("\\.").length == 3
                        && received.split("\\.")[1].equals(name)) {
                    //TODO
                    System.out.println(received + " message sent");
                    String[] s = received.split("\\.");//message.contact.sender
                    String toAdd = s[0] + "." + s[2];
                    messages.add(toAdd);
                }
                o.wait(200);
            }
        }
    }


    public ArrayList<String> getMessages() {
        return messages;
    }

    public String getName() {
        return name;
    }
}
