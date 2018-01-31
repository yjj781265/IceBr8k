package app.jayang.icebr8k.Modle;

import android.support.annotation.NonNull;

import java.util.Comparator;

/**
 * Created by yjj781265 on 1/25/2018.
 */

public class UserLocationDialog implements Comparable<UserLocationDialog>,Comparator<UserLocationDialog> {
    private String id,distance;
    private User mUser;

    public UserLocationDialog() {
    }

    public UserLocationDialog(String id, String distance, User user) {
        this.id = id;
        this.distance = distance;
        mUser = user;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public User getUser() {
        return mUser;
    }

    public void setUser(User user) {
        mUser = user;
    }

    @Override
    public int compare(UserLocationDialog userLocationDialog, UserLocationDialog t1) {
        return Double.valueOf(t1.getDistance()).compareTo(Double.valueOf(userLocationDialog.getDistance()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserLocationDialog dialog = (UserLocationDialog) o;

        return id.equals(dialog.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }



    @Override
    public int compareTo(@NonNull UserLocationDialog userLocationDialog) {
        return Double.valueOf(this.distance).compareTo(Double.valueOf(userLocationDialog.getDistance()));
    }

    @Override
    public String toString() {
        return "UserLocationDialog{" +
                "id='" + id + '\'' +
                ", distance='" + distance + '\'' +
                '}';
    }
}
