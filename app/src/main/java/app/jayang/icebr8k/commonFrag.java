package app.jayang.icebr8k;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

/**
 * Created by yjj781265 on 10/30/2017.
 */

public class commonFrag extends Fragment {

    View mView;
    TextView mTextView;
    Handler mHandler;
    Runnable runnable;

    public commonFrag() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.common_frag,container,false);
        mTextView = mView.findViewById(R.id.commonFrag_text);
        return mView;

    }

    @Override
    public void onStart() {
        super.onStart();
         mHandler = new Handler();



        runnable = new Runnable() {
            @Override
            public void run() {
      /* do what you need to do */
                YoYo.with(Techniques.FadeInLeft)
                        .duration(500)
                        .repeat(0)
                        .playOn(mView);


                mTextView.setText("Do you like sport ?");
                mView.setClickable(true);

      /* and here comes the "trick" */
                mHandler.postDelayed(this, 1000);
                mHandler.removeCallbacks(this);


            }
        };


        mView.setOnClickListener(new View.OnClickListener() {
            int i =0;
            @Override
            public void onClick(View view) {

                YoYo.with(Techniques.FadeInRight)
                        .duration(500)
                        .repeat(0)
                        .playOn(mView);
                    mView.setClickable(false);


                    mTextView.setText("You both answered : Yes");


                    mHandler.postDelayed(runnable,2000);





            }
        });
    }
}
