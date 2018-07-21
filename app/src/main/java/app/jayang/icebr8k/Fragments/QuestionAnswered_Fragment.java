package app.jayang.icebr8k.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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
import app.jayang.icebr8k.SearchableActivity;

public class QuestionAnswered_Fragment extends Fragment {
    private View mView;
    private RelativeLayout loadingGif;
    private RecyclerView mRecyclerView;
    private ArrayList<UserQA> mList;
    private TextView searchView;
    private ArrayList<String> qIdList;
    private FrameLayout searchLayout;
    private QuestionAnsweredAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_question_answered, container, false);
        loadingGif = mView.findViewById(R.id.loadingImg_questionTab);

        // init hashmap
        mList = new ArrayList<>();
        qIdList = new ArrayList<>();

        mRecyclerView = mView.findViewById(R.id.recyclerView_questionTab);
        searchLayout = mView.findViewById(R.id.search_layout);
        searchView = mView.findViewById(R.id.searchview_question);
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), SearchableActivity.class);
                i.putExtra("questions", mList);
                getActivity().overridePendingTransition(0,0);
                startActivity(i);
            }
        });


        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        // specify an adapter (see also next example)
        mAdapter = new QuestionAnsweredAdapter(mList, mView.getContext());
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setAdapter(mAdapter);

        getQuestionData();

        return mView;
    }


    // retrieve user's question data from Firebase
    void getQuestionData() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("UserQA")
                .child(currentUser.getUid());
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final UserQA userQA = dataSnapshot.getValue(UserQA.class);
                final DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference()
                        .child("Comments").child(userQA.getQuestionId());

                commentRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Long numComments = dataSnapshot.getChildrenCount();
                        String num = numComments>0 ? String.valueOf(numComments) :"";
                        userQA.setNumComments(num);
                        if(qIdList.contains(userQA.getQuestionId())){
                            for(UserQA temp : mList){
                                if(temp.getQuestionId().equals(userQA.getQuestionId())){
                                    mList.set(mList.indexOf(temp),userQA);
                                    mAdapter.notifyItemChanged(mList.indexOf(temp));
                                }
                            }


                        }else{
                            mList.add(userQA);
                            qIdList.add(userQA.getQuestionId());
                            Collections.sort(mList);
                            mAdapter.notifyDataSetChanged();
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                UserQA userQA = dataSnapshot.getValue(UserQA.class);
                if(qIdList.contains(userQA.getQuestionId())){
                    for(UserQA temp : mList){
                        if(temp.getQuestionId().equals(userQA.getQuestionId())){
                            mList.set(mList.indexOf(temp),userQA);
                            mAdapter.notifyItemChanged(mList.indexOf(temp));
                        }
                    }


                }


            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               Long count = dataSnapshot.getChildrenCount();
               loadingGif.setVisibility(View.GONE);
               searchLayout.setVisibility(count!=null && count>0 ?View.VISIBLE:View.GONE );

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}

