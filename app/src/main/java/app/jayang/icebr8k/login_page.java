package app.jayang.icebr8k;


import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import app.jayang.icebr8k.Modle.Birthdate;
import app.jayang.icebr8k.Modle.DatePickerFragment;
import app.jayang.icebr8k.Modle.OnDoneListener;
import app.jayang.icebr8k.Modle.User;
import dmax.dialog.SpotsDialog;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;


public class login_page extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener
        {

    private static final String TAG = "loginPage";
    private static final int RC_SIGN_IN = 9001;

    private long lastClickTime =0;
    // [START declare_auth]
    private FirebaseAuth mAuth;
    private GoogleSignInAccount acct;
    // [END declare_auth]
    private RelativeLayout loginPage_Rlayout;
    private FirebaseDatabase mdatabase;
    private FirebaseUser currentUser;
    private DatabaseReference myRef;
    private GoogleApiClient mGoogleApiClient;
    private Intent intent;
    private  DatePickerFragment datePickerFragment;
    private  SpotsDialog loadingdialog;
    private  ScrollView sv;
    private TextInputLayout email_layout,password_layout;
    private TextInputEditText password,email;
    private MaterialDialog userNameDialog,dismissDialog;
    private final String DEFAULT_PHOTO_URL = "https://i.imgur.com/zI4v7oF.png";


            @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        mdatabase = FirebaseDatabase.getInstance();
        myRef = mdatabase.getReference("Users");
        loadingdialog = new SpotsDialog(this,"Signing in...");
        loadingdialog.setCanceledOnTouchOutside(false);

        password =findViewById(R.id.password_login);
        email = findViewById(R.id.email_login);
        email_layout = findViewById(R.id.email_layout_login);
        password_layout = findViewById(R.id.password_layout_login);
        intent = new Intent(this,Homepage.class);
        datePickerFragment = new DatePickerFragment();

        sv =findViewById(R.id.mScroll);
        loginPage_Rlayout = findViewById(R.id.login_page);
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.signup_login).setOnClickListener(this);
        findViewById(R.id.login_btn).setOnClickListener(this);
        findViewById(R.id.sign_in_button).setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                            view.setAlpha(0.5f);
                        }else{
                            view.setAlpha(1.0f);
                        }
                        return false;
                    }
                });

        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                 sv.smoothScrollBy(0,sv.getBottom());
                }
            }
        });



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
        } catch (PackageManager.NameNotFoundException ignored) {

        } catch (NoSuchAlgorithmException e) {

        }

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();








        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,this
                        /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();



    }


    public void onStart() {
        super.onStart();
        loadingdialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                signOut();
            }
        });
        datePickerFragment.setOnDoneListener(new OnDoneListener() {
            @Override
            public void isDone() {
                if(acct!=null){
                    usernameCreateCheck(acct);
                }else{
                    usernameCreateCheck();
                }
                loadingdialog.show();

            }


        });


    }


    @Override
    protected void onPause() {
        super.onPause();

        if(userNameDialog!=null
           && userNameDialog.isShowing() &&mGoogleApiClient.isConnected()){
            userNameDialog.cancel();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();



    }
    @Override
        protected void onDestroy() {
                super.onDestroy();


            }
            public void showToast(String str){
        Toast.makeText(getApplicationContext(),str,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN ) {
            loadingdialog.show();
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);


            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                if (account == null) throw new AssertionError();
                Log.d("loginPage",account.getPhotoUrl()+"from Google");
                firebaseAuthWithGoogle(account);
            }else{
                loadingdialog.dismiss();
            }
        }
    }


    // [START auth_with_google]
    private void firebaseAuthWithGoogle( final GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Firebase Sign in success, update UI with the signed-in user's information
                            Log.d("haha",mAuth.getCurrentUser().getPhotoUrl().
                                    toString()+" from currentUserBeforeUpdate");
                            currentUser = mAuth.getCurrentUser();
                            login_page.this.acct = acct;
                            ageCreateCheck(acct,true);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                           showDismissDialog(task.getException().getMessage());
                            // Google sign out
                            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                                    new ResultCallback<Status>() {

                                        @Override
                                        public void onResult(@NonNull Status status) {

                                        }
                                    });
                           loadingdialog.dismiss();

                        }

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
        // Google sign out
        try {
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {

                        @Override
                        public void onResult(@NonNull Status status) {


                        }
                    });
        }catch (Exception e){

        }

    }

    private void revokeAccess() {


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
        loadingdialog.dismiss();
    }

    public boolean checkFirebasePathError(String str){
                String [] strArr = {".","#","$","[","]","/"};
                for(String s :strArr){
                    if(str.contains(s)){
                        return true;
                    }
                }
                return false;

            }


    public void updateCurrentUser(final User user, final FirebaseUser currentUser){
        if(currentUser!=null ) {
            myRef.child(currentUser.getUid()).setValue(user);
            DatabaseReference userNameRef = mdatabase.getReference("Usernames");
            userNameRef.child(user.getUsername()).setValue(currentUser.getUid());
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(user.getDisplayname())
                    .setPhotoUri(Uri.parse(user.getPhotourl()))
                    .build();

            currentUser.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("loginPage", "profile updated " +currentUser.getPhotoUrl() );
                                if(mGoogleApiClient.isConnected()) {
                                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                                            new ResultCallback<Status>() {

                                                @Override
                                                public void onResult(@NonNull Status status) {
                                                    startActivity(intent);
                                                    loadingdialog.dismiss();
                                                    finish();
                                                    overridePendingTransition(android.R.anim.fade_in, android.
                                                            R.anim.fade_out);
                                                }
                                            });
                                }


                            }else{
                                dismissDialog= showDismissDialog(task.getException().getMessage());
                                dismissDialog.show();
                            }
                        }
                    });
        }else{
            dismissDialog= showDismissDialog("Error Occured, try again");
            dismissDialog.show();
        }

    }


    public void createUsernameDialog(final GoogleSignInAccount account){
        loadingdialog.dismiss();
        MaterialDialog.Builder userNameDialogBuilder = new MaterialDialog.Builder(this)
                .title("Username").maxIconSize(60)
                .content("Create a username for " + mAuth.getCurrentUser().getEmail() + " \n(" +
                        "at least 3 characters)")
                .inputType(InputType.TYPE_CLASS_TEXT).inputRange(3, 20,
                        getResources().
                                getColor(R.color.red_error)).positiveText("Confirm").positiveColor
                        (getResources().getColor(R.color.colorAccent)).negativeText("Discard").
                        negativeColor(getResources().getColor(R.color.red_error)).
                        icon(getDrawable(R.mipmap.ic_launcher));

        userNameDialogBuilder.input("Username", null,
                false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull final MaterialDialog dialog, CharSequence input) {
                        final String username = input.toString();

                        DatabaseReference mRef = mdatabase.getReference().child("Usernames");
                        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                boolean flag = false;
                                for(DataSnapshot usernameSnap:dataSnapshot.getChildren()){
                                    if(usernameSnap.getKey().equals(username)){
                                        flag=true; // Username is not unique in database
                                        break;
                                    }
                                }
                                if(flag){
                                    showToast(getString(R.string.usernameError));
                                     usernameCreateCheck(account);
                                }else if(username.length()>20 ||username.length()<3){
                                    showToast(getString(R.string.usernameError2));
                                    usernameCreateCheck(account);
                                }else if(username.isEmpty()) {
                                    showToast(getString(R.string.emptyfieldError));
                                    usernameCreateCheck(account);
                                }else if(username.contains(" ")) {
                                    showToast(getString(R.string.usernameError3));
                                    usernameCreateCheck(account);
                                }else if(checkFirebasePathError(username)) {
                                        showToast(getString(R.string.firebase_path_error));
                                        usernameCreateCheck(account);
                                }else if(username.trim().matches("")){
                                    showToast(getString(R.string.emptyfieldError));
                                    usernameCreateCheck(account);
                                }else{
                                    User user = new User();
                                    user.setDisplayname(account.getDisplayName());
                                    user.setEmail(account.getEmail());
                                    user.setUsername(username);
                                    user.setPhotourl(DEFAULT_PHOTO_URL);
                                    user.setPrivacy("private");
                                    updateCurrentUser(user, mAuth.getCurrentUser());
                                    dialog.dismiss();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.cancel();
                    }
                }).cancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (mAuth.getCurrentUser() != null) {
                    mAuth.getCurrentUser().delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "User account deleted.");
                                    }
                                }
                            });
                    signOut();
                }
            }
        });

        userNameDialog = userNameDialogBuilder.build();
        userNameDialog.show();
    }
    //check user has username or not
