package app.jayang.icebr8k;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import app.jayang.icebr8k.Adapter.UserMessageAdapter;
import app.jayang.icebr8k.Utility.MyDateFormatter;
import app.jayang.icebr8k.Modle.MyEditText;
import app.jayang.icebr8k.Modle.OnLoadMoreListener;
import app.jayang.icebr8k.Modle.UserMessage;
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class UserChatActvity extends SwipeBackActivity implements View.OnTouchListener,View.OnClickListener,View.OnLongClickListener {
    private static final int MAX_COUNT = 20;
    private final String VIEWTYPE_TEXT = "text";
    private final String VIEWTYPE_VIDEO = "video";
    private final String VIEWTYPE_IMAGE= "image";
    private final String VIEWTYPE_VOICE = "voice";
    private final String VIEWTYPE_TEXT_PENDING = "text_pend";

    private final String VIEWTYPE_LOAD = "load";
    private final String VIEWTYPE_DATE = "date";
    private final String VIEWTYPE_TYPE = "type";
    private final String LOAD_ID ="loading";
    private final String TYPE_ID ="typing";

    private final String user2Uid = "WDmiRBfGNORfyc5di1YzOMeAeVr2";

    private MyEditText editText;
    private Long lastTimeStamp;
    private FirebaseUser currentUser;
    private RecyclerView mRecyclerView;
    private BroadcastReceiver tickReceiver;
    private android.support.v7.widget.Toolbar toolbar;
    private android.support.v7.widget.GridLayout mGridLayout;
    private ImageView send,attachment,voice,voiceChat,image,video;
    private SwipeBackLayout swipeBackLayout;
    private ArrayList<UserMessage> mMessages,tempMessages;
    private UserMessageAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private Menu mOptionsMenu;
    private   MenuItem inChatItem;
    private  HashMap<String,String> userPhotoUrlMap;
    private Boolean mute =false,loaded =false;
    protected Handler handler;
    private   UserMessage isTypingMessage, loadingMessage;
    private long timestamp;
    private DatabaseReference senderMessageRef , receiverMessageRef,isTypingRef,inChatRef,muteRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_chat);
        editText = (MyEditText) findViewById(R.id.userChat_input);
        send = (ImageView) findViewById(R.id.userChat_send);
        attachment = (ImageView) findViewById(R.id.userChat_attachment);



        voice = (ImageView) findViewById(R.id.userChat_voicemessage);
        voice.setOnTouchListener(this);
        voice.setOnClickListener(this);
        voice.setOnLongClickListener(this);

        image = (ImageView) findViewById(R.id.userChat_image);
        image.setOnTouchListener(this);
        image.setOnClickListener(this);
        image.setOnLongClickListener(this);

        voiceChat = (ImageView) findViewById(R.id.userChat_voicechat);
        voiceChat.setOnTouchListener(this);
        voiceChat.setOnClickListener(this);
        voiceChat.setOnLongClickListener(this);

        video = (ImageView) findViewById(R.id.userChat_video);
        video.setOnTouchListener(this);
        video.setOnClickListener(this);
        video.setOnLongClickListener(this);


        send.setEnabled(false);

        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.userChat_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Stephen Hearrington");
        timestamp = new Date().getTime();
        MyDateFormatter myDateFormatter = new MyDateFormatter();
        userPhotoUrlMap =  new HashMap<>();
        setUserPhotoUrlMap(user2Uid);

        getSupportActionBar().setSubtitle( myDateFormatter.lastSeenConverter(timestamp));
        mGridLayout = (android.support.v7.widget.GridLayout) findViewById(R.id.gridLayout_attachments);



        mMessages =new ArrayList<>();
        tempMessages =new ArrayList<>();
        handler = new Handler();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

       //istyping message
        isTypingMessage = new UserMessage();
        isTypingMessage.setMessageid(TYPE_ID);
        isTypingMessage.setSenderid(user2Uid);
        isTypingMessage.setMessagetype(VIEWTYPE_TYPE);

        //isloading message
        loadingMessage = new UserMessage();
        loadingMessage.setMessageid(LOAD_ID);
        loadingMessage.setMessagetype(VIEWTYPE_LOAD);


        mRecyclerView = (RecyclerView) findViewById(R.id.userChat_list);

        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(true);
        // use a linear layout manager
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.getItemAnimator().setChangeDuration(0);
        mAdapter = new UserMessageAdapter(mMessages,mRecyclerView,userPhotoUrlMap);

        // set the adapter object to the Recyclerview

        //loadMessages();
        mRecyclerView.setAdapter(mAdapter);

        //  mAdapter.notifyDataSetChanged();


        if(!mMessages.isEmpty()){
            mRecyclerView.scrollToPosition(0);
        }
