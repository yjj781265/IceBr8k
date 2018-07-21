package app.jayang.icebr8k.Modle;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.Comparator;

/**
 * Created by yjj781265 on 1/25/2018.
 */

public class UserLocationDialog implements Comparable<UserLocationDialog>,Parcelable {
    private String id,distance,score;
    private User mUser;
    private LatLng latLng;

    public UserLocationDialog() {
    }


    public UserLocationDialog(String id, String distance, User mUser, LatLng latLng, String score,Long timestamp) {
        this.id = id;
        this.distance = distance;
        this.mUser = mUser;
        this.latLng = latLng;
        this.score = score;

    }




    public User getUser() {
        return mUser;
    }

    public void setUser(User mUser) {
        this.mUser = mUser;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatlng(LatLng latLng) {
        this.latLng = latLng;
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

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
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
                ", score='" + score + '\'' +
                ", timestamp=" +
                ", mUser=" + mUser +
                ", latLng=" + latLng +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public UserLocationDialog(Parcel in){
        this.id = in.readString();
        this.distance = in.readString();
        this.score = in.readString();
        this.mUser = (User) in.readSerializable();



    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
       dest.writeString(this.id);
       dest.writeString(this.distance);
       dest.writeString(this.score);
       dest.writeSerializable(mUser);


    }


    public static final Parcelable.Creator CREATOR = new Parcelable.Creator(){
        @Override
        public UserLocationDialog createFromParcel(Parcel parcel) {
            return  new UserLocationDialog(parcel);
        }

        @Override
        public UserLocationDialog[] newArray(int i) {
            return new UserLocationDialog[i];
        }
    };
}