public void usernameCreateCheck(final GoogleSignInAccount account) {
    final DatabaseReference mRef = mdatabase.
            getReference("Users/" + mAuth.getCurrentUser().getUid());

    mRef.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (!dataSnapshot.hasChild("username")) {
                //user doesn't have username
                createUsernameDialog(account);
            } else {
                User user = dataSnapshot.getValue(User.class);
                user.setDisplayname(account.getDisplayName());
                user.setEmail(account.getEmail());
                user.setUsername(dataSnapshot.child("username").getValue(String.class));
                updateCurrentUser(user, mAuth.getCurrentUser());
            }
            mRef.removeEventListener(this);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    });
}

            public void ageCreateCheck(final GoogleSignInAccount account, final Boolean isGoogle) {
                final DatabaseReference mRef = mdatabase.
                        getReference("Users/" + mAuth.getCurrentUser().getUid());

                mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.hasChild("birthdate")) {
                            loadingdialog.dismiss();
                            showDatePickerDialog();

                        } else {
                            if (isGoogle) {
                                usernameCreateCheck(account);
                            } else {
                                usernameCreateCheck();
                            }
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }


            public void showDatePickerDialog() {

                if (datePickerFragment != null) {
                    datePickerFragment.show(getFragmentManager(), "datePicker");
                    Toast.makeText(this, "Please Choose Your Birthdate", Toast.LENGTH_LONG).show();


                }
            }


    //check user from Firebase Sign in has username or not
    public void usernameCreateCheck(){
        final DatabaseReference mRef = mdatabase.
                getReference("Users/"+mAuth.getCurrentUser().getUid());

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild("username")) {
                    //user doesn't have username
                    User user = dataSnapshot.getValue(User.class);
                    user.setDisplayname(currentUser.getDisplayName());
                    user.setEmail(currentUser.getEmail());
                    user.setPhotourl(currentUser.getPhotoUrl().toString());
                    createFirebaesUsernameDialog(user);
                } else {
                    startActivity(intent);
                    finish();
                    overridePendingTransition(android.R.anim.fade_in,android.
                            R.anim.fade_out);
                }
                mRef.removeEventListener(this);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
            public void createFirebaesUsernameDialog(final User user){
                loadingdialog.dismiss();
                MaterialDialog.Builder userNameDialogBuilder = new MaterialDialog.Builder(this)
                        .title("Username").maxIconSize(60)
                        .content("Create a username for " + mAuth.getCurrentUser().getEmail() + " \n(" +
                                "at least 3 characters)")
                        .inputType(InputType.TYPE_CLASS_TEXT).inputRange(3, 20,
                                getResources().
                                        getColor(R.color.red_error)).positiveText("Confirm").positiveColor
                                (getResources().getColor(R.color.colorAccent)).negativeText("Discard").
                                negativeColor(getResources().getColor(R.color.red_error)).
                                icon(getDrawable(R.mipmap.ic_launcher));

                userNameDialogBuilder.input("Username", null,
                        false, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull final MaterialDialog dialog, CharSequence input) {
                                final String username = input.toString();

                                DatabaseReference mRef = mdatabase.getReference().child("Usernames");
                                mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        boolean flag = false;
                                        for(DataSnapshot usernameSnap:dataSnapshot.getChildren()){
                                            if(usernameSnap.getKey().equals(username)){
                                                flag=true; // Username is not unique in database
                                                break;
                                            }
                                        }
                                        if(flag){
                                            showToast(getString(R.string.usernameError));
                                            usernameCreateCheck();
                                        }else if(username.length()>20 ||username.length()<3){
                                            showToast(getString(R.string.usernameError2));
                                            usernameCreateCheck();
                                        }else if(username.isEmpty()) {
                                            showToast(getString(R.string.emptyfieldError));
                                            usernameCreateCheck();
                                        }else if(username.contains(" ")) {
                                            showToast(getString(R.string.usernameError3));
                                            usernameCreateCheck();
                                        }else if(checkFirebasePathError(username)) {
                                            showToast(getString(R.string.firebase_path_error));
                                            usernameCreateCheck();
                                        }else if(username.trim().matches("")){
                                            showToast(getString(R.string.emptyfieldError));
                                            usernameCreateCheck();
                                        }else{
                                            User user2 = new User();
                                            user2.setDisplayname(user.getDisplayname());
                                            user2.setEmail(user.getEmail());
                                            user2.setUsername(username);
                                            user2.setPhotourl(DEFAULT_PHOTO_URL);
                                            user2.setPrivacy("private");
                                            updateCurrentUser(user2, mAuth.getCurrentUser());
                                            dialog.dismiss();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.cancel();
                    }
                }).cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (mAuth.getCurrentUser() != null) {
                            mAuth.getCurrentUser().delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "User account deleted.");
                                            }
                                        }
                                    });
                            signOut();
                        }
                    }
                });

                userNameDialog = userNameDialogBuilder.build();
                userNameDialog.show();
            }



            public void login(String email, String password){
                loadingdialog.show();

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    currentUser = mAuth.getCurrentUser();
                                    if(currentUser.isEmailVerified()){
                                      ageCreateCheck(null,false);
                                    }else{
                                        loadingdialog.dismiss();

                             MaterialDialog materialDialog =new MaterialDialog.Builder(login_page.this)
                                                .content(currentUser.getEmail()+ "\nEmail is not verified by user ")
                                                .negativeColor(getResources().getColor(R.color.colorPrimary))
                                                .positiveText("Okay")
                                                .negativeText("Resend Email").onNegative(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                currentUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()) {
                                                            showToast("Verification email sent to  "+currentUser.getEmail());
                                                        }
                                                        mAuth.signOut();
                                                    }
                                                });
                                            }
                                        }).onPositive(new MaterialDialog.SingleButtonCallback() {
                                         @Override
                                         public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                             mAuth.signOut();
                                         }
                                     }) .show();

                                   materialDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                       @Override
                                       public void onCancel(DialogInterface dialog) {
                                           mAuth.signOut();
                                       }
                                   });

                                    }

                                } else {
                                    showDismissDialog(task.getException().getMessage()).show();
                                    loadingdialog.dismiss();
                                }
                            }
                        });
            }

    public void CheckUserInput(){
        String emailstr = email.getText().toString();
            String passwordStr = password.getText().toString();
               if (!emailstr.contains("@") && !emailstr.isEmpty()) {
                    email_layout.setErrorEnabled(true);
                    email_layout.setError(getString(R.string.emailError));
                    email.requestFocus();
                } else if (emailstr.isEmpty()) {
                    email_layout.setErrorEnabled(true);
                    email_layout.setError(getString(R.string.emptyfieldError));
                    email.requestFocus();

                } else if (passwordStr.isEmpty()) {
                    password_layout.setErrorEnabled(true);
                    password_layout.setError(getString(R.string.emptyfieldError));
                    password.requestFocus();

                } else if (passwordStr.length() < 6 || passwordStr.contains(" ")) {
                    password_layout.setErrorEnabled(true);
                    password_layout.setError(getString(R.string.pwdError2));
                    password.requestFocus();
                }  else {
                    login(emailstr,passwordStr);
                }

            }


    public MaterialDialog showDismissDialog(String Str){
       return  new MaterialDialog.Builder(this)
                .title("Error").titleColor(ContextCompat.getColor(getApplicationContext(),
                R.color.red_error))
                .content(Str)
                .positiveText("okay").build();

    }

    public void showPrivacyPolicy(final Boolean isGoogle) {
        new MaterialDialog.Builder(this)
                .iconRes(R.mipmap.ic_launcher)
                .limitIconToDefaultSize()
                .content(R.string.privacy_policy)
                .title("Privacy Policy")
                .positiveText("Agree")
                .negativeText("Disagree").onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                 addPolicySharedPreference();
                if (isGoogle) {
                    signIn();
                } else {
                    email.clearFocus();
                    password.clearFocus();
                    password_layout.setError(null);
                    email_layout.setError(null);
                    CheckUserInput();
                }

            }
        }).show();
    }


    private  boolean agreedPolicySharedPreference() {
        SharedPreferences prefs = getDefaultSharedPreferences(getApplicationContext());
        boolean defaultValue = false;
        boolean agreedPolicy = prefs.getBoolean("policy", defaultValue);
        return agreedPolicy;
    }

    public void addPolicySharedPreference() {

        SharedPreferences prefs = getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("policy", true);
        editor.commit();

    }










    @SuppressLint("RestrictedApi")
    @Override
    public void onClick(View view) {
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
            return;
        }
        lastClickTime = SystemClock.elapsedRealtime();
        if (view.getId() == R.id.sign_in_button && mAuth.getCurrentUser()==null) {
            if(checkInternet() ) {

                if(agreedPolicySharedPreference()){
                    signIn();
                }else{
                    showPrivacyPolicy(true);
                }

            }else{
                Snackbar snackbar = Snackbar
                        .make(loginPage_Rlayout, "No Internet Connection", Snackbar.LENGTH_LONG)
                        .setAction("Setting", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity(new Intent(Settings.ACTION_SETTINGS));
                            }
                        });

                snackbar.show();
            }

        }else if((view.getId() == R.id.sign_in_button )&& mAuth.getCurrentUser()!=null){

            Toast.makeText(this,"You already sign in with "+
                    mAuth.getCurrentUser().getProviders(),Toast.LENGTH_LONG).show();
        }else if(view.getId() == R.id.signup_login){
            Intent i = new Intent(getApplicationContext(),signup.class);
            startActivity(i);
            overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);

        }else if(view.getId() == R.id.login_btn){

            if(agreedPolicySharedPreference()){
                email.clearFocus();
                password.clearFocus();
                password_layout.setError(null);
                email_layout.setError(null);
                CheckUserInput();
            }else{
                showPrivacyPolicy(false);
            }

        }

    }


    public boolean checkInternet() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }
        return null != activeNetwork;
    }








}

