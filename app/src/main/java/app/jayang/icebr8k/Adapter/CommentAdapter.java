package app.jayang.icebr8k.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import app.jayang.icebr8k.Fragments.CommentBottomSheetFrag;
import app.jayang.icebr8k.Modle.Comment;
import app.jayang.icebr8k.Modle.User;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.Reply;
import app.jayang.icebr8k.UserProfilePage;
import app.jayang.icebr8k.Utility.MyDateFormatter;
import app.jayang.icebr8k.Utility.OnLoadMoreListener;

public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String VIEWTYPE_TEXT_STR = "text";
    private  final int VIEWTYPE_TEXT =0;
    private final String VIEWTYPE_LOAD_STR = "load";
    private  final int VIEWTYPE_LOAD =1;
    private final String VIEWTYPE_INPUT__STR = "input";
    private  final int VIEWTYPE_INPUT =2;



    private ArrayList<Comment> mComments;
    private boolean loading;
    private int lastVisibleItem, totalItemCount;
    private Context mContext;
    private String questionId;
    private android.support.v4.app.FragmentManager mFragmentManager;
    private int threshHold =1;
    private final FirebaseUser currentuser = FirebaseAuth.getInstance().getCurrentUser();
    private OnLoadMoreListener onLoadMoreListener;


    public CommentAdapter(ArrayList<Comment> comments, String mQuestionId, RecyclerView recyclerView, Context context, android.support.v4.app.FragmentManager fragmentManager) {
       mComments = comments;
        mContext = context;
        this.questionId = mQuestionId;
        mFragmentManager = fragmentManager;
        if(recyclerView.getLayoutManager() instanceof LinearLayoutManager){
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView
                    .getLayoutManager();

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {


                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);


                    if(onLoadMoreListener!=null &&!mComments.isEmpty() && dy>1) {
                        totalItemCount = linearLayoutManager.getItemCount();
                        lastVisibleItem = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                        Log.d("commentAdapter",""+loading);
                        Log.d("commentAdapter",""+lastVisibleItem + " total"+totalItemCount);
                        if (!loading && totalItemCount <= (lastVisibleItem+threshHold)) {
                            if (onLoadMoreListener != null) {

                                onLoadMoreListener.onLoadMore();
                                //Toast.makeText(mContext, "loading more", Toast.LENGTH_SHORT).show();
                            }
                           // loading = true;
                        }
                    }







                }
            });
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        switch (viewType){
            case VIEWTYPE_TEXT :
                v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_comment, parent, false);
                return new CommentViewHolder(v);

            case VIEWTYPE_LOAD :
                 v = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.loadingmore, parent, false);
                return new LoadMoreViewHolder(v);

            case VIEWTYPE_INPUT :
                v = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_comment_input, parent, false);
                return new CommentInputViewHolder(v);
        }

       return  null;

    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof CommentViewHolder){
            Comment comment = mComments.get(position);

            ((CommentViewHolder) holder).text.setText(comment.getText());
            ((CommentViewHolder) holder).reply.setText(comment.getReply()!=null&& comment.getReply()>0? comment.getReply().toString() :"");
             ((CommentViewHolder) holder).answer.setVisibility(comment.getAnswer()==null ? View.GONE:View.VISIBLE);
            ((CommentViewHolder) holder).answer.setText( comment.getAnswer());
            ((CommentViewHolder) holder).timestamp.setText(MyDateFormatter.commentTImeConverter(comment.getTimestamp()));
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
                            .into(((CommentViewHolder) holder).avatar);
                    ((CommentViewHolder) holder).name.setText(user.getDisplayname());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });




    }else  if(holder instanceof CommentAdapter.LoadMoreViewHolder){
            ((LoadMoreViewHolder) holder).mProgressBar.setIndeterminate(true);

        }else  if(holder instanceof CommentInputViewHolder){
            ((CommentInputViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    CommentBottomSheetFrag commentBottomSheetFrag =  CommentBottomSheetFrag.newInstance(questionId);
                    commentBottomSheetFrag.show(mFragmentManager,"commentBts");

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

            case VIEWTYPE_INPUT__STR: viewType =VIEWTYPE_INPUT;
                break;
            default: viewType = VIEWTYPE_TEXT;
                break;
        }
        return viewType;
    }

    class CommentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView avatar;
        private LinearLayout replyBubbleLayout;
        private TextView name, text,answer,timestamp,reply;

        public CommentViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            avatar = itemView.findViewById(R.id.comment_item_avatar);
            replyBubbleLayout = itemView.findViewById(R.id.item_comment_reply_layout);
            replyBubbleLayout.setOnClickListener(this);
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

        @Override
        public void onClick(View v) {
            if(getAdapterPosition()!= RecyclerView.NO_POSITION){
                Comment comment = mComments.get(getAdapterPosition());

                Intent intent = new Intent(mContext, Reply.class);
                intent.putExtra("questionId",questionId);
                intent.putExtra("topCommentId",comment.getId());
                intent.putExtra("title",name.getText().toString());
                mContext.startActivity(intent);

            }
        }
    }


    // load more progress bar
    public  class LoadMoreViewHolder extends RecyclerView.ViewHolder {
        private ProgressBar mProgressBar;
        public LoadMoreViewHolder(View itemView) {
            super(itemView);
            mProgressBar =itemView.findViewById(R.id.loadingmore);
        }
    }

    // input layout
    public  class CommentInputViewHolder extends RecyclerView.ViewHolder  {
        ImageView avatar;

        public CommentInputViewHolder(View itemView) {
            super(itemView);
            avatar =itemView.findViewById(R.id.comment_avatar);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(currentuser.getUid())
                    .child("photourl");

            // set avatar
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String url = dataSnapshot.getValue(String.class);
                    Glide.with(mContext).load(url).apply(RequestOptions.circleCropTransform().placeholder(R.drawable.default_avatar3)).transition(DrawableTransitionOptions.withCrossFade(300))
                            .into(avatar);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


    }



    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }
    public void setLoaded() {
        loading = false;
    }
}
