package app.jayang.icebr8k.Fragments;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.common.api.Scope;
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


import app.jayang.icebr8k.Homepage;
import app.jayang.icebr8k.Modle.User;
import app.jayang.icebr8k.Modle.UserDialog;

import app.jayang.icebr8k.Modle.UserQA;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.Adapter.RecyclerAdapter;
import app.jayang.icebr8k.SearchName;
import app.jayang.icebr8k.SearchUser;
import app.jayang.icebr8k.Utility.ActivityCommunicator;
import app.jayang.icebr8k.Utility.Compatability;
import app.jayang.icebr8k.Utility.MyDateFormatter;


/**
 * Created by LoLJay on 10/20/2017.
 */

public class Userstab_Fragment extends Fragment{
    View view;
    String TAG ="UserFrag123";

    FirebaseDatabase mDatabase;
    me_frag parentFrag;
    DatabaseReference databaseReference;
    ArrayList<UserDialog> mUserDialogArrayList;
    RelativeLayout loadingGif;
    RecyclerView mRecyclerView;
    FirebaseUser currentUser;
    RecyclerAdapter mAdapter;
    TextView seachView;
    Integer friendCount;
    BootstrapButton addFriend;
    FrameLayout searchLayout;
    Button filter_btn;
    Boolean isVisiable = false;
    private BroadcastReceiver tickReceiver;
    private SharedPreferences sharedPref;
    private Boolean sortByScore,done;
    private String sortByScoreStr;
    private Boolean firstTime = true;
    private long lastClickTime = 0;



