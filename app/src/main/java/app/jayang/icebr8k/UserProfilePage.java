package app.jayang.icebr8k;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.api.attributes.BootstrapBrand;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.dd.processbutton.FlatButton;
import com.dd.processbutton.iml.ActionProcessButton;
import com.github.lzyzsd.circleprogress.ArcProgress;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;

import app.jayang.icebr8k.Modle.User;
import app.jayang.icebr8k.Modle.UserQA;


public class UserProfilePage extends AppCompatActivity implements View.OnClickListener {
    Toolbar profileToolbar;
    ImageView mImageView,qrImage;
    ActionProcessButton  compare_btn;
    FlatButton message_btn,addFriend_btn,deleteFriend_btn,reset_btn;
    TextView displayname_profile, email_profile, username_profile;


    FirebaseDatabase database;
    FirebaseUser currentUser;
    User mUser, selfUser;
    String uid;
    Dialog dialog;
    ArcProgress arcProgress;
    String User2Uid;
    ArrayList<String> User1QArr, User2QArr;
    ArrayList<UserQA> User1QA, User2QA;
    ArrayList<UserQA> temp1QA, temp2QA;
    int score;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_page);


        profileToolbar = findViewById(R.id.profileToolbar);
        setSupportActionBar(profileToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mImageView = findViewById(R.id.profileButton);
        qrImage = findViewById(R.id.profile_QR);
        compare_btn = findViewById(R.id.compare_btn);
        addFriend_btn = findViewById(R.id.addFriend_btn);
        message_btn = findViewById(R.id.message_btn);
        deleteFriend_btn = findViewById(R.id.deleteFriend_btn);
        reset_btn =findViewById(R.id.reset_btn);
        displayname_profile = findViewById(R.id.displayname_profile);
        email_profile = findViewById(R.id.email_profile);
        username_profile = findViewById(R.id.username_profile);
        database = FirebaseDatabase.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        compare_btn.setOnClickListener(this);
        addFriend_btn.setOnClickListener(this);
        message_btn.setOnClickListener(this);
        deleteFriend_btn.setOnClickListener(this);
        reset_btn.setOnClickListener(this);




        Intent i = getIntent();
        if(i!=null) {
            mUser = (User) i.getSerializableExtra("userInfo"); //user2
            uid = i.getStringExtra("userUid");
        }



        // uid is not my self
        if (mUser != null && uid!=null &&! uid.equals(currentUser.getUid())) {
            compare_btn.setVisibility(View.VISIBLE);
            reset_btn.setVisibility(View.GONE);
            qrImage.setVisibility(View.GONE);
            compare_btn.setMode(ActionProcessButton.Mode.PROGRESS);
            updateUI(mUser);
            checkFriendStats();

            // uid is currentuser
        }else if(mUser != null && uid!=null && uid.equals(currentUser.getUid())){
            qrImage.setVisibility(View.VISIBLE);
            compare_btn.setVisibility(View.GONE);
            addFriend_btn.setVisibility(View.GONE);
            message_btn.setVisibility(View.GONE);
            deleteFriend_btn.setVisibility(View.GONE);
            reset_btn.setVisibility(View.VISIBLE);
            updateUI(mUser);
        }


    }


    @Override
    protected void onStart() {
        super.onStart();
        updateUI(mUser);



    }


    public void updateUI(User user) {
        getSupportActionBar().setTitle(user.getDisplayname());
        Glide.with(getBaseContext()).load(user.getPhotourl()).
                apply(RequestOptions.circleCropTransform()).into(mImageView);
        displayname_profile.setText(user.getDisplayname());
        email_profile.setText(user.getEmail());
        username_profile.setText(user.getUsername());

    }






    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }


    public int compareWithUser2(User user2) {
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Usernames/" + user2.getUsername());
        mRef.keepSynced(true);
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User2Uid = dataSnapshot.getValue(String.class);
                compare_btn.setProgress(20);
                Log.d("user2", User2Uid);
                pullUser2Q(User2Uid);

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return score;

    }

    public void pullUser1Q(String Uid, final ArrayList arrayList) {
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("UserQA/" + Uid);
        mRef.keepSynced(true);
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User1QArr = new ArrayList<String>();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String key = childSnapshot.getKey();
                    User1QArr.add(key);
                    Log.d("key", key);
                }
                User1QArr.retainAll(arrayList);
                compare_btn.setProgress(60);
                Log.d("arr", User1QArr.toString());
                getUser1QA(FirebaseAuth.getInstance().getCurrentUser().getUid());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void pullUser2Q(String Uid) {
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("UserQA/" + Uid);
        mRef.keepSynced(true);
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                User2QArr = new ArrayList<String>();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String key = childSnapshot.getKey();
                    User2QArr.add(key);
                    Log.d("key2", key);
                }
                compare_btn.setProgress(40);
                pullUser1Q(FirebaseAuth.getInstance().getCurrentUser().getUid(), User2QArr);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void getUser1QA(final String User1ID) {
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("UserQA/" + User1ID);
        mRef.keepSynced(true);


        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            ArrayList<String>user1QuestionArr = new ArrayList<>();
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    if (User1QArr.contains(childSnapshot.getKey())) {
                        UserQA user1QA = childSnapshot.getValue(UserQA.class);
                        if (!user1QA.getAnswer().equals("skipped")) {
                            user1QuestionArr.add(user1QA.getQuestionId());
                        }

                    }


                }
                compare_btn.setProgress(99);
                getUser2QA(User2Uid, user1QuestionArr);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void getUser2QA(final String User2ID, final ArrayList arr) {
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("UserQA/" + User2ID);
        mRef.keepSynced(true);


        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            ArrayList<String>user2QuestionArr = new ArrayList<>();
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    if (arr.contains(childSnapshot.getKey())) {

                        UserQA user2QA = childSnapshot.getValue(UserQA.class);
                        if (!user2QA.getAnswer().equals("skipped")) {
                            user2QuestionArr.add(user2QA.getQuestionId());
                        }

                    }

                }
                //all the non-skipped questions id both users answered
                arr.retainAll(user2QuestionArr);

                User2QA = new ArrayList<>();
                temp2QA = new ArrayList<>();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    if (arr.contains(childSnapshot.getKey())) {

                        UserQA user2QA = childSnapshot.getValue(UserQA.class);
                        if (!user2QA.getAnswer().equals("skipped")) {
                            User2QA.add(user2QA);
                            temp2QA.add(user2QA);
                        }

                    }

                }
                DatabaseReference mRef2 = FirebaseDatabase.getInstance().getReference("UserQA/" + currentUser.getUid());
                mRef2.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User1QA = new ArrayList<>();
                        temp1QA = new ArrayList<>();
                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                            if (arr.contains(childSnapshot.getKey())) {

                                UserQA user1QA = childSnapshot.getValue(UserQA.class);
                                if (!user1QA.getAnswer().equals("skipped")) {
                                    User1QA.add(user1QA);
                                    temp1QA.add(user1QA);
                                }


                            }


                        }

                        //find questions with the same answer
                        User1QA.retainAll(User2QA);
                        //find questions with different answer for user1,user2
                        temp1QA.removeAll(User1QA);
                        temp2QA.removeAll(User1QA);
                        Log.d("afterQA", User1QA.toString());
                        Log.d("afterQA", String.valueOf(temp1QA.size()));
                        Log.d("afterQA", String.valueOf(temp2QA.size()));

                        score = (int) (((double) User1QA.size() / (double) arr.size()) * 100);

                        compare_btn.setClickable(true);
                        if (User2QArr.isEmpty()) {
                            Toast.makeText(getBaseContext(), mUser.getDisplayname() + " hasn't answered any questions yet", Toast.LENGTH_LONG).show();
                            compare_btn.setProgress(0);
                        } else {
                            dialog = new Dialog(UserProfilePage.this);
                            dialog.setCanceledOnTouchOutside(true);
                            dialog.setContentView(R.layout.score_dialog);
                            TextView textview = dialog.findViewById(R.id.compareText);
                            textview.setText("Compare with " + mUser.getUsername()+"\n\t\t"+User1QA.size()+"/"+User2QA.size());
                            TextView cancel = dialog.findViewById(R.id.cancel_btn);
                            TextView details = dialog.findViewById(R.id.details_btn);
                            arcProgress = dialog.findViewById(R.id.arc_progress);
                            arcProgress.setProgress(score);
                            dialog.show();
                           compare_btn.setProgress(0);
                            cancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.dismiss();
                                    compare_btn.setProgress(0);
                                }
                            });

                            details.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // send user QA data to the result details activity
                                    Intent i = new Intent(getApplicationContext(), ResultActivity.class);
                                    i.putExtra("sameAnswer", User1QA);
                                    i.putExtra("user2", mUser);
                                    i.putExtra("user2Id" ,uid);
                                    i.putExtra("diffAnswer1", temp1QA);
                                    i.putExtra("diffAnswer2", temp2QA);
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.
                                          FLAG_ACTIVITY_BROUGHT_TO_FRONT);

                                    startActivity(i);
                                    overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                                    dialog.dismiss();
                                }
                            });
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

    private void checkFriendStats(){
        DatabaseReference friendStatsRef = database.getReference().child("Friends").
                child(currentUser.getUid()).child(uid).child("Stats");
        friendStatsRef.keepSynced(true);
        friendStatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String stats = dataSnapshot.getValue(String.class);
                if(stats ==null){
                    addFriend_btn.setVisibility(View.VISIBLE);
                    addFriend_btn.setClickable(true);
                    addFriend_btn.setText("Send Friend Request");
                    message_btn.setVisibility(View.VISIBLE);
                    deleteFriend_btn.setVisibility(View.GONE);
                }else if(stats.equals("Pending")){
                    deleteFriend_btn.setVisibility(View.GONE);
                    addFriend_btn.setVisibility(View.VISIBLE);
                    message_btn.setVisibility(View.VISIBLE);
                    addFriend_btn.setText("Respond to Friend Request");
                }else if(stats.equals("Accepted")){
                    addFriend_btn.setVisibility(View.GONE);
                    message_btn.setVisibility(View.VISIBLE);
                    deleteFriend_btn.setVisibility(View.VISIBLE);
                }else{
                    addFriend_btn.setVisibility(View.VISIBLE);
                    addFriend_btn.setClickable(true);
                    addFriend_btn.setText("Send Friend Request");
                    message_btn.setVisibility(View.VISIBLE);
                    deleteFriend_btn.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference friendStatsRef2 = database.getReference().child("Friends").
              child(uid).child(currentUser.getUid()).child("Stats");
        friendStatsRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                 String stats = dataSnapshot.getValue(String.class);
                         if(stats!=null &&  stats.equals("Pending")){
                             deleteFriend_btn.setVisibility(View.GONE);
                             addFriend_btn.setVisibility(View.VISIBLE);
                             message_btn.setVisibility(View.VISIBLE);
                             addFriend_btn.setText("Friend Request Pending");
                             addFriend_btn.setClickable(false);


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void resetQuestions(){
        DatabaseReference resetRef = database.getReference().child("UserQA").child(currentUser.getUid());
        if(uid.equals(currentUser.getUid()) && reset_btn.getVisibility()==View.VISIBLE){
            resetRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Intent intent = new Intent(getApplicationContext(),Homepage.class);
                    Toast.makeText(getApplicationContext(),"Reset Success",Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                    finish();
                }
            });

        }
    }

    private void sendFriendRequest(){
        if( uid !=null && !uid.equals(currentUser.getUid())){
            DatabaseReference setUser2FriendRef = database.getReference().child("Friends").child(uid)
                    .child(currentUser.getUid()).child("Stats");
            setUser2FriendRef.setValue("Pending").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(getApplicationContext(),"Request Sent",Toast.LENGTH_SHORT).show();
                }
            });

            DatabaseReference playerIdRef = database.getReference().child("Notification").child(uid).
                    child("player_id");
            playerIdRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String playerid = dataSnapshot.getValue(String.class);
                  SendNotification.sendFriendRequestNotification(playerid,"Friend Request",  currentUser.getDisplayName()+" send you a friend request");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }
    }

    private void deleteFriend(){
        if(uid!=null && !uid.equals(currentUser.getUid())) {
            DatabaseReference deleteRef = database.getReference().child("Friends").child(currentUser.getUid())
                    .child(uid);
            deleteRef.removeValue();
            DatabaseReference deleteRef2 = database.getReference().child("Friends").child(uid).child(currentUser.getUid());
            deleteRef2.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(getApplicationContext(),"Friend Deleted",Toast.LENGTH_SHORT).show();
                    addFriend_btn.setVisibility(View.VISIBLE);
                    addFriend_btn.setText("Send Friend Request");
                    message_btn.setVisibility(View.VISIBLE);
                    deleteFriend_btn.setVisibility(View.GONE);
                }
            });

        }




    }


    public void qrOnClick(View view) {
     Intent intent = new Intent(getApplicationContext(),ImageViewer.class);
     startActivity(intent);

    }


    @Override
    public void onClick(View view) {
        int id =view.getId();

        if(checkInternet()) {
            if (id == R.id.reset_btn) {
             showBasicDialog("Are you sure to reset all the questions?");
            } else if (id == R.id.compare_btn) {
                if (mUser != null) {
                    compare_btn.setProgress(0);
                    compareWithUser2(mUser);
                }
            } else if (id == R.id.message_btn) {
                if (!uid.equals(currentUser.getUid())) {
                    Intent intent = new Intent(getApplicationContext(), MainChatActivity.class);
                    intent.putExtra("user2Id", uid);
                    intent.putExtra("user2Name", mUser.getDisplayname());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.
                            FLAG_ACTIVITY_REORDER_TO_FRONT );
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.slide_from_right,android.R.anim.fade_out);
                }
            } else if (id == R.id.addFriend_btn) {
                if (addFriend_btn.getText().toString().equals("Send Friend Request")) {
                     sendFriendRequest();
                }else if(addFriend_btn.getText().toString().equals("Respond to Friend Request")){
                    Intent intent = new Intent(getApplicationContext(),FriendRequestPage.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.
                            FLAG_ACTIVITY_REORDER_TO_FRONT );
                    startActivity(intent);
                    finish();
                }
            }else if(id==R.id.deleteFriend_btn){
                 deleteFriend();
            }
        }else{
            Snackbar snackbar = Snackbar
                    .make(profileToolbar, "No Internet Connection", Snackbar.LENGTH_LONG)
                    .setAction("Setting", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startActivity(new Intent(Settings.ACTION_SETTINGS));
                        }
                    });

            snackbar.show();
        }

    }

    private void showBasicDialog(String str){
        new MaterialDialog.Builder(this)
                .content(str).positiveColor(getResources().getColor(R.color.colorAccent))
                .negativeColor(getResources().getColor(R.color.holo_red_light))
                .positiveText("Yes").onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                resetQuestions();
            }
        }).negativeText("No")
                .show();
    }

    public boolean checkInternet() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            return true;
        } else {
            return false;


        }
    }
}
