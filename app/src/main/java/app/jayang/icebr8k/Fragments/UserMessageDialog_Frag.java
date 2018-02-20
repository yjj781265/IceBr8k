package app.jayang.icebr8k.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

import app.jayang.icebr8k.Modle.UserMessage;
import app.jayang.icebr8k.Modle.UserMessageDialog;
import app.jayang.icebr8k.R;

/**
 * Created by yjj781265 on 2/19/2018.
 */

public class UserMessageDialog_Frag extends Fragment {
    private View fragView;
    private TextView noChat;
    private ArrayList<UserMessageDialog> mDialogs;
    private FirebaseUser currentUser;
    private RelativeLayout loadingGif;
    private RecyclerView mRecyclerView;
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
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mDialogs =new ArrayList<>();

        loadDialogs();
        return fragView;
    }

    public void showLog(String str){
        Log.d(TAG,str);
    }
    public void loadDialogs(){
        DatabaseReference dialogsRef = FirebaseDatabase.getInstance().getReference().
                child("Messages")
                .child(currentUser.getUid());
        dialogsRef.keepSynced(true);

        dialogsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.hasChild("lastmessage")){
                     UserMessageDialog messageDialog = new UserMessageDialog();
                     messageDialog.setId(dataSnapshot.getKey());
                     if(!mDialogs.contains(messageDialog)) {
                        // createDialg(messageDialog);
                     }
                 }



            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                showLog(dataSnapshot.getKey() + " Changed");
                if(!mDialogs.isEmpty()){

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

        dialogsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                showLog(mDialogs.size() + " Done");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private  void createDialg(final UserMessageDialog dialog){

        mDialogs.add(dialog);
        showLog(dialog.getId() + " Added");
        showLog("index " + mDialogs.indexOf(dialog));

        if(dialog.getId()!=null){
            DatabaseReference lastMessageRef = FirebaseDatabase.getInstance().getReference()
                    .child("Messages").child(currentUser.getUid()).child(dialog.getId()).child("lastmessage");

            lastMessageRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    UserMessage lastmessage = dataSnapshot.getValue(UserMessage.class);
                    showLog(lastmessage.toString());
                    dialog.setLastMessage(lastmessage);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }
}
