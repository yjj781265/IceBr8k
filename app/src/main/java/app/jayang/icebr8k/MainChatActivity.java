package app.jayang.icebr8k;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OSNotificationPayload;
import com.stfalcon.chatkit.commons.ImageLoader;

import com.stfalcon.chatkit.commons.models.IUser;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;
import com.stfalcon.chatkit.utils.DateFormatter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import app.jayang.icebr8k.Modle.Author;

import app.jayang.icebr8k.Modle.CustomIncomingMessageViewHolder;
import app.jayang.icebr8k.Modle.CustomOutcomingMessageViewHolder;
import app.jayang.icebr8k.Modle.Message;




public class MainChatActivity extends AppCompatActivity implements MessagesListAdapter.OnLoadMoreListener {

    // global variables
    private final String senderId = "0406";
    private final String recieverId = "0401";
    private final String bullet = "\u2022";
    private final static int MAX_VOLUME = 100;


    private MessagesList messagesList;
    private MessageInput messageInput;
    private MessagesListAdapter<Message> adapter;
    private Toolbar chatToolBar;
    private Message oldMessage;
    private  DatabaseReference settypingRef;
    private FirebaseUser currentUser;
    private FirebaseDatabase mDatabase;
    private  DatabaseReference uploadMessageRef;
    private ArrayList<Message> templist = new ArrayList();
    private String user2Id,user2Name ;
    private ArrayList<Message> mMessages;
    private ImageView inChatIndicator;
    DateFormatter.Formatter formatter,formatter2;
    ImageLoader imageLoader;
    final int COUNT_LIMT = 20;
    final int CHAT_MAX = 100000;
    float volume;
   private Boolean inChat,init,user2Inchat;
   private MediaPlayer mp ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //connect to the view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_chat);

        mMessages =new ArrayList<>();
        mp = MediaPlayer.create(this, R.raw.typing);
        inChat=false;

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();

