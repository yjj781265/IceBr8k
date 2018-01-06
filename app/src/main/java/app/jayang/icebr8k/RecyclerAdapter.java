package app.jayang.icebr8k;

import android.content.Context;
import android.content.Intent;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import app.jayang.icebr8k.Fragments.Userstab_Fragment;
import app.jayang.icebr8k.Modle.User;
import app.jayang.icebr8k.Modle.UserDialog;
import app.jayang.icebr8k.Modle.UserQA;


/**
 * Created by LoLJay on 10/20/2017.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<Viewholder> {




    private Context context;
    private ArrayList<UserDialog>mUserDialogs;
    private Boolean sortByscore;
    private Viewholder mViewholder;


    private Comparator<UserDialog> mComparator;



    private static final Comparator<UserDialog>ONLINESTATS = new Comparator<UserDialog>() {
        @Override
        public int compare(UserDialog dialog, UserDialog t1) {
            int result= Integer.valueOf(t1.getOnlineStats()).
                    compareTo( Integer.valueOf(dialog.getOnlineStats()));
            if(result==0){
                result =dialog.getName().compareTo(t1.getName());
            }
            return  result;
        }
    };

    private static final Comparator<UserDialog> SCORE = new Comparator<UserDialog>() {
        @Override
        public int compare(UserDialog dialog, UserDialog t1) {
            int  result= Integer.valueOf(t1.getScore()).
                    compareTo( Integer.valueOf(dialog.getScore()));
            if(result==0){
                result =dialog.getName() .compareTo(t1.getName());
            }
            return result;
        };
    };

    private  final SortedList<UserDialog> mSortedList = new SortedList<>(UserDialog.class,
            new SortedList.Callback<UserDialog>() {
        @Override
        public void onInserted(int position, int count) {
            notifyItemChanged(position,count);

        }

        @Override
        public void onRemoved(int position, int count) {
            notifyItemChanged(position,count);

        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            notifyItemChanged(fromPosition,toPosition);

        }

        @Override
        public int compare(UserDialog o1, UserDialog o2) {
          return mComparator.compare(o1,o2);
        }

        @Override
        public void onChanged(int position, int count) {
            notifyItemChanged(position,count);

        }

        @Override
        public boolean areContentsTheSame(UserDialog oldItem, UserDialog newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areItemsTheSame(UserDialog item1, UserDialog item2) {
            return item1.getId()==item2.getId();
        }
    });

    public void add(UserDialog dialog){
        mSortedList.add(dialog);
    }

    public void remove(UserDialog dialog){
        mSortedList.remove(dialog);
    }

    public void add(ArrayList<UserDialog> dialogs){
        mSortedList.addAll(dialogs);
    }

    public void remove(ArrayList<UserDialog> dialogs){
        mSortedList.beginBatchedUpdates();
        for(UserDialog dialog : dialogs ){
            mSortedList.remove(dialog);
        }
        mSortedList.endBatchedUpdates();
    }

    public void replaceAll(ArrayList<UserDialog> dialogs){
        mSortedList.beginBatchedUpdates();
       for(int i = mSortedList.size()-1;i>=0;i--){
           final UserDialog dialog = mSortedList.get(i);
           if(!dialogs.contains(dialog)){
               mSortedList.remove(dialog);
           }
       }
       mSortedList.addAll(dialogs);
        mSortedList.endBatchedUpdates();
    }


    public RecyclerAdapter(Context context, ArrayList<UserDialog> dialogs, Boolean sortByscore) {
        this.context = context;
        mUserDialogs = dialogs;
        this.sortByscore =sortByscore;




    }



    @Override
    public Viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).
                inflate(R.layout.recycler_item, parent, false);

        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(final Viewholder holder, final int position) {
        this.mViewholder = holder;


        final UserDialog dialog = mUserDialogs.get(position);
       mViewholder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User mUser = new User(dialog.getName(), dialog.getUsername(),
                        dialog.getPhotoUrl(), dialog.getEmail());
                Intent intent = new Intent(view.getContext(), UserProfilePage.class);
                intent.putExtra("userInfo", mUser);
                intent.putExtra("userUid",dialog.getId());
                context.startActivity(intent);
            }
        });

        holder.username.setText(dialog.getUsername());
        holder.displayname.setText(dialog.getName());
        Glide.with(holder.image.getContext()).load(dialog.getPhotoUrl()).
                apply(RequestOptions.circleCropTransform()).into(holder.image);

        if (dialog.getScore().equals("")) {
            holder.score.setVisibility(View.INVISIBLE);
        } else {
            holder.score.setText(dialog.getScore() + "%");

        }
        DatabaseReference mRef2 = FirebaseDatabase.getInstance().
                        getReference("Users/" + dialog.getId() + "/onlineStats");
                mRef2.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String online = dataSnapshot.getValue(String.class);
                        if (online != null) {

                            if (online.equals("2") && holder.onlineStats.getBackground()!= context.getDrawable(R.drawable.green_dot) ) {
                                holder.onlineStats.setBackground(context.getDrawable(R.drawable.green_dot));
                                holder.onlineStats.setVisibility(View.VISIBLE);
                                holder.linearLayout.setAlpha((float) 1.0);

                            } else if (online.equals("1")&&holder.onlineStats.getBackground()!= context.getDrawable(R.drawable.circleshape_busy)) {
                                holder.onlineStats.setBackground(context.getDrawable(R.drawable.circleshape_busy));
                                holder.onlineStats.setVisibility(View.VISIBLE);
                                holder.linearLayout.setAlpha((float) 1.0);

                            } else {
                                holder.linearLayout.setAlpha((float) 0.5);
                                holder.onlineStats.setVisibility(View.INVISIBLE);

                            }

                            if(!dialog.getOnlineStats().equals(online)){
                                dialog.setOnlineStats(online);
                            }





                        } else {
                            // null
                            holder.linearLayout.setVisibility(View.VISIBLE);
                            holder.linearLayout.setAlpha((float) 0.5);
                            holder.onlineStats.setVisibility(View.INVISIBLE);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        //addQAListener(dialog);





    }

    public void bind(final UserDialog dialog, final Userstab_Fragment.OnItemClickListener listener){

    }




    @Override
    public int getItemCount() {
        return mUserDialogs.size();
    }

    public void compareWithUser2(UserDialog dialog) {
        pullUser1QA(dialog);

    }

    public void pullUser1QA(final UserDialog dialog) {
       FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("" +
                "UserQA/" + currentUser.getUid());
        mRef.keepSynced(true);
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            ArrayList<UserQA> User1QA = new ArrayList<>();

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    UserQA userQA = childSnapshot.getValue(UserQA.class);
                    User1QA.add(userQA);

                }

                if (dataSnapshot.getChildrenCount() == User1QA.size()) {
                    pullUser2QA( User1QA, dialog);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void pullUser2QA(final ArrayList<UserQA> user1QA, final UserDialog dialog) {

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("UserQA/" + dialog.getId());
        mRef.keepSynced(true);
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            ArrayList<UserQA> User2QA = new ArrayList<>();

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    UserQA userQA = childSnapshot.getValue(UserQA.class);
                    User2QA.add(userQA);
                }
                SetScore(user1QA,User2QA, dialog);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void SetScore(ArrayList<UserQA> user1Arr, ArrayList<UserQA> user2Arr,
                         UserDialog dialog) {
        int size = user1Arr.size();
        int commonQuestionSize = 0;
        String score;

        ArrayList<String> user1StrArr = new ArrayList<>();
        ArrayList<String> user2StrArr = new ArrayList<>();

        for (UserQA userQA : user1Arr) {
            if (!userQA.getAnswer().equals("skipped")) {
                user1StrArr.add(userQA.getQuestionId());
            }

        }
        for (UserQA userQA : user2Arr) {
            if (!userQA.getAnswer().equals("skipped")) {
                user2StrArr.add(userQA.getQuestionId());
            }
        }

        user1StrArr.retainAll(user2StrArr);

        commonQuestionSize = user1StrArr.size();

        Log.d("Score", "Common Question " + commonQuestionSize);
        user1Arr.retainAll(user2Arr);
        Log.d("Score", String.valueOf(user1Arr.size()));
        Log.d("Score", "Size " + size);
        if (commonQuestionSize != 0) {
            score = String.valueOf((int) (((double) user1Arr.size() / (double) commonQuestionSize) * 100));
            Log.d("Score", "Score is " + score);

        }else if(user1Arr.isEmpty() || user2Arr.isEmpty()){
            score ="0";

        } else {
            score = "0";
        }
        if(dialog.getOnlineStats()==null){
            dialog.setOnlineStats("0");
        }
        dialog.setScore(score);
        if(sortByscore){
            Collections.sort(mUserDialogs,SCORE);
            notifyDataSetChanged();
        }else{
            mUserDialogs.indexOf(dialog);
            notifyItemChanged( mUserDialogs.indexOf(dialog));
        }







    }





}






