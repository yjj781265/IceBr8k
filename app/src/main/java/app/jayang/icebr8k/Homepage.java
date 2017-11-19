package app.jayang.icebr8k;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.florent37.bubbletab.BubbleTab;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OSSubscriptionObserver;
import com.onesignal.OSSubscriptionStateChanges;
import com.onesignal.OneSignal;


import java.util.HashMap;

import app.jayang.icebr8k.Fragments.SurveyTab_Fragment;
import app.jayang.icebr8k.Fragments.Userstab_Fragment;
import app.jayang.icebr8k.Fragments.chat_frag;
import app.jayang.icebr8k.Modle.User;

public class Homepage extends AppCompatActivity implements OSSubscriptionObserver,chat_frag.OnCompleteListener,GoogleApiClient.OnConnectionFailedListener  {
    TabLayout homepageTab;
    ViewPager viewPager;
    DatabaseReference mRef;
    FirebaseUser currentUser;
    ImageView reddot;
    ImageView profileImg;
    User currentUserDB;
    GoogleApiClient mGoogleApiClient;
    Toolbar mToolbar;
    ViewPagerAdapter viewPagerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        mToolbar = findViewById(R.id.users_toolbar);
        setSupportActionBar(mToolbar);



        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        getCurrentUserDB(currentUser);

        OneSignal.addSubscriptionObserver(this);
        OneSignal.setSubscription(true);


        HashMap<String, Object> map = new HashMap<>();
        map.put("onlineStats", "1");
        mRef = FirebaseDatabase.getInstance().getReference("Users/" + currentUser.getUid());
        mRef.updateChildren(map);
        mRef.child("onlineStats").onDisconnect().setValue("0");


        Boolean inChatRoom = false;
        DatabaseReference inChatRef = FirebaseDatabase.getInstance().getReference("Messages/" + currentUser.getUid());
        inChatRef.child("inChatRoom").setValue(inChatRoom);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // [END configure_signin]

        // [START build_client]
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        homepageTab = findViewById(R.id.homepageTab);
        viewPager = findViewById(R.id.homepage_viewpager);
        reddot = findViewById(R.id.red_dot);
        reddot.setVisibility(View.GONE);
        unReadCheck();

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new SurveyTab_Fragment());
        viewPagerAdapter.addFragment(new Userstab_Fragment());
        viewPagerAdapter.addFragment(new chat_frag());
        viewPager.setAdapter(viewPagerAdapter);
        homepageTab.setupWithViewPager(viewPager);
        viewPager.setOffscreenPageLimit(2);
        profileImg = findViewById(R.id.imageBtn);
        profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), UserProfilePage.class);
                intent.putExtra("selfProfile", currentUserDB);
                startActivity(intent);
            }
        });

        homepageTab.getTabAt(0).setIcon(R.drawable.survey_selector);
        homepageTab.getTabAt(1).setIcon(R.drawable.user_selector);
        homepageTab.getTabAt(2).setIcon(R.drawable.message_selector);

        if( getIntent().getExtras().getString("mainchat")!=null){
            viewPager.setCurrentItem(2);
        }




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.log_out_item:
                Signout();
                 finish();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public User getCurrentUserDB(FirebaseUser currentUser) {
        FirebaseDatabase.getInstance().getReference("Users/" + currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUserDB = dataSnapshot.getValue(User.class);
                Glide.with(getApplicationContext()).load(currentUserDB.getPhotourl()).
                        apply(RequestOptions.circleCropTransform()).into(profileImg);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return currentUserDB;

    }



    @Override
    protected void onStart() {
        super.onStart();




    }

    @Override
    protected void onResume() {
        super.onResume();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Users/" + currentUser.getUid());
            mRef.child("onlineStats").setValue("1");
        }
        Snackbar snackbar = Snackbar.make(viewPager, "No internet connection", Snackbar.LENGTH_SHORT);
        ConnectivityManager cm =
                (ConnectivityManager) viewPager.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            snackbar.show();
        } else {
            snackbar.dismiss();
        }



    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Users/" + currentUser.getUid());
            mRef.child("onlineStats").setValue("0");
        }
        Boolean inChatRoom = false;
        DatabaseReference inChatRef = FirebaseDatabase.getInstance().getReference("Messages/" + currentUser.getUid());
        inChatRef.child("inChatRoom").setValue(inChatRoom);
    }

    public void unReadCheck() {
        DatabaseReference mref = FirebaseDatabase.getInstance().getReference("Messages/" + currentUser.getUid());
        mref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = 0;
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    Integer i = childSnapshot.child("unRead").getValue(Integer.class);
                    if (i != null) {
                        count = count + i;
                    }
                }
                if (count > 0) {
                    reddot.setVisibility(View.VISIBLE);
                } else {
                    reddot.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onOSSubscriptionChanged(OSSubscriptionStateChanges stateChanges) {
        if (!stateChanges.getFrom().getSubscribed() &&
                stateChanges.getTo().getSubscribed()) {

            // get player ID
            String player_id = stateChanges.getTo().getUserId();
            DatabaseReference notificationRef = FirebaseDatabase.getInstance().
                    getReference("Notification");
            notificationRef.child(currentUser.getUid()).child("player_id").setValue(player_id);
            notificationRef.child(currentUser.getUid()).child("name").setValue(currentUser.getDisplayName());

        }

        Log.i("Debug", "onOSPermissionChanged: " + stateChanges);
    }


    @Override
    public void onBackPressed() {
        moveTaskToBack(true);

    }


    @Override
    public void onComplete() {
        Bundle extras = getIntent().getExtras();
        if (viewPagerAdapter.getCount() > 2) {


            if (extras.getString("user2Uid") != null) {
                Intent mIntent = new Intent(this, MainChatActivity.class);
                mIntent.putExtras(extras);
                startActivity(mIntent);
                getIntent().removeExtra("user2Uid");


            }
        }
    }

    public void Signout() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Users/" + currentUser.getUid());
            mRef.child("onlineStats").setValue("0");
        }
        OneSignal.setSubscription(false);
        FirebaseAuth.getInstance().signOut();



        if (currentUser.getProviders().get(0).contains("google")) {

            // Google sign out
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {

                        @Override
                        public void onResult(@NonNull Status status) {

                        }
                    });
        }
        Intent intent = new Intent(getApplicationContext(), login_page.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }



}
