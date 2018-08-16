package app.jayang.icebr8k.Modle;

import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.view.inputmethod.EditorInfoCompat;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

import app.jayang.icebr8k.Utility.GifSelectedListener;


/**
 * Created by yjj781265 on 2/25/2018.
 */

public class MyEditText extends AppCompatEditText {


    private GifSelectedListener mGifSelectedListener;

    public MyEditText(Context context) {

        super(context);


    }




    public MyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public InputConnection onCreateInputConnection(EditorInfo editorInfo) {
        final InputConnection ic = super.onCreateInputConnection(editorInfo);

        EditorInfoCompat.setContentMimeTypes(editorInfo,
                new String[]{"image/jpeg",
                        "image/bmp",
                        "image/gif",
                        "image/jpg",
                        "image/png"});

        final InputConnectionCompat.OnCommitContentListener callback =
                new InputConnectionCompat.OnCommitContentListener() {
                    @Override
                    public boolean onCommitContent(InputContentInfoCompat inputContentInfo,
                                                   int flags, Bundle opts) {
                        // read and display inputContentInfo asynchronously
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && (flags &
                                InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0) {
                            try {
                                inputContentInfo.requestPermission();

                            } catch (Exception e) {
                                return false; // return false if failed
                            }
                        }
                        mGifSelectedListener = (GifSelectedListener) getContext();
                        // read and display inputContentInfo asynchronously.
                        // call inputContentInfo.releasePermission() as needed.
                        Log.d("MyEditext","link "+inputContentInfo.getLinkUri().toString());
                      //  ic.commitText(inputContentInfo.getLinkUri().toString(),inputContentInfo.getLinkUri().toString().length());
                        Log.d("MyEditext","content "+inputContentInfo.getContentUri().toString());
                        mGifSelectedListener.OnGifSelected(inputContentInfo.getLinkUri().toString());

                        inputContentInfo.releasePermission();

                        //Toast.makeText(getContext(), "Gif image is not currently support it", Toast.LENGTH_SHORT).show();

                        return true;  // return true if succeeded
                    }
                };
        return InputConnectionCompat.createWrapper(ic, editorInfo, callback);
    }








}
