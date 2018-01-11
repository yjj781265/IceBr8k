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
        final String user2Id ,name;
        if (data != null) {
            user2Id = data.optString("user2Id");
            name =data.optString("user2Name");

            final DatabaseReference onLineCheck = FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(user2Id).child("onlineStats");
            onLineCheck.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                   String online = dataSnapshot.getValue(String.class);
                    if(!online.equals("0")){
                        if (user2Id != null && name!=null) {

                            Intent mIntent = new Intent(MyApplication.getContext(), MainChatActivity.class);
                            mIntent.putExtra("user2Id", user2Id);
                            mIntent.putExtra("user2Name", name);
                            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.
                                    FLAG_ACTIVITY_REORDER_TO_FRONT );
                            MyApplication.getContext().startActivity(mIntent);
                        }
                    }else{
                        if (user2Id != null && name!=null) {
                            Intent mIntent = new Intent(MyApplication.getContext(), Homepage.class);
                            mIntent.putExtra("user2Id", user2Id);
                            mIntent.putExtra("user2Name", name);
                            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.
                                    FLAG_ACTIVITY_REORDER_TO_FRONT );
                            MyApplication.getContext().startActivity(mIntent);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }else{
            Intent mIntent = new Intent(MyApplication.getContext(),Homepage.class);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.
                    FLAG_ACTIVITY_REORDER_TO_FRONT );
            MyApplication.getContext().startActivity(mIntent);
        }


            // The following can be used to open an Activity of your choice.


        }
    }





