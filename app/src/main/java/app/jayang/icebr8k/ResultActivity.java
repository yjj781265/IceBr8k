package app.jayang.icebr8k;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TableLayout;

public class ResultActivity extends AppCompatActivity {
    TabLayout mLayout ;
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        mLayout = findViewById(R.id.tablayout_result);

        mViewPager =findViewById(R.id.viewpager_result);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewPagerAdapter.addFragment(new commonFrag());
        viewPagerAdapter.addFragment(new commonFrag());

        mViewPager.setAdapter(viewPagerAdapter);
        mLayout.setupWithViewPager(mViewPager);
        mLayout.getTabAt(0).setIcon(R.drawable.check_mark);
        mLayout.getTabAt(1).setIcon(R.drawable.axe_mark);
    }
}
