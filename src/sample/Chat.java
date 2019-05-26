package sample;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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
            } else {
                sendMessage(received);
            }
        }
    }

    private void sendMessage(String receivedMessage) throws IOException {
        String message = receivedMessage.substring(0, receivedMessage.lastIndexOf('.'));
        String contact = receivedMessage.substring(receivedMessage.lastIndexOf('.') + 1);
        for (sample.ClientHandler client : Server.getClients()) {
            if (client.getClientName().equals(contact)) {
                client.getDataOutputStream().writeUTF(message + "." + clientName);
                break;
            }
        }
    }

    private void sendEmoji(String messageReceived) throws IOException {
        //5780emoji.numberOfEmoji.contactName
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
