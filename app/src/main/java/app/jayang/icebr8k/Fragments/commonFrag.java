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
import app.jayang.icebr8k.Model.ResultItem;
import app.jayang.icebr8k.Model.UserQA;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.Utility.ActivityCommunicator;
import app.jayang.icebr8k.Utility.Compatability;

/**
 * Created by yjj781265 on 10/30/2017.
 */

public class commonFrag extends Fragment {

    private final static String SAME_ANSWER = "sameAnswer";
    private final static String USER2_UID ="user2Uid";

    final ArrayList<UserQA> userQA1 = new ArrayList<>();
    final ArrayList<UserQA> userQA2 = new ArrayList<>();
    private ActivityCommunicator mCommunicator;
    private String childKey = "";
    private HashMap<DatabaseReference,ValueEventListener> mListenerHashMap = new HashMap<>();
    DatabaseReference mRef ;
    DatabaseReference mRef2;

    Compatability mCompatability;
    ResultItemAdapter mAdapter;
    Boolean firstTime = true;

    View mView;
    ArrayList<UserQA> commonArrList;
    ArrayList<ResultItem> mResultItems;
    RecyclerView mRecyclerView_common;
    SwipeRefreshLayout mRefreshLayout;
    String user2Uid;
    private DatabaseReference mUserQARef;
    private ChildEventListener mUserQARefChildListener;

    public commonFrag() {


    }
    public static commonFrag newInstance(String user2uid){
        commonFrag commonFrag = new commonFrag();
        Bundle args = new Bundle();
        args.putString(USER2_UID, user2uid);
        commonFrag.setArguments(args);
        return commonFrag;
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
            user2Uid = getArguments().getString(USER2_UID);
            mRef = FirebaseDatabase.getInstance().getReference("UserQA/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
            mRef2 = FirebaseDatabase.getInstance().getReference("UserQA/" + user2Uid);
            }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mResultItems = new ArrayList<>();
        mView = inflater.inflate(R.layout.common_frag, container, false);
        mRecyclerView_common = mView.findViewById(R.id.recyclerView_common);
        LinearLayoutManager manager = new LinearLayoutManager(mView.getContext(),LinearLayoutManager.VERTICAL,false);
        mRecyclerView_common.setLayoutManager(manager);
        mAdapter = new ResultItemAdapter(mResultItems,getActivity());
        mAdapter.setHasStableIds(false);
        mRecyclerView_common.setAdapter(mAdapter);
        mRecyclerView_common.setHasFixedSize(true);
        mRecyclerView_common.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView_common.getItemAnimator().setChangeDuration(0);
        mRefreshLayout = mView.findViewById(R.id.refreshLayout);
        mRecyclerView_common.setVisibility(View.GONE);

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mAdapter.notifyDataSetChanged();
               mRefreshLayout.setRefreshing(false);
            }
        });
        loadData();


        return mView;

    }



    @Override
    public void onStart() {
        super.onStart();



    }

    @Override
    public void onResume() {
        super.onResume();



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



    void loadData(){
        if(user2Uid!=null){
            Log.d("result123","user2uid not null");
            mResultItems.clear();
            mAdapter.notifyDataSetChanged();
            commonArrList = new ArrayList<>();
            userQA1.clear();
            userQA2.clear();
            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        if (!"skipped".equals(child.getValue(UserQA.class).getAnswer())) {
                            userQA1.add(child.getValue(UserQA.class));
                        }

                    }


                    mRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot child : dataSnapshot.getChildren()) {
                                if (!"skipped".equals(child.getValue(UserQA.class).getAnswer())) {
                                    userQA2.add(child.getValue(UserQA.class));
                                }
                            }

                            mCompatability = new Compatability(userQA1, userQA2);
                            int score = mCompatability.getScore();

                            if(mCompatability!=null){
                                Log.d("result123","mcomp not null");
                                commonArrList = mCompatability.getCommonList();
                                if(commonArrList.isEmpty()){
                                    mRefreshLayout.setRefreshing(false);
                                }

                                Log.d("result123","size"+ mCompatability.getCommonList().size());
                                for(final UserQA userQA : commonArrList){

                                    /// if is a scale question
                                    if(isInteger(userQA.getAnswer())){
                                        DatabaseReference useQA1Ref = FirebaseDatabase.getInstance()
                                                .getReference().child("UserQA").child(user2Uid).child(userQA.getQuestionId());
                                        useQA1Ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.getValue()!=null){
                                                    final ResultItem resultItem = new ResultItem();
                                                    resultItem.setQuestionId(userQA.getQuestionId());
                                                    resultItem.setAnswer2(dataSnapshot.child("answer").getValue(String.class));
                                                    DatabaseReference useQA2Ref = FirebaseDatabase.getInstance()
                                                            .getReference().child("UserQA").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(userQA.getQuestionId());
                                                    useQA2Ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            if(dataSnapshot.getValue()!=null){
                                                                resultItem.setAnswer1(dataSnapshot.child("answer").getValue (String.class));
                                                                resultItem.setUser2Id(user2Uid);
                                                                resultItem.setQuesiton(userQA.getQuestion().trim());
                                                                mResultItems.add(resultItem);
                                                                Log.d("result123","added scale ");
                                                                if(mResultItems.size() == commonArrList.size()){
                                                                    mRefreshLayout.setRefreshing(false);
                                                                    Collections.sort(mResultItems);
                                                                    mCommunicator.passDataToActivity(mResultItems,"common");
                                                                    mAdapter.notifyDataSetChanged();
                                                                    mRecyclerView_common.setVisibility(View.VISIBLE);
                                                                    if(firstTime){
                                                                        addUserQAListener();
                                                                        firstTime = false;
                                                                    }


                                                                    }

                                                                //add comment listener
                                                                DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference()
                                                                        .child("Comments")
                                                                        .child(resultItem.getQuestionId()) ;
                                                        ValueEventListener  commentRefListener = new ValueEventListener() {
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
                                                        //store value listener
                                                        mListenerHashMap.put(commentRef,commentRefListener);

                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }else{
                                        final ResultItem resultItem = new ResultItem(userQA.getQuestionId(),userQA.getQuestion().trim(),userQA.getAnswer(),userQA.getAnswer(),"",user2Uid);
                                        mResultItems.add(resultItem);
                                        Log.d("result123","added ");

                                        if(mResultItems.size() == commonArrList.size()){
                                            mRefreshLayout.setRefreshing(false);
                                            Collections.sort(mResultItems);
                                            mCommunicator.passDataToActivity(mResultItems,"common");
                                            mAdapter.notifyDataSetChanged();
                                            mRecyclerView_common.setVisibility(View.VISIBLE);
                                        }


                                        DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference()
                                                .child("Comments")
                                                .child(resultItem.getQuestionId()) ;
                                     ValueEventListener   commentRefListener = new ValueEventListener() {
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

                                if(mResultItems.isEmpty()){
                                    mRefreshLayout.setRefreshing(false);
                                }
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


    public void addUserQAListener() {
        mUserQARef = FirebaseDatabase.getInstance().getReference("UserQA/" + FirebaseAuth.getInstance().getCurrentUser().getUid());
        mUserQARefChildListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if(!dataSnapshot.getKey().equals(childKey)){
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
