package app.jayang.icebr8k;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.github.florent37.bubbletab.BubbleTab;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Homepage extends AppCompatActivity {
    BubbleTab bubbleTab;
    ViewPager viewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        bubbleTab = findViewById(R.id.bubbleTab);

        viewPager =findViewById(R.id.homepage_viewpager);
        ViewPagerAdapter vIewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());


        vIewPagerAdapter.addFragment(new Userstab_Fragment());

        viewPager.setAdapter(vIewPagerAdapter);
        bubbleTab.setupWithViewPager(viewPager);



    }


}
