package app.jayang.icebr8k.Adapter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import app.jayang.icebr8k.Modle.UserDialog;
import app.jayang.icebr8k.R;

/**
 * Created by LoLJay on 10/20/2017.
 */

public class Viewholder extends RecyclerView.ViewHolder {
    public ImageView user1_pic,user2_pic,user1_pic_diff,user2_pic_diff,onlineStats;
    public TextView username;
    public RelativeLayout mRelativeLayout,diffRelativeLayout;
    public CardView diffCardView,commonCardView;
    public TextView score,question_common,answer_common;
    public TextView question_diff,answer1_diff,answer2_diff;


    public Viewholder(View itemView) {
        super(itemView);
        //for reuslt activity_common
        question_common =itemView.findViewById(R.id.question_common);
        answer_common =itemView.findViewById(R.id.answer_common);
        user1_pic = itemView.findViewById(R.id.user1_pic);
        user2_pic = itemView.findViewById(R.id.user2_pic);
        mRelativeLayout = itemView.findViewById(R.id.comman_Rlayout);
        commonCardView =itemView.findViewById(R.id.commonCardView);

        //for reuslt activity_diff
        question_diff =itemView.findViewById(R.id.question_diff);
        answer1_diff =itemView.findViewById(R.id.answer1_diff);
        answer2_diff =itemView.findViewById(R.id.answer2_diff);
        user1_pic_diff = itemView.findViewById(R.id.user1_pic_diff);
        user2_pic_diff = itemView.findViewById(R.id.user2_pic_diff);
        diffRelativeLayout = itemView.findViewById(R.id.diff_Rlayout);
        diffCardView =itemView.findViewById(R.id.diffCardView);

    }






}