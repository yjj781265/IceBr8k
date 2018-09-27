package app.jayang.icebr8k.Adapter;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;

import app.jayang.icebr8k.Model.TagModel;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.Utility.MyToolBox;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class TagAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ArrayList<TagModel> tagModels;
    Activity activity;
    final int EMPTY_VH = 0,TAG_VH =1;

    public TagAdapter(ArrayList<TagModel> tagModels, Activity activity) {
        this.tagModels = tagModels;
        this.activity = activity;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == EMPTY_VH){
            view = LayoutInflater.from(activity).inflate(R.layout.item_empty_placeholder, parent, false);
            return  new EmptyViewHolder(view);
        }else {
            view = LayoutInflater.from(activity).inflate(R.layout.item_tag, parent, false);
            return new TagViewHolder(view);
        }


    }

    @Override
    public int getItemViewType(int position) {
        TagModel tagModel = tagModels.get(position);
        return tagModel.getTagtxt()==null ?
                EMPTY_VH:
                TAG_VH;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TagModel tagModel = tagModels.get(position);
        if(holder instanceof TagViewHolder){
            ((TagViewHolder) holder).bindView(tagModel);
        }
    }

    @Override
    public int getItemCount() {
        return tagModels.size();
    }

    class TagViewHolder extends RecyclerView.ViewHolder {
        TextView tagName;
        FrameLayout container;
        PopupWindow mPopupWindow;
        View customView;
        ImageView thumbUp;

        ImageView thumbDown;
        TextView likes;
        TextView dislikes;


        public TagViewHolder(View itemView) {
            super(itemView);

                tagName = itemView.findViewById(R.id.tag_txt);
                container = itemView.findViewById(R.id.tag_container);
                // Initialize a new instance of LayoutInflater service
                LayoutInflater inflater = (LayoutInflater) activity.getSystemService(LAYOUT_INFLATER_SERVICE);

                // Inflate the custom layout/view
                customView = inflater.inflate(R.layout.tag_popup, null);
                thumbUp = customView.findViewById(R.id.tag_thumbup);
                thumbDown = customView.findViewById(R.id.tag_thumbdown);
                likes = customView.findViewById(R.id.tag_likes);
                dislikes = customView.findViewById(R.id.tag_dislikes);

                mPopupWindow = new PopupWindow(
                        customView,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);


                container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showPopup(view);
                    }
                });







        }

        private void bindView(TagModel tagModel) {
            tagName.setText(tagModel.getTagtxt());
        }

        private void showPopup(View parentView) {

            mPopupWindow.setBackgroundDrawable(new ColorDrawable());
            mPopupWindow.setOutsideTouchable(true);


            thumbUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int count = 2;
                    if (view.isSelected()) {
                        likes.setText("44");
                        view.setSelected(false);
                    } else {
                        likes.setText("7");
                        view.setSelected(true);
                        thumbDown.setSelected(false);
                    }
                }
            });
            thumbDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int count = 6;
                    if (view.isSelected()) {
                        dislikes.setText("123");
                        view.setSelected(false);
                    } else {
                        dislikes.setText("4324");
                        view.setSelected(true);
                        thumbUp.setSelected(false);
                    }
                }
            });

            mPopupWindow.showAsDropDown(parentView,
                    MyToolBox.convertDptoPixel(16f, parentView.getContext()), 0);

        }


    }


}