    private static final Comparator<UserDialog>ONLINESTATS = new Comparator<UserDialog>() {
        @Override
        public int compare(UserDialog dialog, UserDialog t1) {
            int result =0;
            if(t1.getUser()!=null && dialog.getUser()!=null) {
                result = Integer.valueOf(t1.getUser().getOnlinestats()).
                        compareTo(Integer.valueOf(dialog.getUser().getOnlinestats()));
            }
            if(result==0){
                if(t1.getUser()!=null && dialog.getUser()!=null) {
                    result =dialog.getUser().getDisplayname().compareTo(t1.getUser().getDisplayname());
                }

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
                if(t1.getUser()!=null && dialog.getUser()!=null) {
                    result =dialog.getUser().getDisplayname().compareTo(t1.getUser().getDisplayname());
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
        addFriend = view.findViewById(R.id.add_friend_frag);
        addFriend.setVisibility(View.GONE);
        filter_btn = view.findViewById(R.id.filter_btn);
        seachView = view.findViewById(R.id.searchview_user);
        searchLayout = view.findViewById(R.id.search_layout);


        final LinearLayoutManager manager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new RecyclerAdapter(getActivity(),mUserDialogArrayList);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(mAdapter);
        populateUserDialogList();
        parentFrag = (me_frag) getParentFragment();
        if(parentFrag!=null){
            parentFrag.getFragmentVisiable();
            Log.d("interface123",    ""+ parentFrag.getFragmentVisiable());
        }





        filter_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getContext(),"Clicked",Toast.LENGTH_SHORT).show();
                PopupMenu popup = new PopupMenu(view.getContext(),filter_btn, Gravity.RIGHT);
                popup.getMenuInflater().inflate(R.menu.sort_menu,popup.getMenu());
                popup.show();
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @SuppressLint("StaticFieldLeak")
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if(id==R.id.score) {
                            //save user setting locally
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("sort", "yes");
                            editor.commit();
                            sortByScore=true;
                            new  AsyncTask<Void,Void,Void>(){
                                @Override
                                protected Void doInBackground(Void... voids) {
                                    Collections.sort(mUserDialogArrayList,SCORE);


                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Void aVoid) {
                                    mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                                    mAdapter.notifyDataSetChanged();
                                }
                            }.execute();
                        }
                        if (id == R.id.online_stats){
                            //save user setting locally
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("sort", "no");
                            editor.commit();
                            sortByScore=false;
                            new AsyncTask<Void,Void,Void>(){
                                @Override
                                protected Void doInBackground(Void... voids) {
                                    Collections.sort(mUserDialogArrayList,ONLINESTATS);

                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Void aVoid) {
                                    mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                                    mAdapter.notifyDataSetChanged();
                                }
                            }.execute();
                        }

                        return true;

                    }
                });



            }
        });

        seachView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // preventing double, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();
                Intent i = new Intent(getContext(), SearchName.class);
                i.putExtra("friendList",mUserDialogArrayList);
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
         if(getView()!=null){
             //Toast.makeText(getActivity(), ""+parentFrag.getFragmentVisiable(), Toast.LENGTH_SHORT).show();
             isVisiable =isVisibleToUser;
             if(isVisibleToUser && parentFrag.getFragmentVisiable()){
                 if(!sortByScore){

                     Collections.sort(mUserDialogArrayList,ONLINESTATS);
                     mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                     mAdapter.notifyDataSetChanged();
                 }else{
                     Collections.sort(mUserDialogArrayList, SCORE);
                     mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                     mAdapter.notifyDataSetChanged();
                 }
             }
         }

        }

    @Override
    public void onResume() {
        super.onResume();


        }


    private  void showLog(Object str){
        Log.d(TAG,String.valueOf(str));
    }

  //  added then call notifydataset changed in the value listener , update in the adapter

    public void populateUserDialogList() {
      loadingGif .setVisibility(View.VISIBLE);

      databaseReference = FirebaseDatabase.getInstance().getReference().child("UserFriends")
              .child(currentUser.getUid());
      databaseReference.orderByChild("stats").equalTo("accepted").addChildEventListener(new ChildEventListener() {
          @SuppressLint("StaticFieldLeak")
          @Override
          public void onChildAdded(DataSnapshot dataSnapshot, String s) {
              showLog(dataSnapshot.getKey() + " added" );
              final UserDialog userDialog = new UserDialog(null,null,dataSnapshot.getKey());

              if(dataSnapshot.hasChild("score")){
                  userDialog.setScore(dataSnapshot.child("score").getValue(String.class));
              }
              if(!mUserDialogArrayList.contains(userDialog)){
                  mUserDialogArrayList.add(userDialog);
              }


              // add onLine ref
              DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                      .child("Users")
                      .child(userDialog.getId());
              ref.addValueEventListener(new ValueEventListener() {
                  @Override
                  public void onDataChange(final DataSnapshot dataSnapshot) {

                      new AsyncTask<Void,Void,Void>(){

                          @Override
                          protected Void doInBackground(Void... voids) {
                              User user = dataSnapshot.getValue(User.class);
                              if(user!=null){
                                  userDialog.setUser(user);
                              }


                              return null;
                          }

                          @Override
                          protected void onPostExecute(Void aVoid) {
                              if(parentFrag.getFragmentVisiable() &&isVisiable){
                                  if(!sortByScore){
                                      Collections.sort(mUserDialogArrayList,ONLINESTATS);
                                      mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                                      mAdapter.notifyDataSetChanged();
                                  }else{
                                      int index = mUserDialogArrayList.indexOf(userDialog);
                                      mRecyclerView.setItemAnimator(null);
                                      mAdapter.notifyItemChanged(index);
                                  }
                              }
                          }
                      }.execute();





                  }

                  @Override
                  public void onCancelled(DatabaseError databaseError) {

                  }
              });

              // add score ref
              DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference()
                      .child("UserFriends")
                      .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                      .child(userDialog.getId())
                      .child("score");
              ref2.addValueEventListener(new ValueEventListener() {
                  @Override
                  public void onDataChange(final DataSnapshot dataSnapshot) {


                      new AsyncTask<Void,Void,Void>(){

                          @Override
                          protected Void doInBackground(Void... voids) {
                           String score = dataSnapshot.getValue(String.class);
                              if(score!=null ) {
                                  userDialog.setScore(score);
                                  Log.d("usertab123",""+ isMainThread());
                              }
                              return null;
                          }

                          @Override
                          protected void onPostExecute(Void aVoid) {
                              Log.d("usertab123","after"+ isMainThread());
                              if(parentFrag.getFragmentVisiable()&&isVisiable){
                                  if(sortByScore){
                                      Collections.sort(mUserDialogArrayList,SCORE);
                                      mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                                      mAdapter.notifyDataSetChanged();
                                  }else{
                                      int index = mUserDialogArrayList.indexOf(userDialog);
                                      mRecyclerView.setItemAnimator(null);
                                      mAdapter.notifyItemChanged(index);
                                  }
                              }
                          }
                      }.execute();



                  }

                  @Override
                  public void onCancelled(DatabaseError databaseError) {

                  }
              });

              showLog(userDialog.getScore() + " score " );

              if(!firstTime){
                  new AsyncTask<Void,Void,Void>(){
                      @Override
                      protected Void doInBackground(Void... voids) {
                          if(!mUserDialogArrayList.isEmpty()){
                              getUserInfo(userDialog);

                          }
                          return null;
                      }

                      @Override
                      protected void onPostExecute(Void aVoid) {
                          if(isVisiable){
                              if(sortByScore){
                                  Collections.sort(mUserDialogArrayList,SCORE);
                              }else{
                                  Collections.sort(mUserDialogArrayList,ONLINESTATS);
                              }
                              mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                              mAdapter.notifyDataSetChanged();
                          }


                      }

                      @Override
                      protected void onPreExecute() {
                          showLog("getting user info");
                      }
                  }.execute();
              }

          }

          @Override
          public void onChildChanged(DataSnapshot dataSnapshot, String s) {
              showLog(dataSnapshot.getKey() + " changed" );
          }

          @Override
          public void onChildRemoved(DataSnapshot dataSnapshot) {
              showLog(dataSnapshot.getKey() + " moved" );
              UserDialog userDialog = new UserDialog(null,null,dataSnapshot.getKey());
              if(mUserDialogArrayList.contains(userDialog)){
                  int index = mUserDialogArrayList.indexOf(userDialog);
                  mUserDialogArrayList.remove(index);
                  mAdapter.notifyItemRemoved(index);
              }
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
              showLog(dataSnapshot.getKey() + " value listener" );
              // getting userinfo in the background first time
              new getting_user_info().execute();


          }

          @Override
          public void onCancelled(DatabaseError databaseError) {

          }
      });
    }

