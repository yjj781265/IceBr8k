package app.jayang.icebr8k;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.ClusterManager;

import java.io.IOException;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import app.jayang.icebr8k.Modle.User;

import app.jayang.icebr8k.Modle.UserDialog;
import app.jayang.icebr8k.Modle.UserLocationDialog;

import app.jayang.icebr8k.Modle.UserQA;
import belka.us.androidtoggleswitch.widgets.BaseToggleSwitch;
import belka.us.androidtoggleswitch.widgets.ToggleSwitch;

public class PeopleNearby extends AppCompatActivity implements OnMapReadyCallback,GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,BaseToggleSwitch.OnToggleSwitchChangeListener,GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,GeoQueryDataEventListener {

    private final int UPDATE_INTERVAL = 5000; //5sec
    private final int FASTEST_INTERVAL = 3000;
    private final int DISPLACEMENT = 3;
    private final int   REQUEST_CHECK_SETTINGS =9000;
    private FirebaseUser curretUser;
    private GoogleMap map;
    private  ToggleSwitch mToggleSwitch;
    private  FrameLayout mFrameLayout;
    private RecyclerView mRecyclerView;
    private View mCustomerMarkerView;
    private ImageView mMarkerImageView;
    private ImageButton mLocationButton;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private Boolean center = true;
    private Double radius =1.6;
    private float ZoomLevel =17f;
    private MaterialDialog mProgressDialog;
    private GeoFire geofire;
    private Toolbar mToolbar;
    private TextView noUser;
    private int index =0;

    private SupportMapFragment mapFragment;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private HashMap<String,Marker> mHashMap;
    private UserLocationDialogAdapter mLocationDialogAdapter;
    private ArrayList<UserLocationDialog> mLocationDialogs;
    private ImageButton mfilter;
    private GeoQuery geoQuery;
    // Declare a variable for the cluster manager.
    private ClusterManager<MyMarker> mClusterManager;

