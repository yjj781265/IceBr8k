package app.jayang.icebr8k.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.beardedhen.androidbootstrap.BootstrapButton;
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

import app.jayang.icebr8k.Modle.User;
import app.jayang.icebr8k.Modle.UserDialog;
import app.jayang.icebr8k.Modle.UserQA;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.RecyclerAdapter;
import app.jayang.icebr8k.SearchName;
import app.jayang.icebr8k.SearchUser;


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
    RelativeLayout mSearchView;
    Button filter_btn;
    BootstrapButton addFriend;
    private SharedPreferences sharedPref;
    private Boolean sortByScore,done;
    private String sortByScoreStr;
    private long lastClickTime = 0;



    private static final Comparator<UserDialog>ONLINESTATS = new Comparator<UserDialog>() {
        @Override
        public int compare(UserDialog dialog, UserDialog t1) {
           int result= Integer.valueOf(t1.getOnlineStats()).
                    compareTo( Integer.valueOf(dialog.getOnlineStats()));
            if(result==0){
                result =dialog.getName().compareTo(t1.getName());
            }
            return  result;
        }
    };

    private static final Comparator<UserDialog> SCORE = new Comparator<UserDialog>() {
        @Override
        public int compare(UserDialog dialog, UserDialog t1) {
          int  result= Integer.valueOf(t1.getScore()).
                    compareTo( Integer.valueOf(dialog.getScore()));
          if(result==0){
              result =dialog.getName() .compareTo(t1.getName());
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
        mSearchView.setVisibility(View.GONE);
        addFriend = view.findViewById(R.id.add_friend_frag);
        addFriend.setVisibility(View.GONE);
        mAppBarLayout = view.findViewById(R.id.appbar);
        mAppBarLayout.setExpanded(false,false);
        final LinearLayoutManager manager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new RecyclerAdapter(getContext(),mUserDialogArrayList);
        mAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(mAdapter);
        filter_btn =view.findViewById(R.id.filter_btn);


        populateUserDialogList();
        addQAListener();

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
                                  SharedPreferences.Editor editor = sharedPref.edit();
                                  editor.putString("sort", "yes");
                                  editor.commit();
                                  sortByScore=true;
                                  Collections.sort(mUserDialogArrayList,SCORE);
                                  mAdapter.notifyDataSetChanged();
                              }
                                if (id == R.id.online_stats){
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
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.
                        FLAG_ACTIVITY_REORDER_TO_FRONT );
                startActivity(i);
            }
        });

        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), SearchUser.class);
                startActivity(i);
            }
        });
        databaseReference = mDatabase.getReference("Friends").child(currentUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(mAdapter.getItemCount()==0 &&done){
                    addFriend.setVisibility(View.VISIBLE);
                    loadingGif.setVisibility(View.GONE);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
        friendCount=0;
        databaseReference = mDatabase.getReference("Friends").child(currentUser.getUid());
        databaseReference.keepSynced(true);


        databaseReference.equalTo("Accepted").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount()>0){
                   addFriend.setVisibility(View.GONE);

                }else{
                    addFriend.setVisibility(View.VISIBLE);
                }
                loadingGif.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if(dataSnapshot.child("Stats").getValue(String.class).equals("Accepted")){
                    showLog(dataSnapshot.getKey() +" added");
                    done =false;
                   UserDialog dialog = new UserDialog();
                   dialog.setId(dataSnapshot.getKey());
                    getUserinfo(dialog);
                    addFriend.setVisibility(View.GONE);
                    mSearchView.setVisibility(View.VISIBLE);
                    filter_btn.setVisibility(View.VISIBLE);
                    friendCount++;
                    showLog("friendCount "+friendCount);
                }


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                showLog("Changed "+dataSnapshot.getKey());
                if(dataSnapshot.child("Stats").getValue(String.class).equals("Accepted")){
                    showLog(dataSnapshot.getKey() +" added");
                    done =false;
                    UserDialog dialog = new UserDialog();
                    dialog.setId(dataSnapshot.getKey());
                    getUserinfo(dialog);
                    addFriend.setVisibility(View.GONE);
                    mSearchView.setVisibility(View.VISIBLE);
                    filter_btn.setVisibility(View.VISIBLE);
                    friendCount++;

                    showLog("friendCount "+friendCount);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
               UserDialog dialog = new UserDialog();
               dialog.setId(dataSnapshot.getKey());
               mUserDialogArrayList.remove(dialog);
               friendCount = mUserDialogArrayList.size();
               mAdapter.notifyDataSetChanged();
               if(mAdapter.getItemCount()==0){
                   addFriend.setVisibility(View.VISIBLE);
                   mSearchView.setVisibility(View.GONE);
                   filter_btn.setVisibility(View.GONE);
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




    }



    private void getUserinfo(final UserDialog dialog){
     DatabaseReference userinfoRef = mDatabase.getReference().child("Users").child(dialog.getId());
     userinfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
         @Override
         public void onDataChange(DataSnapshot dataSnapshot) {
             User user = dataSnapshot.getValue(User.class);
             dialog.setName(user.getDisplayname());
             dialog.setUsername(user.getUsername());
             dialog.setPhotoUrl(user.getPhotourl());
             dialog.setEmail(user.getEmail());
             dialog.setOnlineStats(user.getOnlineStats());
             compareWithUser2(dialog);



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
        if(dialog.getOnlineStats()==null){
            dialog.setOnlineStats("0");
        }
        dialog.setScore(score);
        if(!done) {
            mUserDialogArrayList.add(dialog);
            addOnLineListener(dialog);
            addUser2QAListener(dialog);
        }else if(done){
            int index;
            for(index =0; index<mUserDialogArrayList.size();index++ ){
               if(mUserDialogArrayList.get(index).getId().equals(dialog.getId())){
                   mUserDialogArrayList.set(index,dialog);
                   if(sortByScore){
                       Collections.sort(mUserDialogArrayList,SCORE);
                       mAdapter.notifyDataSetChanged();
                       showLog("resorted by score");
                   }else{
                       mAdapter.notifyDataSetChanged();
                       showLog("updated at "+ index); //update score
                   }
               }
           }


        }


        showLog("list Size " + mUserDialogArrayList.size());


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


    private void addQAListener(){
        FirebaseUser currentUser =FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference mref = FirebaseDatabase.getInstance().getReference().child("UserQA")
                .child(currentUser.getUid());
        mref.keepSynced(true);
        mref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(done) {
                    showLog("New Question with User Id" + dataSnapshot.getKey());
                  for(UserDialog dialog : mUserDialogArrayList){
                      compareWithUser2(dialog);

                  }


                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addUser2QAListener(final UserDialog dialog){
        DatabaseReference mref = FirebaseDatabase.getInstance().getReference().child("UserQA")
                .child(dialog.getId());
        mref.keepSynced(true);
        mref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(done) {
                    showLog("New Question with User2 Id" + dataSnapshot.getKey());

                        compareWithUser2(dialog);




                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    private void addOnLineListener(final UserDialog dialog){
        DatabaseReference mref = FirebaseDatabase.getInstance().getReference().child("Users").
                child(dialog.getId()).child("onlineStats");
        mref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int index;
                if(done ) {
                    String online = dataSnapshot.getValue(String.class);
                    for(index=0;index<mUserDialogArrayList.size();index++){
                        if(mUserDialogArrayList.get(index).getId().equals(dialog.getId())){
                            dialog.setOnlineStats(online);
                            mUserDialogArrayList.set(index,dialog);
                            if(!sortByScore) {
                                Collections.sort(mUserDialogArrayList, ONLINESTATS);
                                mAdapter.notifyDataSetChanged();
                                showLog("online changed");
                            }else{
                                mAdapter.notifyDataSetChanged();
                                showLog("online updated");
                            }

                        }
                    }


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }






    @Override
    public void onStop() {
        super.onStop();

    }






}
