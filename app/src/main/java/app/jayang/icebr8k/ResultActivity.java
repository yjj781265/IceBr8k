package app.jayang.icebr8k;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TableLayout;

import java.util.ArrayList;
import java.util.HashMap;

public class ResultActivity extends AppCompatActivity {
    TabLayout mLayout ;
    ViewPager mViewPager;
    Toolbar mToolbar;
    User user2;
    ArrayList<UserQA> mArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        mLayout = findViewById(R.id.tabs_result);
        mToolbar = findViewById(R.id.toolbar_result);


        mViewPager =findViewById(R.id.viewpager_result);

        mArrayList = getIntent().getParcelableArrayListExtra("sameAnswer");
        Log.d("mapArr",mArrayList.toString());
        user2 = (User)getIntent().getSerializableExtra("user2");
        Log.d("mapArr",user2.getDisplayname());



        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());


        viewPagerAdapter.addFragment(new commonFrag());


        mViewPager.setAdapter(viewPagerAdapter);
        mLayout.setupWithViewPager(mViewPager);
        mLayout.getTabAt(0).setIcon(R.drawable.check_mark);
       // mLayout.getTabAt(1).setIcon(R.drawable.axe_mark);



    }

    public User getUser2() {
        return user2;
    }

    public ArrayList<UserQA> getArrayList() {
        return mArrayList;
    }
}
