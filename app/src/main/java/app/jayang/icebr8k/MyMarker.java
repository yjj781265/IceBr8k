package app.jayang.icebr8k;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by yjj781265 on 1/31/2018.
 */

public class MyMarker implements ClusterItem {
    private  LatLng mPosition;
    private  String mTitle;
    private  String mSnippet;
    public MyMarker(double lat, double lng) {

        mPosition = new LatLng(lat, lng);
    }

    public MyMarker(double lat, double lng, String title, String snippet) {
        mPosition = new LatLng(lat, lng);
        mTitle = title;
        mSnippet = snippet;
    }
    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSnippet() {
        return mSnippet;
    }
}
