package sample;

import java.io.*;
import java.util.ArrayList;

public class Chat {
    private static final String CODE = "5780";
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private String clientName;

    public Chat(DataInputStream dataInputStream, DataOutputStream dataOutputStream, String clientName) {
        this.dataInputStream = dataInputStream;
        this.dataOutputStream = dataOutputStream;
        this.clientName = clientName;
    }


    public void startChat() throws IOException {
        String received;
        while (true) {
            received = dataInputStream.readUTF();
            if (received.equals(CODE + "exit")) {
                break;
            } else if (received.length() >= 9 && received.substring(0, 9).matches(CODE + "emoji")) {
                sendEmoji(received);
            } else if (received.length() >= 9 && received.substring(0, 9).equals("5780image")) {
                String contact = received.substring(9, received.lastIndexOf('.'));
                long length = Integer.parseInt(received.substring(received.lastIndexOf('.') + 1));
                FileOutputStream f = new FileOutputStream("src/images/serverTemp/1.jpg");
                int count;
                byte[] buffer = new byte[70000]; // or 4096, or more
                while ((count = dataInputStream.read(buffer)) > 0) {
                    f.write(buffer, 0, count);
                    if (count == length)
                        break;
                }
                f.close();
                for (ClientHandler client : Server.getClients()) {
                    if (client.getClientName().equals(contact)) {
                        FileInputStream fileInputStream = new FileInputStream("src/images/serverTemp/1.jpg");
                        client.getDataOutputStream().writeUTF("5780image" + client.getClientName() + "." + length);
                        //TODO
                        System.out.println("salam");
                        //TODO
                        System.out.println("ok sent");
                        byte[] bytes = new byte[70000];
                        int count2;
                        while ((count2 = fileInputStream.read(bytes)) > 0) {
                            client.getDataOutputStream().write(bytes, 0, count);
                        }
                        fileInputStream.close();
                        break;
                    }
                }
            } else if (received.contains(".")) {
                sendMessage(received);
            }
        }
    }

    public void sendMessage(String receivedMessage) throws IOException {
        receivedMessage = receivedMessage.substring(0, receivedMessage.lastIndexOf('.'));
        String message = receivedMessage.substring(0, receivedMessage.lastIndexOf('.'));
        String contact = receivedMessage.substring(receivedMessage.lastIndexOf('.') + 1);
        for (sample.ClientHandler client : Server.getClients()) {
            if (client.getClientName().equals(contact)) {
                client.getDataOutputStream().writeUTF(message + "." + clientName);
                break;
            }
        }
    }

    public void sendEmoji(String messageReceived) throws IOException {
        //5780emoji.numberOfEmoji.contactName.sender
        messageReceived = messageReceived.substring(0, messageReceived.lastIndexOf('.'));
        String[] messageSplit = messageReceived.split("\\.");
        String contact = messageSplit[2];
        String toSend = messageReceived.substring(0, messageReceived.lastIndexOf('.') + 1);
        toSend += clientName;//5780emoji.numberOfEmoji.clientName
        for (ClientHandler client : Server.getClients()) {
            if (client.getClientName().equals(contact)) {
                client.getDataOutputStream().writeUTF(toSend);
                break;
            }
        }
    }
}
