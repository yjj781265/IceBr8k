package app.jayang.icebr8k.Adapter;

import android.os.SystemClock;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import app.jayang.icebr8k.Modle.UserMessageDialog;
import app.jayang.icebr8k.R;

/**
 * Created by yjj781265 on 2/19/2018.
 */

public class UserMessageDialogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<UserMessageDialog> dialogs;


    public UserMessageDialogAdapter(ArrayList<UserMessageDialog> dialogs) {
        this.dialogs = dialogs;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.usermessagedialog, parent, false);
        return new UserMessageDialogViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return dialogs.size();
    }

    @Override
    public long getItemId(int position) {
        return dialogs.get(position).getId().hashCode();
    }

    static class UserMessageDialogViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private long lastClickTime = 0;
        public TextView dialogName,unRead;
        public TextView lastMessage,date;
        public ImageView avatar,muted;
        public UserMessageDialogViewHolder(View itemView) {
            super(itemView);
            dialogName = (TextView) itemView.findViewById(R.id.messageDialog_name);
            unRead = (TextView) itemView.findViewById(R.id.messageDialog_unread);
            lastMessage = itemView.findViewById(R.id.messageDialog_lastmessage);
            date = itemView.findViewById(R.id.messageDialog_date);
            avatar =itemView.findViewById(R.id.messageDialog_avatar);
            muted = itemView.findViewById(R.id.messageDialog_notification_off);
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

            }
        }
    }
}
