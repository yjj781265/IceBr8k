package app.jayang.icebr8k.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
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
import java.util.Comparator;

import app.jayang.icebr8k.Adapter.UserMessageDialogAdapter;
import app.jayang.icebr8k.Homepage;
import app.jayang.icebr8k.Model.UserMessage;
import app.jayang.icebr8k.Model.UserMessageDialog;
import app.jayang.icebr8k.R;

/**
 * Created by yjj781265 on 2/19/2018.
 */

public class UserMessageDialog_Frag extends Fragment {
    private View fragView;
    private Homepage mHomepage;
    private TextView noChat;
    private ArrayList<UserMessageDialog> mDialogs;
    private UserMessageDialogAdapter mAdapter;
    private FirebaseUser currentUser;
    private Comparator<UserMessageDialog> mComparator;
    private DatabaseReference userInfoRef,msgRef;
    private RelativeLayout loadingGif;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private final String Default_Url = "https://firebasestorage.googleapis.com/v0/b/icebr8k-98675.appspot.com/o/UserAvatars%2Fdefault_avatar.png?alt=media&token=ccbf30ce-5cfb-493a-8c28-8bf7ee18cc9a";
    private final String TAG ="UserMessageDialog_Frag";
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragView = inflater.inflate(R.layout.fragment_usermessagedialog, container, false);
        noChat = fragView.findViewById(R.id.messageDialog_noChat);
        loadingGif = fragView.findViewById(R.id.messageDialog_loading);

        mRecyclerView =fragView.findViewById(R.id.messageDialog_list);
        mComparator = new Comparator<UserMessageDialog>() {
            @Override
            public int compare(UserMessageDialog dialog, UserMessageDialog t1) {
                int result =0;
                if(dialog.getUnRead()!=null && t1.getUnRead()!=null){
                    result = t1.getUnRead().compareTo(dialog.getUnRead());
                }
                if(result == 0){
                    if(dialog.getLastMessage().getTimestamp()!=null &&t1.getLastMessage().getTimestamp()!=null) {
                        result = t1.getLastMessage().getTimestamp().compareTo(dialog.getLastMessage().getTimestamp());
                    }
                 }
                return result;
            }
        };


        mDialogs =new ArrayList<>();

        mLayoutManager = new LinearLayoutManager(fragView.getContext());
        mAdapter = new UserMessageDialogAdapter(mDialogs,getActivity());
        mAdapter.setHasStableIds(true);

