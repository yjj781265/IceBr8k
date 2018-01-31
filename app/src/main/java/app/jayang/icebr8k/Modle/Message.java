package app.jayang.icebr8k.Modle;

import android.support.annotation.NonNull;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.Date;

/**
 * Created by LoLJay on 11/5/2017.
 */

public class Message implements IMessage,Comparable<Message> {
    private String id,text;
    private Date createdAt;
    private Author author;
    private String status;
    private String timestamp;


    public Message() {
    }

    public Message(String id, String text, Date createdAt, Author author) {
        this.id = id;
        this.text = text;
        this.createdAt = createdAt;
        this.author= author ;
    }

    public Message(String id, String text, Date createdAt, Author author, String status, String timestamp) {
        this.id = id;
        this.text = text;
        this.createdAt = createdAt;
        this.author = author;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return  String.valueOf( createdAt.getTime());
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public IUser getUser() {
       return author;
    }

    public Author getAuthor(){return  author;}

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAuthor(Author user){this.author =user;}

    public void setText(String text) {
        this.text = text;
    }

    public void setCreatedAt(Date date) {
        this.createdAt = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if (id != null ? !id.equals(message.id) : message.id != null) return false;
        if (text != null ? !text.equals(message.text) : message.text != null) return false;
        return createdAt != null ? createdAt.equals(message.createdAt) : message.createdAt == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(@NonNull Message message) {
        return message.getCreatedAt().compareTo(this.createdAt);
    }
}
