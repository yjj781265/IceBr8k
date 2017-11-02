package app.jayang.icebr8k;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by LoLJay on 10/20/2017.
 */

public class Viewholder extends RecyclerView.ViewHolder {
    public ImageView image,user1_pic,user2_pic;
    public TextView displayname;
    public TextView username;
    public LinearLayout linearLayout;
    public RelativeLayout mRelativeLayout;
    public TextView score,question_common,answer_common;


    public Viewholder(View itemView) {
        super(itemView);
        image = itemView.findViewById(R.id.imageview_id);
        displayname =itemView.findViewById(R.id.displayname_textview);
        username=itemView.findViewById(R.id.username_textview);
        linearLayout =itemView.findViewById(R.id.recycleritem_id);
        score = itemView.findViewById(R.id.score);
        //for reuslt activity_common
        question_common =itemView.findViewById(R.id.question_common);
        answer_common =itemView.findViewById(R.id.answer_common);
        user1_pic = itemView.findViewById(R.id.user1_pic);
        user2_pic = itemView.findViewById(R.id.user2_pic);
        mRelativeLayout = itemView.findViewById(R.id.comman_Rlayout);

    }

}
