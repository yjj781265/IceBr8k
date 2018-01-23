package app.jayang.icebr8k.Modle;

import com.google.firebase.auth.FirebaseUser;

/**
 * Created by yjj781265 on 1/22/2018.
 */

public class UserMarker {
    private double latitude,longtitude;
    private String privacy,photoUrl;




    public UserMarker() {
    }

    public UserMarker(double latitude, double longtitude, String privacy, String photoUrl) {
        this.latitude = latitude;
        this.longtitude = longtitude;
        this.privacy = privacy;
        this.photoUrl =photoUrl;

    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(double longtitude) {
        this.longtitude = longtitude;
    }

    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
