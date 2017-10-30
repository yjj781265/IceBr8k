package app.jayang.icebr8k;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by LoLJay on 10/20/2017.
 */

public class User implements Serializable,Comparable<User> {
    private String displayname,username,photourl,email;

    public User() {
    }

    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhotourl() {
        return photourl;
    }

    public void setPhotourl(String photourl) {
        this.photourl = photourl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    @Override
    public int compareTo(@NonNull User user) {
        return displayname.toUpperCase().compareTo(user.getDisplayname().toUpperCase());

    }
}