// get user obj and score
   void  getUserInfo(final UserDialog userDialog){
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(userDialog.getId());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                userDialog.setUser(user);
                if(userDialog.getScore()==null){
                    compareWithUser2(userDialog);
                    }
                if(sortByScore){
                    Collections.sort(mUserDialogArrayList,SCORE);
                }else{
                    Collections.sort(mUserDialogArrayList,ONLINESTATS);
                }
                }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



   }



    public void compareWithUser2(final UserDialog userDialog) {
        final ArrayList<UserQA> userQA1 = new ArrayList<>();
        final ArrayList<UserQA> userQA2 = new ArrayList<>();
        final DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("UserQA/" + currentUser.getUid());
        final DatabaseReference mRef2 = FirebaseDatabase.getInstance().getReference("UserQA/" + userDialog.getId());

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
                        // important
                        int score = mCompatability.getScore();
                        userDialog.setScore(String.valueOf(score));
                        if(sortByScore && isVisiable){
                            Collections.sort(mUserDialogArrayList,SCORE);
                            mAdapter.notifyDataSetChanged();
                            }

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                                .child("UserFriends")
                                .child(currentUser.getUid())
                                .child(userDialog.getId())
                                .child("score");
                        ref.setValue(String.valueOf(score));
                        showLog(String.valueOf(score) + " SCORE IS HERE");




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

    @Override
    public void onStart() {
        super.onStart();
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

    void reLoad(){
        if(getView()!=null){

            if(isVisiable && parentFrag.getFragmentVisiable()){
                if(!sortByScore){

                    Collections.sort(mUserDialogArrayList,ONLINESTATS);
                    mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                    mAdapter.notifyDataSetChanged();
                }else{
                    Collections.sort(mUserDialogArrayList, SCORE);
                    mRecyclerView.setItemAnimator(new DefaultItemAnimator());
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    }




    public  Boolean isMainThread(){
        return Looper.myLooper() == Looper.getMainLooper();
    }





    public  class  getting_user_info extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            if (!mUserDialogArrayList.isEmpty()) {
                for (UserDialog userDialog : mUserDialogArrayList) {
                    getUserInfo(userDialog);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            mAdapter.notifyDataSetChanged();
            loadingGif.setVisibility(View.GONE);
            addFriend.setVisibility(mUserDialogArrayList.isEmpty() ? View.VISIBLE : View.GONE);
            searchLayout.setVisibility(mUserDialogArrayList.isEmpty() ? View.GONE : View.VISIBLE);
            firstTime = false;
        }

        @Override
        protected void onPreExecute() {
            showLog("getting user info");
        }
    };

}