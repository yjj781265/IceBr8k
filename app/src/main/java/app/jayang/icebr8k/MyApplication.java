package app.jayang.icebr8k;

import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.onesignal.OneSignal;
import com.squareup.leakcanary.LeakCanary;
import com.zplesac.connectionbuddy.ConnectionBuddy;
import com.zplesac.connectionbuddy.ConnectionBuddyConfiguration;

import app.jayang.icebr8k.Utility.MyNotificationOpenedHandler;
import app.jayang.icebr8k.Utility.MyNotificationReceivedHandler;
import cat.ereza.customactivityoncrash.config.CaocConfig;




/**
 * Created by yjj781265 on 11/8/2017.
 */

public class MyApplication extends Application {

    public MyApplication() {
        super();
    }

    private static Context context;


    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();


        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        // Normal app init code...


        context = getApplicationContext();




// url loading logic for drawer header account image
       /* DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Glide.with(imageView.getContext()).load(uri).apply(new RequestOptions().placeholder(placeholder)).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Glide.with(imageView.getContext()).clear(imageView);
            }
        });*/


        CaocConfig.Builder.create()
                .backgroundMode(CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
                .enabled(true) //default: true
                .showErrorDetails(true) //default: true
                .showRestartButton(true) //default: true
                .trackActivities(false) //default: false
                .minTimeBetweenCrashesMs(3000).apply(); //default: 3000


        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true).
                setNotificationOpenedHandler(new MyNotificationOpenedHandler()).
                setNotificationReceivedHandler(new MyNotificationReceivedHandler())
                .init();

        ConnectionBuddyConfiguration networkInspectorConfiguration =
                new ConnectionBuddyConfiguration.Builder(this).
                        notifyOnlyReliableEvents(true)
                        .build();
        ConnectionBuddy.getInstance().init(networkInspectorConfiguration);

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()

           .cacheInMemory(true)
                .cacheOnDisk(true)
           .build();
        // Create global configuration and initialize ImageLoader with this config
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(defaultOptions)
			.build();
        ImageLoader.getInstance().init(config);



    }


    @Override
    public void onTerminate() {
        super.onTerminate();
    }




}


