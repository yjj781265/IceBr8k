package app.jayang.icebr8k.Modle;

/**
 * Created by Jayang on 2/16/2018.
 */

public class UserMessage {
    private String text, senderid, messagetype, messageid;
    private Long timestamp;
    private Boolean doneNetWorking =false;  //default value



    public UserMessage() {
    }

    public UserMessage(String text, String senderid, String messagetype, String messageid, Long timestamp) {
        this.text = text;
        this.senderid = senderid;
        this.messagetype = messagetype;
        this.messageid = messageid;
        this.timestamp = timestamp;
    }

    public UserMessage(String text, String senderid, String messagetype, String messageid, Long timestamp, Boolean doneNetWorking) {
        this.text = text;
        this.senderid = senderid;
        this.messagetype = messagetype;
        this.messageid = messageid;
        this.timestamp = timestamp;
        this.doneNetWorking = doneNetWorking;
    }

    public Boolean getDoneNetWorking() {
        return doneNetWorking;
    }

    public void setDoneNetWorking(Boolean doneNetWorking) {
        this.doneNetWorking = doneNetWorking;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSenderid() {
        return senderid;
    }

    public void setSenderid(String senderid) {
        this.senderid = senderid;
    }

    public String getMessagetype() {
        return messagetype;
    }

    public void setMessagetype(String messagetype) {
        this.messagetype = messagetype;
    }

    public String getMessageid() {
        return messageid;
    }

    public void setMessageid(String messageid) {
        this.messageid = messageid;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserMessage that = (UserMessage) o;

        return messageid.equals(that.messageid);
    }

    @Override
    public int hashCode() {
        return messageid.hashCode();
    }

    @Override
    public String toString() {
        return "UserMessage{" +
                "text='" + text + '\'' +
                ", senderId='" + senderid + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", messageType='" + messagetype + '\'' +
                ", messageId='" + messageid + '\'' +
                '}';
    }
}


