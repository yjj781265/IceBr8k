package app.jayang.icebr8k.Utility;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONObject;

import app.jayang.icebr8k.Homepage;
import app.jayang.icebr8k.MyApplication;


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

        if (data != null) {
            // extras for chat Page
            String user2Id = data.optString("chatId",null);
            String name =data.optString("chatName",null);

            // extras for reply Page
            String questionId = data.optString("questionId",null);
            String title = data.optString("title",null);
            String commentAuthorId = data.optString("commentAuthorId",null);
            String topCommentId = data.optString("topCommentId",null);
            String commentId = data.optString("commentId",null);

            Log.d("notificationHandler",questionId +"\n" + title +"\n" + commentAuthorId +"\n" + topCommentId +"\n" + commentId +"\n"+ user2Id + "\n chatId"+ name);



            if(questionId!=null){
                Intent mIntent = new Intent(MyApplication.getContext(), Homepage.class);
                mIntent.putExtra("questionId", questionId);
                mIntent.putExtra("topCommentId", topCommentId);
                mIntent.putExtra("commentId", commentId);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT );
                MyApplication.getContext().startActivity(mIntent);


            }


            if (user2Id != null && name!=null) {
                Intent  mIntent = new Intent(MyApplication.getContext(), Homepage.class);
                mIntent.putExtra("chatId", user2Id);
                mIntent.putExtra("chatName", name);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT );
                MyApplication.getContext().startActivity(mIntent);

            } else{
               /* Intent mIntent = new Intent(MyApplication.getContext(),FriendRequestPage.class);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT );
                MyApplication.getContext().startActivity(mIntent);*/
            }


        }else{
            Intent mIntent = new Intent(MyApplication.getContext(),Homepage.class);
            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT );
            MyApplication.getContext().startActivity(mIntent);
        }


            // The following can be used to open an Activity of your choice.


        }
    }





