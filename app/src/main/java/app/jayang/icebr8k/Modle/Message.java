package app.jayang.icebr8k.Modle;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.Date;

/**
 * Created by LoLJay on 11/5/2017.
 */

public class Message implements IMessage {
    private String id,text;
    private Date createdAt;
    private Author author;
    private String month,timezoneOffset,time,minutes,seconds,hours,day,date,year;

    public Message() {
    }

    public Message(String id, String text, Date createdAt, Author author) {
        this.id = id;
        this.text = text;
        this.createdAt = createdAt;
        this.author= author ;
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




    @Override
    public Date getCreatedAt() {
        return createdAt;
    }


    public void setId(String id) {
        this.id = id;
    }
    public void setUser(Author user){this.author =user;}


    public void setText(String text) {
        this.text = text;
    }

    public void setCreatedAt(Date date) {
        this.createdAt = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }
}
