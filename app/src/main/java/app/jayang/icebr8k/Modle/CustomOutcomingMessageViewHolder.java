package app.jayang.icebr8k.Modle;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;

import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.text.SimpleDateFormat;

import app.jayang.icebr8k.R;



/**
 * Created by yjj781265 on 11/14/2017.
 */

public class CustomOutcomingMessageViewHolder extends MessagesListAdapter.OutcomingMessageViewHolder<Message> {
    public CustomOutcomingMessageViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void onBind(Message message) {
        super.onBind(message);
        if (message.getId()==null) {
            time.setText("Send Text Failed, Check Internet Connection");
            time.setTextColor(itemView.getContext().getColor(android.R.color.holo_red_light));
            time.setTextSize((float) 11);
        }else{
            time.setText(new SimpleDateFormat("hh:mm a").format(message.getCreatedAt()));
            time.setTextSize((float) 11);
            time.setTextColor(itemView.getContext().getColor(android.R.color.secondary_text_dark));
        }
    }



    }



