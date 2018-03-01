package app.jayang.icebr8k;
import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.onesignal.OSSubscriptionObserver;
import com.onesignal.OSSubscriptionStateChanges;
import com.onesignal.OneSignal;
import com.zplesac.connectionbuddy.ConnectionBuddy;
import com.zplesac.connectionbuddy.cache.ConnectionBuddyCache;
import com.zplesac.connectionbuddy.interfaces.ConnectivityChangeListener;
import com.zplesac.connectionbuddy.models.ConnectivityEvent;
import com.zplesac.connectionbuddy.models.ConnectivityState;

import app.jayang.icebr8k.Adapter.ViewPagerAdapter;
import app.jayang.icebr8k.Fragments.SurveyTab_Fragment;
import app.jayang.icebr8k.Fragments.UserMessageDialog_Frag;
import app.jayang.icebr8k.Fragments.Userstab_Fragment;
import app.jayang.icebr8k.Fragments.chat_frag;
import app.jayang.icebr8k.Fragments.me_frag;
import app.jayang.icebr8k.Modle.ActivityCommunicator;
import app.jayang.icebr8k.Modle.myViewPager;


public class Homepage extends AppCompatActivity  implements
        OSSubscriptionObserver,chat_frag.OnCompleteListener,
        ConnectivityChangeListener,
        SharedPreferences.OnSharedPreferenceChangeListener,ActivityCommunicator {
    private final int  REQUEST_CHECK_SETTINGS =9000;
    private AHBottomNavigation homepageTab;
    private SwitchCompat mSwitchCompat;
    private String radius = "1 mi";
    private ViewPagerAdapter mViewPagerAdapter;
    private TextView noConnection_tv;
    protected myViewPager viewPager;
    private FirebaseUser currentUser;
    private long lastClickTime = 0;
    private Toolbar mToolbar;
    private DatabaseReference mRef;
    private int index =0;
    private DatabaseReference presenceRef,lastSeenRef;
     private ScreenStateReceiver mReceiver;

    private String TAG = "homePage";

    //job scheduler variables
    private static final String Job_TaG ="MY_JOB_TAG";
    private FirebaseJobDispatcher mDispatcher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        mToolbar = findViewById(R.id.users_toolbar);
        setSupportActionBar(mToolbar);
        homepageTab = findViewById(R.id.bottom_navigation);
        viewPager = findViewById(R.id.homepage_viewpager);
        viewPager.setSwipeable(true);
        noConnection_tv = findViewById(R.id.noConnection_tv);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mRef = FirebaseDatabase.getInstance().getReference();
        mRef.keepSynced(true);
        presenceRef = FirebaseDatabase.getInstance().getReference().child("Users").
                child(currentUser.getUid()).child("onlinestats");

        lastSeenRef = FirebaseDatabase.getInstance().getReference().child("Users").
                child(currentUser.getUid()).child("lastseen");
        lastSeenRef.keepSynced(true);

        mDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));



        showLog("onCreate");
        showLog(currentUser.getPhotoUrl().toString());


        /****************************google client stuff*/////////////////
        GoogleSignInOptions gso = new GoogleSignInOptions.
                Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();





        /**************************************************************//////
        if (savedInstanceState != null) {
            ConnectionBuddyCache.clearLastNetworkState(this);
        }
        ConnectionBuddy.getInstance().registerForConnectivityEvents(this, this);



        //enable onesignal notification service
        OneSignal.addSubscriptionObserver(this);
        OneSignal.setSubscription(true);

        Log.d("haha", currentUser.getPhotoUrl().toString() + " from currentuserAfterUpdate");


        initialiseOnlinePresence();
        deleteInChatRoomNode();
        unReadCheck();
        setBadge();


        if("public".equals(getPrivacySharedPreference())){
            startJob();
        }else if("private".equals(getPrivacySharedPreference())){
            stopJob();
        }
        // bottom nav bar
        setHomepageTab();

        if(getIntent().getExtras()!=null) {

            if (getIntent().getExtras().getString("mainchat") != null) {
                viewPager.setCurrentItem(2);
            }
            if (getIntent().getExtras().getString("user2Id") != null &&
                    getIntent().getExtras().getString("user2Name") != null){
                String user2Id = getIntent().getExtras().getString("user2Id");
                String name = getIntent().getExtras().getString("user2Name");
                Intent mIntent = new Intent(this, MainChatActivity.class);
                getIntent().addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                mIntent.putExtra("user2Id", user2Id);
                mIntent.putExtra("user2Name", name);
                startActivity(mIntent);
                overridePendingTransition(R.anim.slide_from_right,0);
            }
        }
        setScreenOnOffListener();

    }

    public void startJob(){
        Job job = mDispatcher.newJobBuilder().setService(MyJobService.class).
                setLifetime(Lifetime.FOREVER).setRecurring(true).setTag(Job_TaG).setTrigger(Trigger.executionWindow(600,900))
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL).setConstraints(Constraint.ON_ANY_NETWORK).setReplaceCurrent(true).build();
        mDispatcher.mustSchedule(job);
        Toast.makeText(this,"Sharing Location in the background ",Toast.LENGTH_LONG).show();
    }
    public void stopJob(){
        mDispatcher.cancel(Job_TaG);
        Toast.makeText(this,"Sharing Location off",Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onNewIntent(Intent intent) {
      //Toast.makeText(getApplicationContext(),"New Intent",Toast.LENGTH_SHORT).show();
        if (intent.getExtras() != null) {
            if (intent.getExtras().getString("mainchat") != null) {
                viewPager.setCurrentItem(2,false);
            }
            // handle notification clicked
            else if (intent.getExtras().getString("user2Id") != null &&
                    intent.getExtras().getString("user2Name") != null){
                String user2Id = intent.getExtras().getString("user2Id");
                String name = intent.getExtras().getString("user2Name");
                Intent mIntent = new Intent(this, MainChatActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                mIntent.putExtra("user2Id", user2Id);
                mIntent.putExtra("user2Name", name);
                startActivity(mIntent);
                overridePendingTransition(R.anim.slide_from_right,0);
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

                if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                    return false;
                }

                lastClickTime = SystemClock.elapsedRealtime();
                Signout();
                 finish();
                return true;

            case R.id.add_friend:
                if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                    return false;
                }

                lastClickTime = SystemClock.elapsedRealtime();
             Intent i = new Intent(getApplicationContext(),SearchUser.class);
             i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
             startActivity(i);
                return true;

            case R.id.scan_qr:
                if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                    return false;
                }

                lastClickTime = SystemClock.elapsedRealtime();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    checkCameraPermission();
                } else {
                    Intent intent = new Intent(getApplicationContext(),DevoderActivity.class);
                   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
                return true;
            case R.id.people_nearby:
                if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                    return false;
                }

                lastClickTime = SystemClock.elapsedRealtime();

                    // if share my postion is on
                    if("public".equals(getPrivacySharedPreference())){
                        showSingleChoiceDialog();
                    }else {
                        new MaterialDialog.Builder(this)
                                .content("\"Share My Location\" is disabled, you can go to Me tab to enable.")
                                .positiveText(R.string.ok).onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                viewPager.setCurrentItem(3,true);
                            }
                        })
                                .show();
                    }


                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }






    @Override
    protected void onStart() {
        super.onStart();
        showLog("onStart");
        setOnline();

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        sharedPref.registerOnSharedPreferenceChangeListener(this);
        if(checkInternet()){
            // device has active internet connection
            noConnection_tv.setVisibility(View.GONE);
            setOnline();

        }
        else{
            // there is no active internet connection on this device
            noConnection_tv.setVisibility(View.VISIBLE);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

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

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        sharedPref.unregisterOnSharedPreferenceChangeListener(this);


        showLog("onStop");
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        if(ConnectionBuddy.getInstance()!=null){
            ConnectionBuddy.getInstance().unregisterFromConnectivityEvents(this);
        }



        setOffline();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }


        showLog("onDestroy");

    }


    public void checkCameraPermission(){
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                        Intent intent = new Intent(getApplicationContext(),DevoderActivity.class);
                       intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);

                    }
                    @Override public void onPermissionDenied(PermissionDeniedResponse response) {
                        showSnackbarWithSetting("Camera Permission needed for Scanning QR Code",viewPager);
                    }
                    @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
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
                    mToolbar.setTitle("IceBr8k"+" ("+count+")");
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

    private void setBadge(){
        DatabaseReference badgeRef = FirebaseDatabase.getInstance().getReference().child("Friends")
                .child(currentUser.getUid());
        badgeRef.keepSynced(true);

        badgeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count =0;
                for(DataSnapshot chidSnapShot : dataSnapshot.getChildren()){
                    if("Pending".equals(chidSnapShot.child("stats").getValue(String.class))){
                        count++;
                    }
                }

                if(count==0){
                    homepageTab.setNotification("", 3);
                }else{
                    homepageTab.setNotification(String.valueOf(count), 3);
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

    public void setHomepageTab(){ mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mViewPagerAdapter.addFragment(new SurveyTab_Fragment());
      mViewPagerAdapter.addFragment(new Userstab_Fragment());
      mViewPagerAdapter.addFragment(new chat_frag() );
      mViewPagerAdapter.addFragment(new me_frag());
      mViewPagerAdapter.addFragment(new UserMessageDialog_Frag());


      viewPager.setAdapter(mViewPagerAdapter);

        homepageTab.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
        homepageTab.setAccentColor(getResources().getColor(R.color.colorPrimary));
        AHBottomNavigationItem item1 = new AHBottomNavigationItem("Survey",
                R.drawable.survey_selector);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem("Friends",
                R.drawable.user_selector);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem("Chat",
                R.drawable.message_selector);
        AHBottomNavigationItem item4 = new AHBottomNavigationItem("Me",
                R.drawable.me_selector);
        AHBottomNavigationItem item5 = new AHBottomNavigationItem("Me2",
                R.drawable.me_selector);


        // Add items
        homepageTab.addItem(item1);
        homepageTab.addItem(item2);
        homepageTab.addItem(item3);
        homepageTab.addItem(item4);
        homepageTab.addItem(item5);


        // for smooth swipe
        viewPager.setOffscreenPageLimit(4);
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







    public void deleteInChatRoomNode(){
        mRef.child("Messages").child(currentUser.getUid()).child("inChatRoom").setValue(null);
    }


    public void setOnline(){
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            presenceRef.setValue("2");
        }
    }

    public void setBusy(){
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            presenceRef.setValue("1");
        }
    }

    public void setOffline(){
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            presenceRef.setValue("0");
            lastSeenRef.setValue(ServerValue.TIMESTAMP);

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

    public void showSnackbarWithSetting(String str, View view){
        Snackbar snackbar = Snackbar
                .make(view, str, Snackbar.LENGTH_LONG)
                .setAction("Setting", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Context context = view.getContext();
                        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:" + context.getPackageName()));
                        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(myAppSettings);
                    }
                });
        snackbar.show();
    }



    @Override
    public void onComplete() {

    }


    public void Signout() {
       setOffline();
        //user will not receive notification
        OneSignal.setSubscription(false);
        if(FirebaseAuth.getInstance()!=null) {
            FirebaseAuth.getInstance().signOut();
        }
        stopJob();


        setUserPrivacy(false);

        Intent intent = new Intent(this, SplashScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();


    }

    // check internet connections
    @Override
    public void onConnectionChange(ConnectivityEvent event) {
        if(event.getState() == ConnectivityState.CONNECTED){
            // device has active internet connection
            noConnection_tv.setVisibility(View.GONE);
            setOnline();

        }
        else{
            // there is no active internet connection on this device
            noConnection_tv.setVisibility(View.VISIBLE);
            showToast("Lost Internet Connection");
        }
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

    public void setScreenOnOffListener(){
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new ScreenStateReceiver();
        registerReceiver(mReceiver, intentFilter);
    }

    private  boolean checkInternet(){
        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return  isConnected;
    }



    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        String privacy = sharedPreferences.getString(s, null);
        if(privacy!=null){
            if("public".equals(privacy)){
                //initGoogleMapLocation();
                startJob();

            }else if("private".equals(privacy)) {
                stopJob();
            }
        }
    }

    @Override
    public void passDataToActivity(View view) {
        mSwitchCompat= (SwitchCompat) view;
    }

    public class ScreenStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                showLog("Screen ON");
                setOnline();
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                showLog("Screen OFF");
                setBusy();
            }
        }
    }









    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode ==REQUEST_CHECK_SETTINGS && resultCode   ==RESULT_OK){
            setSharedPreference(true);

        }else if(requestCode ==REQUEST_CHECK_SETTINGS && resultCode!=RESULT_OK){
            setSharedPreference(false);


        }
    }




    private String getPrivacySharedPreference(){
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String defaultValue = "private";
        String privacy = sharedPref.getString(currentUser.getUid()+"privacy", defaultValue);
        return privacy;
    }

    private void showSingleChoiceDialog() {

        new MaterialDialog.Builder(this)
                .title(R.string.radius_title)
                .items(R.array.radius)
                .itemsCallbackSingleChoice(index, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        index =which;
                        radius = String.valueOf(text);
                        Intent intent = new Intent(getApplicationContext(),PeopleNearby.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.putExtra("radius",radius);
                         startActivity(intent);
                        return true;
                    }
                })
                .positiveText(R.string.ok).show();
    }

    private void setSharedPreference(boolean isChecked){
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if(isChecked) {
            editor.putString(currentUser.getUid()+"privacy", "public");
            editor.commit();
            if(mSwitchCompat!=null){
                mSwitchCompat.setChecked(true);
            }
        }else{
            editor.putString(currentUser.getUid()+"privacy", "private");
            editor.commit();
            if(mSwitchCompat!=null){
                mSwitchCompat.setChecked(false);
            }
        }
    }

    public myViewPager getViewPager() {
        return viewPager;
    }

    public void setViewPager(myViewPager viewPager) {
        this.viewPager = viewPager;
    }
}













