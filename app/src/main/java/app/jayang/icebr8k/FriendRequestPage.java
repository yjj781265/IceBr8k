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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import app.jayang.icebr8k.Modle.User;
import app.jayang.icebr8k.Modle.UserDialog;

public class FriendRequestPage extends AppCompatActivity {

    private Toolbar mToolbar;
    private ArrayList<UserDialog> mUserDialogs;
    private FriendReqestItemAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private Boolean init;
    private TextView noFrt;
    private FirebaseUser currentUser;
    private int counter,childerenCount;

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
        init =false;
        DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference().child("Friends")
                .child(currentUser.getUid());
        requestRef.keepSynced(true);
        requestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                 mUserDialogs.clear();
                  counter =0;
                 childerenCount=(int)dataSnapshot.getChildrenCount();
                 if(childerenCount==0){
                     noFrt.setVisibility(View.VISIBLE);
                 }

                for(DataSnapshot chidSnapShot : dataSnapshot.getChildren()){
                   counter++;
                    if(chidSnapShot.child("Stats").getValue(String.class).equals("Pending")){
                        addToDialog( chidSnapShot.getKey());
                    }

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
                UserDialog dialog  =new UserDialog(user2Uid,user.getDisplayname(),user.getUsername()
                ,user.getPhotourl(), null,null,user.getEmail());
                mUserDialogs.add(dialog);

                if(counter!=0 && counter==childerenCount){
                    noFrt.setVisibility(View.GONE);
                 mAdapter.notifyDataSetChanged();
                }else{
                    noFrt.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}
