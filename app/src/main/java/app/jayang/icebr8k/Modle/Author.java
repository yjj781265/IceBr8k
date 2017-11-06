package app.jayang.icebr8k.Modle;

import com.stfalcon.chatkit.commons.models.IUser;

/**
 * Created by LoLJay on 11/5/2017.
 */

public class Author implements IUser {
    private String id,name,avatar;

    public Author() {
    }

    public Author(String id, String name, String avatar) {
        this.id = id;
        this.name = name;
        this.avatar= avatar;
    }

    @Override
    public String getId() {
       return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAvatar(String avatarUrl) {
        this.avatar = avatar;
    }
}
