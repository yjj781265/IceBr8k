package app.jayang.icebr8k.Modle;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yjj781265 on 11/9/2017.
 */

public class ChatDialog implements IDialog,Comparable<ChatDialog> {
    String id, dialogphoto,dialogname;
    ArrayList<Author> users;
    IMessage lastmessage;
    int unreadcount;

    public ChatDialog(){}


    public ChatDialog(String id,String dialogphoto, String dialogname,
                      ArrayList<Author> users, Message lastmessage, int unreadcount) {
        this.id = id;
        this.dialogphoto = dialogphoto;
        this.dialogname = dialogname;
        this.users = users;
        this.lastmessage = lastmessage;
        this.unreadcount = unreadcount;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDialogPhoto() {
        return dialogphoto;
    }

    @Override
    public String getDialogName() {
        return dialogname;
    }

    @Override
    public List<? extends IUser> getUsers() {
        return users;
    }

    @Override
    public IMessage getLastMessage() {
        return lastmessage;
    }

    @Override
    public void setLastMessage(IMessage message) {
        this.lastmessage = message;
    }

    @Override
    public int getUnreadCount() {
        return unreadcount;
    }


    public void setUnreadcount(int unreadcount) {
        this.unreadcount = unreadcount;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDialogphoto(String dialogphoto) {
        this.dialogphoto = dialogphoto;
    }

    public void setDialogname(String dialogname) {
        this.dialogname = dialogname;
    }

    public void setUsers(ArrayList<Author> users) {
        this.users = users;
    }

    public void setLastmessage(Message lastmessage) {
        this.lastmessage = lastmessage;
    }

    @Override
    public int compareTo(@NonNull ChatDialog chatDialog) {

        int result =Integer.valueOf(chatDialog.getUnreadCount()).compareTo(unreadcount);
        if(result==0){
           result= chatDialog.getLastMessage().getCreatedAt().compareTo(lastmessage.getCreatedAt());
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChatDialog that = (ChatDialog) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
