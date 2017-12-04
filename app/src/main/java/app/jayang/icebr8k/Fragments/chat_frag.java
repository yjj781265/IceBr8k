package app.jayang.icebr8k.Fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
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
    int counter;









    public chat_frag() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("view","onCreate");
        currrentUser = FirebaseAuth.getInstance().getCurrentUser();


        mImageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                Glide.with(imageView).load(url).
                        apply(RequestOptions.circleCropTransform()).into(imageView);
            }
        };
        mChatDialogs =new ArrayList<>();
        dialogsListAdapter =new DialogsListAdapter(mImageLoader);
        dialogsListAdapter.setDatesFormatter(this);


}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.chat_frag, container, false);
        mDialogsList=  mView.findViewById(R.id.dialogsList);
        mDialogsList.setVisibility(View.INVISIBLE);
        mDialogsList.setAdapter(dialogsListAdapter,false);
        loading = mView.findViewById(R.id.chatFregLoading);
        loading.setVisibility(View.VISIBLE);

        getChatNodes();
        addChildListener();






        Log.d("view","createV");

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

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d("view","Visable");
        if(isVisibleToUser){



        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("view","Resume");



    }

    public void getChatNodes(){

        counter =0;
        mChatDialogs.clear();


        final DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Messages/"+currrentUser.getUid());
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            HashMap <String,User> map = new HashMap<>();
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                Log.d("herer","changed");
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    if (!childSnapshot.getKey().equals("inChatRoom")) {
                        String Uid = childSnapshot.getKey();
                        map.put(Uid,null);

                    }

                }
                Log.d("Map",map.toString());
                getUsersNode(map);
                mListener.onComplete();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void getUsersNode(final HashMap<String,User> map){
        final DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Users");
        final int size= map.size();

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
             for(final String key : map.keySet()){
                 mRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                       User user = dataSnapshot.getValue(User.class);
                       map.put(key,user);
                        counter++;
                        if(counter==size){
                            Log.d("map",map.toString());
                            getLastMessageNode(map);
                        }
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

    public void getLastMessageNode(final HashMap<String,User> map){
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Messages/" +
                currrentUser.getUid());



        for(final String key: map.keySet()){
            mRef.child(key).child("lastmessage").addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                  Message message = dataSnapshot.getValue(Message.class);
                  User user = map.get(key);
                      counter++;
                      if(message!=null) {
                          Log.d("herer","message");
                          getUnread(message, user, key);
                      }else{

                              loading.setText("No Conversations");
                              mDialogsList.setVisibility(View.INVISIBLE);

                      }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public void getUnread(final Message message, final User user, final String Uid){

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Messages/"+
                currrentUser.getUid()+"/"+Uid+"/unRead");
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Integer uncount = dataSnapshot.getValue(Integer.class);
                if(uncount!=null) {
                    String dialogId =currrentUser.getUid()+Uid;
                    Author author;
                    if(message.getId()=="0406") { //if lastmessage is send by the self user;
                        author = new Author("0406",currrentUser.getDisplayName(),
                                currrentUser.getPhotoUrl().toString() );
                    }else{
                        author =new Author("0401",user.getDisplayname(),
                                user.getPhotourl());
                    }
                    ArrayList<Author> authors= new ArrayList<>();
                    authors.add(author);
                    ChatDialog dialog = new ChatDialog(dialogId,
                            user.getPhotourl(),user.getDisplayname(),authors,message,uncount);
                    mChatDialogs.add(dialog);
                    Log.d("herer","unread");

                        Log.d("herer","unread");
                        String size = String.valueOf(mChatDialogs.size());
                        Collections.sort(mChatDialogs);
                        dialogsListAdapter.setItems(mChatDialogs);
                        loading.setVisibility(View.GONE);
                        mDialogsList.setVisibility(View.VISIBLE);


                    YoYo.with(Techniques.FadeIn).duration(200).playOn(mDialogsList);
                        Log.d("herer",size);


                    }

            }



            @Override
            public void onCancelled(DatabaseError databaseError) {

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






    @Override
    public void onPause() {
        super.onPause();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

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




    public void addChildListener(){
        DatabaseReference ref = FirebaseDatabase.getInstance().
                getReference("Messages/"+currrentUser.getUid());
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("child","child added");
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Integer count = 0;
                Message message = null;
                Author author = null;
                String url;
                String name;


                    Log.d("child", "child changed" + dataSnapshot.getKey());
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.getKey().equals("unRead")) {
                            count = snapshot.getValue(Integer.class);

                        } else if (snapshot.getKey().equals("lastmessage")) {
                            message = snapshot.getValue(Message.class);
                            url = snapshot.child("user").child("avatar").getValue(String.class);
                            name = snapshot.child("user").child("name").getValue(String.class);
                            author = new Author(message.getId(), name, url);

                        }

                    }

                    if (message != null && count != null && author != null) {
                        final String dialogId = currrentUser.getUid() + dataSnapshot.getKey();
                        final ArrayList<Author> authors = new ArrayList<>();
                        authors.add(author);
                        DatabaseReference mref = FirebaseDatabase.getInstance().
                                getReference("Users/" + dataSnapshot.getKey());
                        final Author finalAuthor = author;
                        final Message finalMessage = message;
                        final Integer finalCount = count;
                        mref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot != null) {
                                    User user = dataSnapshot.getValue(User.class);
                                    ChatDialog dialog = new ChatDialog(dialogId, user.getPhotourl(), finalAuthor.getName(),
                                            authors, finalMessage, finalCount);


                                    if (!dialogsListAdapter.updateDialogWithMessage(dialogId, finalMessage)) {
                                        dialogsListAdapter.addItem(dialog);
                                        loading.setVisibility(View.GONE);
                                        mDialogsList.setVisibility(View.VISIBLE);


                                        YoYo.with(Techniques.FadeIn).duration(200).playOn(mDialogsList);


                                    } else {
                                       dialogsListAdapter.updateItemById(dialog);
                                        loading.setVisibility(View.GONE);
                                        mDialogsList.setVisibility(View.VISIBLE);


                                        YoYo.with(Techniques.FadeIn).duration(200).playOn(mDialogsList);

                                    }
                                    dialogsListAdapter.notifyDataSetChanged();

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    }
                }


            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d("child","child removed");
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d("child","child moved");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("child","child cancel");
            }
        });


    }

}
