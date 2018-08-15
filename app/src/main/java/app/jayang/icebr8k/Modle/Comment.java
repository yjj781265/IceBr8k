package app.jayang.icebr8k.Modle;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Objects;

public class Comment implements Serializable{
    private String id, senderId, text,type,answer;
    private Long timestamp,reply = null;



    public Comment() {
    }

    public Comment(String id, String senderId, String text, String type, String answer, Long timestamp, Long reply) {
        this.id = id;
        this.senderId = senderId;
        this.text = text;
        this.type = type;
        this.answer = answer;
        this.timestamp = timestamp;
        this.reply = reply;
    }

    public Long getReply() {
        return reply;
    }

    public void setReply(Long reply) {
        this.reply = reply;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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
        Comment comment = (Comment) o;
        return Objects.equals(id, comment.id) &&
                Objects.equals(senderId, comment.senderId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, senderId);
    }




}

