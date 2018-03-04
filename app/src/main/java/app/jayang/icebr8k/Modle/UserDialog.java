package app.jayang.icebr8k.Modle;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.util.SortedList;
import android.support.v7.widget.util.SortedListAdapterCallback;

import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter;


/**
 * Created by yjj781265 on 1/2/2018.
 */

public class UserDialog implements Parcelable{
    private String name, username,photoUrl,score,onlinestats,email,id,lastseen;


    public UserDialog() {
    }

    public UserDialog(String name, String username, String photoUrl, String score, String onlinestats, String email, String id, String lastseen) {
        this.name = name;
        this.username = username;
        this.photoUrl = photoUrl;
        this.score = score;
        this.onlinestats = onlinestats;
        this.email = email;
        this.id = id;
        this.lastseen = lastseen;
    }


    public String getOnlinestats() {
        return onlinestats;
    }

    public void setOnlinestats(String onlinestats) {
        this.onlinestats = onlinestats;
    }

    public String getLastseen() {
        return lastseen;
    }

    public void setLastseen(String lastseen) {
        this.lastseen = lastseen;
    }

    public static Creator getCREATOR() {
        return CREATOR;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserDialog dialog = (UserDialog) o;

        return id.equals(dialog.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "UserDialog{" +
                "name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", photoUrl='" + photoUrl + '\'' +
                ", score='" + score + '\'' +
                ", onlinestats='" + onlinestats + '\'' +
                ", email='" + email + '\'' +
                ", id='" + id + '\'' +
                ", lastseen='" + lastseen + '\'' +
                '}';
    }




    @Override
    public int describeContents() {
        return 0;
    }

    public UserDialog(Parcel in){
        this.id = in.readString();
        this.name = in.readString();
        this.username = in.readString();
        this.photoUrl = in.readString();
        this.score = in.readString();
        this.onlinestats = in.readString();
        this.email = in.readString();
        this.lastseen = in.readString();


    }


    @Override
    public void writeToParcel(Parcel parcel, int i) {
       parcel.writeString(this.id);
        parcel.writeString(this.name);
        parcel.writeString(this.username);
        parcel.writeString(this.photoUrl);
        parcel.writeString(this.score);
        parcel.writeString(this.onlinestats);
        parcel.writeString(this.email);
        parcel.writeString(this.lastseen);

    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public UserDialog createFromParcel(Parcel in) {
            return new UserDialog(in);
        }

        public UserDialog[] newArray(int size) {
            return new UserDialog[size];
        }
    };
}
