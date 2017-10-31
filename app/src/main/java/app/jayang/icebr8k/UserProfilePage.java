package app.jayang.icebr8k;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.login.LoginManager;
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
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.R.id.progress;


public class UserProfilePage extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{
    Toolbar profileToolbar;
    ImageView mImageView;
    Button mButton,resetBtn;
    TextView displayname_profile, email_profile,username_profile;

    FirebaseDatabase database;
    DatabaseReference reference;
    FirebaseUser currentUser;
    GoogleApiClient mGoogleApiClient;
    User mUser,selfUser;
    Dialog dialog;
    ArcProgress arcProgress;
    String User1Uid, User2Uid;
    ArrayList<String> User1QArr, User2QArr ,CommonQarr;
    ArrayList<HashMap> User1QA, User2QA,CommonQA;
    int score;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_page);
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
                 compareWithUser2(mUser);


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

    public void updateUI(User user){
        getSupportActionBar().setTitle(user.getDisplayname()+"'s Profile");
        Glide.with(getBaseContext()).load(user.getPhotourl()).
                apply(RequestOptions.circleCropTransform()).into(mImageView);
        displayname_profile.setText(user.getDisplayname());
        email_profile.setText(user.getEmail());
        username_profile.setText(user.getUsername());

    }
    public void Signout(){
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


 public void compareWithUser2(User user2) {
     DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Usernames/" + user2.getUsername());
     mRef.addListenerForSingleValueEvent(new ValueEventListener() {
         @Override
         public void onDataChange(DataSnapshot dataSnapshot) {
             User2Uid = dataSnapshot.getValue(String.class);
             Log.d("user2", User2Uid);
             pullUser2Q(User2Uid);

         }


         @Override
         public void onCancelled(DatabaseError databaseError) {

         }
     });

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
             Log.d("arr",User1QArr.toString());
             getUser1QA(currentUser.getUid());
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
                pullUser1Q(currentUser.getUid(),User2QArr);


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
                for(DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                    if(User1QArr.contains(childSnapshot.getKey())) {

                        HashMap<String,Object> map =  new HashMap<String, Object>();

                                map.put(childSnapshot.getKey(),childSnapshot.getValue());
                        User1QA.add(map);

                    }

                }
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
                for(DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                    if(arr.contains(childSnapshot.getKey())) {

                        HashMap<String,Object> map =  new HashMap<String, Object>();

                        map.put(childSnapshot.getKey(),childSnapshot.getValue());
                        User2QA.add(map);

                    }

                }
                User1QA.retainAll(User2QA);
                Log.d("afterQA", User1QA.toString());

                Log.d("map2", User2QA.toString());
                score = (int)(((double)User1QA.size()/(double)User1QArr.size())*100);
                mButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(User2QArr.isEmpty()){
                            Toast.makeText(getBaseContext(),mUser.getDisplayname() +" hasn't answered any questions yet",Toast.LENGTH_LONG).show();

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
                            cancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.dismiss();
                                }
                            });

                            details.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent i = new Intent(getApplicationContext(),ResultActivity.class);
                                    startActivity(i);
                                    dialog.dismiss();
                                }
                            });
                        }


                    }

                });

/********/




            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }





}
