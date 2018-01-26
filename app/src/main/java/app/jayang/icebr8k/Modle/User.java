package app.jayang.icebr8k.Modle;

import android.support.annotation.InterpolatorRes;
import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by LoLJay on 10/20/2017.
 */

public class User implements Serializable,Comparable<User> {
    private String displayname,username,photourl,email,score,privacy;
    private String onlineStats ="0";

    public User() {
    }

    public User(String displayname, String username, String photourl, String email) {
        this.displayname = displayname;
        this.username = username;
        this.photourl = photourl;
        this.email = email;
    }

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
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
        int result;
        Integer user1 =  Integer.valueOf(onlineStats);
        Integer user2 =  Integer.valueOf(user.getOnlineStats());
        result = user2.compareTo(user1);
        if(result==0 ){
            result = Integer.valueOf(score).compareTo(Integer.valueOf(user.getScore()));
        }else {
            result =displayname.compareTo(user.getDisplayname());
        }
          return result;





    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (displayname != null ? !displayname.equals(user.displayname) : user.displayname != null)
            return false;
        return photourl != null ? photourl.equals(user.photourl) : user.photourl == null;
    }

    @Override
    public int hashCode() {
        int result = displayname != null ? displayname.hashCode() : 0;
        result = 31 * result + (photourl != null ? photourl.hashCode() : 0);
        return result;
    }
}
