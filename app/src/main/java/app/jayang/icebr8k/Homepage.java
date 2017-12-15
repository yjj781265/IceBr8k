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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
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
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.BottomBarTab;
import com.roughike.bottombar.OnTabSelectListener;


import java.util.HashMap;

import app.jayang.icebr8k.Fragments.SurveyTab_Fragment;
import app.jayang.icebr8k.Fragments.Userstab_Fragment;
import app.jayang.icebr8k.Fragments.chat_frag;
import app.jayang.icebr8k.Modle.User;

public class Homepage extends AppCompatActivity implements OSSubscriptionObserver,chat_frag.OnCompleteListener,GoogleApiClient.OnConnectionFailedListener  {
    BottomBar homepageTab;
    ViewPager viewPager;
    DatabaseReference mRef;
    FirebaseUser currentUser;
    BottomBarTab chatTab;
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
        unReadCheck();

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new SurveyTab_Fragment());
        viewPagerAdapter.addFragment(new Userstab_Fragment());
        viewPagerAdapter.addFragment(new chat_frag());
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(2);
        homepageTab.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(int tabId) {
                switch (tabId){
                    case R.id.tab_survey:
                        viewPager.setCurrentItem(0,false);

                        break;

                    case R.id.tab_users:
                        viewPager.setCurrentItem(1,false);

                        break;

                    case R.id.tab_message:
                        viewPager.setCurrentItem(2,false);

                        break;

                    default:
                        viewPager.setCurrentItem(0,false);
                        break;


                }
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                homepageTab.selectTabAtPosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        profileImg = findViewById(R.id.imageBtn);
        profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), UserProfilePage.class);
                intent.putExtra("selfProfile", currentUserDB);
                startActivity(intent);

            }
        });








    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Toast.makeText(getApplicationContext(),"New Intent",Toast.LENGTH_SHORT).show();
        if(intent!=null) {
            if (intent.getExtras().getString("mainchat") != null) {
                viewPager.setCurrentItem(2);
                homepageTab.selectTabWithId(R.id.tab_message);
            }else{
                viewPager.setCurrentItem(0);
                homepageTab.selectTabWithId(R.id.tab_survey);
            }
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
                YoYo.with(Techniques.FadeIn).duration(300).playOn(profileImg);


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
                chatTab = homepageTab.getTabWithId(R.id.tab_message);
                int count = 0;
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    Integer i = childSnapshot.child("unRead").getValue(Integer.class);
                    if (i != null) {
                        count = count + i;
                    }
                }
                if (count > 0) {
                    chatTab.setBadgeCount(count);
                    mToolbar.setTitle("IceBr8k"+"("+count+")");

                } else {
                    chatTab.removeBadge();
                    mToolbar.setTitle("IceBr8k");
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
