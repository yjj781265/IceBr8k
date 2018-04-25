package app.jayang.icebr8k.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import app.jayang.icebr8k.Modle.User;
import app.jayang.icebr8k.Modle.UserDialog;
import app.jayang.icebr8k.Modle.UserMessage;
import app.jayang.icebr8k.Modle.UserQA;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.Adapter.RecyclerAdapter;
import app.jayang.icebr8k.SearchName;
import app.jayang.icebr8k.SearchUser;
import app.jayang.icebr8k.Utility.MyDateFormatter;


/**
 * Created by LoLJay on 10/20/2017.
 */

public class Userstab_Fragment extends Fragment  {
    View view;
    String TAG ="UserFrag";
    AppBarLayout mAppBarLayout;
    FirebaseDatabase mDatabase;
    DatabaseReference databaseReference;
    ArrayList<UserDialog> mUserDialogArrayList;
    RelativeLayout loadingGif;
    RecyclerView mRecyclerView;
    FirebaseUser currentUser;
    RecyclerAdapter mAdapter;
    Integer friendCount,counter;
    TextView mSearchView;
    Button filter_btn;
    BootstrapButton addFriend;
    private BroadcastReceiver tickReceiver;
    private SharedPreferences sharedPref;
    private Boolean sortByScore,done;
    private String sortByScoreStr;
    private long lastClickTime = 0;



    private static final Comparator<UserDialog>ONLINESTATS = new Comparator<UserDialog>() {
        @Override
        public int compare(UserDialog dialog, UserDialog t1) {
            int result =0;
            if(t1.getOnlinestats()!=null && dialog.getOnlinestats()!=null) {
                result = Integer.valueOf(t1.getOnlinestats()).
                        compareTo(Integer.valueOf(dialog.getOnlinestats()));
            }
            if(result==0){
                result =dialog.getName().compareTo(t1.getName());
            }
            return  result;
        }
    };

