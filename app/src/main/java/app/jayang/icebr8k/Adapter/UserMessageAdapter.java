package app.jayang.icebr8k.Adapter;


import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import app.jayang.icebr8k.MediaViewActivty;
import app.jayang.icebr8k.Modle.User;
import app.jayang.icebr8k.Modle.UserMessage;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.UserProfilePage;
import app.jayang.icebr8k.Utility.MyDateFormatter;
import app.jayang.icebr8k.Utility.OnLoadMoreListener;

import static app.jayang.icebr8k.MyApplication.getContext;

/**
 * Created by yjj781265 on 2/19/2018.
 */

public class UserMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnTouchListener {
    private ArrayList<UserMessage> mMessages;
    private final int VIEWTYPE_TEXT_IN = 0;
    private final int VIEWTYPE_TEXT_OUT = 1;
    private final int VIEWTYPE_LOAD = 2;
    private final int VIEWTYPE_HEADER = 3;
    private final int VIEWTYPE_VIDEO_IN = 4;
    private final int VIEWTYPE_IMAGE_IN = 5;
    private final int VIEWTYPE_VOICE_IN = 6;
    private final int VIEWTYPE_VIDEO_OUT = 7;
    private final int VIEWTYPE_IMAGE_OUT = 8;
    private final int VIEWTYPE_VOICE_OUT = 9;
    private final int VIEWTYPE_TYPING = 10;
    private final int VIEWTYPE_TEXT_OUT_PENDING = 11;

    private final String VIEWTYPE_TEXT = "text";
    private final String VIEWTYPE_TEXT_PENDING = "text_pend";
    private final String VIEWTYPE_TYPING_STR = "type";

    private final String VIEWTYPE_VIDEO = "video";
    private final String VIEWTYPE_IMAGE = "image";
    private final String VIEWTYPE_VOICE = "voice";

    private final String VIEWTYPE_LOAD_STR = "load";
    private final String VIEWTYPE_HEADER_STR = "header";
    private final String Default_Url = "https://i.imgur.com/zI4v7oF.png";

    private boolean loading;
    private long lastClickTime = 0;
    private User user = null;
    private int lastVisibleItem, totalItemCount;
    private int threshHold = 1;
    private HashMap<String, String> userPhotoUrlMap;
    private OnLoadMoreListener onLoadMoreListener;
    private Activity mContext;


