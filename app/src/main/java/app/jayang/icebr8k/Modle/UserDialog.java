package app.jayang.icebr8k.Modle;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.util.SortedList;
import android.support.v7.widget.util.SortedListAdapterCallback;

import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter;

import java.io.Serializable;
import java.util.Objects;


/**
 * Edited by yjj781265 on 7/29/2018.
 */

public class UserDialog implements Serializable{
    private User user;
    private String score = "" , id;

    public UserDialog(User user, String score, String id) {
        this.user = user;
        this.score = score;
        this.id = id;
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
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
        UserDialog that = (UserDialog) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
/*
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(score);
        dest.writeSerializable(user);
    }

    public static final Parcelable.Creator<UserDialog> CREATOR
            = new Parcelable.Creator<UserDialog>(){
        @Override
        public UserDialog createFromParcel(Parcel source) {
            return new UserDialog(source);
        }

        @Override
        public UserDialog[] newArray(int size) {
            return new UserDialog[size];
        }
    };

    public UserDialog(Parcel source) {
         score = source.readString();
         user = (User) source.readSerializable();
    }
*/

}