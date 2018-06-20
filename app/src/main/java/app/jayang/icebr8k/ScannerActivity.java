package app.jayang.icebr8k;

import android.content.Intent;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import app.jayang.icebr8k.Modle.User;
import cn.bingoogolapple.qrcode.core.QRCodeView;


public class ScannerActivity extends AppCompatActivity implements QRCodeView.Delegate  {
    private  QRCodeView qrCodeView;
    private Toolbar mToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_scanner);
         qrCodeView = findViewById(R.id.zxingview);
         mToolbar = findViewById(R.id.scan_toolbar);
         setSupportActionBar(mToolbar);
         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        qrCodeView.setDelegate(this);
        qrCodeView.startCamera();
    }

    @Override
    protected void onStart() {
        super.onStart();
        qrCodeView.showScanRect();
        qrCodeView.startSpotAndShowRect();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.scan_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.scan_myQR:
                Intent intent = new Intent( this, MyQR_Code.class);
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
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
                        finish();
                        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                    } else {
                        Toast.makeText(getApplicationContext(),"Sorry, not a valid IceBr8k QR code, try it again", Toast.LENGTH_SHORT).show();
                        qrCodeView.startSpot();

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
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onResume() {
        super.onResume();
               // Start camera on resume
    }

    @Override
    protected void onStop() {
        qrCodeView.stopCamera();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        qrCodeView.onDestroy();
        super.onDestroy();

    }

    @Override
    public void onBackPressed() {
        finish();
    }


    @Override
    public void onScanQRCodeSuccess(String result) {
       // Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        searchUser(result);
        vibrate();
        qrCodeView.startSpot();
    }

    @Override
    public void onScanQRCodeOpenCameraError() {

    }
}

