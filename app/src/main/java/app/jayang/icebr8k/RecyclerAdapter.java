package app.jayang.icebr8k;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;


import java.util.ArrayList;



/**
 * Created by LoLJay on 10/20/2017.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<Viewholder> implements FastScrollRecyclerView.SectionedAdapter  {
    private ArrayList<User> userArrayList = new ArrayList<>();
    private Context context;


    public RecyclerAdapter(Context context,ArrayList<User> userArrayList) {
        this.userArrayList = userArrayList;
        this.context=context;

    }

    @Override
    public Viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item,parent,false);
        return  new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(Viewholder holder,  int position) {
        final User mUser = userArrayList.get(position);
        holder.username .setText(mUser.getUsername());
        holder.displayname.setText(mUser.getDisplayname());
        Glide.with(holder.image.getContext()).load(mUser.getPhotourl()).
                apply(RequestOptions.circleCropTransform()).into(holder.image);






       // SpannableString ss1=  new SpannableString(score);
       // ss1.setSpan(new RelativeSizeSpan((float)0.5),1,3,0);// set size for %
       // ss1.setSpan(new ForegroundColorSpan(context.getColor(R.color.darkYellow)), 0, 2, 0);// set color


        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(),UserProfilePage.class);
                intent.putExtra("userInfo",mUser);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userArrayList.size();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return userArrayList.get(position).getDisplayname().substring(0,1);
    }
}

