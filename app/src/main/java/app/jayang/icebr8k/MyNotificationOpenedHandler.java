package app.jayang.icebr8k;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

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
        OSNotificationAction.ActionType actionType = result.action.type;
        JSONObject data = result.notification.payload.additionalData;
        String user2Id =null;


        if (data != null) {
            user2Id =data.optString("user2Id");

            if (user2Id != null)
                Log.i("OneSignalExample", "customkey set with value: " + user2Id);

            DatabaseReference mref = FirebaseDatabase.getInstance().getReference("Users/"+user2Id);
            final String finalUser2Id = user2Id;
            mref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user2 = dataSnapshot.getValue(User.class);
                    Intent intent = new Intent(MyApplication.getContext(), login_page.class);
                    intent.putExtra("user2",user2);
                    intent.putExtra("user2Id", finalUser2Id);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    MyApplication.getContext().startActivity(intent);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        if (actionType == OSNotificationAction.ActionType.ActionTaken)
            Log.i("OneSignalExample", "Button pressed with id: " + result.action.actionID);

        // The following can be used to open an Activity of your choice.






    }

}


