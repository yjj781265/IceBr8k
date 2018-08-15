package app.jayang.icebr8k.Utility;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

public class MyToolBox {


    public static int convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return  Integer.valueOf((int)dp);
    }

}
