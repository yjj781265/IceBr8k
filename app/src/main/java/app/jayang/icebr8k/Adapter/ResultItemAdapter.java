package app.jayang.icebr8k.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import app.jayang.icebr8k.Model.ResultItem;
import app.jayang.icebr8k.QuestionActivity;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.SearchResult;

/**
 * Created by LoLJay on 11/2/2017.
 */

public class ResultItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  private  ArrayList<ResultItem> mResultItems;

     private Context context;

    public ResultItemAdapter(ArrayList<ResultItem> resultItems, Context context) {
        mResultItems = resultItems;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_result,parent,false);
        return new ResultItemViewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {


        if(holder instanceof ResultItemViewholder){
            ResultItem resultItem = mResultItems.get(position);
            ((ResultItemViewholder) holder).answer1.setText(resultItem.getAnswer1());
            ((ResultItemViewholder) holder).answer2.setText(resultItem.getAnswer2());
            ((ResultItemViewholder) holder).question.setText(resultItem.getQuesiton());
            ((ResultItemViewholder) holder).comment.setText(resultItem.getComment());
            Log.d("result123","binding");

            // get user2avatar
            DatabaseReference avatar2Ref = FirebaseDatabase.getInstance().getReference()
                    .child("Users").child(resultItem.getUser2Id()).child("photourl");
            avatar2Ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String url = dataSnapshot.getValue(String.class);
                    if(url!=null){
                        Glide.with(context).load(url).
                                apply(RequestOptions.circleCropTransform().placeholder(R.drawable.default_avatar3)).transition(DrawableTransitionOptions.withCrossFade(300)).into(((ResultItemViewholder) holder).avatar2);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

    }



    @Override
    public int getItemCount() {

        return mResultItems.size();
    }

    @Override
    public long getItemId(int position) {
        return mResultItems.get(position).getQuestionId().hashCode();
    }

    public class ResultItemViewholder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView avatar1,avatar2;
        public CardView mCardView;
        public TextView question,answer1,answer2,comment;



        public ResultItemViewholder(View itemView)  {
            super(itemView);
            mCardView = itemView.findViewById(R.id.cardView);
            avatar1 = itemView.findViewById(R.id.avatar1);
            avatar2 = itemView.findViewById(R.id.avatar2);
            question = itemView.findViewById(R.id.question);
            answer1 = itemView.findViewById(R.id.answer1);
            answer2 = itemView.findViewById(R.id.answer2);
            comment = itemView.findViewById(R.id.commentNum);
            mCardView.setOnClickListener(this);

            Glide.with(context).load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()).
                    apply(RequestOptions.circleCropTransform().placeholder(R.drawable.default_avatar3))
                    .transition(DrawableTransitionOptions.withCrossFade(300)).into(avatar1);

            if(getAdapterPosition()!=RecyclerView.NO_POSITION){
                final ResultItem resultItem = mResultItems.get(getAdapterPosition());
                // get comments



            }



        }


        @Override
        public void onClick(View v) {
            if(getAdapterPosition()!=RecyclerView.NO_POSITION){
                ResultItem resultItem = mResultItems.get(getAdapterPosition());
                Intent intent = new Intent(context, QuestionActivity.class);
                intent.putExtra("questionId",resultItem.getQuestionId());
                context.startActivity(intent);
                if(context instanceof SearchResult){
                    ((SearchResult) context).finish();
                }
            }

        }
    }

   /* public void updateList(ArrayList<ResultItem> newList) {
        Collections.sort(newList);
        Collections.sort(newList);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ResultDiffCallBack(mResultItems, newList));
        diffResult.dispatchUpdatesTo(this);
    }*/

}
