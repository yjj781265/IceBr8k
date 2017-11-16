package app.jayang.icebr8k;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONObject;

import app.jayang.icebr8k.Modle.User;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by yjj781265 on 11/13/2017.
 */

public class MyNotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {
    // This fires when a notification is opened by tapping on it.
    @Override
    public void notificationOpened(OSNotificationOpenResult result) {
        OneSignal.clearOneSignalNotifications();
        final JSONObject data = result.notification.payload.additionalData;
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String user2Id = null;
        if (data != null) {
            user2Id = data.optString("user2Id");
            if (user2Id != null) {
                DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Users/" + user2Id);
                final String finalUser2Id = user2Id;
                mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final User user = dataSnapshot.getValue(User.class);
                        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Users/" + currentUser.getUid());
                        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String online = dataSnapshot.child("onlineStats").getValue(String.class);
                                if (online.equals("1")) {
                                    Intent mIntent = new Intent(MyApplication.getContext(), MainChatActivity.class);
                                    Bundle mBundle = new Bundle();
                                    mBundle.putString("user2Uid", finalUser2Id);
                                    mBundle.putSerializable("user2", user);
                                    mIntent.putExtras(mBundle);
                                    mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT );
                                    MyApplication.getContext().startActivity(mIntent);
                                } else {
                                    Intent mIntent = new Intent(MyApplication.getContext(), Homepage.class);
                                    Bundle mBundle = new Bundle();
                                    mBundle.putString("user2Uid", finalUser2Id);
                                    mBundle.putSerializable("user2", user);
                                    mIntent.putExtras(mBundle);
                                    mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT );
                                    MyApplication.getContext().startActivity(mIntent);
                                }


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


            // The following can be used to open an Activity of your choice.


        }
    }
}




