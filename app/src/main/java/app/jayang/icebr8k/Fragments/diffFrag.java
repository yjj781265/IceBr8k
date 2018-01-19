package app.jayang.icebr8k.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;

import app.jayang.icebr8k.Modle.User;
import app.jayang.icebr8k.Modle.UserQA;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.RecyclerAdapterDiff;
import app.jayang.icebr8k.ResultActivity;

/**
 * Created by LoLJay on 11/2/2017.
 */

public class diffFrag extends Fragment {
    View mview;
    ArrayList<UserQA> user1QA,user2QA;
    User user2;
    RecyclerView recyclerView;

    public diffFrag() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        user1QA= ((ResultActivity)getActivity()).getDiffAnswer1();
        user2QA = ((ResultActivity)getActivity()).getDiffAnswer2();




        user2=((ResultActivity)getActivity()).getUser2();
        mview = inflater.inflate(R.layout.diff_frag,container,false);
        recyclerView = mview.findViewById(R.id.recyclerView_diff);
        LinearLayoutManager manager = new LinearLayoutManager(mview.getContext());
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(false);
        Collections.sort(user1QA);
        Collections.sort(user2QA);
        recyclerView.setAdapter(new RecyclerAdapterDiff(user1QA,user2QA,user2,mview.getContext()));
        return  mview;
    }
}
