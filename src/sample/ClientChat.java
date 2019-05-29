package sample;

import javafx.scene.image.*;
import javafx.scene.paint.Color;

import java.io.*;


public class ClientChat {
    protected Client currentClient;
    private String contactName;

    public ClientChat(Client currentClient, String contactName) {
        this.currentClient = currentClient;
        this.contactName = contactName;
    }


    public String sendMessage(String replyTo, String message, boolean isGroup) throws IOException {
        String relation = " -> ";
        if (replyTo.equals("Reply To : No Message") || replyTo.equals("Reply To :  ")) {
            replyTo = "";
            relation = "";
        }
        String toSend;
        if (!isGroup)
            toSend = replyTo + relation + message + "." + contactName + "." + currentClient.getName();
        else
            toSend = replyTo + relation + message + " ( " + currentClient.getName() + " sent )" + "." + currentClient.getName();
        currentClient.getDataOutputStream().writeUTF(toSend);
        return toSend.substring(0, toSend.indexOf('.'));
    }


    public String receiveMessage(String message) {
        String contactSent = message.substring(message.lastIndexOf('.') + 1);
        String messageSent = message.substring(0, message.lastIndexOf('.'));
        if (contactSent.equals(contactName)) {
            return messageSent;
        } else
            return null;
    }

    public ImageView receiveEmoji(String message, boolean empty) {
        if (empty) {
            return new ImageView(getCopyOfEmoji("images/" + 5780
                    + ".jpg"));
        }
        String[] messages = message.split("\\.");
        String numberOfEmoji = messages[1];
        String contactSent = messages[2];
        Image emoji = getCopyOfEmoji("images/" + numberOfEmoji
                + ".jpg");
        ImageView imageView = new ImageView(emoji);
        if (contactSent.equals(contactName)) {
            return imageView;
        } else
            return null;
    }

    public static WritableImage getCopyOfEmoji(String address) {
        Image image = new Image(address);
        int height = (int) image.getHeight();
        int width = (int) image.getWidth();
        PixelReader pixelReader = image.getPixelReader();
        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                pixelWriter.setColor(x, y, color);
            }
        }
        return writableImage;
    }

    public ImageView sendEmoji(int index, String CODE, boolean empty, boolean isGroup) throws IOException {
        if (empty) {
            return new ImageView(getCopyOfEmoji("images/5780.jpg"));
        }
        String toSend;
        if (!isGroup)
            toSend = CODE + "emoji." + index + "." + contactName + "." + currentClient.getName();
        else
            toSend = CODE + "emoji." + index + "." + currentClient.getName();
        currentClient.getDataOutputStream().writeUTF(toSend);
        return new ImageView(getCopyOfEmoji("images/" + index + ".jpg"));
    }


}
