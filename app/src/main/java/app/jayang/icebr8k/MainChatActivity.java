package app.jayang.icebr8k;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
    private DatabaseReference mRef;
    private FirebaseUser currentUser;
    private User user2;
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
        messages = new ArrayList<>();

        user2=(User)getIntent().getSerializableExtra("user2");
        user2Id = getIntent().getStringExtra("user2Id");
        Log.d("user2",user2.getUsername());




        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mRef = FirebaseDatabase.getInstance().getReference();





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
                adapter.addToStart(message2, true);


              /*  if (user2 != null) {
                    mRef.child("Messages").child(currentUser.getUid()).child(user2Id).push().setValue(message);
                    mRef.child("Messages").child(currentUser.getUid()).child(user2Id).child("lastmessage").setValue(input.toString());
                    //reciever
                    mRef.child("Messages").child(user2Id).child(currentUser.getUid()).push().setValue(message2);
                    mRef.child("Messages").child(user2Id).child(currentUser.getUid()).child("lastmessage").setValue(input.toString());
                }*/
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
                imageLoader = new ImageLoader() {
                    @Override
                    public void loadImage(ImageView imageView, String url) {
                        Glide.with(getApplicationContext()).load(url).
                                apply(RequestOptions.circleCropTransform()).into(imageView);
                    }
                };
                adapter = new MessagesListAdapter<>(senderId, imageLoader);
                messagesList.setAdapter(adapter);


                messages.clear();
              for(DataSnapshot childSnapshot :dataSnapshot.getChildren()){
                  if(!childSnapshot.getKey().equals("lastmessage")){
                    Message message = childSnapshot.getValue(Message.class);
                      messages.add(message);

                  }
              }


               adapter.addToEnd(messages,true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
