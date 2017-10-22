package app.jayang.icebr8k;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

/**
 * Created by LoLJay on 10/20/2017.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<Viewholder> {
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
    public void onBindViewHolder(Viewholder holder, final int position) {
         final User mUser = userArrayList.get(position);
        holder.username .setText(mUser.getUsername());
        holder.displayname.setText(mUser.getDisplayname());
        Glide.with(holder.image.getContext()).load(mUser.getPhotourl()).
                apply(RequestOptions.circleCropTransform()).into(holder.image);
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
}
