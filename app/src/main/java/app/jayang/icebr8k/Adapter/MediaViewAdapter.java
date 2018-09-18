package app.jayang.icebr8k.Adapter;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;

import app.jayang.icebr8k.Model.UserMessage;
import app.jayang.icebr8k.R;

public class MediaViewAdapter extends RecyclerView.Adapter<MediaViewAdapter.MediaViewVH> {
    private ArrayList<UserMessage> mMessages;
    private Activity mActivity;
    private PhotoViewClickListener mListener;


    public MediaViewAdapter(ArrayList<UserMessage> messages, Activity activity) {
        mMessages = messages;
        mActivity = activity;
        mListener = (PhotoViewClickListener) activity;
    }

    @NonNull
    @Override
    public MediaViewVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mActivity).inflate(
                R.layout.item_photoview, parent, false);
        return new MediaViewVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MediaViewVH holder, int position) {
        UserMessage message = mMessages.get(position);
        holder.mProgressBar.setVisibility(View.VISIBLE);
        Glide.with(mActivity).load(message.getText()).apply(new RequestOptions().error(R.drawable.noimage))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.mProgressBar.setVisibility(View.GONE);
                        return false;
                    }
                }) .transition(DrawableTransitionOptions.withCrossFade()).into(holder.mPhotoView).clearOnDetach();

    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    public class MediaViewVH extends RecyclerView.ViewHolder implements View.OnClickListener {
         PhotoView mPhotoView;
         ProgressBar mProgressBar;
        public MediaViewVH(View itemView) {
            super(itemView);
            mPhotoView = itemView.findViewById(R.id.photoView);
            mPhotoView.setOnClickListener(this);
            mProgressBar = itemView.findViewById(R.id.progressBar);
        }

        @Override
        public void onClick(View v) {
           // user interface to communicate with parent activity to show the toolbar or hide
            mListener.onPhotoViewClicked();
        }
    }

    public interface PhotoViewClickListener{
        void onPhotoViewClicked();
    }

}
