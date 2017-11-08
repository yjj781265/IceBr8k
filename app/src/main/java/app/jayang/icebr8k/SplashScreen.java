package app.jayang.icebr8k;

import android.content.Intent;
import android.icu.util.Calendar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseUser;

import java.util.Date;

public class SplashScreen extends AppCompatActivity {
    FirebaseUser currentUser;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Date currentTime = Calendar.getInstance().getTime();

        Log.d("time",currentTime.toString());
        if(currentUser!=null){


            Intent i = new Intent(this,Homepage.class);
            startActivity(i);
            finish();
        }else{


            Intent i = new Intent(this,login_page.class);
            startActivity(i);
            finish();
        }

    }
}

