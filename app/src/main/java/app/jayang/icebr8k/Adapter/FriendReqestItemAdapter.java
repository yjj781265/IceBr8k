package app.jayang.icebr8k.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import app.jayang.icebr8k.Modle.Author;
import app.jayang.icebr8k.Modle.Message;
import app.jayang.icebr8k.Modle.User;
import app.jayang.icebr8k.Modle.UserDialog;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.SendNotification;
import app.jayang.icebr8k.UserProfilePage;

/**
 * Created by yjj781265 on 1/3/2018.
 */

public class FriendReqestItemAdapter extends RecyclerView.Adapter<FriendReqestItemAdapter.FriendRequestItemViewHolder> {

    private List<UserDialog> requestList;
    public  Context mContext;
    public class FriendRequestItemViewHolder extends RecyclerView.ViewHolder{
        public TextView name,username;
        public BootstrapButton  delete,accept;
        public ImageView avatar;

        public FriendRequestItemViewHolder(View itemView) {
            super(itemView);
            name =itemView.findViewById(R.id.name_frt);
            username =itemView.findViewById(R.id.username_frt);
            delete = itemView.findViewById(R.id.delete_frt);
            accept = itemView.findViewById(R.id.accept_frt);
            avatar = itemView.findViewById(R.id.avatar_frt);
        }
    }

    public FriendReqestItemAdapter(List<UserDialog> requestList,Context context) {
        this.requestList = requestList;
        mContext =context;
    }

    @Override
    public FriendRequestItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friendrequst_item, parent, false);
        return new FriendRequestItemViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(FriendRequestItemViewHolder holder, final int position) {
         final UserDialog dialog = requestList.get(position);
        holder.name.setText(dialog.getName());
        holder.username.setText(dialog.getUsername());
        Glide.with(mContext).load(dialog.getPhotoUrl()).apply(RequestOptions.circleCropTransform())
                .into(holder.avatar);
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteFriend(dialog.getId(),position);
            }
        });

        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acceptFriend(dialog.getId(),position);
            }
        });
        holder.avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toUserProfilePage(dialog);
            }
        });
        holder.avatar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                 view.setAlpha(0.5f);
                }else{
                    view.setAlpha(1.0f);
                }
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }


    private void deleteFriend(String uid, final int position) {
        FirebaseUser currentuser = FirebaseAuth.getInstance().getCurrentUser();
        if (uid != null && !uid.equals(currentuser.getUid())) {
            DatabaseReference deleteRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentuser.getUid())
                    .child(uid);
            deleteRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(mContext, "Friend Request Deleted", Toast.LENGTH_SHORT).show();

                }
            });


        }
    }

    private void acceptFriend(final String uid, final int position) {
        final FirebaseUser currentuser = FirebaseAuth.getInstance().getCurrentUser();
        if (uid != null && !uid.equals(currentuser.getUid())) {
            DatabaseReference acceptRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentuser.getUid())
                    .child(uid).child("stats");
            acceptRef.setValue("Accepted");

            DatabaseReference acceptRef2 = FirebaseDatabase.getInstance().getReference().child("Friends")
                    .child(uid).child(currentuser.getUid()).child("stats");
            acceptRef2.setValue("Accepted").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(mContext, "Friend Request Accepted", Toast.LENGTH_SHORT).show();
                    Author author = new Author("0406", currentuser.getDisplayName(),
                            currentuser.getPhotoUrl().toString());
                    final Message message = new Message();
                    message.setId(UUID.randomUUID().toString());
                    message.setText("I have accepted your friend request.");
                    Date date = new Date();
                    message.setTimestamp(String.valueOf(date.getTime()));
                    message.setAuthor(author);
                    message.setCreatedAt(date);
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().
                            child("Messages").child(currentuser.getUid()).
                            child(uid);
                    ref.child("chathistory").child(message.getId()).setValue(message);
                    ref.child("lastmessage").setValue(message);
                    ref.child("inChat").setValue(false);
                    ref.child("unRead").setValue(0);
                    DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference().
                            child("Messages").child(uid).child(currentuser.getUid());
                    author.setId("0401");
                    message.setAuthor(author);
                    ref2.child("chathistory").child(message.getId()).
                            setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            DatabaseReference playerIdRef = FirebaseDatabase.getInstance().getReference().child("Notification").child(uid).
                                    child("player_id");
                            playerIdRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String playerid = dataSnapshot.getValue(String.class);
                                    SendNotification.sendNotificationTo(playerid, currentuser.getDisplayName(), message.getText(), currentuser.getUid());
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                        }
                    });
                    ref2.child("lastmessage").setValue(message);
                    ref2.child("inChat").setValue(false);
                    ref2.child("unRead").setValue(1);


                }
            });


        }
    }
    public void toUserProfilePage(UserDialog dialog){
        User mUser = new User(dialog.getName(), dialog.getUsername(),
                dialog.getPhotoUrl(), dialog.getEmail());
        Intent intent = new Intent(mContext, UserProfilePage.class);
        intent.putExtra("userInfo", mUser);
        intent.putExtra("userUid",dialog.getId());
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_NEW_TASK );
        mContext.startActivity(intent);
    }









    public void removeAt(int position) {
        requestList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, requestList.size());
    }


}
