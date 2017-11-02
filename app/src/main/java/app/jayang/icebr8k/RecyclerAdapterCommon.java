package app.jayang.icebr8k;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by yjj781265 on 11/2/2017.
 */

public class RecyclerAdapterCommon extends RecyclerView.Adapter<Viewholder> {
    private ArrayList<UserQA> commonArrayList = new ArrayList<>();
    private Context context;
    private User user2;
    private Handler mHandler;
    private Runnable runnable;
    public RecyclerAdapterCommon(ArrayList<UserQA> arrayList,Context context,User user2) {
        this.context = context;
        commonArrayList = arrayList;
        this.user2 = user2;


    }

    @Override
    public Viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.result_common_item,parent,false);


        mHandler = new Handler();






        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(final Viewholder holder, int position) {
        final UserQA userQA = commonArrayList.get(position);
        final RelativeLayout mRlayout = holder.mRelativeLayout;
         holder.question_common.setText(userQA.getQuestion());
        YoYo.with(Techniques.Wobble)
                .duration(500)
                .repeat(0)
                .playOn(mRlayout);
        runnable = new Runnable() {
            @Override
            public void run() {
      //* do what you need to do *//*

                holder.user1_pic.setVisibility(View.VISIBLE);
                holder.user2_pic.setVisibility(View.VISIBLE);
                holder.answer_common.setVisibility(View.VISIBLE);

                holder.answer_common.setText(userQA.getAnswer());
                Glide.with(holder.user1_pic.getContext()).load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()).
                        apply(RequestOptions.circleCropTransform()).into(holder.user1_pic);
                Glide.with(holder.user2_pic.getContext()).load(user2.getPhotourl()).
                        apply(RequestOptions.circleCropTransform()).into(holder.user2_pic);
                YoYo.with(Techniques.FadeIn)
                        .duration(500)
                        .repeat(0)
                        .playOn(mRlayout);


                mRlayout.setClickable(false);

      //* and here comes the "trick" *//*
                mHandler.postDelayed(this, 1000);
                mHandler.removeCallbacks(this);


            }
        };
         mRlayout.setOnClickListener(new View.OnClickListener() {
             int i=0;
             @Override
             public void onClick(View view) {
                 if (i == 0) {
                     Toast.makeText(context, "clicked", Toast.LENGTH_SHORT).show();
                     holder.question_common.setVisibility(View.GONE);
                     // mHandler.postDelayed(runnable,2000);
                     holder.user1_pic.setVisibility(View.VISIBLE);
                     holder.user2_pic.setVisibility(View.VISIBLE);
                     holder.answer_common.setVisibility(View.VISIBLE);
                     holder.answer_common.setText(userQA.getAnswer());
                     Glide.with(holder.user1_pic.getContext()).load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()).
                             apply(RequestOptions.circleCropTransform()).into(holder.user1_pic);
                     Glide.with(holder.user2_pic.getContext()).load(user2.getPhotourl()).
                             apply(RequestOptions.circleCropTransform()).into(holder.user2_pic);
                     i=1;

                 }else{
                     holder.user1_pic.setVisibility(View.INVISIBLE);
                     holder.user2_pic.setVisibility(View.INVISIBLE);
                     holder.answer_common.setVisibility(View.INVISIBLE);
                     holder.question_common.setVisibility(View.VISIBLE);
                     holder.question_common.setText(userQA.getQuestion());
                     i=0;
                 }
             }
         });






    }

    @Override
    public int getItemCount() {
        return commonArrayList.size();
    }
}
