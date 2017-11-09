package app.jayang.icebr8k;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.florent37.bubbletab.BubbleTab;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import app.jayang.icebr8k.Fragments.SurveyTab_Fragment;
import app.jayang.icebr8k.Fragments.Userstab_Fragment;

public class Homepage extends AppCompatActivity {
    BubbleTab bubbleTab;
    ViewPager viewPager;
    DatabaseReference mRef;
    FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        bubbleTab = findViewById(R.id.bubbleTab);

        viewPager = findViewById(R.id.homepage_viewpager);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewPagerAdapter.addFragment(new SurveyTab_Fragment());
        viewPagerAdapter.addFragment(new Userstab_Fragment());

        viewPager.setAdapter(viewPagerAdapter);
        bubbleTab.setupWithViewPager(viewPager);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();


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
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            mRef = FirebaseDatabase.getInstance().getReference("Users/" + currentUser.getUid());
            mRef.child("onlineStats").setValue("1");
            mRef.child("onlineStats").onDisconnect().setValue("0");
        }



    }

    @Override
    protected void onResume() {
        super.onResume();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Users/" + currentUser.getUid());
            mRef.child("onlineStats").setValue("0");
        }
    }
}
