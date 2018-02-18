package app.jayang.icebr8k.Modle;

/**
 * Created by Jayang on 2/16/2018.
 */

public class UserMessage {
    private String text, senderId, timestamp, messageType, messageId;



    public UserMessage() {
    }

    public UserMessage(String text, String senderId, String timestamp, String messageType, String messageId) {
        this.text = text;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.messageType = messageType;
        this.messageId = messageId;

    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserMessage that = (UserMessage) o;

        return messageId.equals(that.messageId);
    }

    @Override
    public int hashCode() {
        return messageId.hashCode();
    }

    @Override
    public String toString() {
        return "UserMessage{" +
                "text='" + text + '\'' +
                ", senderId='" + senderId + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", messageType='" + messageType + '\'' +
                ", messageId='" + messageId + '\'' +
                '}';
    }
}


