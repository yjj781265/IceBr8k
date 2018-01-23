package app.jayang.icebr8k;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;

import app.jayang.icebr8k.Modle.UserMarker;
import belka.us.androidtoggleswitch.widgets.BaseToggleSwitch;
import belka.us.androidtoggleswitch.widgets.ToggleSwitch;

public class PeopleNearby extends AppCompatActivity implements OnMapReadyCallback,GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,BaseToggleSwitch.OnToggleSwitchChangeListener,
        GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener{

    private final int MY_PERMISSION_REQUEST_CODE = 781265;
    private final int PLAY_SERVICES_RESOLUTION_REQUEST = 950406;
    private final int UPDATE_INTERVAL = 10000; //10 sec
    private final int FASTEST_INTERVAL = 3000;
    private final int DISPLACEMENT = 5;
    private FirebaseUser curretUser;
    private GoogleMap map;
    private  ToggleSwitch mToggleSwitch;
    private  FrameLayout mFrameLayout;
    private RecyclerView mRecyclerView;
    private View mCustomerMarkerView;
    private ImageButton mLocationButton;
    private  Marker marker;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private Boolean init = false;

    private SupportMapFragment mapFragment;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private HashMap<String,UserMarker> mHashMap;
    private final String TAG = "PeopleNearby_frag";


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people_nearby);
        mCustomerMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).
                inflate(R.layout.custom_marker, null);
        mFrameLayout =findViewById(R.id.people_mapFrame);
        mRecyclerView =findViewById(R.id.people_recyclerView);
        mLocationButton =findViewById(R.id.people_myLocation);

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.people_mapView);
        mapFragment.getMapAsync(this);
        mToggleSwitch = findViewById(R.id.people_toggle);
        mToggleSwitch.setOnToggleSwitchChangeListener(this);
        int position = mToggleSwitch.getCheckedTogglePosition();
        setUI(position);
        if(checkGooglePlayService() ){
             buildGoogleApiClient();
            mLocationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mCurrentLocation!=null && map!=null){
                        float zoom = map.getCameraPosition().zoom;
                        Log.d(TAG, "ZOOM LEVEL "+zoom);
                        LatLng latLng = new LatLng(mCurrentLocation.getLatitude(),
                                mCurrentLocation.getLongitude());
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,17f));
                    }
                }
            });
        }




    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mGoogleApiClient!=null) {
            mGoogleApiClient.reconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkGooglePlayService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mGoogleApiClient!=null) {
            mGoogleApiClient.disconnect();
        }
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void initGoogleMapLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                super.onLocationResult(result);
               // mCurrentLocation = result.getLastLocation();
                mCurrentLocation = result.getLocations().get(0);
                LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                addCustomMarkerFromURL(latLng);
              Log.d(TAG,"Current location:\n" + mCurrentLocation) ;

            }
        };

        startLocationMonitoring();
    }

    private void startLocationMonitoring(){
        Log.d(TAG,"startLocation called");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).
              setInterval(UPDATE_INTERVAL).setFastestInterval(FASTEST_INTERVAL).setSmallestDisplacement(DISPLACEMENT);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        SettingsClient mSettingsClient = LocationServices.getSettingsClient(this);
        LocationSettingsRequest mLocationSettingsRequest = builder.build();

        Task<LocationSettingsResponse> locationResponse = mSettingsClient.checkLocationSettings(mLocationSettingsRequest);
        locationResponse.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.e("Response", "Successful acquisition of location information!!");
                //
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setTrafficEnabled(true);
        map.setIndoorEnabled(true);
        map.setBuildingsEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setOnMyLocationButtonClickListener(this);
        map.setOnMyLocationClickListener(this);




    }


    @Override
    public boolean onMyLocationButtonClick() {
       // Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
       // Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onToggleSwitchChangeListener(int position, boolean isChecked) {
     if(isChecked){
       setUI(position);
     }
    }

    private void setUI(int position){
        switch (position){
            case 1:
                mFrameLayout.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
                break;
            case 0:
                mFrameLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                break;
            default:
                mFrameLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                break;
        }
    }



    private Bitmap getMarkerBitmapFromView(View view, Bitmap bitmap) {

        ImageView mMarkerImageView = mCustomerMarkerView.findViewById(R.id.marker_image);

        mMarkerImageView.setImageBitmap(bitmap);
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = view.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        view.draw(canvas);
        return returnedBitmap;

    }
    private void addCustomMarkerFromURL(final LatLng mDummyLatLng) {

        if (map == null) {
            return;
        }
        // adding a marker with image from URL using glide image loading library
        String photourl =String.valueOf(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl());
        Glide.with(this).asBitmap()
                .load(photourl).apply(RequestOptions.circleCropTransform())
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        if( marker!=null){
                            marker.remove();
                        }
                        marker =map.addMarker(new MarkerOptions().position(mDummyLatLng)
                                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomerMarkerView, resource))));
                        if(!init){
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(mDummyLatLng, 17f));
                            init =true;
                        }else{


                        }



                    }
                });
     ;

    }


    private boolean checkGooglePlayService(){
        int response =GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if(response!= ConnectionResult.SUCCESS){
            GoogleApiAvailability.getInstance().getErrorDialog(this,response,1).show();
            return false;
        }else{
            return  true;
        }
    }



//google api client call backs
    @Override
    public void onConnected(@Nullable Bundle bundle) {
      if(mGoogleApiClient!=null){
         initGoogleMapLocation();
      }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


}

