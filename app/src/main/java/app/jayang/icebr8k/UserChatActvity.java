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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import app.jayang.icebr8k.Adapter.RecyclerAdapter;
import app.jayang.icebr8k.Adapter.UserMessageAdapter;
import app.jayang.icebr8k.Modle.MyDateFormatter;
import app.jayang.icebr8k.Modle.OnLoadMoreListener;
import app.jayang.icebr8k.Modle.UserMessage;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class UserChatActvity extends SwipeBackActivity implements View.OnTouchListener {
    private final String VIEWTYPE_TEXT = "text";
    private final String VIEWTYPE_VIDEO = "video";
    private final String VIEWTYPE_IMAGE= "image";
    private final String VIEWTYPE_VOICE = "voice";

    private final String VIEWTYPE_LOAD = "load";
    private final String VIEWTYPE_DATE = "date";
    private final String VIEWTYPE_TYPE = "type";
    private final String LOAD_ID ="loading";
    private final String TYPE_ID ="typing";


    private EditText editText;
    private FirebaseUser currentUser;
    private RecyclerView mRecyclerView;
    private android.support.v7.widget.Toolbar toolbar;
    private android.support.v7.widget.GridLayout mGridLayout;
    private ImageView send,attachment,voice,voiceChat,image,video;
    private Boolean typingStarted =false;
    private SwipeBackLayout swipeBackLayout;
    private ArrayList<UserMessage> mMessages;
    private UserMessageAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private   MenuItem inChatItem;
    private boolean on =true;
    protected Handler handler;
    private   UserMessage isTypingMessage, loadingMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_chat);
        editText = (EditText) findViewById(R.id.userChat_input);
        send = (ImageView) findViewById(R.id.userChat_send);
        attachment = (ImageView) findViewById(R.id.userChat_attachment);
        //attachment.setOnTouchListener(this);

        voice = (ImageView) findViewById(R.id.userChat_voicemessage);
        voice.setOnTouchListener(this);

        image = (ImageView) findViewById(R.id.userChat_image);
        image.setOnTouchListener(this);

        voiceChat = (ImageView) findViewById(R.id.userChat_voicechat);
        voiceChat.setOnTouchListener(this);

        video = (ImageView) findViewById(R.id.userChat_video);
        video.setOnTouchListener(this);


        send.setEnabled(false);
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.userChat_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Stephen Hearrington");
        mGridLayout = (android.support.v7.widget.GridLayout) findViewById(R.id.gridLayout_attachments);



        mMessages =new ArrayList<>();
        handler = new Handler();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

       //istyping message
        isTypingMessage = new UserMessage();
        isTypingMessage.setMessageId(TYPE_ID);
        isTypingMessage.setMessageType(VIEWTYPE_TYPE);

        //isloading message
        loadingMessage = new UserMessage();
        loadingMessage.setMessageId(LOAD_ID);
        loadingMessage.setMessageType(VIEWTYPE_LOAD);







        mRecyclerView = (RecyclerView) findViewById(R.id.userChat_list);
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(true);
        // use a linear layout manager
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new UserMessageAdapter(mMessages,mRecyclerView);
        mAdapter.setHasStableIds(true);
        // set the adapter object to the Recyclerview

        loadMessages();
        mRecyclerView.setAdapter(mAdapter);

        //  mAdapter.notifyDataSetChanged();


        if(!mMessages.isEmpty()){
            mRecyclerView.scrollToPosition(0);
        }


      mAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
          @Override
          public void onLoadMore() {
              Toast.makeText(UserChatActvity.this, "loading more messages ", Toast.LENGTH_SHORT).show();
              if(!mMessages.isEmpty()){
                  mMessages.add(loadingMessage);
                  mAdapter.notifyItemInserted(mMessages.size()-1);

              }
             handler.postDelayed(new Runnable() {
                 @Override
                 public void run() {
                     int index = mMessages.indexOf(loadingMessage);
                     mMessages.remove(index);
                     mAdapter.notifyItemRemoved(index);

                     loadMessages();




                 }
             },3000);
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
                hideAttachment();



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
                        editable.toString().trim().length() ==1  &&!typingStarted) {
                    final int length =editable.toString().trim().length();

                    //Log.i(TAG, “typing started event…”);
                    addIsTypingMessage();
                    typingStarted =true;



                   // Toast.makeText(UserChatActvity.this, "typing", Toast.LENGTH_SHORT).show();


                    //send typing started status

                } else if (editable.toString().trim().length() == 0 && typingStarted) {

                    //Log.i(TAG, “typing stopped event…”);
                   // Toast.makeText(UserChatActvity.this, "not typing", Toast.LENGTH_SHORT).show();
                    removeIsTypingMessage();

                    typingStarted = false;

                    //send typing stopped status

                }
            }
        });

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!b){
                    typingStarted = false;

                }else{
                    mGridLayout.setVisibility(View.GONE);
                }

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
                editText.clearFocus();;
                if(mGridLayout.getVisibility() == View.GONE){
                 mGridLayout.setVisibility(View.VISIBLE);
                 attachment.setSelected(true);
                 hideKeyboard();
                }else{
                  hideAttachment();
                    hideKeyboard();
                }

            }
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(dy<0){
                    hideKeyboard();
                    hideAttachment();
                }
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.one_to_one_chat_menu, menu);
        final MenuItem item = menu.findItem(R.id.notification_switch);
       inChatItem = menu.findItem(R.id.inChat_indicator);
        //make inchat Indicator onclickable
        inChatItem.setEnabled(false);
        if(on){
           item.setIcon(R.drawable.icon_notificastion_on_light);

            on =false;
        }else{
         item.setIcon(R.drawable.icon_notification_off_light);

            on =true;
        }

        // return true so that the menu pop up is opened
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.notification_switch:
                if(on){
                  item.setIcon(R.drawable.icon_notificastion_on_light);
                  on =false;
                }else{
                    item.setIcon(R.drawable.icon_notification_off_light);
                  on =true;
                }
                return  true;
        }
        return super.onOptionsItemSelected(item);
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
        if(mGridLayout.getVisibility() ==View.VISIBLE){
            hideAttachment();
        }else{
            hideKeyboard();
            finish();
            overridePendingTransition(0,R.anim.slide_to_right);
        }

    }

    private  void hideKeyboard(){
        //hide keyboard
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private  void hideAttachment(){
        if(mGridLayout.getVisibility() == View.VISIBLE){
            mGridLayout.setVisibility(View.GONE);
            attachment.setSelected(false);
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
        generateDateHeaders(mMessages);
        mRecyclerView.scrollToPosition(0);

    }

    private void addIsTypingMessage(){

        if(!mMessages.contains(isTypingMessage)) {
            mMessages.add(0, isTypingMessage);
            mAdapter.notifyItemInserted(0);
            mRecyclerView.scrollToPosition(0);
        }

    }

    private void removeIsTypingMessage(){
        int index = mMessages.indexOf(isTypingMessage);
        mMessages.remove(index);
        mAdapter.notifyItemRemoved(index);
    }

    private void generateDateHeaders(ArrayList<UserMessage> messages) {

        for (int i = 0; i < messages.size(); i++) {
            UserMessage message = messages.get(i);
            if (messages.size() > i + 1) {
                UserMessage nextMessage = messages.get(i + 1);

                if (message.getMessageType().equals(VIEWTYPE_TEXT) && nextMessage.getMessageType().equals(VIEWTYPE_TEXT) &&
                        !MyDateFormatter.isSameDay(new Date(message.getTimestamp()),new Date(nextMessage.getTimestamp()))) {
                    UserMessage dateHeaderMessage = new UserMessage();
                    dateHeaderMessage.setMessageId(VIEWTYPE_DATE + UUID.randomUUID().toString());
                    dateHeaderMessage.setTimestamp(message.getTimestamp());
                    dateHeaderMessage.setMessageType(VIEWTYPE_DATE);
                    mMessages.add(i+1, dateHeaderMessage);

                }
            }else {

                if(message.getMessageType().equals(VIEWTYPE_TEXT)){
                    UserMessage dateHeaderMessage = new UserMessage();
                    dateHeaderMessage.setMessageId(VIEWTYPE_DATE + UUID.randomUUID().toString());
                    dateHeaderMessage.setTimestamp(message.getTimestamp());
                    dateHeaderMessage.setMessageType(VIEWTYPE_DATE);
                    mMessages.add(dateHeaderMessage);
                }

            }
        }
        mAdapter.notifyDataSetChanged();
        mAdapter.setLoaded();
    }


    private void loadMessages(){

        Random rnd = new Random();
        Date date = new Date(Math.abs(System.currentTimeMillis() - rnd.nextLong()));
        Date date2 = new Date(Math.abs(System.currentTimeMillis() - rnd.nextLong()));
        UserMessage message1 = new UserMessage(UUID.randomUUID().toString(),currentUser.getUid(),
                date.getTime(),VIEWTYPE_TEXT,UUID.randomUUID().toString());
        UserMessage message2 = new UserMessage(UUID.randomUUID().toString(),"HISs3hwOwdN2HEy1ZdpNo7XjsxZ2",
               date2.getTime(),VIEWTYPE_TEXT,UUID.randomUUID().toString());

        for(int i =0 ; i<10; i++){
            mMessages.add(message1);
            mMessages.add(message2);
        }
        generateDateHeaders(mMessages);




    }

    private void loadMessages2(){
        for(int i =0 ; i<2; i++){
            UserMessage message1 = new UserMessage(String.valueOf(i),currentUser.getUid(),
                    new Date().getTime(),VIEWTYPE_TEXT,UUID.randomUUID().toString());
            UserMessage message2 = new UserMessage(String.valueOf(i+1),"HISs3hwOwdN2HEy1ZdpNo7XjsxZ2",
                    new Date().getTime(),VIEWTYPE_TEXT,UUID.randomUUID().toString());
            mMessages.add(message1);
            mMessages.add(message2);
        }
        generateDateHeaders(mMessages);







    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN)
            {
                view.setAlpha(0.6f);
            } else {
                view.setAlpha(1f);
            }


        return true;
    }

}
