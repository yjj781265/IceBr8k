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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
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


import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
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

import java.util.ArrayList;
import java.util.HashMap;

import app.jayang.icebr8k.Adapter.ViewPagerAdapter;
import app.jayang.icebr8k.Fragments.PeopleNearby_Fragment;
import app.jayang.icebr8k.Fragments.SurveyTab_Fragment;
import app.jayang.icebr8k.Fragments.UserMessageDialog_Frag;
import app.jayang.icebr8k.Fragments.me_frag;
import app.jayang.icebr8k.Modle.UserComp;
import app.jayang.icebr8k.Modle.UserQA;
import app.jayang.icebr8k.Utility.ActivityCommunicator;
import app.jayang.icebr8k.Modle.myViewPager;
import app.jayang.icebr8k.Utility.Compatability;
import app.jayang.icebr8k.Utility.DimmedPromptBackground;
import app.jayang.icebr8k.Utility.MyJobService;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;


public class Homepage extends AppCompatActivity implements
        OSSubscriptionObserver,
        ConnectivityChangeListener{
    private AHBottomNavigation homepageTab;


    private ImageView menu;
    private ActivityCommunicator mCommunicator;
    private TextView menuBadge;
    private ViewPagerAdapter mViewPagerAdapter;
    private TextView noConnection_tv;
    protected myViewPager viewPager;
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private long lastClickTime = 0;
    private DatabaseReference mRef;

    private DatabaseReference presenceRef, lastSeenRef;
    private ScreenStateReceiver mReceiver;
    private Drawer drawer;


    private String TAG = "homePage";
    private final String DEFAULTURL = "https://i.imgur.com/xUAsoWs.png";

    //job scheduler variables
    private static final String Job_TaG = "MY_JOB_TAG";
    private FirebaseJobDispatcher mDispatcher;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    // nav drawer item
    private PrimaryDrawerItem friendRequest, addFriend, feedback, logOut, setting;




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

        mRef = FirebaseDatabase.getInstance().getReference();
        mRef.keepSynced(true);
        presenceRef = FirebaseDatabase.getInstance().getReference().child("Users").
                child(currentUser.getUid()).child("onlinestats");


        lastSeenRef = FirebaseDatabase.getInstance().getReference().child("Users").
                child(currentUser.getUid()).child("lastseen");
        lastSeenRef.keepSynced(true);

        mDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
//tutorial sharedPref
        sharedPref = this.getSharedPreferences(
                "tutorial", Context.MODE_PRIVATE);
        editor = sharedPref.edit();


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


        // bottom nav bar
        setHomepageTab();
        // addUserQAListener();

        if (getIntent().getExtras() != null) {
            // extras for chat Page
            String chatId = getIntent().getExtras().getString("chatId");
            String name = getIntent().getExtras().getString("chatName");

            // extras for reply Page
            String questionId = getIntent().getExtras().getString("questionId");
            String topCommentId = getIntent().getExtras().getString("topCommentId");
            String commentId = getIntent().getExtras().getString("commentId");

            if (getIntent().getExtras().getString("mainchat") != null) {
                viewPager.setCurrentItem(1);
            } else if (chatId != null && !chatId.isEmpty() && name != null && !name.isEmpty()) {

                Toast.makeText(this, chatId + name, Toast.LENGTH_SHORT).show();
                Intent mIntent = new Intent(this, UserChatActvity.class);
                getIntent().addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                mIntent.putExtra("chatId", chatId);
                mIntent.putExtra("chatName", name);
                startActivity(mIntent);
                overridePendingTransition(R.anim.slide_from_right, 0);
            } else if (questionId != null) {
                Intent mIntent = new Intent(this, QuestionActivity.class);
                mIntent.putExtra("questionId", questionId);
                mIntent.putExtra("topCommentId", topCommentId);
                mIntent.putExtra("commentId", commentId);

                startActivity(mIntent);
            }
        }
        setScreenOnOffListener();

        // update Id;
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("Users");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ref.child(snapshot.getKey()).child("id").setValue(snapshot.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    @Override
    protected void onNewIntent(Intent intent) {


        if (intent.getExtras() != null) {
            // extras for chat Page
            String chatId = intent.getExtras().getString("chatId");
            String name = intent.getExtras().getString("chatName");


            // extras for reply Page
            String questionId = intent.getExtras().getString("questionId", null);
            String topCommentId = intent.getExtras().getString("topCommentId");
            String commentId = intent.getExtras().getString("commentId");

            if (chatId != null && !chatId.isEmpty() && name != null && !name.isEmpty()) {
                Intent mIntent = new Intent(this, UserChatActvity.class);
                getIntent().addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                mIntent.putExtra("chatId", chatId);
                mIntent.putExtra("chatName", name);
                startActivity(mIntent);
                overridePendingTransition(R.anim.slide_from_right, 0);
            } else if (questionId != null && !questionId.isEmpty()) {
                Intent mIntent = new Intent(this, QuestionActivity.class);
                mIntent.putExtra("questionId", questionId);
                mIntent.putExtra("topCommentId", topCommentId);
                mIntent.putExtra("commentId", commentId);

                startActivity(mIntent);
            }
        }


        if (checkInternet()) {
            // device has active internet connection
            noConnection_tv.setVisibility(View.GONE);
            setOnline();

        } else {
            // there is no active internet connection on this device
            noConnection_tv.setVisibility(View.VISIBLE);
        }


    }


    public boolean onCreateOptionsMenu(Menu menu) {

        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        new Handler().post(new Runnable() {
            @Override
            public void run() {

                final DatabaseReference userQARef = FirebaseDatabase.getInstance()
                        .getReference().child("UserQA")
                        .child(currentUser.getUid());
                final DatabaseReference friendsRef = FirebaseDatabase.getInstance()
                        .getReference().child("UserFriends")
                        .child(currentUser.getUid());
                userQARef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() >= 16) {
                            Log.d("homepage123", "more than 16 questions answered");
                            friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(final DataSnapshot dataSnapshot) {
                                    boolean show = false;
                                    if (dataSnapshot == null || dataSnapshot.getChildrenCount() == 0) {
                                        Log.d("homepage123", "hasn o friends");
                                        show = true;
                                    } else if (dataSnapshot.hasChild("stats") && dataSnapshot.getChildrenCount() > 0) {
                                        int counter = 0;
                                        for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                                            if (childSnap.child("stats").getValue(String.class).equals("accepted")) {
                                                counter++;
                                                break;
                                            }
                                        }
                                        Log.d("homepage123", "hasn " + counter + "friends");
                                        show = (counter == 0);
                                    }

                                    if (show) {
                                        showAddFriendPrompt(Homepage.this);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        });

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        //respond to menu item selection
        switch (item.getItemId()) {
            case R.id.add_friend:
                startActivity(new Intent(this, SearchUser.class));
                return true;
         /*   case R.id.leaderboard:
                startActivity(new Intent(this, Leaderboard.class));
                return true;*/

            default:
                return super.onOptionsItemSelected(item);

        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        showLog("onStart");
        setOnline();
// change icon when minimize
        Bitmap icon = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.ic_stat_onesignal_default);
        String label = Html.fromHtml("<font color=\"#fffff4\">" + "IceBr8k" + "</font>").toString();
        ActivityManager.TaskDescription taskDescription = new ActivityManager.

                TaskDescription(label, icon, getResources().getColor(R.color.colorPrimary));
        ((Activity) this).setTaskDescription(taskDescription);


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
        if (checkInternet()) {
            // device has active internet connection
            noConnection_tv.setVisibility(View.GONE);
            setOnline();

        } else {
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


        try {
            if (ConnectionBuddy.getInstance() != null) {
                ConnectionBuddy.getInstance().unregisterFromConnectivityEvents(this);
            }
        } catch (Exception e) {

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

    void setUpNavDrawer() {
        BadgeStyle badgeStyle = new BadgeStyle();
        badgeStyle.withBadgeBackground(getDrawable(R.drawable.badge_circle));
        badgeStyle.withCornersDp(16);
        badgeStyle.withTextColor(ContextCompat.getColor(this, R.color.white));

        friendRequest = new PrimaryDrawerItem().withName("Friend Request").
                withIcon(R.drawable.user_icon).withBadge("").withBadgeStyle(badgeStyle);
        addFriend = new PrimaryDrawerItem().withName("Add Friend").withIcon(R.drawable.ic_action_addfriend);
        feedback = new PrimaryDrawerItem().withName(getString(R.string.feedback)).withIcon(R.drawable.ic_action_feedback);
        logOut = new PrimaryDrawerItem().withName("Log Out").withIcon(R.drawable.ic_action_exit);

        setting = new PrimaryDrawerItem().withName("Settings").withIcon(R.drawable.ic_action_settings);


        IProfile profile = new ProfileDrawerItem().withName(currentUser.getDisplayName()).
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
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withSelectedItem(-1)
                .withActionBarDrawerToggle(false)
                .withAccountHeader(headerResult)
                .withToolbar((Toolbar) findViewById(R.id.users_toolbar))
                .addDrawerItems(
                        friendRequest, addFriend, feedback, setting
                ).addStickyDrawerItems(logOut)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        switch (position) {
                            case -1:

                                if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                                    return false;
                                }

                                lastClickTime = SystemClock.elapsedRealtime();
                                Signout();
                                finish();
                                return true;

                            case 1:
                                if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                                    return false;
                                }
                                lastClickTime = SystemClock.elapsedRealtime();
                                drawer.setSelection(-1);
                                drawer.closeDrawer();

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent i = new Intent(getApplicationContext(), FriendRequestPage.class);
                                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                        startActivity(i);
                                    }
                                },300);



                                return true;

                            case 2:
                                if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                                    return false;
                                }

                                lastClickTime = SystemClock.elapsedRealtime();
                                drawer.setSelection(-1);
                                drawer.closeDrawer();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent mintent = new Intent(getApplicationContext(), SearchUser.class);
                                        mintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                        startActivity(mintent);
                                    }
                                },300);



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
                                if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                                    return false;
                                }
                                lastClickTime = SystemClock.elapsedRealtime();
                                drawer.setSelection(-1);
                                drawer.closeDrawer();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(getApplicationContext(), Feedback.class);
                                        startActivity(intent);
                                    }
                                },300);


                                return true;


                            case 4:
                                if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                                    return false;
                                }
                                lastClickTime = SystemClock.elapsedRealtime();
                                drawer.setSelection(-1);
                                drawer.closeDrawer();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                       Intent intent = new Intent(getApplicationContext(), Settings_Activity.class);
                                        startActivity(intent);
                                    }
                                },300);

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
                    homepageTab.setNotification(String.valueOf(count), 2);

                } else {
                    homepageTab.setNotification("", 2);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void setBadge() {
        DatabaseReference badgeRef = FirebaseDatabase.getInstance().getReference().child("UserFriends")
                .child(currentUser.getUid());
        badgeRef.keepSynced(true);

        badgeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = 0;
                for (DataSnapshot chidSnapShot : dataSnapshot.getChildren()) {
                    if ("pending".equals(chidSnapShot.child("stats").getValue(String.class))) {
                        count++;
                    }
                }

                if (count == 0) {
                    menuBadge.setText("");
                    menuBadge.setVisibility(View.GONE);
                    friendRequest.withBadge((StringHolder) (null));
                    drawer.updateItem(friendRequest);
                } else {
                    menuBadge.setText("" + count);
                    menuBadge.setVisibility(View.VISIBLE);
                    YoYo.with(Techniques.Pulse).repeat(666).playOn(menuBadge);
                    friendRequest.withBadge("" + count);
                    drawer.updateItem(friendRequest);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void initialiseOnlinePresence() {
        final DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = dataSnapshot.getValue(Boolean.class);
                if (connected) {


                    // when this device disconnects, remove it
                    presenceRef.onDisconnect().setValue("0");

                    // when I disconnect, update the last time I was seen online
                    lastSeenRef.onDisconnect().setValue(ServerValue.TIMESTAMP);

                    presenceRef.setValue("2");
                    lastSeenRef.removeValue();

                    // add this device to my connections list
                    // this value could contain info about the device or a timestamp too

                }else{
                    presenceRef.setValue("0");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });






    }

    public void showToast(String str) {
        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
    }

    private void showLog(String str) {
        Log.d(TAG, str);
    }


    private void setHomepageTab() {


        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mViewPagerAdapter.addFragment(new SurveyTab_Fragment());
        mViewPagerAdapter.addFragment(new PeopleNearby_Fragment());
        mViewPagerAdapter.addFragment(new UserMessageDialog_Frag());
        mViewPagerAdapter.addFragment(new me_frag());
        viewPager.setAdapter(mViewPagerAdapter);
        viewPager.setCurrentItem(0);


        homepageTab.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
        homepageTab.setAccentColor(getResources().getColor(R.color.colorPrimary));
        homepageTab.setForceTint(true);


        AHBottomNavigationItem item1 = new AHBottomNavigationItem("Questions",
                R.drawable.survey_tab_icon);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem("Nearby", R.drawable.peoplenearby);

        AHBottomNavigationItem item3 = new AHBottomNavigationItem("Chat",
                R.drawable.message_selector);
        AHBottomNavigationItem item4 = new AHBottomNavigationItem("Me", R.drawable.me_selector);

        ArrayList<AHBottomNavigationItem> itemArrayList = new ArrayList<>();
        itemArrayList.add(item1);
        itemArrayList.add(item2);
        itemArrayList.add(item3);
        itemArrayList.add(item4);

        // Add items

        homepageTab.addItems(itemArrayList);

        homepageTab.setCurrentItem(0);

        // for smooth swipe
        viewPager.setOffscreenPageLimit(3);

        Log.d("HomePage123", "OutSide tabSelected " + homepageTab.getViewAtPosition(0));

        // Set listeners
        homepageTab.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                viewPager.setCurrentItem(position, false);
                if (position == 1) {
                    showNearbyPrompt(position);
                }
                Log.d("HomePage123", "Under tabSelected " + homepageTab.getViewAtPosition(0));
                return true;
            }
        });


    }


    void showAddFriendPrompt(Activity activity) {

        boolean show = sharedPref.getBoolean("tutorialAddFriend", true);

        if (show) {
            // show tutorial if necessary
            new MaterialTapTargetPrompt.Builder(activity)
                    .setPromptBackground(new DimmedPromptBackground())
                    .setBackgroundColour(ContextCompat.getColor(activity, R.color.colorPrimary))
                    .setIcon(R.drawable.ic_action_add_friend_white)
                    .setIconDrawableColourFilter(ContextCompat.getColor(this, R.color.colorPrimary))
                    .setTarget(R.id.add_friend).
                    setSecondaryText("If you already know some people using IceBr8k, you can put their username here and add them")
                    .show();

            editor.putBoolean("tutorialAddFriend", false);
            editor.commit();
        }


    }

    void showNearbyPrompt(final int position) {
        final boolean showPrompt = sharedPref.getBoolean("tutorialNearBy", true);

        if (showPrompt) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {

                    final DatabaseReference userQARef = FirebaseDatabase.getInstance()
                            .getReference().child("UserQA")
                            .child(currentUser.getUid());
                    final DatabaseReference friendsRef = FirebaseDatabase.getInstance()
                            .getReference().child("UserFriends")
                            .child(currentUser.getUid());
                    userQARef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getChildrenCount() >= 16) {
                                Log.d("homepage123", "more than 16 questions answered");
                                friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(final DataSnapshot dataSnapshot) {
                                        boolean noFriend = false;
                                        if (dataSnapshot == null || dataSnapshot.getChildrenCount() == 0) {
                                            Log.d("homepage123", "hasn o friends");
                                            noFriend = true;
                                        } else if (dataSnapshot.getChildrenCount() > 0) {
                                            int counter = 0;
                                            for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                                                if (childSnap.child("stats").getValue(String.class).equals("accepted")) {
                                                    counter++;
                                                    break;
                                                }
                                            }
                                            Log.d("homepage123", "hasn " + counter + "friends");
                                            noFriend = (counter == 0);
                                        }

                                        if (noFriend) {
                                            // show tutorial if necessary
                                            new MaterialTapTargetPrompt.Builder(Homepage.this)
                                                    .setPromptBackground(new DimmedPromptBackground())
                                                    .setBackgroundColour(ContextCompat.getColor(Homepage.this, R.color.colorPrimary))
                                                    .setTarget(homepageTab.getViewAtPosition(position)).
                                                    setSecondaryText("We see you haven't added anyone on IceBr8k yet. This tab will show who is using IceBr8k close to you")
                                                    .show();

                                            editor.putBoolean("tutorialNearBy", false);
                                            editor.commit();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                }
            });
        }
    }


    public void setOnline() {
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
                if (dataSnapshot.hasChild("onlineStats")) {
                    oldOnLineref.child("onlineStats").removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setBusy() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            presenceRef.setValue("1");
        }
    }

    public void setOffline() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            presenceRef.setValue("0");
            lastSeenRef.setValue(ServerValue.TIMESTAMP);

        }

    }


    public void addUserQAListener() {
        final DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("UserQA/" + currentUser.getUid());


        mRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        compareWithFriends();
                        return null;
                    }
                }.execute();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void compareWithFriends() {

        DatabaseReference mFriendRef = FirebaseDatabase.getInstance().getReference()
                .child("UserFriends").child(currentUser.getUid());
        mFriendRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                ArrayList<String> friendUidList = new ArrayList<>();

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    if (childSnapshot.hasChild("stats") &&
                            childSnapshot.child("stats").getValue(String.class).equals("accepted")) {
                        friendUidList.add(childSnapshot.getKey());
                    }
                }
                if (!friendUidList.isEmpty()) {
                    new CompareWithFriends().execute(friendUidList);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public void compareWithUser2(final String user2Uid) {
        final ArrayList<UserQA> userQA1 = new ArrayList<>();
        final ArrayList<UserQA> userQA2 = new ArrayList<>();
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("UserQA/" + currentUser.getUid());
        final DatabaseReference mRef2 = FirebaseDatabase.getInstance().getReference("UserQA/" + user2Uid);

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
                        UserComp userComp = new UserComp(mCompatability,user2Uid);

                        new SetScoreNodes().execute(userComp);


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

    public void setScoreNode(final String user2Uid, final String score) {
        DatabaseReference scoreRef = FirebaseDatabase.getInstance().getReference()
                .child("UserFriends")
                .child(currentUser.getUid())
                .child(user2Uid)
                .child("score");
        scoreRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {

                mutableData.setValue(score);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

            }
        });

        DatabaseReference scoreRef2 = FirebaseDatabase.getInstance().getReference()
                .child("UserFriends")
                .child(user2Uid)
                .child(currentUser.getUid())
                .child("score");

        scoreRef2.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {

                mutableData.setValue(score);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
// Transaction completed
                //Log.d("SurveyAdapter123r", "postTransaction:onComplete:" + databaseError);
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

        Log.i("haha", "onOSPermissionChanged: " + stateChanges);
    }


    @Override
    public void onBackPressed() {
        moveTaskToBack(true);

    }

    public void showSnackbarWithSetting(String str, View view) {
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

    public void stopJob() {
        mDispatcher.cancel(Job_TaG);
        // Toast.makeText(this,"Sharing Location off",Toast.LENGTH_LONG).show();
    }


    public void Signout() {
        setOffline();
        //user will not receive notification
        OneSignal.setSubscription(false);
        setUserPrivacy(false);
        if (FirebaseAuth.getInstance() != null) {
            FirebaseAuth.getInstance().signOut();
        }
        stopJob();


        Intent intent = new Intent(this, SplashScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();


    }

    // check internet connections
    @Override
    public void onConnectionChange(ConnectivityEvent event) {
        if (event.getState().getValue() == ConnectivityState.CONNECTED) {
            // device has active internet connection
            noConnection_tv.setVisibility(View.GONE);
            setOnline();

        } else {
            // there is no active internet connection on this device
            noConnection_tv.setVisibility(View.VISIBLE);
            showToast("Lost Internet Connection");
        }
    }

    private void setUserPrivacy(boolean isChecked) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").
                child(currentUser.getUid());
        if (isChecked) {
            ref.child("privacy").setValue("public");
        } else {
            ref.child("privacy").setValue("private");
        }
    }

    public void setScreenOnOffListener() {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new ScreenStateReceiver();
        registerReceiver(mReceiver, intentFilter);
    }

    private boolean checkInternet() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
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


    private String getPrivacySharedPreference() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String defaultValue = "private";
        String privacy = sharedPref.getString(currentUser.getUid() + "privacy", defaultValue);
        return privacy;
    }


    private void setSharedPreference(boolean isChecked) {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if (isChecked) {
            editor.putString(currentUser.getUid() + "privacy", "public");
            editor.commit();

        } else {
            editor.putString(currentUser.getUid() + "privacy", "private");
            editor.commit();

        }
    }


    public myViewPager getViewPager() {
        return viewPager;
    }



    public void setViewPager(myViewPager viewPager) {
        this.viewPager = viewPager;
    }

    public class CompareWithFriends extends AsyncTask<ArrayList<String>, Void, Void> {


        @Override
        protected Void doInBackground(ArrayList<String>... arrayLists) {
            ArrayList<String> mList = arrayLists[0];
            if (!mList.isEmpty()) {
                for (String uid : mList) {
                    compareWithUser2(uid);
                }
            }
            return null;
        }
    }

    public class SetScoreNodes extends AsyncTask<UserComp, Void, Void> {


        @Override
        protected Void doInBackground(UserComp... userComps) {
            Compatability compatability = userComps[0].getCompatability();
            String user2Uid = userComps[0].getUserUid();
            int score = compatability.getScore();
            setScoreNode(user2Uid,String.valueOf(score));
            return null;
        }
    }
}
















