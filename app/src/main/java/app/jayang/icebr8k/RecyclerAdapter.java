package app.jayang.icebr8k;

import android.content.Context;
import android.content.Intent;
import android.support.v7.util.DiffUtil;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import app.jayang.icebr8k.Modle.UserDialogsChange;
import app.jayang.icebr8k.Modle.User;
import app.jayang.icebr8k.Modle.UserDialog;
import app.jayang.icebr8k.Modle.UserQA;


/**
 * Created by LoLJay on 10/20/2017.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.UserDialogViewHolder>  {
    private Context context;
    private ArrayList<UserDialog>mUserDialogs;
    private Comparator<UserDialog> mComparator;


    public class UserDialogViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
         private ImageView image,onlineStats;
         private TextView displayname,username,score;
         private RelativeLayout mRelativeLayout;

        public UserDialogViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageview_id);
            displayname =itemView.findViewById(R.id.displayname_textview);
            username=itemView.findViewById(R.id.username_textview);
            mRelativeLayout =itemView.findViewById(R.id.user_item_RLayout);
            score = itemView.findViewById(R.id.score);
            onlineStats =itemView.findViewById(R.id.onlineStats);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition(); // gets item position
            if (position != RecyclerView.NO_POSITION) { // Check if an item was deleted, but the user clicked it before the UI removed it
                UserDialog dialog = mUserDialogs.get(position);
                User mUser = new User(dialog.getName(), dialog.getUsername(),
                        dialog.getPhotoUrl(), dialog.getEmail());
                Intent intent = new Intent(view.getContext(), UserProfilePage.class);
                intent.putExtra("userInfo", mUser);
                intent.putExtra("userUid",dialog.getId());
                context.startActivity(intent);

            }
        }
    }


    private  final SortedList<UserDialog> mSortedList = new SortedList<>(UserDialog.class,
            new SortedList.Callback<UserDialog>() {
        @Override
        public void onInserted(int position, int count) {
            notifyItemChanged(position,count);

        }

        @Override
        public void onRemoved(int position, int count) {
            notifyItemChanged(position,count);

        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            notifyItemChanged(fromPosition,toPosition);

        }

        @Override
        public int compare(UserDialog o1, UserDialog o2) {

            return mComparator.compare(o1,o2);
        }

        @Override
        public void onChanged(int position, int count) {
            notifyItemChanged(position,count);

        }

        @Override
        public boolean areContentsTheSame(UserDialog oldItem, UserDialog newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areItemsTheSame(UserDialog item1, UserDialog item2) {
            return item1.getId()==item2.getId();
        }
    });

    public void add(UserDialog dialog){
        mSortedList.add(dialog);
    }

    public void remove(UserDialog dialog){
        mSortedList.remove(dialog);
    }

    public void add(ArrayList<UserDialog> dialogs){
        mSortedList.addAll(dialogs);
    }

    public void remove(ArrayList<UserDialog> dialogs){
        mSortedList.beginBatchedUpdates();
        for(UserDialog dialog : dialogs ){
            mSortedList.remove(dialog);
        }
        mSortedList.endBatchedUpdates();
    }

    public void replaceAll(ArrayList<UserDialog> dialogs){
        mSortedList.beginBatchedUpdates();
       for(int i = mSortedList.size()-1;i>=0;i--){
           final UserDialog dialog = mSortedList.get(i);
           if(!dialogs.contains(dialog)){
               mSortedList.remove(dialog);
           }
       }
       mSortedList.addAll(dialogs);
        mSortedList.endBatchedUpdates();
    }


    public RecyclerAdapter(Context context, ArrayList<UserDialog> dialogs) {
        this.context = context;
        mUserDialogs = dialogs;

    }



    @Override
    public UserDialogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context =parent.getContext();
        View view = LayoutInflater.from(context).
                inflate(R.layout.recycler_item, parent, false);
        return new UserDialogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final UserDialogViewHolder holder, int position) {
        final UserDialog dialog = mUserDialogs.get(position);
        Log.d("UserFrag","onBind "+ dialog.getId());
        holder.username.setText(dialog.getUsername());
        holder.displayname.setText(dialog.getName());
        if(holder.image.getAlpha()!=1.0f) {
            holder.image.setAlpha(1.0f);
        }
        Glide.with(holder.image.getContext()).load(dialog.getPhotoUrl()).
                apply(RequestOptions.circleCropTransform()).into(holder.image);

        if (dialog.getScore().equals("")) {
            holder.score.setVisibility(View.INVISIBLE);
        } else {
            holder.score.setText(dialog.getScore() + "%");

        }
        String online =dialog.getOnlineStats();
           if(online!=null){
                    if (online.equals("2")) {
                        holder.onlineStats.setImageResource(R.drawable.green_dot);
                        holder.onlineStats.setVisibility(View.VISIBLE);
                        if(holder.mRelativeLayout.getAlpha()!=1.0f) {
                            holder.mRelativeLayout.setAlpha(1.0f);
                        }



                    } else if (online.equals("1")) {
                        holder.onlineStats.setImageResource(R.drawable.circleshape_busy);
                        holder.onlineStats.setVisibility(View.VISIBLE);
                        if(holder.mRelativeLayout.getAlpha()!=1.0f) {
                            holder.mRelativeLayout.setAlpha(1.0f);
                        }




                    } else {
                        holder.onlineStats.setVisibility(View.INVISIBLE);
                        holder.image.setAlpha(0.5f);
                        holder.mRelativeLayout.setAlpha(0.5f);

                    }

                } else {
                    // null
                    holder.onlineStats.setVisibility(View.INVISIBLE);
                    holder.image.setAlpha(0.5f);
                    holder.mRelativeLayout.setAlpha(0.5f);
                }





            }





    @Override
    public int getItemCount() {
        return mUserDialogs.size();
    }

    @Override
    public long getItemId(int position) {
        return  (mUserDialogs.get(position).getId()).hashCode() ;
    }

    public void swapItems(ArrayList<UserDialog> dialogs) {
        // compute diffs
        final UserDialogsChange diffCallback = new UserDialogsChange(mUserDialogs,dialogs);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        // clear contacts and add
       mUserDialogs.clear();
       mUserDialogs.addAll(dialogs);

        diffResult.dispatchUpdatesTo(this);
        // calls adapter's notify methods after diff is computed
    }


}










