package app.jayang.icebr8k.Modle;

import java.util.ArrayList;

public class Comment {
    private String id, senderId, text,type;
    private Long like, dislike;
    private Long timestamp;

    public Comment(String id, String senderId, String text, String type, Long timestamp) {
        this.id = id;
        this.senderId = senderId;
        this.text = text;
        this.type = type;
        this.timestamp = timestamp;
    }

    public Comment() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getLike() {
        return like;
    }

    public void setLike(Long like) {
        this.like = like;
    }

    public Long getDislike() {
        return dislike;
    }

    public void setDislike(Long dislike) {
        this.dislike = dislike;
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
}

