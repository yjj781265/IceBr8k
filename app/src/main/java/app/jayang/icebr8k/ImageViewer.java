package app.jayang.icebr8k;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toolbar;

import com.afollestad.materialdialogs.MaterialDialog;
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
        mDialog = new MaterialDialog.Builder(this)
                .content("Loading")
                .progress(true, 0)
                .build();

        if(photourl!=null){
            mDialog.show();
            new DownloadFilesTask().execute(photourl);
        }


    }

    public Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private class DownloadFilesTask extends AsyncTask<String, Integer, Bitmap> {
        protected  Bitmap  doInBackground(String... urls) {

            return getBitmapFromURL(urls[0]);
        }

        protected void onProgressUpdate(Integer... progress) {


        }

        protected void onPostExecute(Bitmap result) {
            new AwesomeQRCode.Renderer()
                    .contents(currentUser.getUid()).logo(result)
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
    }

}
