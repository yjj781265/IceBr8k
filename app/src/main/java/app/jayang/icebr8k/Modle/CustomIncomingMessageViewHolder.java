package app.jayang.icebr8k.Modle;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.utils.DateFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;

import app.jayang.icebr8k.R;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * Created by yjj781265 on 1/9/2018.
 */

public class CustomIncomingMessageViewHolder extends MessageHolders.BaseMessageViewHolder<Message>
implements View.OnClickListener,View.OnLongClickListener,DateFormatter.Formatter{
    protected ImageView userAvatar;
    protected TextView messageText, messageTime;
    public CustomIncomingMessageViewHolder(View itemView) {
        super(itemView);
        userAvatar = itemView.findViewById(R.id.incoming_avatar);
        messageTime = itemView.findViewById(R.id.incoming_time);
        messageText = itemView.findViewById(R.id.incoming_text);

        messageText.setOnClickListener(this);
        messageText.setLongClickable(true);
        messageText.setOnLongClickListener(this);
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
    public boolean onLongClick(View view) {
        ClipboardManager myClipboard = (ClipboardManager) itemView.getContext().getSystemService(CLIPBOARD_SERVICE);
        String text;
        text = messageText.getText().toString();

        ClipData myClip = ClipData.newPlainText("text", text);
        myClipboard.setPrimaryClip(myClip);

        Toast.makeText(itemView.getContext(), "Text Copied",Toast.LENGTH_SHORT).show();
        return true;


    }

    @Override
    public void onBind(Message message) {
        if(message.getText()!=null){

        }
        if(message.getCreatedAt()!=null){

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

        if( getAdapterPosition()!=0 && getAdapterPosition()==getLayoutPosition()){
            messageTime.setVisibility(View.INVISIBLE);
        }else if(getAdapterPosition()==0 && getAdapterPosition()==getLayoutPosition()){
            messageTime.setVisibility(View.VISIBLE);
        }

        if (messageTime != null) {
            messageTime.setText(format(message.getCreatedAt()));
            }
            if(messageText!=null){
            messageText.setText(message.getText());
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
}
