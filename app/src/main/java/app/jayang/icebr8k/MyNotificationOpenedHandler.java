package app.jayang.icebr8k;

import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONObject;


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
            user2Id = data.optString("chatId");
            name =data.optString("chatName");

            if (user2Id != null && name!=null) {
                Intent  mIntent = new Intent(MyApplication.getContext(), Homepage.class);
                mIntent.putExtra("chatId", user2Id);
                mIntent.putExtra("chatName", name);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT );
                MyApplication.getContext().startActivity(mIntent);
                        }


        }else{
            Intent mIntent = new Intent(MyApplication.getContext(),Homepage.class);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT );
            MyApplication.getContext().startActivity(mIntent);
        }


            // The following can be used to open an Activity of your choice.


        }
    }





