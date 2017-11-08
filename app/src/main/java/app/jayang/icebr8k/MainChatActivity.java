package app.jayang.icebr8k;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import app.jayang.icebr8k.Modle.Author;
import app.jayang.icebr8k.Modle.Message;
import app.jayang.icebr8k.Modle.User;


public class MainChatActivity extends AppCompatActivity implements MessagesListAdapter.OnLoadMoreListener {

    private  final String senderId ="0406";
    private  final String recieverId ="0401";
    private MessagesList messagesList;
    private MessageInput messageInput;
    private MessagesListAdapter<Message> adapter;
    private Toolbar chatToolBar;
    private DatabaseReference mRef;
    private FirebaseUser currentUser;
    private User user2,selfUser;
    private String user2Id;
    Date lastLoadedDate;
    private ArrayList<Message> messages;

    ImageLoader imageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_chat);
        messagesList = findViewById(R.id.messagesList);
        messageInput = findViewById(R.id.input_message);
        chatToolBar =findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(" ");
        messages = new ArrayList<>();

        user2=(User)getIntent().getSerializableExtra("user2");


        if(selfUser!=null){
            user2Id = currentUser.getUid();
            getSupportActionBar().setTitle(currentUser.getDisplayName());

        }else{
            user2Id = getIntent().getStringExtra("user2Id");
            getSupportActionBar().setTitle(user2.getDisplayname());
        }
        Log.d("user2",user2.getUsername());
        imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                Glide.with(imageView).load(url).
                        apply(RequestOptions.circleCropTransform()).into(imageView);
            }
        };



        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mRef = FirebaseDatabase.getInstance().getReference();
        adapter = new MessagesListAdapter<>(senderId, imageLoader);
        messagesList.setAdapter(adapter);






    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return  true;
    }


    @Override
    protected void onStart() {
        super.onStart();
        loadMessage();

        messageInput.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(CharSequence input) {

                Calendar calendar = Calendar.getInstance();
                calendar.get(Calendar.DAY_OF_MONTH);
                lastLoadedDate = calendar.getTime();
                Author author = new Author(senderId, currentUser.getDisplayName(), currentUser.getPhotoUrl().toString());
                Message message = new Message(senderId, input.toString(), lastLoadedDate, author);

                Author author2 = new Author(recieverId, currentUser.getDisplayName(), currentUser.getPhotoUrl().toString());
                Message message2 = new Message(recieverId, input.toString(), lastLoadedDate, author2);




             if (user2 != null) {
                    mRef.child("Messages").child(currentUser.getUid()).child(user2Id).push().setValue(message);
                    mRef.child("Messages").child(currentUser.getUid()).child(user2Id).child("lastmessage").setValue(input.toString());
                    //reciever
                    mRef.child("Messages").child(user2Id).child(currentUser.getUid()).push().setValue(message2);
                    mRef.child("Messages").child(user2Id).child(currentUser.getUid()).child("lastmessage").setValue(input.toString());
                    adapter.addToStart(message,true);


                }


                return true;

            }

        });







    }







    @Override
    public void onLoadMore(int page, int totalItemsCount) {
        Log.d("load","trigger");
        if(totalItemsCount <100) {

        }
    }


    public void loadMessage(){

        DatabaseReference mref = FirebaseDatabase.getInstance().getReference("Messages/"+currentUser.getUid()+"/"+user2Id);
        mref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                messages.clear();
                for(DataSnapshot childSnapshot :dataSnapshot.getChildren()){
                  if(!childSnapshot.getKey().equals("lastmessage") && user2Id.equals(currentUser.getUid())){
                    Message message = childSnapshot.getValue(Message.class);
                    Author author = new Author(message.getId(),currentUser.getDisplayName(),currentUser.getPhotoUrl().toString());
                    message.setUser(author);
                    messages.add(message);
                  }else if(!childSnapshot.getKey().equals("lastmessage") && user2!=null){

                      Message message = childSnapshot.getValue(Message.class);
                      Author author = new Author(message.getId(),user2.getDisplayname(),user2.getPhotourl());
                      message.setUser(author);
                      messages.add(message);
                  }
              }
                adapter = new MessagesListAdapter<>(senderId, imageLoader);
                messagesList.setAdapter(adapter);
                adapter.addToEnd(messages,true);




            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
