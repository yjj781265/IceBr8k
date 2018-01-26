package app.jayang.icebr8k;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import app.jayang.icebr8k.Modle.User;
import app.jayang.icebr8k.Modle.UserLocationDialog;

/**
 * Created by yjj781265 on 1/25/2018.
 */

public class UserLocationDialogAdapter extends RecyclerView.Adapter<UserLocationDialogAdapter.UserLocationDialogVH> {


    private ArrayList<UserLocationDialog> mLocationDialogs;

    public UserLocationDialogAdapter(ArrayList<UserLocationDialog> locationDialogs) {
        mLocationDialogs = locationDialogs;
    }
    @Override
    public UserLocationDialogVH onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context =parent.getContext();
        View view = LayoutInflater.from(context).
                inflate(R.layout.userlocationdialog_item, parent, false);
        return new UserLocationDialogVH(view);
    }

    @Override
    public void onBindViewHolder(UserLocationDialogVH holder, int position) {
        Log.d("PeopleNearby_IceBr8k","binding");
         User user = mLocationDialogs.get(position).getUser();
        holder.username.setText(user.getUsername());
        holder.displayname.setText(user.getDisplayname());
        holder.score.setText(user.getScore());

        double miles = Double.valueOf(mLocationDialogs.get(position).getDistance());
        int intMiles= (int) Math.round(miles);
        String unit ="mi";
        if(intMiles ==0){
            intMiles = (int)Math.round(miles*5280);
            unit="ft";
        }

        holder.distanance.setText(String.valueOf(intMiles)+" "+unit);
            if(holder.image.getAlpha()!=1.0f) {
            holder.image.setAlpha(1.0f);
        }
        Glide.with(holder.image.getContext()).load(user.getPhotourl()).
                apply(RequestOptions.circleCropTransform()).into(holder.image);

        if ( user.getScore()!=null && user.getScore().equals("")) {
            holder.score.setVisibility(View.INVISIBLE);
        } else {
            holder.score.setText(user.getScore() + "%");

        }
        String online =user.getOnlineStats();
        if(online!=null){
            if (online.equals("2")) {
                holder.onlineStats.setImageResource(R.drawable.green_dot);
                holder.onlineStats.setVisibility(View.VISIBLE);
                if(holder.mRelativeLayout.getAlpha()!=1.0f) {
                    holder.mRelativeLayout.setAlpha(1.0f);
                }
            } else if (online.equals("1")) {
                holder.onlineStats.setImageResource(R.drawable.circle_shape_busy);
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
        return mLocationDialogs.size();
    }

    @Override
    public long getItemId(int position) {
        return (mLocationDialogs.get(position).getId()).hashCode() ;
    }

    public class UserLocationDialogVH extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView image,onlineStats;
        private TextView displayname,username,score,distanance;
        private RelativeLayout mRelativeLayout;

        protected UserLocationDialogVH(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.location_avatar);
            displayname =itemView.findViewById(R.id.location_name);
            username=itemView.findViewById(R.id.location_username);
            mRelativeLayout =itemView.findViewById(R.id.location_Rlayout);
            score = itemView.findViewById(R.id.location_score);
            onlineStats =itemView.findViewById(R.id.location_onlineStats);
            distanance =itemView.findViewById(R.id.location_distance);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition(); // gets item position
            if (position != RecyclerView.NO_POSITION) { // Check if an item was deleted, but the user clicked it before the UI removed it
               UserLocationDialog dialog = mLocationDialogs.get(position);
                User mUser =dialog.getUser();
                Intent intent = new Intent(view.getContext(), UserProfilePage.class);
                intent.putExtra("userInfo", mUser);
                intent.putExtra("userUid",dialog.getId());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                view.getContext().startActivity(intent);
            }
        }
    }



}