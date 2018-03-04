package app.jayang.icebr8k;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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

import app.jayang.icebr8k.Adapter.FriendReqestItemAdapter;
import app.jayang.icebr8k.Modle.User;
import app.jayang.icebr8k.Modle.UserDialog;

public class FriendRequestPage extends AppCompatActivity {

    private Toolbar mToolbar;
    private ArrayList<UserDialog> mUserDialogs;
    private FriendReqestItemAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private TextView noFrt;
    private FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request_page);
        mToolbar = findViewById(R.id.friendRequestToolbar);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mUserDialogs =new ArrayList<>();
        mRecyclerView = findViewById(R.id.frt_recycler);
        noFrt = findViewById(R.id.frt_noFrt);
        mAdapter =new FriendReqestItemAdapter(mUserDialogs,getApplicationContext());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
        getData();
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        OneSignal.clearOneSignalNotifications();
    }


    @Override
    public boolean onSupportNavigateUp() {
       finish();
       return true;
    }

    @Override
    public void onBackPressed() {
        finish();

    }

    private void getData(){
        DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference().child("UserFriends")
                .child(currentUser.getUid());
        requestRef.keepSynced(true);

        requestRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if("pending".equals(dataSnapshot.child("stats").getValue(String.class))){
                    addToDialog( dataSnapshot.getKey());
                }
                noFrt.setVisibility(View.GONE);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if("accepted".equals(dataSnapshot.child("stats").getValue(String.class))){
                    UserDialog dialog = new UserDialog();
                    dialog.setId(dataSnapshot.getKey());
                    mUserDialogs.remove(dialog);
                    mAdapter.notifyDataSetChanged();
                    if(mAdapter.getItemCount() ==0){
                        noFrt.setVisibility(View.VISIBLE);
                    }
                }


            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                UserDialog dialog = new UserDialog();
                dialog.setId(dataSnapshot.getKey());
                mUserDialogs.remove(dialog);
                mAdapter.notifyDataSetChanged();
                if(mAdapter.getItemCount() ==0){
                    noFrt.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        requestRef.equalTo("pending").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               if(dataSnapshot.hasChildren()){
                   noFrt.setVisibility(View.GONE);
               }else{
                   noFrt.setVisibility(View.VISIBLE);
               }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }





    private void addToDialog(final String user2Uid){

        DatabaseReference userref = FirebaseDatabase.getInstance().getReference().child("Users").
                child(user2Uid);
        userref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                UserDialog dialog  =new UserDialog(user.getDisplayname(),user.getUsername()
                ,user.getPhotourl(), null,null,user.getEmail(),user2Uid,null);
                mUserDialogs.add(dialog);
                noFrt.setVisibility(View.GONE);
                 mAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}
