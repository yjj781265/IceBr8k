package app.jayang.icebr8k;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.HashMap;

import app.jayang.icebr8k.Adapter.ViewPagerAdapter;
import app.jayang.icebr8k.Fragments.Leaderboard_Fragment_friends;
import app.jayang.icebr8k.Fragments.Leaderboard_Fragment_global;

import app.jayang.icebr8k.Model.LeaderboardDialog;
import app.jayang.icebr8k.Utility.ActivityCommunicator;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class Leaderboard extends SwipeBackActivity implements ActivityCommunicator {
    private ViewPager mViewpager;
    private TabLayout mTabLayout;
    private TextView rank,qSum;
    private ImageView avatar;
    private SwipeBackLayout mSwipeBackLayout;
    private HashMap<String,LeaderboardDialog> mDialogHashMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);
        mDialogHashMap = new HashMap<>();
        mViewpager = (ViewPager) findViewById(R.id.leaderboard_viewpager);
        mTabLayout = (TabLayout) findViewById(R.id.leaderboard_tab);
        mSwipeBackLayout = getSwipeBackLayout();
        mSwipeBackLayout.setEdgeSize(40);
        rank = (TextView) findViewById(R.id.leaderboard_myrank);
        qSum = (TextView) findViewById(R.id.leaderboard_qnum);
        avatar = (ImageView) findViewById(R.id.leaderboard_avatar);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());


        viewPagerAdapter.addFragment(new Leaderboard_Fragment_friends());
        viewPagerAdapter.addFragment(new Leaderboard_Fragment_global());

        // connect tablayout with view pager
        mViewpager.setAdapter(viewPagerAdapter);
        mViewpager.setOffscreenPageLimit(1);
        mTabLayout.setupWithViewPager(mViewpager);
        mTabLayout.getTabAt(0).setText("Friends");
        mTabLayout.getTabAt(1).setText("All");


        mViewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                LeaderboardDialog dialog = mDialogHashMap.get(String.valueOf(position+1));
                if(dialog!=null){
                    rank.setText(dialog.getRank());
                    qSum.setText(dialog.getQuestionSum().toString());
                }else{
                    rank.setText("--");
                    qSum.setText("--");
                }

                if(position ==0){
                    YoYo.with(Techniques.BounceInLeft).playOn(rank);
                }else if(position ==1){
                    YoYo.with(Techniques.BounceInRight).playOn(rank);
                }


            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void passDataToActivity(Object o,String tag) {
        if(o instanceof LeaderboardDialog && "1".equals(tag) &&mViewpager.getCurrentItem() ==0){
            rank.setText(((LeaderboardDialog) o).getRank());
            qSum.setText(((LeaderboardDialog) o).getQuestionSum().toString());
            Glide.with(getApplicationContext()).load(((LeaderboardDialog) o).getUser().getPhotourl()).
                    apply(RequestOptions.circleCropTransform().placeholder(R.drawable.default_avatar3)).into(avatar);
            mDialogHashMap.put("1", (LeaderboardDialog) o);
        }else if (o instanceof LeaderboardDialog && "2".equals(tag)){
            mDialogHashMap.put("2", (LeaderboardDialog) o);
            if(mViewpager.getCurrentItem() ==1){
                rank.setText(((LeaderboardDialog) o).getRank());
                qSum.setText(((LeaderboardDialog) o).getQuestionSum().toString());
                Glide.with(getApplicationContext()).load(((LeaderboardDialog) o).getUser().getPhotourl()).
                        apply(RequestOptions.circleCropTransform().placeholder(R.drawable.default_avatar3)).into(avatar);
            }
        }
    }
}
