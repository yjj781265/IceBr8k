package app.jayang.icebr8k.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import app.jayang.icebr8k.Adapter.ResultItemAdapter;
import app.jayang.icebr8k.Modle.ResultItem;
import app.jayang.icebr8k.Modle.User;
import app.jayang.icebr8k.Modle.UserQA;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.ResultActivity;
import app.jayang.icebr8k.Utility.ActivityCommunicator;
import app.jayang.icebr8k.Utility.Compatability;

/**
 * Created by LoLJay on 11/2/2017.
 */

public class diffFrag extends Fragment {
    View mview;
    ArrayList<ResultItem> mResultItems;
    ArrayList<UserQA> userdiff1,userdiff2;
    String user2Uid;
    private String childKey = "";
    SwipeRefreshLayout mSwipeRefreshLayout;
    User user2;
    RecyclerView recyclerView;
    ResultItemAdapter mAdapter;
    final ArrayList<UserQA> userQA1 = new ArrayList<>();
    final ArrayList<UserQA> userQA2 = new ArrayList<>();
    DatabaseReference mRef ;
    DatabaseReference mRef2;
    Boolean firstTime = true;
    private ActivityCommunicator mCommunicator;
    private DatabaseReference mUserQARef;
    private ChildEventListener mUserQARefChildListener;
    private HashMap<DatabaseReference,ValueEventListener> mListenerHashMap = new HashMap<>();

    public diffFrag() {
    }

    public static diffFrag newInstance(String user2Uid){
        diffFrag diffFrag = new diffFrag();
        Bundle args = new Bundle();
        args.putString("user2Uid", user2Uid);
        diffFrag.setArguments(args);
        return  diffFrag;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCommunicator = (ActivityCommunicator) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user2Uid = getArguments().getString("user2Uid");
            mRef = FirebaseDatabase.getInstance().getReference("UserQA/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
            mRef2 = FirebaseDatabase.getInstance().getReference("UserQA/" + user2Uid);
            }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mview = inflater.inflate(R.layout.diff_frag,container,false);
        mResultItems = new ArrayList<>();
        recyclerView = mview.findViewById(R.id.recyclerView_diff);
        LinearLayoutManager manager = new LinearLayoutManager(mview.getContext());
        recyclerView.setLayoutManager(manager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setVisibility(View.GONE);
        mAdapter = new ResultItemAdapter(mResultItems,getActivity());
        mAdapter.setHasStableIds(false);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.getItemAnimator().setChangeDuration(0);
        recyclerView.getItemAnimator().setAddDuration(0);
        mSwipeRefreshLayout = mview.findViewById(R.id.refreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
               mAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        loadData();
        if(firstTime){
            addUserQAListener();
            firstTime = false;
        }

        return  mview;
    }


    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    void loadData(){

        if(user2Uid!=null){
            mResultItems.clear();
            mAdapter.notifyDataSetChanged();
            userQA1.clear();
            userQA2.clear();


            //get comp score
            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot child : dataSnapshot.getChildren()){
                        if( !"skipped".equals(child.getValue(UserQA.class).getAnswer())){
                            userQA1.add(child.getValue(UserQA.class));
                        }

                    }
                    mRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for(DataSnapshot child : dataSnapshot.getChildren()){
                                if(!"skipped".equals(child.getValue(UserQA.class).getAnswer())){
                                    userQA2.add(child.getValue(UserQA.class));
                                }


                            }
                            Compatability mCompatability = new Compatability(userQA1,userQA2);
                            int score = mCompatability.getScore();

                           userdiff1 = mCompatability.getDiffList();
                           userdiff2 = mCompatability.getDiffList2();
                            Log.d("diff123",""+userdiff1.size() + " "+userdiff2.size() );

                           //set answer for user1
                           for(UserQA userQA : userdiff1){
                               for(UserQA userQA2 : userdiff2){
                                   if(userQA.getQuestionId().equals(userQA2.getQuestionId())){
                                       final ResultItem resultItem = new ResultItem();
                                       resultItem.setQuestionId(userQA.getQuestionId());
                                       resultItem.setQuesiton(userQA.getQuestion().trim());
                                       resultItem.setUser2Id(user2Uid);
                                       resultItem.setAnswer1(userQA.getAnswer());
                                       resultItem.setAnswer2(userQA2.getAnswer());
                                       mResultItems.add(resultItem);
                                       if(mResultItems.size() == userdiff2.size()){
                                           mSwipeRefreshLayout.setRefreshing(false);
                                           Collections.sort(mResultItems);
                                           recyclerView.setVisibility(View.VISIBLE);
                                           mAdapter.notifyDataSetChanged();
                                           mCommunicator.passDataToActivity(mResultItems,"diff");



                                       }

                                       // add comment listener
                                       DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference()
                                               .child("Comments")
                                               .child(resultItem.getQuestionId()) ;

                                       ValueEventListener commentRefListener = new ValueEventListener() {
                                           @Override
                                           public void onDataChange(DataSnapshot dataSnapshot) {
                                               Long count = dataSnapshot.getChildrenCount();
                                               resultItem.setComment( count!=null && count>0 ? ""+count :"");
                                               if(mResultItems.contains(resultItem)){
                                                   mAdapter.notifyItemChanged(mResultItems.indexOf(resultItem));
                                               }
                                           }

                                           @Override
                                           public void onCancelled(DatabaseError databaseError) {

                                           }
                                       };
                                       commentRef.addValueEventListener(commentRefListener);
                                       mListenerHashMap.put(commentRef,commentRefListener);
                                   }
                               }

                           }
                            //set answer for user2
                            if(mResultItems.isEmpty()){
                                mSwipeRefreshLayout.setRefreshing(false);

                            }

                            }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });




        }
    }

    public static boolean isInteger(String s) {
        return isInteger(s,10);
    }

    public static boolean isInteger(String s, int radix) {
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i),radix) < 0) return false;
        }
        return true;
    }

    public void addUserQAListener() {
        mUserQARef= FirebaseDatabase.getInstance().getReference("UserQA/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
        mUserQARefChildListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d("result123",""+dataSnapshot);
                if(!dataSnapshot.getKey().equals(childKey) ){
                    loadData();
                    childKey = dataSnapshot.getKey();
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
        };
        mUserQARef.addChildEventListener(mUserQARefChildListener);

    }

    @Override
    public void onDestroy() {
        try {
            mUserQARef.removeEventListener(mUserQARefChildListener);
            for (DatabaseReference databaseReference : mListenerHashMap.keySet()) {
                databaseReference.removeEventListener(mListenerHashMap.get(databaseReference));
            }
        }catch (NullPointerException e){
            Log.d("result123",e.getMessage());
        }
        Log.d("result123",mListenerHashMap.size()+"");
        super.onDestroy();
    }
}
