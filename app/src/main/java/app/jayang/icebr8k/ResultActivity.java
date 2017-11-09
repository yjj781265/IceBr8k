package app.jayang.icebr8k;

import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import app.jayang.icebr8k.Fragments.commonFrag;
import app.jayang.icebr8k.Fragments.diffFrag;
import app.jayang.icebr8k.Modle.User;
import app.jayang.icebr8k.Modle.UserQA;

public class ResultActivity extends AppCompatActivity {
    TabLayout mLayout;
    ViewPager mViewPager;
    Toolbar mToolbar;
    User user2;
    ImageView user2Icon;
    ArrayList<UserQA> mArrayList, diffAnswer1, diffAnswer2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);


        mLayout = findViewById(R.id.tabs_result);
        mToolbar = findViewById(R.id.toolbar_result);
        user2Icon = findViewById(R.id.user2_icon);


        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mViewPager = findViewById(R.id.viewpager_result);

        mArrayList = getIntent().getParcelableArrayListExtra("sameAnswer");
        Log.d("mapArr", mArrayList.toString());
        user2 = (User) getIntent().getSerializableExtra("user2");
        Log.d("mapArr", user2.getDisplayname());
        diffAnswer1 = getIntent().getParcelableArrayListExtra("diffAnswer1");
        Log.d("diff", diffAnswer1.toString());
        diffAnswer2 = getIntent().getParcelableArrayListExtra("diffAnswer2");
        Log.d("diff", diffAnswer2.toString());
//user2 avatar on toolbar
        Glide.with(getBaseContext()).load(user2.getPhotourl()).
                apply(RequestOptions.circleCropTransform()).into(user2Icon);


        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());


        viewPagerAdapter.addFragment(new commonFrag());
        viewPagerAdapter.addFragment(new diffFrag());


        mViewPager.setAdapter(viewPagerAdapter);
        mLayout.setupWithViewPager(mViewPager);
        mLayout.getTabAt(0).setIcon(R.drawable.check_mark);
        mLayout.getTabAt(1).setIcon(R.drawable.axe_mark);


    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public User getUser2() {
        return user2;
    }

    public ArrayList<UserQA> getArrayList() {
        return mArrayList;
    }

    public ArrayList<UserQA> getDiffAnswer1() {
        return diffAnswer1;
    }

    public ArrayList<UserQA> getDiffAnswer2() {
        return diffAnswer2;
    }
}
