package app.jayang.icebr8k;

import android.app.Dialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.processbutton.ProcessButton;


import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import app.jayang.icebr8k.Modle.User;
import dmax.dialog.SpotsDialog;


public class login_page extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;

    private ProgressBar mProgressBar;
    private  Dialog dialog;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]
    private FirebaseDatabase mdatabase;
    private DatabaseReference myRef;

    private GoogleApiClient mGoogleApiClient;
    private TextView email,username;
    private TextInputEditText input;
    private ArrayList<String> usernameList;
    private Intent intent;
    private String user2Id;
    private User user2;
    private  SpotsDialog loadingdialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        user2Id = getIntent().getExtras().getString("user2Id");
        user2 =(User)getIntent().getExtras().getSerializable("user2");

        intent = new Intent(this, Homepage.class);





        mdatabase = FirebaseDatabase.getInstance();
        myRef = mdatabase.getReference("Users");
        usernameList = new ArrayList<>();
        loadingdialog = new SpotsDialog(this,"Signing in...");




//get shai key;
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "app.jayang.icebr8k",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
        mAuth = FirebaseAuth.getInstance();;
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            loadingdialog.dismiss();
            startActivity(intent);
            finish();
        }



// ...



        //setup click Listener

         findViewById(R.id.sign_in_button).setOnClickListener(this);







        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,this/* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        //facebook callback


    }

    //facebook sigin onresult callback





    public void onStart() {
        super.onStart();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase

                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);


            } else {
                // Google Sign In failed, update UI appropriately
                // ...

            }
        }
    }






    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        //showProgressDialog();
        // [END_EXCLUDE]
        loadingdialog.show();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            Log.d(TAG, "signInWithCredential:success");
                            usernameCreateCheck();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getBaseContext(), task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();

                        }

                        // [START_EXCLUDE]
                       // hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }



    // [START signin]
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }
    // [END signin]

    private void signOut() {
        // Firebase sign out

        mAuth.signOut();
        Toast.makeText(getApplicationContext(),"signed out",Toast.LENGTH_LONG).show();


        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {

                    @Override
                    public void onResult(@NonNull Status status) {

                    }
                });

    }


    private void revokeAccess() {
        // Firebase sign out
        mAuth.signOut();

        // Google revoke access
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {

                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    // link the user info to the google firebase
    public void updateDatabase(FirebaseUser user){
        Map<String, Object> userInfo = new HashMap<>();

        userInfo.put("displayname",user.getDisplayName());
        userInfo.put("email",user.getEmail());
        userInfo.put("photourl",user.getPhotoUrl().toString());
        myRef.child(user.getUid()).updateChildren(userInfo);

        Log.d("Database123",user.getEmail()+user.getDisplayName());
    }

    public void createUsernameDialog(FirebaseUser user){
        createUsernameList();
        input = new TextInputEditText(this);
        input.setHint("Username");
        input.setSingleLine(true);
        final DatabaseReference mRef = mdatabase.getReference("Users/"+user.getUid());
         dialog = new Dialog(this);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.username_dialog);
        dialog.setTitle(user.getEmail());
        dialog.show();

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                FirebaseAuth.getInstance().getCurrentUser().delete();
              signOut();
              loadingdialog.dismiss();
              dialog.dismiss();
            }
        });
        final TextInputEditText mEdittext = (TextInputEditText) dialog.findViewById(R.id.username_edittext);
        TextView title =dialog.findViewById(R.id.dialog_title);
        title.setText(user.getEmail());
        final ProcessButton confirmbtn = dialog.findViewById(R.id.btnConfirm);
        final ProcessButton discardbtn = dialog.findViewById(R.id.btnDiscard);
        confirmbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               String user_username = mEdittext.getText().toString();

                if(user_username.isEmpty()) {
                    mEdittext.setError(getString(R.string.error_field_required));
                    mEdittext.requestFocus();
                }else if(usernameList.contains(user_username)) {
                    mEdittext.setError("Username already exist");
                    mEdittext.requestFocus();

                }else if(user_username.contains(" ")) {
                    mEdittext.setError(getString(R.string.error_field_nospace));
                    mEdittext.requestFocus();
                }else if(user_username.length()<5){

                    mEdittext.setError(getString(R.string.error_invalid_username));
                    mEdittext.requestFocus();
                }else{
                    FirebaseUser user = mAuth.getCurrentUser();
                    updateDatabase(user);
                    HashMap<String,Object> hashMap = new HashMap<>();
                    HashMap<String,Object> hashMap2 = new HashMap<>();
                    hashMap.put("username",user_username);
                    hashMap2.put(user_username,mAuth.getCurrentUser().getUid());
                    mRef.updateChildren(hashMap);
                    DatabaseReference databaseReference = mdatabase.getReference();
                    databaseReference.child("Usernames").updateChildren(hashMap2);

                    dialog.dismiss();
                    startActivity(intent);
                    loadingdialog.dismiss();
                    finish();

                }
            }
        });

        discardbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                loadingdialog.dismiss();

                FirebaseAuth.getInstance().getCurrentUser().delete();
                signOut();

            }
        });






    }
public void usernameCreateCheck(){
    DatabaseReference mRef = mdatabase.getReference("Users/"+mAuth.getCurrentUser().getUid()+"/username");

    mRef.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.getValue() == null) {
                createUsernameDialog(mAuth.getCurrentUser());
            } else {

                Log.d("dialog", dataSnapshot.getValue().toString());
                startActivity(intent);
                loadingdialog.dismiss();
                finish();
            }
        }


        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    });
}

public void createUsernameList(){
    usernameList.clear();
    DatabaseReference databaseReference = mdatabase.getReference("Usernames");
    databaseReference.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            for(DataSnapshot usernameShot :dataSnapshot.getChildren()){
                String username = usernameShot.getKey();
                Log.d("Key",username);
                usernameList.add(username);



            }
            Log.d("Arraylist",String.valueOf(usernameList.size()));
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    });

}


    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.sign_in_button && mAuth.getCurrentUser()==null) {

                signIn();

        }else if((view.getId() == R.id.sign_in_button )&& mAuth.getCurrentUser()!=null){

            Toast.makeText(this,"You already sign in with "+ mAuth.getCurrentUser().getProviders(),Toast.LENGTH_LONG).show();
        }
       



    }


}

