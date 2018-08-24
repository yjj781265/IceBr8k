package app.jayang.icebr8k;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

import java.util.ArrayList;

import app.jayang.icebr8k.Adapter.FriendRequestAdapter;
import app.jayang.icebr8k.Modle.User;
import app.jayang.icebr8k.Modle.UserQA;
import app.jayang.icebr8k.Utility.Compatability;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class FriendRequestPage extends SwipeBackActivity {
    private RecyclerView mRecyclerView;
    private TextView nofrt;
    private Toolbar mToolbar;
    private LinearLayoutManager mLayoutManager;
    private ArrayList<User> mUsers;
    private FriendRequestAdapter mAdapter;
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference ref;
    private ChildEventListener friendRequestChildListener;


    @Override
   public  void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request_page);
        mRecyclerView = (RecyclerView) findViewById(R.id.frt_recycler);
        nofrt = (TextView) findViewById(R.id.frt_noFrt);
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mUsers = new ArrayList<>();
        mToolbar = (Toolbar) findViewById(R.id.friendRequestToolbar);
        mAdapter = new FriendRequestAdapter(mUsers,this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setHasFixedSize(true);
        nofrt.setVisibility(View.VISIBLE);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        OneSignal.clearOneSignalNotifications();
        loadData();
    }

    void loadData(){
        ref = FirebaseDatabase.getInstance().getReference()
                .child("UserFriends")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        friendRequestChildListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("friend123",dataSnapshot.getKey() +"added");
                if("pending".equals(dataSnapshot.child("stats").getValue(String.class))){
                    nofrt.setVisibility(View.GONE);
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                            .child("Users").child(dataSnapshot.getKey());
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            mUsers.add(user);
                            mAdapter.notifyItemInserted(mUsers.indexOf(user));
                            compareWithUser2(dataSnapshot.getKey(),user);
                            nofrt.setVisibility(mUsers.isEmpty()? View.VISIBLE:View.GONE);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if("accepted".equals(dataSnapshot.child("stats").getValue(String.class))){
                    nofrt.setVisibility(View.GONE);
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                            .child("Users").child(dataSnapshot.getKey());
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);

                            if(mUsers.contains(user)){
                                mUsers.remove(mUsers.indexOf(user));
                                mAdapter.notifyItemRemoved(mUsers.indexOf(user));
                            }

                            nofrt.setVisibility(mUsers.isEmpty()? View.VISIBLE:View.GONE);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }else if("pending".equals(dataSnapshot.child("stats").getValue(String.class))){
                    nofrt.setVisibility(View.GONE);
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                            .child("Users").child(dataSnapshot.getKey());
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            mUsers.add(user);
                            mAdapter.notifyItemInserted(mUsers.indexOf(user));
                            compareWithUser2(dataSnapshot.getKey(),user);
                            nofrt.setVisibility(mUsers.isEmpty()? View.VISIBLE:View.GONE);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
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
        ref.orderByChild("stats").addChildEventListener(friendRequestChildListener);


    }
    public void compareWithUser2(final String user2Uid, final User user) {
        final ArrayList<UserQA> userQA1 = new ArrayList<>();
        final ArrayList<UserQA> userQA2 = new ArrayList<>();
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("UserQA/" + currentUser.getUid());
        final DatabaseReference mRef2 = FirebaseDatabase.getInstance().getReference("UserQA/" + user2Uid);

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
                        user.setScore(""+score);
                        if(mUsers.contains(user)){
                            mAdapter.notifyItemChanged(mUsers.indexOf(user));
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
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        if(friendRequestChildListener!=null && ref!=null){
            ref.removeEventListener(friendRequestChildListener);
        }

        super.onDestroy();
    }
}
