package app.jayang.icebr8k;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;
import java.util.TreeSet;

import app.jayang.icebr8k.Modle.User;
import app.jayang.icebr8k.Modle.UserLocationDialog;


public class SplashScreen extends AppCompatActivity {
    private final String TAG ="SplashScreen";
    private final int NUM=100000;
    private long time =System.currentTimeMillis();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("holy shit",toISO8601UTC(new Date()));

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

                    Intent intent = new Intent(this, Homepage.class);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
                Intent i = new Intent(this, login_page.class);
                startActivity(i);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

            }


        }

    public static String toISO8601UTC(Date date) {

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        return df.format(date);
    }
    }


