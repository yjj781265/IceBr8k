package app.jayang.icebr8k.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import app.jayang.icebr8k.Adapter.LeaderboardAdapter;
import app.jayang.icebr8k.Modle.LeaderboardDialog;
import app.jayang.icebr8k.Modle.User;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.Utility.ActivityCommunicator;

public class Leaderboard_Fragment_friends extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private View mView;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LinearLayoutManager mLayoutManager;
    private ArrayList<LeaderboardDialog> mLeaderboardDialogs;
    private LeaderboardAdapter mAdapter;
    private ActivityCommunicator mActivityCommunicator;
    private int COUNT;
    private final String Tag = "Leaderboard_frag_friend";

    // Firebase Variables
    private FirebaseUser currentUser;
    private FirebaseDatabase mDatabase;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivityCommunicator =(ActivityCommunicator)context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView =  inflater.inflate(R.layout.fragment_leaderboard_friends, container, false);
        mLeaderboardDialogs = new ArrayList<>();
        mSwipeRefreshLayout = mView.findViewById(R.id.leaderboard_friends_swipe);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        // Firebase init
        mDatabase = FirebaseDatabase.getInstance();

        // Firebase user init
        currentUser = FirebaseAuth.getInstance().getCurrentUser();



        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView = mView.findViewById(R.id.leaderboard_friends_recyclerview);
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter
        mAdapter = new LeaderboardAdapter(mLeaderboardDialogs,getActivity());
        mRecyclerView.setAdapter(mAdapter);

        loadData();
        return mView;
    }

    public void loadData(){
        COUNT =1;
        mLeaderboardDialogs.clear();
        mSwipeRefreshLayout.setRefreshing(true);
        DatabaseReference mRef = mDatabase.getReference()
                .child("UserFriends")
                .child(currentUser.getUid());

        // retrieve all user friends
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // add myfriends in to rank calculation
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if("accepted".equals(snapshot.child("stats").getValue(String.class))){
                        Log.d("Leaderboard_frag_friend",snapshot.getKey());
                        COUNT++;
                        LeaderboardDialog dialog = new LeaderboardDialog();
                        dialog.setId(snapshot.getKey());
                        getUser(dialog);
                    }
                }
                // add myself in to rank calculation
                LeaderboardDialog dialog = new LeaderboardDialog();
                dialog.setId(currentUser.getUid());
                getUser(dialog);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
     //get User's basic info
    private void getUser(final LeaderboardDialog dialog){
        DatabaseReference mRef = mDatabase.getReference()
                .child("Users")
                .child(dialog.getId());
        mRef .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                dialog.setUser(user);
                Log.d(Tag,user.getDisplayname() + "\n"+user.getPhotourl());
                getQsum(dialog);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //get question counts
    private void getQsum(final LeaderboardDialog dialog){
        DatabaseReference mRef = mDatabase.getReference()
                .child("UserQA")
                .child(dialog.getId());

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Long count = dataSnapshot.getChildrenCount();
                Log.d(Tag, ""+count + " "+dialog.getUser().getDisplayname());
                dialog.setQuestionSum(count);
                mLeaderboardDialogs.add(dialog);
                Log.d(Tag,""+COUNT + " "+ mLeaderboardDialogs.size());

                if(mLeaderboardDialogs.size() == COUNT){
                    Collections.sort(mLeaderboardDialogs);
                    for(LeaderboardDialog dialog1: mLeaderboardDialogs){
                        dialog1.setRank(String.valueOf(mLeaderboardDialogs.indexOf(dialog1) +1));
                    }
                    Collections.sort(mLeaderboardDialogs);
                    //notify adapter
                    mAdapter.notifyDataSetChanged();

                    // pass my data to the parent activity
                    for(LeaderboardDialog dialog1: mLeaderboardDialogs){
                        if(dialog1.getId().equals(currentUser.getUid())){
                            mActivityCommunicator.passDataToActivity(dialog1,"1");
                        }
                    }
                    mSwipeRefreshLayout.setRefreshing(false);
                }






            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onRefresh() {
        Log.i(Tag, "onRefresh called from SwipeRefreshLayout");
        loadData();
    }
}
