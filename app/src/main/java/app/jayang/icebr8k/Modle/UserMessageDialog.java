package app.jayang.icebr8k.Modle;

/**
 * Created by Jayang on 2/16/2018.
 */

public class UserMessageDialog {
    private String id,dialogName;
    private UserMessage lastMessage;
    private Boolean groupChat;

    public UserMessageDialog() {
    }


    public UserMessageDialog(String id, String dialogName, UserMessage lastMessage, Boolean groupChat) {
        this.id = id;
        this.dialogName = dialogName;
        this.lastMessage = lastMessage;
        this.groupChat = groupChat;
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

    public Boolean getGroupChat() {
        return groupChat;
    }

    public void setGroupChat(Boolean groupChat) {
        this.groupChat = groupChat;
    }
}
