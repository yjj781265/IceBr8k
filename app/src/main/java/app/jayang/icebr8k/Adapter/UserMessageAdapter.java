package app.jayang.icebr8k.Adapter;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import app.jayang.icebr8k.Modle.MyDateFormatter;
import app.jayang.icebr8k.Modle.OnLoadMoreListener;
import app.jayang.icebr8k.Modle.UserMessage;
import app.jayang.icebr8k.R;

import static app.jayang.icebr8k.MyApplication.getContext;

/**
 * Created by yjj781265 on 2/19/2018.
 */

public class UserMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnTouchListener {
    private ArrayList<UserMessage> mMessages;
    private final int VIEWTYPE_TEXT_IN = 0;
    private final int VIEWTYPE_TEXT_OUT = 1;
    private final int VIEWTYPE_LOAD = 2;
    private final int VIEWTYPE_DATE = 3;
    private final int VIEWTYPE_VIDEO_IN = 4;
    private final int VIEWTYPE_IMAGE_IN= 5;
    private final int VIEWTYPE_VOICE_IN = 6;
    private final int VIEWTYPE_VIDEO_OUT = 7;
    private final int VIEWTYPE_IMAGE_OUT= 8;
    private final int VIEWTYPE_VOICE_OUT = 9;
    private final int VIEWTYPE_TYPING = 10;
    private final int VIEWTYPE_TEXT_OUT_PENDING = 11;

    private final String VIEWTYPE_TEXT = "text";
    private final String VIEWTYPE_TEXT_PENDING = "text_pend";
    private final String VIEWTYPE_TYPING_STR = "type";

    private final String VIEWTYPE_VIDEO = "video";
    private final String VIEWTYPE_IMAGE= "image";
    private final String VIEWTYPE_VOICE = "voice";

    private final String VIEWTYPE_LOAD_STR = "load";
    private final String VIEWTYPE_DATE_STR = "date";

    private boolean loading;
    private int lastVisibleItem, totalItemCount;
    private int threshHold =1;
    private OnLoadMoreListener onLoadMoreListener;

