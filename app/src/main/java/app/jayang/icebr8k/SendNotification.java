package app.jayang.icebr8k;

import android.content.Context;
import android.preference.PreferenceActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

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
    private static final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();


    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void sendNotificationTo(String playerId, String name, String text, Context context,User user,String userId) {
        final String appId = context.getString(R.string.OneSignal_App_id);
        String request = "{"
                +   "\"app_id\": "+"\""+appId+"\""+","
                +   "\"include_player_ids\": " + "["+"\""+playerId+"\""+"],"
                +   "\"headings\":"+"{"+"\"en\": "+"\""+name+"\""+"}"+","
                +   "\"contents\":"+"{"+"\"en\": "+"\""+text+"\""+"}"+","
                +    "\"data\": "+"{"+"\"currentuserId\": "+"\""+uid+"\""+","+"\"user2Id\": "+"\""+userId+"\""+"}"
                + "}";
        client.addHeader("Content-Type","application/json; charset=UTF-8");
        client.addHeader("Authorization", "Basic "+context.getString(R.string.OneSignal_user_API_key));
        try {
            StringEntity entity = new StringEntity(request);
            client.post(context, BASE_URL, entity, "application/json",new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    Log.d("header",response.toString());
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    Log.d("header",errorResponse.toString());
                }
            } );

        } catch (UnsupportedEncodingException e) {
            Log.d("entity",e.getMessage());
        }


    }
}
