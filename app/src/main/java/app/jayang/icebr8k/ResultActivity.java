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
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;

import app.jayang.icebr8k.Adapter.ViewPagerAdapter;
import app.jayang.icebr8k.Fragments.commonFrag;
import app.jayang.icebr8k.Fragments.diffFrag;
import app.jayang.icebr8k.Modle.ResultItem;
import app.jayang.icebr8k.Modle.User;
import app.jayang.icebr8k.Modle.UserQA;
import app.jayang.icebr8k.Utility.ActivityCommunicator;
import app.jayang.icebr8k.Utility.Compatability;

public class ResultActivity extends AppCompatActivity implements ActivityCommunicator {
    TabLayout mLayout;
    ViewPager mViewPager;
    Toolbar mToolbar;
    TextView mTextView;
    FirebaseUser currentUser;
    FrameLayout searchLayout;
    User user2;
    ImageView user2Icon;
    DatabaseReference mRef, userQARef, mRef2;


    String user2Id;
    private long lastClickTime = 0;
    private ArrayList<ResultItem> commonItems, diffItems, mResultItems;
    private ValueEventListener userQAListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();


        mLayout = findViewById(R.id.tabs_result);
        mToolbar = findViewById(R.id.toolbar_result);
        user2Icon = findViewById(R.id.user2_icon);
        searchLayout = findViewById(R.id.search_layout);


        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("");

        mViewPager = findViewById(R.id.viewpager_result);

        user2Id = getIntent().getExtras().getString("user2Id");
        user2 = (User) getIntent().getExtras().getSerializable("user2");
        mTextView = findViewById(R.id.result_comp);
        // set score top
        if (user2Id != null) {
            userQAListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    compareWithUser2();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            userQARef = FirebaseDatabase.getInstance().getReference("UserQA/" + currentUser.getUid());
            userQARef.addValueEventListener(userQAListener);



        }

        // init arraylist
        diffItems = new ArrayList<>();
        commonItems = new ArrayList<>();
        mResultItems = new ArrayList<>();


//user2 avatar on toolbar
        Glide.with(getBaseContext()).load(user2.getPhotourl()).transition(DrawableTransitionOptions.withCrossFade(300))
                .apply(RequestOptions.circleCropTransform()).into(user2Icon);


        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());


        viewPagerAdapter.addFragment(commonFrag.newInstance(user2Id));
        viewPagerAdapter.addFragment(diffFrag.newInstance(user2Id));


        mViewPager.setAdapter(viewPagerAdapter);
        mLayout.setupWithViewPager(mViewPager);

        mLayout.getTabAt(0).setIcon(R.drawable.check_mark);
        mLayout.getTabAt(1).setIcon(R.drawable.axe_mark);

        searchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(ResultActivity.this, ""+ commonItems.size(), Toast.LENGTH_SHORT).show();
                mResultItems.clear();
                mResultItems.addAll(commonItems);
                mResultItems.addAll(diffItems);

                Intent intent = new Intent(ResultActivity.this, SearchResult.class);
                intent.putExtra("resultList", mResultItems);
                startActivity(intent);

            }
        });

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
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return false;
        }
        lastClickTime = SystemClock.elapsedRealtime();

        if (id == R.id.mybutton) {
            Intent mIntent = new Intent(getBaseContext(), UserChatActvity.class);
            mIntent.putExtra("chatName", user2.getDisplayname());
            mIntent.putExtra("chatId", user2Id);
            startActivity(mIntent);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            overridePendingTransition(R.anim.slide_from_right, android.R.anim.fade_out);

        }


        return super.onOptionsItemSelected(item);
    }

    public void compareWithUser2() {
        final ArrayList<UserQA> userQA1 = new ArrayList<>();
        final ArrayList<UserQA> userQA2 = new ArrayList<>();
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("UserQA/" + currentUser.getUid());
        final DatabaseReference mRef2 = FirebaseDatabase.getInstance().getReference("UserQA/" + user2Id);

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
                        int score = mCompatability.getScore();
                        mTextView.setText(score + "%");


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


    @Override
    public boolean onSupportNavigateUp() {
        super.onSupportNavigateUp();
        finish();
        // overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        //overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }


    @Override
    public void passDataToActivity(Object o, String tag) {
        if (o instanceof ArrayList) {
            if (tag != null && tag.equals("common")) {
                commonItems.clear();
                commonItems.addAll((Collection<? extends ResultItem>) o);
            } else if (tag != null && tag.equals("diff")) {
                diffItems.clear();
                diffItems.addAll((Collection<? extends ResultItem>) o);
            }
        }
    }

    @Override
    protected void onDestroy() {
        userQARef.removeEventListener(userQAListener);
        super.onDestroy();
    }
}
