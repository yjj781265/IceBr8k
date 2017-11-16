package app.jayang.icebr8k;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;
import com.stfalcon.chatkit.utils.DateFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import app.jayang.icebr8k.Fragments.chat_frag;
import app.jayang.icebr8k.Modle.Author;
import app.jayang.icebr8k.Modle.CustomMessageViewHolder;
import app.jayang.icebr8k.Modle.CustomOutcomingMessageViewHolder;
import app.jayang.icebr8k.Modle.Message;
import app.jayang.icebr8k.Modle.User;


public class MainChatActivity extends AppCompatActivity   {

    // global variables
    private  final String senderId ="0406";
    private  final String recieverId ="0401";
    private MessagesList messagesList;
    private MessageInput messageInput;
    private MessagesListAdapter<Message> adapter;
    private Toolbar chatToolBar;
    private DatabaseReference mRef;
    private FirebaseUser currentUser;
    private User user2;
    private String user2Id;
    Date lastLoadedDate;
    private ArrayList<Message> messages,temp;
    Boolean inChat = true;
    DateFormatter.Formatter formatter;
    MessagesListAdapter.HoldersConfig holdersConfig;
    ImageLoader imageLoader;
   final  int COUNT_LIMT =8;
        final   int CHAT_MAX =100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //connect to the view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_chat);
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
        messagesList = findViewById(R.id.messagesList);
        messageInput = findViewById(R.id.input_message);

