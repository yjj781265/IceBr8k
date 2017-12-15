package app.jayang.icebr8k.Fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.health.UidHealthStats;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.internal.MDAdapter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.auth.api.Auth;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;
import com.stfalcon.chatkit.utils.DateFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import app.jayang.icebr8k.Homepage;
import app.jayang.icebr8k.MainChatActivity;
import app.jayang.icebr8k.Modle.Author;
import app.jayang.icebr8k.Modle.ChatDialog;
import app.jayang.icebr8k.Modle.Message;
import app.jayang.icebr8k.Modle.User;
import app.jayang.icebr8k.R;

/**
 * Created by yjj781265 on 11/9/2017.
 */

public class chat_frag extends Fragment implements DateFormatter.Formatter {
    View mView;
    DialogsListAdapter dialogsListAdapter;
    DialogsList mDialogsList;
    FirebaseUser currrentUser;
    ImageLoader mImageLoader;
    ArrayList<ChatDialog> mChatDialogs;
    TextView loading;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;
    HashMap<String,String> map ;
    HashMap<String,Integer> map2;
;
    User user2;
    ArrayList<Author> authors;
    String TAG = "chatfrag";

    public chat_frag() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("view","onCreate");
        currrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        map = new HashMap<>();
        map2 = new HashMap<>();

        mImageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                Glide.with(imageView).load(url).
                        apply(RequestOptions.circleCropTransform()).into(imageView);
            }
        };
        authors = new ArrayList<>();
        mChatDialogs =new ArrayList<>();
        dialogsListAdapter =new DialogsListAdapter(mImageLoader);
        dialogsListAdapter.setDatesFormatter(this);


}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.chat_frag, container, false);
        mDialogsList=  mView.findViewById(R.id.dialogsList);
        mDialogsList.setAdapter(dialogsListAdapter,false);
        loading = mView.findViewById(R.id.chatFregLoading);
        loading.setVisibility(View.VISIBLE);
        mDialogsList.setVisibility(View.VISIBLE);

        loadDialogs();

        return  mView;
    }




    @Override
    public void onStart() {
        super.onStart();
        Log.d("view","Start");
        OneSignal.clearOneSignalNotifications();




        dialogsListAdapter.setOnDialogClickListener(new DialogsListAdapter.OnDialogClickListener() {
            @Override
            public void onDialogClick(IDialog dialog) {
                // clear notifications in the notifications bar
                OneSignal.clearOneSignalNotifications();

               String id ;
               if(dialog.getId().equals(currrentUser.getUid()+currrentUser.getUid())){
                   id =currrentUser.getUid();
               }else{
                   id = dialog.getId().replace(currrentUser.getUid(),"");
               }

                DatabaseReference mref = FirebaseDatabase.getInstance().getReference("Users/"+id);
                final String finalId = id;
                mref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        Log.d("Userid",user.getDisplayname());
                        Intent i = new Intent(getContext(), MainChatActivity.class);
                i.putExtra("user2Id", finalId);
                i.putExtra("user2",user);
                startActivity(i);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        });



    }






    public static interface OnCompleteListener {
        public abstract void onComplete();
    }

    private OnCompleteListener mListener;

    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.mListener = (OnCompleteListener)context;
        }
        catch ( ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnCompleteListener");
        }
    }








    // format dialog date
    @Override
    public String format(Date date) {
        if (DateFormatter.isToday(date)) {
            return  new SimpleDateFormat("hh:mm a").format(date);
        } else if (DateFormatter.isYesterday(date)) {
            return getString(R.string.date_header_yesterday);
        } else if(DateFormatter.isCurrentYear(date)){
            return DateFormatter.format(date, DateFormatter.Template.STRING_DAY_MONTH);
        }else{
            return DateFormatter.format(date, DateFormatter.Template.STRING_DAY_MONTH_YEAR);
        }
    }


    public void loadDialogs(){

        //init the database reference
        mRef = mDatabase.getReference("Messages/"+currrentUser.getUid());
        mRef.keepSynced(true);


        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
           Integer totalCount =0;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG,"LOADING DATA");
                for(DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    if (childSnap.hasChild("lastmessage")) {
                        totalCount++;
                    }


                }

                    Log.d(TAG,"added counter");
                    childAddedListener(totalCount);

                if(totalCount==0){
                    loading.setText("No Conversations");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public void updateDialog(final Message message,final Integer unRead,final String user2Uid,
                             final ArrayList<Author> authors,final Integer counter,
                             final Integer totalcount){
        mRef=mDatabase.getReference("Users/"+user2Uid);
        mRef.keepSynced(true);
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        user2 = dataSnapshot.getValue(User.class);
                      String dialogUrl =user2.getPhotourl();
                      String dialogId = currrentUser.getUid()+user2Uid;
                        Log.d(TAG,"dialogid "+dialogId );
                        ChatDialog chatDialog = new ChatDialog(dialogId,dialogUrl,
                                user2.getDisplayname(),authors,message,unRead);
                        mChatDialogs.add(chatDialog);
                        //end of the for loop
                        if(counter==totalcount && counter!=null){
                            Log.d(TAG,"TOTAL "+totalcount + "We are at item "+counter+
                                    "dialog count is " + mChatDialogs.size());
                            Collections.sort(mChatDialogs);
                            dialogsListAdapter.addItems(mChatDialogs);
                            loading.setVisibility(View.GONE);
                        }else if(counter==null){
                            if(dialogsListAdapter.updateDialogWithMessage(dialogId,message)){
                                Log.d(TAG,"update");
                                dialogsListAdapter.updateItemById(chatDialog);
                            }else{
                                Log.d(TAG,"added");
                                dialogsListAdapter.addItem(chatDialog);
                            }
                            loading.setVisibility(View.GONE);

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


    }



    public void childAddedListener(final Integer total){
        mDatabase.getReference("Messages/"+currrentUser.getUid()).keepSynced(true);
        mDatabase.getReference("Messages/"+currrentUser.getUid()).addChildEventListener
                (new ChildEventListener() {
                    Integer counter =0;
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "added " + dataSnapshot.toString());
                if(dataSnapshot.hasChild("lastmessage")) {
                    counter++;

                    String user2Uid = dataSnapshot.getKey();
                    Message  message = dataSnapshot.child("lastmessage").getValue(Message.class);
                    Integer unRead =dataSnapshot.child("unRead").getValue(Integer.class);
                    Log.d(TAG,message.getId() + " " + message.getCreatedAt()+" " +
                            message.getText());
                    Author author = new Author(message.getUser().getId(),
                            message.getUser().getName(),dataSnapshot.child("lastmessage")
                            .child("user").child("avatar").getValue(String.class));
                    Log.d(TAG,"photourl is " + author.getAvatar());
                    message.setUser(author);
                    authors.clear();
                    authors.add(author);
                    if(unRead==null){
                        unRead=0;
                    }
                    map.put(user2Uid,message.getCreatedAt().toString());
                    map2.put(user2Uid,unRead);

                    updateDialog(message,unRead,user2Uid,authors,counter,total);


                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                HashMap<String,String> tempMap = new HashMap<>();
                HashMap<String,Integer> tempMap2 = new HashMap<>();

                Date tempDate;
                if(dataSnapshot.hasChild("lastmessage") ) {
                    tempDate = dataSnapshot.child("lastmessage").getValue(Message.class).
                            getCreatedAt();
                  tempMap.put(dataSnapshot.getKey(),tempDate.toString());
                  if(map.get(dataSnapshot.getKey())!=null && !map.get(dataSnapshot.getKey()).
                                  equals(tempMap.get(dataSnapshot.getKey()))){

                      Log.d(TAG,"DATE IS DIFFERENT");
                      String user2Uid = dataSnapshot.getKey();
                      Message  message = dataSnapshot.child("lastmessage").getValue(Message.class);
                      Integer unRead =dataSnapshot.child("unRead").getValue(Integer.class);
                      Log.d(TAG,message.getId() + " " + message.getCreatedAt()+" " +
                              message.getText());
                      Author author = new Author(message.getUser().getId(),
                              message.getUser().getName(),dataSnapshot.child("lastmessage")
                              .child("user").child("avatar").getValue(String.class));
                      Log.d(TAG,"photourl is " + author.getAvatar());
                      message.setUser(author);
                      authors.clear();
                      authors.add(author);
                      if(unRead==null){
                          unRead=0;
                      }
                      updateDialog(message,unRead,user2Uid,authors,null,null);
                      map.put(dataSnapshot.getKey(),tempDate.toString());

                      }
                      else if (map.get(dataSnapshot.getKey())==null) {
                      Log.d(TAG,"DATE IS null");
                      String user2Uid = dataSnapshot.getKey();
                      Message  message = dataSnapshot.child("lastmessage").getValue(Message.class);
                      Integer unRead =dataSnapshot.child("unRead").getValue(Integer.class);
                      Log.d(TAG,message.getId() + " " + message.getCreatedAt()+" " +
                              message.getText());
                      Author author = new Author(message.getUser().getId(),
                              message.getUser().getName(),dataSnapshot.child("lastmessage")
                              .child("user").child("avatar").getValue(String.class));
                      Log.d(TAG,"photourl is " + author.getAvatar());
                      message.setUser(author);
                      authors.clear();
                      authors.add(author);
                      if(unRead==null){
                          unRead=0;
                      }
                      updateDialog(message,unRead,user2Uid,authors,null,null);
                      map.put(dataSnapshot.getKey(),tempDate.toString());
                  }else{
                      Log.d(TAG,"DATE IS the same");
                      map.put(dataSnapshot.getKey(),tempDate.toString());

                  }


                }
                    if(dataSnapshot.hasChild("unRead") && dataSnapshot.hasChild("lastmessage")) {
                        Log.d(TAG, dataSnapshot.child("unRead").getValue(Integer.class).toString());
                        Integer unRead2 = dataSnapshot.child("unRead").getValue(Integer.class);
                        tempMap2.put(dataSnapshot.getKey(),unRead2);

                        if(map2.get(dataSnapshot.getKey())!=null && map2.get(dataSnapshot.getKey())
                               !=tempMap2.get(dataSnapshot.getKey())){

                            Log.d(TAG,"unread IS DIFFERENT");
                            String user2Uid = dataSnapshot.getKey();
                            Message  message = dataSnapshot.child("lastmessage").getValue(Message.class);
                            Integer unRead =dataSnapshot.child("unRead").getValue(Integer.class);
                            Log.d(TAG,message.getId() + " " + message.getCreatedAt()+" " +
                                    message.getText());

                            Author author = new Author(message.getUser().getId(),
                                    message.getUser().getName(),dataSnapshot.child("lastmessage")
                                    .child("user").child("avatar").getValue(String.class));
                            Log.d(TAG,"photourl is " + author.getAvatar());
                            message.setUser(author);
                            authors.clear();
                            authors.add(author);
                            if(unRead==null){
                                unRead=0;
                            }
                            updateDialog(message,unRead,user2Uid,authors,null,null);
                            map2.put(dataSnapshot.getKey(),unRead);

                        }
                        else if (map2.get(dataSnapshot.getKey())==null) {
                            Log.d(TAG,"unread IS null");
                            String user2Uid = dataSnapshot.getKey();
                            Message  message = dataSnapshot.child("lastmessage").getValue(Message.class);
                            Integer unRead =dataSnapshot.child("unRead").getValue(Integer.class);
                            Log.d(TAG,message.getId() + " " + message.getCreatedAt()+" " +
                                    message.getText());
                            Author author = new Author(message.getUser().getId(),
                                    message.getUser().getName(),dataSnapshot.child("lastmessage")
                                    .child("user").child("avatar").getValue(String.class));
                            Log.d(TAG,"photourl is " + author.getAvatar());
                            message.setUser(author);
                            authors.clear();
                            authors.add(author);
                            if(unRead==null){
                                unRead=0;
                            }
                            updateDialog(message,unRead,user2Uid,authors,null,null);
                            map2.put(dataSnapshot.getKey(),unRead);
                        }else{
                            Log.d(TAG,"unread IS the same");
                            map2.put(dataSnapshot.getKey(),unRead2);

                        }

                    }


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
    }

    }













