package app.jayang.icebr8k.Fragments;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;


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
import app.jayang.icebr8k.Modle.ChatDialog;
import app.jayang.icebr8k.Modle.User;
import app.jayang.icebr8k.Modle.UserDialog;
import app.jayang.icebr8k.Modle.UserQA;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.RecyclerAdapter;
import app.jayang.icebr8k.SearchName;
import app.jayang.icebr8k.Viewholder;


/**
 * Created by LoLJay on 10/20/2017.
 */

public class Userstab_Fragment extends Fragment {
    View view;
    FirebaseDatabase mDatabase;
    DatabaseReference databaseReference;
    ArrayList<UserDialog> mUserDialogArrayList;
    RecyclerView mRecyclerView;
    RelativeLayout loadingGif;
    FirebaseUser currentUser;
    RecyclerAdapter mAdapter;
    Integer childCount,counter;
    SwipeRefreshLayout mRefreshLayout;
    LinearLayout mSearchView;
    Button filter_btn;
    private ArrayList<UserDialog> mFilteredList;
    private boolean sortByscore,once;

    public interface OnItemClickListener{
        void onItemClick(UserDialog dialog);
    }

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
        sortByscore =true;


    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.users_tab, container, false);
        mRecyclerView = view.findViewById(R.id.recyclerView_id);
        mSearchView =view.findViewById(R.id.searchview_user);
        final LinearLayoutManager manager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setHasFixedSize(true);
        mRefreshLayout =view.findViewById(R.id.swipetoRefresh);
                filter_btn =view.findViewById(R.id.filter_btn);
        setHasOptionsMenu(true);

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
                                  if (!sortByscore) {
                                      sortByscore = true;
                                      populateUserDialogList();
                                  }
                              }
                                if (id == R.id.online_stats)
                                    if (sortByscore) {
                                        sortByscore = false;
                                        populateUserDialogList();
                                    }
                                return true;

                            }
                        });



                    }
                });
        loadingGif = view.findViewById(R.id.loadingImg_friendtab);
        loadingGif.setVisibility(View.VISIBLE);
        addQAListener();


        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mRefreshLayout.setRefreshing(true);
                populateUserDialogList();
            }
        });

        mSearchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), SearchName.class);
                startActivity(i);
            }
        });

        return view;
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
       if(isVisibleToUser && getView()!=null && !once){

       }
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d("Score","OnResume");

    }

    public void populateUserDialogList() {

        databaseReference = mDatabase.getReference("Friends").child(currentUser.getUid());
        databaseReference.keepSynced(true);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                once =true;
                childCount =(int) dataSnapshot.getChildrenCount();
                counter =0;
                mUserDialogArrayList.clear();
                for(DataSnapshot childSnapshot: dataSnapshot.getChildren()){
                    if(childSnapshot.child("Stats").getValue(String.class).equals("Accepted")){
                        UserDialog dialog = new UserDialog();
                        dialog.setId(childSnapshot.getKey());
                        getUserinfo(dialog);

                    }
                    counter++;
                }
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
             dialog.setScore("0");
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

        mUserDialogArrayList.add(dialog);
        if(once){
            
        }




        if (mUserDialogArrayList != null && !mUserDialogArrayList.isEmpty()
                && counter==childCount) {
            if(sortByscore){
                Collections.sort(mUserDialogArrayList,SCORE);
                mAdapter = new RecyclerAdapter(getContext(),mUserDialogArrayList,sortByscore);
            }else{
                Collections.sort(mUserDialogArrayList,ONLINESTATS);
                mAdapter = new RecyclerAdapter(getContext(),mUserDialogArrayList,sortByscore);
            }

            mRecyclerView.setAdapter( mAdapter);
            mRefreshLayout.setRefreshing(false);
            once =false;
            loadingGif.setVisibility(view.INVISIBLE);

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
               populateUserDialogList();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addChildeventListener(UserDialog dialog){
        DatabaseReference mref = FirebaseDatabase.getInstance().getReference().child("Users").child(dialog.getId())

                ;
        mref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
               if(!sortByscore){
                   Log.d("UseTab","Online Changed" +dataSnapshot);

                  populateUserDialogList();
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
    }






    @Override
    public void onStop() {
        populateUserDialogList();
        super.onStop();

    }

    public ArrayList<UserDialog> getUserDialogArrayList() {
        return mUserDialogArrayList;
    }

    public RecyclerAdapter getAdapter() {
        return mAdapter;
    }
}