        chatToolBar =findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(" ");
        messages = new ArrayList<>();
        temp =new ArrayList<>();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        holdersConfig = new MessagesListAdapter.HoldersConfig();
        holdersConfig.setOutcomingTextHolder(CustomOutcomingMessageViewHolder.class);
        holdersConfig.setIncomingTextHolder(CustomMessageViewHolder.class);
        imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                Glide.with(imageView).load(url).
                        apply(RequestOptions.circleCropTransform()).into(imageView);
            }
        };
        adapter = new MessagesListAdapter<>(senderId, holdersConfig, imageLoader);



        loadMessage();



        //imageloader for the chat bubble only work for incoming message

        //setup firebase database instance
        mRef = FirebaseDatabase.getInstance().getReference();

       formatter =new DateFormatter.Formatter() {
            @Override
            public String format(Date date) {
                if (DateFormatter.isToday(date)) {
                    return   "Today "+ new SimpleDateFormat("hh:mm a").format(date);
                } else if (DateFormatter.isYesterday(date)) {
                    return getString(R.string.date_header_yesterday) + " " +
                            new SimpleDateFormat("hh:mm a").format(date);


                } else  {
                    return DateFormatter.format(date, DateFormatter.Template.STRING_DAY_MONTH_YEAR)
                            +" "+
                            new SimpleDateFormat("hh:mm a").format(date);
                }
            }
        };

        // set inchat node for the chatroom
        DatabaseReference chatInfoRef = FirebaseDatabase.getInstance().getReference
                ("Messages/"+currentUser.getUid()+"/"+user2Id);
        chatInfoRef.child("inChat").setValue(inChat);
        chatInfoRef.child("unRead").setValue(0);
        //set node to true if user is in chat activity can be texting to other users beside user2
        Boolean inChatRoom = true;
        DatabaseReference inChatRef = FirebaseDatabase.getInstance().getReference("Messages/"+currentUser.getUid());
        inChatRef.child("inChatRoom").setValue(inChatRoom);









    }





    @Override
    protected void onStart() {
        super.onStart();

        messageInput.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(final CharSequence input) {

                // get current time
                Calendar calendar = Calendar.getInstance();
                calendar.get(Calendar.DAY_OF_MONTH);
                lastLoadedDate = calendar.getTime();
                Author author = new Author(senderId, currentUser.getDisplayName(), currentUser.getPhotoUrl().toString());
                Message message = new Message(senderId, input.toString(), lastLoadedDate, author);

                Author author2 = new Author(recieverId, currentUser.getDisplayName(), currentUser.getPhotoUrl().toString());
                Message message2 = new Message(recieverId, input.toString(), lastLoadedDate, author2);
                // create two identical messages one for the user1 node ,one for the user2 node
                if (user2 != null) {
                    mRef.child("Messages").child(currentUser.getUid()).child(user2Id).child("chathistory").push().setValue(message);
                    mRef.child("Messages").child(currentUser.getUid()).child(user2Id).child("lastmessage").setValue(message);
                    //reciever
                    mRef.child("Messages").child(user2Id).child(currentUser.getUid()).child("chathistory").push().setValue(message2);
                    mRef.child("Messages").child(user2Id).child(currentUser.getUid()).child("lastmessage").setValue(message2);
                    adapter.addToStart(message, true);


                    mRef.child("Messages").child(user2Id).child(currentUser.getUid()).child("inChat").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Boolean inChat;
                            if(dataSnapshot.getValue()!=null) {
                                inChat = dataSnapshot.getValue(Boolean.class);
                            }else{
                                inChat =false;
                            }

                            final Boolean finalInChat = inChat;
                            // if user2 is in chat the unread stay at 0, else user2 unread will plus one for that specific one to one chat room
                            mRef.child("Messages").child(user2Id).child(currentUser.getUid()).child("unRead").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Integer unRead = dataSnapshot.getValue(Integer.class);
                                    if  (finalInChat == false && unRead != null ) {
                                        unRead++;
                                        mRef.child("Messages").child(user2Id).child(currentUser.getUid()).child("unRead").setValue(unRead);
                                        //sending notification to that user;
                                        DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference("Notification/"+user2Id);
                                        notificationRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                String playerid = dataSnapshot.child("player_id").getValue(String.class);
                                                if(playerid!=null){
                                                    SendNotification.sendNotificationTo(playerid,
                                                           currentUser.getDisplayName(), input.toString(), currentUser.getUid());
                                                }

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    } else if( unRead == null ) { // user2 doenst have unread node yet , set it to 1 prevent nullpointer exception
                                        mRef.child("Messages").child(user2Id).child(currentUser.getUid()).child("unRead").setValue(1);
                                        mRef.child("Messages").child(user2Id).child(currentUser.getUid()).child("inChat").setValue(false);
                                        DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference("Notification/"+user2Id);
                                        notificationRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                String playerid = dataSnapshot.child("player_id").getValue(String.class);
                                                if(playerid!=null){
                                                    SendNotification.sendNotificationTo
                                                            (playerid,  currentUser.getDisplayName(), input.toString(), currentUser.getUid());
                                                }

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                                        // user2 is in chat live
                                    }else {
                                        mRef.child("Messages").child(user2Id).child(currentUser.getUid()).child("unRead").setValue(0);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                }

                return true;
            }


        });









    }

    @Override
    protected void onResume() {
        super.onResume();
        inChat =true;
        DatabaseReference chatInfoRef = FirebaseDatabase.getInstance().getReference
                ("Messages/"+currentUser.getUid()+"/"+user2Id);
        chatInfoRef.child("inChat").setValue(inChat);
        chatInfoRef.child("unRead").setValue(0);

        Boolean inChatRoom = true;
        DatabaseReference inChatRef = FirebaseDatabase.getInstance().getReference("Messages/"+currentUser.getUid());
        inChatRef.child("inChatRoom").setValue(inChatRoom);
    }

    @Override
    protected void onPause() {
        super.onPause();
        inChat =false;
        DatabaseReference chatInfoRef = FirebaseDatabase.getInstance().getReference
                ("Messages/"+currentUser.getUid()+"/"+user2Id);
        chatInfoRef.child("inChat").setValue(inChat);

        Boolean inChatRoom = false;
        DatabaseReference inChatRef = FirebaseDatabase.getInstance().getReference("Messages/"+currentUser.getUid());
        inChatRef.child("inChatRoom").setValue(inChatRoom);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        inChat =false;
        DatabaseReference chatInfoRef = FirebaseDatabase.getInstance().getReference
                ("Messages/"+currentUser.getUid()+"/"+user2Id);
        chatInfoRef.child("inChat").setValue(inChat);
        chatInfoRef.child("unRead").setValue(0);
        Boolean inChatRoom = false;
        DatabaseReference inChatRef = FirebaseDatabase.getInstance().getReference("Messages/"+currentUser.getUid());
        inChatRef.child("inChatRoom").setValue(inChatRoom);
    }


 // load history message in real time
    public void loadMessage(){

        Bundle extras = getIntent().getExtras();
        if (extras.get("user2Uid") != null) {
            user2Id = extras.getString("user2Uid");
            user2 = (User) extras.getSerializable("user2");
        }else{
            // get user2 the people you chat with info
            user2 =(User)getIntent().getExtras().getSerializable("user2");
            // get user2's unique id for database
            user2Id = getIntent().getStringExtra("user2Id");

        }
        if(user2!=null) {
            getSupportActionBar().setTitle(user2.getDisplayname());
            Log.d("user2", user2.getUsername());
        }

        final DatabaseReference mref = FirebaseDatabase.getInstance().getReference("Messages/"+currentUser.getUid()+"/"+user2Id+"/chathistory");
        mref.limitToLast(COUNT_LIMT).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                messages.clear();
                adapter.clear();
                temp.clear();
                for(DataSnapshot childSnapshot :dataSnapshot.getChildren()){
                  if(user2Id.equals(currentUser.getUid())){
                    Message message = childSnapshot.getValue(Message.class);
                    Author author = new Author(message.getId(),currentUser.getDisplayName(),currentUser.getPhotoUrl().toString());
                    message.setUser(author);
                    messages.add(message);

                  }else if(user2!=null){

                      Message message = childSnapshot.getValue(Message.class);
                      Author author = new Author(message.getId(),user2.getDisplayname(),user2.getPhotourl());
                      message.setUser(author);
                      messages.add(message);

                  }



              }
                Log.d("arraylist",String.valueOf(messages.size()));


                adapter.setDateHeadersFormatter(formatter);
                adapter.addToEnd(messages,true);
                messagesList.setAdapter(adapter);


                final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Messages/"+currentUser.getUid()+"/"+user2Id+"/chathistory");
                adapter.setLoadMoreListener(new MessagesListAdapter.OnLoadMoreListener() {
                    @Override
                    public void onLoadMore(int page, final int totalItemsCount) {
                        if(totalItemsCount<CHAT_MAX && totalItemsCount>=COUNT_LIMT){
                            Log.d("arraylist","triggered");
                            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    long total = dataSnapshot.getChildrenCount();
                                    if(total>totalItemsCount)
                                        ref.limitToFirst((int)( total-totalItemsCount)).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                                    Message message = snapshot.getValue(Message.class);
                                                    Author author = new Author(message.getId(),user2.getDisplayname(),user2.getPhotourl());
                                                    message.setUser(author);
                                                    temp.add(message);
                                                }
                                                adapter.addToEnd(temp,true);
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });


                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                });



            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }



    @Override
    public boolean onSupportNavigateUp() {

       Intent intent = new Intent(this,Homepage.class) ;
         intent.putExtra("mainchat","1");
         startActivity(intent);
         finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        return true;
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(this,Homepage.class) ;
        intent.putExtra("mainchat","1");
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }



}
