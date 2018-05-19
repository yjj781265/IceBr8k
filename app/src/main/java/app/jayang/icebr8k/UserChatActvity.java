package app.jayang.icebr8k;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import app.jayang.icebr8k.Adapter.UserMessageAdapter;
import app.jayang.icebr8k.Utility.MyDateFormatter;
import app.jayang.icebr8k.Modle.MyEditText;
import app.jayang.icebr8k.Modle.OnLoadMoreListener;
import app.jayang.icebr8k.Modle.UserMessage;
import app.jayang.icebr8k.Utility.SendNotification;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class UserChatActvity extends SwipeBackActivity implements View.OnTouchListener, View.OnClickListener, View.OnLongClickListener
,RecyclerView.OnChildAttachStateChangeListener{
    private static final int MAX_COUNT = 20;
    private final String VIEWTYPE_TEXT = "text";
    private final String VIEWTYPE_VIDEO = "video";
    private final String VIEWTYPE_IMAGE = "image";
    private final String VIEWTYPE_VOICE = "voice";
    private final String VIEWTYPE_TEXT_PENDING = "text_pend";

    private final String VIEWTYPE_LOAD = "load";
    private final String VIEWTYPE_HEADER = "header";
    private final String VIEWTYPE_TYPE = "type";
    private final String LOAD_ID = "loading";
    private final String TYPE_ID = "typing";

    private  String user2Uid = "WDmiRBfGNORfyc5di1YzOMeAeVr2";
    //EUcJQFrdj5g7MA4Xkyzc57UnjKt1
    //WDmiRBfGNORfyc5di1YzOMeAeVr2

    private MyEditText editText;
    private TextView toast;
    private Long lastTimeStamp;
    private FirebaseUser currentUser;
    private RecyclerView mRecyclerView;
    private BroadcastReceiver tickReceiver;
    private android.support.v7.widget.Toolbar toolbar;
    private android.support.v7.widget.GridLayout mGridLayout;
    private ImageView send, attachment, voice, voiceChat, image, video;
    private SwipeBackLayout swipeBackLayout;
    private ArrayList<UserMessage> mMessages, tempMessages;
    private UserMessageAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private Menu mOptionsMenu;
    private MenuItem inChatItem;
    private HashMap<String, String> userPhotoUrlMap;
    private Boolean mute = false, loaded = false,user2Inchat =false;
    protected Handler handler;
    private UserMessage isTypingMessage, loadingMessage;
    private Long lastSeen;
    private Integer firstVisablePos =0 , lastVisablePos =0,toastThreshhold =2;
    private String  name;
    private DatabaseReference senderMessageRef, receiverMessageRef, isTypingRef, inChatRef, muteRef
            , subTitleRef,titleRef,user2InchatRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_chat);
        OneSignal.clearOneSignalNotifications();
        editText = (MyEditText) findViewById(R.id.userChat_input);
        send = (ImageView) findViewById(R.id.userChat_send);
        attachment = (ImageView) findViewById(R.id.userChat_attachment);
        toast = (TextView)findViewById(R.id.userChat_toast);
        toast.setVisibility(View.GONE);
        toast.setOnTouchListener(this);
        if(getIntent()!=null) {
            user2Uid = getIntent().getExtras().getString("chatId");
            name = getIntent().getExtras().getString("chatName");

        }



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
        userPhotoUrlMap = new HashMap<>();
        setUserPhotoUrlMap(user2Uid);

        mGridLayout = (android.support.v7.widget.GridLayout) findViewById(R.id.gridLayout_attachments);


        mMessages = new ArrayList<>();
        tempMessages = new ArrayList<>();
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
        mAdapter = new UserMessageAdapter(mMessages, mRecyclerView, userPhotoUrlMap);

        // set the adapter object to the Recyclerview

        //loadMessages();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnChildAttachStateChangeListener(this);



        if (!mMessages.isEmpty()) {
            mRecyclerView.scrollToPosition(0);
        }
