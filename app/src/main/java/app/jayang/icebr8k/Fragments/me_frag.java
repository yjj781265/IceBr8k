package app.jayang.icebr8k.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.dd.processbutton.FlatButton;
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
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import app.jayang.icebr8k.Adapter.ViewPagerAdapter;
import app.jayang.icebr8k.FriendRequestPage;
import app.jayang.icebr8k.FullImageView;
import app.jayang.icebr8k.Homepage;
import app.jayang.icebr8k.Modle.UserQA;
import app.jayang.icebr8k.MyQR_Code;
import app.jayang.icebr8k.Utility.ActivityCommunicator;
import app.jayang.icebr8k.Modle.User;
import app.jayang.icebr8k.R;
import id.zelory.compressor.Compressor;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import static android.app.Activity.RESULT_OK;

/**
 * Created by yjj781265 on 1/4/2018.
 */

public class me_frag extends Fragment {
    private View mView;
    private ImageView avatar;
    private TextView username, name;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private ViewPagerAdapter mViewPagerAdapter;
    private RelativeLayout loadingGif;
    private MaterialDialog mProgressDialog;
    private String DEFAULT_URL  = "https://i.imgur.com/xUAsoWs.png";


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.me_frag, container, false);
        mTabLayout = mView.findViewById(R.id.me_tablayout);
        mViewPager = mView.findViewById(R.id.me_viewpager);
        mViewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager());
        loadingGif = mView.findViewById(R.id.loadingImg_meTab);
        avatar = mView.findViewById(R.id.me_avatar);
        username = mView.findViewById(R.id.me_username);
        name = mView.findViewById(R.id.me_displayname);
        mProgressDialog = new MaterialDialog.Builder(getActivity())
                .content("Updating Avatar...")
                .progress(true, 0).build();

        mViewPagerAdapter.addFragment(new Userstab_Fragment());
        mViewPagerAdapter.addFragment(new QuestionAnswered_Fragment());
        mViewPager.setAdapter(mViewPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        mTabLayout.getTabAt(0).setText("Friends");
        mTabLayout.getTabAt(1).setText("Questions\nAnswered");
        setTabTitle();

// set my info UI
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user.getPhotourl() != null) {
                    Glide.with(getContext()).load(user.getPhotourl()).
                            apply(RequestOptions.circleCropTransform().placeholder(R.drawable.default_avatar3)).into(avatar);
                }
                name.setText(user.getDisplayname());
                username.setText(user.getUsername());

                if (user.getDisplayname() != null && user.getPhotourl() != null) {
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(user.getDisplayname())
                            .setPhotoUri(Uri.parse(user.getPhotourl()))
                            .build();

                    FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        loadingGif.setVisibility(View.GONE);

                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSignleChoiceDialog();
            }
        });

        avatar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    avatar.setAlpha(.6f);
                } else {
                    avatar.setAlpha(1f);
                }

                return false;
            }
        });


        return mView;

    }

    void setTabTitle(){
        // retrieve user's question count from Firebase

            FirebaseUser currentuser = FirebaseAuth.getInstance().getCurrentUser();
            DatabaseReference questionRef = FirebaseDatabase .getInstance().getReference()
                    .child("UserQA")
                    .child(currentuser.getUid());
            questionRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                      String count = "("+dataSnapshot.getChildrenCount()+")";
                    mTabLayout.getTabAt(1).setText("Questions Answered "+ count);
                }


                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

    }


    public void showSignleChoiceDialog() {

        PopupMenu popup = new PopupMenu(getActivity(), avatar);
        //Inflating the Popup using xml file
        popup.getMenuInflater()
                .inflate(R.menu.photo_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.photo_menu_change) {
                    changeAvatar();
                    return true;
                } else {
                    // view photo
                    Intent i = new Intent(getActivity(), FullImageView.class);
                    i.putExtra("photoUrl", FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString());
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), avatar, "profile");
                    startActivity(i, options.toBundle());
                    return true;
                }

            }
        });
        popup.show();
    }

    public void changeAvatar() {
        //request permission
        Dexter.withActivity(getActivity())
                .withPermissions(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE).withListener(
                new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if( report.areAllPermissionsGranted()) {

                            //open camera or gallery
                            EasyImage.openChooserWithGallery(me_frag.this,
                                    "Take or Pick a photo for your avatar", 0);
                        }else{
                            Toast.makeText(getContext(), "Camera and Storage permission are " +
                                    "needed for your avatar", Toast.LENGTH_LONG).show();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {


        super.onActivityResult(requestCode, resultCode, data);
        EasyImage.handleActivityResult(requestCode, resultCode, data,
                getActivity(), new DefaultCallback() {
                    @Override
                    public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                        CropImage.activity(Uri.fromFile(imageFile)).setCropShape
                                (CropImageView.CropShape.RECTANGLE).setFixAspectRatio(true).
                                setAutoZoomEnabled(false)
                                .start(getContext(), me_frag.this);
                    }

                });
        //add cropped image on avatar imageview
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                //uri to bitmap
                try {


                    File imageFile = new File(result.getUri().getPath());
                    Bitmap avatarBitmap = new Compressor(getActivity()).compressToBitmap(imageFile);

                    mProgressDialog.show();
                    uploadImage(avatarBitmap,FirebaseAuth.getInstance().getCurrentUser());

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
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
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
                //delete the old avatar from storage
                deleteOldAvatarFile(currentUser.getPhotoUrl().toString());
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type,
                // and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                if(downloadUrl!=null) {
                    updateDatabaseAndCurrentUser(downloadUrl.toString());
                }else{
                    showDismissDialog("Avatar update failed");
                }
            }
        });


    }


    public void updateDatabaseAndCurrentUser(final String photoUrl){

        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("photourl").setValue(photoUrl);
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(Uri.parse(photoUrl))
                .build();
        mProgressDialog.dismiss();
        FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            Glide.with(mView).load(photoUrl).
                                    apply(RequestOptions.circleCropTransform().placeholder(R.drawable.default_avatar3)).into(avatar);
                            showToast("Avatar Updated ");

                        }else{
                            showDismissDialog(task.getException().getMessage());
                        }
                    }
                });

    }

    private void deleteOldAvatarFile(String photoUrl) {
        if (!DEFAULT_URL.equals(photoUrl)) {
            try {

                StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(photoUrl);
                photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // File deleted successfully
                        // showToast("File deleted");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Uh-oh, an error occurred!
                        //showToast("File deleted failed");
                    }
                });
            } catch (Exception e) {

            }
        }
    }

    public void showDismissDialog(String Str){
        new MaterialDialog.Builder(getActivity())
                .title("Error").titleColor(getResources().getColor(R.color.red_error))
                .content(Str)
                .positiveText("okay")
                .show();
    }

    public void showToast(String str){
        Toast.makeText(getActivity(),str,Toast.LENGTH_LONG).show();
    }
}
