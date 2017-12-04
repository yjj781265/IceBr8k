package app.jayang.icebr8k.Modle;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;

import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.text.SimpleDateFormat;

import app.jayang.icebr8k.MainChatActivity;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by yjj781265 on 11/14/2017.
 */

public class CustomMessageViewHolder extends MessagesListAdapter.IncomingMessageViewHolder<Message> {
    public CustomMessageViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void onBind(Message message) {
        super.onBind(message);
        time.setText(new SimpleDateFormat("hh:mm a").format(message.getCreatedAt()));
        time.setTextColor(itemView.getContext().getColor(android.R.color.primary_text_light));
        time.setAlpha((float) 0.5);
        time.setTextSize((float) 11);


    }

    }



