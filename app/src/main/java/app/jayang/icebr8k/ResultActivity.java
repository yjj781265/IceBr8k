package app.jayang.icebr8k;

import android.content.Intent;
import android.os.SystemClock;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import app.jayang.icebr8k.Adapter.ViewPagerAdapter;
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
    String user2Id ;
    private long lastClickTime = 0;

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
        getSupportActionBar().setTitle("");

        mViewPager = findViewById(R.id.viewpager_result);

        mArrayList = getIntent().getParcelableArrayListExtra("sameAnswer");
        Log.d("mapArr", mArrayList.toString());
        user2 = (User) getIntent().getSerializableExtra("user2");
        Log.d("mapArr", user2.getDisplayname());
        user2Id = getIntent().getExtras().getString("user2Id");
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
    //create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.result_menu, menu);


        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // preventing double, using threshold of 1000 ms
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
            return false;
        }
        lastClickTime = SystemClock.elapsedRealtime();

        if (id == R.id.mybutton) {
            Intent mIntent = new Intent(getBaseContext(),UserChatActvity.class);
                    mIntent.putExtra("chatName",user2.getDisplayname());
                    mIntent.putExtra("chatId",user2Id);
                    startActivity(mIntent);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    overridePendingTransition(R.anim.slide_from_right,android.R.anim.fade_out);

                }



        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onSupportNavigateUp() {
        super.onSupportNavigateUp();
        finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
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
