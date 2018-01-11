package app.jayang.icebr8k.Modle;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import com.google.firebase.database.DatabaseReference;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.utils.DateFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;

import app.jayang.icebr8k.R;

import static android.content.Context.CLIPBOARD_SERVICE;


/**
 * Created by yjj781265 on 12/28/2017.
 */

 public class CustomOutcomingMessageViewHolder extends MessageHolders.BaseMessageViewHolder<Message>
        implements View.OnClickListener,DateFormatter.Formatter,View.OnLongClickListener {

    protected ImageView userAvatar;
    protected TextView messageText, messageTime;


    public CustomOutcomingMessageViewHolder(View itemView) {
        super(itemView);

        userAvatar = itemView.findViewById(R.id.outcoming_avatar);
        messageTime = itemView.findViewById(R.id.outcoming_time);
        messageText = itemView.findViewById(R.id.outcoming_text);

        messageText.setOnClickListener(this);
        messageText.setLongClickable(true);
        messageText.setOnLongClickListener(this);

    }

    @Override
    public void onBind(Message message) {
        if( getAdapterPosition()!=0 && getAdapterPosition()==getLayoutPosition()){
            messageTime.setVisibility(View.INVISIBLE);
        }else if(getAdapterPosition()==0 && getAdapterPosition()==getLayoutPosition()){
            messageTime.setVisibility(View.VISIBLE);
        }

        if (messageTime != null) {
            if (message.getStatus() != null && !message.getStatus().
                    equals(itemView.getContext().getResources().getString(R.string.sendMessageError))) {
                messageTime.setSelected(true);
                messageTime.setText(message.getStatus());
                messageTime.setSelected(false);
                Log.d("MainChat", "not null" + message.getStatus() + "at" + getAdapterPosition());

            } else {
                messageTime.setSelected(false);
                messageTime.setText(format(message.getCreatedAt()));
            }

        } else {
            Log.d("MainChat", "null" + message.getStatus() + "at" + getAdapterPosition());
        }

        if (messageText != null) {
            messageText.setText(message.getText());
        }

        if (userAvatar != null) {
            boolean isAvatarExists = getImageLoader() != null
                    && message.getAuthor().getAvatar() != null
                    && !message.getAuthor().getAvatar().isEmpty();

            userAvatar.setVisibility(isAvatarExists ? View.VISIBLE : View.GONE);
            if (isAvatarExists) {
                getImageLoader().loadImage(userAvatar, message.getUser().getAvatar());
            }
        }

    }

    @Override
    public void onClick(View view) {

        if (messageTime.getVisibility() == View.VISIBLE) {
            messageTime.setVisibility(View.INVISIBLE);


        } else {
            messageTime.setVisibility(View.VISIBLE);

        }


    }


    @Override
    public String format(Date date) {
        if (date != null) {

            if (DateFormatter.isToday(date)) {
                return new SimpleDateFormat("h:mm a").format(date);
            } else if (DateFormatter.isYesterday(date)) {
                return "Yesterday " + new SimpleDateFormat("h:mm a").format(date);
            } else if (DateFormatter.isCurrentYear(date)) {
                return new SimpleDateFormat("h:mm a").format(date) + " " +
                        new SimpleDateFormat("MMM d ").format(date);
            } else {
                return new SimpleDateFormat("h:mm a").format(date) + " " +
                        new SimpleDateFormat("MMM d yyyy").format(date);
            }
        } else {
            return "";
        }


    }

    @Override
    public boolean onLongClick(View view) {
        ClipboardManager myClipboard = (ClipboardManager) itemView.getContext().getSystemService(CLIPBOARD_SERVICE);
        String text;
        text = messageText.getText().toString();

        ClipData myClip = ClipData.newPlainText("text", text);
        myClipboard.setPrimaryClip(myClip);

        Toast.makeText(itemView.getContext(), "Text Copied",Toast.LENGTH_SHORT).show();
        return true;

    }
}