        // use a linear layout manager
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);


        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userInfoRef = FirebaseDatabase.getInstance().getReference().child("Users");
        msgRef = FirebaseDatabase.getInstance().getReference().child("UserMessages").child(currentUser.getUid());
        mHomepage = (Homepage)getActivity();
        if(mHomepage!=null) {
            mHomepage.getViewPager().setSwipeable(true);
        }

        loadDialogs();
        return fragView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(!isVisibleToUser && getView()!=null && mAdapter!=null){
            mAdapter.dismissActionMode();
        }
    }



    public void showLog(String str){
        Log.d(TAG,str);
    }
    public void loadDialogs(){

        msgRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.hasChild("lastmessage") ){
                     boolean muted =false ,groupchat =false;
                     String name = "";
                     UserMessageDialog messageDialog = new UserMessageDialog();
                     UserMessage message = dataSnapshot.getValue(UserMessage.class);
                     if(dataSnapshot.hasChild("mute")){
                         muted = dataSnapshot.child("mute").getValue(boolean.class);
                     }
                     messageDialog.setId(dataSnapshot.getKey());
                     messageDialog.setLastMessage(message);
                     messageDialog.setMuted(muted);

                     if(dataSnapshot.hasChild("groupchat")){
                         groupchat = dataSnapshot.child("groupchat").getValue(Boolean.class);
                     }

                     if(dataSnapshot.hasChild("chatname")){
                         name = dataSnapshot.child("chatname").getValue(String.class);
                     }
                     messageDialog.setGroupchat(groupchat);
                     messageDialog.setDialogName(name);

                     // is one to one chat
                    if(!mDialogs.contains(messageDialog)&& !groupchat) {
                        noChat.setVisibility(View.GONE);
                       mDialogs.add(messageDialog);
                        addlastMessageListener(messageDialog);
                        addMuteListener(messageDialog);
                        addUnreadListener(messageDialog);
                        addnameListener(messageDialog);
                        addPhotoUrlListener(messageDialog);
                     }
                 }



            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                showLog(dataSnapshot + " Changed");
                if(dataSnapshot.hasChild("lastmessage") ){
                    boolean muted =false ,groupchat =false;
                    String name = "";
                    UserMessageDialog messageDialog = new UserMessageDialog();
                    UserMessage message = dataSnapshot.getValue(UserMessage.class);
                    if(dataSnapshot.hasChild("mute")){
                        muted = dataSnapshot.child("mute").getValue(boolean.class);
                    }
                    messageDialog.setId(dataSnapshot.getKey());
                    messageDialog.setLastMessage(message);
                    messageDialog.setMuted(muted);

                    if(dataSnapshot.hasChild("groupchat")){
                        groupchat = dataSnapshot.child("groupchat").getValue(Boolean.class);
                    }

                    if(dataSnapshot.hasChild("chatname")){
                        name = dataSnapshot.child("chatname").getValue(String.class);
                    }
                    messageDialog.setGroupchat(groupchat);
                    messageDialog.setDialogName(name);

                    // is one to one chat
                    if(!mDialogs.contains(messageDialog)&& !groupchat) {
                        noChat.setVisibility(View.GONE);
                        mDialogs.add(messageDialog);
                        addlastMessageListener(messageDialog);
                        addMuteListener(messageDialog);
                        addUnreadListener(messageDialog);
                        addnameListener(messageDialog);
                        addPhotoUrlListener(messageDialog);
                    }
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("lastmessage")){
                    UserMessageDialog messageDialog = new UserMessageDialog();
                    messageDialog.setId(dataSnapshot.getKey());
                    mDialogs.remove(messageDialog);
                }
                if(mDialogs.isEmpty()){
                    noChat.setVisibility(View.VISIBLE);
                }


            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        msgRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                showLog(mDialogs.size() + " Done");
                loadingGif.setVisibility(View.GONE);
               if(mDialogs.isEmpty()){
                   noChat.setVisibility(View.VISIBLE);
               }else{
                   noChat.setVisibility(View.GONE);
                   YoYo.with(Techniques.FadeIn).duration(500).playOn(mRecyclerView);
               }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

     private  void addUnreadListener(final UserMessageDialog dialog){
        if(dialog.getId()!=null){
            String chatId = dialog.getId();
         msgRef.child(chatId).child("unread").addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(DataSnapshot dataSnapshot) {
                 if(mDialogs.contains(dialog)){
                     int index = mDialogs.indexOf(dialog);
                     Integer unread = dataSnapshot.getValue(Integer.class);
                     if(unread!=null){
                         dialog.setUnRead(unread);
                     }else{
                         dialog.setUnRead(0);
                     }
                     mDialogs.set(index,dialog);
                     Collections.sort(mDialogs,mComparator);
                     mAdapter.notifyDataSetChanged();


                 }
             }

             @Override
             public void onCancelled(DatabaseError databaseError) {

             }
         });
        }
     }

    private  void addMuteListener(final UserMessageDialog dialog){
        if(dialog.getId()!=null){
            String chatId = dialog.getId();
            msgRef.child(chatId).child("mute").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(mDialogs.contains(dialog)){
                        int index = mDialogs.indexOf(dialog);
                        Boolean mute  = dataSnapshot.getValue(Boolean.class);
                        if(mute!=null){
                            dialog.setMuted(mute);
                        }else{
                            dialog.setMuted(false);
                        }
                        mDialogs.set(index,dialog);
                        Collections.sort(mDialogs,mComparator);
                        mAdapter.notifyDataSetChanged();


                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }


    private  void addlastMessageListener(final UserMessageDialog dialog){
        if(dialog.getId()!=null){
            String chatId = dialog.getId();
            msgRef.child(chatId).child("lastmessage").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(mDialogs.contains(dialog)){
                        int index = mDialogs.indexOf(dialog);
                        UserMessage lastMessage = dataSnapshot.getValue(UserMessage.class);
                        if(lastMessage!=null){
                            dialog.setLastMessage(lastMessage);
                        }
                        mDialogs.set(index,dialog);
                        Collections.sort(mDialogs,mComparator);
                        mAdapter.notifyDataSetChanged();


                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }


    private  void addnameListener(final UserMessageDialog dialog){
        if(dialog.getId()!=null){
            String chatId = dialog.getId();
            userInfoRef.child(chatId).child("displayname").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(mDialogs.contains(dialog)){
                        int index = mDialogs.indexOf(dialog);
                        String name = dataSnapshot.getValue(String.class);
                        if(name!=null){
                            dialog.setDialogName(name);
                        }else{
                            dialog.setDialogName("");
                        }
                        mDialogs.set(index,dialog);
                        Collections.sort(mDialogs,mComparator);
                        mAdapter.notifyDataSetChanged();


                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private  void addPhotoUrlListener(final UserMessageDialog dialog){
        if(dialog.getId()!=null){
            String chatId = dialog.getId();
            userInfoRef.child(chatId).child("photourl").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(mDialogs.contains(dialog)){
                        int index = mDialogs.indexOf(dialog);
                        String photourl = dataSnapshot.getValue(String.class);
                        if(photourl!=null){
                            dialog.setPhotoUrl(photourl);
                        }else{
                            dialog.setPhotoUrl(Default_Url);
                        }
                        mDialogs.set(index,dialog);
                        Collections.sort(mDialogs,mComparator);
                        mAdapter.notifyDataSetChanged();


                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }


}
