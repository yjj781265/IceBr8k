package app.jayang.icebr8k;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.concurrent.Executor;

/**
 * Created by yjj781265 on 2/5/2018.
 */

public class MyJobService extends com.firebase.jobdispatcher.JobService implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationClient;
    private final String TAG = "BackGround Service";
    private LocationRequest mLocationRequest;

    @Override
    public boolean onStartJob(final com.firebase.jobdispatcher.JobParameters job) {
        Toast.makeText(getApplicationContext(), "IceBr8k Background Job Service Activated", Toast.LENGTH_LONG).show();
        setUpLocationClientIfNeeded();
        return false;

    }

    @Override
    public boolean onStopJob(com.firebase.jobdispatcher.JobParameters job) {

        return true;
    }


    private void initGoogleMapLocation() {
        createLocationRequest();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (mLocationRequest != null) {
            final LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);
            SettingsClient client = LocationServices.getSettingsClient(this);
            Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
            task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    if (ActivityCompat.checkSelfPermission(MyJobService.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MyJobService.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        showPermissionNotification();
                        return;
                    }
                    mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if(location!=null){
                             updateLocationtoDatabase(location.getLatitude(),location.getLongitude());
                            }

                        }
                    });



                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    if (e instanceof ResolvableApiException) {
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        showNotification();
                    }
                }
            });
        }

    }

    private void updateLocationtoDatabase( final double lat, final double lng){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("GeoFireLocations");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.setLocation( FirebaseAuth.getInstance().getCurrentUser().getUid(),
                new GeoLocation(lat, lng), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                Toast.makeText(getApplicationContext(),"last location is updated to firebase",Toast.LENGTH_LONG).show();
            }
        });
        ref.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("timestamp").setValue(new Date().getTime());

    }

    private void setUpLocationClientIfNeeded()
    {
        if(mGoogleApiClient == null) {
            buildGoogleApiClient();
        }else{
            initGoogleMapLocation();
        }
    }

    private  void  showPermissionNotification() {
        // the user default notification sound
        Uri notificaiontSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
    // The id of the channel.
            String id = "LocationPermission_id";
   // The user-visible name of the channel.
            CharSequence name = getString(R.string.channel_name);
   // The user-visible description of the channel.
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(id, name, importance);
   // Configure the notification channel.
            mChannel.setDescription(description);
            mChannel.enableLights(true);
   // Sets the notification light color for notifications posted to this
  // channel, if the device supports this feature.
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mNotificationManager.createNotificationChannel(mChannel);
        }


        NotificationCompat.Builder mBuilder =
                new NotificationCompat. Builder(this,"LocationPermssion_id")
                        .setSmallIcon(R.drawable.ic_stat_onesignal_default)
                        .setAutoCancel(true).setColorized(true)
                        .setOnlyAlertOnce(true)
                        .setColor(getResources().getColor(R.color.colorPrimary)).setSound(notificaiontSound)
                        .setContentTitle(getString(R.string.Icebr8k_notification_title_location_permission))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.Icebr8k_notification_context_location_permission)));


        if(Build.VERSION.SDK_INT<26){
            mBuilder.setPriority(Notification.PRIORITY_MAX);
        }

        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + getApplicationContext().getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        myAppSettings,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        // Sets an ID for the notification
        int mNotificationId = 002;
// Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
// Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

    }

    private  void  showNotification() {
        // the user default notification sound
        Uri notificaiontSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
// The id of the channel.
            String id = "LocationService_id";
// The user-visible name of the channel.
            CharSequence name = getString(R.string.channel_name);
// The user-visible description of the channel.
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(id, name, importance);
// Configure the notification channel.
            mChannel.setDescription(description);
            mChannel.enableLights(true);
// Sets the notification light color for notifications posted to this
// channel, if the device supports this feature.
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mNotificationManager.createNotificationChannel(mChannel);
        }


        NotificationCompat.Builder mBuilder =
                new NotificationCompat. Builder(this,"LocationService_id")
                        .setSmallIcon(R.drawable.ic_stat_onesignal_default)
                        .setContentTitle(getString(R.string.Icebr8k_notification_title_location_service))
                        .setAutoCancel(true).setColorized(true)
                        .setOnlyAlertOnce(true)
                        .setColor(getResources().getColor(R.color.colorPrimary)).setSound(notificaiontSound)
                        .setStyle(new NotificationCompat.BigTextStyle().
                                bigText(getString(R.string.Icebr8k_notification_context_location_service)));


        if(Build.VERSION.SDK_INT<26){
            mBuilder.setPriority(Notification.PRIORITY_MAX);
        }

        Intent resultIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        // Sets an ID for the notification
        int mNotificationId = 001;
// Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
// Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected synchronized void buildGoogleApiClient() {
        this.mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        this.mGoogleApiClient.connect();
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(getApplicationContext(), "Google Service Connected", Toast.LENGTH_LONG).show();
        initGoogleMapLocation();

    }

    @Override
    public void onConnectionSuspended(int i) {
     mGoogleApiClient.reconnect();
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
