package app.jayang.icebr8k;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by yjj781265 on 10/30/2017.
 */

public class commonFrag extends Fragment {

    View mView;
    TextView mTextView;
    Handler mHandler;
    Runnable runnable;
    ArrayList<UserQA> mArrayList;
    RecyclerView mRecyclerView_common;
    User user2;

    public commonFrag() {


    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mArrayList = new ArrayList<>();
        mArrayList= ((ResultActivity)getActivity()).getArrayList();
        user2=((ResultActivity)getActivity()).getUser2();
            Log.d("mapArr2",mArrayList.toString());






        mView = inflater.inflate(R.layout.common_frag,container,false);
        mRecyclerView_common =mView.findViewById(R.id.recyclerView_common);
        LinearLayoutManager manager = new LinearLayoutManager(mView.getContext());
        mRecyclerView_common.setLayoutManager(manager);
        mRecyclerView_common.setHasFixedSize(false);
        mRecyclerView_common.setAdapter(new RecyclerAdapterCommon(mArrayList,mView.getContext(),user2));
        return mView;

    }

    @Override
    public void onStart() {
        super.onStart();
         mHandler = new Handler();



       /* runnable = new Runnable() {
            @Override
            public void run() {
      *//* do what you need to do *//*
                YoYo.with(Techniques.FadeInLeft)
                        .duration(500)
                        .repeat(0)
                        .playOn(mView);


                mTextView.setText("Do you like sport ?");
                mView.setClickable(true);

      *//* and here comes the "trick" *//*
                mHandler.postDelayed(this, 1000);
                mHandler.removeCallbacks(this);


            }
        };
         mHandler.postDelayed(runnable,2000);*/



    }
}
