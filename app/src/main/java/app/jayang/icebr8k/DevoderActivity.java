package app.jayang.icebr8k;

import android.content.Intent;
import android.graphics.PointF;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

import app.jayang.icebr8k.Modle.User;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class DevoderActivity extends AppCompatActivity  implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    private  MediaPlayer player;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        player = MediaPlayer.create(DevoderActivity.this,R.raw.ding_sound);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
    }




    private void searchUser(final String userId) {
        if (checkString(userId)) {
            DatabaseReference usernameRef = FirebaseDatabase.getInstance().getReference().
                    child("Users").child(userId);

            usernameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {

                        Intent i = new Intent(getBaseContext(), UserProfilePage.class);
                        i.putExtra("userInfo", user);
                        i.putExtra("userUid",userId);
                        startActivity(i);
                        player.start();
                        finish();
                        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                    } else {
                        Toast.makeText(getApplicationContext(),"Sorry, not a valid IceBr8k QR code, try it again", Toast.LENGTH_SHORT).show();

                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }else{
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(userId));
            startActivity(browserIntent);


        }
    }







    private boolean checkString(String str){
        if(str.contains(".")||str.contains("#")||str.contains("$")||str.contains("{")
                ||str.contains("}")){
            return  false;
        }else{
            return  true;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(player!=null){
            player.release();
        }
    }

    @Override
    public void handleResult(Result result) {
         searchUser(result.getText());


    }
    }

