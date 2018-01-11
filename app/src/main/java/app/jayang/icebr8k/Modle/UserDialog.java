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

public class UserDialog implements Comparable<UserDialog>,SortedListAdapter.ViewModel,Parcelable{
    private String name, username,photoUrl,score,onlineStats,email,id;


    public UserDialog() {
    }

    public UserDialog(String id , String name, String username, String photoUrl, String score, String onlineStats
            , String email) {
        this.id =id;
        this.name = name;
        this.username = username;
        this.photoUrl = photoUrl;
        this.score = score;
        this.onlineStats = onlineStats;
        this.email =email;

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

    public String getOnlineStats() {
        return onlineStats;
    }

    public void setOnlineStats(String onlineStats) {
        this.onlineStats = onlineStats;
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
    public int compareTo(@NonNull UserDialog userDialog) {
        int result;

        Integer user1 =  Integer.valueOf(onlineStats);
        Integer user2 =  Integer.valueOf(userDialog.getOnlineStats());
        result = user2.compareTo(user1);
        if(result==0  ){
            if(score.equals("") ){
                score="0";
            }

            if(userDialog.getScore().equals("")){
                userDialog.setScore("0");
            }
            result = Integer.valueOf(userDialog.getScore()).compareTo(Integer.valueOf(score));
        }



        if(result==0) {
            result =name.compareTo(userDialog.getName());
        }
        return result;
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
                ", onlineStats='" + onlineStats + '\'' +
                ", email='" + email + '\'' +
                ", id='" + id + '\'' +
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
        this.onlineStats = in.readString();
        this.email = in.readString();


    }


    @Override
    public void writeToParcel(Parcel parcel, int i) {
       parcel.writeString(this.id);
        parcel.writeString(this.name);
        parcel.writeString(this.username);
        parcel.writeString(this.photoUrl);
        parcel.writeString(this.score);
        parcel.writeString(this.onlineStats);
        parcel.writeString(this.email);

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
