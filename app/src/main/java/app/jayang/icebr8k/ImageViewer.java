package app.jayang.icebr8k;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Fade;
import android.widget.ImageView;
import android.widget.Toolbar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.sumimakito.awesomeqr.AwesomeQRCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageViewer extends AppCompatActivity {

    private ImageView photoView;
    private android.support.v7.widget.Toolbar  mToolbar;
    private String photourl;
    private Handler handler;
    private MaterialDialog mDialog;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        photoView =  findViewById(R.id.photo_view);
        mToolbar =findViewById(R.id.imageViewer_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        photourl  =currentUser.getPhotoUrl().toString();
        handler = new Handler();
        mDialog = new MaterialDialog.Builder(this)
                .content("Loading...").canceledOnTouchOutside(false)
                .progress(true, 0)
                .build();



        if(photourl!=null){
            mDialog.show();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Glide.with(getApplicationContext()).asBitmap().load(photourl).into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            new AwesomeQRCode.Renderer()
                                    .contents(currentUser.getUid()).logo(resource)
                                    .size(800).margin(20)
                                    .renderAsync(new AwesomeQRCode.Callback() {
                                        @Override
                                        public void onRendered(final AwesomeQRCode.Renderer renderer, final Bitmap bitmap) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    // Tip: here we use runOnUiThread(...) to avoid the problems caused by operating UI elements from a non-UI thread.
                                                    photoView.setImageBitmap(bitmap);
                                                    mDialog.dismiss();
                                                }
                                            });
                                        }

                                        @Override
                                        public void onError(AwesomeQRCode.Renderer renderer, Exception e) {
                                            e.printStackTrace();
                                        }
                                    });
                        }
                    });

                }
            },666);


        }


    }



    @Override
    public boolean onSupportNavigateUp() {
        supportFinishAfterTransition();
        return true;
    }


    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
       // finish();
    }
}
