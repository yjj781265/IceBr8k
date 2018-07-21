package app.jayang.icebr8k;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import app.jayang.icebr8k.R;
import app.jayang.icebr8k.Utility.MyJobService;

public class Settings_Activity extends AppCompatActivity {
    private Switch mSwitch;
    private Toolbar mToolbar;
    //job scheduler variables
    private static final String Job_TaG ="MY_JOB_TAG";
    private FirebaseJobDispatcher mDispatcher;
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDispatcher =  new FirebaseJobDispatcher(new GooglePlayDriver(this));
        setContentView(R.layout.activity_settings);
        mSwitch = findViewById(R.id.settings_share_location_switch);
        mToolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        mSwitch.setChecked(getPrivacySharedPreference().equals("public"));
       // Toast.makeText(this, ""+getPrivacySharedPreference(), Toast.LENGTH_SHORT).show();

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    showReminderDialog();

                }else{
                    setSharedPreference(isChecked);
                    setUserPrivacy(isChecked);
                    stopJob();
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }


    private void showReminderDialog(){
        new MaterialDialog.Builder(this)
                .title("Reminder").canceledOnTouchOutside(false)
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
                mSwitch.setChecked(isChecked);
                setSharedPreference(isChecked);
                setUserPrivacy(isChecked);
            }
        }).show();
    }

    //run time permission
    private void checkLocationPermission() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        boolean isChecked =true;
                        mSwitch.setChecked(isChecked);
                        setSharedPreference(isChecked);
                        setUserPrivacy(isChecked);
                        startJob();

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        boolean isChecked =false;
                        mSwitch.setChecked(isChecked);
                        setSharedPreference(isChecked);
                        setUserPrivacy(isChecked);
                        stopJob();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }


    private void setSharedPreference(boolean isChecked){
        SharedPreferences sharedPref = this.getSharedPreferences("privacy",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if(isChecked) {
            editor.putString(currentUser.getUid()+"privacy", "public");
            editor.apply();
        }else{
            editor.putString(currentUser.getUid()+"privacy", "private");
            editor.apply();
        }
    }

    private String getPrivacySharedPreference(){
        SharedPreferences sharedPref = this.getSharedPreferences("privacy",MODE_PRIVATE);
        String privacy = sharedPref.getString(currentUser.getUid()+"privacy", "private");
        return privacy;
    }

    private void setUserPrivacy(boolean isChecked){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").
                child(currentUser.getUid());
        if(isChecked) {
            ref.child("privacy").setValue("public");
        }else{
            ref.child("privacy").setValue("private");
        }
    }




    public void startJob(){
        Job job = mDispatcher.newJobBuilder().setService(MyJobService.class).
                setLifetime(Lifetime.FOREVER).setRecurring(true).setTag(Job_TaG).setTrigger(Trigger.executionWindow(600,900))
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL).setConstraints(Constraint.ON_ANY_NETWORK).setReplaceCurrent(true).build();
        mDispatcher.mustSchedule(job);
        //Toast.makeText(this,"Sharing Location in the background ",Toast.LENGTH_LONG).show();
    }
    public void stopJob(){
        mDispatcher.cancel(Job_TaG);
        // Toast.makeText(this,"Sharing Location off",Toast.LENGTH_LONG).show();
    }
}
