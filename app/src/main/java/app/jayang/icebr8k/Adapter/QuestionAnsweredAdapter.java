package app.jayang.icebr8k.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.xw.repo.BubbleSeekBar;

import java.util.ArrayList;
import java.util.Date;

import app.jayang.icebr8k.Model.SurveyQ;
import app.jayang.icebr8k.Model.UserQA;
import app.jayang.icebr8k.QuestionActivity;
import app.jayang.icebr8k.R;

public class QuestionAnsweredAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<UserQA> mList;
    private Context mContext;
    private Dialog questionDialog;
    private DatabaseReference  userQARef = FirebaseDatabase.getInstance().getReference()
            .child("UserQA").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
    private final long DAYS = 60*60*48*1000;  // 2 DAYS

    public QuestionAnsweredAdapter(ArrayList<UserQA> list, Context context) {
        mList = list;
        mContext = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_question, parent, false);
        return new QuestionAnsweredViewholder(itemView);

    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        UserQA userQA = mList.get(position);
        if(holder instanceof QuestionAnsweredAdapter.QuestionAnsweredViewholder){
            ((QuestionAnsweredViewholder) holder).question.setText(userQA.getQuestion().trim());
           ((QuestionAnsweredViewholder) holder).stamp.setVisibility("skipped".equals(userQA.getAnswer()) ? View.VISIBLE:View.GONE);
            ((QuestionAnsweredViewholder) holder).answer.setVisibility("skipped".equals(userQA.getAnswer()) ? View.GONE:View.VISIBLE);
            ((QuestionAnsweredViewholder) holder).answer_bubble.setVisibility("skipped".equals(userQA.getAnswer()) ? View.GONE:View.VISIBLE);
           ((QuestionAnsweredViewholder) holder).answer.setText(userQA.getAnswer());
            ((QuestionAnsweredViewholder) holder).comment.setText(userQA.getNumComments());

        }



    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class  QuestionAnsweredViewholder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView question,answer,comment;
        ImageView stamp,answer_bubble;
        CardView mCardView;
        LinearLayout comment_layout;

        public QuestionAnsweredViewholder(View itemView) {
            super(itemView);
            question = itemView.findViewById(R.id.question);
            stamp = itemView.findViewById(R.id.question_stamp);
            answer_bubble = itemView.findViewById(R.id.question_answerIcon);
            comment_layout = itemView.findViewById(R.id.question_comment_layout);

            answer = itemView.findViewById(R.id.answer);
            comment = itemView.findViewById(R.id.commentNum);
            mCardView = itemView.findViewById(R.id.cardView);
            mCardView.setOnClickListener(this);
            comment_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(getAdapterPosition()!= RecyclerView.NO_POSITION){
                        UserQA userQA = mList.get(getAdapterPosition());
                        Intent intent = new Intent(mContext, QuestionActivity.class);
                        intent.putExtra("questionId",userQA.getQuestionId());
                        mContext.startActivity(intent);
                    }

                }
            });

        }

        @Override
        public void onClick(View v) {
            if(getAdapterPosition()!= RecyclerView.NO_POSITION){
                UserQA userQA = mList.get(getAdapterPosition());
                Intent intent = new Intent(mContext, QuestionActivity.class);
                intent.putExtra("questionId",userQA.getQuestionId());
                mContext.startActivity(intent);
            }

        }
    }



    void showQuestionCard(final UserQA userQA){

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("Questions_8")
                .child(userQA.getQuestionId())
                .child("type");
        questionDialog = new Dialog(mContext);
        questionDialog.setCanceledOnTouchOutside(true);
        questionDialog.setContentView(R.layout.question_dialog);
        TextView question = questionDialog.findViewById(R.id.question_id);
        final TextView comments =  questionDialog.findViewById(R.id.question_comment);
        // skip stamp visibility
        questionDialog.findViewById(R.id.question_skip_stamp).
                setVisibility("skipped".equals(userQA.getAnswer()) ? View.VISIBLE :View.GONE);

        question.setText(userQA.getQuestion().trim());

        // get num of comments
        DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference()
                .child("Comments")
                .child(userQA.getQuestionId());
        commentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                long count = dataSnapshot.getChildrenCount();
                String str = count>0 ? ""+count :"";
                comments.setText("Comments "+str );

                comments.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, QuestionActivity.class);
                        intent.putExtra("questionId",userQA.getQuestionId());
                        mContext.startActivity(intent);
                        questionDialog.dismiss();
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // get question and type

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String type = dataSnapshot.getValue(String.class);

                switch (type) {
                    case "mc": isMultipleChoice(questionDialog,userQA);
                    break;

                    case "sp": isDropDown(questionDialog,userQA);
                    break;

                    case "sc" : isScale(questionDialog,userQA);
                    break;

                    default: return ;



                }
                questionDialog.show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //skip button click
        questionDialog.findViewById(R.id.question_skip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference resetRef = userQARef
                        .child(userQA.getQuestionId())
                        .child("reset");

                resetRef .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        // if timestamp less than cuurent time question is resetable
                        if(dataSnapshot.getValue(Long.class)==null
                                || new Date().getTime()> dataSnapshot.getValue(Long.class)){
                            final String originalAnswer = userQA.getAnswer();
                            userQA.setAnswer("skipped");
                            showVerifyDialog(originalAnswer,"skipped",userQA);




                        }else{
                            setTimerUI(dataSnapshot.getValue(Long.class) - new Date().getTime());
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });

    }

    void isScale(final Dialog dialog, final UserQA userQA){
        dialog.findViewById(R.id.sub_question_id).setVisibility(View.VISIBLE);

        final BubbleSeekBar mSeekBar = dialog.findViewById(R.id.seekBar);
        mSeekBar.setVisibility(View.VISIBLE);
        //handle skipped answer
        mSeekBar.setProgress( !"skipped".equals(userQA.getAnswer()) ? Float.valueOf(userQA.getAnswer()) :5f);

        dialog.findViewById(R.id.question_reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check question is resetable or not
            DatabaseReference resetRef = userQARef
                        .child(userQA.getQuestionId())
                        .child("reset");

            resetRef .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                   // if timestamp less than cuurent time question is resetable
                    if(dataSnapshot.getValue(Long.class)==null
                            || new Date().getTime()> dataSnapshot.getValue(Long.class)){
                        final String originalAnswer = userQA.getAnswer();
                        userQA.setAnswer(String.valueOf(mSeekBar.getProgress()));
                        showVerifyDialog(originalAnswer,String.valueOf(mSeekBar.getProgress()),userQA);



                    }else{
                        setTimerUI(dataSnapshot.getValue(Long.class) - new Date().getTime());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });




            }
        });



    }

    void isDropDown(final Dialog dialog,final UserQA userQA){
        dialog.findViewById(R.id.sub_question_id).setVisibility(View.GONE);
        final Spinner spinner = dialog.findViewById(R.id.spinner_id);
        spinner.setVisibility(View.VISIBLE);

        DatabaseReference mRef= FirebaseDatabase.getInstance().getReference("Questions_8")
                .child(userQA.getQuestionId());

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> answer = null;


                String type = dataSnapshot.child("type").getValue(String.class);
                String question = dataSnapshot.child("question").getValue(String.class);
                String question_id = dataSnapshot.child("questionId").getValue(String.class);
                if(dataSnapshot.hasChild("answer") ) {
                    GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>(){};
                    answer = dataSnapshot.child("answer").getValue(t);

                }

                SurveyQ surveyQ = new SurveyQ(type, question, question_id, answer);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_dropdown_item, surveyQ.getAnswer());
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                int position = surveyQ.getAnswer().indexOf(userQA.getAnswer());
                spinner.setAdapter(adapter);
                // set answer
                if(position>=0){
                    spinner.setSelection(position);
                }

                dialog.findViewById(R.id.question_reset).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.findViewById(R.id.question_reset).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // check question is resetable or not
                                DatabaseReference resetRef = userQARef
                                        .child(userQA.getQuestionId())
                                        .child("reset");

                                resetRef .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        // if timestamp less than cuurent time question is resetable
                                        if(dataSnapshot.getValue(Long.class)==null
                                                || new Date().getTime()> dataSnapshot.getValue(Long.class)){
                                            final String originalAnswer = userQA.getAnswer();
                                            userQA.setAnswer(spinner.getSelectedItem().toString());
                                            showVerifyDialog(originalAnswer,spinner.getSelectedItem().toString(),userQA);



                                        }else{
                                            setTimerUI(dataSnapshot.getValue(Long.class) - new Date().getTime());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });




                            }
                        });
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });





    }

    void isMultipleChoice(final Dialog dialog, final UserQA userQA){
        dialog.findViewById(R.id.sub_question_id).setVisibility(View.GONE);


        dialog.findViewById(R.id.sub_question_id).setVisibility(View.INVISIBLE);
        final RadioGroup radioGroup = dialog.findViewById(R.id.radioGroup);
        radioGroup.setVisibility(View.VISIBLE);

        DatabaseReference mRef= FirebaseDatabase.getInstance().getReference("Questions_8")
                .child(userQA.getQuestionId());

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> answer = null;


                String type = dataSnapshot.child("type").getValue(String.class);
                String question = dataSnapshot.child("question").getValue(String.class);
                String question_id = dataSnapshot.child("questionId").getValue(String.class);
                if(dataSnapshot.hasChild("answer") ) {
                    GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>(){};
                    answer = dataSnapshot.child("answer").getValue(t);

                }

                SurveyQ surveyQ = new SurveyQ(type, question, question_id, answer);
                //Ui Stuff
                for (int i = 0; i < surveyQ.getAnswer().size(); i++) {
                    RadioButton button = new RadioButton(mContext);
                    button.setText(surveyQ.getAnswer().get(i));
                    radioGroup.addView(button);
                    if(surveyQ.getAnswer().get(i).toString().equals(userQA.getAnswer())) {
                        ((RadioButton)radioGroup.getChildAt(i)).setChecked(true);
                    }

            }
                // reset click listener
                dialog.findViewById(R.id.question_reset).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // check question is resetable or not
                        DatabaseReference resetRef = userQARef
                                .child(userQA.getQuestionId())
                                .child("reset");

                        resetRef .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // if timestamp less than cuurent time question is resetable
                                if(dataSnapshot.getValue(Long.class)==null
                                        || new Date().getTime()> dataSnapshot.getValue(Long.class)){
                                    final String originalAnswer = userQA.getAnswer();
                                    if(radioGroup.getCheckedRadioButtonId()==-1&& radioGroup.getVisibility()==View.VISIBLE){
                                        Toast.makeText(mContext,"Make a selection",Toast.LENGTH_SHORT).show();

                                    }else if(radioGroup.getCheckedRadioButtonId()!=-1&& radioGroup.getVisibility()==View.VISIBLE) {
                                        int id = radioGroup.getCheckedRadioButtonId();
                                        View radioButton = radioGroup.findViewById(id);
                                        int radioId = radioGroup.indexOfChild(radioButton);
                                        RadioButton btn = (RadioButton) radioGroup.getChildAt(radioId);
                                        String selection = (String) btn.getText();
                                        userQA.setAnswer(selection);
                                        showVerifyDialog(originalAnswer,selection,userQA);
                                    }



                                }else{
                                    setTimerUI(dataSnapshot.getValue(Long.class) - new Date().getTime());
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    void setTimerUI (long mills){

      final MaterialDialog dialog =  new MaterialDialog.Builder(mContext)
              .title("You can't reset this question until after")
                .positiveText(R.string.ok)
               .show();
        new CountDownTimer(mills, 1000) {

            public void onTick(long millisUntilFinished) {
                long leftMills;
                long day =   millisUntilFinished/(60*24*60*1000);
                leftMills = millisUntilFinished - day *24*60*60*1000;
                long hour =  leftMills/(60*60*1000);
                leftMills = leftMills - hour*60*60*1000;
                long  min =  (leftMills)/(1000*60);
                leftMills = leftMills-(min*60*1000);
                long sec = leftMills/1000;

                dialog.setContent(day +"d "+hour+"h "+min+"m "+ sec+"s");

            }

            public void onFinish() {
                dialog.setContent("done!");

            }
        }.start();
    }

    void showVerifyDialog (String originalAnswer, final String newAnswer, final UserQA userQA){
        String content = "Are you sure to change answer from \""+ originalAnswer +"\" to \""+ newAnswer+"\"?";
        new MaterialDialog.Builder(mContext)
                .content(content)
                .positiveText("Yes")
                .positiveColor(ContextCompat.getColor(mContext, R.color.colorAccent))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which) {
                        // update the database
                        userQARef.child(userQA.getQuestionId()).setValue(userQA);
                        userQARef.child(userQA.getQuestionId()).child("reset").setValue(new Date().getTime()+
                                (DAYS)).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // show updating dialog
                                final MaterialDialog resetDialog =  new MaterialDialog.Builder(mContext)
                                        .content("Resetting your answer...")
                                        .positiveText(R.string.ok)
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                            }
                                        })
                                        .show();

                                resetDialog.setContent("Reset Success");
                                resetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface mdialog) {
                                        questionDialog.findViewById(R.id.question_skip_stamp)
                                                .setVisibility("skipped".equals(newAnswer)? View.VISIBLE :View.GONE);
                                    }
                                });



                            }
                        });
                    }
                })

                .negativeText("NO")
                .negativeColor(ContextCompat.getColor(mContext, R.color.holo_red_light))
                .show();

    }

}
