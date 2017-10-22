package app.jayang.icebr8k;

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
import com.facebook.login.LoginManager;
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


public class UserProfilePage extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{
    Toolbar profileToolbar;
    ImageView mImageView;
    Button mButton;
    TextView displayname_profile, email_profile,username_profile;

    FirebaseDatabase database;
    DatabaseReference reference;
    FirebaseUser currentUser;
    GoogleApiClient mGoogleApiClient;
    User mUser;
    String Uid;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_page);
        profileToolbar =  findViewById(R.id.profileToolbar);
        setSupportActionBar(profileToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mImageView =  findViewById(R.id.profileButton);
        mButton =  findViewById(R.id.compareBtn);
        displayname_profile =  findViewById(R.id.displayname_profile);
        email_profile =  findViewById(R.id.email_profile);
        username_profile =  findViewById(R.id.username_profile);
        database = FirebaseDatabase.getInstance();
       currentUser = FirebaseAuth.getInstance().getCurrentUser();
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
        mUser = (User)i.getSerializableExtra("userInfo");
        reference = database.getReference("Usernames/"+mUser.getUsername());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Uid = dataSnapshot.getValue(String.class);
                Log.d("uid",Uid);
                String currentUID = currentUser.getUid();
                if(Uid.compareTo(currentUID)==0){
                    mButton.setText("Logout");
                    mButton.setBackgroundColor(getResources().getColor(R.color.holo_red_light));
                    mButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Signout();

                            Intent intent = new Intent(view.getContext(),login_page.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);

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
    protected void onStart() {
        super.onStart();
        updateUI(mUser);


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
        finish();
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
