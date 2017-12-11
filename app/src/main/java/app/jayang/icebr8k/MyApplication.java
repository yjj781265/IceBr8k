package app.jayang.icebr8k;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import cat.ereza.customactivityoncrash.config.CaocConfig;




/**
 * Created by yjj781265 on 11/8/2017.
 */

public class MyApplication extends Application  {

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
        context = getApplicationContext();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);


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
                setNotificationOpenedHandler( new MyNotificationOpenedHandler()).
                setNotificationReceivedHandler(new MyNotificationReceivedHandler())
                .init();





    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }


}
