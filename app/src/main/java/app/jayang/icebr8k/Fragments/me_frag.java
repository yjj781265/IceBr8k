package app.jayang.icebr8k.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.dd.processbutton.FlatButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.w3c.dom.Text;

import app.jayang.icebr8k.FriendRequestPage;
import app.jayang.icebr8k.Homepage;
import app.jayang.icebr8k.ImageViewer;
import app.jayang.icebr8k.Modle.ActivityCommunicator;
import app.jayang.icebr8k.Modle.User;
import app.jayang.icebr8k.PeopleNearby;
import app.jayang.icebr8k.R;

/**
 * Created by yjj781265 on 1/4/2018.
 */

public class me_frag extends Fragment {
    private View fragView;
    private FlatButton reset;
    private TextView displayname,username,email,badge;
    private ImageView avatar,qrCode;
    private FirebaseUser currentuser;
    private LinearLayout mLinearLayout;
    private SwitchCompat mSwitchCompat;
    private ActivityCommunicator activityCommunicator;
    private User user;
    private long lastClickTime = 0;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activityCommunicator =(ActivityCommunicator)context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentuser = FirebaseAuth.getInstance().getCurrentUser();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragView = inflater.inflate(R.layout.me_frag, container, false);
        displayname =fragView.findViewById(R.id.displayname_frag);
        username =fragView.findViewById(R.id.username_frag);
        email =fragView.findViewById(R.id.email_frag);
        badge =fragView.findViewById(R.id.badge_frag);
        avatar = fragView.findViewById(R.id.avatar_frag);
        qrCode = fragView.findViewById(R.id.QR_frag);
        reset = fragView.findViewById(R.id.reset_frag);
        mLinearLayout = fragView.findViewById(R.id.frt_frag);
        mSwitchCompat =fragView.findViewById(R.id.switch_frag);
        activityCommunicator.passDataToActivity(mSwitchCompat);

        //update ui
        badge.setVisibility(View.INVISIBLE);
        displayname.setText(currentuser.getDisplayName());
        email.setText(currentuser.getEmail());
        Glide.with(fragView).load(currentuser.getPhotoUrl()).
                apply(RequestOptions.circleCropTransform()).into(avatar);

        setBadge();
        //set switchcompat state base on user's choice history
        if("public".equals(getPrivacySharedPreference())){
            mSwitchCompat.setChecked(true);
            setUserPrivacy(true);
        }else{
            mSwitchCompat.setChecked(false);
            setUserPrivacy(false);
        }



        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").
                child(currentuser.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               user = dataSnapshot.getValue(User.class);
               if(user!=null){
                   username.setText(user.getUsername());
               }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        qrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();
                Intent intent = new Intent( fragView.getContext(), ImageViewer.class);
                startActivity(intent);
            }
        });

        mLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                    return;
                }
                lastClickTime = SystemClock.elapsedRealtime();
                Intent i = new Intent(getContext(), FriendRequestPage.class);
                startActivity(i);
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             showBasicDialog("Are you sure to reset all the questions ?");
            }
        });

        mSwitchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                      showReminderDialog();

                }else{
                    setSharedPreference(isChecked);
                    setUserPrivacy(isChecked);
                    Toast.makeText(getActivity(),"Location Service Off",Toast.LENGTH_SHORT).show();
                }
            }
        });

        return  fragView;


    }

    @Override
    public void onStart() {
        super.onStart();

    }

    private void setBadge(){
        DatabaseReference badgeRef = FirebaseDatabase.getInstance().getReference().child("Friends")
                .child(currentuser.getUid());
        badgeRef.keepSynced(true);

        badgeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count =0;
             for(DataSnapshot chidSnapShot : dataSnapshot.getChildren()){
                 if("Pending".equals(chidSnapShot.child("Stats").getValue(String.class))){
                    count++;
                 }
             }

             if(count==0){
                 badge.setVisibility(View.INVISIBLE);
             }else{
                 badge.setVisibility(View.VISIBLE);
                 badge.setText(String.valueOf(count));
             }
        }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void resetQuestions(){
        DatabaseReference resetRef = FirebaseDatabase.getInstance().getReference().child("UserQA").child(currentuser.getUid());

            resetRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(getActivity(),"Reset Sucess",Toast.LENGTH_SHORT).show();
                    Homepage homepage= (Homepage)getActivity();
                    homepage.getViewPager().setCurrentItem(0,false);
                }
            });


    }
    //run time permission
    private void checkLocationPermission() {
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        boolean isChecked =true;
                        mSwitchCompat.setChecked(isChecked);
                        setSharedPreference(isChecked);
                        setUserPrivacy(isChecked);

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        boolean isChecked =false;
                        mSwitchCompat.setChecked(isChecked);
                        mSwitchCompat.setChecked(isChecked);
                        setSharedPreference(isChecked);
                        setUserPrivacy(isChecked);

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }


    private void showBasicDialog(String str){
        new MaterialDialog.Builder(getContext())
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

    private void setSharedPreference(boolean isChecked){
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if(isChecked) {
            editor.putString(currentuser.getUid()+"privacy", "public");
            editor.commit();
        }else{
            editor.putString(currentuser.getUid()+"privacy", "private");
            editor.commit();
        }
    }

    private void setUserPrivacy(boolean isChecked){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").
                child(currentuser.getUid());
        if(isChecked) {
            ref.child("privacy").setValue("public");
        }else{
            ref.child("privacy").setValue("private");
        }
    }

    private String getPrivacySharedPreference(){
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String defaultValue = "private";
        String privacy = sharedPref.getString(currentuser.getUid()+"privacy", defaultValue);
        return privacy;
    }

    private void showReminderDialog(){
        new MaterialDialog.Builder(getActivity())
                .title("Reminder")
                .content(R.string.location_reminder).
                negativeColor(getResources().getColor(R.color.bootstrap_gray))
                .positiveText(R.string.ok).onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                checkLocationPermission();
            }
        }).negativeText(R.string.cancel).onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                boolean isChecked =false;
                mSwitchCompat.setChecked(isChecked);
                setSharedPreference(isChecked);
                setUserPrivacy(isChecked);
            }
        }).show();
    }



}
