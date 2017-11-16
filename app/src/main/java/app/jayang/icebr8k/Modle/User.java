package app.jayang.icebr8k.Modle;

import android.support.annotation.InterpolatorRes;
import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by LoLJay on 10/20/2017.
 */

public class User implements Serializable,Comparable<User> {
    private String displayname,username,photourl,email,score;
    private String onlineStats ="0";

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

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getOnlineStats() {
        return onlineStats;
    }

    public void setOnlineStats(String onlineStats) {
        if(onlineStats==null) {
            onlineStats = "0";
        }
        this.onlineStats = onlineStats;
    }

    @Override
    public int compareTo(@NonNull User user) {
        Integer user1 =  Integer.valueOf(onlineStats);
         int result;

        Integer user2 =  Integer.valueOf(user.getOnlineStats());


        result = user2.compareTo(user1);
        if(result==0 && displayname!=null){
            result =displayname.toUpperCase().compareTo(user.getDisplayname().toUpperCase());
        }




        return result;

    }
}
