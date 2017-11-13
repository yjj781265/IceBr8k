package app.jayang.icebr8k;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import static com.facebook.FacebookSdk.getApplicationContext;


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
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.None)
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
