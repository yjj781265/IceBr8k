package app.jayang.icebr8k.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.daimajia.androidanimations.library.Techniques;


import java.util.ArrayList;

import app.jayang.icebr8k.Modle.UserMessageDialog;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.UserChatActvity;
import app.jayang.icebr8k.Utility.MyDateFormatter;

import static app.jayang.icebr8k.MyApplication.getContext;

/**
 * Created by yjj781265 on 2/19/2018.
 */

public class UserMessageDialogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<UserMessageDialog> dialogs;
    private Activity mContext;


    public UserMessageDialogAdapter(ArrayList<UserMessageDialog> dialogs, Activity context) {
        this.dialogs = dialogs;
        mContext =context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.usermessagedialog, parent, false);
        return new UserMessageDialogViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        UserMessageDialog messageDialog = dialogs.get(position);
        if(holder instanceof UserMessageDialogViewHolder){
            //check mute
            if(messageDialog.getMuted()){
                ((UserMessageDialogViewHolder) holder).muted.setVisibility(View.VISIBLE);
            }else{
                ((UserMessageDialogViewHolder) holder).muted.setVisibility(View.INVISIBLE);
            }
            // set badge unread
            if(messageDialog.getUnRead()!=null && messageDialog.getUnRead()>0){
                ((UserMessageDialogViewHolder) holder).unRead.setText(messageDialog.getUnRead().toString());
                ((UserMessageDialogViewHolder) holder).unRead.setVisibility(View.VISIBLE);
            }else{
                ((UserMessageDialogViewHolder) holder).unRead.setVisibility(View.INVISIBLE);
            }
            //photo avatar
            if(messageDialog.getPhotoUrl()!=null){
              ImageView avatsr=((UserMessageDialogViewHolder) holder).avatar;
                Glide.with(mContext).load(messageDialog.getPhotoUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(avatsr);
            }

            if(messageDialog.getLastMessage()!=null){
                if(messageDialog.getUnRead()!=null && messageDialog.getUnRead()>0){
                    ((UserMessageDialogViewHolder) holder).lastMessage.setText(messageDialog.getLastMessage().getText());
                    ((UserMessageDialogViewHolder) holder).lastMessage.setTypeface(null, Typeface.BOLD);

                }else{
                    ((UserMessageDialogViewHolder) holder).lastMessage.setText(messageDialog.getLastMessage().getText());
                    ((UserMessageDialogViewHolder) holder).lastMessage.setTypeface(null, Typeface.NORMAL);
                }
            }

            if(messageDialog.getDialogName()!=null){
                if(messageDialog.getUnRead()!=null && messageDialog.getUnRead()>0){
                    ((UserMessageDialogViewHolder) holder).dialogName.setText(messageDialog.getDialogName());
                    ((UserMessageDialogViewHolder) holder).dialogName.setTypeface(null, Typeface.BOLD);

                }else{
                    ((UserMessageDialogViewHolder) holder).dialogName.setText(messageDialog.getDialogName());
                    ((UserMessageDialogViewHolder) holder).dialogName.setTypeface(null, Typeface.NORMAL);
                }
            }

            if(messageDialog.getLastMessage().getTimestamp()!=null){
                long timestamp = messageDialog.getLastMessage().getTimestamp();
                ((UserMessageDialogViewHolder) holder).date.setText(MyDateFormatter.timeStampToDateConverter(timestamp,true));
            }

        }

    }

    @Override
    public int getItemCount() {
        return dialogs.size();
    }

    @Override
    public long getItemId(int position) {
        return dialogs.get(position).getId().hashCode();
    }

     public class UserMessageDialogViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private long lastClickTime = 0;
        private TextView dialogName,unRead;
        private TextView lastMessage,date;
        private ImageView avatar,muted;
        private UserMessageDialogViewHolder(View itemView) {
            super(itemView);
            dialogName = (TextView) itemView.findViewById(R.id.messageDialog_name);
            unRead = (TextView) itemView.findViewById(R.id.messageDialog_unread);
            lastMessage = itemView.findViewById(R.id.messageDialog_lastmessage);
            date = itemView.findViewById(R.id.messageDialog_date);
            avatar =itemView.findViewById(R.id.messageDialog_avatar);
            muted = itemView.findViewById(R.id.messageDialog_notification_off);
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
               Intent i =  new Intent(mContext, UserChatActvity.class);
               i.putExtra("chatId",dialogs.get(position).getId());
               i.putExtra("chatName",dialogs.get(position).getDialogName());
               i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
               mContext.startActivity(i);
               mContext.overridePendingTransition(R.anim.slide_from_right,0);

            }
        }
    }
}
