package app.jayang.icebr8k;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.transition.Fade;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.dd.processbutton.FlatButton;
import com.dd.processbutton.iml.ActionProcessButton;
import com.github.lzyzsd.circleprogress.ArcProgress;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import app.jayang.icebr8k.Modle.User;
import app.jayang.icebr8k.Modle.UserMessage;
import app.jayang.icebr8k.Modle.UserQA;
import app.jayang.icebr8k.Utility.Compatability;
import app.jayang.icebr8k.Utility.SendNotification;
import id.zelory.compressor.Compressor;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;


public class UserProfilePage extends SwipeBackActivity implements View.OnClickListener,View.OnTouchListener {
    Toolbar profileToolbar;
    ImageView mImageView,qrImage;
    ActionProcessButton  compare_btn;
    ArcProgress mArcProgress;
    FlatButton message_btn,addFriend_btn,deleteFriend_btn,reset_btn;
    TextView displayname_profile, username_profile;
    MaterialDialog mProgressDialog;
    SwipeBackLayout mSwipeBackLayout;
    private long lastClickTime = 0;
    DatabaseReference senderMessageRef, receiverMessageRef;

    FirebaseDatabase database;
    FirebaseUser currentUser;
    DatabaseReference mRef;
    User mUser;
    String uid;
    Dialog dialog;
    boolean firstTime  = true;
    ProgressBar mProgressBar;


