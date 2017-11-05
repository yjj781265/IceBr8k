package app.jayang.icebr8k;

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

/**
 * Created by yjj781265 on 11/2/2017.
 */

public class RecyclerAdapterCommon extends RecyclerView.Adapter<Viewholder> {
    private ArrayList<UserQA> commonArrayList = new ArrayList<>();
    private Context context;
    private User user2;
    private Handler mHandler;


    public RecyclerAdapterCommon(ArrayList<UserQA> arrayList,Context context,User user2) {
        this.context = context;
        commonArrayList = arrayList;
        this.user2 = user2;


    }

    @Override
    public Viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.result_common_item,parent,false);






        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(final Viewholder holder, int position) {

        final UserQA userQA = commonArrayList.get(position);



        hideAnswer(holder,userQA);
        holder.mRelativeLayout.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                 Runnable r = new Runnable() {
                    public void run() {
                        YoYo.with(Techniques.FlipInX)
                                .duration(1200)
                                .repeat(0)
                                .playOn(holder.commonCardView);
                        hideAnswer(holder,userQA);
                        holder.mRelativeLayout.setClickable(true);

                    }
                };
                mHandler =new Handler();
                holder.mRelativeLayout.setClickable(false);
                showAnswer(holder,userQA);
                mHandler.postDelayed(r, 2000);


            }
        });






    }

    @Override
    public int getItemCount() {
        return commonArrayList.size();
    }


    public void hideAnswer(Viewholder viewholder,UserQA userQA){
        viewholder.question_common.setVisibility(View.VISIBLE);
        viewholder.user2_pic.setVisibility(View.GONE);
        viewholder.user1_pic.setVisibility(View.GONE);
        viewholder.answer_common.setVisibility(View.GONE);
        viewholder.question_common.setText(userQA.getQuestion());


    }
    public void showAnswer(Viewholder viewholder,UserQA userQA){
        viewholder.question_common.setVisibility(View.GONE);
        viewholder.user2_pic.setVisibility(View.VISIBLE);
        viewholder.user1_pic.setVisibility(View.VISIBLE);
        viewholder.answer_common.setVisibility(View.VISIBLE);

        Glide.with(viewholder.user1_pic.getContext()).load(FirebaseAuth.getInstance().
                getCurrentUser().getPhotoUrl()).
                apply(RequestOptions.circleCropTransform()).into(viewholder.user1_pic);
        Glide.with(viewholder.user2_pic.getContext()).load(user2.getPhotourl()).
                apply(RequestOptions.circleCropTransform()).into(viewholder.user2_pic);
        viewholder.answer_common.setText(userQA.getAnswer());
        YoYo.with(Techniques.FlipInX)
                .duration(1200)
                .repeat(0)
                .playOn(viewholder.commonCardView);

    }

}
