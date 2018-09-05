package app.jayang.icebr8k;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.yarolegovich.discretescrollview.DSVOrientation;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.Pivot;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;

import app.jayang.icebr8k.Adapter.MediaViewAdapter;
import app.jayang.icebr8k.Modle.UserMessage;
import app.jayang.icebr8k.Utility.MyDateFormatter;


public class MediaViewActivty extends AppCompatActivity implements MediaViewAdapter.PhotoViewClickListener {
    private DiscreteScrollView mRecyclerView;
    private UserMessage currentMessage, firstMessage;
    private LinearLayoutManager manager;
    private MediaViewAdapter mAdapter;
    private Toolbar mToolbar;
    private ArrayList<UserMessage> mMessages = new ArrayList<>();
    private boolean hide;
    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private PagerSnapHelper snapHelper = new PagerSnapHelper();
    private View decorView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_view_activty);




        decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        // Note that system bars will only be "visible" if none of the
                        // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            YoYo.with(Techniques.SlideInDown).duration(300).playOn(mToolbar);


                            // adjustments to your UI, such as showing the action bar or
                            // other navigational controls.
                        } else {
                            YoYo.with(Techniques.SlideOutUp).duration(300).playOn(mToolbar);
                            // adjustments to your UI, such as hiding the action bar or
                            // other navigational controls.
                        }
                    }
                });

        mRunnable = new Runnable() {
            @Override
            public void run() {
                hideSystemUI();
            }
        };


        if (getIntent() != null) {
            mMessages = (ArrayList<UserMessage>) getIntent().getExtras().getSerializable("photoViews");
            currentMessage = (UserMessage) getIntent().getExtras().getSerializable("photoView");
            firstMessage = currentMessage;
        }
        if (currentMessage != null) {
            setTitle(currentMessage);
        }

        mRecyclerView = findViewById(R.id.media_recylerview);
       // manager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mAdapter = new MediaViewAdapter(mMessages, this);
       // mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setOrientation(DSVOrientation.HORIZONTAL);
        mRecyclerView.setItemTransformer(new ScaleTransformer.Builder()

                .setMaxScale(1.0f)
                .setMinScale(0.6f)
                .setPivotX(Pivot.X.CENTER) // CENTER is a default one
                .setPivotY(Pivot.Y.CENTER) // CENTER is a default one
                .build());
        mRecyclerView.setSlideOnFling(false);
        mRecyclerView.setItemTransitionTimeMillis(150);
      //  snapHelper.attachToRecyclerView(mRecyclerView);
        mToolbar = findViewById(R.id.media_toolbar);




        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mToolbar.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.SlideInDown).duration(300).playOn(mToolbar);

            }
        },500);


        mRecyclerView.addScrollStateChangeListener(new DiscreteScrollView.ScrollStateChangeListener<RecyclerView.ViewHolder>() {
            @Override
            public void onScrollStart(@NonNull RecyclerView.ViewHolder currentItemHolder, int adapterPosition) {

            }

            @Override
            public void onScrollEnd(@NonNull RecyclerView.ViewHolder currentItemHolder, int adapterPosition) {
                currentMessage = mMessages.get(adapterPosition);
                setTitle(currentMessage);
            }

            @Override
            public void onScroll(float scrollPosition, int currentPosition, int newPosition, @Nullable RecyclerView.ViewHolder currentHolder, @Nullable RecyclerView.ViewHolder newCurrent) {

            }
        });




        if (currentMessage != null && mMessages.contains(currentMessage)) {
            mRecyclerView.scrollToPosition(mMessages.indexOf(currentMessage));
        }
    }




    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.media_menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.share:
                shareImage();


                return true;
            case R.id.save_file:

                Dexter.withActivity(this)
                        .withPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {
                                new SaveImageTask(getApplicationContext()).execute(currentMessage.getText());

                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse response) {
                                showSnackbarWithSetting("Storage Permission needed for saving images", mRecyclerView);
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();


                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Glide.get(this).clearMemory();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){

            mHandler.postDelayed(mRunnable, 8000);
        }



    }

    public void copyFile(String oldPath, final String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }


                inStream.close();
                galleryAddPic(newPath);
            }
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Image Saved Under Folder Icebr8k_PIC", Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();

        }

    }




    private void galleryAddPic(String imagePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
    }

    private void shareImage() {
        new GetImageFile(this).execute(currentMessage.getText());
    }


    public void showSnackbarWithSetting(String str, View view) {
        Snackbar snackbar = Snackbar
                .make(view, str, Snackbar.LENGTH_LONG)
                .setAction("Setting", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Context context = view.getContext();
                        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:" + context.getPackageName()));
                        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(myAppSettings);
                    }
                });
        snackbar.show();
    }


    private void setTitle(final UserMessage message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(message.getSenderid()).child("displayname");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getSupportActionBar().setTitle(dataSnapshot.getValue(String.class));
                if(message.getTimestamp()!=null){
                    getSupportActionBar().setSubtitle(MyDateFormatter.timeStampToDateConverter(message.getTimestamp(), false));
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    private void hideSystemUI() {

       /* decorView.setSystemUiVisibility(
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);*/

        YoYo.with(Techniques.SlideOutUp).duration(300).playOn(mToolbar);


        hide = true;
    }

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {


        YoYo.with(Techniques.SlideInDown).duration(300).playOn(mToolbar);
      /*  decorView.setFitsSystemWindows(true);
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);*/
        hide = false;
        mHandler.postDelayed(mRunnable, 8000);

    }

    @Override
    public void onBackPressed() {
        showSystemUI();
        if (currentMessage.equals(firstMessage)) {
            supportFinishAfterTransition();
        } else {
            finish();
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        showSystemUI();
        if (currentMessage.equals(firstMessage)) {
            supportFinishAfterTransition();
        } else {
            finish();
        }
        return true;
    }

    @Override
    public void onPhotoViewClicked() {
        mHandler.removeCallbacks(mRunnable);
        if (hide) {
            showSystemUI();
        } else {
            hideSystemUI();
        }
    }


    public class SaveImageTask extends AsyncTask<String, Void, File> {
        private
        final Context context;

        public SaveImageTask(Context context) {
            this.context = context;
        }

        @Override
        protected File doInBackground(String... params) {
            String url = params[0]; // should be easy to extend to share multiple images at once
            try {
                return Glide
                        .with(context)
                        .load(url)
                        .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .get() // needs to be called on background thread
                        ;
            } catch (Exception ex) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(File result) {
            if (result == null) {
                return;
            }
            String path = result.getPath();
            String savedImagePath;


            String imageFileName = currentMessage.getGif() ?  "GIF_" + UUID.randomUUID().toString().substring(0, 8) + ".gif" :"IMG_" + UUID.randomUUID().toString().substring(0, 8) + ".jpg";
            File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    + "/Icebr8k_PIC");
            File imageFile = new File(storageDir, imageFileName);
            savedImagePath = imageFile.getAbsolutePath();
            copyFile(path, savedImagePath);
        }
    }

    public class GetImageFile extends AsyncTask<String, Void, File> {
        private
        final Context context;

        public GetImageFile(Context context) {
            this.context = context;
        }

        @Override
        protected File doInBackground(String... params) {
            String url = params[0]; // should be easy to extend to share multiple images at once
            try {
                return Glide
                        .with(context)
                        .load(url)
                        .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                        .get() // needs to be called on background thread
                        ;
            } catch (Exception ex) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(File result) {
            if (result == null) {
                return;
            }
            String path = result.getPath();
            String savedImagePath;
            Uri fileUri = null;

            String imgtype = currentMessage.getGif()? "gif" :"jpg";
            String imageFileName = "Img_" + UUID.randomUUID().toString().substring(0, 8) +"." +imgtype;
            File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    + "/Icebr8k_PIC");
            File imageFile = new File(storageDir, imageFileName);
            savedImagePath = imageFile.getAbsolutePath();
            // save on file first
            try {
                int bytesum = 0;
                int byteread = 0;
                File oldfile = new File(path);
                if (oldfile.exists()) { //文件存在时
                    InputStream inStream = new FileInputStream(path); //读入原文件
                    FileOutputStream fs = new FileOutputStream(savedImagePath);
                    byte[] buffer = new byte[1444];
                    int length;
                    while ((byteread = inStream.read(buffer)) != -1) {
                        bytesum += byteread; //字节数 文件大小
                        System.out.println(bytesum);
                        fs.write(buffer, 0, byteread);
                    }

                    inStream.close();

                }

            } catch (Exception e) {
                e.printStackTrace();

            }

            // Use the FileProvider to get a content URI
            try {
                fileUri = FileProvider.getUriForFile(
                        MediaViewActivty.this,
                        "app.jayang.icebr8k.fileprovider",
                        imageFile);
            } catch (IllegalArgumentException e) {
                Log.e("Sharing File ", e.getMessage());
            }
            Intent mResultIntent = new Intent(Intent.ACTION_SEND);
            if (fileUri != null) {
                // Grant temporary read permission to the content URI
                mResultIntent.setAction(Intent.ACTION_SEND);
                mResultIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                mResultIntent.setType("image/"+imgtype);
                startActivity(Intent.createChooser(mResultIntent, "Share image using"));
            } else {
                mResultIntent.setDataAndType(null, "");
                MediaViewActivty.this.setResult(RESULT_CANCELED,
                        mResultIntent);
            }
        }


    }
}


