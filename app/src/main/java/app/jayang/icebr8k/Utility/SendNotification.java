package app.jayang.icebr8k.Utility;

import android.content.Context;
import android.preference.PreferenceActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.onesignal.OneSignal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import app.jayang.icebr8k.Modle.User;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

/**
 * Created by yjj781265 on 11/13/2017.
 */

public class SendNotification {
    private static final String BASE_URL = "https://onesignal.com/api/v1/notifications";
    public static void sendNotificationTo( final String name, final String text, final String user2Id) {

        DatabaseReference playserIdRef = FirebaseDatabase.getInstance().getReference()
                .child("Notification").child(user2Id).child("player_id");
        playserIdRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String playerId = dataSnapshot.getValue(String.class);
                if(playerId!=null) {
                    try {
                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        String url = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString();
                        OneSignal.postNotification(new JSONObject("{'contents': {'en':'" + text + "'}," +
                                        " 'include_player_ids': ['" + playerId + "'], 'data' : { 'chatId':'"
                                        +userId+"'"+",'chatName':'"+name+"'"+"},"
                                        + "'headings': { 'en':'"+ name+"'},"+"large_icon :'"+ url+"'"+" }"),
                                new OneSignal.PostNotificationResponseHandler() {
                                    @Override
                                    public void onSuccess(JSONObject response) {
                                        Log.d("Jsonjay","got it");
                                    }

                                    @Override
                                    public void onFailure(JSONObject response) {
                                        Log.d("Jsonjay", response.toString());
                                    }
                                });
                    } catch (JSONException e) {
                        Log.d("Jsonjay", e.getMessage());
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });





    }


    public static void sendFriendRequestNotification(final String user2Id, final String name, final String text) {


        DatabaseReference playserIdRef = FirebaseDatabase.getInstance().getReference()
                .child("Notification").child(user2Id).child("player_id");
        playserIdRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String playerId = dataSnapshot.getValue(String.class);
                if(playerId!=null) {
                    try {
                        String url = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString();
                        OneSignal.postNotification(new JSONObject("{'contents': {'en':'" + text + "'}," +
                                        " 'include_player_ids': ['" + playerId + "'],"

                                        + "'headings': { 'en':'"+ name+"'},"+"large_icon :'"+ url+"'"+" }"),
                                new OneSignal.PostNotificationResponseHandler() {
                                    @Override
                                    public void onSuccess(JSONObject response) {
                                        Log.d("Jsonjay","got it");
                                    }

                                    @Override
                                    public void onFailure(JSONObject response) {
                                        Log.d("Jsonjay", response.toString());
                                    }
                                });
                    } catch (JSONException e) {
                        Log.d("Jsonjay", e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    public static void sendReplyNotification(final String user2Id, final String name,
                                             final String text,final String questionId,final String topCommentId, final String commentId) {


        DatabaseReference playserIdRef = FirebaseDatabase.getInstance().getReference()
                .child("Notification").child(user2Id).child("player_id");
        playserIdRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String playerId = dataSnapshot.getValue(String.class);
                if(playerId!=null) {
                    try {
                        String url = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString();
                        OneSignal.postNotification(new JSONObject("{'contents': {'en':'" + text + "'}," +
                                        " 'include_player_ids': ['" + playerId + "'],"
                                        +"'data': {'questionId': '"+ questionId +"', " +
                                        " 'topCommentId': '"+ topCommentId+"', 'commentId': '"+commentId +"' },"
                                        + "'headings': { 'en':'"+ name+"'},"+"large_icon :'"+ url+"'"+" }"),
                                new OneSignal.PostNotificationResponseHandler() {
                                    @Override
                                    public void onSuccess(JSONObject response) {
                                        Log.d("Jsonjay","got it");
                                    }

                                    @Override
                                    public void onFailure(JSONObject response) {
                                        Log.d("Jsonjay", response.toString());
                                    }
                                });
                    } catch (JSONException e) {
                        Log.d("Jsonjay", e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }



}
