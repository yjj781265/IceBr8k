package app.jayang.icebr8k.Modle;

/**
 * Created by Jayang on 2/16/2018.
 */

public class UserMessageDialog {
    private String id,dialogName,photoUrl;
    private UserMessage lastMessage;
    private Integer unRead;
    private Boolean groupchat,muted;

    public UserMessageDialog() {
    }


    public UserMessageDialog(String id, String dialogName, String photoUrl, UserMessage lastMessage, Integer unRead, Boolean groupchat, Boolean muted) {
        this.id = id;
        this.dialogName = dialogName;
        this.photoUrl = photoUrl;
        this.lastMessage = lastMessage;
        this.unRead = unRead;
        this.groupchat = groupchat;
        this.muted = muted;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Integer getUnRead() {
        return unRead;
    }

    public void setUnRead(Integer unRead) {
        this.unRead = unRead;
    }

    public String getDialogName() {
        return dialogName;
    }

    public void setDialogName(String dialogName) {
        this.dialogName = dialogName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserMessage getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(UserMessage lastMessage) {
        this.lastMessage = lastMessage;
    }


    public Boolean getGroupchat() {
        return groupchat;
    }

    public void setGroupchat(Boolean groupchat) {
        this.groupchat = groupchat;
    }

    public Boolean getMuted() {
        return muted;
    }

    public void setMuted(Boolean muted) {
        this.muted = muted;
    }

    @Override
    public String toString() {
        return "UserMessageDialog{" +
                "id='" + id + '\'' +
                ", dialogName='" + dialogName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserMessageDialog that = (UserMessageDialog) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