    private final String TAG = "PeopleNearby_IceBr8k";



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people_nearby);
        mCustomerMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).
                inflate(R.layout.custom_marker, null);
        mMarkerImageView = mCustomerMarkerView.findViewById(R.id.marker_image);
        mFrameLayout =findViewById(R.id.people_mapFrame);
        mRecyclerView =findViewById(R.id.people_recyclerView);
        mfilter =findViewById(R.id.people_filter);
        mToolbar =findViewById(R.id.people_toolbar);
        noUser =findViewById(R.id.people_noUsers);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mLocationButton =findViewById(R.id.people_myLocation);
        curretUser =FirebaseAuth.getInstance().getCurrentUser();
        mHashMap =new HashMap<>();
        mLocationDialogs =new ArrayList<>();
        mLocationDialogAdapter = new UserLocationDialogAdapter(mLocationDialogs);
        mLocationDialogAdapter.setHasStableIds(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mLocationDialogAdapter);
        mRecyclerView.setHasFixedSize(true);



        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.people_mapView);
        mapFragment.getMapAsync(this);
        mToggleSwitch = findViewById(R.id.people_toggle);
        mToggleSwitch.setOnToggleSwitchChangeListener(this);
        //map or list
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
                        Log.d(TAG, "center true");
                        center =true;
                        LatLng latLng = new LatLng(mCurrentLocation.getLatitude(),
                                mCurrentLocation.getLongitude());
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,18f));
                    }
                }
            });
        }
        if(getIntent().getExtras()!=null) {
        String radiusString=getIntent().getExtras().getString("radius");
          radius = convertMileStringtoKm(radiusString);
          if(radius<2){
              ZoomLevel =13f;
              index =0;
          }else if(radius>15 && radius<32){
              index =1;
              ZoomLevel =11f;
          }else{
              index=2;
              ZoomLevel =9f;
          }
          mProgressDialog =ProgressDialog(radiusString);
            mProgressDialog.show();

        }
        mfilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSingleChoiceDialog();
            }
        });




    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mGoogleApiClient!=null && !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.reconnect();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mGoogleApiClient!=null) {
            mGoogleApiClient.disconnect();
        }
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
        if(geoQuery!=null){
           geoQuery.removeGeoQueryEventListener(this);

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
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                mCurrentLocation =location;
                if(mCurrentLocation!=null) {
                    findPeopleNearby(location.getLatitude(), location.getLongitude(), radius);
                }

            }
        });

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                super.onLocationResult(result);
               // mCurrentLocation = result.getLastLocation();
                mCurrentLocation = result.getLocations().get(0);
                if(mCurrentLocation!=null) {
                    updateLocationtoDatabase(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), radius);
                }



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
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mProgressDialog.dismiss();
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(PeopleNearby.this,
                           REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CHECK_SETTINGS && resultCode == RESULT_OK){
            Toast.makeText(this, "It's okay", Toast.LENGTH_SHORT).show();
            if(checkGooglePlayService()){
                buildGoogleApiClient();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setTrafficEnabled(true);
        map.setIndoorEnabled(true);
        map.setBuildingsEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setOnMarkerClickListener(this);
       map.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
       @Override
       public void onCameraMove() {
           ZoomLevel= map.getCameraPosition().zoom;
           //Log.d(TAG, "ZOOM LEVEL "+ZoomLevel);
       }
   });
       map.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
           @Override
           public void onCameraMoveStarted(int i) {
               Log.d(TAG, "center off " + i);
               if(i==REASON_GESTURE) {
                   center = false;
               }
           }
       });

        mClusterManager = new ClusterManager<>(this,map);
        mClusterManager.setAnimation(true);
       map.setOnCameraIdleListener(mClusterManager);
       map.setOnMarkerClickListener(mClusterManager);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }






    }


    @Override
    public boolean onMyLocationButtonClick() {
       // Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
       // Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
        updateUserLocation(location.getLatitude(),location.getLatitude(),radius);

    }

    @Override
    public void onToggleSwitchChangeListener(int position, boolean isChecked) {
     if(isChecked){
         switch (position){
             case 1:
                 mFrameLayout.setVisibility(View.VISIBLE);
                 mRecyclerView.setVisibility(View.GONE);

                 break;
             case 0:
                 mFrameLayout.setVisibility(View.GONE);
                 mRecyclerView.setVisibility(View.VISIBLE);
                 if(mLocationDialogs.isEmpty()){
                     noUser.setVisibility(View.VISIBLE);
                 }else{
                     noUser.setVisibility(View.GONE);
                 }

                 break;
             default:
                 mFrameLayout.setVisibility(View.GONE);
                 mRecyclerView.setVisibility(View.VISIBLE);
                 break;
         }
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
    private void addCustomMarkerFromURL(final LatLng mDummyLatLng,final String userUid) {

        if (map == null) {
            return;
        }
        Log.d(TAG,"reach adding photo progress") ;

        DatabaseReference urlRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(userUid);
        urlRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                 String photoUrl =null;
                  User user =null;
                if(dataSnapshot.exists()){
                   user = dataSnapshot.getValue(User.class);
                   photoUrl = user.getPhotourl();
                }
                if("public".equals(user.getPrivacy())&&photoUrl!=null ){
                    if(mHashMap.containsKey(userUid)){
                        Log.d(TAG,userUid+ " old marker before "+  mHashMap.get(userUid).getPosition()) ;
                        mHashMap.get(userUid).hideInfoWindow();
                        mHashMap.get(userUid).setSnippet(user.getDisplayname());
                        addmarkerWithTitle(mDummyLatLng,mHashMap.get(userUid));
                        mHashMap.get(userUid).setPosition(mDummyLatLng);


                        Log.d(TAG,userUid+ " old marker updated "+  mHashMap.get(userUid).getPosition()) ;

                    }else{
                        final User finalUser = user;
                        Glide.with(getApplicationContext()).asBitmap().load(photoUrl).
                                apply(RequestOptions.circleCropTransform()).into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {

                                Marker marker;
                                marker = map.addMarker(new MarkerOptions().position(mDummyLatLng)
                                            .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomerMarkerView, resource))));
                                marker.setSnippet(finalUser.getDisplayname());
                                    addmarkerWithTitle(mDummyLatLng,marker);
                                    mHashMap.put(userUid, marker);
                                UserLocationDialog temp = new UserLocationDialog();
                                temp.setId(userUid);
                                if(!mLocationDialogs.contains(temp)) {
                                    updateList(userUid, mDummyLatLng, finalUser);
                                    Log.d(TAG, userUid + " marker and list item added");
                                }

                            }


                        });
                    }
                    if(userUid.equals(curretUser.getUid()) && center){
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(mDummyLatLng, ZoomLevel));

                    }


                }else if("private".equals(user.getPrivacy())&&photoUrl!=null){
                    if(mHashMap.containsKey(userUid)) {
                        Marker oldMarker = mHashMap.get(userUid);
                        if (oldMarker != null) {
                            oldMarker.remove();
                            mHashMap.remove(userUid);


                         UserLocationDialog temp =new UserLocationDialog();
                         temp.setId(userUid);
                         if(mLocationDialogs.contains(temp)) {
                             mLocationDialogs.remove(temp);
                             mLocationDialogAdapter.notifyDataSetChanged();
                             Log.d(TAG, "old marker and list item removed privacy changed" );
                         }

                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // adding a marker with image from URL using glide image loading library


      // map.addMarker(new MarkerOptions().position())





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

    private void updateUserLocation(double lat, double lng,double radius)  {
        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(lat, lng, 3);

        } catch (IOException e) {
            e.printStackTrace();
        }
        if(addresses!=null){
            Address address = addresses.get(0);
            if(address.getCountryName()!=null && address.getAdminArea() !=null &&address.getLocality()!=null){
               // Toast.makeText(this,address.getCountryName()+" "+address.getAdminArea() + " "+address.getLocality(),Toast.LENGTH_LONG).show();
               // updateLocationtoDatabase(address.getCountryName(),address.getAdminArea(),address.getLocality(),lat,lng,radius);
            }

        }



    }

    private void updateLocationtoDatabase(final double lat,final double lng,double radius){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("GeoFireLocations");

         geofire = new GeoFire(ref);
        geofire.setLocation( curretUser.getUid(), new GeoLocation(lat, lng), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                Log.d(TAG,"location is updated to firebase");

            }
        });
        ref.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("timestamp").setValue(new Date().getTime());



    }
    private void updateTimeStamp(DatabaseReference ref){
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        ref.child("timestamp").setValue(timestamp);

    }
    private void findPeopleNearby(final double lat, final double lng, double radius){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("GeoFireLocations");

        geofire = new GeoFire(ref);
        geoQuery = geofire.queryAtLocation(new GeoLocation(lat,lng), radius);
        geoQuery.addGeoQueryDataEventListener(this);
    }

    private Double convertMileStringtoKm(String string){
        string = string.replaceAll("\\D+","");
        double radius = Double.valueOf(string)*1.60934;
        return radius;
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

   private void updateList(String userUid,LatLng latLng,User user){
           if(!userUid.equals(curretUser.getUid())) {
               Log.d(TAG, "reach adding item progress");
               Location destLocaiton = new Location("");
               destLocaiton.setLatitude(latLng.latitude);
               destLocaiton.setLongitude(latLng.longitude);
               float meter = mCurrentLocation.distanceTo(destLocaiton);
               double miles = (double) meter * 0.000621371192;



               String distance = String.valueOf(miles);
               UserLocationDialog dialog = new UserLocationDialog(userUid, distance, user);
               if (!mLocationDialogs.contains(dialog)) {
                   Log.d(TAG, "item  need update");
                   compareWithUser2(dialog);
               }else{
                   int index = mLocationDialogs.indexOf(dialog);
                   if(index!=-1) {
                       mLocationDialogs.remove(dialog);
                       compareWithUser2(dialog);
                   }

               }

           }

   }

    public void compareWithUser2(UserLocationDialog dialog) {
        pullUser1QA(dialog);

    }

    public void pullUser1QA(final UserLocationDialog dialog) {


        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("" +
                "UserQA/" + curretUser.getUid());
        mRef.keepSynced(true);
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            ArrayList<UserQA> User1QA = new ArrayList<>();

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    UserQA userQA = childSnapshot.getValue(UserQA.class);
                    User1QA.add(userQA);

                }

                if (dataSnapshot.getChildrenCount() == User1QA.size()) {
                    pullUser2QA( User1QA, dialog);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void pullUser2QA(final ArrayList<UserQA> user1QA, final UserLocationDialog dialog) {

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("UserQA/" + dialog.getId());
        mRef.keepSynced(true);
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            ArrayList<UserQA> User2QA = new ArrayList<>();

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    UserQA userQA = childSnapshot.getValue(UserQA.class);
                    User2QA.add(userQA);
                }
                SetScore(user1QA,User2QA, dialog);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void SetScore(ArrayList<UserQA> user1Arr, ArrayList<UserQA> user2Arr,
                         UserLocationDialog dialog) {
        int size = user1Arr.size();
        int commonQuestionSize = 0;
        String score;

        ArrayList<String> user1StrArr = new ArrayList<>();
        ArrayList<String> user2StrArr = new ArrayList<>();

        for (UserQA userQA : user1Arr) {
            if (!userQA.getAnswer().equals("skipped")) {
                user1StrArr.add(userQA.getQuestionId());
            }

        }
        for (UserQA userQA : user2Arr) {
            if (!userQA.getAnswer().equals("skipped")) {
                user2StrArr.add(userQA.getQuestionId());
            }
        }

        user1StrArr.retainAll(user2StrArr);

        commonQuestionSize = user1StrArr.size();

        Log.d("Score", "Common Question " + commonQuestionSize);
        user1Arr.retainAll(user2Arr);
        Log.d("Score", String.valueOf(user1Arr.size()));
        Log.d("Score", "Size " + size);
        if (commonQuestionSize != 0) {
            score = String.valueOf((int) (((double) user1Arr.size() / (double) commonQuestionSize) * 100));
            Log.d("Score", "Score is " + score);

        }else if(user1Arr.isEmpty() || user2Arr.isEmpty()){
            score ="0";

        } else {
            score = "0";
        }

        dialog.getUser().setScore(score);
        if(!mLocationDialogs.contains(dialog)) {
            mLocationDialogs.add(dialog);
        }
        Collections.sort(mLocationDialogs);
        mLocationDialogAdapter.notifyDataSetChanged();
        noUser.setVisibility(View.GONE);
        Log.d(TAG,"new item added");

        }


    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    public MaterialDialog ProgressDialog(Object object){
       String radius =String .valueOf(object);
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .content("Searching People Nearby in "+radius+" radius.")
                .progress(true, 0)
                .build();
        dialog.setCanceledOnTouchOutside(false);
       return dialog;
    }



    private void showSingleChoiceDialog() {

        new MaterialDialog.Builder(this)
                .title(R.string.radius_title)
                .items(R.array.radius)
                .itemsCallbackSingleChoice(index, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        index =which;
                        radius = convertMileStringtoKm(String.valueOf(text));;
                        if(radius<2){

                            ZoomLevel =13f;
                        }else if(radius>15 && radius<32){

                            ZoomLevel =11f;
                        }else{

                            ZoomLevel =9f;
                        }
                        map.clear();
                        center =true;
                        noUser.setVisibility(View.GONE);


                        mLocationDialogs.clear();
                        mLocationDialogAdapter.notifyDataSetChanged();
                        mHashMap.clear();
                        mProgressDialog = ProgressDialog(text);
                        mProgressDialog.show();
                        if(mCurrentLocation!=null) {
                            findPeopleNearby(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), radius);
                        }
                        return true;
                    }
                })
                .positiveText(R.string.ok).show();
    }

    public void addmarkerWithTitle(LatLng latLng,Marker marker){
        Geocoder geocoder;

        List<Address> yourAddresses = new ArrayList<>();
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            yourAddresses= geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (yourAddresses.size() > 0)
        {
            String yourAddress = yourAddresses.get(0).getAddressLine(0);
            marker.setTitle(yourAddress);



            //MyMarker offsetItem = new MyMarker(latLng.latitude, latLng.longitude,yourAddress,yourCity);
           // mClusterManager.addItem(offsetItem);

        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {
        Log.d(TAG,"new key entered "+ dataSnapshot.getKey()) ;
        if(dataSnapshot.getKey()!=null){
            String userUid =dataSnapshot.getKey();
            LatLng latLng = new LatLng(location.latitude,location.longitude);
            addCustomMarkerFromURL(latLng,userUid);

        }
    }

    @Override
    public void onDataExited(DataSnapshot dataSnapshot) {
        if(dataSnapshot.getKey()!=null){
            String userUid =dataSnapshot.getKey();
            if(mHashMap.containsKey(userUid)){
                mHashMap.get(userUid).remove();
                mHashMap.remove(userUid);
            }
            UserLocationDialog temp =new UserLocationDialog();
            temp.setId(userUid);
            if(mLocationDialogs.contains(temp)) {
                mLocationDialogs.remove(temp);
                mLocationDialogAdapter.notifyDataSetChanged();
            }
            if(mLocationDialogs.isEmpty() && mRecyclerView.getVisibility() == View.VISIBLE){
                noUser.setVisibility(View.VISIBLE);
            }



        }
    }

    @Override
    public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) {
        if(dataSnapshot.getKey()!=null){
            Log.d(TAG,"user moved "+ dataSnapshot.getKey()) ;
            // .makeText(getApplicationContext(),"user moved "+ dataSnapshot.getKey(),Toast.LENGTH_LONG).show();
            String userUid =dataSnapshot.getKey();
            LatLng latLng = new LatLng(location.latitude,location.longitude);
            if(mHashMap.containsKey(userUid) && mHashMap.get(userUid)!=null){
                Log.d(TAG,userUid+ " old marker moved before "+  mHashMap.get(userUid).getPosition()) ;
                mHashMap.get(userUid).hideInfoWindow();
                mHashMap.get(userUid).setPosition(latLng);
                addmarkerWithTitle(latLng, mHashMap.get(userUid));


                Log.d(TAG,userUid+ " old marker moved updated "+  mHashMap.get(userUid).getPosition()) ;
                UserLocationDialog temp =new UserLocationDialog();
                temp.setId(userUid);
                if(mLocationDialogs.contains(temp)) {
                    int index = mLocationDialogs.indexOf(temp);
                    if(index!=-1) {
                        User user = mLocationDialogs.get(index).getUser();
                        updateList(userUid, new LatLng(location.latitude,location.longitude),user);
                    }
                }


            }
        }
    }

    @Override
    public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {
        Log.d(TAG,dataSnapshot.toString());

    }

    @Override
    public void onGeoQueryReady() {
        if(mLocationDialogAdapter.getItemCount()>0 && !mLocationDialogs.isEmpty()){

            noUser.setVisibility(View.GONE);
        }else if(mLocationDialogs.isEmpty() && mRecyclerView.getVisibility() == View.VISIBLE){
            noUser.setVisibility(View.VISIBLE);
        }else{
            noUser.setVisibility(View.GONE);
        }
        mProgressDialog.dismiss();



    }

    @Override
    public void onGeoQueryError(DatabaseError error) {
        mProgressDialog.dismiss();
        Toast.makeText(getApplicationContext(), error.getMessage(),Toast.LENGTH_LONG).show();

    }
}


