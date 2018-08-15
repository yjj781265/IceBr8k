package app.jayang.icebr8k.Adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.recyclerview.extensions.AsyncListDiffer;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
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
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;

import app.jayang.icebr8k.Modle.User;
import app.jayang.icebr8k.Modle.UserDialog;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.SearchName;
import app.jayang.icebr8k.UserProfilePage;
import app.jayang.icebr8k.Utility.MyDateFormatter;


/**
 * Created by LoLJay on 10/20/2017.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.UserDialogViewHolder> {
    private Activity mActivity;
    private ArrayList<UserDialog> mUserDialogs;
    private ArrayList<UserDialog> mFilteredList;


    public class UserDialogViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView image, onlineStats;
        private TextView displayname, username, score, lastSeen;
        private long lastClickTime = 0;

        public UserDialogViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageview_id);
            displayname = itemView.findViewById(R.id.displayname_textview);
            username = itemView.findViewById(R.id.username_textview);
            score = itemView.findViewById(R.id.score);
            onlineStats = itemView.findViewById(R.id.onlineStats);
            lastSeen = itemView.findViewById(R.id.lastseen);


            if (getAdapterPosition() != RecyclerView.NO_POSITION) {

            }

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            // preventing double, using threshold of 1000 ms
            if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                return;
            }
            lastClickTime = SystemClock.elapsedRealtime();
            int position = getAdapterPosition(); // gets item position
            if (position != RecyclerView.NO_POSITION) { // Check if an item was deleted, but the user clicked it before the UI removed it
                UserDialog dialog = mUserDialogs.get(position);
                Intent i = new Intent(mActivity, UserProfilePage.class);
                i.putExtra("userInfo", dialog.getUser());
                i.putExtra("userUid", dialog.getId());
                mActivity.startActivity(i);
                if (mActivity instanceof SearchName) {
                    mActivity.finish();
                }
            }
        }


    }


    public RecyclerAdapter(Activity activity, ArrayList<UserDialog> dialogs) {
        mActivity = activity;
        mUserDialogs = dialogs;
        mFilteredList = dialogs;

    }


    @Override
    public UserDialogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).
                inflate(R.layout.recycler_item, parent, false);
        return new UserDialogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final UserDialogViewHolder holder, int position) {
        final UserDialog dialog = mUserDialogs.get(position);
        final User user = dialog.getUser();

        if (dialog.getScore() == null || dialog.getScore().equals("")) {
            holder.score.setVisibility(View.GONE);
        } else {
            holder.score.setVisibility(View.VISIBLE);
            holder.score.setText(dialog.getScore() + "%");

        }


        if (user != null) {
            holder.displayname.setText(user.getDisplayname());
            String online = dialog.getUser().getOnlinestats();
            if (online != null) {
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
                } else if ("0".equals(online)) {
                    holder.onlineStats.setVisibility(View.INVISIBLE);
                    holder.score.setAlpha(0.5f);
                    holder.image.setAlpha(0.5f);
                    holder.displayname.setAlpha(0.5f);
                    holder.lastSeen.setAlpha(0.5f);

                } else {
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

            Long lastseen = user.getLastseen();
            if (lastseen != null) {
                holder.lastSeen.setVisibility(View.VISIBLE);
                holder.lastSeen.setText(MyDateFormatter.lastSeenConverterShort(lastseen));
            } else {
                holder.lastSeen.setVisibility(View.GONE);
            }

            Glide.with(mActivity).load(user.getPhotourl()).apply(RequestOptions.circleCropTransform().placeholder(R.drawable.default_avatar3)).transition(DrawableTransitionOptions.withCrossFade(300))
                    .into(holder.image);
        }


    }

    @Override
    public int getItemCount() {
        return mUserDialogs.size();
    }

    @Override
    public long getItemId(int position) {
        return (mUserDialogs.get(position).getId()).hashCode();
    }


}
