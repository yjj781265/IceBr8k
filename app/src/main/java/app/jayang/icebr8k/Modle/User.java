package app.jayang.icebr8k.Modle;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.InterpolatorRes;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by LoLJay on 10/20/2017.
 */

public class User implements Serializable,Comparable<User>{
    private String displayname,username,photourl,email,score,privacy,id;
    private Long lastseen;
    private String onlinestats;
    private Birthdate mBirthdate;

    public User() {
    }

    public User(String displayname, String username, String photourl, String email) {
        this.displayname = displayname;
        this.username = username;
        this.photourl = photourl;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getOnlinestats() {
        return onlinestats;
    }

    public Birthdate getBirthdate() {
        return mBirthdate;
    }

    public void setBirthdate(Birthdate birthdate) {
        mBirthdate = birthdate;
    }

    public void setOnlinestats(String onlinestats) {
        this.onlinestats = onlinestats;
    }

    public Long getLastseen() {
        return lastseen;
    }

    public void setLastseen(Long lastseen) {
        this.lastseen = lastseen;
    }

    @Override
    public int compareTo(@NonNull User user) {
        int result;
        Integer user1 =  Integer.valueOf(onlinestats);
        Integer user2 =  Integer.valueOf(user.getOnlinestats());
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
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {

        return Objects.hash(username);
    }
}
