package app.jayang.icebr8k;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;


import java.util.ArrayList;

import app.jayang.icebr8k.Modle.User;


/**
 * Created by LoLJay on 10/20/2017.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<Viewholder> implements FastScrollRecyclerView.SectionedAdapter {
    private ArrayList<User> userArrayList = new ArrayList<>();
    private Context context;
    FirebaseUser currentUser;


    public RecyclerAdapter(Context context, ArrayList<User> userArrayList) {
        this.userArrayList = userArrayList;
        this.context = context;

    }


    @Override
    public Viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(final Viewholder holder, int position) {
        holder.onlineStats.setVisibility(View.GONE);
        holder.linearLayout.setAlpha((float) 0.5);


        final User mUser = userArrayList.get(position);


        holder.username.setText(mUser.getUsername());
        holder.displayname.setText(mUser.getDisplayname());
        Glide.with(holder.image.getContext()).load(mUser.getPhotourl()).
                apply(RequestOptions.circleCropTransform()).into(holder.image);

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Usernames/" + mUser.getUsername());
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String mUserUid = dataSnapshot.getValue(String.class);

                DatabaseReference mRef2 = FirebaseDatabase.getInstance().getReference("Users/" + mUserUid + "/onlineStats");
                mRef2.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String online = dataSnapshot.getValue(String.class);
                        if (online != null) {
                            if (online.equals("1")) {
                                holder.onlineStats.setImageResource(R.drawable.green_dot);
                                holder.onlineStats.setVisibility(View.VISIBLE);
                                holder.linearLayout.setAlpha((float) 1.0);


                            }else if(online.equals("2")) {
                                holder.onlineStats.setImageResource(R.drawable.red_dot);
                                holder.onlineStats.setVisibility(View.VISIBLE);
                                holder.linearLayout.setAlpha((float) 0.8);
                            } else {
                                holder.linearLayout.setAlpha((float) 0.5);
                                holder.onlineStats.setVisibility(View.GONE);
                            }
                        } else {
                            // null
                            holder.linearLayout.setVisibility(View.VISIBLE);
                            holder.linearLayout.setAlpha((float) 0.5);
                            holder.onlineStats.setVisibility(View.GONE);
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


        // SpannableString ss1=  new SpannableString(score);
        // ss1.setSpan(new RelativeSizeSpan((float)0.5),1,3,0);// set size for %
        // ss1.setSpan(new ForegroundColorSpan(context.getColor(R.color.darkYellow)), 0, 2, 0);// set color


        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), UserProfilePage.class);
                intent.putExtra("userInfo", mUser);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userArrayList.size();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return userArrayList.get(position).getDisplayname().substring(0, 1);
    }


}