    private Compatability mCompatability;
    int score;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_page);
        mRef= FirebaseDatabase.getInstance().getReference();
        mProgressDialog =  new MaterialDialog.Builder(this)
                .content("Updating Avatar...")
                .progress(true, 0).build();
        mSwipeBackLayout =getSwipeBackLayout();
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);


        profileToolbar = (Toolbar) findViewById(R.id.profileToolbar);
        setSupportActionBar(profileToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mImageView = (ImageView) findViewById(R.id.profileButton);
        qrImage = (ImageView) findViewById(R.id.profile_QR);
        compare_btn = (ActionProcessButton) findViewById(R.id.compare_btn);
        addFriend_btn = (FlatButton) findViewById(R.id.addFriend_btn);
        message_btn = (FlatButton) findViewById(R.id.message_btn);
        deleteFriend_btn = (FlatButton) findViewById(R.id.deleteFriend_btn);
        reset_btn = (FlatButton) findViewById(R.id.reset_btn);
        displayname_profile = (TextView) findViewById(R.id.displayname_profile);
        username_profile = (TextView) findViewById(R.id.username_profile);
        mArcProgress = (ArcProgress) findViewById(R.id.arc_progress);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        database = FirebaseDatabase.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        compare_btn.setOnClickListener(this);
        addFriend_btn.setOnClickListener(this);
        addFriend_btn.setOnTouchListener(UserProfilePage.this);
        message_btn.setOnClickListener(this);
        deleteFriend_btn.setOnClickListener(this);
        reset_btn.setOnClickListener(this);
        mImageView.setOnTouchListener(this);
        mImageView.setOnClickListener(this);








        Intent i = getIntent();
        /*******Required extras *******************************/
        if(i!=null) {
            mUser = (User) i.getSerializableExtra("userInfo"); //user2
            uid = i.getStringExtra("userUid");
            senderMessageRef = FirebaseDatabase.getInstance().getReference().child("UserMessages").child(currentUser.getUid()).child(uid);
            receiverMessageRef = FirebaseDatabase.getInstance().getReference().child("UserMessages").child(uid).child(currentUser.getUid());
        }



        // uid is not my self
        if (mUser != null && uid!=null &&! uid.equals(currentUser.getUid())) {
            compare_btn.setVisibility(View.VISIBLE);
            reset_btn.setVisibility(View.GONE);
            qrImage.setVisibility(View.GONE);
            compare_btn.setMode(ActionProcessButton.Mode.PROGRESS);
            updateUI(mUser);
            checkFriendStats();
            final DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("UserQA/" + currentUser.getUid());
            final DatabaseReference mRef2 = FirebaseDatabase.getInstance().getReference("UserQA/" + uid);

            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    compareWithUser2();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            mRef2.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    compareWithUser2();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });





            // uid is currentuser
        }else if(mUser != null && uid!=null && uid.equals(currentUser.getUid())){
            qrImage.setVisibility(View.GONE);
            compare_btn.setVisibility(View.GONE);
            addFriend_btn.setVisibility(View.GONE);
            message_btn.setVisibility(View.GONE);
            deleteFriend_btn.setVisibility(View.GONE);
            reset_btn.setVisibility(View.VISIBLE);
            updateUI(mUser);
            mImageView.setOnClickListener(this);
        }


    }


    @Override
    protected void onStart() {
        super.onStart();
        updateUI(mUser);



    }


    public void updateUI(User user) {
        getSupportActionBar().setTitle("");

        Glide.with(getBaseContext()).load(user.getPhotourl()).
                apply(RequestOptions.circleCropTransform()).into(mImageView);
        displayname_profile.setText(user.getDisplayname());
        username_profile.setText(user.getUsername());

    }






    @Override
    public boolean onSupportNavigateUp() {
        supportFinishAfterTransition();
        return true;
    }


    public void compareWithUser2() {
        if(firstTime){
            mProgressBar.setVisibility(View.VISIBLE);
            mArcProgress.setVisibility(View.INVISIBLE);
        }

        final ArrayList<UserQA> userQA1 = new ArrayList<>();
        final ArrayList<UserQA> userQA2 = new ArrayList<>();
        final DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("UserQA/" + currentUser.getUid());
        final DatabaseReference mRef2 = FirebaseDatabase.getInstance().getReference("UserQA/" + uid);

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

                       mCompatability = new Compatability(userQA1,userQA2);
                       mProgressBar.setVisibility(View.GONE);
                        score = mCompatability.getScore();

                      if(score<20){
                         mArcProgress.setTextColor(ContextCompat.getColor(UserProfilePage.this,R.color.holo_red_light));
                         mArcProgress.setFinishedStrokeColor(ContextCompat.getColor(UserProfilePage.this,R.color.holo_red_light));
                      }else if(score<50){
                          mArcProgress.setTextColor(ContextCompat.getColor(UserProfilePage.this,R.color.orange_500));
                          mArcProgress.setFinishedStrokeColor(ContextCompat.getColor(UserProfilePage.this,R.color.orange_500));
                      }else if(score<80) {
                          mArcProgress.setTextColor(ContextCompat.getColor(UserProfilePage.this, R.color.colorPrimary));
                          mArcProgress.setFinishedStrokeColor(ContextCompat.getColor(UserProfilePage.this,R.color.colorPrimary));
                      }else if (score >=80){
                          mArcProgress.setTextColor(ContextCompat.getColor(UserProfilePage.this, R.color.colorAccent));
                          mArcProgress.setFinishedStrokeColor(ContextCompat.getColor(UserProfilePage.this,R.color.colorAccent));
                      }
                       mArcProgress.setProgress(score);
                      mArcProgress.setVisibility(View.VISIBLE);
                      firstTime = false;

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


/*
    private void setProgressDialog(int Score){

        Handler mHandler = new Handler();

        dialog = new Dialog(UserProfilePage.this);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.score_dialog);
        TextView textview = dialog.findViewById(R.id.compareText);
        textview.setText("Compare with " + mUser.getUsername()+"\n\t\t"+mCompatability.getCommonList().size()+"/"+mCompatability.getCommonQ());
        TextView cancel = dialog.findViewById(R.id.cancel_btn);
        TextView details = dialog.findViewById(R.id.details_btn);
        arcProgress = dialog.findViewById(R.id.arc_progress);
        dialog.show();
        compare_btn.setProgress(0);
        arcProgress.setProgress(0);

        for(int i =0 ; i<Score+1;i++){
            final int finalI = i;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    arcProgress.setProgress(finalI);
                }
            },20*i);
        }


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

            }
        });
        if(mCompatability.getCommonQ()==0){
            details.setClickable(false);
            details.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.ripple));
            details.setEnabled(false);
        }else{
            details.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // send user QA data to the result details activity
                    Intent i = new Intent(getApplicationContext(), ResultActivity.class);
                    i.putExtra("sameAnswer", mCompatability.getCommonList());
                    i.putExtra("user2", mUser);
                    i.putExtra("user2Id" ,uid);
                    i.putExtra("diffAnswer1", mCompatability.getDiffList());
                    i.putExtra("diffAnswer2", mCompatability.getDiffList2());
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.
                            FLAG_ACTIVITY_BROUGHT_TO_FRONT);

                    startActivity(i);
                    overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                    dialog.dismiss();
                }
            });
        }

    }
*/

    private void checkFriendStats(){
        DatabaseReference friendStatsRef = database.getReference().child("UserFriends").
                child(currentUser.getUid()).child(uid).child("stats");
        friendStatsRef.keepSynced(true);
        friendStatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String stats = dataSnapshot.getValue(String.class);
                if(stats ==null){
                    addFriend_btn.setVisibility(View.VISIBLE);
                    addFriend_btn.setAlpha(1f);
                    addFriend_btn.setClickable(true);
                    addFriend_btn.setText("Send Friend Request");
                    addFriend_btn.setBackgroundColor(getResources().getColor(R.color.colorAccent));

                    message_btn.setVisibility(View.VISIBLE);
                    deleteFriend_btn.setVisibility(View.GONE);
                }else if(stats.equals("pending")){
                    deleteFriend_btn.setVisibility(View.GONE);
                    addFriend_btn.setVisibility(View.VISIBLE);
                    message_btn.setVisibility(View.VISIBLE);
                    addFriend_btn.setText("Respond to Friend Request");
                    addFriend_btn.setOnTouchListener(UserProfilePage.this);
                    addFriend_btn.setBackgroundColor(getResources().getColor(R.color.darkOrange));
                }else if(stats.equals("accepted")){
                    addFriend_btn.setVisibility(View.GONE);
                    message_btn.setVisibility(View.VISIBLE);
                    deleteFriend_btn.setVisibility(View.VISIBLE);
                }else{
                    addFriend_btn.setVisibility(View.VISIBLE);
                    addFriend_btn.setClickable(true);
                    addFriend_btn.setText("Send Friend Request");
                    addFriend_btn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    addFriend_btn.setOnTouchListener(UserProfilePage.this);
                    message_btn.setVisibility(View.VISIBLE);
                    deleteFriend_btn.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference friendStatsRef2 = database.getReference().child("UserFriends").
              child(uid).child(currentUser.getUid()).child("stats");
        friendStatsRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                 String stats = dataSnapshot.getValue(String.class);
                         if(stats!=null &&  stats.equals("pending")){
                             deleteFriend_btn.setVisibility(View.GONE);
                             addFriend_btn.setVisibility(View.VISIBLE);
                             message_btn.setVisibility(View.VISIBLE);
                             addFriend_btn.setText("Friend Request Pending");
                             addFriend_btn.setBackgroundColor(getResources().getColor(R.color.darkOrange));
                             addFriend_btn.setClickable(false);
                             addFriend_btn.setAlpha(0.6f);
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

    private void sendFriendRequest() {
        if (uid != null && !uid.equals(currentUser.getUid())) {
            DatabaseReference setUser2FriendRef = database.getReference().child("UserFriends").child(uid)
                    .child(currentUser.getUid()).child("stats");
            setUser2FriendRef.setValue("pending").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    SendNotification.sendFriendRequestNotification(uid,"Friend Request",  currentUser.getDisplayName()+" send you a friend request.");
                    Toast.makeText(getApplicationContext(), "Request Sent", Toast.LENGTH_SHORT).show();
                }
            });


        }
    }

    private void deleteFriend(){
        if(uid!=null && !uid.equals(currentUser.getUid())) {
            DatabaseReference deleteRef = database.getReference().child("UserFriends").child(currentUser.getUid())
                    .child(uid);
            deleteRef.removeValue();
            DatabaseReference deleteRef2 = database.getReference().child("UserFriends").child(uid).child(currentUser.getUid());
            deleteRef2.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(getApplicationContext(),"Friend Deleted",Toast.LENGTH_SHORT).show();
                    addFriend_btn.setVisibility(View.VISIBLE);

                    addFriend_btn.setText("Send Friend Request");
                    addFriend_btn.setBackgroundColor(getResources().getColor(R.color.colorAccent));

                    message_btn.setVisibility(View.VISIBLE);
                    deleteFriend_btn.setVisibility(View.GONE);
                }
            });

        }




    }


    public void qrOnClick(View view) {
     Intent intent = new Intent(getApplicationContext(),MyQR_Code.class);
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(this, findViewById(R.id.profile_QR), "qr_transition");
        startActivity(intent,options.toBundle());

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

    public void showDismissDialog(String Str){
        new MaterialDialog.Builder(this)
                .title("Error").titleColor(getResources().getColor(R.color.red_error))
                .content(Str)
                .positiveText("okay")
                .show();
    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        EasyImage.handleActivityResult(requestCode, resultCode, data,
                this, new DefaultCallback() {
                    @Override
                    public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                        CropImage.activity(Uri.fromFile(imageFile)).setCropShape
                                (CropImageView.CropShape.RECTANGLE).setFixAspectRatio(true).
                                setAutoZoomEnabled(false)
                                .start(UserProfilePage.this);
                    }
                });
        //add cropped image on avatar imageview
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                //uri to bitmap
                try {
                    File imageFile = new File(result.getUri().getPath());
                    Bitmap avatarBitmap = new Compressor(this).compressToBitmap(imageFile);
                   mProgressDialog.show();
                    uploadImage(avatarBitmap,currentUser);

                } catch (IOException e) {
                    showToast(e.getMessage());
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                showToast(error.getMessage());
            }
        }
    }



    public void uploadImage(Bitmap bitmap, final FirebaseUser currentUser){
        String filename = UUID.randomUUID().toString()+".JPEG";
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference avatarRef = storage.getReference().child("UserAvatars/"+filename);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream);
        // Get the data from an ImageView as bytes
        UploadTask uploadTask = avatarRef.putBytes(stream.toByteArray());
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                showDismissDialog(exception.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type,
                // and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                updateDatabaseAndCurrentUser(downloadUrl.toString());
            }
        });


    }

    public void updateDatabaseAndCurrentUser(final String photoUrl){

        mRef.child("Users").child(currentUser.getUid()).child("photourl").setValue(photoUrl);
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(Uri.parse(photoUrl))
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mProgressDialog.dismiss();
                            Glide.with(getApplicationContext()).load(photoUrl).
                                    apply(RequestOptions.circleCropTransform()).into(mImageView);
                            showToast("Avatar Updated ");

                        }else{
                            mProgressDialog.dismiss();
                            showDismissDialog(task.getException().getMessage());
                        }
                    }
                });

    }



    public void showToast(String str){
        Toast.makeText(getApplicationContext(),str,Toast.LENGTH_LONG).show();
    }

    public void showGreetingDialog(){
        new MaterialDialog.Builder(this)
                .title("Greeting Message")
                .inputType(InputType.TYPE_CLASS_TEXT ).inputRange(0,30)
                .input("(Optional)", null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        if(!input.toString().trim().isEmpty()){
                            String text = input.toString();
                          UserMessage message = new UserMessage(text, currentUser.getUid(),
                                    "text", UUID.randomUUID().toString().replaceAll("-", ""),
                                    new Date().getTime());
                          updateMessagetoFirebase(message);
                        }
                        sendFriendRequest();
                    }
                }).show();
    }

    private void updateMessagetoFirebase(final UserMessage message) {
        senderMessageRef.child("chathistory").
                child(message.getMessageid()).setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                receiverMessageRef.child("chathistory").
                        child(message.getMessageid()).setValue(message);
                setLastMessageNode(message);
                setUnread();

            }
        });
    }

    private void setUnread(){
        final DatabaseReference checkUnreadRef = receiverMessageRef.child("unread");
        checkUnreadRef.keepSynced(true);
        checkUnreadRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer unRead = dataSnapshot.getValue(Integer.class);
                if(unRead==null){
                    checkUnreadRef.setValue(1);
                }else{
                    checkUnreadRef.setValue(++unRead);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void setLastMessageNode(UserMessage message){
        DatabaseReference lastMegNode = senderMessageRef.child("lastmessage");
        lastMegNode.setValue(message);
        DatabaseReference lastmessage2 = receiverMessageRef.child("lastmessage");
        lastmessage2.setValue(message);
    }









    @Override
    public void onClick(View view) {
        int id =view.getId();
        // preventing double, using threshold of 1000 ms
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
            return;
        }
        lastClickTime = SystemClock.elapsedRealtime();
        if(checkInternet()) {
            if (id == R.id.reset_btn) {
             showBasicDialog("Are you sure to reset all the questions ?");
            } else if (id == R.id.compare_btn) {
                if (mUser != null) {
                    compareWithUser2();
                }
            } else if (id == R.id.message_btn) {
                if (!uid.equals(currentUser.getUid())) {
                    Intent intent = new Intent(getApplicationContext(), UserChatActvity.class);
                    intent.putExtra("chatId", uid);
                    intent.putExtra("chatName", mUser.getDisplayname());
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_from_right,android.R.anim.fade_out);
                }
            } else if (id == R.id.addFriend_btn) {
                if (addFriend_btn.getText().toString().equals("Send Friend Request")) {
                     showGreetingDialog();
                }else if(addFriend_btn.getText().toString().equals("Respond to Friend Request")){
                    Intent intent = new Intent(getApplicationContext(),FriendRequestPage.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_NEW_TASK );
                    startActivity(intent);
                }
            }else if(id==R.id.deleteFriend_btn){
                new MaterialDialog.Builder(this)
                        .content("Are you sure to unfriend "+mUser.getDisplayname()+" ?").positiveColor(getResources().getColor(R.color.colorAccent))
                        .negativeColor(getResources().getColor(R.color.holo_red_light))
                        .positiveText("Yes").onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        deleteFriend();
                    }
                }).negativeText("No")
                        .show();

            }else if(id == R.id.profileButton ){
                    Intent i = new Intent(getApplicationContext(),FullImageView.class);
                    i.putExtra("photoUrl",mUser.getPhotourl());
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(this, findViewById(R.id.profileButton), "profile");

                    startActivity(i,options.toBundle());

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

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
           view.setAlpha(.6f);
        }else{
            view.setAlpha(1f);
        }
        return false;
    }



}


