package app.jayang.icebr8k.Model;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by yjj781265 on 1/3/2018.
 */

public class myViewPager extends ViewPager  {
    private boolean swipeable;
    public myViewPager(Context context) {
        super(context);
    }


    public myViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.swipeable = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        return false;
    }

    @Override
    public int getCurrentItem() {
        return super.getCurrentItem();
    }

    public void setSwipeable(boolean swipeable) {
        this.swipeable = swipeable;
    }
}


