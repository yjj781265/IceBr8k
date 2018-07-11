package app.jayang.icebr8k.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import app.jayang.icebr8k.Modle.Comment;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.Utility.MyDateFormatter;
import app.jayang.icebr8k.Utility.OnLoadMoreListener;

public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String VIEWTYPE_TEXT_STR = "text";
    private  final int VIEWTYPE_TEXT =0;
    private final String VIEWTYPE_LOAD_STR = "load";
    private  final int VIEWTYPE_LOAD =1;


    private ArrayList<Comment> comments;
    private boolean loading;
    private int lastVisibleItem, totalItemCount;
    private Context mContext;
    private int threshHold =1;
    private OnLoadMoreListener onLoadMoreListener;

    public CommentAdapter(final ArrayList<Comment> comments, RecyclerView recyclerView, Context context) {
        this.comments = comments;
        mContext = context;
        if(recyclerView.getLayoutManager() instanceof LinearLayoutManager){
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView
                    .getLayoutManager();

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {


                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);


                    if(onLoadMoreListener!=null &&!comments.isEmpty() && dy>1) {
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
        }

       return  null;

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof CommentViewHolder){
            Comment comment = comments.get(position);
            ((CommentViewHolder) holder).name.setText(comment.getSenderId());
            ((CommentViewHolder) holder).text.setText(comment.getText());
            ((CommentViewHolder) holder).timestamp.setText(MyDateFormatter.lastSeenConverterShort(comment.getTimestamp()));

        }else  if(holder instanceof UserMessageAdapter.LoadMoreViewHolder){
            ((LoadMoreViewHolder) holder).mProgressBar.setIndeterminate(true);

        }



    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    @Override
    public int getItemViewType(int position) {

        String messageType = comments.get(position).getType();
        int viewType;

        switch (messageType){

            case VIEWTYPE_LOAD_STR: viewType =VIEWTYPE_LOAD;
                break;

            case VIEWTYPE_TEXT_STR: viewType =VIEWTYPE_TEXT;
                break;
            default: viewType = VIEWTYPE_TEXT;
                break;
        }
        return viewType;
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        private ImageView avatar;
        private TextView name, text,reply,timestamp;

        public CommentViewHolder(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.comment_item_avatar);
            name = itemView.findViewById(R.id.comment_item_name);
            text = itemView.findViewById(R.id.comment_item_text);
            reply = itemView.findViewById(R.id.commentNum);
            timestamp = itemView.findViewById(R.id.comment_item_timestamp);
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

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }
    public void setLoaded() {
        loading = false;
    }
}
