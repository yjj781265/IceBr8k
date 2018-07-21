package app.jayang.icebr8k.Adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.xw.repo.BubbleSeekBar;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Handler;

import app.jayang.icebr8k.Feedback;
import app.jayang.icebr8k.Homepage;
import app.jayang.icebr8k.Modle.SurveyQ;
import app.jayang.icebr8k.Modle.UserQA;
import app.jayang.icebr8k.QuestionActivity;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.Utility.Compatability;
import me.toptas.fancyshowcase.FancyShowCaseQueue;
import me.toptas.fancyshowcase.FancyShowCaseView;
import me.toptas.fancyshowcase.FocusShape;

import static android.support.constraint.Constraints.TAG;
import static app.jayang.icebr8k.MyApplication.getContext;

public class SurveyQuestionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    // view type
    private final String MC_STR = "mc";
    private final int MC = 0;
    private final String SC_STR = "sc";
    private final int SC = 1;
    private final String SP_STR = "sp";
    private final int SP = 2;
    private final String FEEDBACK_STR = "fb";
    private final int FEEDBACK = 3;
    private  Boolean tutorialConfirm ,tutorialPieChart,tutorialForward,tutorialComment,tutorialFab,tutorialSkip;

    private final long DAYS = 60*60*48*1000;  // 2 DAYS


    private ArrayList<SurveyQ> mQArrayList;
    private Context mContext;
    private Activity mActivity;
    private SubmitedListener mListener;
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private android.os.Handler mHandler = new android.os.Handler();
    private HashMap<SurveyQ, String> mHashMap =new HashMap<>();


    public SurveyQuestionAdapter(ArrayList<SurveyQ> QArrayList, Context context, final SubmitedListener mListener, Activity activity) {
        mQArrayList = QArrayList;
        mContext = context;
        this.mListener = mListener;
        mActivity = activity;
        getTutorialSettings();










        }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == MC) {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_survey_mc, parent, false);
            return new McViewHolder(v);

        } else if(viewType == SC) {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_survey_sc, parent, false);

            return new ScViewHolder(v);
        }else if(viewType == SP){
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_survey_sp, parent, false);

            return new SpViewHolder(v);
        }else if(viewType == FEEDBACK){
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_survey_feedback, parent, false);

            return new FeedbackViewHolder(v);
        }



        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
         SurveyQ surveyQ = mQArrayList.get(position);

        Log.d("SurveyAdapter123r", "binding view " + position + " " + surveyQ .getQuestion() );

        if(holder instanceof  McViewHolder) {
            RadioGroup mRadioGroup = ((McViewHolder) holder).mRadioGroup;
            ((McViewHolder) holder).question.setText(surveyQ.getQuestion());
         mRadioGroup.clearCheck();
         mRadioGroup.removeAllViews();


            String answer =null;
            if(mHashMap.get(surveyQ)!=null) {
                answer =mHashMap.get(surveyQ);
                ((McViewHolder) holder).stamp.setVisibility("skipped".equals(answer)?View.VISIBLE:View.GONE);

            }
            //Ui Stuff add radio buttons
            for (int i = 0; i < surveyQ.getAnswer().size(); i++) {
                RadioButton button = new RadioButton(mContext);
                button.setText(surveyQ.getAnswer().get(i));
                ((McViewHolder) holder).mRadioGroup.addView(button);
                if(answer!=null&&!"skipped".equals(answer)  && surveyQ.getAnswer().get(i).toString().equals(answer)) {
                    ((RadioButton)mRadioGroup.getChildAt(i)).setChecked(true);
                }
            }

            getComments(surveyQ ,((McViewHolder) holder).comment);

            }
            else if(holder instanceof  ScViewHolder){
            ((ScViewHolder) holder).question.setText(surveyQ.getQuestion());

            String answer = mHashMap.get(surveyQ);
            if(answer!=null){
                ((ScViewHolder) holder).stamp.setVisibility("skipped".equals(answer)?View.VISIBLE:View.GONE);
            }

            if(answer!=null && !"skipped".equals(answer)) {
                Float f = Float.valueOf(mHashMap.get(surveyQ));
                ((ScViewHolder) holder).mSeekBar.setProgress(f);
            }else{
                ((ScViewHolder) holder).mSeekBar.setProgress(5f);

            }
            getComments(surveyQ ,((ScViewHolder) holder).comment);




        }else if(holder instanceof  SpViewHolder){
            ((SpViewHolder) holder).question.setText(surveyQ.getQuestion());
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_dropdown_item, surveyQ.getAnswer());
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            ((SpViewHolder) holder).mSpinner.setAdapter(adapter);
            ((SpViewHolder) holder).mSpinner.setVisibility(View.VISIBLE);
            String answer = mHashMap.get(surveyQ);

            ((SpViewHolder) holder).stamp.setVisibility("skipped".equals(answer)?View.VISIBLE:View.GONE);

            if(answer!=null&&!"skipped".equals(answer)) {
                for (int i = 0; i < surveyQ.getAnswer().size(); i++) {
                    if (surveyQ.getAnswer().get(i).equals(answer)) {
                        ((SpViewHolder) holder).mSpinner .setSelection(i);
                    }
                }
            }
            getComments(surveyQ ,((SpViewHolder) holder).comment);


        }

        bindView(holder,surveyQ.getQuestionId());

    }

    @Override
    public int getItemCount() {
        return mQArrayList.size();
    }


    @Override
    public int getItemViewType(int position) {
        SurveyQ surveyQ = mQArrayList.get(position);
        String type = surveyQ.getType();

        switch(type){
            case MC_STR : return MC;

            case SC_STR : return SC;

            case SP_STR : return SP;

            case FEEDBACK_STR : return FEEDBACK;


        }
        return -1;
    }

    class McViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView question,confirm,skip,comment;
        ImageView check,pieChart,stamp;
        RadioGroup mRadioGroup;
        ProgressBar mProgressBar;
        SurveyQ mSurveyQ;


        public McViewHolder(View itemView) {
            super(itemView);
            question = itemView.findViewById(R.id.survey_mc_question);
            stamp = itemView.findViewById(R.id.survey_mc_stamp);
            confirm = itemView.findViewById(R.id.survey_mc_confirm);
            comment = itemView.findViewById(R.id.survey_mc_comment);
            pieChart = itemView.findViewById(R.id.survey_mc_result);
            mRadioGroup = itemView.findViewById(R.id.survey_mc_radioGroup);
            check = itemView.findViewById(R.id.checked);
            skip = itemView.findViewById(R.id.survey_mc_skip);
            mProgressBar = itemView.findViewById(R.id.survey_mc_progressBar);

            mProgressBar.setVisibility(View.GONE);
            skip.setVisibility(View.VISIBLE);
            confirm.setVisibility(View.VISIBLE);
            check.setVisibility(View.GONE);
            comment.setVisibility(View.GONE);
            pieChart.setVisibility(View.GONE);
            stamp.setVisibility(View.GONE);



            confirm.setOnClickListener(this);
            skip.setOnClickListener(this);
            pieChart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(getAdapterPosition()!= RecyclerView.NO_POSITION){
                        mSurveyQ = mQArrayList.get(getAdapterPosition());
                        showPieChart(mSurveyQ ,pieChart);
                    }

                }
            });
            Log.d("SurveyAdapter123r", "MC view " );



        }


        @Override
        public void onClick(View v) {

            if(getAdapterPosition()!= RecyclerView.NO_POSITION){
                mSurveyQ = mQArrayList.get(getAdapterPosition());
            }
            if(mSurveyQ!=null){

                if(v == confirm){
                    if(mRadioGroup.getCheckedRadioButtonId()==-1){
                        Toast.makeText(getContext(),"Make a selection",Toast.LENGTH_SHORT).show();

                    }else if(mRadioGroup.getCheckedRadioButtonId()!=-1) {
                        int id = mRadioGroup.getCheckedRadioButtonId();
                        View radioButton = mRadioGroup.findViewById(id);
                        int radioId = mRadioGroup.indexOfChild(radioButton);
                        RadioButton btn = (RadioButton) mRadioGroup.getChildAt(radioId);
                        final String selection = (String) btn.getText();
                        mProgressBar.setVisibility(View.VISIBLE);
                        skip.setVisibility(View.GONE);
                        confirm.setVisibility(View.GONE);


                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                uploadtoDatabase(mSurveyQ,skip,confirm, selection,
                                       mProgressBar,check,
                                       comment,pieChart,stamp);

                            }
                        },666);

                    }
                }else if(v == skip){

                    mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                uploadtoDatabase(mSurveyQ,skip,confirm, "skipped",
                                        mProgressBar,check,
                                        comment,pieChart,stamp
                                );

                            }
                        },666);

                    }
                }
            }


    }

    class ScViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView question, confirm, skip;
        BubbleSeekBar mSeekBar;
        ProgressBar mProgressBar;
        TextView comment;
        ImageView pieChart, check, stamp;
        SurveyQ mSurveyQ;

        public ScViewHolder(View itemView) {
            super(itemView);
            question = itemView.findViewById(R.id.survey_sc_question);
            confirm = itemView.findViewById(R.id.survey_sc_confirm);
            skip = itemView.findViewById(R.id.survey_sc_skip);
            mProgressBar = itemView.findViewById(R.id.survey_sc_progressBar);
            comment = itemView.findViewById(R.id.survey_sc_comment);
            pieChart = itemView.findViewById(R.id.survey_sc_piechart);
            mSeekBar = itemView.findViewById(R.id.survey_sc_seekBar);
            stamp = itemView.findViewById(R.id.survey_sc_stamp);
            check = itemView.findViewById(R.id.checked);

            mProgressBar.setVisibility(View.GONE);
            skip.setVisibility(View.VISIBLE);
            confirm.setVisibility(View.VISIBLE);
            check.setVisibility(View.GONE);
            comment.setVisibility(View.GONE);
            pieChart.setVisibility(View.GONE);
            stamp.setVisibility(View.GONE);





            confirm.setOnClickListener(this);
            skip.setOnClickListener(this);
            pieChart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(getAdapterPosition()!= RecyclerView.NO_POSITION){
                        mSurveyQ = mQArrayList.get(getAdapterPosition());
                        showPieChart(mSurveyQ ,pieChart);
                    }

                }
            });
            Log.d("SurveyAdapter123r", "SC view " );


        }

        @Override
        public void onClick(View v) {
            if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                mSurveyQ = mQArrayList.get(getAdapterPosition());
            }
            if (mSurveyQ != null) {
                mProgressBar.setVisibility(View.VISIBLE);
                skip.setVisibility(View.GONE);
                confirm.setVisibility(View.GONE);
                if (v == confirm) {

                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            uploadtoDatabase(mSurveyQ,skip,confirm,  "" + mSeekBar.getProgress(),
                                    mProgressBar, check,
                                    comment, pieChart, stamp);

                        }
                    }, 666);


                } else if (v == skip) {

                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            uploadtoDatabase(mSurveyQ,skip,confirm,  "skipped",
                                    mProgressBar, check,
                                    comment, pieChart, stamp
                            );

                        }
                    }, 666);

                }
            }
        }
    }



    class SpViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView question, confirm, skip;
        ProgressBar mProgressBar;
        TextView comment;
        ImageView pieChart, check, stamp;
        SurveyQ mSurveyQ;
        Spinner mSpinner;

        public SpViewHolder(View itemView) {
            super(itemView);
            question = itemView.findViewById(R.id.survey_sp_question);
            mSpinner = itemView.findViewById(R.id.survey_sp_spinner);
            confirm = itemView.findViewById(R.id.survey_sp_confirm);
            skip = itemView.findViewById(R.id.survey_sp_skip);
            mProgressBar = itemView.findViewById(R.id.survey_sp_progressBar);
            comment = itemView.findViewById(R.id.survey_sp_commentNum);
            pieChart = itemView.findViewById(R.id.survey_sp_result);
            check = itemView.findViewById(R.id.checked);
            stamp = itemView.findViewById(R.id.survey_sp_stamp);


            mProgressBar.setVisibility(View.GONE);
            skip.setVisibility(View.VISIBLE);
            confirm.setVisibility(View.VISIBLE);
            check.setVisibility(View.GONE);
            comment.setVisibility(View.GONE);
            pieChart.setVisibility(View.GONE);
            stamp.setVisibility(View.GONE);







            confirm.setOnClickListener(this);
            skip.setOnClickListener(this);
            pieChart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(getAdapterPosition()!= RecyclerView.NO_POSITION){
                        mSurveyQ = mQArrayList.get(getAdapterPosition());
                        showPieChart(mSurveyQ ,pieChart);
                    }

                }
            });
            Log.d("SurveyAdapter123r", "SP view " );

        }

        @Override
        public void onClick(View v) {
            if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                mSurveyQ = mQArrayList.get(getAdapterPosition());
            }
            if (mSurveyQ != null) {
                mProgressBar.setVisibility(View.VISIBLE);
                skip.setVisibility(View.GONE);
                confirm.setVisibility(View.GONE);
                if (v == confirm) {

                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            uploadtoDatabase(mSurveyQ, skip,confirm, mSpinner.getSelectedItem().toString(),
                                    mProgressBar, check,
                                    comment, pieChart, stamp);

                        }
                    }, 666);

                } else if (v == skip) {

                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            uploadtoDatabase(mSurveyQ, skip,confirm,"skipped",
                                    mProgressBar, check,
                                    comment, pieChart, stamp);

                        }
                    }, 666);

                }
            }
        }
    }

    class FeedbackViewHolder extends RecyclerView.ViewHolder {
        TextView question;
        public FeedbackViewHolder(View itemView) {
            super(itemView);

        }
    }


    void uploadtoDatabase(final SurveyQ surveyQ, final TextView skip, final TextView confirm,  final String answer, final ProgressBar progressBar,
                          final ImageView check, final TextView comment, final ImageView pieChart, final ImageView stamp){




        SharedPreferences sharedPref = mContext.getSharedPreferences( "SurveyDsk", Context.MODE_PRIVATE);
       final  SharedPreferences.Editor editor = sharedPref.edit();


        //read sharedPref
        Boolean dsk = sharedPref.getBoolean("SurveyDsk", false);



        // show check dialog if dks is false


        if(!dsk) {
            new MaterialDialog.Builder(mContext)
                    .title("Reminder")
                    .content("Once the answer is submitted, you can't change the answer in next 48 hours")
                    .positiveText(R.string.ok)
                    .negativeText(R.string.cancel).negativeColor(ContextCompat.getColor(mContext,R.color.black))
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            progressBar.setVisibility(View.GONE);
                            skip.setVisibility(View.VISIBLE);
                            confirm.setVisibility(View.VISIBLE);
                        }
                    })
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            // upload the answer
                            final  UserQA userQA = new UserQA();
                            userQA.setQuestionId(surveyQ.getQuestionId());
                            userQA.setQuestion(surveyQ.getQuestion());
                            userQA.setAnswer(answer);
                            userQA.setReset(new Date().getTime()+
                                    (DAYS));

                            stamp.setVisibility("skipped".equals(answer)? View.VISIBLE : View.GONE);


                            final  DatabaseReference userQARef = FirebaseDatabase.getInstance().getReference()
                                    .child("UserQA")
                                    .child(currentUser.getUid())
                                    .child(userQA.getQuestionId());

                                    userQARef.setValue(userQA).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            check.setVisibility(View.VISIBLE);
                                            progressBar.setVisibility(View.GONE);

                                            mHashMap.put(surveyQ,answer);
                                            mListener.onClick();
                                            comment.setVisibility(View.VISIBLE);
                                            pieChart.setVisibility(View.VISIBLE);

                                            //show tutorial for piechart
                                            final FancyShowCaseView fancyShowCaseView1= new FancyShowCaseView.Builder(mActivity)
                                                    .focusOn(pieChart)
                                                    .title("Click here see the overall result for this question")
                                                    .showOnce("tutorialPiechart")
                                                    .focusShape(FocusShape.ROUNDED_RECTANGLE)
                                                    .delay(300)
                                                    .fitSystemWindows(true)
                                                    .roundRectRadius(90)
                                                    .build();


                                            //show tutorial for comment
                                            final FancyShowCaseView fancyShowCaseView2= new FancyShowCaseView.Builder(mActivity)
                                                    .focusOn(comment)
                                                    .title("Click here to add comment or view other user's comment")
                                                    .showOnce("tutorialComment")
                                                    .delay(300)
                                                    .fitSystemWindows(true)
                                                    .focusShape(FocusShape.ROUNDED_RECTANGLE)
                                                    .roundRectRadius(90)
                                                    .build();
                                            new FancyShowCaseQueue()
                                                    .add(fancyShowCaseView1)
                                                    .add(fancyShowCaseView2).show();




                                            // update score with all the friends
                                            mHandler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                   compareWithFriends();
                                                }
                                            },300);
                                        }
                                    });



                        }
                    })
                    .checkBoxPromptRes(R.string.dont_ask_again, false, new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) {
                                editor.putBoolean("SurveyDsk", isChecked);
                                editor.commit();
                            } else {
                                editor.putBoolean("SurveyDsk", isChecked);
                                editor.commit();
                            }
                        }
                    })
                    .show();
    // don't show agian is checked
        }else{
            // upload the answer
            final  UserQA userQA = new UserQA();
            userQA.setQuestionId(surveyQ.getQuestionId());
            userQA.setQuestion(surveyQ.getQuestion());
            userQA.setAnswer(answer);
            userQA.setReset(new Date().getTime()+
                    (DAYS));


            stamp.setVisibility("skipped".equals(answer)? View.VISIBLE : View.GONE);


            final  DatabaseReference userQARef = FirebaseDatabase.getInstance().getReference()
                    .child("UserQA")
                    .child(currentUser.getUid())
                    .child(userQA.getQuestionId());

            userQARef.setValue(userQA).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    check.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    mHashMap.put(surveyQ,answer);
                    mListener.onClick();
                    comment.setVisibility(View.VISIBLE);
                    pieChart.setVisibility(View.VISIBLE);

                    //show tutorial for piechart
                    final FancyShowCaseView fancyShowCaseView1= new FancyShowCaseView.Builder(mActivity)
                            .focusOn(pieChart)
                            .title("Click here see the overall result for this question")
                            .showOnce("tutorialPiechart")
                            .delay(300)
                            .focusShape(FocusShape.ROUNDED_RECTANGLE)
                            .roundRectRadius(90)
                            .fitSystemWindows(true)
                            .build();


                    //show tutorial for comment
                    final FancyShowCaseView fancyShowCaseView2= new FancyShowCaseView.Builder(mActivity)
                            .focusOn(comment)
                            .title("Click here to add comment or view other user's comment")
                            .showOnce("tutorialComment")
                            .focusShape(FocusShape.ROUNDED_RECTANGLE)
                            .delay(300)
                            .roundRectRadius(90)
                            .fitSystemWindows(true)
                            .build();
                    new FancyShowCaseQueue()
                            .add(fancyShowCaseView1)
                            .add(fancyShowCaseView2).show();




                    // update score with all the friends
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            compareWithFriends();
                        }
                    },300);
                }
            });

        }






    }

    void getComments( final SurveyQ surveyq, final TextView comment){
        final String questionId = surveyq.getQuestionId();


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("Comments")
                .child(surveyq.getQuestionId());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String str =  dataSnapshot.getChildrenCount()>0 ? ""+dataSnapshot.getChildrenCount():"";
                comment.setText("Comment " +str);



                //Todo add tutorial here

                comment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(mContext, QuestionActivity.class);
                        i.putExtra("questionId",questionId);
                        mContext.startActivity(i);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    void showPieChart(final SurveyQ surveyQ, final ImageView pieChartBtn){
       final  Dialog pieDialg = new Dialog(mContext);;
        pieDialg.setCanceledOnTouchOutside(true);
        pieDialg.setContentView(R.layout.result_dialog);









                PieChart mPieChart = pieDialg.findViewById(R.id.chart);
                ImageView close = pieDialg.findViewById(R.id.result_dialog_close);
                ProgressBar mProgressBar = pieDialg.findViewById(R.id.result_dialog_progressBar);
                mProgressBar.setVisibility(View.VISIBLE);
                 pieDialg.show();





                mPieChart.setVisibility(View.GONE);
                loadPieChart(mPieChart,mProgressBar,surveyQ);

                close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {

                            pieDialg.dismiss();

                        }catch(Exception e){

                        }



                    }
                });




            }









    void loadPieChart(final PieChart mPieChart,  final ProgressBar mProgressBar, final SurveyQ surveyQ){


        mPieChart.setUsePercentValues(true);
        mPieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        mPieChart.setUsePercentValues(true);

        mPieChart.getDescription().setEnabled(false);
        mPieChart.setExtraOffsets(5, 10, 5, 5);

        mPieChart.setDragDecelerationFrictionCoef(0.95f);



        mPieChart.setDrawHoleEnabled(true);
        mPieChart.setHoleColor(Color.WHITE);

        mPieChart.setTransparentCircleColor(Color.WHITE);
        mPieChart.setTransparentCircleAlpha(110);

        mPieChart.setHoleRadius(58f);
        mPieChart.setTransparentCircleRadius(51f);

        mPieChart.setDrawCenterText(true);

        mPieChart.setRotationAngle(0);

        mPieChart.setRotationEnabled(true);
        mPieChart.setHighlightPerTapEnabled(false);

        Legend l = mPieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setWordWrapEnabled(true);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(1f);
        l.setYOffset(8f);

        // entry label styling
        mPieChart.setEntryLabelColor(ContextCompat.getColor(mContext,R.color.dark_gray));
        mPieChart.setEntryLabelTextSize(12f);


        final HashMap<String,Integer> answersMap= new HashMap<>();




            final  String question = surveyQ.getQuestion();
            final  String questionId = surveyQ.getQuestionId();



                // search user's answer
                DatabaseReference answersRef = FirebaseDatabase .getInstance().getReference()
                        .child("UserQA");

                answersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot childSnap : dataSnapshot.getChildren()){
                            if(childSnap.hasChild(questionId)){

                                String answer = childSnap.
                                        child(questionId).
                                        child("answer").getValue(String.class);

                                // set up answer map default value is 0 else increment by 1

                                if(answersMap.containsKey(answer)){
                                    int count = answersMap.get(answer);
                                    answersMap.put(answer, ++count );
                                }else{
                                    answersMap.put(answer, 1 );
                                }
                            }
                        }
                        Integer total =0;
                        for(String str : answersMap.keySet()){
                            total += answersMap.get(str);

                        }
                        String user = answersMap.size()>1 ?" users" :" user";
                        String numText = "\n(" + total + user+" have answered this question)";

                        CharSequence string  = Html.fromHtml("<font color=\"#3c3e42\">" + numText+ "</font>");
                        String centerText = question +"\n"+ string;
                        mPieChart.setCenterText(centerText);


                        List<PieEntry> entries = new ArrayList<>();
                        if(total>0){
                            PieEntry pieEntry;
                            Integer count ;
                            Float result;
                            for(String str: answersMap.keySet()){
                                count = answersMap.get(str);
                                Log.d("Result_Frag"," count is "+ count);
                                result = ((float)count)/(float)total;
                                Log.d("Result_Frag"," result is "+ result);

                                pieEntry = new PieEntry(result,str);
                                entries.add(pieEntry);

                            }
                        }


                        Log.d("Result_Frag",""+entries.size());

                        // add a lot of colors

                        ArrayList<Integer> colors = new ArrayList<Integer>();

                        for (int c : ColorTemplate.VORDIPLOM_COLORS)
                            colors.add(c);

                        for (int c : ColorTemplate.JOYFUL_COLORS)
                            colors.add(c);

                        for (int c : ColorTemplate.COLORFUL_COLORS)
                            colors.add(c);

                        for (int c : ColorTemplate.LIBERTY_COLORS)
                            colors.add(c);

                        for (int c : ColorTemplate.PASTEL_COLORS)
                            colors.add(c);
                        colors.add(ColorTemplate.getHoloBlue());


                        PieDataSet dataSet = new PieDataSet(entries, "");
                        dataSet.setColors(colors);
                        dataSet.setDrawIcons(false);

                        dataSet.setSliceSpace(3f);
                        dataSet.setIconsOffset(new MPPointF(0, 40));
                        dataSet.setSelectionShift(5f);



                        PieData data = new PieData(dataSet);
                        data.setValueFormatter(new PercentFormatter());
                        data.setValueTextSize(11f);
                        data.setValueTextColor(Color.BLACK);
                        data.setValueTextSize(11f);
                        mProgressBar.setVisibility(View.GONE);

                        mPieChart.setData(data);
                        mPieChart.setVisibility(View.VISIBLE);
                        mPieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
                        mPieChart.invalidate(); // refresh
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }







    public void compareWithFriends() {

        DatabaseReference mFriendRef = FirebaseDatabase.getInstance().getReference()
                .child("UserFriends").child(currentUser.getUid());
        mFriendRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                    if( childSnapshot.hasChild("stats") &&
                            childSnapshot.child("stats").getValue(String.class).equals("accepted")){
                        compareWithUser2(childSnapshot.getKey());
                    }
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public void compareWithUser2(final String user2Uid) {
        final ArrayList<UserQA> userQA1 = new ArrayList<>();
        final ArrayList<UserQA> userQA2 = new ArrayList<>();
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("UserQA/" + currentUser.getUid());
        final DatabaseReference mRef2 = FirebaseDatabase.getInstance().getReference("UserQA/" + user2Uid);

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child : dataSnapshot.getChildren()){
                    if( !"skipped".equals(child.getValue(UserQA.class).getAnswer())){
                        userQA1.add(child.getValue(UserQA.class));
                    }

                }


                mRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot child : dataSnapshot.getChildren()){
                            if(!"skipped".equals(child.getValue(UserQA.class).getAnswer())){
                                userQA2.add(child.getValue(UserQA.class));
                            }


                        }

                        Compatability mCompatability = new Compatability(userQA1,userQA2);
                        setScoreNode(user2Uid,mCompatability.getScore().toString());



                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    public void setScoreNode(final String user2Uid, final String score){
        DatabaseReference scoreRef = FirebaseDatabase.getInstance().getReference()
                .child("UserFriends")
                .child(currentUser.getUid())
                .child(user2Uid)
                .child("score");
        scoreRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {

                mutableData.setValue(score);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

            }
        });

        DatabaseReference scoreRef2 = FirebaseDatabase.getInstance().getReference()
                .child("UserFriends")
                .child(user2Uid)
                .child(currentUser.getUid())
                .child("score");

        scoreRef2.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {

                mutableData.setValue(score);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
// Transaction completed
                //Log.d("SurveyAdapter123r", "postTransaction:onComplete:" + databaseError);
            }
        });



    }


    void bindView (final RecyclerView.ViewHolder holder , final String questionId){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("UserQA")
                .child(currentUser.getUid());

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean answered = dataSnapshot.hasChild(questionId);


                if(answered){
                    UserQA userQA = dataSnapshot.child(questionId).getValue(UserQA.class);
                    String answer = userQA.getAnswer();
                    if(holder instanceof McViewHolder){

                        ((McViewHolder) holder).mProgressBar.setVisibility(View.GONE);
                        ((McViewHolder) holder).skip.setVisibility(View.GONE);
                        ((McViewHolder) holder).confirm.setVisibility(View.GONE);
                        ((McViewHolder) holder). check.setVisibility(View.VISIBLE);
                        ((McViewHolder) holder) .comment.setVisibility(View.VISIBLE);
                        ((McViewHolder) holder) .pieChart.setVisibility(View.VISIBLE);
                        ((McViewHolder) holder) .stamp.setVisibility(answer!=null&&  answer.equals("skipped") ? View.VISIBLE :View.GONE);

                    }else if(holder instanceof ScViewHolder){
                        ((ScViewHolder) holder).mProgressBar.setVisibility(View.GONE);
                        ((ScViewHolder) holder).skip.setVisibility(View.GONE);
                        ((ScViewHolder) holder).confirm.setVisibility(View.GONE);
                        ((ScViewHolder) holder). check.setVisibility(View.VISIBLE);
                        ((ScViewHolder) holder) .comment.setVisibility(View.VISIBLE);
                        ((ScViewHolder) holder) .pieChart.setVisibility(View.VISIBLE);
                        ((ScViewHolder) holder) .stamp.setVisibility(answer!=null&&answer.equals("skipped") ? View.VISIBLE :View.GONE);
                    }else if(holder instanceof  SpViewHolder){
                        ((SpViewHolder) holder).mProgressBar.setVisibility(View.GONE);
                        ((SpViewHolder) holder).skip.setVisibility(View.GONE);
                        ((SpViewHolder) holder).confirm.setVisibility(View.GONE);
                        ((SpViewHolder) holder). check.setVisibility(View.VISIBLE);
                        ((SpViewHolder) holder) .comment.setVisibility(View.VISIBLE);
                        ((SpViewHolder) holder) .pieChart.setVisibility(View.VISIBLE);
                        ((SpViewHolder) holder) .stamp.setVisibility( answer!=null && answer.equals("skipped") ? View.VISIBLE :View.GONE);
                    }



                }else{
                    if(holder instanceof McViewHolder){
                        ((McViewHolder) holder).mProgressBar.setVisibility(View.GONE);
                        ((McViewHolder) holder).skip.setVisibility(View.VISIBLE);
                        ((McViewHolder) holder).confirm.setVisibility(View.VISIBLE);
                        ((McViewHolder) holder). check.setVisibility(View.GONE);
                        ((McViewHolder) holder) .comment.setVisibility(View.GONE);
                        ((McViewHolder) holder) .pieChart.setVisibility(View.GONE);
                        ((McViewHolder) holder) .stamp.setVisibility(View.GONE);
                    }else if(holder instanceof ScViewHolder){
                        ((ScViewHolder) holder).mProgressBar.setVisibility(View.GONE);
                        ((ScViewHolder) holder).skip.setVisibility(View.VISIBLE);
                        ((ScViewHolder) holder).confirm.setVisibility(View.VISIBLE);
                        ((ScViewHolder) holder). check.setVisibility(View.GONE);
                        ((ScViewHolder) holder) .comment.setVisibility(View.GONE);
                        ((ScViewHolder) holder) .pieChart.setVisibility(View.GONE);
                        ((ScViewHolder) holder) .stamp.setVisibility(View.GONE);
                    }else if(holder instanceof  SpViewHolder){
                        ((SpViewHolder) holder).mProgressBar.setVisibility(View.GONE);
                        ((SpViewHolder) holder).skip.setVisibility(View.VISIBLE);
                        ((SpViewHolder) holder).confirm.setVisibility(View.VISIBLE);
                        ((SpViewHolder) holder). check.setVisibility(View.GONE);
                        ((SpViewHolder) holder) .comment.setVisibility(View.GONE);
                        ((SpViewHolder) holder) .pieChart.setVisibility(View.GONE);
                        ((SpViewHolder) holder) .stamp.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    void getTutorialSettings(){
        SharedPreferences sharedPref = mContext.getSharedPreferences(
                "tutorial", Context.MODE_PRIVATE);
       // tutorialConfirm ,tutorialPieChart,tutorialForward,tutorialComment,tutorialFab,tutorialSkip
        tutorialComment = sharedPref.getBoolean("tutorialComment", false);
        tutorialPieChart = sharedPref.getBoolean("tutorialPieChart", false);
        tutorialConfirm = sharedPref.getBoolean("tutorialPieChart", false);
        tutorialSkip = sharedPref.getBoolean("tutorialPieChart", false);
        }


   public interface SubmitedListener{
        void onClick();


    }


}
