package app.jayang.icebr8k;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
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
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.onesignal.OSSubscriptionObserver;
import com.onesignal.OSSubscriptionStateChanges;
import com.onesignal.OneSignal;
import com.zplesac.connectionbuddy.ConnectionBuddy;
import com.zplesac.connectionbuddy.cache.ConnectionBuddyCache;
import com.zplesac.connectionbuddy.interfaces.ConnectivityChangeListener;
import com.zplesac.connectionbuddy.models.ConnectivityEvent;
import com.zplesac.connectionbuddy.models.ConnectivityState;

import app.jayang.icebr8k.Fragments.SurveyTab_Fragment;
import app.jayang.icebr8k.Fragments.Userstab_Fragment;
import app.jayang.icebr8k.Fragments.chat_frag;


public class Homepage extends AppCompatActivity  implements
        OSSubscriptionObserver,chat_frag.OnCompleteListener,
        GoogleApiClient.OnConnectionFailedListener,ConnectivityChangeListener {
   private AHBottomNavigation homepageTab;
   private TextView noConnection_tv;
   private ViewPager viewPager;
   private ViewPagerAdapter mViewPagerAdapter;
   private FirebaseUser currentUser;
   private GoogleApiClient mGoogleApiClient;
   private Toolbar mToolbar;
   private DatabaseReference mRef;
   private DatabaseReference presenceRef;
   private CoordinatorLayout mCoordinatorLayout;
   private ScreenStateReceiver mReceiver;
   private String TAG ="homePage";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        mToolbar = findViewById(R.id.users_toolbar);
        setSupportActionBar(mToolbar);
        homepageTab = findViewById(R.id.bottom_navigation);
        viewPager = findViewById(R.id.homepage_viewpager);
        noConnection_tv = findViewById(R.id.noConnection_tv);
        mCoordinatorLayout = findViewById(R.id.coordLayout);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mRef =  FirebaseDatabase.getInstance().getReference();
        mRef.keepSynced(true);
        presenceRef = FirebaseDatabase.getInstance().getReference().child("Users").
                child(currentUser.getUid()).child("onlineStats");
        presenceRef.keepSynced(true);

        showLog("onCreate");
        showLog(currentUser.getPhotoUrl().toString());



        /****************************google client stuff*/////////////////
        GoogleSignInOptions gso =new GoogleSignInOptions.
                Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        /**************************************************************//////
        if(savedInstanceState != null){
            ConnectionBuddyCache.clearLastNetworkState(this);
        }


        //enable onesignal notification service
        OneSignal.addSubscriptionObserver(this);
        OneSignal.setSubscription(true);

        Log.d("haha",currentUser.getPhotoUrl().toString() +" from currentuserAfterUpdate");

        initialiseOnlinePresence();
        setScreenOnOffListener();
        deleteInChatRoomNode();
        unReadCheck();
        setOnline();

        // bottom nav bar
        setHomepageTab();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
       // Toast.makeText(getApplicationContext(),"New Intent",Toast.LENGTH_SHORT).show();
        if(intent!=null) {
            if (intent.getExtras().getString("mainchat") != null) {
                viewPager.setCurrentItem(2);

            }else{
                viewPager.setCurrentItem(0);

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


    @Override
    protected void onStart() {
        super.onStart();
        showLog("onStart");


    }

    @Override
    protected void onResume() {
        super.onResume();
        ConnectionBuddy.getInstance().registerForConnectivityEvents(this, this);
        setOnline();
        showLog("onResume");

    }

    @Override
    protected void onPause() {
        super.onPause();
        showLog("onPause");


    }

    @Override
    protected void onStop() {
        super.onStop();
        ConnectionBuddy.getInstance().unregisterFromConnectivityEvents(this);
        showLog("onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setOffline();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        showLog("onDestroy");

    }

    public void unReadCheck() {
        DatabaseReference unReadcountRef = FirebaseDatabase.getInstance().
                getReference("Messages/" + currentUser.getUid());
        unReadcountRef.addValueEventListener(new ValueEventListener() {
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
                    homepageTab.setNotification(String.valueOf(count), 2);
                    mToolbar.setTitle("IceBr8k"+"("+count+")");
                } else {
                    homepageTab.setNotification("", 2);
                    mToolbar.setTitle("IceBr8k");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    private void initialiseOnlinePresence() {

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                Log.d("haha", "DataSnapshot:" + dataSnapshot);
                if (dataSnapshot.hasChildren()) {
                    presenceRef.onDisconnect().setValue("0");

                }
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
                Log.d("haha", "DatabaseError:" + databaseError);
            }
        });
    }

    public void showToast(String str){
        Toast.makeText(getApplicationContext(),str,Toast.LENGTH_LONG).show();
    }

    private void showLog(String str){ Log.d(TAG,str);}

    public void setHomepageTab(){mViewPagerAdapter =
            new ViewPagerAdapter(getSupportFragmentManager());
      mViewPagerAdapter.addFragment(new SurveyTab_Fragment());
      mViewPagerAdapter.addFragment(new Userstab_Fragment());
      mViewPagerAdapter.addFragment(new chat_frag() );
      viewPager.setAdapter(mViewPagerAdapter);

        homepageTab.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
        homepageTab.setAccentColor(getResources().getColor(R.color.primary));
        AHBottomNavigationItem item1 = new AHBottomNavigationItem("Survey",
                R.drawable.survey_selector);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem("Friends",
                R.drawable.user_selector);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem("Chat",
                R.drawable.message_selector);
        // Add items
        homepageTab.addItem(item1);
        homepageTab.addItem(item2);
        homepageTab.addItem(item3);
        // for smooth swipe
        viewPager.setOffscreenPageLimit(2);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                homepageTab.setCurrentItem(position);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        // Set listeners
        homepageTab.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                viewPager.setCurrentItem(position,false);
                return true;
            }
        });
    }



    public void updateDrawerUI(){
        Drawer drawer= new DrawerBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggle(true).withToolbar(mToolbar).
                        withActionBarDrawerToggleAnimated(true)
                .addDrawerItems(
                        //pass your items here
                )
                .build();
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
    }


    public void deleteInChatRoomNode(){
        mRef.child("Messages").child(currentUser.getUid()).child("inChatRoom").setValue(null);
    }


    public void setOnline(){
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            presenceRef.setValue("1");
        }
    }

    public void setOffline(){
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            presenceRef.setValue("0");
        }

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

        Log.i("haha", "onOSPermissionChanged: " + stateChanges);
    }


    @Override
    public void onBackPressed() {
        moveTaskToBack(true);

    }


    @Override
    public void onComplete() {
        Bundle extras = getIntent().getExtras();
        if (homepageTab.getItemsCount() > 2) {
            if (extras.getString("user2Uid") != null) {
                Intent mIntent = new Intent(this, MainChatActivity.class);
                mIntent.putExtras(extras);
                startActivity(mIntent);
                getIntent().removeExtra("user2Uid");
            }
        }
    }


    public void Signout() {
       setOffline();
        //user will not receive notification
        OneSignal.setSubscription(false);
        if(FirebaseAuth.getInstance()!=null) {
            FirebaseAuth.getInstance().signOut();
        }
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
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    // check internet connections
    @Override
    public void onConnectionChange(ConnectivityEvent event) {
        if(event.getState() == ConnectivityState.CONNECTED){
            // device has active internet connection
            noConnection_tv.setVisibility(View.GONE);


        }
        else{
            // there is no active internet connection on this device
            noConnection_tv.setVisibility(View.VISIBLE);
            showToast("Lost Internet Connection");
        }
    }

    public void setScreenOnOffListener(){
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new ScreenStateReceiver();
        registerReceiver(mReceiver, intentFilter);
    }

    public class ScreenStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                showLog("Screen ON");
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                showLog("Screen OFF");
            }
        }
    }


}
