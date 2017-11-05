package app.jayang.icebr8k;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.dd.processbutton.ProcessButton;
import com.dd.processbutton.iml.ActionProcessButton;
import com.github.lzyzsd.circleprogress.ArcProgress;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
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


public class UserProfilePage extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{
    Toolbar profileToolbar;
    ImageView mImageView;
    Button resetBtn;
    ActionProcessButton mButton;
    TextView displayname_profile, email_profile,username_profile;

    FirebaseDatabase database;
    FirebaseUser currentUser;
    GoogleApiClient mGoogleApiClient;
    User mUser,selfUser;
    Dialog dialog;
    ArcProgress arcProgress;
    String User2Uid;
    ArrayList<String> User1QArr, User2QArr;
    ArrayList<UserQA> User1QA, User2QA;
    ArrayList<UserQA> temp1QA,temp2QA;
    int score;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_page);

        if(FirebaseAuth.getInstance().getCurrentUser()!=null) {
            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Users/" +
                    FirebaseAuth.getInstance().getCurrentUser().getUid());
            mRef.child("onlineStats").setValue("1");
        }

        profileToolbar =  findViewById(R.id.profileToolbar);
        setSupportActionBar(profileToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mImageView =  findViewById(R.id.profileButton);
        mButton =  findViewById(R.id.compareBtn);
        displayname_profile =  findViewById(R.id.displayname_profile);
        email_profile =  findViewById(R.id.email_profile);
        username_profile =  findViewById(R.id.username_profile);
        database = FirebaseDatabase.getInstance();
       currentUser = FirebaseAuth.getInstance().getCurrentUser();
        resetBtn = findViewById(R.id.reset);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // [END configure_signin]

        // [START build_client]
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
         mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        Intent i = getIntent();
        mUser = (User)i.getSerializableExtra("userInfo"); //user2
        selfUser = (User)i.getSerializableExtra("selfProfile");


               if(mUser!=null) {
                   mButton.setMode(ActionProcessButton.Mode.PROGRESS);
                   mButton.setOnClickListener(new View.OnClickListener() {
                       @Override
                       public void onClick(View view) {
                           mButton.setProgress(0);
                           mButton.setClickable(false);
                           compareWithUser2(mUser);

                       }
                   });



               }






        }





       






    @Override
    protected void onStart() {
        super.onStart();
        if(selfUser!=null) {
            updateUI(selfUser);

            mButton.setText("Logout");
            mButton.setBackgroundColor(getResources().getColor(R.color.holo_red_light));
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Signout();


                }
            });
            resetBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("UserQA/"+currentUser.getUid());
                    mRef.removeValue();
                    Toast.makeText(getApplicationContext(),"Reset is done",Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getApplicationContext(),Homepage.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    startActivity(i);

                }
            });
        }else{
            resetBtn.setVisibility(View.GONE);
            updateUI(mUser);
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(FirebaseAuth.getInstance().getCurrentUser()!=null) {
            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Users/" +
                    FirebaseAuth.getInstance().getCurrentUser().getUid());
            mRef.child("onlineStats").setValue("0");
        }
    }

    public void updateUI(User user){
        getSupportActionBar().setTitle(user.getDisplayname()+"'s Profile");
        Glide.with(getBaseContext()).load(user.getPhotourl()).
                apply(RequestOptions.circleCropTransform()).into(mImageView);
        displayname_profile.setText(user.getDisplayname());
        email_profile.setText(user.getEmail());
        username_profile.setText(user.getUsername());

    }
    public void Signout(){
        if(FirebaseAuth.getInstance().getCurrentUser()!=null) {
            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Users/" + currentUser.getUid());
            mRef.child("onlineStats").setValue("0");
        }
        FirebaseAuth.getInstance().signOut();





            if(currentUser.getProviders().get(0).contains("google")) {

            // Google sign out
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {

                        @Override
                        public void onResult(@NonNull Status status) {

                        }
                    });
        }
        Intent intent = new Intent(UserProfilePage.this,login_page.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public boolean onSupportNavigateUp() {
       finish();
       return  true;
    }


 public int compareWithUser2(User user2) {
     DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Usernames/" + user2.getUsername());
     mRef.addListenerForSingleValueEvent(new ValueEventListener() {
         @Override
         public void onDataChange(DataSnapshot dataSnapshot) {
             User2Uid = dataSnapshot.getValue(String.class);
             mButton.setProgress(20);
             Log.d("user2", User2Uid);
             pullUser2Q(User2Uid);

         }


         @Override
         public void onCancelled(DatabaseError databaseError) {

         }
     });
     return score;

 }
 public void pullUser1Q(String Uid, final ArrayList arrayList){
     DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("UserQA/"+Uid);
     mRef.addListenerForSingleValueEvent(new ValueEventListener() {
         @Override
         public void onDataChange(DataSnapshot dataSnapshot) {
             User1QArr = new ArrayList<String>();
             for(DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                String key = childSnapshot.getKey();
               User1QArr.add(key);
                 Log.d("key",key);
             }
           User1QArr.retainAll(arrayList);
             mButton.setProgress(60);
             Log.d("arr",User1QArr.toString());
             getUser1QA(FirebaseAuth.getInstance().getCurrentUser().getUid());
         }

         @Override
         public void onCancelled(DatabaseError databaseError) {

         }
     });

 }

    public void pullUser2Q(String Uid){
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("UserQA/"+Uid);
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                User2QArr = new ArrayList<String>();
                for(DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                    String key = childSnapshot.getKey();
                    User2QArr.add(key);
                    Log.d("key2",key);
                }
                mButton.setProgress(40);
                pullUser1Q(FirebaseAuth.getInstance().getCurrentUser().getUid(),User2QArr);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void getUser1QA(final String User1ID){
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("UserQA/"+User1ID);

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User1QA = new ArrayList<>();
                temp1QA =new ArrayList<>();
                for(DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                    if(User1QArr.contains(childSnapshot.getKey())) {
                        UserQA user1QA = childSnapshot.getValue(UserQA.class);
                        User1QA.add(user1QA);
                        temp1QA.add(user1QA);

                    }

                }
                mButton.setProgress(99);
                Log.d("map", User1QA.toString());
                getUser2QA(User2Uid,User1QArr);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void getUser2QA(final String User2ID, final ArrayList arr){
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("UserQA/"+User2ID);

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User2QA = new ArrayList<>();
                temp2QA = new ArrayList<>();
                for(DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                    if(arr.contains(childSnapshot.getKey())) {

                        UserQA user2QA = childSnapshot.getValue(UserQA.class);
                        User2QA.add(user2QA);
                        temp2QA.add(user2QA);

                    }

                }

                //find questions with the same answer
                User1QA.retainAll(User2QA);
                //find questions with different answer
                temp1QA.removeAll(User1QA);
                temp2QA.removeAll(User1QA);
                Log.d("afterQA", User1QA.toString());

                Log.d("map2", User2QA.toString());
                score = (int)(((double)User1QA.size()/(double)User1QArr.size())*100);

                mButton.setClickable(true);
                        if(User2QArr.isEmpty()){
                            Toast.makeText(getBaseContext(),mUser.getDisplayname() +" hasn't answered any questions yet",Toast.LENGTH_LONG).show();
                            mButton.setProgress(0);
                        }else{
                            dialog = new Dialog(UserProfilePage.this);
                            dialog.setCanceledOnTouchOutside(true);
                            dialog.setContentView(R.layout.score_dialog);
                            TextView textview = dialog.findViewById(R.id.compareText);
                            textview.setText("Compare with "+ mUser.getUsername());
                            TextView cancel = dialog.findViewById(R.id.cancel_btn);
                            TextView details = dialog.findViewById(R.id.details_btn);
                            arcProgress = dialog.findViewById(R.id.arc_progress);
                            arcProgress.setProgress(score);
                            dialog.show();
                            mButton.setProgress(0);
                            cancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.dismiss();
                                    mButton.setProgress(0);
                                }
                            });

                            details.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent i = new Intent(getApplicationContext(),ResultActivity.class);
                                    i.putExtra("sameAnswer",User1QA);
                                    i.putExtra("user2",mUser);
                                    i.putExtra("diffAnswer1",temp1QA);
                                    i.putExtra("diffAnswer2",temp2QA);

                                    startActivity(i);
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



}
