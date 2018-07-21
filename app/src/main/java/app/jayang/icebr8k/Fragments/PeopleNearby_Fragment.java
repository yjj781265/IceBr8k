package app.jayang.icebr8k.Fragments;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import app.jayang.icebr8k.Adapter.UserLocationDialogAdapter;
import app.jayang.icebr8k.Modle.User;
import app.jayang.icebr8k.Modle.UserLocationDialog;
import app.jayang.icebr8k.Modle.UserQA;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.SearchPeopleNearby;
import app.jayang.icebr8k.Utility.Compatability;
import app.jayang.icebr8k.Utility.MyJobService;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class PeopleNearby_Fragment extends Fragment {
    final int MY_PERMISSIONS_LOCATION =6666;



    View mView;
    private TextView centerText;
   private  FirebaseJobDispatcher mDispatcher;
    private Button filter_btn;
    private ArrayList<UserLocationDialog > mLocationDialogs;
    private RecyclerView mRecyclerView;
    private Boolean firsttime = true, isPublic = false;
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private RelativeLayout loadingGif;
    private SwipeRefreshLayout mRefreshLayout;
    private UserLocationDialogAdapter mAdapter;
    private Location mLocation;
    private  Double radius = 80.4672; // in km
    private  int index = 0;
    private FrameLayout searchLayout;
    private  final String Job_TaG ="MY_JOB_TAG";
    private final String locationDisabledText = "\"Share My Location\" is disabled, click on the hamburger icon  "
            + getEmojiByUnicode(0x2630) + "  at the top left and go to Settings  "  +getEmojiByUnicode(0x02699)
            +"  to turn on \"Share My Location\"";

    private FusedLocationProviderClient mFusedLocationClient;


    public PeopleNearby_Fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView= inflater.inflate(R.layout.fragment_people_nearby, container, false);
        mLocationDialogs = new ArrayList<>();

        centerText = mView.findViewById(R.id.peoplenearby_centerText);
        loadingGif = mView.findViewById(R.id.loadingImg_peopleNearbyTab);
        mRecyclerView = mView.findViewById(R.id.peoplenearby_recyclerview);
        mRefreshLayout = mView.findViewById(R.id.peoplenearby_swipeRLayout);
        searchLayout = mView.findViewById(R.id.search_layout);
        filter_btn = mView.findViewById(R.id.filter_btn);
        loadingGif.setVisibility(View.VISIBLE);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

    //set up adapter and recyclerview
        mAdapter = new UserLocationDialogAdapter(mLocationDialogs);
        mAdapter.setHasStableIds(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);


       //show filter option
        filter_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSingleChoiceDialog();
            }
        });
        searchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), SearchPeopleNearby.class);
                i.putExtra("peopleNearbyList",  mLocationDialogs);
                startActivity(i);
                getActivity().overridePendingTransition(0,0);
            }
        });

        mDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(getContext()));

        if("public".equals(getPrivacySharedPreference())){
            setUserPrivacy(true);
            startJob();
        }else{
            stopJob();
            setUserPrivacy(false);
        }
       // Toast.makeText(getActivity(), getPrivacySharedPreference(), Toast.LENGTH_LONG).show();
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mRefreshLayout.setRefreshing(true);
               loadData();
            }
        });



        return  mView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(isVisibleToUser&& firsttime && getView()!=null){
            setCenterText();
            firsttime = false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    loadData();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    centerText.setText("Location Permission Denied");
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    public String getEmojiByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }


    private void setCenterText(){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(currentUser.getUid())
                .child("privacy");

        // center Text listener
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               if(dataSnapshot.getValue(String.class).equals("public")){
                   centerText.setVisibility(View.GONE);
                   isPublic = true;
                   loadData();
                   Log.d("pplnby",""+ "Loading data");
               }else{
                   loadingGif.setVisibility(View.GONE);
                   centerText.setText(locationDisabledText);
                   centerText.setVisibility(View.VISIBLE);
                   isPublic = false;
               }
               mRecyclerView.setVisibility(isPublic ? View.VISIBLE: View.GONE);
               mRefreshLayout.setVisibility(isPublic ? View.VISIBLE: View.GONE);
               searchLayout.setVisibility(isPublic ? View.VISIBLE: View.GONE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    void loadData(){
        // check permission
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // No explanation needed; request the permission
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_LOCATION);
        }else{
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                mLocation = location;
                                Log.d("pplnby",""+ "currentLocation got it");
                                findPeopleNearby(mLocation.getLatitude(),mLocation.getLongitude(),radius);
                            }
                        }
                    });


        }


    }


    private void findPeopleNearby(final double lat, final double lng, final double radius){

        mLocationDialogs.clear();
        mRecyclerView.setVisibility(View.GONE);

        Log.d("pplnby",""+ "finding peoplenearbyu");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("GeoFireLocations");
               GeoFire geofire = new GeoFire(ref);
               GeoQuery geoQuery = geofire.queryAtLocation(new GeoLocation(lat,lng), radius);
                geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
                    @Override
                    public void onDataEntered(DataSnapshot dataSnapshot, final GeoLocation location) {
                        Log.d("pplnby",""+ "find user "+ dataSnapshot.getKey());
                        final UserLocationDialog userLocationDialog = new UserLocationDialog();
                        if(!dataSnapshot.getKey().equals(currentUser.getUid())){
                            userLocationDialog.setId(dataSnapshot.getKey());
                            userLocationDialog.setLatlng(new LatLng(location.latitude,location.longitude));
                            // get user info
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                                    .child("Users").child(userLocationDialog.getId());
                            reference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    User user = dataSnapshot.getValue(User.class);
                                    userLocationDialog.setUser(user);
                                    if( user.getPrivacy()==null ||  user.getPrivacy().equals("private")){
                                        if(mLocationDialogs.contains(userLocationDialog)){
                                            mLocationDialogs.remove(userLocationDialog);
                                            mAdapter.notifyDataSetChanged();
                                            Log.d("pplnby",""+ "user has changed privacy "+user.getDisplayname());
                                        }
                                    }else{
                                        Log.d("pplnby","getting distance data " +user.getDisplayname());
                                        Location destLocaiton = new Location("");
                                        destLocaiton.setLatitude(userLocationDialog.getLatLng().latitude);
                                        destLocaiton.setLongitude(userLocationDialog.getLatLng().longitude);
                                        float meter = mLocation.distanceTo(destLocaiton);
                                        double miles = (double) meter * 0.000621371192;
                                        String distance = String.valueOf(miles);
                                        userLocationDialog.setDistance(distance);
                                        compareWithUser2(userLocationDialog);

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                    }

                    @Override
                    public void onDataExited(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) {

                    }

                    @Override
                    public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {

                        final UserLocationDialog userLocationDialog = new UserLocationDialog();
                        if(!dataSnapshot.getKey().equals(currentUser.getUid())){
                            userLocationDialog.setId(dataSnapshot.getKey());
                            userLocationDialog.setLatlng(new LatLng(location.latitude,location.longitude));
                            // get user info
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                                    .child("Users").child(userLocationDialog.getId());
                            reference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    User user = dataSnapshot.getValue(User.class);
                                    userLocationDialog.setUser(user);
                                    if( user.getPrivacy()==null ||  user.getPrivacy().equals("private")){
                                        if(mLocationDialogs.contains(userLocationDialog)){
                                            mLocationDialogs.remove(userLocationDialog);
                                            mAdapter.notifyDataSetChanged();
                                            Log.d("pplnby",""+ "user has changed privacy "+user.getDisplayname());
                                        }
                                    }else{
                                        Log.d("pplnby","getting distance data " +user.getDisplayname());
                                        Location destLocaiton = new Location("");
                                        destLocaiton.setLatitude(userLocationDialog.getLatLng().latitude);
                                        destLocaiton.setLongitude(userLocationDialog.getLatLng().longitude);
                                        float meter = mLocation.distanceTo(destLocaiton);
                                        double miles = (double) meter * 0.000621371192;
                                        String distance = String.valueOf(miles);
                                        userLocationDialog.setDistance(distance);
                                        compareWithUser2(userLocationDialog);

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }


                    }

                    @Override
                    public void onGeoQueryReady() {
                        loadingGif.setVisibility(View.GONE);
                        mRefreshLayout.setRefreshing(false);
                        mRecyclerView.setVisibility(View.VISIBLE);


                    }

                    @Override
                    public void onGeoQueryError(DatabaseError error) {

                    }
                });



    }


    public void compareWithUser2(final UserLocationDialog dialog) {
        Log.d("pplnby",""+ "getting score "+dialog.getUser().getDisplayname());
        final ArrayList<UserQA> userQA1 = new ArrayList<>();
        final ArrayList<UserQA> userQA2 = new ArrayList<>();
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("UserQA/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
        final DatabaseReference mRef2 = FirebaseDatabase.getInstance().getReference("UserQA/" + dialog.getId());

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    if (!"skipped".equals(child.getValue(UserQA.class).getAnswer())) {
                        userQA1.add(child.getValue(UserQA.class));
                    }

                }


                mRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            if (!"skipped".equals(child.getValue(UserQA.class).getAnswer())) {
                                userQA2.add(child.getValue(UserQA.class));
                            }


                        }

                        Compatability mCompatability = new Compatability(userQA1, userQA2);
                        dialog.setScore(mCompatability.getScore().toString());


                        if (mLocationDialogs.contains(dialog)) {
                            int i = mLocationDialogs.indexOf(dialog);
                            mLocationDialogs.set(i, dialog);
                        }else{
                            mLocationDialogs.add(dialog);
                        }
                        Log.d("pplnby",""+ "ADDED");
                        Collections.sort(mLocationDialogs);
                        mAdapter.notifyDataSetChanged();


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

    private void showSingleChoiceDialog() {

        new MaterialDialog.Builder(getContext())
                .title(R.string.radius_title)
                .items(R.array.radius)
                .itemsCallbackSingleChoice(index, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        index =which;
                        radius = convertMileStringtoKm(String.valueOf(text));;
                        if(mLocation!=null) {
                            findPeopleNearby(mLocation.getLatitude(), mLocation.getLongitude(), radius);

                        }
                        return true;
                    }
                })
                .positiveText(R.string.ok).show();
    }

    private Double convertMileStringtoKm(String string){
        string = string.replaceAll("\\D+","");
        double radius = Double.valueOf(string)*1.60934;
        return radius;
    }

    private String getPrivacySharedPreference(){
        SharedPreferences sharedPref =  getActivity().getSharedPreferences  ("privacy",MODE_PRIVATE);
        String privacy = sharedPref.getString(currentUser.getUid()+"privacy", "private");
        return privacy;
    }



    public void startJob(){
        Job job = mDispatcher.newJobBuilder().setService(MyJobService.class).
                setLifetime(Lifetime.FOREVER).setRecurring(true).setTag(Job_TaG).setTrigger(Trigger.executionWindow(600,900))
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL).setConstraints(Constraint.ON_ANY_NETWORK).setReplaceCurrent(true).build();
        mDispatcher.mustSchedule(job);
        //Toast.makeText(this,"Sharing Location in the background ",Toast.LENGTH_LONG).show();
    }
    public void stopJob(){
        mDispatcher.cancel(Job_TaG);
        // Toast.makeText(this,"Sharing Location off",Toast.LENGTH_LONG).show();
    }

    private void setUserPrivacy(boolean isChecked){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").
                child(currentUser.getUid());
        if(isChecked) {
            ref.child("privacy").setValue("public");
        }else{
            ref.child("privacy").setValue("private");
        }
    }






}
