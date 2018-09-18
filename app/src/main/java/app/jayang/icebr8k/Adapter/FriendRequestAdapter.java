package app.jayang.icebr8k.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import app.jayang.icebr8k.Model.User;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.UserProfilePage;
import app.jayang.icebr8k.Utility.SendNotification;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.FriendRequestViewHolder> {

    private ArrayList<User> mUsers;
    private Context mContext;

    public FriendRequestAdapter(ArrayList<User> users, Context context) {
        mUsers = users;
        mContext = context;
    }

    @NonNull
    @Override
    public FriendRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        View v =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friendrequst_item, parent, false);

        return new FriendRequestViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendRequestViewHolder holder, int position){
       User user = mUsers.get(position);
       if(user!=null){
           holder.name .setText(user.getDisplayname());
           holder.score.setText(user.getScore()+"%");
           holder.username.setText(user.getUsername());
           Glide.with(mContext).load(user.getPhotourl()).apply(RequestOptions.circleCropTransform().placeholder(R.drawable.default_avatar3)).transition(DrawableTransitionOptions.withCrossFade(300))
                   .into(holder.avatar);
       }

    }



    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    class FriendRequestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView avatar;
        TextView name,username,score;
        Button accept,decline;
        public FriendRequestViewHolder(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            name = itemView.findViewById(R.id.name);
            username = itemView.findViewById(R.id.username);
            score = itemView.findViewById(R.id.score);
            accept = itemView.findViewById(R.id.accept);
            decline = itemView.findViewById(R.id.decline);
            avatar.setOnClickListener(this);
            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(getAdapterPosition()!=RecyclerView.NO_POSITION){
                        User user = mUsers.get(getAdapterPosition());
                       acceptFriend(user.getId());
                    }

                }
            });
            decline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(getAdapterPosition()!=RecyclerView.NO_POSITION){
                        User user = mUsers.get(getAdapterPosition());
                       declineFriend(user.getId());
                    }
                }
            });

        }

        @Override
        public void onClick(View v) {
            if(getAdapterPosition()!=RecyclerView.NO_POSITION){
                User user = mUsers.get(getAdapterPosition());
                Intent intent = new Intent(mContext, UserProfilePage.class);
                intent.putExtra("userInfo",user);
                intent.putExtra("userUid",user.getId());
                mContext.startActivity(intent);
            }
        }
    }


    private void acceptFriend(final String uid) {
        final FirebaseUser currentuser = FirebaseAuth.getInstance().getCurrentUser();
        if (uid != null && !uid.equals(currentuser.getUid())) {
            DatabaseReference acceptRef = FirebaseDatabase.getInstance().getReference().child("UserFriends").child(currentuser.getUid())
                    .child(uid).child("stats");
            acceptRef.setValue("accepted").addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    SendNotification.sendFriendRequestNotification(uid, currentuser.getDisplayName(), currentuser.getDisplayName()+" have accepted your friend request");
                }
            });

            DatabaseReference acceptRef2 = FirebaseDatabase.getInstance().getReference().child("UserFriends")
                    .child(uid).child(currentuser.getUid()).child("stats");
            acceptRef2.setValue("accepted");








        }
    }

    private void declineFriend(final String uid) {
        final FirebaseUser currentuser = FirebaseAuth.getInstance().getCurrentUser();
        if (uid != null && !uid.equals(currentuser.getUid())) {
            DatabaseReference acceptRef = FirebaseDatabase.getInstance().getReference().child("UserFriends").child(currentuser.getUid())
                    .child(uid).child("stats");
            acceptRef.setValue("declined").addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                }
            });

            DatabaseReference acceptRef2 = FirebaseDatabase.getInstance().getReference().child("UserFriends")
                    .child(uid).child(currentuser.getUid()).child("stats");
            acceptRef2.setValue("declined");
            }
    }
}
