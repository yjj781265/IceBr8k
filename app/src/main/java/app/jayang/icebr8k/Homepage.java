package app.jayang.icebr8k;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
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
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.onesignal.OSSubscriptionObserver;
import com.onesignal.OSSubscriptionStateChanges;
import com.onesignal.OneSignal;

import com.zplesac.connectionbuddy.ConnectionBuddy;
import com.zplesac.connectionbuddy.interfaces.ConnectivityChangeListener;
import com.zplesac.connectionbuddy.models.ConnectivityEvent;
import com.zplesac.connectionbuddy.models.ConnectivityState;

import app.jayang.icebr8k.Adapter.ViewPagerAdapter;
import app.jayang.icebr8k.Fragments.SurveyTab_Fragment;
import app.jayang.icebr8k.Fragments.UserMessageDialog_Frag;
import app.jayang.icebr8k.Fragments.me_frag;
import app.jayang.icebr8k.Utility.ActivityCommunicator;
import app.jayang.icebr8k.Modle.myViewPager;
import app.jayang.icebr8k.Utility.MyJobService;


public class Homepage extends AppCompatActivity  implements
        OSSubscriptionObserver,
        ConnectivityChangeListener,
        SharedPreferences.OnSharedPreferenceChangeListener,ActivityCommunicator {
    private final int  REQUEST_CHECK_SETTINGS =9000;
    private AHBottomNavigation homepageTab;
    private SwitchCompat mSwitchCompat;
    private String radius = "1 mi";
    private ImageView menu;
    private TextView menuBadge;
    private ViewPagerAdapter mViewPagerAdapter;
    private TextView noConnection_tv;
    protected myViewPager viewPager;
    private FirebaseUser currentUser;
    private long lastClickTime = 0;
    private DatabaseReference mRef;
    private int index =0;
    private DatabaseReference presenceRef,lastSeenRef;
     private ScreenStateReceiver mReceiver;
     private Drawer drawer;


    private String TAG = "homePage";
    private final String DEFAULTURL = "https://i.imgur.com/xUAsoWs.png";

    //job scheduler variables
    private static final String Job_TaG ="MY_JOB_TAG";
    private FirebaseJobDispatcher mDispatcher;


  // nav drawer item
    private PrimaryDrawerItem friendRequest, addFriend,feedback,logOut,setting;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        // prevent flash on status bar

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        menu = findViewById(R.id.menu);
        menuBadge = findViewById(R.id.menuBadge);
        setSupportActionBar((Toolbar) findViewById(R.id.users_toolbar));
        getSupportActionBar().setTitle("");


        homepageTab = findViewById(R.id.bottom_navigation);
        viewPager = findViewById(R.id.homepage_viewpager);
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





        //enable onesignal notification service
        OneSignal.addSubscriptionObserver(this);
        OneSignal.setSubscription(true);

        Log.d("haha", currentUser.getPhotoUrl().toString() + " from currentuserAfterUpdate");


        initialiseOnlinePresence();
        unReadCheck();

        setUpNavDrawer();



        if("public".equals(getPrivacySharedPreference())){
            startJob();
        }else if("private".equals(getPrivacySharedPreference())){
            stopJob();
        }
        // bottom nav bar
        setHomepageTab();

        if(getIntent().getExtras()!=null) {
           // extras for chat Page
            String chatId = getIntent().getExtras().getString("chatId");
            String name = getIntent().getExtras().getString("chatName");

            // extras for reply Page
            String questionId = getIntent().getExtras().getString("questionId");
            String topCommentId = getIntent().getExtras().getString("topCommentId");
            String commentId = getIntent().getExtras().getString("commentId");

            if (getIntent().getExtras().getString("mainchat") != null) {
                viewPager.setCurrentItem(1);
            }else if (chatId!=null && !chatId.isEmpty() && name !=null && !name.isEmpty()){

                Toast.makeText(this, chatId + name, Toast.LENGTH_SHORT).show();
                Intent mIntent = new Intent(this, UserChatActvity.class);
                getIntent().addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                mIntent.putExtra("chatId", chatId);
                mIntent.putExtra("chatName", name);
                startActivity(mIntent);
                overridePendingTransition(R.anim.slide_from_right,0);
            }else if(questionId!=null ){
                Intent mIntent = new Intent(this, QuestionActivity.class);
                mIntent.putExtra("questionId", questionId);
                mIntent.putExtra("topCommentId", topCommentId);
                mIntent.putExtra("commentId", commentId);

                startActivity(mIntent);
            }
        }
        setScreenOnOffListener();

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


    @Override
    protected void onNewIntent(Intent intent) {





        if(intent.getExtras()!=null) {
            // extras for chat Page
            String chatId = intent.getExtras().getString("chatId");
            String name = intent.getExtras().getString("chatName");


            // extras for reply Page
            String questionId = intent.getExtras().getString("questionId",null);
            String topCommentId = intent.getExtras().getString("topCommentId");
            String commentId = intent.getExtras().getString("commentId");

           if (chatId!=null && !chatId.isEmpty() && name !=null && !name.isEmpty()){
                Intent mIntent = new Intent(this, UserChatActvity.class);
                getIntent().addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                mIntent.putExtra("chatId", chatId);
                mIntent.putExtra("chatName", name);
                startActivity(mIntent);
                overridePendingTransition(R.anim.slide_from_right,0);
            }else if(questionId!=null && !questionId.isEmpty() ){
                Intent mIntent = new Intent(this, QuestionActivity.class);
                mIntent.putExtra("questionId", questionId);
                mIntent.putExtra("topCommentId", topCommentId);
                mIntent.putExtra("commentId", commentId);

                startActivity(mIntent);
            }
        }




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


    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        //respond to menu item selection
        switch (item.getItemId()) {
            case R.id.add_friend:
                startActivity(new Intent(this, SearchUser.class));
                return true;
            case R.id.leaderboard:
                startActivity(new Intent(this, Leaderboard.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }


    @SuppressLint("RestrictedApi")









    @Override
    protected void onStart() {
        super.onStart();
        showLog("onStart");
        setOnline();

        Bitmap icon = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.ic_stat_onesignal_default);
        String label =Html.fromHtml("<font color=\"#fffff4\">" + "IceBr8k"+ "</font>").toString();
        ActivityManager.TaskDescription taskDescription = new ActivityManager.

                TaskDescription(label, icon, getResources().getColor(R.color.colorPrimary));
        ((Activity)this).setTaskDescription(taskDescription);

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        sharedPref.registerOnSharedPreferenceChangeListener(this);

        ConnectionBuddy.getInstance().registerForConnectivityEvents(this, this);

        //check total num quesitons
       /* DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("Questions_8");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Toast.makeText(Homepage.this, "Question count " +dataSnapshot.getChildrenCount(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/


    }

    @Override
    protected void onResume() {
        super.onResume();
        if(checkInternet()){
            // device has active internet connection
            noConnection_tv.setVisibility(View.GONE);
            setOnline();

        }
        else{
            // there is no active internet connection on this device
            noConnection_tv.setVisibility(View.VISIBLE);
        }


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
        try {
            if (ConnectionBuddy.getInstance() != null) {
                ConnectionBuddy.getInstance().unregisterFromConnectivityEvents(this);
            }
        }catch (Exception e){

        }



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

    void setUpNavDrawer(){

        friendRequest = new PrimaryDrawerItem().withName("Friend Request").
                withIcon(R.drawable.user_icon).withBadge("1").withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.holo_red_light));
        addFriend  = new PrimaryDrawerItem().withName("Add Friend").withIcon(R.drawable.ic_action_addfriend);
        feedback  = new PrimaryDrawerItem().withName(getString(R.string.feedback)).withIcon(R.drawable.ic_action_feedback);
        logOut  = new PrimaryDrawerItem().withName("Log Out").withIcon(R.drawable.ic_action_exit);

        setting = new PrimaryDrawerItem().withName("Settings").withIcon(R.drawable.ic_action_settings);





        IProfile profile =  new ProfileDrawerItem().withName(currentUser.getDisplayName()).
                withEmail(currentUser.getEmail()).withIcon(currentUser.getPhotoUrl());



        // Create the AccountHeader
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)

                .withSelectionListEnabledForSingleProfile(false)
                .withAlternativeProfileHeaderSwitching(false)
                .withHeaderBackground(R.drawable.header_img)
                .addProfiles(
                        profile
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();


//create the drawer and remember the `Drawer` result object
         drawer  = new DrawerBuilder()
                .withActivity(this)
                .withSelectedItem(-1)
                 .withActionBarDrawerToggle(false)
                .withAccountHeader(headerResult)
                .withToolbar( (Toolbar) findViewById(R.id.users_toolbar))
                .addDrawerItems(
                        friendRequest, addFriend,feedback, setting
                ).addStickyDrawerItems(logOut)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        Intent intent;
                        switch (position) {
                            case -1:

                                if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                                    return false;
                                }

                                lastClickTime = SystemClock.elapsedRealtime();
                                Signout();
                                finish();
                                return true;

                            case 1:
                                if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                                    return false;
                                }
                                lastClickTime = SystemClock.elapsedRealtime();
                                Intent i = new Intent(getApplicationContext(),FriendRequestPage.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivity(i);
                                drawer.setSelection(-1);
                                drawer.closeDrawer();
                                return true;

                            case 2:
                                if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                                    return false;
                                }

                                lastClickTime = SystemClock.elapsedRealtime();
                                Intent mintent = new Intent(getApplicationContext(),SearchUser.class);
                                mintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivity(mintent);
                                drawer.setSelection(-1);
                                drawer.closeDrawer();
                                return true;
/*
            case R.id.scan_qr:
                if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                    return false;
                }

                lastClickTime = SystemClock.elapsedRealtime();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    checkCameraPermission();
                } else {
                    Intent intent = new Intent(getApplicationContext(),ScannerActivity.class);
                   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
                return true;
                */
                          /*  case R.id.people_nearby:
                                if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                                    return false;
                                }
                                lastClickTime = SystemClock.elapsedRealtime();

                                // if share my postion is on
                                if("public".equals(getPrivacySharedPreference())){
                                    showSingleChoiceDialog();
                                }else {
                                    new MaterialDialog.Builder(getApplicationContext())
                                            .content("\"Share My Location\" is disabled, you can go to Me tab to enable it.")
                                            .positiveText(R.string.ok).onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            viewPager.setCurrentItem(2,true);
                                        }
                                    })
                                            .show();
                                }
                                return true;*/
                            case 3:
                                if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                                    return false;
                                }
                                lastClickTime = SystemClock.elapsedRealtime();
                                intent = new Intent(getApplicationContext(),Feedback.class);
                                startActivity(intent);
                                drawer.setSelection(-1);
                                drawer.closeDrawer();
                                return true;


                            case 4:
                                if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                                    return false;
                                }
                                lastClickTime = SystemClock.elapsedRealtime();
                                intent = new Intent(getApplicationContext(), Settings_Activity.class);
                                startActivity(intent);
                                drawer.setSelection(-1);
                                drawer.closeDrawer();
                                return true;

                            default:
                                return false;
                        }
                    }
                })
                .build();

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer();
            }
        });
        setBadge();

    }




    public void checkCameraPermission(){
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                        Intent intent = new Intent(getApplicationContext(),ScannerActivity.class);
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
                getReference("UserMessages/" + currentUser.getUid());
        unReadcountRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = 0;
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    Integer i = childSnapshot.child("unread").getValue(Integer.class);
                    if (i != null) {
                        count = count + i;
                    }
                }
                if (count > 0) {
                    homepageTab.setNotification(String.valueOf(count), 1);

                } else {
                    homepageTab.setNotification("", 1);

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void setBadge(){
        DatabaseReference badgeRef = FirebaseDatabase.getInstance().getReference().child("UserFriends")
                .child(currentUser.getUid());
        badgeRef.keepSynced(true);

        badgeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count =0;
                for(DataSnapshot chidSnapShot : dataSnapshot.getChildren()){
                    if("pending".equals(chidSnapShot.child("stats").getValue(String.class))){
                        count++;
                    }
                }

                if(count==0){
                    menuBadge.setText("");
                    menuBadge.setVisibility(View.GONE);
                    friendRequest.withBadge( (StringHolder)(null));
                   drawer.updateItem(friendRequest);
                }else{
                    menuBadge.setText(""+count);
                    menuBadge.setVisibility(View.VISIBLE);
                    YoYo.with(Techniques.Pulse).repeat(666).playOn(menuBadge);
                    friendRequest.withBadge(""+count);
                    drawer.updateItem(friendRequest);

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
                    lastSeenRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
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

    mViewPagerAdapter.addFragment(new UserMessageDialog_Frag());
      mViewPagerAdapter.addFragment(new me_frag());





      viewPager.setAdapter(mViewPagerAdapter);
      viewPager.setCurrentItem(0);


        homepageTab.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
        homepageTab.setAccentColor(getResources().getColor(R.color.colorPrimary));


        AHBottomNavigationItem item1 = new AHBottomNavigationItem("Questions",
                R.drawable.survey_tab_icon);

        AHBottomNavigationItem item2 = new AHBottomNavigationItem("Chat",
                R.drawable.message_selector);
          AHBottomNavigationItem item3 = new AHBottomNavigationItem("Me", R.drawable.me_selector);

        // set my avatar via firebase changes






        // Add items

        homepageTab.addItem(item1);
        homepageTab.addItem(item2);
        homepageTab.addItem(item3);





        // for smooth swipe
        viewPager.setOffscreenPageLimit(2);

        // Set listeners
        homepageTab.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                viewPager.setCurrentItem(position,false);
                return true;
            }
        });
    }










    public void setOnline(){
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            presenceRef.setValue("2");
            lastSeenRef.removeValue();
        }
        final DatabaseReference oldOnLineref = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(currentUser.getUid());
        oldOnLineref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("onlineStats")){
                    oldOnLineref.child("onlineStats").removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
        if(event.getState().getValue() == ConnectivityState.CONNECTED){
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
    public void passDataToActivity(Object view,String tag) {
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
                        intent.putExtra("index",index);
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













