package app.jayang.icebr8k.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.recyclerview.extensions.AsyncListDiffer;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;


import java.util.ArrayList;

import app.jayang.icebr8k.Modle.User;
import app.jayang.icebr8k.Modle.UserDialog;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.UserProfilePage;


/**
 * Created by LoLJay on 10/20/2017.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.UserDialogViewHolder>
{
    private Context context;
    private ArrayList<UserDialog>mUserDialogs;
    private ArrayList<UserDialog> mFilteredList;


    public class UserDialogViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView image,onlineStats;
        private TextView displayname,username,score,lastSeen;
        private ConstraintLayout container;
        private long lastClickTime =0;

        public UserDialogViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageview_id);
            displayname =itemView.findViewById(R.id.displayname_textview);
            username=itemView.findViewById(R.id.username_textview);
            score = itemView.findViewById(R.id.score);
            onlineStats =itemView.findViewById(R.id.onlineStats);
            lastSeen =itemView.findViewById(R.id.lastseen);
            container = itemView.findViewById(R.id.userTab_item_container);


            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            // preventing double, using threshold of 1000 ms
            if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                return;
            }
            lastClickTime = SystemClock.elapsedRealtime();
            int position = getAdapterPosition(); // gets item position
            if (position != RecyclerView.NO_POSITION) { // Check if an item was deleted, but the user clicked it before the UI removed it
                UserDialog dialog = mUserDialogs.get(position);
                User mUser = new User(dialog.getName(), dialog.getUsername(),
                        dialog.getPhotoUrl(), dialog.getEmail());
                Intent intent = new Intent(view.getContext(), UserProfilePage.class);
                intent.putExtra("userInfo", mUser);
                intent.putExtra("userUid",dialog.getId());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                hideKeyboard(view);
                view.getContext().startActivity(intent);


            }
        }

        private  void hideKeyboard(View view){
            //hide keyboard
            if (view != null) {
                InputMethodManager imm =   (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }




    public RecyclerAdapter(Context context, ArrayList<UserDialog> dialogs) {
        this.context = context;
        mUserDialogs = dialogs;
        mFilteredList =dialogs;

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
        if(dialog.getUsername()!=null) {
            holder.username.setText(dialog.getUsername());
        }

        if(dialog.getName()!=null){
            holder.displayname.setText(dialog.getName());
        }


        if(dialog.getPhotoUrl()!=null){
            Glide.with(holder.image.getContext()). load(dialog.getPhotoUrl()).
                    apply(RequestOptions.circleCropTransform().placeholder(R.drawable.default_avatar3)). into(holder.image);
        }


        if ( dialog.getScore()!=null && dialog.getScore().equals("")) {
            holder.score.setVisibility(View.GONE);
        } else {
            holder.score.setVisibility(View.VISIBLE);
            holder.score.setText(dialog.getScore() + "%");

        }
        String lastseen = dialog.getLastseen();
        if(lastseen!=null){
            holder.lastSeen.setVisibility(View.VISIBLE);
            holder.lastSeen.setText(lastseen);
        }else{
            holder.lastSeen.setVisibility(View.GONE);
        }


        String online =dialog.getOnlinestats();
        if(online!=null){
            if (online.equals("2")) {
                holder.onlineStats.setImageResource(R.drawable.green_dot);
                holder.onlineStats.setVisibility(View.VISIBLE);
                holder.score.setAlpha(1.0f);
                holder.image.setAlpha(1.0f);
                holder.displayname.setAlpha(1.0f);




            } else if (online.equals("1")) {
                holder.onlineStats.setImageResource(R.drawable.circle_shape_busy);
                holder.onlineStats.setVisibility(View.VISIBLE);
                holder.score.setAlpha(1.0f);
                holder.image.setAlpha(1.0f);
                holder.displayname.setAlpha(1.0f);
            } else if("0".equals(online)) {
                holder.onlineStats.setVisibility(View.INVISIBLE);
                holder.score.setAlpha(0.5f);
                holder.image.setAlpha(0.5f);
                holder.displayname.setAlpha(0.5f);
                holder.lastSeen.setAlpha(0.5f);

            }else{
                holder.onlineStats.setVisibility(View.INVISIBLE);
                holder.score.setAlpha(0.5f);
                holder.image.setAlpha(0.5f);
                holder.displayname.setAlpha(0.5f);
                holder.lastSeen.setAlpha(0.5f);

            }

        } else {
            // null
            holder.onlineStats.setVisibility(View.INVISIBLE);
            holder.score.setAlpha(0.5f);
            holder.image.setAlpha(0.5f);
            holder.displayname.setAlpha(0.5f);
            holder.lastSeen.setAlpha(0.5f);

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

    public void submitList(ArrayList<UserDialog> list) {
        mDiffer.submitList(list);
    }


    private final AsyncListDiffer<UserDialog> mDiffer = new AsyncListDiffer(this, DIFF_CALLBACK);
     public static final  DiffUtil.ItemCallback<UserDialog> DIFF_CALLBACK
             = new DiffUtil.ItemCallback<UserDialog>() {
         @Override
         public boolean areItemsTheSame(UserDialog oldItem, UserDialog newItem) {
             return oldItem.getId().equals(newItem);
         }

         @Override
         public boolean areContentsTheSame(UserDialog oldItem, UserDialog newItem) {
              return oldItem.getScore().
                     equals(newItem.getScore())
                     ||oldItem.getOnlinestats().
                     equals(newItem.getOnlinestats())
                     ||oldItem.getPhotoUrl().
                     equals(newItem.getPhotoUrl())
                     ||oldItem.getName().
                     equals(newItem.getName())
                     ||oldItem.getLastseen().
                     equals(newItem.getLastseen());
         }
     };


}
