package app.jayang.icebr8k.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import app.jayang.icebr8k.Model.Comment;
import app.jayang.icebr8k.Model.User;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.UserProfilePage;
import app.jayang.icebr8k.Utility.MyDateFormatter;

public class ReplyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    private ArrayList<Comment> mComments;
    private Context mContext;
    private RecyclerView mRecyclerView;
    private replyClickedListener mReplyClickedListener ;

    private final String VIEWTYPE_TEXT_STR = "text";
    private  final int VIEWTYPE_TEXT =0;
    private final String VIEWTYPE_LOAD_STR = "load";
    private  final int VIEWTYPE_LOAD =1;

    private final String VIEWTYPE_TOP_STR = "top";
    private  final int VIEWTYPE_TOP =2;

    public ReplyAdapter(ArrayList<Comment> comments, Context context, RecyclerView recyclerView) {
        mComments = comments;
        mContext = context;
        mReplyClickedListener = (replyClickedListener) mContext;
        mRecyclerView = recyclerView;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        switch (viewType){
            case VIEWTYPE_TEXT :
                v = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_comment, parent, false);
                return new ReplyAdapter.CommentViewHolder(v);

            case VIEWTYPE_LOAD :
                v = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.loadingmore, parent, false);
                return new ReplyAdapter.LoadMoreViewHolder(v);

            case VIEWTYPE_TOP :
                v = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_reply_top, parent, false);
                return new ReplyAdapter.ReplyTopViewHolder(v);
        }
        return  null;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {

        Comment comment = mComments.get(position);
        if(holder instanceof ReplyAdapter.CommentViewHolder){


            ((ReplyAdapter.CommentViewHolder) holder).text.setText(comment.getText());
            ((ReplyAdapter.CommentViewHolder) holder).reply.setText(comment.getReply()!=null? comment.getReply().toString() :"");
            ((ReplyAdapter.CommentViewHolder) holder).answer.setVisibility(comment.getAnswer()==null ? View.GONE:View.VISIBLE);
            ((ReplyAdapter.CommentViewHolder) holder).answer.setText( comment.getAnswer());
            ((ReplyAdapter.CommentViewHolder) holder).timestamp.setText(MyDateFormatter.commentTImeConverter(comment.getTimestamp()));
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(comment.getSenderId())
                    ;

            // set avatar and name
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    Glide.with(mContext).load(user.getPhotourl()).apply(RequestOptions.circleCropTransform().placeholder(R.drawable.default_avatar3)).transition(DrawableTransitionOptions.withCrossFade(300))
                            .into(((ReplyAdapter.CommentViewHolder) holder).avatar);
                    ((ReplyAdapter.CommentViewHolder) holder).name.setText(user.getDisplayname());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });




        }else  if(holder instanceof ReplyAdapter.LoadMoreViewHolder){
            ((ReplyAdapter.LoadMoreViewHolder) holder).mProgressBar.setIndeterminate(true);

        }else  if(holder instanceof ReplyAdapter.ReplyTopViewHolder){
            ((ReplyAdapter.ReplyTopViewHolder) holder).text.setText(comment.getText());
            String reply = comment.getReply()>1 ? " Replies" : " Reply";
            ((ReplyAdapter.ReplyTopViewHolder) holder).reply.setText(comment.getReply()!=null?  comment.getReply().toString()+reply :"");
            ((ReplyAdapter.ReplyTopViewHolder) holder).reply.setVisibility(comment.getReply()!=null &&comment.getReply()>0 ? View.VISIBLE :View.GONE);
            ((ReplyAdapter.ReplyTopViewHolder) holder).answer.setVisibility(comment.getAnswer()==null ? View.GONE:View.VISIBLE);
            ((ReplyAdapter.ReplyTopViewHolder) holder).answer.setText( comment.getAnswer());
            ((ReplyAdapter.ReplyTopViewHolder) holder).timestamp.setText(MyDateFormatter.commentTImeConverter(comment.getTimestamp()));
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(comment.getSenderId())
                    ;

            // set avatar and name
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    Glide.with(mContext).load(user.getPhotourl()).apply(RequestOptions.circleCropTransform().placeholder(R.drawable.default_avatar3)).transition(DrawableTransitionOptions.withCrossFade(300))
                            .into(((ReplyAdapter.ReplyTopViewHolder) holder).avatar);
                    ((ReplyAdapter.ReplyTopViewHolder) holder).name.setText(user.getDisplayname());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    @Override
    public int getItemViewType(int position) {
        String messageType = mComments.get(position).getType();
        int viewType;

        switch (messageType){

            case VIEWTYPE_LOAD_STR: viewType =VIEWTYPE_LOAD;
                break;

            case VIEWTYPE_TEXT_STR: viewType =VIEWTYPE_TEXT;
                break;

            case VIEWTYPE_TOP_STR: viewType =VIEWTYPE_TOP;
                break;
            default: viewType = VIEWTYPE_TEXT;
                break;
        }
        return viewType;
    }

    class ReplyTopViewHolder extends RecyclerView.ViewHolder {
        private TextView name, answer,text,reply,timestamp;
        private ImageView avatar;
        public ReplyTopViewHolder(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.reply_item_avatar);
            reply = itemView.findViewById(R.id.replyNumTxt);
            name = itemView.findViewById(R.id.reply_item_name);
            text = itemView.findViewById(R.id.reply_item_text);
            answer = itemView.findViewById(R.id.reply_item_answer);
            timestamp = itemView.findViewById(R.id.reply_item_timestamp);
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


    class CommentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView avatar;
        private TextView name, text,answer,timestamp,reply;

        public CommentViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            avatar = itemView.findViewById(R.id.comment_item_avatar);
            reply = itemView.findViewById(R.id.commentNum);
            name = itemView.findViewById(R.id.comment_item_name);
            text = itemView.findViewById(R.id.comment_item_text);
            answer = itemView.findViewById(R.id.comment_item_answer);
            timestamp = itemView.findViewById(R.id.comment_item_timestamp);

            avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(getAdapterPosition()!= RecyclerView.NO_POSITION){
                        final Comment comment = mComments.get(getAdapterPosition());

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                                .child("Users")
                                .child(comment.getSenderId());
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                User user = dataSnapshot.getValue(User.class);
                                Intent intent = new Intent(mContext, UserProfilePage.class);
                                intent.putExtra("userInfo",user);
                                intent.putExtra("userUid",comment.getSenderId());
                                mContext.startActivity(intent);

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }

                }
            });
        }

    //itemview CLick
        @Override
        public void onClick(View v) {
            if(getAdapterPosition()!= RecyclerView.NO_POSITION) {
                Comment comment = mComments.get(getAdapterPosition());
                String str = "Reply @"+ name.getText()+": ";
                mReplyClickedListener .replyClicked(str,comment.getSenderId(),comment.getId());

            }



        }
    }
    public interface replyClickedListener{
       void replyClicked(String text,String commentAuthorId,String commentId);

    }

}