        messagesList = findViewById(R.id.messagesList);
        messagesList.setHasFixedSize(true);
        messageInput = findViewById(R.id.input_message);
        chatToolBar = findViewById(R.id.chat_toolbar);
        inChatIndicator = findViewById(R.id.inChat_indicator);
        setSupportActionBar(chatToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        user2Id= getIntent().getExtras().getString("user2Id");
        user2Name = getIntent().getExtras().getString("user2Name");

        if(user2Id!=null) {
            loadMessage();
            setInchatAndUnread();
            setSubTitle();
            setSeenIndicator();
            setTypingIndicator();
            user2Inchat=false;
        }
        if(user2Name!=null){
            getSupportActionBar().setTitle(user2Name);
            addTypingListener();
        }


        formatter = new DateFormatter.Formatter() {
            @Override
            public String format(Date date) {
                if (DateFormatter.isToday(date)) {
                    return "Today " + new SimpleDateFormat("h:mm a").format(date);
                } else if (DateFormatter.isYesterday(date)) {
                    return getString(R.string.date_header_yesterday) ;
                } else {
                    return new SimpleDateFormat("MMM d,yyyy").format(date);

                }
            }
        };

        formatter2 = new DateFormatter.Formatter() {
            @Override
            public String format(Date date) {
               return new SimpleDateFormat("h:mm a").format(date);

            }
        };

        imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                Glide.with(imageView).load(url).
                        apply(RequestOptions.circleCropTransform()).into(imageView);
            }
        };
        MessageHolders holdersConfig = new MessageHolders();
         holdersConfig.setOutcomingTextConfig(CustomOutcomingMessageViewHolder.class,R.layout.item_custom_outcoming_message);
         holdersConfig.setIncomingTextConfig(CustomIncomingMessageViewHolder.class,R.layout.item_custom_incoming_message);
         adapter = new MessagesListAdapter<>(senderId, holdersConfig, imageLoader);
         adapter.setDateHeadersFormatter(formatter);
         adapter.setLoadMoreListener(this);
         messagesList.setAdapter(adapter);


        messageInput.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(CharSequence input) {
                Date date  = new Date();//
                String messageId = createTransactionID();
                Author author = new Author(senderId,currentUser.getDisplayName(),
                        currentUser.getPhotoUrl().toString());
                final Message message = new Message();
                message.setCreatedAt(date);
                message.setId(messageId);
                message.setText(input.toString());
                message.setUser(author);
                message.setStatus("Sending...");
                if(checkInternet()) {
                    adapter.addToStart(message, true);
                    //update old message
                    if(oldMessage!=null) {
                        adapter.update(oldMessage);
                    }

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            pushMessagetoDatabase(message);
                        }
                    }, 500);
                }else{
                    Snackbar snackbar = Snackbar
                            .make(messageInput, "No Internet Connection", Snackbar.LENGTH_LONG)
                            .setAction("Setting", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    startActivity(new Intent(Settings.ACTION_SETTINGS));
                                }
                            });

                    snackbar.show();
                }

                return true;
            }
        });
        adapter.setOnMessageLongClickListener(new MessagesListAdapter.OnMessageLongClickListener<Message>() {
            @Override
            public void onMessageLongClick(Message message) {

            }
        });







    }


    @Override
    protected void onStart() {
        super.onStart();
        showLog("Start");
        DatabaseReference clearunReadRef = mDatabase.getReference().child("Messages").
                child(currentUser.getUid()).child(user2Id).child("unRead");
        clearunReadRef.setValue(0);
        mp = MediaPlayer.create(this, R.raw.typing);
        volume = (float) (1 - (Math.log(MAX_VOLUME - 30) / Math.log(MAX_VOLUME)));
        mp.setVolume(volume, volume);



    }

    @Override
    protected void onResume() {
        super.onResume();
       showLog("Resume");
       setInchatAndUnread();

    }

    @Override
    protected void onPause() {
        super.onPause();

     showLog("Pause");

    }

    @Override
    protected void onStop() {
        super.onStop();
        showLog("Stop");
        setoffChat();
        mp.release();
        mp=null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        showLog("Destory");



    }


    @Override
    public boolean onSupportNavigateUp() {

        Intent intent = new Intent(this, Homepage.class);
        intent.putExtra("mainchat", "1");
        startActivity(intent);
        finish();

        return true;
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(this, Homepage.class);
        intent.putExtra("mainchat", "1");
        startActivity(intent);
        finish();

    }


    public String createTransactionID() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0,6);
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


    private void pushMessagetoDatabase(final Message message){
         message.setStatus(null);
        uploadMessageRef = mDatabase.getReference().child("Messages").child(currentUser.getUid()).child(user2Id).
                child("chathistory").child(message.getId());
        uploadMessageRef.setValue(message, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError!=null){
                    showLog("error occured");
                    message.setStatus(getResources().getString(R.string.sendMessageError));
                    adapter.update(message);
                }else{

                    message.setStatus(null);

                    showLog("sent succssfull");

                    uploadMessageRef = mDatabase.getReference().child("Messages").
                            child(currentUser.getUid()).child(user2Id).child("lastmessage");
                    message.setStatus(null);
                    uploadMessageRef.setValue(message);

                   // for use2 message node///////////////
                    Message message2 = message;
                    //change to recieverid for user2database node
                    Author author = new Author(recieverId,currentUser.getDisplayName(),
                            currentUser.getPhotoUrl().toString());
                    message2.setUser(author);
                    message2.setStatus(null);
                    DatabaseReference uploadMessageRef2 = mDatabase.getReference().
                            child("Messages").child(user2Id).child(currentUser.getUid()).
                            child("chathistory").child(message2.getId());
                    uploadMessageRef2.setValue(message2);
                    uploadMessageRef2 =mDatabase.getReference().child("Messages").
                            child(user2Id).child(currentUser.getUid()).child("lastmessage");
                    uploadMessageRef2.setValue(message2);

                    message.getAuthor().setId(senderId);
                    if(!user2Inchat) {
                        message.setStatus("Sent " + bullet + " "
                                + formatter2.format(message.getCreatedAt()));
                    }else{
                        message.setStatus("Seen " + bullet + " "
                                + formatter2.format(message.getCreatedAt()));
                    }
                    adapter.update(message);
                    oldMessage =message;
                    setUnread(message);
                    sendNotification(message);

                }

            }


        });

    }

    private void setUnread(final Message message){
        final DatabaseReference checkUnreadRef = mDatabase.getReference().child("Messages").child(user2Id).
                child(currentUser.getUid()).child("unRead");
        checkUnreadRef.keepSynced(true);
        checkUnreadRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer unRead = dataSnapshot.getValue(Integer.class);
                if(unRead==null){
                    showLog("User2 doesnt have unRead node");
                    checkUnreadRef.setValue(1);
                    showLog("unread set  was null");
                }else {
                    checkUnreadRef.setValue(++unRead);




                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void showLog(String str){
        Log.d("MainChatActivity",str);
    }

    private void sendNotification(final Message message){
        DatabaseReference iuser2InchatRef = mDatabase.getReference().child("Messages").child(user2Id).
                child(currentUser.getUid()).child("inChat");
        iuser2InchatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user2Inchat = dataSnapshot.getValue(Boolean.class );
        if(user2Inchat==null||!user2Inchat){
            DatabaseReference notificationRef = mDatabase.getReference().child("Notification")
                    .child(user2Id);
            notificationRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String playerId= dataSnapshot.child("player_id").getValue(String.class);
                    SendNotification.sendNotificationTo(playerId,currentUser.getDisplayName()
                            ,message.getText(),currentUser.getUid());
                    showLog("notification sent");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });



        }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void setInchatAndUnread(){
        if(user2Id!=null) {
            DatabaseReference inchatUnreadRef = mDatabase.getReference().child("Messages").
                    child(currentUser.getUid()).child(user2Id);
            inChat=true;
            inchatUnreadRef.child("inChat").setValue(true);
            inchatUnreadRef.child("unRead").setValue(0);
        }
    }

    private void setoffChat(){
        if(user2Id!=null) {
            DatabaseReference inchatRef = mDatabase.getReference().child("Messages").
                    child(currentUser.getUid()).child(user2Id);
            inchatRef.child("inChat").setValue(false);

        }
    }




    @Override
    public void onLoadMore(int page, int totalItemsCount) {
        templist.clear();

       showLog("loadmore");
       if(totalItemsCount<=CHAT_MAX && !mMessages.isEmpty()){

            if(mMessages.size()>=COUNT_LIMT){

                Handler handler = new Handler() ;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        for(int i=0;i<5;i++){
                            templist.add(mMessages.get(i));
                        }
                        Collections.sort(templist);
                        mMessages.removeAll(templist);
                        adapter.addToEnd(templist,false);
                        showLog("Added new 5 item");
                    }
                });

            }else{
                Handler handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        adapter.addToEnd(mMessages,false);
                        showLog("Added new " + String.valueOf(mMessages.size())+"itmes");
                        mMessages.clear();
                    }
                });
            }
       }
       showLog(String.valueOf(totalItemsCount));

    }


    public void loadMessage(){
        DatabaseReference loadMessageRef = mDatabase.getReference().child("Messages").
                child(currentUser.getUid()).child(user2Id).child("chathistory");
       loadMessageRef.orderByChild("createdAt").
               addListenerForSingleValueEvent  (new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               for(DataSnapshot childSnap:dataSnapshot.getChildren()) {
                   Message tempMessage = childSnap.getValue(Message.class);
                   String photoUrl = childSnap.child("user").child("avatar").getValue(String.class);
                   Author author = new Author(tempMessage.getUser().getId(), tempMessage.getUser().getName(), photoUrl);
                   Message message = new Message(tempMessage.getId(), tempMessage.getText(), tempMessage.getCreatedAt(), author);
                   showLog(message.getId() + " " + message.getAuthor().getId() + " " + message.getUser().getAvatar()
                           + " " + message.getAuthor().getName() + " " + message.getText() + " " + message.getCreatedAt());
                   mMessages.add(message);
               }





               showLog(String.valueOf(mMessages.size()));
               showLog(String.valueOf(dataSnapshot.getChildrenCount()));
               Collections.sort(mMessages);
               //get lastmessage;
               if(!mMessages.isEmpty()){
                   oldMessage = mMessages.get(0);
                   showLog("oldmessage"+oldMessage.getId());
               }
               addValueChangeListener();
               ArrayList<Message> tempAddList = new ArrayList<>();
               if(mMessages.size()>COUNT_LIMT) {
                   for (int i = 0; i < COUNT_LIMT; i++) {
                       tempAddList.add(mMessages.get(i));
                   }
                   adapter.addToEnd(tempAddList, false);
                   mMessages.removeAll(tempAddList);
               }else{
                   adapter.addToEnd(mMessages, false);
                   mMessages.clear();
               }
               showLog(String.valueOf(tempAddList.size()));



           }

           @Override
           public void onCancelled(DatabaseError databaseError) {

           }
       });

    }
    private void addValueChangeListener() {
        if (user2Id != null) {

            DatabaseReference newMessageAddedRef = mDatabase.getReference().child("Messages").
                    child(currentUser.getUid()).child(user2Id).child("lastmessage");

           newMessageAddedRef.addValueEventListener(new ValueEventListener() {
               @Override
               public void onDataChange(DataSnapshot dataSnapshot) {
                   if (dataSnapshot.getValue() != null) {
                       Message message = dataSnapshot.getValue(Message.class);
                       String photoUrl = dataSnapshot.child("user").child("avatar").getValue(String.class);
                       Author author = new Author(message.getAuthor().getId(), message.getUser().
                               getName(), photoUrl);
                       message.setUser(author);
                       showLog(message.getId() + " " + message.getAuthor().getId() + " " + message.getUser().getAvatar()
                               + " " + message.getAuthor().getName() + " " + message.getText() + " " + message.getCreatedAt());

                       //new message
                       showLog(message.getId());
                       if(message.getAuthor().getId().equals(recieverId) &&
                               !message.getId().equals(oldMessage.getId())) {
                           adapter.addToStart(message, true);
                           showLog("new Chat"+message.getId());
                       }



                       }

                   }



               @Override
               public void onCancelled(DatabaseError databaseError) {

               }
           });

        }
    }

    private void setSubTitle(){
        DatabaseReference titleRef = mDatabase.getReference().child("Users").child(user2Id).child("onlineStats");
        titleRef.keepSynced(true);
        titleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(String.class) != null) {
                    if (dataSnapshot.getValue(String.class).equals("0")) {

                        getSupportActionBar().setSubtitle("Offline");
                    } else if (dataSnapshot.getValue(String.class).equals("2")) {
                        getSupportActionBar().setSubtitle((Html.fromHtml("<font color=\"#78FF44\">" + "Online"+ "</font>")));
                    } else if (dataSnapshot.getValue(String.class).equals("1")) {
                        getSupportActionBar().setSubtitle((Html.fromHtml("<font color=\"#FF8C00\">" + "Busy"+ "</font>")));
                    } else {
                        getSupportActionBar().setSubtitle(" ");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setSeenIndicator(){
        final DatabaseReference seenRef = mDatabase.getReference().child("Messages").child(user2Id).
                child(currentUser.getUid());
       seenRef.keepSynced(true);
        seenRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("inChat")) {
                    user2Inchat = dataSnapshot. child("inChat").getValue(Boolean.class);
                    if (user2Inchat) {
                        inChatIndicator.setVisibility(View.VISIBLE);
                        if (oldMessage != null) {
                                oldMessage.setStatus("Seen " + bullet + " " +
                                        formatter2.format(new Date()));
                                 adapter.update(oldMessage);
                        }
                    }else{
                        inChatIndicator.setVisibility(View.INVISIBLE);
                    }
                }else{
                    inChatIndicator.setVisibility(View.INVISIBLE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setTypingIndicator(){
        messageInput.getInputEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //showLog("before");
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
               showLog("user is typing");
               showLog(charSequence.toString());
              settypingRef = mDatabase.getReference().child("Messages").
                       child(currentUser.getUid()).child(user2Id).child("isTyping");
               if(!charSequence.toString().equals("") ||!charSequence.toString().isEmpty()){
                   settypingRef.setValue(true);
                   Handler handler = new Handler();
                   handler.postDelayed(new Runnable() {
                       @Override
                       public void run() {
                           settypingRef.setValue(false);
                       }
                   },2000); // if user hasn't send text for 3s the typing is off
               }


               }




            @Override
            public void afterTextChanged(Editable editable) {
               // showLog("after");
            }
        });
    }


 private void addTypingListener(){
     DatabaseReference gettypingRef = mDatabase.getReference().child("Messages").
             child(user2Id).child(currentUser.getUid()).child("isTyping");
     gettypingRef.keepSynced(true);

     gettypingRef.addValueEventListener(new ValueEventListener() {
         @Override
         public void onDataChange(DataSnapshot dataSnapshot) {
            Boolean isTyping=dataSnapshot.getValue(boolean.class);
            if(isTyping==null){
                isTyping=false;
            }
             if(isTyping &&inChat){
                 getSupportActionBar().setTitle("Typing...");

                 //play sound


             }else{
                 getSupportActionBar().setTitle(user2Name);

             }
         }

         @Override
         public void onCancelled(DatabaseError databaseError) {

         }
     });

 }


}
