package app.jayang.icebr8k;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;


public class SplashScreen extends AppCompatActivity {
    private final String TAG ="SplashScreen";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


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


    }


