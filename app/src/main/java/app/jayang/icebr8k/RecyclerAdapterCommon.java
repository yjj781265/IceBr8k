package app.jayang.icebr8k;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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
    public void onBindViewHolder( Viewholder holder, int position) {
        UserQA userQA = commonArrayList.get(position);
         holder.question_common.setText(userQA.getQuestion());

        //holder.answer_common.setText(userQA.getAnswer());
        Glide.with(holder.user1_pic.getContext()).load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()).
                apply(RequestOptions.circleCropTransform()).into(holder.user1_pic);
        Glide.with(holder.user2_pic.getContext()).load(user2.getPhotourl()).
                apply(RequestOptions.circleCropTransform()).into(holder.user2_pic);



    }

    @Override
    public int getItemCount() {
        return commonArrayList.size();
    }
}
