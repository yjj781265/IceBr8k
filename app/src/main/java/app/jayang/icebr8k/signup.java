package app.jayang.icebr8k;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import app.jayang.icebr8k.Modle.User;
import dmax.dialog.SpotsDialog;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;


public class signup extends SwipeBackActivity {
    private final String DEFAULT_PHOTO_URL = "https://firebasestorage.googleapis.com/v0/b/" +
            "icebr8k-98675.appspot.com/o/UserAvatars%2Fdefault_avatar.png?alt=media&token" +
            "=ccbf30ce-5cfb-493a-8c28-8bf7ee18cc9a";
    private ImageView avatar;
    private ScrollView sv;
    private Boolean flag,defaultPhotoFlag;
    private TextInputEditText email,password,username,password2,displayname;
    private TextInputLayout email_layout,username_layout,password_layout,displayname_layout,
            password2_layout;
    private Toolbar mToolbar;
    private SpotsDialog loadingdialog;
    private MaterialDialog reminderDialog;
    private Intent mIntent;
    private Bitmap avatarBitmap;
    private SwipeBackLayout mSwipeBackLayout;
    private  MultiplePermissionsListener snackbarMultiplePermissionsListener;
    private DatabaseReference mRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        avatar = (ImageView) findViewById(R.id.avatar_signup);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_signup);
        sv = (ScrollView) findViewById(R.id.sv_signup);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mIntent = new Intent(this,login_page.class);
        loadingdialog = new SpotsDialog(this,"Signing Up...");
        mRef = FirebaseDatabase.getInstance().getReference();
        defaultPhotoFlag =true;
        mSwipeBackLayout = getSwipeBackLayout();
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);



         /*edittext*/
        email = (TextInputEditText) findViewById(R.id.email_signup);
        password = (TextInputEditText) findViewById(R.id.password_signup);
        password2 = (TextInputEditText) findViewById(R.id.confirmpwd_signup);
        username = (TextInputEditText) findViewById(R.id.username_signup);
        displayname = (TextInputEditText) findViewById(R.id.fullname_signup);

        //textinputlayout
        email_layout = (TextInputLayout) findViewById(R.id.email_layout_signup);
        password_layout = (TextInputLayout)findViewById(R.id.password_layout_signup);
        password2_layout = (TextInputLayout)findViewById(R.id.confirmpwd_layout_signup);
        username_layout =(TextInputLayout) findViewById(R.id.username_layout_signup);
        displayname_layout = (TextInputLayout)findViewById(R.id.fullname_layout_signup);

        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    sv.smoothScrollBy(0,sv.getBottom());
                }
            }
        });
        password2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    sv.smoothScrollBy(0,sv.getBottom());
                }
            }
        });

        password2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                clearFocusAndError();
                CheckUserInput();
                return true;
            }
        });








    }


    public void avatarOnClick_signup(final View view) {


        //request permission
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE).withListener(
                new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                       if( report.areAllPermissionsGranted()) {

                           //open camera or gallery
                           EasyImage.openChooserWithGallery(signup.this,
                                   "Take or Pick a photo for your avatar", 0);
                       }else{
                          showSnackbarWithSetting("Camera and Storage permission are " +
                                  "needed for your avatar",view);
                       }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown
                            (List<PermissionRequest> permissions, PermissionToken token) {
                        //asker user for permission again if user denied(not ask again is unchecked)
                     token.continuePermissionRequest();
                    }
                }). check();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        EasyImage.handleActivityResult(requestCode, resultCode, data,
                this, new DefaultCallback() {
            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                                        CropImage.activity(Uri.fromFile(imageFile)).setCropShape
                                                (CropImageView.CropShape.OVAL).setFixAspectRatio(true).
                                                setAutoZoomEnabled(false)
                                                .start(signup.this);
            }




        });
        //add cropped image on avatar imageview
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                //uri to bitmap
                try {
                    avatarBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),
                            result.getUri());
                    Glide.with(getApplicationContext()).load(result.getUri()).
                            apply(RequestOptions.circleCropTransform()).into(avatar);
                    defaultPhotoFlag = false;

                } catch (IOException e) {
                    showToast(e.getMessage());
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                showToast(error.getMessage());
            }
        }
    }

    public void showToast(String str){
        Toast.makeText(getApplicationContext(),str,Toast.LENGTH_LONG).show();
    }

    public void showSnackbarWithSetting(String str, View view){
        Snackbar snackbar = Snackbar
                .make(view, str, Snackbar.LENGTH_LONG)
                .setAction("Setting", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Context context = view.getContext();
                        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:" + context.getPackageName()));
                        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(myAppSettings);
                    }
                });
        snackbar.show();
    }

    public Bitmap getCroppedBitmap(Bitmap bitmap) {

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        output = Bitmap.createScaledBitmap(output, 512, 512, false);

        return output;
    }



    public void onClickSignUp(View view) {
        clearFocusAndError();
        CheckUserInput();
    }

    public void CheckUserInput(){
        DatabaseReference mref = FirebaseDatabase.getInstance().getReference("Usernames");
        mref.addListenerForSingleValueEvent(new ValueEventListener() {
            String emailstr = email.getText().toString();
            String usernameStr = username.getText().toString();
            String passwordStr = password.getText().toString();
            String passwordStr2 = password2.getText().toString();
            String fullname = displayname.getText().toString();

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                flag = false;
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    if (child.getKey().equals(usernameStr)) {
                        flag = true;
                        break;
                    }
                }
                if (fullname.isEmpty()) {
                    displayname_layout.setErrorEnabled(true);
                    displayname_layout.setError(getString(R.string.emptyfieldError));
                    displayname.requestFocus();
                } else if (fullname.trim().matches("")) {
                        displayname_layout.setErrorEnabled(true);
                        displayname_layout.setError(getString(R.string.emptyfieldError));
                        displayname.requestFocus();
                } else if (flag) {
                    username_layout.setErrorEnabled(true);
                    username_layout.setError(getString(R.string.usernameError));
                    username.requestFocus();
                } else if (usernameStr.isEmpty()) {
                    username_layout.setErrorEnabled(true);
                    username_layout.setError(getString(R.string.emptyfieldError));
                    username.requestFocus();
                }else if (usernameStr.contains(" ")) {
                    username_layout.setErrorEnabled(true);
                    username_layout.setError(getString(R.string.usernameError3));
                    username.requestFocus();
                } else if (checkFirebasePathError(usernameStr)) {
                        username_layout.setErrorEnabled(true);
                        username_layout.setError(getString(R.string.firebase_path_error));
                        username.requestFocus();
                } else if (usernameStr.length() < 3 ||usernameStr.length() > 20) {
                    username_layout.setErrorEnabled(true);
                    username_layout.setError(getString(R.string.usernameError2));
                    username.requestFocus();
                } else if (!emailstr.contains("@") && !emailstr.isEmpty()) {
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
                } else if (!passwordStr.equals(passwordStr2)) {
                    password2_layout.setErrorEnabled(true);
                    password2_layout.setError(getString(R.string.pwdError));
                    password.requestFocus();
                } else if (passwordStr.length() < 6 || passwordStr.contains(" ")) {
                    password_layout.setErrorEnabled(true);
                    password_layout.setError(getString(R.string.pwdError2));
                    password.requestFocus();
                } else if (defaultPhotoFlag) {
                    showReminderDialog();
                } else {
                    SignUp(emailstr,passwordStr,fullname,usernameStr);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            showDismissDialog(databaseError.getMessage());
            }
        });


    }

    public void showReminderDialog(){

        reminderDialog = new MaterialDialog.Builder(this)
                .content("Because you didn't choose your own avatar photo, " +
                        "the default one will be used (You also can change it later)")
                .positiveText("I am fine with it").positiveColor(getResources().getColor(R.color.colorPrimary))
                .negativeText("Choose my own")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        SignUp(email.getText().toString(),password.getText().toString(),
                                displayname.getText().toString(),username.getText().toString());
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        EasyImage.openChooserWithGallery(signup.this,
                                "Take or Pick a photo for your avatar", 0);
                    }
                }).build();

        reminderDialog.show();
    }

    public void showDismissDialog(String Str){
               new MaterialDialog.Builder(this)
                .title("Error").titleColor(getResources().getColor(R.color.red_error))
                .content(Str)
                .positiveText("okay")
                .show();
    }

    public void clearFocusAndError(){
        displayname.clearFocus();
        username.clearFocus();
        email.clearFocus();
        password.clearFocus();
        password2.clearFocus();

        displayname_layout.setErrorEnabled(false);
        username_layout.setErrorEnabled(false);
        email_layout.setErrorEnabled(false);
        password_layout.setErrorEnabled(false);
        password2_layout.setErrorEnabled(false);

        displayname.setError(null);
        username.setError(null);
        email.setError(null);
        password.setError(null);
        password2.setError(null);

    }

    public boolean checkFirebasePathError(String str){
        char[] strArr = {'.','#','$','[',']','/'};
        for(char s :strArr){
            if(str.contains(String.valueOf(s))){
                return true;
            }

        }
        return false;

    }


    public void uploadImage(Bitmap bitmap, final User user, final FirebaseUser currentUser){
        String filename = UUID.randomUUID().toString()+".PNG";
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference avatarRef = storage.getReference().child("UserAvatars/"+filename);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        getCroppedBitmap(bitmap).compress(Bitmap.CompressFormat.PNG, 75, stream);
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
                updateDatabaseAndCurrentUser(user,currentUser,downloadUrl.toString());
            }
        });


    }

    public void SignUp(final String email, String password, final String displayname,
                       final String username){
        loadingdialog.show();

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            User user = new User();
                            user.setDisplayname(displayname);
                            user.setUsername(username);
                            user.setEmail(email);
                            if(defaultPhotoFlag) {
                                updateDatabaseAndCurrentUser(user,currentUser,DEFAULT_PHOTO_URL);
                            }else{
                               uploadImage(avatarBitmap,user,currentUser);
                            }
                        } else {
                          showDismissDialog(task.getException().getMessage());
                            loadingdialog.dismiss();
                        }

                    }
                });
    }

    public void updateDatabaseAndCurrentUser(User user, FirebaseUser currentUser,String photoUrl){

        user.setPhotourl(photoUrl);

        mRef.child("Users").child(currentUser.getUid()).setValue(user);
        mRef.child("Usernames").child(user.getUsername()).setValue(currentUser.getUid());



        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(user.getDisplayname())
                .setPhotoUri(Uri.parse(user.getPhotourl()))
                .build();

        currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            loadingdialog.dismiss();
                          showToast("Sign Up Success");
                          startActivity(mIntent);
                          finish();

                        }else{
                            loadingdialog.dismiss();
                            showDismissDialog(task.getException().getMessage());
                        }
                    }
                });

    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_from_left,R.anim.slide_to_right);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        overridePendingTransition(R.anim.slide_from_left,R.anim.slide_to_right);
        return true;
    }
}