    private static final Comparator<UserDialog> SCORE = new Comparator<UserDialog>() {
        @Override
        public int compare(UserDialog dialog, UserDialog t1) {
            int result =0;
            if(t1.getScore()!=null &&dialog.getScore()!=null){
            result= Integer.valueOf(t1.getScore()).
                        compareTo( Integer.valueOf(dialog.getScore()));
            }

          if(result==0){
              if(t1.getName()!=null &&dialog.getName()!=null){
                  result =dialog.getName() .compareTo(t1.getName());
              }

          }
          return result;
        };
    };
    public Userstab_Fragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance();
        mUserDialogArrayList = new ArrayList<>();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        done =false;
        friendCount=0;
        counter=0;
        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        sortByScoreStr= sharedPref.getString("sort",null);
        if(sortByScoreStr==null){
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("sort", "yes");
            editor.commit();
            sortByScore =true;
        }else if(sortByScoreStr.equals("no")){
            sortByScore =false;
        }else{
            sortByScore =true;
        }

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.users_tab, container, false);
        mRecyclerView = view.findViewById(R.id.recyclerView_id);
        loadingGif =view.findViewById(R.id.loadingImg_userTab);
        loadingGif.setVisibility(View.GONE);
        mSearchView =view.findViewById(R.id.searchview_user);
        addFriend = view.findViewById(R.id.add_friend_frag);
        addFriend.setVisibility(View.GONE);
        mAppBarLayout = view.findViewById(R.id.appbar);
        mAppBarLayout.setExpanded(false);
        final LinearLayoutManager manager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new RecyclerAdapter(getContext(),mUserDialogArrayList);
        mAdapter.setHasStableIds(false);
        mRecyclerView.setAdapter(mAdapter);
        filter_btn =view.findViewById(R.id.filter_btn);



        populateUserDialogList();
        setTimeChsngeListener();



        filter_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Toast.makeText(getContext(),"Clicked",Toast.LENGTH_SHORT).show();
                        PopupMenu popup = new PopupMenu(view.getContext(),filter_btn, Gravity.RIGHT);
                        popup.getMenuInflater().inflate(R.menu.sort_menu,popup.getMenu());
                        popup.show();
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                              int id = item.getItemId();
                              if(id==R.id.score) {
                                  //save user setting locally
                                  SharedPreferences.Editor editor = sharedPref.edit();
                                  editor.putString("sort", "yes");
                                  editor.commit();
                                  sortByScore=true;
                                  Collections.sort(mUserDialogArrayList,SCORE);
                                  mAdapter.notifyDataSetChanged();
                              }
                                if (id == R.id.online_stats){
                                    //save user setting locally
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putString("sort", "no");
                                    editor.commit();
                                    sortByScore=false;
                                    Collections.sort(mUserDialogArrayList,ONLINESTATS);
                                    mAdapter.notifyDataSetChanged();
                                }

                                return true;

                            }
                        });



                    }
                });





        mSearchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // preventing double, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                    return;
                }

                lastClickTime = SystemClock.elapsedRealtime();
                Intent i = new Intent(getContext(), SearchName.class);
                i.putParcelableArrayListExtra("friendList",mUserDialogArrayList);
                i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                mAppBarLayout.setExpanded(false,true);
                startActivity(i);
                getActivity().overridePendingTransition(0,0);
            }
        });

        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), SearchUser.class);
                startActivity(i);
            }
        });



        return view;
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {

    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG,"OnResume");

    }

    private  void showLog(Object str){
        Log.d(TAG,String.valueOf(str));
    }



    public void populateUserDialogList() {
        mUserDialogArrayList.clear();
        addFriend.setVisibility(View.GONE);
        loadingGif.setVisibility(View.VISIBLE);
        databaseReference = mDatabase.getReference("UserFriends").child(currentUser.getUid());
        databaseReference.keepSynced(true);



        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if(dataSnapshot.hasChild("stats")){
                    showLog(dataSnapshot.getKey() +" added");
                    if(dataSnapshot.child("stats").getValue(String.class).equals("accepted")){
                     UserDialog dialog = new UserDialog();
                     dialog.setId(dataSnapshot.getKey());
                      if(!mUserDialogArrayList.contains(dialog)){
                          mUserDialogArrayList.add(dialog);
                          addFriend.setVisibility(View.GONE);
                          mAppBarLayout.setExpanded(false);
                          mAppBarLayout.setVisibility(View.VISIBLE);
                          getUserinfo(dialog);

                      }
                    }
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                showLog("Changed "+dataSnapshot.getKey());
                if(dataSnapshot.hasChild("stats")){
                    showLog(dataSnapshot.getKey() +" added");
                    if(dataSnapshot.child("stats").getValue(String.class).equals("accepted")){
                        UserDialog dialog = new UserDialog();
                        dialog.setId(dataSnapshot.getKey());
                        if(!mUserDialogArrayList.contains(dialog)){
                            mUserDialogArrayList.add(dialog);
                            getUserinfo(dialog);
                            mAppBarLayout.setExpanded(false);
                            mAppBarLayout.setVisibility(View.VISIBLE);
                            loadingGif.setVisibility(View.GONE);
                            addFriend.setVisibility(View.GONE);


                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
               UserDialog dialog = new UserDialog();
               dialog.setId(dataSnapshot.getKey());
               if(mUserDialogArrayList.contains(dialog)) {
                   mUserDialogArrayList.remove(dialog);
               }
               mAdapter.notifyDataSetChanged();
               if(mUserDialogArrayList.isEmpty()){
                   addFriend.setVisibility(View.VISIBLE);
                   mAppBarLayout.setExpanded(false);
                   mAppBarLayout.setVisibility(View.GONE);

               }

               showLog("Child Removed"+dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!mUserDialogArrayList.isEmpty()){
                    mAppBarLayout.setExpanded(false);
                    mAppBarLayout.setVisibility(View.VISIBLE);

                    addFriend.setVisibility(View.GONE);
                    YoYo.with(Techniques.FadeIn).duration(500).playOn(mRecyclerView);
                    for(UserDialog dialog :mUserDialogArrayList){
                        getUserinfo(dialog);
                    }
                }else{
                    mAppBarLayout.setExpanded(false);
                    mAppBarLayout.setVisibility(View.GONE);

                    addFriend.setVisibility(View.VISIBLE);
                }
                done =true;
                loadingGif.setVisibility(View.GONE);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




    }

    private void setTimeChsngeListener() {

      tickReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                for(UserDialog dialog : mUserDialogArrayList){
                    if(dialog.getLastseen()!=null){
                        dialog.setLastseen(MyDateFormatter.lastSeenConverterShort(dialog.getTimestamp()));
                    }
                }
                mAdapter.notifyDataSetChanged();
            }
        };
     getActivity().registerReceiver(tickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK)); // register the broadcast receiver to receive TIME_TICK

    }



    private void getUserinfo(final UserDialog dialog){
     DatabaseReference userinfoRef = mDatabase.getReference().child("Users").child(dialog.getId());
     userinfoRef.keepSynced(true);
     userinfoRef.addValueEventListener(new ValueEventListener() {
         @Override
         public void onDataChange(DataSnapshot dataSnapshot) {
             User user = dataSnapshot.getValue(User.class);
             if(dataSnapshot.hasChild("lastseen") && "0".equals(user.getOnlinestats())){
                 Long timestamp = dataSnapshot.child("lastseen").getValue(Long.class);
                 dialog.setTimestamp(timestamp);
                 dialog.setLastseen(MyDateFormatter.lastSeenConverterShort(timestamp));
             }else{
                 dialog.setLastseen(null);
             }
             if(user!=null) {
                 dialog.setName(user.getDisplayname());
                 dialog.setOnlinestats(user.getOnlinestats());
                 dialog.setUsername(user.getUsername());
                 dialog.setPhotoUrl(user.getPhotourl());
                 dialog.setEmail(user.getEmail());
                 addScoreListener(dialog);
             }



         }



         @Override
         public void onCancelled(DatabaseError databaseError) {

         }
     });

    }

    private void addScoreListener(final UserDialog dialog) {
        DatabaseReference scoreRef = FirebaseDatabase.getInstance().getReference()
                .child("UserFriends")
                .child(currentUser.getUid())
                .child(dialog.getId());
        scoreRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("score")){
                    String score =dataSnapshot.child("score").getValue(String.class);
                    dialog.setScore(score);
                    if(mUserDialogArrayList.contains(dialog)){
                        int index = mUserDialogArrayList.indexOf(dialog);
                        mUserDialogArrayList.set(index,dialog);
                        if(sortByScore){
                            Collections.sort(mUserDialogArrayList,SCORE);
                        }else{
                            Collections.sort(mUserDialogArrayList,ONLINESTATS);
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                }else if(dataSnapshot!=null && dataSnapshot.hasChild("stats")){
                    compareWithUser2(dialog);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void compareWithUser2(UserDialog dialog) {
        pullUser1QA(dialog);

    }

    public void pullUser1QA(final UserDialog dialog) {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("" +
                "UserQA/" + currentUser.getUid());
        mRef.keepSynced(true);
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            ArrayList<UserQA> User1QA = new ArrayList<>();

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    UserQA userQA = childSnapshot.getValue(UserQA.class);
                    User1QA.add(userQA);

                }

                if (dataSnapshot.getChildrenCount() == User1QA.size()) {
                    pullUser2QA( User1QA, dialog);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void pullUser2QA(final ArrayList<UserQA> user1QA, final UserDialog dialog) {

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("UserQA/" + dialog.getId());
        mRef.keepSynced(true);
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            ArrayList<UserQA> User2QA = new ArrayList<>();

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    UserQA userQA = childSnapshot.getValue(UserQA.class);
                    User2QA.add(userQA);
                }
                SetScore(user1QA,User2QA, dialog);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void SetScore(ArrayList<UserQA> user1Arr, ArrayList<UserQA> user2Arr,
                         UserDialog dialog) {
        int size = user1Arr.size();
        int commonQuestionSize = 0;
        String score;

        ArrayList<String> user1StrArr = new ArrayList<>();
        ArrayList<String> user2StrArr = new ArrayList<>();

        for (UserQA userQA : user1Arr) {
            if (!userQA.getAnswer().equals("skipped")) {
                user1StrArr.add(userQA.getQuestionId());
            }

        }
        for (UserQA userQA : user2Arr) {
            if (!userQA.getAnswer().equals("skipped")) {
                user2StrArr.add(userQA.getQuestionId());
            }
        }

        user1StrArr.retainAll(user2StrArr);

        commonQuestionSize = user1StrArr.size();

        Log.d("Score", "Common Question " + commonQuestionSize);
        user1Arr.retainAll(user2Arr);
        Log.d("Score", String.valueOf(user1Arr.size()));
        Log.d("Score", "Size " + size);
        if (commonQuestionSize != 0) {
            score = String.valueOf((int) (((double) user1Arr.size() / (double) commonQuestionSize) * 100));
            Log.d("Score", "Score is " + score);

        }else if(user1Arr.isEmpty() || user2Arr.isEmpty()){
            score ="0";

        } else {
            score = "0";
        }
        if(dialog.getOnlinestats()==null){
            dialog.setOnlinestats("0");
        }
        dialog.setScore(score);
        if(dialog.getId()!=null) {
            setScoreNode(dialog.getId(), score);
        }
         if(mUserDialogArrayList.contains(dialog)){
            int index = mUserDialogArrayList.indexOf(dialog);
            mUserDialogArrayList.set(index,dialog);
         }

         if(sortByScore){
             Collections.sort(mUserDialogArrayList,SCORE);
         }else {
             Collections.sort(mUserDialogArrayList,ONLINESTATS);
        }
        mAdapter.notifyDataSetChanged();








        if(mUserDialogArrayList.size()==friendCount && !done){
            done =true;
            showLog("DONE" + "LIST SIZE " + mUserDialogArrayList.size());


         if(sortByScore){
             Collections.sort(mUserDialogArrayList,SCORE);
             mAdapter.notifyDataSetChanged();
             loadingGif.setVisibility(View.GONE);

         }else {
             Collections.sort(mUserDialogArrayList,ONLINESTATS);
             mAdapter.notifyDataSetChanged();
             loadingGif.setVisibility(View.GONE);
         }

        }

    }





    public void setScoreNode(String user2Uid,String score){
        DatabaseReference scoreRef = FirebaseDatabase.getInstance().getReference()
                .child("UserFriends")
                .child(currentUser.getUid())
                .child(user2Uid)
                .child("score");
        scoreRef.setValue(score);

        DatabaseReference scoreRef2 = FirebaseDatabase.getInstance().getReference()
                .child("UserFriends")
                .child(user2Uid)
                .child(currentUser.getUid())
                .child("score");

        scoreRef2.setValue(score);
    }





    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroy() {
        try {
            if (tickReceiver != null) {
              getActivity().unregisterReceiver(tickReceiver);
            }
        } catch (IllegalArgumentException e) {
            tickReceiver = null;
        }
        super.onDestroy();
    }
}
