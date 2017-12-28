package app.jayang.icebr8k.Fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.health.UidHealthStats;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
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
import java.util.Collections;
import java.util.Date;

import app.jayang.icebr8k.MainChatActivity;
import app.jayang.icebr8k.Modle.Author;
import app.jayang.icebr8k.Modle.ChatDialog;
import app.jayang.icebr8k.Modle.Message;
import app.jayang.icebr8k.Modle.User;
import app.jayang.icebr8k.R;

/**
 * Created by yjj781265 on 11/9/2017.
 */

public class chat_frag extends Fragment implements DateFormatter.Formatter{
    private View mView;
    private DialogsListAdapter dialogsListAdapter;
    private DialogsList mDialogsList;
    private FirebaseUser currrentUser;
    private ImageLoader mImageLoader;
    private ArrayList<ChatDialog> mChatDialogs,tempChatList;
    private FirebaseDatabase mDatabase;
    private DatabaseReference childAddedRef,unReadCountRef,childChangededRef;
    private ActionMode actionMode;
    private ArrayList<Author> authors;
    private String TAG = "chatfrag";
    private FloatingActionButton fabChat;
    private Integer unReadcount,messageNodeCount,newDialogCount;
    private  boolean initChat,clicked;
    private Author author;
    private Message message;
    private RelativeLayout loadingGif;


    public chat_frag() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        newDialogCount =0;

        mImageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {
                Glide.with(imageView).load(url).
                        apply(RequestOptions.circleCropTransform()).into(imageView);
            }
        };

        authors = new ArrayList<>();
        mChatDialogs =new ArrayList<>();
        tempChatList= new ArrayList<>();
        dialogsListAdapter =new DialogsListAdapter(mImageLoader);
        dialogsListAdapter.setDatesFormatter(this);


        Log.d(TAG,"onCreate");








}
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.chat_frag, container, false);
        fabChat = mView.findViewById(R.id.fabChat);
        loadingGif = mView.findViewById(R.id.loadingImg_chatTab);
        mDialogsList=  mView.findViewById(R.id.dialogsList);
        mDialogsList.setAdapter(dialogsListAdapter,false);
        mDialogsList.setVisibility(View.VISIBLE);
        Log.d(TAG,"onCreateView");
        getMessages();


        dialogsListAdapter.setOnDialogClickListener(new DialogsListAdapter.OnDialogClickListener<ChatDialog>() {
            @Override
            public void onDialogClick(ChatDialog dialog) {
                if(actionMode!=null){
                    int poistion = mChatDialogs.indexOf(dialog);
                    View chatView= mDialogsList.getLayoutManager().findViewByPosition(poistion);
                  if(chatView.isSelected()){
                      chatView.setBackgroundColor(getContext().getResources().getColor(R.color.white));
                      chatView.setSelected(false);
                      tempChatList.remove(dialog);
                      actionMode.setTitle(String.valueOf(tempChatList.size()) + " Selected");
                  }else{
                      chatView.setBackgroundColor(getContext().getResources().getColor(R.color.creamYellow));
                      chatView.setSelected(true);
                      tempChatList.add(dialog);
                      actionMode.setTitle(String.valueOf(tempChatList.size()) + " Selected");

                  }
                }else{
                      refreshListAndToChat(dialog);
                }

            }

        });

        dialogsListAdapter.setOnDialogLongClickListener(new DialogsListAdapter.OnDialogLongClickListener<ChatDialog>() {
            @Override
            public void onDialogLongClick(ChatDialog dialog) {

                int poistion = mChatDialogs.indexOf(dialog);
                View chatView= mDialogsList.getLayoutManager().findViewByPosition(poistion);
                if (actionMode != null) {

                }else{
                    tempChatList.clear();
                    // Start the CAB using the ActionMode.Callback defined above
                    actionMode = getActivity().startActionMode(mActionModeCallback);
                    chatView.setSelected(true);
                    chatView.setBackgroundColor(getContext().getResources().
                            getColor(R.color.creamYellow));
                    tempChatList.add(dialog);
                    actionMode.setTitle(String.valueOf(tempChatList.size()) + " Selected");

                }

            }
        });

        return  mView;
    }




    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"Start");
        OneSignal.clearOneSignalNotifications();

        fabChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(getContext(),String.valueOf(messageNodeCount),Toast.LENGTH_SHORT).show();


            }
        });
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.chatdialog_menu, menu);
            return true;
        }


        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    showRemoveDialog();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;

            // reset color
            for(ChatDialog dialog : mChatDialogs){
                int poistion = mChatDialogs.indexOf(dialog);
                View chatView= mDialogsList.getLayoutManager().findViewByPosition(poistion);
                chatView.setBackgroundColor(getContext().getResources().getColor(R.color.white));
                chatView.setSelected(false);
            }

        }
    };

    public void showRemoveDialog(){
        new MaterialDialog.Builder(getContext())
                .content("Remove these conversations ?")
                .positiveText("Yes")
                .negativeText("No").onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                removeDialog();
            }
        }).onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.cancel();
            }
        }).show();
    }



    public void removeDialog(){
        for(ChatDialog dialog: tempChatList) {
            mChatDialogs.remove(dialog);
            dialogsListAdapter.setItems(mChatDialogs);
            DatabaseReference removeRef = mDatabase.getReference().child("Messages").child(currrentUser.getUid()).
                    child(dialog.getId());
            removeRef.removeValue();
            showToast("Removed");
        }

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

    //if user has lastmessage node
    private void getMessages() {
        dialogsListAdapter.clear();
        mChatDialogs.clear();
        messageNodeCount =0;
        initChat = false;
        DatabaseReference lastMessageRef = mDatabase.getReference().child("Messages").
                child(currrentUser.getUid());
        lastMessageRef.keepSynced(true);
        lastMessageRef. addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot messageNode : dataSnapshot.getChildren()){
                    if(!messageNode.getKey().equals("inChatRoom") &&
                            messageNode.hasChild("lastmessage") ){
                        message= messageNode.child("lastmessage").getValue(Message.class);
                        Log.d(TAG,message.getId() + " "+message.getCreatedAt()+ " " +
                                "\n"+message.getText() +"\n UserInfo for lastmessage "+message.getUser().getAvatar() + " "+message.getUser().getId() + " "+ message.getUser().getName() );
                        getUser(messageNode.getKey(),message,false);
                        messageNodeCount++;
                    }
                }
                initChat=true;

                if( initChat && messageNodeCount==0 ){
                    if(childAddedRef==null){
                        showToast("User has no chats");
                    }
                    addChildAddedListener();
                    Log.d(TAG,"childlistnener added from 0");
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getUser(final String userUid, final Message message, final boolean newChat){
        DatabaseReference userRef = mDatabase.getReference().child("Users").child(userUid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Log.d(TAG, user.getDisplayname()+" "+ user.getDisplayname()+ " "+user.getPhotourl());
                /************************* url is not accurate here ************************/
                author = new Author(message.getUser().getId(),message.getUser().getName(),currrentUser.getPhotoUrl().toString());
                message.setUser(author);

                Log.d(TAG,"after add userinfo"+ message.getId() + " "+message.getCreatedAt()+ " " +
                        "\n"+message.getText() +"\n UserInfo for lastmessage "+message.getUser().getAvatar() + " "+message.getUser().getId() + " "+ message.getUser().getName() );
                createDialogs(message,userUid,user,newChat);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void  createDialogs(final Message message, final String userUid, final User user, final boolean newChat){

        authors.clear();
        authors.add(message.getAuthor());
        Log.d(TAG, "newChat boolean is"+newChat);
         unReadCountRef = mDatabase.getReference("Messages").
                child(currrentUser.getUid()).child(userUid).child("unRead");
        unReadCountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                unReadcount = dataSnapshot.getValue(Integer.class);
                Log.d(TAG, "unreadcount is here");
                if (unReadcount != null ) {
                    Log.d(TAG, "unreadcount is " + dataSnapshot.getValue(Integer.class).
                            toString());
                  ChatDialog  mdialog = new ChatDialog(userUid, user.getPhotourl(), user.getDisplayname(),
                            authors, message, unReadcount);
                    Log.d(TAG, "new Dialog  " + mdialog.getDialogName() + " " +
                            mdialog.getUnreadCount() + mdialog.getDialogPhoto());
                    Log.d(TAG, "COndition " + mChatDialogs.contains(mdialog));


                    mChatDialogs.add(mdialog);
                    if (mChatDialogs.size() == messageNodeCount && messageNodeCount!=0 && !newChat) {
                        Log.d(TAG, "init chat done there are " + mChatDialogs.size());
                        Collections.sort(mChatDialogs);
                        dialogsListAdapter.setItems(mChatDialogs);
                        loadingGif.setVisibility(View.INVISIBLE);
                        for(ChatDialog dialog : mChatDialogs){
                            addChildChangeListener(dialog,false);
                        }
                        if(childAddedRef ==null) {
                            addChildAddedListener();
                            Log.d(TAG,"childlistnener added from null");
                        }




                    }else if(newChat){
                        Log.d(TAG, "The dialogArrList size is " + mChatDialogs.size());
                        Collections.sort(mChatDialogs);
                        dialogsListAdapter.setItems(mChatDialogs);
                        addChildChangeListener(mdialog, false);
                        Log.d(TAG, "new Dialog added into the adapter "
                                + dialogsListAdapter.getItemCount());
                    }



                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void addChildChangeListener(final ChatDialog chatDialog,boolean isNewChat) {
        String uid = chatDialog.getId();
        if (isNewChat) {
            final DatabaseReference lastMessageRef = mDatabase.getReference().child("Messages").
                    child(currrentUser.getUid()).child(uid);
            lastMessageRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.getKey().equals("inChatRoom") &&
                            dataSnapshot.hasChild("lastmessage")&&dataSnapshot.hasChild("unRead")) {
                        message = dataSnapshot.child("lastmessage").getValue(Message.class);
                        Log.d(TAG, "new dialog creating " + message.getText() + " "+ message.getUser().getAvatar() + " "+ message.getUser().getName());
                        getUser(dataSnapshot.getKey(), message,true);
                       lastMessageRef.removeEventListener(this);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } else {
            childChangededRef = mDatabase.getReference("Messages").
                    child(currrentUser.getUid()).child(uid);
            childChangededRef.keepSynced(true);
            childChangededRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Log.d(TAG, dataSnapshot.getKey() + " child changed");
                    if (dataSnapshot.getKey().equals("unRead")) {
                        unReadcount = dataSnapshot.getValue(Integer.class);
                        chatDialog.setUnreadcount(unReadcount);
                        dialogsListAdapter.updateItemById(chatDialog);
                        Log.d(TAG, "unreadcount changed to " + dataSnapshot.getValue(Integer.class).toString());

                    } else if (dataSnapshot.getKey().equals("lastmessage")) {
                        Message lastMessage = dataSnapshot.getValue(Message.class);
                        ///////// not accurent photourl
                        lastMessage.getAuthor().setAvatar(currrentUser.getPhotoUrl().toString());
                        chatDialog.setLastMessage(lastMessage);
                        dialogsListAdapter.updateDialogWithMessage(chatDialog.getId(),lastMessage);

                        Log.d(TAG, "lastmessage changed to " + lastMessage.getText());
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

    public void addChildAddedListener(){
        childAddedRef = mDatabase.getReference().child("Messages").child(currrentUser.getUid());
        childAddedRef.keepSynced(true);
              childAddedRef.addChildEventListener(new ChildEventListener() {
                  @Override
                  public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                      if(!dataSnapshot.getKey().equals("inChatRoom")
                               ){
                          Log.d(TAG,dataSnapshot.getKey() + " child added");
                          Log.d(TAG,"Dialog has size"+mChatDialogs.size());
                          ChatDialog chatDialog = new ChatDialog();
                          chatDialog.setId(dataSnapshot.getKey());
                          if(!mChatDialogs.isEmpty()&&checkDialogUniqueness(mChatDialogs,chatDialog)) {
                              Log.d(TAG, "new  find");
                              addChildChangeListener(chatDialog, true);
                          }else if(mChatDialogs.isEmpty()){
                              addChildChangeListener(chatDialog,true);
                              Log.d(TAG, "new  find was empty");
                          }
                      }
                  }

                  @Override
                  public void onChildChanged(DataSnapshot dataSnapshot, String s) {

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







    private boolean checkDialogUniqueness(ArrayList<ChatDialog> arrayList,ChatDialog mChatDialog){
        boolean flag = true;
        for(ChatDialog dialog :arrayList){
            if(dialog.getId().equals(mChatDialog.getId())){
              flag= false;
              break;
            }
        }
        return  flag;
    }

    private void refreshListAndToChat(ChatDialog dialog){
        if(dialog.getUnreadCount()>0 ) {
            dialog.setUnreadcount(0);
            Collections.sort(mChatDialogs);
            dialogsListAdapter.setItems(mChatDialogs);
        }
        Intent i = new Intent(getContext(), MainChatActivity.class);
        User user = new User();
        user.setDisplayname(dialog.getDialogName());
        user.setPhotourl(dialog.getDialogPhoto());
        i.putExtra("user2",user);
        i.putExtra("user2Id",dialog.getId());
        startActivity(i);
    }

    private  void toggleColor(View view){
        if(clicked){
           view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.creamYellow));
           clicked=false;

        }else{
            view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
            clicked=true;
        }
    }








    public void showToast(String str){
        Toast.makeText(getContext(),str,Toast.LENGTH_LONG).show();
    }



    }













