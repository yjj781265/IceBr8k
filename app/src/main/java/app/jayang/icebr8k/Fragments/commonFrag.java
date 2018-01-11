package app.jayang.icebr8k.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import app.jayang.icebr8k.Modle.User;
import app.jayang.icebr8k.Modle.UserQA;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.RecyclerAdapterCommon;
import app.jayang.icebr8k.ResultActivity;

/**
 * Created by yjj781265 on 10/30/2017.
 */

public class commonFrag extends Fragment {

    View mView;
    ArrayList<UserQA> mArrayList, commonArrList;
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
        commonArrList = new ArrayList<>();
        mArrayList = ((ResultActivity) getActivity()).getArrayList();

        for(UserQA userQA: mArrayList){
            if(!userQA.getAnswer().equals(getString(R.string.question_skip))){
                commonArrList.add(userQA);
            }
        }
        user2 = ((ResultActivity) getActivity()).getUser2();
        Log.d("mapArr2", mArrayList.toString());


        mView = inflater.inflate(R.layout.common_frag, container, false);
        mRecyclerView_common = mView.findViewById(R.id.recyclerView_common);
        LinearLayoutManager manager = new LinearLayoutManager(mView.getContext());
        mRecyclerView_common.setLayoutManager(manager);
        mRecyclerView_common.setHasFixedSize(false);
        Collections.sort(commonArrList);
        mRecyclerView_common.setAdapter(new RecyclerAdapterCommon(commonArrList, mView.getContext(), user2));
        return mView;

    }

    @Override
    public void onStart() {
        super.onStart();


    }
}
