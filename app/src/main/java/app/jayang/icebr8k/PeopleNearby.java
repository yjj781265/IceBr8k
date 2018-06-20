package app.jayang.icebr8k;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
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

import java.util.ArrayList;
import java.util.Collections;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import app.jayang.icebr8k.Adapter.UserLocationDialogAdapter;
import app.jayang.icebr8k.Modle.User;

import app.jayang.icebr8k.Modle.UserLocationDialog;

import app.jayang.icebr8k.Modle.UserQA;
import app.jayang.icebr8k.Utility.Compatability;
import app.jayang.icebr8k.Utility.MyDateFormatter;
import belka.us.androidtoggleswitch.widgets.BaseToggleSwitch;
import belka.us.androidtoggleswitch.widgets.ToggleSwitch;

public class PeopleNearby extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, BaseToggleSwitch.OnToggleSwitchChangeListener, GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GeoQueryDataEventListener,
        GoogleMap.OnInfoWindowClickListener {

    private final int UPDATE_INTERVAL = 5000; //5sec
    private final int FASTEST_INTERVAL = 3000;
    private final int DISPLACEMENT = 3;
    private final int REQUEST_CHECK_SETTINGS = 9000;
    private final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 8000;
    private FirebaseUser curretUser;
    private GoogleMap map;
    private ToggleSwitch mToggleSwitch;
    private FrameLayout mFrameLayout;
    private RecyclerView mRecyclerView;
    private View mCustomerMarkerView;
    private ImageView mMarkerImageView;
    private ImageButton mLocationButton;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private Boolean center = true, flag = false;
    private Double radius = 1.6;
    private float ZoomLevel = 17f;
    private MaterialDialog mProgressDialog;
    private GeoFire geofire;
    private Toolbar mToolbar;
    private TextView noUser;
    private int index = 0;
    private Handler handler;

    private SupportMapFragment mapFragment;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private HashMap<String, Marker> mHashMap;
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
        mFrameLayout = findViewById(R.id.people_mapFrame);
        mRecyclerView = findViewById(R.id.people_recyclerView);
        mfilter = findViewById(R.id.people_filter);
        mToolbar = findViewById(R.id.people_toolbar);
        noUser = findViewById(R.id.people_noUsers);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mLocationButton = findViewById(R.id.people_myLocation);
        curretUser = FirebaseAuth.getInstance().getCurrentUser();
        mHashMap = new HashMap<>();
        mLocationDialogs = new ArrayList<>();
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
        if (checkGooglePlayService()) {
            buildGoogleApiClient();
            mLocationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mCurrentLocation != null && map != null) {
                        float zoom = map.getCameraPosition().zoom;
                        Log.d(TAG, "ZOOM LEVEL " + zoom);
                        Log.d(TAG, "center true");
                        center = true;
                        LatLng latLng = new LatLng(mCurrentLocation.getLatitude(),
                                mCurrentLocation.getLongitude());
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f));
                    }
                }
            });
        }
        if (getIntent().getExtras() != null) {
            String radiusString = getIntent().getExtras().getString("radius");
            radius = convertMileStringtoKm(radiusString);
            index = getIntent().getExtras().getInt("index");
            if (index == 0) {
                ZoomLevel = 9f;
            } else if (index == 1) {
                ZoomLevel = 7f;
            }  else {
                ZoomLevel = 5f;
            }
            mProgressDialog = ProgressDialog(radiusString);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    finish();
                }
            });

        }
        mfilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSingleChoiceDialog();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.peoplenearby_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.peoplenearby_maptype:
                if (map != null) {
                    showMapTypeSelectorDialog();
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
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
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
        if (geoQuery != null) {
            geoQuery.removeGeoQueryEventListener(this);

        }
    }

    private static final String[] MAP_TYPE_ITEMS =
            {"Road Map", "Satellite", "Terrain", "Hybrid"};

    private void showMapTypeSelectorDialog() {
        // Prepare the dialog by setting up a Builder.
        final String fDialogTitle = "Select Map Type";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(fDialogTitle);

        // Find the current map type to pre-check the item representing the current state.
        int checkItem = map.getMapType() - 1;

        // Add an OnClickListener to the dialog, so that the selection will be handled.
        builder.setSingleChoiceItems(
                MAP_TYPE_ITEMS,
                checkItem,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {
                        // Locally create a finalised object.

                        // Perform an action depending on which item was selected.
                        switch (item) {
                            case 0:
                                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                break;
                            case 1:
                                map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                                break;
                            case 2:
                                map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                                break;
                            case 3:
                                map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                                break;
                            default:
                                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        }
                        dialog.dismiss();
                    }
                }
        );

        // Build the dialog and show it.
        AlertDialog fMapTypeDialog = builder.create();
        fMapTypeDialog.setCanceledOnTouchOutside(true);
        fMapTypeDialog.show();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @SuppressLint("RestrictedApi")
    private void startLocationMonitoring() {
        mProgressDialog.show();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                super.onLocationResult(result);

                mCurrentLocation = result.getLocations().get(0);
                if (mCurrentLocation != null) {
                    updateLocationtoDatabase(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), radius);
                    // flag first time open the map
                    if (flag == false) {
                        findPeopleNearby(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), radius);
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude()
                                    ,mCurrentLocation.getLongitude()), ZoomLevel));

                        flag = true;
                    }

                }

                Log.d(TAG, "Current location:\n" + mCurrentLocation);

            }
        };
        Log.d(TAG, "startLocation called");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY).
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
                    ActivityCompat.requestPermissions(PeopleNearby.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_FINE_LOCATION);

                    mProgressDialog.dismiss();
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

                } catch (Exception excpetion) {
                    // Toast.makeText(PeopleNearby.this, excpetion.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    startLocationMonitoring();

                } else {
                    Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS && resultCode == RESULT_OK) {
            if (checkGooglePlayService()) {
                //  Toast.makeText(this, "okay", Toast.LENGTH_SHORT).show();
                //reset
                mHashMap.clear();
                map.clear();
                mLocationDialogs.clear();
                mLocationDialogAdapter.notifyDataSetChanged();

                startLocationMonitoring();
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
        map.getUiSettings().setMapToolbarEnabled(false);


        map.setOnMarkerClickListener(this);
        map.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                ZoomLevel = map.getCameraPosition().zoom;
                //Log.d(TAG, "ZOOM LEVEL "+ZoomLevel);
            }
        });
        map.setOnInfoWindowClickListener(this);
        map.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                Log.d(TAG, "center off " + i);
                if (i == REASON_GESTURE) {
                    center = false;
                }
            }
        });





        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;

        }
        map.setMyLocationEnabled(true);






    }


    @Override
    public boolean onMyLocationButtonClick() {
       // Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {


    }

    @Override
    public void onToggleSwitchChangeListener(int position, boolean isChecked) {
     if(isChecked){
         switch (position){
             case 1:
                 mFrameLayout.setVisibility(View.VISIBLE);
                 mRecyclerView.setVisibility(View.GONE);
                 noUser.setVisibility(View.GONE);

                 break;
             case 0:
                 mFrameLayout.setVisibility(View.GONE);
                 mRecyclerView.setVisibility(View.VISIBLE);
                 if(mLocationDialogs.isEmpty() || (mLocationDialogs.size()==1
                 && mLocationDialogs.get(0).getId().equals(curretUser.getUid()))){
                     noUser.setVisibility(View.VISIBLE);
                 }else {
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






    private boolean checkGooglePlayService(){
        int response =GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if(response!= ConnectionResult.SUCCESS){
            GoogleApiAvailability.getInstance().getErrorDialog(this,response,1).show();
            return false;
        }else{
            return  true;
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

    private void findPeopleNearby(final double lat, final double lng, final double radius){
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("GeoFireLocations");
                geofire = new GeoFire(ref);
                geoQuery = geofire.queryAtLocation(new GeoLocation(lat,lng), radius);
                geoQuery.addGeoQueryDataEventListener(PeopleNearby.this);
            }
        },666);

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
         startLocationMonitoring();
      }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


/***************************get score*****************************************/

public void compareWithUser2(final UserLocationDialog dialog) {
    final ArrayList<UserQA> userQA1 = new ArrayList<>();
    final ArrayList<UserQA> userQA2 = new ArrayList<>();
    DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("UserQA/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
    final DatabaseReference mRef2 = FirebaseDatabase.getInstance().getReference("UserQA/" + dialog.getId());

    mRef.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            for(DataSnapshot child : dataSnapshot.getChildren()){
                if( !"skipped".equals(child.getValue(UserQA.class).getAnswer())){
                    userQA1.add(child.getValue(UserQA.class));
                }

            }


            mRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot child : dataSnapshot.getChildren()){
                        if(!"skipped".equals(child.getValue(UserQA.class).getAnswer())){
                            userQA2.add(child.getValue(UserQA.class));
                        }


                    }

                    Compatability mCompatability = new Compatability(userQA1,userQA2);
                    dialog.setScore(mCompatability.getScore().toString());
                    addMapMarker(dialog);

                    if(mLocationDialogs.contains(dialog)){
                        int i = mLocationDialogs.indexOf(dialog);
                        mLocationDialogs.set(i,dialog);
                    }
                    Collections.sort(mLocationDialogs);
                    mLocationDialogAdapter.notifyDataSetChanged();
                    if(mProgressDialog.isShowing()){
                        mProgressDialog.dismiss();
                    }



                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    });



}





    /*************************** end get score*****************************************/

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
                        if(index ==0){
                            ZoomLevel =9f;
                        }else if(index == 1){
                            ZoomLevel =7f;
                        }else{
                            ZoomLevel =5f;
                        }
                        map.clear();
                        center =true;
                        noUser.setVisibility(View.GONE);


                        mLocationDialogs.clear();
                        mHashMap.clear();
                        mProgressDialog = ProgressDialog(text);
                        mProgressDialog.show();
                        if(mCurrentLocation!=null) {
                            findPeopleNearby(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), radius);
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLocation.getLatitude()
                                    ,mCurrentLocation.getLongitude()), ZoomLevel));
                        }
                        return true;
                    }
                })
                .positiveText(R.string.ok).show();
    }


    private String distanceConverter(String dist){
        double miles = Double.valueOf(dist);
        int intMiles;
        String distance;
        String unit;
        String str;
        intMiles = (int)Math.round(miles*5280);
        if(intMiles<=1000){
            unit="ft";
            distance = String.valueOf(intMiles);
        }else{
            int scale = (int) Math.pow(10, 1);
            miles =(double) Math.round(miles * scale) / scale;
            distance = String.valueOf(miles);
            unit ="mi";
        }
        return  distance+unit + " away";
    }


    private void addMapMarker(final UserLocationDialog dialog){



        if(mHashMap.get(dialog.getId())!=null){
            mHashMap.get(dialog.getId()).setPosition(dialog.getLatLng());

                String title = dialog.getUser().getDisplayname()+"("+distanceConverter(dialog.getDistance())+")";
                String snippet = "Compatibility: " + dialog.getScore()+"%";


            mHashMap.get(dialog.getId()).hideInfoWindow();
            mHashMap.get(dialog.getId()).setTitle(title);
            mHashMap.get(dialog.getId()).setSnippet(snippet);

            if (dialog.getId().equals(curretUser.getUid()) && center) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(dialog.getLatLng(), ZoomLevel));
            }
        }else{
            String photoUrl = dialog.getUser().getPhotourl();
            Glide.with(getApplicationContext()).asBitmap().load(photoUrl).
                    apply(RequestOptions.circleCropTransform()).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {

                    String title = dialog.getUser().getDisplayname() +"("+distanceConverter(dialog.getDistance())+")";
                    String snippet = "Compatibility: " + dialog.getScore()+"%";

                    Marker marker;
                    marker = map.addMarker(new MarkerOptions().position(dialog.getLatLng())
                            .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomerMarkerView, resource))));
                    mHashMap.put(dialog.getId(),marker);

                    mHashMap.get(dialog.getId()).hideInfoWindow();
                    marker.setTitle(title);
                    marker.setSnippet(snippet);




                }


            });

        }







    }

    private  void setListView(final UserLocationDialog dialog){

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(dialog.getId());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(user.getDisplayname()!=null && user.getPhotourl()!=null &&
                        user.getOnlinestats()!=null &&user.getUsername()!=null && "public".equals(user.getPrivacy())){
                    dialog.setUser(user);
                    if(!dialog.getId().equals(curretUser.getUid())){
                        Location destLocaiton = new Location("");
                        destLocaiton.setLatitude(dialog.getLatLng().latitude);
                        destLocaiton.setLongitude(dialog.getLatLng().longitude);
                        float meter = mCurrentLocation.distanceTo(destLocaiton);
                        double miles = (double) meter * 0.000621371192;
                        String distance = String.valueOf(miles);
                        dialog.setDistance(distance);

                        compareWithUser2(dialog);
                    }


                }
                if( mLocationDialogs.isEmpty() && mRecyclerView.getVisibility()==View.VISIBLE){
                    noUser.setVisibility(View.VISIBLE);
                }else{
                    noUser.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public void addPrivacyListener (final UserLocationDialog dialog){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(dialog.getId())
                .child("privacy");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
            //    Toast.makeText(PeopleNearby.this, "changed", Toast.LENGTH_SHORT).show();
                String privacy = dataSnapshot.getValue(String.class);
                if("private".equals(privacy)){
                    if(mHashMap.get(dialog.getId())!=null){
                        mHashMap.get(dialog.getId()).remove();
                        mHashMap.remove(dialog.getId());
                    }
                    if(mLocationDialogs.contains(dialog)){
                        mLocationDialogs.remove(dialog);
                        mLocationDialogAdapter.notifyDataSetChanged();
                        if(mLocationDialogs.isEmpty() && mRecyclerView.getVisibility() ==View.VISIBLE){
                            noUser.setVisibility(View.VISIBLE);
                        }
                    }
                }else{

                    if(mLocationDialogs.contains(dialog)){
                       setListView(dialog);
                    }else{
                        mLocationDialogs.add(dialog);
                        setListView(dialog);
                        noUser.setVisibility(View.GONE);
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
        Log.d(TAG,"new key entered " + radius+ " "+ dataSnapshot.getKey()) ;
        if(dataSnapshot.getKey()!=null && !curretUser.getUid().equals(dataSnapshot.getKey()) ){
            String userUid =dataSnapshot.getKey();
            UserLocationDialog dialog = new UserLocationDialog();
            dialog.setId(userUid);
            LatLng latLng = new LatLng(location.latitude,location.longitude);
            dialog.setLatlng(latLng);
            if(!mLocationDialogs.contains(dialog )&& !userUid.equals(curretUser.getUid())){
                if(dataSnapshot.hasChild("timestamp")){
                        dialog.setTimestamp(dataSnapshot.child("timestamp").getValue(Long.class));
                }

                addPrivacyListener(dialog);
            }
            //dont add self in the list view
            if(userUid.equals(curretUser.getUid())){
                if(dataSnapshot.hasChild("timestamp")){
                    dialog.setTimestamp(dataSnapshot.child("timestamp").getValue(Long.class));
                }
              setListView(dialog);
            }




        }
    }

    @Override
    public void onDataExited(DataSnapshot dataSnapshot) {
        if(dataSnapshot.getKey()!=null && !curretUser.getUid().equals(dataSnapshot.getKey())){
           String uid = dataSnapshot.getKey();
           if(uid.equals(curretUser.getUid())){
               map.clear();
               mLocationDialogs.clear();
               mHashMap.clear();
               findPeopleNearby(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude(),radius);
           }else{
               UserLocationDialog dialog = new UserLocationDialog();
               dialog.setId(uid);
               mLocationDialogs.remove(dialog);
               mLocationDialogAdapter.notifyDataSetChanged();
               if(mHashMap.get(uid)!=null){
                   mHashMap.get(uid).remove();
                   mHashMap.remove(uid);
               }
            }

        }
    }

    @Override
    public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) {

    }

    @Override
    public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {
        if(dataSnapshot.getKey()!=null && !curretUser.getUid().equals(dataSnapshot.getKey())){
            String uid = dataSnapshot.getKey();
            UserLocationDialog dialog = new UserLocationDialog();
            LatLng latLng = new LatLng(location.latitude,location.longitude);
            dialog.setLatlng(latLng);
            dialog.setId(uid);
            if(uid.equals(curretUser.getUid())){
                mCurrentLocation.setLongitude(location.longitude);
                mCurrentLocation.setLatitude(location.latitude);
            }
            if(dataSnapshot.hasChild("timestamp")){
                dialog.setTimestamp(dataSnapshot.child("timestamp").getValue(Long.class));
            }
            setListView(dialog);




        }

    }

    @Override
    public void onGeoQueryReady() {
        mProgressDialog.dismiss();





    }

    @Override
    public void onGeoQueryError(DatabaseError error) {
        mProgressDialog.dismiss();
        Toast.makeText(getApplicationContext(), error.getMessage(),Toast.LENGTH_LONG).show();

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
       for(UserLocationDialog dialog : mLocationDialogs){
           if(dialog.getLatLng().equals(marker.getPosition())){
               Intent i =  new Intent(this, UserProfilePage.class);
               i.putExtra("userInfo",dialog.getUser());
               i.putExtra("userUid",dialog.getId());
               startActivity(i);
               break;
           }
       }
    }
}