    public UserMessageAdapter(ArrayList<UserMessage> messages, RecyclerView recyclerView, HashMap<String, String> userPhotoUrlMap, Activity context) {
        mMessages = messages;
        this.userPhotoUrlMap = userPhotoUrlMap;
        mContext = context;

        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView
                    .getLayoutManager();

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {


                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    if (onLoadMoreListener != null && !mMessages.isEmpty() && dy < -1) {
                        totalItemCount = linearLayoutManager.getItemCount();
                        lastVisibleItem = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                        if (!loading && totalItemCount <= (lastVisibleItem + threshHold)) {
                            if (onLoadMoreListener != null) {

                                onLoadMoreListener.onLoadMore();
                            }
                            loading = true;
                        }
                    }
                    Log.d("myAdapter", "total" + totalItemCount);
                    Log.d("myAdapter", "last" + lastVisibleItem);
                    Log.d("myAdapter Scroll", "dy " + dy);


                }
            });
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEWTYPE_TEXT_IN) {
            View v = LayoutInflater.from(mContext).inflate(
                    R.layout.item_custom_incoming_message, parent, false);

            return new IncomingTextMessageViewHolder(v);
        } else if (viewType == VIEWTYPE_TEXT_OUT) {
            View v = LayoutInflater.from(mContext).inflate(
                    R.layout.item_custom_outcoming_message, parent, false);

            return new OutcomingTextMessageViewHolder(v);
        } else if (viewType == VIEWTYPE_HEADER) {
            View v = LayoutInflater.from(mContext).inflate(
                    R.layout.dateheader, parent, false);

            return new DateHeaderViewHolder(v);
        } else if (viewType == VIEWTYPE_LOAD) {
            View v = LayoutInflater.from(mContext).inflate(
                    R.layout.loadingmore, parent, false);

            return new LoadMoreViewHolder(v);
        } else if (viewType == VIEWTYPE_TYPING) {
            View v = LayoutInflater.from(mContext).inflate(
                    R.layout.istyping, parent, false);
            return new TypingViewHolder(v);
        } else if (viewType == VIEWTYPE_TEXT_OUT_PENDING) {
            View v = LayoutInflater.from(mContext).inflate(
                    R.layout.item_custom_outcoming_message, parent, false);
            return new OutcomingTextMessageViewHolder(v);
        } else if (viewType == VIEWTYPE_IMAGE_OUT) {
            View v = LayoutInflater.from(mContext).inflate(
                    R.layout.item_image_outcoming_message, parent, false);

            return new OutcomingImageMessageViewHolder(v);
        } else if (viewType == VIEWTYPE_IMAGE_IN) {
            View v = LayoutInflater.from(mContext).inflate(
                    R.layout.item_image_incoming_message, parent, false);

            return new IncomingImageMessageViewHolder(v);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        final UserMessage message = mMessages.get(position);

        // when user swipe top load loadmore
        if (holder instanceof LoadMoreViewHolder) {
            ((LoadMoreViewHolder) holder).mProgressBar.setIndeterminate(true);

            // Incoming message
        } else if (holder instanceof IncomingTextMessageViewHolder) {
            ((IncomingTextMessageViewHolder) holder).text_in.setText(message.getText());

            if (message.getTimestamp() != null) {
                ((IncomingTextMessageViewHolder) holder).date_in.setText(MyDateFormatter.
                        timeStampToDateConverter(message.getTimestamp(), false));
            }
            String url = userPhotoUrlMap.get(message.getSenderid());
            if (url != null && !url.isEmpty()) {
                Glide.with(mContext).load(url)
                        .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.default_avatar3))
                        .into(((IncomingTextMessageViewHolder) holder).avatar_in);
            } else {
                Glide.with(mContext).load(Default_Url)
                        .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.default_avatar3))
                        .into(((IncomingTextMessageViewHolder) holder).avatar_in);
            }
            // Ourcoming  message
        } else if (holder instanceof OutcomingTextMessageViewHolder) {

            ((OutcomingTextMessageViewHolder) holder).text_out.setText(message.getText());
            ((OutcomingTextMessageViewHolder) holder).date_out.setVisibility(View.VISIBLE);
            if (message.getMessagetype().equals(VIEWTYPE_TEXT_PENDING)) {
                ((OutcomingTextMessageViewHolder) holder).date_out.setText("Sending...");
            } else {
                if (message.getTimestamp() != null) {
                    ((OutcomingTextMessageViewHolder) holder).date_out.setText(MyDateFormatter.
                            timeStampToDateConverter(message.getTimestamp(), false));
                }
            }


            //update avatar image
            String url = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString();
            if (url != null && !url.isEmpty()) {
                Glide.with(mContext).load(url)
                        .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.default_avatar3)).transition(DrawableTransitionOptions.withCrossFade(300))
                        .into(((OutcomingTextMessageViewHolder) holder).avatar_out);
            } else {
                Glide.with(mContext).load(Default_Url)
                        .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.default_avatar3)).transition(DrawableTransitionOptions.withCrossFade(300))
                        .into(((OutcomingTextMessageViewHolder) holder).avatar_out);
            }
            // istyping  VH
        } else if (holder instanceof TypingViewHolder) {
            //set image for avatar
            String url = userPhotoUrlMap.get(message.getSenderid());
            if (url != null && !url.isEmpty()) {
                Glide.with(mContext).load(url)
                        .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.default_avatar3)).transition(DrawableTransitionOptions.withCrossFade(300))
                        .into(((TypingViewHolder) holder).avatar);
            } else {
                Glide.with(mContext).load(Default_Url)
                        .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.default_avatar3)).transition(DrawableTransitionOptions.withCrossFade(300))
                        .into(((TypingViewHolder) holder).avatar);
            }

            // istyping  VH
        } else if (holder instanceof DateHeaderViewHolder) {
            if (!mMessages.isEmpty()) {
                if (message.getTimestamp() != null) {
                    ((DateHeaderViewHolder) holder).dateHeader.setText(MyDateFormatter.timeStampToDateConverter(message.getTimestamp(), true));
                }
            }

            // outcoming image message
        } else if (holder instanceof OutcomingImageMessageViewHolder) {
            String url = userPhotoUrlMap.get(message.getSenderid());
            if (url != null && !url.isEmpty()) {
                Glide.with(mContext).load(url)
                        .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.default_avatar3)).transition(DrawableTransitionOptions.withCrossFade(300))
                        .into(((OutcomingImageMessageViewHolder) holder).avatar);
            } else {
                Glide.with(mContext).load(Default_Url)
                        .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.default_avatar3)).transition(DrawableTransitionOptions.withCrossFade(300))
                        .into(((OutcomingImageMessageViewHolder) holder).avatar);
            }


            // if image is uploading to the cloud show text is sending
            ((OutcomingImageMessageViewHolder) holder).timeStamp.setText(message.getDoneNetWorking() ? MyDateFormatter.timeStampToDateConverter(message.getTimestamp(), false)
                    : "Sending...");
            ((OutcomingImageMessageViewHolder) holder).timeStamp.setVisibility(View.VISIBLE);

              DrawableCrossFadeFactory factory =
                        new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();
                Glide.with(mContext).load(message.getText()).
                        apply(new RequestOptions()
                                .error(R.drawable.noimage)
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                .placeholder(R.drawable.img_placeholder)
                                .transform(new RoundedCorners(24))
                                .override(500, 500))
                        .into(((OutcomingImageMessageViewHolder) holder).image);



            // incoming image message
        } else if (holder instanceof IncomingImageMessageViewHolder) {
            String url = userPhotoUrlMap.get(message.getSenderid());
            if (url != null && !url.isEmpty()) {
                Glide.with(mContext).load(url)
                        .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.default_avatar3)).transition(DrawableTransitionOptions.withCrossFade(300))
                        .into(((IncomingImageMessageViewHolder) holder).avatar);
            } else {
                Glide.with(mContext).load(Default_Url)
                        .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.default_avatar3)).transition(DrawableTransitionOptions.withCrossFade(300))
                        .into(((IncomingImageMessageViewHolder) holder).avatar);
            }


            ((IncomingImageMessageViewHolder) holder).timeStamp.setText(MyDateFormatter.timeStampToDateConverter(message.getTimestamp(), false));

            ((IncomingImageMessageViewHolder) holder).timeStamp.setVisibility(View.VISIBLE);

            DrawableCrossFadeFactory factory =
                    new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();
            Glide.with(mContext).load(message.getText()).
                    apply(new RequestOptions()
                            .error(R.drawable.noimage)
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .placeholder(R.drawable.img_placeholder)
                            .transform(new RoundedCorners(24))
                            .override(500, 500))
                    .into(((IncomingImageMessageViewHolder) holder).image);



        }


    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    @Override
    public long getItemId(int position) {
        return mMessages.get(position).getMessageid().hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        String messageType = mMessages.get(position).getMessagetype();
        int viewType;
        boolean income;
        // check is income or outcome message
        if (!FirebaseAuth.getInstance().getCurrentUser().getUid().
                equals(mMessages.get(position).getSenderid())) {
            income = true;
        } else {
            income = false;
        }
        if (income && VIEWTYPE_TEXT.equals(messageType)) {
            viewType = VIEWTYPE_TEXT_IN;
            return viewType;
        } else if (!income && VIEWTYPE_TEXT.equals(messageType)) {
            viewType = VIEWTYPE_TEXT_OUT;
            return viewType;
        } else if (!income && VIEWTYPE_IMAGE.equals(messageType)) {
            viewType = VIEWTYPE_IMAGE_OUT;
            return viewType;
        } else if (income && VIEWTYPE_IMAGE.equals(messageType)) {
            viewType = VIEWTYPE_IMAGE_IN;
            return viewType;
        }
        // add more view type here//////////////////////////

        switch (messageType) {
            case VIEWTYPE_HEADER_STR:
                viewType = VIEWTYPE_HEADER;
                break;
            case VIEWTYPE_LOAD_STR:
                viewType = VIEWTYPE_LOAD;
                break;
            case VIEWTYPE_TYPING_STR:
                viewType = VIEWTYPE_TYPING;
                break;
            case VIEWTYPE_TEXT_PENDING:
                viewType = VIEWTYPE_TEXT_OUT_PENDING;
                break;
            default:
                viewType = VIEWTYPE_HEADER;
                break;
        }

        return viewType;


    }




    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public void setLoaded() {
        loading = false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                v.setAlpha(0.8f);
                break;
            case MotionEvent.ACTION_UP:
                v.setAlpha(1f);
            default :
                v.setAlpha(1f);
        }
        return false;

    }


    //incoming text message
    public class IncomingTextMessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener {
        private ImageView avatar_in;
        private LinearLayout text_container_in;
        private TextView text_in, date_in;

        public IncomingTextMessageViewHolder(View itemView) {
            super(itemView);
            avatar_in = itemView.findViewById(R.id.incoming_avatar);
            date_in = itemView.findViewById(R.id.incoming_time);
            text_in = itemView.findViewById(R.id.incoming_text);
            text_container_in = itemView.findViewById(R.id.incoming_main);
            text_container_in.setOnLongClickListener(this);
            avatar_in.setOnClickListener(this);
            avatar_in.setOnTouchListener(UserMessageAdapter.this);
        }

        @Override
        public void onClick(View view) {
            if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                return;
            }

            lastClickTime = SystemClock.elapsedRealtime();
            if (view.getId() == text_container_in.getId()) {
                // Toast.makeText(view.mContext, "clicked", Toast.LENGTH_SHORT).show();
            } else if (view.equals(avatar_in)) {
                int postion = getAdapterPosition();
                if (postion != RecyclerView.NO_POSITION) {
                    UserMessage message = mMessages.get(postion);
                    toUserProfilePage(message);
                }
            }

        }

        @Override
        public boolean onLongClick(View v) {
            int postion = getAdapterPosition();
            if (postion != RecyclerView.NO_POSITION) {
                UserMessage message = mMessages.get(postion);
                ClipboardManager clipboard = (ClipboardManager)  mContext.getSystemService (Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("IceBr8k", message.getText());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(mContext, "Text Copied ", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
    }




    //outcoming text message
    public class OutcomingTextMessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener {
        private ImageView avatar_out;
        private TextView text_out, date_out, status;
        private LinearLayout text_container_out;

        public OutcomingTextMessageViewHolder(View itemView) {
            super(itemView);
            avatar_out = itemView.findViewById(R.id.outcoming_avatar);
            date_out = itemView.findViewById(R.id.outcoming_time);
            text_out = itemView.findViewById(R.id.outcoming_text);
            text_container_out = itemView.findViewById(R.id.outcoming_main);
            text_container_out.setOnLongClickListener(this);
            avatar_out.setOnClickListener(this);
            avatar_out.setOnTouchListener(UserMessageAdapter.this);
        }

        @Override
        public void onClick(View view) {
            if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                return;
            }
            lastClickTime = SystemClock.elapsedRealtime();

          if (view.equals(avatar_out)) {
                int postion = getAdapterPosition();
                if (postion != RecyclerView.NO_POSITION) {
                    UserMessage message = mMessages.get(postion);
                    toUserProfilePage(message);
                }
            }

        }

        @Override
        public boolean onLongClick(View v) {
            int postion = getAdapterPosition();
            if (postion != RecyclerView.NO_POSITION) {
                UserMessage message = mMessages.get(postion);
                ClipboardManager clipboard = (ClipboardManager)  mContext.getSystemService (Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("IceBr8k", message.getText());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(mContext, "Text Copied", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
    }

    //outcoming text pending message
    public class OutcomingTextPendingMessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView avatar_out;
        private TextView text_out, date_out, status;
        private LinearLayout text_container_out;

        public OutcomingTextPendingMessageViewHolder(View itemView) {
            super(itemView);
            avatar_out = itemView.findViewById(R.id.outcoming_avatar);
            date_out = itemView.findViewById(R.id.outcoming_time);
            text_out = itemView.findViewById(R.id.outcoming_text);
            text_container_out = itemView.findViewById(R.id.outcoming_main);
            text_container_out.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            if (view.getId() == text_container_out.getId()) {
                //Toast.makeText(view.mContext, "clicked", Toast.LENGTH_SHORT).show();
            }

        }
    }

    public class OutcomingImageMessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView image;
        TextView timeStamp;
        ImageView avatar;

        public OutcomingImageMessageViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.outcoming_image_imageView);


            timeStamp = itemView.findViewById(R.id.outcoming_image_time);
            avatar = itemView.findViewById(R.id.outcoming_image_avatar);
            avatar.setOnClickListener(this);
            avatar.setOnTouchListener(UserMessageAdapter.this);
            image.setOnTouchListener(UserMessageAdapter.this);
            image.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                return;
            }
            lastClickTime = SystemClock.elapsedRealtime();

            if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                UserMessage message = mMessages.get(getAdapterPosition());

                if (v == image) {
                    if (message.getDoneNetWorking()) {
                        toMediaGallary(message, v);
                    }
                } else if (v == avatar) {
                    toUserProfilePage(message);
                }

            }
        }
    }

    public class IncomingImageMessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView image;
        TextView timeStamp;
        ImageView avatar;

        public IncomingImageMessageViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.incoming_image_imageView);
            timeStamp = itemView.findViewById(R.id.outcoming_image_time);
            avatar = itemView.findViewById(R.id.outcoming_image_avatar);
            avatar.setOnTouchListener(UserMessageAdapter.this);
            image.setOnTouchListener(UserMessageAdapter.this);
            avatar.setOnClickListener(this);
            image.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            if (SystemClock.elapsedRealtime() - lastClickTime < 1000){
                return;
            }
            lastClickTime = SystemClock.elapsedRealtime();
            if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                UserMessage message = mMessages.get(getAdapterPosition());

                if (v == image) {
                    if (message.getDoneNetWorking()) {
                        toMediaGallary(message,v);

                    }

                } else if (v == avatar) {
                    toUserProfilePage(message);

                }
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
            mProgressBar = itemView.findViewById(R.id.loadingmore);
        }
    }

    public class TypingViewHolder extends RecyclerView.ViewHolder {
        private ImageView avatar;

        public TypingViewHolder(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.isTyping_avatar);


        }
    }

    private void toUserProfilePage(UserMessage message) {
        final String uid = message.getSenderid();
        DatabaseReference infoRef = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(uid);
        infoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User mUser = dataSnapshot.getValue(User.class);
                Intent intent = new Intent(mContext, UserProfilePage.class);
                intent.putExtra("userInfo", mUser);
                intent.putExtra("userUid", uid);
                mContext.startActivity(intent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void toMediaGallary(UserMessage message,View view){
        ArrayList<UserMessage> imgMessages = new ArrayList<>();

        for(UserMessage message1 : mMessages) {
            if (message1.getMessagetype().equals(VIEWTYPE_IMAGE)) {
                imgMessages.add(message1);
            }
        }
        Collections.sort(imgMessages);
        Intent intent = new Intent(mContext,MediaViewActivty.class);
        intent.putExtra("photoViews",imgMessages);
        intent.putExtra("photoView",message);
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(mContext,view, "photoView");
        if(message.getGif()){
            mContext.startActivity(intent);
        }else{
            mContext.startActivity (intent, options.toBundle());
        }




    }

    }



