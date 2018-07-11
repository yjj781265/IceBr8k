package app.jayang.icebr8k.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import app.jayang.icebr8k.Adapter.QuestionAnsweredAdapter;
import app.jayang.icebr8k.Modle.UserQA;
import app.jayang.icebr8k.R;

public class QuestionAnswered_Fragment extends Fragment {
    private  View mView;
    private RelativeLayout loadingGif;
    private RecyclerView mRecyclerView;
    private ArrayList<UserQA> mList;
    private ArrayList<String> qIdList;
    private QuestionAnsweredAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_question_answered,container,false);
        loadingGif = mView.findViewById(R.id.loadingImg_questionTab);

        // init hashmap
        mList = new ArrayList<>();
        qIdList = new ArrayList<>();

        mRecyclerView = mView.findViewById(R.id.recyclerView_questionTab);


        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        // specify an adapter (see also next example)
        mAdapter = new QuestionAnsweredAdapter(mList,mView.getContext());
        mRecyclerView.setAdapter(mAdapter);

        getQuestionData();

        return mView;
    }


// retrieve user's question data from Firebase
    void getQuestionData(){
        FirebaseUser currentuser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference questionRef = FirebaseDatabase .getInstance().getReference()
                .child("UserQA")
                .child(currentuser.getUid());
        questionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot childSnap : dataSnapshot.getChildren()){
                    UserQA userQA = childSnap.getValue(UserQA.class);

                    if(!qIdList.contains(userQA.getQuestionId())){
                        mList.add(userQA);
                        qIdList.add(userQA.getQuestionId());

                    }else{
                        for(UserQA temp : mList){
                            if(temp.getQuestionId().equals(userQA.getQuestionId())){
                               temp.setAnswer(userQA.getAnswer());
                            }
                        }


                    }
                }
                // sort in A-Z Order
                Collections.sort(mList);
                mAdapter.notifyDataSetChanged();
                loadingGif.setVisibility(View.GONE);
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
