package app.jayang.icebr8k;

import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class UserChatActvity extends SwipeBackActivity {
    private final String VIEWTYPE_TEXT = "text";
    private final String VIEWTYPE_VIDEO = "video";
    private final String VIEWTYPE_IMAGE= "image";
    private final String VIEWTYPE_VOICE = "voice";


    private EditText editText;
    private android.support.v7.widget.Toolbar toolbar;
    private ImageView send,attachment;
    private Boolean typingStarted =false;
    private SwipeBackLayout swipeBackLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_chat);
        editText = (EditText) findViewById(R.id.userChat_input);
        send = (ImageView) findViewById(R.id.userChat_send);
        attachment = (ImageView) findViewById(R.id.userChat_attachment);
        send.setEnabled(false);
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.userChat_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        swipeBackLayout = getSwipeBackLayout();
        swipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);


        swipeBackLayout.addSwipeListener(new SwipeBackLayout.SwipeListener() {
            @Override
            public void onScrollStateChange(int state, float scrollPercent) {

            }

            @Override
            public void onEdgeTouch(int edgeFlag) {
                hideKeyboard();
                editText.clearFocus();

            }

            @Override
            public void onScrollOverThreshold() {

            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().trim().isEmpty()){
                    send.setEnabled(false);
                } else {
                    send.setEnabled(true);
                }

            }

            @Override
            public void afterTextChanged( Editable editable) {
                if (!TextUtils.isEmpty(editable.toString()) && editable.toString().trim().length() == 1) {

                    //Log.i(TAG, “typing started event…”);

                    Toast.makeText(UserChatActvity.this, "typing", Toast.LENGTH_SHORT).show();
                    typingStarted =true;

                    //send typing started status

                } else if (editable.toString().trim().length() == 0 && typingStarted) {

                    //Log.i(TAG, “typing stopped event…”);

                    typingStarted = false;
                    Toast.makeText(UserChatActvity.this, "not typing", Toast.LENGTH_SHORT).show();

                    //send typing stopped status

                }
            }
        });

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                Toast.makeText(UserChatActvity.this, String.valueOf(b), Toast.LENGTH_SHORT).show();
            }
        });


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               editText.setText("");
            }
        });


        attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


    }

    @Override
    public boolean onSupportNavigateUp() {
        editText.clearFocus();
        finish();
        overridePendingTransition(0,R.anim.slide_to_right);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        editText.clearFocus();
        overridePendingTransition(0,R.anim.slide_to_right);
    }

    private  void hideKeyboard(){
        //hide keyboard
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    //Todo below method

    private void isTyping(boolean isTyping){}

    private void inChat(boolean inChat){}

    private void setMute(boolean isMute){}

}