///////////////////////////////setup databaseref///////////////////////////////
        senderMessageRef = FirebaseDatabase.getInstance().getReference().child("UserMessages").child(currentUser.getUid()).child(user2Uid);
        receiverMessageRef =FirebaseDatabase.getInstance().getReference().child("UserMessages").child(user2Uid).child(currentUser.getUid());
        isTypingRef = receiverMessageRef.child("istyping");
        inChatRef = senderMessageRef.child("inchat");
        muteRef = senderMessageRef.child("mute");

////////////////////////////////////////////////Custom Methods/////////////////////////////////////
        setTimeChsngeListener();
        setIstypingListener();
        loadMessages();




//////////////////////////////////////////////////Listeners///////////////////////////////////////
      mAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
          @Override
          public void onLoadMore() {
           loadMoreHistory();

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
                    updateIsTyping(false);
                } else {
                    send.setEnabled(true);
                    updateIsTyping(true);
                }
            }

            @Override
            public void afterTextChanged( Editable editable) {



            }
        });
        voice.setOnClickListener(this);

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!b){
                    updateIsTyping(false);

                }else{
                    hideAttachment();
                }

            }
        });








        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();

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
        mOptionsMenu =menu;
        setMuteListener();
       inChatItem = menu.findItem(R.id.inChat_indicator);
        //make inchat Indicator onclickable
        inChatItem.setEnabled(false);



        // return true so that the menu pop up is opened
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.notification_switch:
                if(mute){
                    item.setIcon(R.drawable.icon_notificastion_on_light);
                    setMute(false);
                    Toast.makeText(this, "notification on", Toast.LENGTH_SHORT).show();
                     mute =false;
                }else{
                    item.setIcon(R.drawable.icon_notification_off_light);
                    Toast.makeText(this, "notification off", Toast.LENGTH_SHORT).show();
                    setMute(true);
                    mute =true;
                }
                return  true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        editText.clearFocus();
        hideKeyboard();
        finish();
        overridePendingTransition(0,R.anim.slide_to_right);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(mGridLayout.getVisibility() ==View.VISIBLE){
            hideAttachment();
        }else{
            editText.clearFocus();
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

    private void updateIsTyping(boolean istyping){
        senderMessageRef.child("istyping").setValue(istyping);


    }



    private void inChat(boolean inChat){
        inChatRef.setValue(inChat);
    }

    private void setMute(boolean isMute){
        muteRef.setValue(isMute);

    }
    private  void setMuteListener(){

        muteRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(mOptionsMenu!=null) {
                MenuItem item = mOptionsMenu.findItem(R.id.notification_switch);
                 mute = dataSnapshot.getValue(Boolean.class);
                if(mute ==null || !mute){
                   item.setIcon(R.drawable.icon_notificastion_on_light);
                }else{
                    item.setIcon(R.drawable.icon_notification_off_light);
                }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void setUserPhotoUrlMap(final String uid) {
        if(uid!=null &&!uid.isEmpty()) {


            DatabaseReference urlRef = FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(uid)
                    .child("photourl");
            urlRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                  String url = dataSnapshot.getValue(String.class);
                  if(url!=null){
                      userPhotoUrlMap.put(uid,url);
                  }
                    mAdapter.notifyDataSetChanged();


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }

    private void setTimeChsngeListener(){
        tickReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
                   getSupportActionBar().setSubtitle(MyDateFormatter.lastSeenConverter(timestamp));
                }
            }
        };

        registerReceiver(tickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK)); // register the broadcast receiver to receive TIME_TICK

    }


    private void sendMessage(){
        String text = editText.getText().toString();
        final UserMessage message = new UserMessage(text,currentUser.getUid(),
               VIEWTYPE_TEXT_PENDING,UUID.randomUUID().toString().replaceAll("-",""),
                new Date().getTime());
        editText.setText("");
        int index;
        if(mMessages.contains(isTypingMessage)){
            index = mMessages.indexOf(isTypingMessage);
            index++;

        }else{
            index =0;

        }

        if(!mMessages.isEmpty()) {
            UserMessage prevMessage = mMessages.get(0);

            if (!message.getMessagetype().equals(VIEWTYPE_DATE) && !prevMessage.getMessagetype().equals(VIEWTYPE_DATE) &&
                    !MyDateFormatter.isSameDay(new Date(message.getTimestamp()), new Date(prevMessage.getTimestamp()))) {
                UserMessage dateHeaderMessage = new UserMessage();
                dateHeaderMessage.setMessageid(VIEWTYPE_DATE + UUID.randomUUID().toString());
                dateHeaderMessage.setTimestamp(message.getTimestamp());
                dateHeaderMessage.setMessagetype(VIEWTYPE_DATE);
                mMessages.add(index, dateHeaderMessage);
                mMessages.add(index, message);
                mAdapter.notifyItemRangeInserted(index, mMessages.size()-1);
            }else{
                mMessages.add(index, message);
                mAdapter.notifyItemInserted(index);
            }
        }else {
            UserMessage dateHeaderMessage = new UserMessage();
            dateHeaderMessage.setMessageid(VIEWTYPE_DATE + UUID.randomUUID().toString());
            dateHeaderMessage.setTimestamp(message.getTimestamp());
            dateHeaderMessage.setMessagetype(VIEWTYPE_DATE);
            mMessages.add(0, dateHeaderMessage);
            mMessages.add(0, message);
            mAdapter.notifyItemRangeInserted(0, mMessages.size()-1);
        }
        mRecyclerView.scrollToPosition(0);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateMessagetoFirebase(message);
            }
        },666);


    }

    private void updateMessagetoFirebase(final UserMessage message){
        message.setMessagetype(VIEWTYPE_TEXT);
        senderMessageRef.child("chathistory").
                child(message.getMessageid()).setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                receiverMessageRef.child("chathistory").
                        child(message.getMessageid()).setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if(mMessages.contains(message)){
                            int index = mMessages.indexOf(message);
                            message.setTimestamp(new Date().getTime());
                            mMessages.set(index,message);
                            mAdapter.notifyItemChanged(index);
                        }

                    }
                });

            }
        });
    }

    private void setIstypingListener(){
        isTypingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //default value
                boolean isTyping;
                if(dataSnapshot.getValue(Boolean.class)!=null){
                   // Toast.makeText(UserChatActvity.this, "changed", Toast.LENGTH_SHORT).show();
                    isTyping =dataSnapshot.getValue(Boolean.class);
                    if(isTyping){
                        addIsTypingMessage();
                    }else{
                        removeIsTypingMessage();
                    }
                }else{
                    removeIsTypingMessage();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addLoadMoreMessage(){
        if(!mMessages.isEmpty()){
            mMessages.add(loadingMessage);
            mRecyclerView.post(new Runnable() {
                public void run() {
                    mAdapter.notifyItemInserted(mMessages.size()-1);
                }
            });


        }

    }





    private void addIsTypingMessage(){

        if(!mMessages.contains(isTypingMessage)) {
            mMessages.add(0, isTypingMessage);
            mAdapter.notifyItemInserted(0);
            mRecyclerView.scrollToPosition(0);
        }

    }

    private void removeIsTypingMessage(){
        if(mMessages.contains(isTypingMessage)) {
            int index = mMessages.indexOf(isTypingMessage);
            mMessages.remove(index);
            mAdapter.notifyItemRemoved(index);
        }
    }

    private void generateDateHeaders() {


        for (int i = 0; i < mMessages.size(); i++) {
            UserMessage message = mMessages.get(i);

            if (mMessages.size() > i + 1) {
                UserMessage nextMessage = mMessages.get(i + 1);

                if (message.getMessagetype().equals(VIEWTYPE_TEXT) && nextMessage.getMessagetype().equals(VIEWTYPE_TEXT) &&
                        !MyDateFormatter.isSameDay(new Date(message.getTimestamp()),new Date(nextMessage.getTimestamp()))) {
                    UserMessage dateHeaderMessage = new UserMessage();
                    dateHeaderMessage.setMessageid(VIEWTYPE_DATE + UUID.randomUUID().toString());
                    dateHeaderMessage.setTimestamp(message.getTimestamp());
                    dateHeaderMessage.setMessagetype(VIEWTYPE_DATE);
                    mMessages.add(i+1, dateHeaderMessage);


                }
            }else {

                if(message.getMessagetype().equals(VIEWTYPE_TEXT)){
                    UserMessage dateHeaderMessage = new UserMessage();
                    dateHeaderMessage.setMessageid(VIEWTYPE_DATE + UUID.randomUUID().toString());
                    dateHeaderMessage.setTimestamp(message.getTimestamp());
                    dateHeaderMessage.setMessagetype(VIEWTYPE_DATE);
                    mMessages.add(dateHeaderMessage);


                }

            }
        }
        if(mMessages.contains(loadingMessage)){
            int index = mMessages.indexOf(loadingMessage);
            mMessages.remove(index);
            mAdapter.notifyItemRemoved(index);

        }
        mAdapter.notifyDataSetChanged();

        mAdapter.setLoaded();
        loaded =true;
    }




    private void loadMessages(){
       DatabaseReference  loadMsgRef =
               senderMessageRef.child("chathistory");

        loadMsgRef.orderByChild("timestamp").limitToLast(MAX_COUNT).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("userMessage","added "+ dataSnapshot);
                final UserMessage message = dataSnapshot.getValue(UserMessage.class);
                if(message!=null &&
                        !mMessages.contains(message)&& !loaded){
                    tempMessages.add(message);

                }else {
                    if (message != null &&
                            !mMessages.contains(message) && loaded) {
                        removeIsTypingMessage();
                        Log.d("userMessage","here");
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(!mMessages.isEmpty()) {
                                    UserMessage prevMessage = mMessages.get(0);
                                    Log.d("userMessage","here2");
                                    if (message.getMessagetype().equals(VIEWTYPE_TEXT) && prevMessage.getMessagetype().equals(VIEWTYPE_TEXT) &&
                                            !MyDateFormatter.isSameDay(new Date(message.getTimestamp()), new Date(prevMessage.getTimestamp()))) {
                                        UserMessage dateHeaderMessage = new UserMessage();
                                        dateHeaderMessage.setMessageid(VIEWTYPE_DATE + UUID.randomUUID().toString());
                                        dateHeaderMessage.setTimestamp(message.getTimestamp());
                                        dateHeaderMessage.setMessagetype(VIEWTYPE_DATE);
                                        mMessages.add(0, dateHeaderMessage);
                                        mMessages.add(0, message);
                                        mAdapter.notifyItemRangeInserted(0, mMessages.size()-1);
                                        Log.d("userMessage","here3");
                                    }else {
                                        mMessages.add(0, message);
                                        mAdapter.notifyItemInserted(0);
                                        Log.d("userMessage","here4");
                                    }
                                }else {
                                    Log.d("userMessage","here5");
                                    UserMessage dateHeaderMessage = new UserMessage();
                                    dateHeaderMessage.setMessageid(VIEWTYPE_DATE + UUID.randomUUID().toString());
                                    dateHeaderMessage.setTimestamp(message.getTimestamp());
                                    dateHeaderMessage.setMessagetype(VIEWTYPE_DATE);
                                    mMessages.add(0, dateHeaderMessage);
                                    mMessages.add(0, message);
                                    mAdapter.notifyDataSetChanged();
                                }
                                    mRecyclerView.scrollToPosition(0);


                            }
                        },300);


                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d("userMessage",dataSnapshot.getKey());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        loadMsgRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("userMessage","done "+dataSnapshot.getKey());
                if(!tempMessages.isEmpty() &&!loaded){
                    lastTimeStamp =tempMessages.get(0).getTimestamp();
                    Collections.reverse(tempMessages);
                    mMessages.addAll(tempMessages);
                    generateDateHeaders();

                }
                tempMessages.clear();
                loaded =true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });






    }

    private void loadMoreHistory(){

        tempMessages.clear();;

        DatabaseReference  loadMoreMsgRef =
                senderMessageRef.child("chathistory");
        if(lastTimeStamp!=null) {
            loadMoreMsgRef.orderByChild("timestamp").endAt(lastTimeStamp).limitToLast(MAX_COUNT).
                    addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                        UserMessage message = childSnap.getValue(UserMessage.class);
                        Log.d("userMessage",message.getText().toString());
                      //  Toast.makeText(UserChatActvity.this, "text "+ message.getText(), Toast.LENGTH_SHORT).show();
                        if (message != null
                                && !mMessages.contains(message)) {

                            tempMessages.add(message);

                        }
                    }
                    if(!tempMessages.isEmpty()){
                        lastTimeStamp =tempMessages.get(0).getTimestamp();
                        addLoadMoreMessage();

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Collections.reverse(tempMessages);
                                mMessages.addAll(tempMessages);
                                generateDateHeaders();
                            }
                        },666);
                    }





                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }




    }




    @Override
    public boolean onLongClick(View view) {
        Toast.makeText(this, "this feature is coming soon", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onClick(View view) {
        Toast.makeText(this, "this feature is coming soon", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), 1);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN)
            {
                view.setAlpha(0.6f);
            } else {
                view.setAlpha(1f);
            }


        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        getSupportActionBar().setSubtitle(MyDateFormatter.lastSeenConverter(timestamp));
        inChat(true);

    }

    @Override
    protected void onStop() {
        super.onStop();
        inChat(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(tickReceiver!=null) {
            unregisterReceiver(tickReceiver);
        }
        updateIsTyping(false);
    }


}
