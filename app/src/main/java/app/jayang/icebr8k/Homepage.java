package app.jayang.icebr8k;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.Toast;

import com.github.florent37.bubbletab.BubbleTab;
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

public class Homepage extends AppCompatActivity implements OSSubscriptionObserver,chat_frag.OnCompleteListener {
    TabLayout homepageTab;
    ViewPager viewPager;
    DatabaseReference mRef;
    FirebaseUser currentUser;
    ImageView reddot;
    String user2Id;
    ViewPagerAdapter viewPagerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);


        currentUser = FirebaseAuth.getInstance().getCurrentUser();

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

        homepageTab.getTabAt(0).setIcon(R.drawable.survey_selector);
        homepageTab.getTabAt(1).setIcon(R.drawable.user_selector);
        homepageTab.getTabAt(2).setIcon(R.drawable.message_selector);

        if( getIntent().getExtras().getString("mainchat")!=null){
            viewPager.setCurrentItem(2);
        }




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
}
