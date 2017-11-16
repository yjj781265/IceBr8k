package app.jayang.icebr8k.Modle;

import android.view.View;

import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.text.SimpleDateFormat;

/**
 * Created by yjj781265 on 11/14/2017.
 */

public class CustomOutcomingMessageViewHolder extends MessagesListAdapter.OutcomingMessageViewHolder<Message>{
    public CustomOutcomingMessageViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void onBind(Message message) {
        super.onBind(message);
        time.setText(new SimpleDateFormat("hh:mm a").format(message.getCreatedAt()));
        time.setTextSize((float)11);
    }
}
