package app.jayang.icebr8k;

import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import app.jayang.icebr8k.Adapter.RecyclerAdapter;
import app.jayang.icebr8k.Adapter.UserMessageAdapter;
import app.jayang.icebr8k.Modle.OnLoadMoreListener;
import app.jayang.icebr8k.Modle.UserMessage;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class UserChatActvity extends SwipeBackActivity {
    private final String VIEWTYPE_TEXT = "text";
    private final String VIEWTYPE_VIDEO = "video";
    private final String VIEWTYPE_IMAGE= "image";
    private final String VIEWTYPE_VOICE = "voice";
    private final String VIEWTYPE_LOAD = "load";
    private final String VIEWTYPE_DATE = "date";
    private final String VIEWTYPE_TYPE = "type";
    private final String LOAD_ID ="loading";


    private EditText editText;
    private FirebaseUser currentUser;
    private RecyclerView mRecyclerView;
    private android.support.v7.widget.Toolbar toolbar;
    private ImageView send,attachment;
    private Boolean typingStarted =false;
    private SwipeBackLayout swipeBackLayout;
    private ArrayList<UserMessage> mMessages;
    private UserMessageAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    protected Handler handler;


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

        mMessages =new ArrayList<>();
        handler = new Handler();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();




        mRecyclerView = (RecyclerView) findViewById(R.id.userChat_list);
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(true);
        // use a linear layout manager
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new UserMessageAdapter(mMessages,mRecyclerView);
        mAdapter.setHasStableIds(true);
        // set the adapter object to the Recyclerview
        mRecyclerView.setAdapter(mAdapter);

        //  mAdapter.notifyDataSetChanged();

        loadMessages();
        if(!mMessages.isEmpty()){
            mRecyclerView.scrollToPosition(0);
        }


      mAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
          @Override
          public void onLoadMore() {
              final UserMessage loadMessage = new UserMessage();
             loadMessage.setMessageId(LOAD_ID);
              loadMessage.setMessageType(VIEWTYPE_LOAD);
              mMessages.add(loadMessage);
              mRecyclerView.post(new Runnable() {
                  @Override
                  public void run() {
                      mAdapter.notifyItemInserted(mMessages.size()-1);
                  }
              });

              handler.postDelayed(new Runnable() {
                  @Override
                  public void run() {
                      if(!mMessages.isEmpty()){
                         mMessages.remove(mMessages.size()-1);
                          mAdapter.notifyItemRemoved(mMessages.size());
                          loadMessages2();
                          mAdapter.setLoaded();
                      }

                  }
              },2000);

          }
      });




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
                if (!TextUtils.isEmpty(editable.toString()) &&
                        editable.toString().trim().length() == 1) {

                    //Log.i(TAG, “typing started event…”);

                   // Toast.makeText(UserChatActvity.this, "typing", Toast.LENGTH_SHORT).show();
                    UserMessage isTypingMessage = new UserMessage();
                    isTypingMessage.setMessageId(LOAD_ID);
                    isTypingMessage.setMessageType(VIEWTYPE_TYPE);
                    mMessages.add(0,isTypingMessage);
                    mAdapter.notifyItemInserted(0);
                    mRecyclerView.scrollToPosition(0);
                    typingStarted =true;

                    //send typing started status

                } else if (editable.toString().trim().length() == 0 && typingStarted) {

                    //Log.i(TAG, “typing stopped event…”);
                   // Toast.makeText(UserChatActvity.this, "not typing", Toast.LENGTH_SHORT).show();
                    mMessages.remove(0);
                    mAdapter.notifyItemRemoved(0);
                    typingStarted = false;

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
                addMessage();

            }
        });

        //attachment clicked event
        attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(dy<0){
                    hideKeyboard();
                }
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

    private void addMessage(){
        String text = editText.getText().toString();
        UserMessage message1 = new UserMessage(text,currentUser.getUid(),
                new Date().getTime(),VIEWTYPE_TEXT,UUID.randomUUID().toString());
        editText.setText("");
        mMessages.add(0,message1);
        mAdapter.notifyItemInserted(0);
        mRecyclerView.scrollToPosition(0);

    }

    private void loadMessages(){
        UserMessage message1 = new UserMessage(UUID.randomUUID().toString(),currentUser.getUid(),
                new Date().getTime(),VIEWTYPE_TEXT,UUID.randomUUID().toString());
        UserMessage message2 = new UserMessage(UUID.randomUUID().toString(),"HISs3hwOwdN2HEy1ZdpNo7XjsxZ2",
                new Date().getTime(),VIEWTYPE_TEXT,UUID.randomUUID().toString());

        for(int i =0 ; i<10; i++){
            mMessages.add(message1);
            mMessages.add(message2);
        }

        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });

    }

    private void loadMessages2(){



         int oldSize = mMessages.size();

        for(int i =0 ; i<2; i++){
            UserMessage message1 = new UserMessage(String.valueOf(i),currentUser.getUid(),
                    new Date().getTime(),VIEWTYPE_TEXT,UUID.randomUUID().toString());
            UserMessage message2 = new UserMessage(String.valueOf(i+1),"HISs3hwOwdN2HEy1ZdpNo7XjsxZ2",
                    new Date().getTime(),VIEWTYPE_TEXT,UUID.randomUUID().toString());
            mMessages.add(message1);
            mMessages.add(message2);


        }
        mAdapter.notifyItemRangeInserted(oldSize, mMessages.size() - oldSize);






    }

}
