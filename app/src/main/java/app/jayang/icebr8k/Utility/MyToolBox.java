package app.jayang.icebr8k.Utility;

import android.content.Context;
import android.content.res.Resources;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyToolBox {


    public static int convertDptoPixel(float dp, Context context){
        // Converts 14 dip into its equivalent px
        float dip = dp;
        Resources r = context.getResources();
        float px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.getDisplayMetrics()
        );
        return (int)px;
    }


    public static boolean isInteger(String s) {
        return isInteger(s,10);
    }

    public static boolean isInteger(String s, int radix) {
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i),radix) < 0) return false;
        }
        return true;
    }

    public static boolean isOneWord(String string){
        Pattern wordPattern = Pattern.compile("\\w+");
        Matcher wordMatcher = wordPattern.matcher(string);
        if (!wordMatcher.matches()) {
            // discard user input
            return false;
        }
        return  true;
    }

    public static void showSnackBar(String text , View view, boolean isShort){
        Snackbar snackbar;
        if(isShort){
            snackbar = Snackbar
                    .make(view, text, Snackbar.LENGTH_LONG);
        }else {
            snackbar = Snackbar.make(view,text,Snackbar.LENGTH_LONG);
        }


        snackbar.show();
    }

    public static void showToast(String text,Context context){
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }


    public static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public static void showSoftKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public static void hideSoftKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().
                getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
    }


}
