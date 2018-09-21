package app.jayang.icebr8k.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import app.jayang.icebr8k.Homepage;

import app.jayang.icebr8k.Model.UserDialog;
import app.jayang.icebr8k.Model.UserMessageDialog;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.UserChatActvity;
import app.jayang.icebr8k.Utility.MyDateFormatter;


/**
 * Created by yjj781265 on 2/19/2018.
 */

public class UserMessageDialogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<UserMessageDialog> dialogs;
    private ArrayList<UserMessageDialog>tempChatList;
    private Activity mContext;
    private Homepage mHomepage;
    private ActionMode actionMode;


    private ActionMode.Callback  actionModeCallbacks  = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.chatdialog_menu, menu);
            mHomepage = (Homepage)mContext;
            tempChatList = new ArrayList<>();

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    if(!tempChatList.isEmpty()){
                        showRemoveDialog();
                    }

                    return true;
                default:
                    return false;
            }

        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;

          Comparator<UserMessageDialog>  mComparator = new Comparator<UserMessageDialog>() {
                @Override
                public int compare(UserMessageDialog dialog, UserMessageDialog t1) {
                    int result =0;
                    if(dialog.getUnRead()!=null && t1.getUnRead()!=null){
                        result = t1.getUnRead().compareTo(dialog.getUnRead());
                    }
                    if(result == 0){
                        if(dialog.getLastMessage().getTimestamp()!=null &&t1.getLastMessage().getTimestamp()!=null) {
                            result = t1.getLastMessage().getTimestamp().compareTo(dialog.getLastMessage().getTimestamp());
                        }
                    }
                    return result;
                }
            };

            Collections.sort(dialogs,mComparator);
            notifyDataSetChanged();


        }
    };




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
        if(actionMode!=null){
            holder.itemView.setBackgroundColor(holder.itemView.isSelected()?
                    mContext.getResources().getColor(R.color.ripple):
                    mContext.getResources().getColor(R.color.white));
        }else{
            holder.itemView.setSelected(false);
            holder.itemView.setBackgroundColor(
                    mContext.getResources().getColor(R.color.white));
        }

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
                        .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.default_avatar3)).transition(DrawableTransitionOptions.withCrossFade(300))
                        .into(avatsr);
            }

            if(messageDialog.getLastMessage()!=null){
                String lastMessage = messageDialog.getLastMessage().getMessagetype()!=null &&  messageDialog.getLastMessage().getMessagetype().equals("image")
                        ? "[Image]" : messageDialog.getLastMessage().getText();

                if(messageDialog.getUnRead()!=null && messageDialog.getUnRead()>0){
                    ((UserMessageDialogViewHolder) holder).lastMessage.setText(lastMessage);
                    ((UserMessageDialogViewHolder) holder).lastMessage.setTypeface(null, Typeface.BOLD);

                }else{
                    ((UserMessageDialogViewHolder) holder).lastMessage.setText(lastMessage);
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

     public class UserMessageDialogViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
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
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // preventing double, using threshold of 1000 ms
            if (SystemClock.elapsedRealtime() - lastClickTime < 500){
                return;
            }
            lastClickTime = SystemClock.elapsedRealtime();
            int position = getAdapterPosition(); // gets item position

            if(actionMode!=null){
                UserMessageDialog dialog =null;
                if(position!= RecyclerView.NO_POSITION) {
                    dialog = dialogs.get(position);
                }

                if(actionMode!=null && !tempChatList.contains(dialog)){
                    view.setSelected(true);
                    view.setBackgroundColor(mContext.getResources().
                            getColor(R.color.ripple));
                    tempChatList.add(dialog);
                    actionMode.setTitle(String.valueOf(tempChatList.size()) + " Selected");
                }else if(actionMode != null && tempChatList.contains(dialog)){
                    view.setBackgroundColor(mContext.getResources().getColor(R.color.white));
                    view.setSelected(false);
                    tempChatList.remove(dialog);
                    if(tempChatList.isEmpty()){
                        actionMode.finish();
                    }else {
                        actionMode.setTitle(String.valueOf(tempChatList.size()) + " Selected");
                    }

                }
                // actionmode deactivated
            }else{
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

         @Override
         public boolean onLongClick(View view) {
             int position = getAdapterPosition();
             UserMessageDialog dialog =null;
             if(position!= RecyclerView.NO_POSITION) {
               dialog = dialogs.get(position);
             }

             if(actionMode!=null && !tempChatList.contains(dialog)){
                 view.setSelected(true);
                 view.setBackgroundColor(mContext.getResources().
                         getColor(R.color.ripple));
                 tempChatList.add(dialog);
                 actionMode.setTitle(String.valueOf(tempChatList.size()) + " Selected");
             }else if(actionMode != null && tempChatList.contains(dialog)){
                 view.setBackgroundColor(mContext.getResources().getColor(R.color.white));
                 view.setSelected(false);
                 tempChatList.remove(dialog);
                 if(tempChatList.isEmpty()){
                     actionMode.finish();
                 }else {
                     actionMode.setTitle(String.valueOf(tempChatList.size()) + " Selected");
                 }

             }else{
                 actionMode = ((AppCompatActivity)view.getContext()).startSupportActionMode(actionModeCallbacks);
                 view.setSelected(true);
                 view.setBackgroundColor(mContext.getResources().getColor(R.color.ripple));
                 if(dialog!=null)
                 tempChatList.add(dialog);
                 actionMode.setTitle(String.valueOf(tempChatList.size()) + " Selected");
             }
             return true;
         }
     }

    public void showRemoveDialog(){
        String content;
        if(tempChatList.size() ==1){
            content ="Remove this conversation ?";
        }else{
            content ="Remove these conversations ?";
        }
        new MaterialDialog.Builder(mContext)
                .content(content).negativeColor(mContext.getResources().getColor(R.color.holo_red_light))
                .positiveText("Yes")
                .negativeText("No").onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                removeDialog();
            }
        }).onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.cancel();
                actionMode.finish(); // Action picked, so close the CAB
            }
        }).show();
    }

    public void removeDialog(){
        for(UserMessageDialog dialog: tempChatList) {
            dialogs.remove(dialog);
            DatabaseReference removeRef = FirebaseDatabase.getInstance() .getReference().child("UserMessages").
                    child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                    child(dialog.getId());
            removeRef.removeValue();


        }
        dialogs.removeAll(tempChatList);
        String conversation = "Conversations";
        if(tempChatList.size() ==1){
            conversation = "Conversation";
        }
        Toast.makeText(mContext, tempChatList.size()+" "+ conversation + " Removed", Toast.LENGTH_SHORT).show();
        actionMode.finish(); // Action picked, so close the CAB


    }

     public void dismissActionMode(){
        if(actionMode!=null){
            actionMode.finish();
        }
     }



}
