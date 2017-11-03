package app.jayang.icebr8k;

import android.content.Intent;
import android.os.Handler;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.Transaction;

public class SplashScreen extends AppCompatActivity {
    FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