///////////////////////////////setup databaseref///////////////////////////////
        senderMessageRef = FirebaseDatabase.getInstance().getReference().child("UserMessages").child(currentUser.getUid()).child(user2Uid);
        receiverMessageRef = FirebaseDatabase.getInstance().getReference().child("UserMessages").child(user2Uid).child(currentUser.getUid());
        isTypingRef = receiverMessageRef.child("istyping");
        inChatRef = senderMessageRef.child("inchat");
        muteRef = senderMessageRef.child("mute");

        subTitleRef = FirebaseDatabase.getInstance().getReference().child("Users").child(user2Uid);
        titleRef = FirebaseDatabase.getInstance().getReference().child("Users").
                child(user2Uid).child("displayname");
         user2InchatRef = receiverMessageRef.child("inchat");

////////////////////////////////////////////////Custom Methods/////////////////////////////////////

        if(name!=null && user2Uid!=null) {
            getSupportActionBar().setTitle(name);
            setTitle();
            setSubTitle();
            loadMessages();
            setIstypingListener();


        }else{
            finish();
            Toast.makeText(this, "Unable to connect to the server", Toast.LENGTH_SHORT).show();
        }


//////////////////////////////////////////////////Listeners///////////////////////////////////////
        mAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                loadMoreHistory();

            }
        });

        toast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRecyclerView.scrollToPosition(0);
                toast.setVisibility(View.GONE);
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
                if (charSequence.toString().trim().isEmpty()) {
                    send.setEnabled(false);
                    updateIsTyping(false);
                } else {
                    send.setEnabled(true);
                    updateIsTyping(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {


            }
        });
        voice.setOnClickListener(this);

        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {
                    updateIsTyping(false);

                } else {
                    hideAttachment();
                    if(!editText.getText().toString().trim().isEmpty()){
                        updateIsTyping(true);
                    }
                }

            }
        });


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkInternet()){
                    sendMessage();
                }else{
                    Snackbar snackbar = Snackbar
                            .make(mRecyclerView, "No Internet Connection", Snackbar.LENGTH_LONG)
                            .setAction("Setting", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    startActivity(new Intent(Settings.ACTION_SETTINGS));
                                }
                            });

                    snackbar.show();
                }



            }
        });

        //attachment clicked event
        attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.clearFocus();
                ;
                if (mGridLayout.getVisibility() == View.GONE) {
                    mGridLayout.setVisibility(View.VISIBLE);
                    attachment.setSelected(true);
                    hideKeyboard();
                } else {
                    hideAttachment();
                    hideKeyboard();
                }

            }
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy < 0) {
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
        mOptionsMenu = menu;
        setMuteListener();
        setInChatListener();
        inChatItem = menu.findItem(R.id.inChat_indicator);
        inChatItem.setVisible(false);
        //make inchat Indicator unclickable
        inChatItem.setEnabled(false);



        // return true so that the menu pop up is opened
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.notification_switch:
                if (mute) {
                    item.setIcon(R.drawable.icon_notificastion_on_light);
                    setMute(false);
                    Toast.makeText(this, "notification on", Toast.LENGTH_SHORT).show();
                    mute = false;
                } else {
                    item.setIcon(R.drawable.icon_notification_off_light);
                    Toast.makeText(this, "notification off", Toast.LENGTH_SHORT).show();
                    setMute(true);
                    mute = true;
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        editText.clearFocus();
        hideKeyboard();
        finish();
        overridePendingTransition(0, R.anim.slide_to_right);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mGridLayout.getVisibility() == View.VISIBLE) {
            hideAttachment();
        } else {
            editText.clearFocus();
            hideKeyboard();
            finish();
            overridePendingTransition(0, R.anim.slide_to_right);
        }

    }

    private void hideKeyboard() {
        //hide keyboard
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void hideAttachment() {
        if (mGridLayout.getVisibility() == View.VISIBLE) {
            mGridLayout.setVisibility(View.GONE);
            attachment.setSelected(false);
        }
    }




    private void updateIsTyping(boolean istyping) {
        senderMessageRef.child("istyping").setValue(istyping);
    }


    private void inChat(boolean inChat) {
        inChatRef.setValue(inChat);
    }

    private void setMute(boolean isMute) {
        muteRef.setValue(isMute);

    }

    private void setMuteListener() {

        muteRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MenuItem item = mOptionsMenu.findItem(R.id.notification_switch);
                    mute = dataSnapshot.getValue(Boolean.class);
                    if(mute==null){
                        setMute(false);
                    }

                    if (mute == null || !mute) {
                        mute =false;
                        item.setIcon(R.drawable.icon_notificastion_on_light);
                    } else {
                        item.setIcon(R.drawable.icon_notification_off_light);
                    }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void setUserPhotoUrlMap(final String uid) {
        if (uid != null && !uid.isEmpty()) {
            DatabaseReference urlRef = FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(uid)
                    .child("photourl");
            urlRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String url = dataSnapshot.getValue(String.class);
                    if (url != null) {
                        userPhotoUrlMap.put(uid, url);
                    }
                    mAdapter.notifyDataSetChanged();


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }

    private void setTitle(){

        titleRef.keepSynced(true);
        titleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                 name = dataSnapshot.getValue(String.class);
                if(name!=null){
                    getSupportActionBar().setTitle(name);
                }else{
                    getSupportActionBar().setTitle(getString(R.string.app_name));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setSubTitle(){
        subTitleRef.keepSynced(true);
        subTitleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("lastseen")){
                 lastSeen = dataSnapshot.child("lastseen").getValue(Long.class);
                    setTimeChsngeListener();
                    getSupportActionBar().setSubtitle(MyDateFormatter.lastSeenConverter(lastSeen));
                }else{
                     removeTimeChangeListener();

                    if (dataSnapshot.child("onlinestats").getValue(String.class) != null) {
                        String onlineStats = dataSnapshot.child("onlinestats").getValue(String.class);
                        if (onlineStats.equals("0")) {
                            getSupportActionBar().setSubtitle("Offline");
                        } else if (onlineStats.equals("2") ) {
                                getSupportActionBar().setSubtitle((Html.fromHtml("<font color=\"#fffff4\">" + "Online"+ "</font>")));
                        }else if (onlineStats.equals("1")) {
                            getSupportActionBar().setSubtitle((Html.fromHtml("<font color=\"#FF8C00\">" + "Busy"+ "</font>")));
                        } else {
                            getSupportActionBar().setSubtitle("");
                        }
                    }else{
                        getSupportActionBar().setSubtitle("");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setTimeChsngeListener() {
        tickReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
                    getSupportActionBar().setSubtitle(MyDateFormatter.lastSeenConverter(lastSeen));
                }
            }
        };

        registerReceiver(tickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK)); // register the broadcast receiver to receive TIME_TICK

    }

    private void removeTimeChangeListener(){
        try {
            if (tickReceiver != null) {
                unregisterReceiver(tickReceiver);
            }
        } catch (IllegalArgumentException e) {
            tickReceiver = null;
        }
    }

    private void setUnread(){
        final DatabaseReference checkUnreadRef = receiverMessageRef.child("unread");
        checkUnreadRef.keepSynced(true);
        checkUnreadRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer unRead = dataSnapshot.getValue(Integer.class);
                if(unRead==null){
                    checkUnreadRef.setValue(1);
                }else if( user2Inchat ==null||!user2Inchat) {
                    checkUnreadRef.setValue(++unRead);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
    private void clearUnread(){
        final DatabaseReference clearUnreadRef = senderMessageRef.child("unread");
        clearUnreadRef.setValue(0);

    }

    private void setInChatListener(){
        user2InchatRef.keepSynced(true);
        user2InchatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               user2Inchat = dataSnapshot.getValue(Boolean.class);
               if(user2Inchat == null){
                   user2InchatRef.setValue(false);
                   user2Inchat =false;
               }
               if(user2Inchat){
                   inChatItem.setVisible(true);


               }else{
                   inChatItem.setVisible(false);

               }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void sendMessage() {
        String text = editText.getText().toString();
        final UserMessage message = new UserMessage(text, currentUser.getUid(),
                VIEWTYPE_TEXT_PENDING, UUID.randomUUID().toString().replaceAll("-", ""),
                new Date().getTime());
        editText.setText("");
        int index =0 , prevIndex =0;
      if(mMessages.contains(isTypingMessage)){
          index = mMessages.indexOf(isTypingMessage)+1;
          prevIndex = index;
      }

        if (!mMessages.isEmpty()) {
            UserMessage prevMessage = mMessages.get(prevIndex);

            if (!VIEWTYPE_HEADER.equals(message.getMessagetype())&& !VIEWTYPE_HEADER.equals(prevMessage.getMessagetype())
                    && message.getTimestamp()!=null && prevMessage.getTimestamp()!=null
                    && !MyDateFormatter.isSameDay(new Date(message.getTimestamp()), new Date(prevMessage.getTimestamp()))) {
                UserMessage dateHeaderMessage = new UserMessage();
                dateHeaderMessage.setMessageid(VIEWTYPE_HEADER + UUID.randomUUID().toString());
                dateHeaderMessage.setTimestamp(message.getTimestamp());
                dateHeaderMessage.setMessagetype(VIEWTYPE_HEADER);
                mMessages.add(index, dateHeaderMessage);
                mMessages.add(index, message);
                mAdapter.notifyDataSetChanged();
            }else {
                mMessages.add(index, message);
                mAdapter.notifyItemInserted(index);
            }
        } else {
            UserMessage dateHeaderMessage = new UserMessage();
            dateHeaderMessage.setMessageid(VIEWTYPE_HEADER + UUID.randomUUID().toString());
            dateHeaderMessage.setTimestamp(message.getTimestamp());
            dateHeaderMessage.setMessagetype(VIEWTYPE_HEADER);
            mMessages.add(index, dateHeaderMessage);
            mMessages.add(index, message);
            mAdapter.notifyDataSetChanged();
        }
        mRecyclerView.scrollToPosition(0);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateMessagetoFirebase(message);
            }
        }, 666);


    }

    private void updateMessagetoFirebase(final UserMessage message) {
        message.setMessagetype(VIEWTYPE_TEXT);
        senderMessageRef.child("chathistory").
                child(message.getMessageid()).setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                receiverMessageRef.child("chathistory").
                        child(message.getMessageid()).setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (mMessages.contains(message)) {
                            int index = mMessages.indexOf(message);
                            message.setTimestamp(new Date().getTime());
                            mMessages.set(index, message);
                            mAdapter.notifyItemChanged(index);
                            setLastMessageNode(message);
                            setUnread();

                        }

                    }
                });
                DatabaseReference user2Mute = receiverMessageRef.child("mute");
                user2Mute.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Boolean user2Mute = dataSnapshot.getValue(Boolean.class);
                        if(user2Mute==null){
                            user2Mute =false;
                        }
                        if(!user2Inchat && !user2Mute) {
                            SendNotification.sendNotificationTo(currentUser.getDisplayName(), message.getText(), user2Uid);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        });
    }


    private void setLastMessageNode(UserMessage message){
        DatabaseReference lastMegNode = senderMessageRef.child("lastmessage");
        lastMegNode.setValue(message);
        DatabaseReference lastmessage2 = receiverMessageRef.child("lastmessage");
        lastmessage2.setValue(message);
    }

    private void newMessageReceived(UserMessage message) {


        int index =0 , prevIndex =0;
        if(mMessages.contains(isTypingMessage)){
            removeIsTypingMessage();
            index = mMessages.indexOf(isTypingMessage)+1;
            prevIndex = index;
        }

        if (!mMessages.isEmpty()) {
            UserMessage prevMessage = mMessages.get(prevIndex);

            if (!VIEWTYPE_HEADER.equals(message.getMessagetype())&& !VIEWTYPE_HEADER.equals(prevMessage.getMessagetype())
                    && message.getTimestamp()!=null && prevMessage.getTimestamp()!=null
                    && !MyDateFormatter.isSameDay(new Date(message.getTimestamp()), new Date(prevMessage.getTimestamp()))) {
                UserMessage dateHeaderMessage = new UserMessage();
                dateHeaderMessage.setMessageid(VIEWTYPE_HEADER + UUID.randomUUID().toString());
                dateHeaderMessage.setTimestamp(message.getTimestamp());
                dateHeaderMessage.setMessagetype(VIEWTYPE_HEADER);
                mMessages.add(index, dateHeaderMessage);
                mMessages.add(index, message);
                mAdapter.notifyItemRangeInserted(index, mMessages.size() - 1);
            }else {
                mMessages.add(index, message);
                mAdapter.notifyItemInserted(index);
            }
        } else {
            UserMessage dateHeaderMessage = new UserMessage();
            dateHeaderMessage.setMessageid(VIEWTYPE_HEADER + UUID.randomUUID().toString());
            dateHeaderMessage.setTimestamp(message.getTimestamp());
            dateHeaderMessage.setMessagetype(VIEWTYPE_HEADER);
            mMessages.add(index, dateHeaderMessage);
            mMessages.add(index, message);
            mAdapter.notifyDataSetChanged();
        }

        if(firstVisablePos>index && (lastVisablePos -firstVisablePos+toastThreshhold)<lastVisablePos){
            toast.setText("New Message \u2193");
            toast.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.ZoomIn).duration(300).playOn(toast);
        }else{
            YoYo.with(Techniques.ZoomOut).duration(300).playOn(toast);
            toast.setVisibility(View.GONE);
             mRecyclerView.scrollToPosition(0);
        }
    }


    private void setIstypingListener() {
        isTypingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //default value
                boolean isTyping;
                if (dataSnapshot.getValue(Boolean.class) != null) {
                    // Toast.makeText(UserChatActvity.this, "changed", Toast.LENGTH_SHORT).show();
                    isTyping = dataSnapshot.getValue(Boolean.class);
                    if (isTyping) {
                        addIsTypingMessage();
                    } else {
                        removeIsTypingMessage();
                    }
                } else {
                    removeIsTypingMessage();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addLoadMoreMessage() {
        if (!mMessages.isEmpty()) {
            mMessages.add(loadingMessage);
            mRecyclerView.post(new Runnable() {
                public void run() {
                    mAdapter.notifyItemInserted(mMessages.size() - 1);
                }
            });


        }

    }


    private void addIsTypingMessage() {

        if (!mMessages.contains(isTypingMessage)) {
            mMessages.add(0, isTypingMessage);
            mAdapter.notifyItemInserted(0);

        }
        if(firstVisablePos>0 && (lastVisablePos -firstVisablePos+1)<lastVisablePos){
            toast.setVisibility(View.VISIBLE);
            toast.setText(name + " is typing...");
            YoYo.with(Techniques.ZoomIn).duration(300).playOn(toast);
        }else{
             mRecyclerView.scrollToPosition(0);
        }

    }

    private void removeIsTypingMessage() {
        if (mMessages.contains(isTypingMessage)) {
            int index = mMessages.indexOf(isTypingMessage);
            mMessages.remove(index);
            mAdapter.notifyItemRemoved(index);
            if(toast.getVisibility()==View.VISIBLE){
                YoYo.with(Techniques.ZoomOut).duration(300).playOn(toast);
                toast.setVisibility(View.GONE);
            }
        }

    }

    // add headers for  messages list that as argument
    private void generateDateHeaders(ArrayList<UserMessage> messageArrayList) {

        for (int i = 0; i < messageArrayList.size(); i++) {
            UserMessage message1 = messageArrayList.get(i);
            Long msg1Timestamp = messageArrayList.get(i).getTimestamp();

            if (messageArrayList.size() > i + 1) {
                UserMessage message2 = messageArrayList.get(i + 1);
                Long msg2Timestamp = messageArrayList.get(i + 1).getTimestamp();
                if (!VIEWTYPE_HEADER.equals(message1.getMessagetype()) && !VIEWTYPE_HEADER
                        .equals(message2.getMessagetype())
                        && msg1Timestamp != null && msg2Timestamp != null
                        && !MyDateFormatter.isSameDay(new Date(msg1Timestamp), new Date(msg2Timestamp))) {
                    UserMessage dateHeaderMessage = new UserMessage();
                    dateHeaderMessage.setMessageid(VIEWTYPE_HEADER + UUID.randomUUID().toString());
                    dateHeaderMessage.setTimestamp(message1.getTimestamp());
                    dateHeaderMessage.setMessagetype(VIEWTYPE_HEADER);
                    messageArrayList.add(i + 1, dateHeaderMessage);
                }
            } else {

                if (!VIEWTYPE_HEADER.equals(message1.getMessagetype())) {
                    UserMessage dateHeaderMessage = new UserMessage();
                    dateHeaderMessage.setMessageid(VIEWTYPE_HEADER + UUID.randomUUID().toString());
                    dateHeaderMessage.setTimestamp(message1.getTimestamp());
                    dateHeaderMessage.setMessagetype(VIEWTYPE_HEADER);
                    messageArrayList.add(dateHeaderMessage);
                }

            }
        }
        if (mMessages.contains(loadingMessage)) {
            int index = mMessages.indexOf(loadingMessage);
            mMessages.remove(index);
            mAdapter.notifyItemRemoved(index);

        }
        mMessages.addAll(messageArrayList);
        mAdapter.notifyDataSetChanged();


    }


    // init messageHistory
    private void loadMessages() {
        tempMessages.clear();
        DatabaseReference loadMsgRef =
                senderMessageRef.child("chathistory");
        loadMsgRef.keepSynced(true);

        loadMsgRef.orderByChild("timestamp").limitToLast(MAX_COUNT).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                UserMessage message = dataSnapshot.getValue(UserMessage.class);
                if (message != null &&!loaded) {
                    tempMessages.add(message);
                }else{
                    Log.d("userMessage","new message "+message.getText());
                    if(!message.getSenderid().equals(currentUser.getUid())){
                        newMessageReceived(message);
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // Log.d("userMessage",dataSnapshot.getKey());
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
        //valueEvent Listener will always trigger after childEventListener
        loadMsgRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("userMessage", "done " + dataSnapshot.getKey());


                if (!tempMessages.isEmpty()) {
                    Collections.reverse(tempMessages);
                    Log.d("userMessage", "the last message" + tempMessages.get(tempMessages.size() - 1).getText());
                    lastTimeStamp = tempMessages.get(tempMessages.size() - 1).getTimestamp();
                    generateDateHeaders(tempMessages);
                }
                loaded = true;

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadMoreHistory() {
        // Toast.makeText(this, "loading", Toast.LENGTH_SHORT).show();
        tempMessages.clear();
        DatabaseReference loadMoreMsgRef =
                senderMessageRef.child("chathistory");
        if (lastTimeStamp != null) {
            loadMoreMsgRef.orderByChild("timestamp").endAt(lastTimeStamp).limitToLast(MAX_COUNT).
                    addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                                UserMessage message = childSnap.getValue(UserMessage.class);
                                Log.d("userMessage", message.getText().toString());
                                if (message != null
                                        && !mMessages.contains(message)) {
                                    tempMessages.add(message);
                                }
                            }
                            if (!tempMessages.isEmpty()) {
                                Collections.reverse(tempMessages);
                                lastTimeStamp = tempMessages.get(tempMessages.size() - 1).getTimestamp();
                                addLoadMoreMessage();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        generateDateHeaders(tempMessages);
                                        mAdapter.setLoaded();
                                    }
                                }, 666);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


        }


    }
    @Override
    public void onChildViewAttachedToWindow(View view) {
        handler .postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("userMessage","last visable "+ mLayoutManager.findLastVisibleItemPosition());
                lastVisablePos = mLayoutManager.findLastCompletelyVisibleItemPosition();

            }
        },100);

        handler .postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("userMessage","first visable "+ mLayoutManager.findFirstVisibleItemPosition());
                firstVisablePos = mLayoutManager.findFirstCompletelyVisibleItemPosition();
                if(firstVisablePos<=1){
                    YoYo.with(Techniques.ZoomOut).duration(300).playOn(toast);
                    toast.setVisibility(View.GONE);
                }
            }
        },100);

    }

    @Override
    public void onChildViewDetachedFromWindow(View view) {

    }

    @Override
    public boolean onLongClick(View view) {
        Toast.makeText(this, "this feature is coming soon", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onClick(View view) {
        Toast.makeText(this, "this feature is coming soon", Toast.LENGTH_SHORT).show();
       /* Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);*/
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            view.setAlpha(0.6f);
        } else {
            view.setAlpha(1f);
        }
        return false;
    }


    public boolean checkInternet() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            return true;
        } else {
            return false;


        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        inChat(true);
        clearUnread();


    }

    @Override
    protected void onPause() {
        super.onPause();
        updateIsTyping(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        inChat(false);


    }

    @Override
    protected void onDestroy() {
       removeTimeChangeListener();
        super.onDestroy();




    }



}