    public UserMessageAdapter(ArrayList<UserMessage> messages,RecyclerView recyclerView) {
        mMessages = messages;

        if(recyclerView.getLayoutManager() instanceof LinearLayoutManager){
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView
                    .getLayoutManager();

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {


                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    if(onLoadMoreListener!=null &&!mMessages.isEmpty() && dy<=0) {
                        totalItemCount = linearLayoutManager.getItemCount();
                        lastVisibleItem = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                        if (!loading && totalItemCount <= (lastVisibleItem+threshHold)) {
                            if (onLoadMoreListener != null) {
                                onLoadMoreListener.onLoadMore();
                            }
                            loading = true;
                        }
                    }
                    Log.d("myAdapter","total"+totalItemCount);
                    Log.d("myAdapter","last"+lastVisibleItem);







                }
            });
        }
        }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEWTYPE_TEXT_IN) {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_custom_incoming_message, parent, false);

           return new IncomingTextMessageViewHolder(v);
        } else if(viewType == VIEWTYPE_TEXT_OUT) {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_custom_outcoming_message, parent, false);

            return new OutcomingTextMessageViewHolder(v);
        }else if(viewType == VIEWTYPE_DATE){
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.dateheader, parent, false);

            return new DateHeaderViewHolder(v);
        }else if(viewType == VIEWTYPE_LOAD){
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.loadingmore, parent, false);

            return new LoadMoreViewHolder(v);
        }else if(viewType ==VIEWTYPE_TYPING){
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.istyping, parent, false);
            return new TypingViewHolder(v);
        }else if(viewType ==VIEWTYPE_TEXT_OUT_PENDING) {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.istyping, parent, false);
            return new TypingViewHolder(v);
        }
        return  null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof LoadMoreViewHolder){
            ((LoadMoreViewHolder) holder).mProgressBar.setIndeterminate(true);

        }else if(holder instanceof IncomingTextMessageViewHolder){
            UserMessage message = mMessages.get(position);

            ((IncomingTextMessageViewHolder) holder).text_in.setText(message.getText());
            ((IncomingTextMessageViewHolder) holder).avatar_in.setOnTouchListener(this);
            if(message.getTimestamp()!=null) {
                ((IncomingTextMessageViewHolder) holder).date_in.setText(MyDateFormatter.
                        timeStampToDateConverter(message.getTimestamp(), false));
            }

        }else if(holder instanceof OutcomingTextMessageViewHolder) {
            UserMessage message = mMessages.get(position);
            ((OutcomingTextMessageViewHolder) holder).text_out.setText(message.getText());
            ((OutcomingTextMessageViewHolder) holder).date_out.setVisibility(View.VISIBLE);
            ((OutcomingTextMessageViewHolder) holder).status.setVisibility(View.GONE);
            ((OutcomingTextMessageViewHolder) holder).avatar_out.setOnTouchListener(this);

            if (message.getTimestamp() != null) {
                ((OutcomingTextMessageViewHolder) holder).date_out.setText(MyDateFormatter.
                        timeStampToDateConverter(message.getTimestamp(), false));
            }
        }else if(holder instanceof OutcomingTextPendingMessageViewHolder){
                UserMessage message = mMessages.get(position);
                ((OutcomingTextPendingMessageViewHolder) holder).text_out.setText(message.getText());
             ((OutcomingTextPendingMessageViewHolder) holder).date_out.setVisibility(View.GONE);
            ((OutcomingTextPendingMessageViewHolder) holder).status.setVisibility(View.VISIBLE);
            ((OutcomingTextPendingMessageViewHolder) holder).avatar_out.setOnTouchListener(this);
            ((OutcomingTextPendingMessageViewHolder) holder).status.setText("Sending...");

        }else if(holder instanceof TypingViewHolder){
            Glide.with(getContext()).load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(((TypingViewHolder) holder).avatar);
        }else if(holder instanceof DateHeaderViewHolder){
            if(!mMessages.isEmpty() ) {
                UserMessage message = mMessages.get(position);
                if(message.getTimestamp()!=null) {
                    ((DateHeaderViewHolder) holder).dateHeader.setText(MyDateFormatter.timeStampToDateConverter(message.getTimestamp(), true));
                }
            }
        }


    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        String messageType = mMessages.get(position).getMessageType();
        int viewType;
        boolean income;
        // check is income or outcome message
        if(!FirebaseAuth.getInstance().getCurrentUser().getUid().
                equals(mMessages.get(position).getSenderId())){
           income =true;
        }else{
            income =false;
        }
        if(income &&VIEWTYPE_TEXT.equals(messageType) ){
            viewType = VIEWTYPE_TEXT_IN;
            return viewType;
        }else if(!income &&VIEWTYPE_TEXT.equals(messageType)){
            viewType = VIEWTYPE_TEXT_OUT;
            return viewType;
        }
        // add more view type here//////////////////////////

        switch (messageType){
            case VIEWTYPE_DATE_STR: viewType =VIEWTYPE_DATE;
            break;
            case VIEWTYPE_LOAD_STR: viewType =VIEWTYPE_LOAD;
                break;
            case VIEWTYPE_TYPING_STR: viewType =VIEWTYPE_TYPING;
                break;
            case VIEWTYPE_TEXT_PENDING: viewType =VIEWTYPE_TEXT_OUT_PENDING;
                break;
            default: viewType = VIEWTYPE_DATE;
                break;
        }

        return viewType;



    }

    @Override
    public long getItemId(int position) {
        return mMessages.get(position).getMessageId().hashCode();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }
    public void setLoaded() {
        loading = false;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN)
        {
            view.setAlpha(0.6f);
        } else {
            view.setAlpha(1f);
        }

        return true;

    }

    //incoming text message
    public class IncomingTextMessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView avatar_in;
        private LinearLayout text_container_in;
        private TextView text_in,date_in;
        public IncomingTextMessageViewHolder(View itemView) {
            super(itemView);
            avatar_in = itemView.findViewById(R.id.incoming_avatar);
            date_in = itemView.findViewById(R.id.incoming_time);
            text_in = itemView.findViewById(R.id.incoming_text);
            text_container_in = itemView.findViewById(R.id.incoming_main);
            text_container_in.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(view.getId() == text_container_in.getId()){
                Toast.makeText(view.getContext(), "clicked", Toast.LENGTH_SHORT).show();
            }

        }
    }

    //outcoming text message
    public class OutcomingTextMessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView avatar_out;
        private TextView text_out,date_out,status;
        private LinearLayout text_container_out;

        public OutcomingTextMessageViewHolder(View itemView) {
            super(itemView);
            avatar_out = itemView.findViewById(R.id.outcoming_avatar);
            date_out = itemView.findViewById(R.id.outcoming_time);
            text_out = itemView.findViewById(R.id.outcoming_text);
            status = itemView.findViewById(R.id.outcoming_status);
            text_container_out = itemView.findViewById(R.id.outcoming_main);
            text_container_out.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(view.getId() == text_container_out.getId()){
                Toast.makeText(view.getContext(), "clicked", Toast.LENGTH_SHORT).show();
            }

        }
    }

    //outcoming text pending message
    public class OutcomingTextPendingMessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView avatar_out;
        private TextView text_out,date_out,status;
        private LinearLayout text_container_out;

        public OutcomingTextPendingMessageViewHolder(View itemView) {
            super(itemView);
            avatar_out = itemView.findViewById(R.id.outcoming_avatar);
            date_out = itemView.findViewById(R.id.outcoming_time);
            text_out = itemView.findViewById(R.id.outcoming_text);
            status = itemView.findViewById(R.id.outcoming_status);
            text_container_out = itemView.findViewById(R.id.outcoming_main);
            text_container_out.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(view.getId() == text_container_out.getId()){
                Toast.makeText(view.getContext(), "clicked", Toast.LENGTH_SHORT).show();
            }

        }
    }

    //Date Header
    public class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView dateHeader;
        public DateHeaderViewHolder(View itemView) {
            super(itemView);
            dateHeader = itemView.findViewById(R.id.dateHeader_text);
        }
    }
 // load more progress bar
    public class LoadMoreViewHolder extends RecyclerView.ViewHolder {
        private ProgressBar mProgressBar;
        public LoadMoreViewHolder(View itemView) {
            super(itemView);
            mProgressBar =itemView.findViewById(R.id.loadingmore);
        }
    }

    public class TypingViewHolder extends RecyclerView.ViewHolder {
        private ImageView avatar;
        public TypingViewHolder(View itemView) {
            super(itemView);
            avatar =itemView.findViewById(R.id.isTyping_avatar);


        }
    }




}
