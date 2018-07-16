package app.jayang.icebr8k.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import app.jayang.icebr8k.Modle.LeaderboardDialog;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.UserProfilePage;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {
   private ArrayList<LeaderboardDialog> mLeaderboardDialogs;
   private Context mContext;

    public LeaderboardAdapter(ArrayList<LeaderboardDialog> leaderboardDialogs, Context context) {
        mLeaderboardDialogs = leaderboardDialogs;
        mContext = context;
    }

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new LeaderboardViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
          LeaderboardDialog dialog = mLeaderboardDialogs.get(position);

          holder.rank.setText(dialog.getRank());
          // set badge for top 3 players
          if("1".equals(dialog.getRank())){
              holder.badge.setImageResource(R.drawable.gold_medal);
              holder.badge.setVisibility(View.VISIBLE);
          }else if("2".equals(dialog.getRank())) {
              holder.badge.setImageResource(R.drawable.silver_medal);
              holder.badge.setVisibility(View.VISIBLE);
          }else if("3".equals(dialog.getRank())) {
              holder.badge.setImageResource(R.drawable.bronze_medal);
              holder.badge.setVisibility(View.VISIBLE);
          }else{
              holder.badge.setVisibility(View.GONE);
          }

          if(dialog.getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
              holder.itemView.setBackgroundColor( ContextCompat.getColor(mContext, R.color.dialog_divider));
          }else{
              holder.itemView.setBackgroundColor( ContextCompat.getColor(mContext, R.color.lightBlue));
          }
          holder.qSum.setText(dialog.getQuestionSum().toString());
          if(dialog.getUser()==null){
              holder.name.setText(dialog.getId());
          }else{
              holder.name.setText(dialog.getUser().getDisplayname());
              Glide.with(mContext).load(dialog.getUser().getPhotourl()).
                      apply(RequestOptions.circleCropTransform().placeholder(R.drawable.default_avatar3)).into(holder.avatar);
          }



    }



    @Override
    public int getItemCount() {
        return mLeaderboardDialogs.size();
    }

    public class LeaderboardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView rank,name,qSum;
        private ImageView avatar,badge;

        public LeaderboardViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            rank = itemView.findViewById(R.id.leaderboard_item_rank);
            name = itemView.findViewById(R.id.leaderboard_item_name);
            qSum = itemView.findViewById(R.id.leaderboard_item_qsum);
            avatar = itemView.findViewById(R.id.leaderboard_item_avatar);
            badge = itemView.findViewById(R.id.leaderboard_item_badge);

        }

        @Override
        public void onClick(View v) {
            if(getAdapterPosition()!= RecyclerView.NO_POSITION){
                LeaderboardDialog dialog = mLeaderboardDialogs.get(getAdapterPosition());
                Intent intent = new Intent(mContext, UserProfilePage.class);
                intent.putExtra("userInfo",dialog.getUser());
                intent.putExtra("userUid",dialog.getId());
                mContext.startActivity(intent);
            }
        }
    }
}
