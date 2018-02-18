package app.jayang.icebr8k.Adapter;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import app.jayang.icebr8k.Modle.User;
import app.jayang.icebr8k.Modle.UserQA;
import app.jayang.icebr8k.R;

/**
 * Created by LoLJay on 11/2/2017.
 */

public class RecyclerAdapterDiff extends RecyclerView.Adapter<Viewholder> {
  private  ArrayList<UserQA> user1QA,user2QA;
    private User user2;
     private Context context;

    public RecyclerAdapterDiff(ArrayList<UserQA> user1QA, ArrayList<UserQA> user2QA, User user2,Context context) {
        this.user1QA = user1QA;
        this.user2QA = user2QA;
        this.context = context;
        this.user2 = user2;
    }

    @Override
    public Viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.result_diff_item,parent,false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(final Viewholder holder, int position) {
        final UserQA userQA = user1QA.get(position);
        for(int i =0;i<user2QA.size();i++) {
            if (userQA.getQuestionId().equals(user2QA.get(i).getQuestionId())){
                hideAnswer(holder,userQA);
                final UserQA userQA2 = user2QA.get(i);
                holder.diffRelativeLayout.setOnClickListener(new View.OnClickListener() {
                    int i =0;
                    @Override
                    public void onClick(View view) {
                        Runnable r = new Runnable() {
                            public void run() {
                                YoYo.with(Techniques.FlipInX)
                                        .duration(1200)
                                        .repeat(0)
                                        .playOn(holder.diffCardView);
                                hideAnswer(holder,userQA);
                                holder.diffRelativeLayout.setClickable(true);

                            }
                        };
                        Handler handler = new Handler();
                        holder.diffRelativeLayout.setClickable(false);
                        showAnswer(holder,userQA,userQA2);
                        handler.postDelayed(r,2000);


                    }
                });
            }

        }

    }

    @Override
    public int getItemCount() {
        int counter=0;
        for(UserQA user1 : user1QA){
            for(UserQA user2 : user2QA){
                if(user1.getQuestionId().equals(user2.getQuestionId())){
                    counter++;

                }
            }
        }
        return counter;
    }

    public void hideAnswer(Viewholder viewholder,UserQA userQA){
        viewholder.question_diff.setVisibility(View.VISIBLE);
        viewholder.user2_pic_diff.setVisibility(View.GONE);
        viewholder.user1_pic_diff.setVisibility(View.GONE);
        viewholder.answer1_diff.setVisibility(View.GONE);
        viewholder.answer2_diff.setVisibility(View.GONE);
        viewholder.question_diff.setText(userQA.getQuestion());
    }

    public void showAnswer(Viewholder viewholder,UserQA userQA1,UserQA userQA2){
        viewholder.question_diff.setVisibility(View.GONE);
        viewholder.user2_pic_diff.setVisibility(View.VISIBLE);
        viewholder.user1_pic_diff.setVisibility(View.VISIBLE);
        viewholder.answer1_diff.setVisibility(View.VISIBLE);
        viewholder.answer2_diff.setVisibility(View.VISIBLE);
        Glide.with(viewholder.user1_pic_diff.getContext()).load(FirebaseAuth.getInstance().
                getCurrentUser().getPhotoUrl()).
                apply(RequestOptions.circleCropTransform()).into(viewholder.user1_pic_diff);
        Glide.with(viewholder.user2_pic_diff.getContext()).load(user2.getPhotourl()).
                apply(RequestOptions.circleCropTransform()).into(viewholder.user2_pic_diff);
        viewholder.answer1_diff.setText(userQA1.getAnswer());
        viewholder.answer2_diff.setText(userQA2.getAnswer());
        YoYo.with(Techniques.FlipInX)
                .duration(1200)
                .repeat(0)
                .playOn(viewholder.diffCardView);


    }

}
