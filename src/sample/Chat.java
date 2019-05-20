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
            }
            String message = received.substring(0, received.lastIndexOf('.'));
            String contact = received.substring(received.lastIndexOf('.') + 1);
            for (sample.ClientHandler client : Server.getClients()) {
                if (client.getClientName().equals(contact))
                    client.getDataOutputStream().writeUTF(message + "." + clientName);
            }
        }
    }
}
