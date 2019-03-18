package app.jayang.icebr8k.Adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AlertDialog;
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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.xw.repo.BubbleSeekBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import app.jayang.icebr8k.Model.SurveyQ;
import app.jayang.icebr8k.Model.UserQA;
import app.jayang.icebr8k.QuestionActivity;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.Utility.EmailUtil;
import app.jayang.icebr8k.Utility.MyToolBox;
import app.jayang.icebr8k.Utility.MyXAxisValueFormatter;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetSequence;

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
    private final long DAYS = 60 * 60 * 48 * 1000;  // 2 DAYS
    private Boolean tutorialConfirm, tutorialPieChart, tutorialComment, tutorialSkip, tutorialWelcome;
    private ArrayList<SurveyQ> mQArrayList;
    private Context mContext;
    private Activity mActivity;
    private SubmitedListener mListener;
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private android.os.Handler mHandler = new android.os.Handler();
    private HashMap<SurveyQ, String> mHashMap = new HashMap<>();
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;


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

        } else if (viewType == SC) {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_survey_sc, parent, false);

            return new ScViewHolder(v);
        } else if (viewType == SP) {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_survey_sp, parent, false);

            return new SpViewHolder(v);
        }


        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        SurveyQ surveyQ = mQArrayList.get(position);

        Log.d("SurveyAdapter123r", "binding view " + position + " " + surveyQ.getQuestion());

        if (holder instanceof McViewHolder) {
            RadioGroup mRadioGroup = ((McViewHolder) holder).mRadioGroup;
            ((McViewHolder) holder).question.setText(surveyQ.getQuestion());
            mRadioGroup.clearCheck();
            mRadioGroup.removeAllViews();


            String answer = null;
            if (mHashMap.get(surveyQ) != null) {
                answer = mHashMap.get(surveyQ);
                ((McViewHolder) holder).stamp.setVisibility("skipped".equals(answer) ? View.VISIBLE : View.GONE);

            }
            //Ui Stuff add radio buttons
            for (int i = 0; i < surveyQ.getAnswer().size(); i++) {
                RadioButton button = new RadioButton(mContext);
                button.setText(surveyQ.getAnswer().get(i));
                ((McViewHolder) holder).mRadioGroup.addView(button);
                if (answer != null && !"skipped".equals(answer) && surveyQ.getAnswer().get(i).toString().equals(answer)) {
                    ((RadioButton) mRadioGroup.getChildAt(i)).setChecked(true);
                }
            }

            getComments(surveyQ, ((McViewHolder) holder).comment);

        } else if (holder instanceof ScViewHolder) {
            ((ScViewHolder) holder).question.setText(surveyQ.getQuestion());

            String answer = mHashMap.get(surveyQ);
            if (answer != null) {
                ((ScViewHolder) holder).stamp.setVisibility("skipped".equals(answer) ? View.VISIBLE : View.GONE);
            }

            if (answer != null && !"skipped".equals(answer)) {
                Float f = Float.valueOf(mHashMap.get(surveyQ));
                ((ScViewHolder) holder).mSeekBar.setProgress(f);
            } else {
                ((ScViewHolder) holder).mSeekBar.setProgress(5f);

            }
            getComments(surveyQ, ((ScViewHolder) holder).comment);


        } else if (holder instanceof SpViewHolder) {
            ((SpViewHolder) holder).question.setText(surveyQ.getQuestion());
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_dropdown_item, surveyQ.getAnswer());
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            ((SpViewHolder) holder).mSpinner.setAdapter(adapter);
            ((SpViewHolder) holder).mSpinner.setVisibility(View.VISIBLE);
            String answer = mHashMap.get(surveyQ);

            ((SpViewHolder) holder).stamp.setVisibility("skipped".equals(answer) ? View.VISIBLE : View.GONE);

            if (answer != null && !"skipped".equals(answer)) {
                for (int i = 0; i < surveyQ.getAnswer().size(); i++) {
                    if (surveyQ.getAnswer().get(i).equals(answer)) {
                        ((SpViewHolder) holder).mSpinner.setSelection(i);
                    }
                }
            }
            getComments(surveyQ, ((SpViewHolder) holder).comment);


        }

        bindView(holder, surveyQ.getQuestionId());

    }

    @Override
    public int getItemCount() {
        return mQArrayList.size();
    }


    @Override
    public int getItemViewType(int position) {
        SurveyQ surveyQ = mQArrayList.get(position);
        String type = surveyQ.getType();

        switch (type) {
            case MC_STR:
                return MC;

            case SC_STR:
                return SC;

            case SP_STR:
                return SP;

            case FEEDBACK_STR:
                return FEEDBACK;


        }
        return -1;
    }

    void uploadtoDatabase(final SurveyQ surveyQ, final TextView skip, final TextView confirm, final String answer, final ProgressBar progressBar,
                          final ImageView check, final TextView comment, final TextView pieChart, final ImageView stamp) {


        SharedPreferences sharedPref = mContext.getSharedPreferences("SurveyDsk", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();


        //read sharedPref
        Boolean dsk = sharedPref.getBoolean("SurveyDsk", false);


        // show check dialog if dks is false


        if (!dsk) {
            new MaterialDialog.Builder(mContext)
                    .title("Reminder")
                    .canceledOnTouchOutside(false)
                    .content("Once the answer is submitted, you can't change the answer in next 48 hours")
                    .positiveText(R.string.ok)
                    .negativeText(R.string.cancel).negativeColor(ContextCompat.getColor(mContext, R.color.black))
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
                            final UserQA userQA = new UserQA();
                            userQA.setQuestionId(surveyQ.getQuestionId());
                            userQA.setQuestion(surveyQ.getQuestion());
                            userQA.setAnswer(answer);
                            userQA.setReset(new Date().getTime() +
                                    (DAYS));


                            final DatabaseReference userQARef = FirebaseDatabase.getInstance().getReference()
                                    .child("UserQA")
                                    .child(currentUser.getUid())
                                    .child(userQA.getQuestionId());

                            userQARef.setValue(userQA).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressBar.setVisibility(View.GONE);
                                    comment.setVisibility(View.VISIBLE);
                                    pieChart.setVisibility(View.VISIBLE);
                                    check.setVisibility(View.VISIBLE);
                                    progressBar.setVisibility(View.GONE);
                                    YoYo.with(Techniques.FadeIn).playOn(comment);
                                    YoYo.with(Techniques.FadeIn).playOn(pieChart);
                                    stamp.setVisibility("skipped".equals(answer) ? View.VISIBLE : View.GONE);
                                    mHashMap.put(surveyQ, answer);
                                    mListener.onClick(surveyQ, answer);
                                    //show tutorial for piechart and comments
                                    showPieChartCommentPrompt(comment, pieChart);


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
        } else {


            // upload the answer
            final UserQA userQA = new UserQA();
            userQA.setQuestionId(surveyQ.getQuestionId());
            userQA.setQuestion(surveyQ.getQuestion());
            userQA.setAnswer(answer);
            userQA.setReset(new Date().getTime() +
                    (DAYS));


            final DatabaseReference userQARef = FirebaseDatabase.getInstance().getReference()
                    .child("UserQA")
                    .child(currentUser.getUid())
                    .child(userQA.getQuestionId());

            userQARef.setValue(userQA).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    mHashMap.put(surveyQ, answer);
                    mListener.onClick(surveyQ, answer);
                    comment.setVisibility(View.VISIBLE);
                    pieChart.setVisibility(View.VISIBLE);
                    YoYo.with(Techniques.FadeIn).playOn(comment);
                    YoYo.with(Techniques.FadeIn).playOn(pieChart);
                    check.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    stamp.setVisibility("skipped".equals(answer) ? View.VISIBLE : View.GONE);

                    //show tutorial for piechart and comments
                    showPieChartCommentPrompt(comment, pieChart);


                }
            });

        }


    }

    void getComments(final SurveyQ surveyq, final TextView comment) {
        final String questionId = surveyq.getQuestionId();


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("Comments")
                .child(surveyq.getQuestionId());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String str = dataSnapshot.getChildrenCount() > 0 ? "" + dataSnapshot.getChildrenCount() : "";
                comment.setText("Comment " + str);

                comment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(mContext, QuestionActivity.class);
                        i.putExtra("questionId", questionId);
                        mContext.startActivity(i);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    void showPieChart(final SurveyQ surveyQ) {
        final Dialog pieDialg = new Dialog(mContext);

        pieDialg.setCanceledOnTouchOutside(true);
        pieDialg.setContentView(R.layout.result_dialog);
        TextView textView = pieDialg.findViewById(R.id.centerText);


        BarChart mChart = pieDialg.findViewById(R.id.chart);
        ImageView close = pieDialg.findViewById(R.id.result_dialog_close);
        ProgressBar mProgressBar = pieDialg.findViewById(R.id.result_dialog_progressBar);
        mProgressBar.setVisibility(View.VISIBLE);
        pieDialg.show();


        mChart.setVisibility(View.GONE);
        loadPieChart(mChart, mProgressBar, surveyQ, textView);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    pieDialg.dismiss();

                } catch (Exception e) {

                }


            }
        });


    }

    // if is SC question we will group them
    public void setSCMap(String answer, HashMap<String, Integer> answersMap) {
        if (Integer.valueOf(answer) >= 1 && Integer.valueOf(answer) <= 2) {
            String key = "(1-2)";
            if (answersMap.containsKey(answer)) {
                int count = answersMap.get(answer);

                answersMap.put(key, ++count);
            } else {
                answersMap.put(key, 1);
            }
        } else if (Integer.valueOf(answer) >= 3 && Integer.valueOf(answer) <= 5) {
            String key = "(3-5)";
            if (answersMap.containsKey(answer)) {
                int count = answersMap.get(answer);

                answersMap.put(key, ++count);
            } else {
                answersMap.put(key, 1);
            }
        } else if (Integer.valueOf(answer) >= 6 && Integer.valueOf(answer) <= 8) {
            String key = "(6-8)";
            if (answersMap.containsKey(answer)) {
                int count = answersMap.get(answer);

                answersMap.put(key, ++count);
            } else {
                answersMap.put(key, 1);
            }
        } else if (Integer.valueOf(answer) >= 9 && Integer.valueOf(answer) <= 10) {
            String key = "(9-10)";
            if (answersMap.containsKey(answer)) {
                int count = answersMap.get(answer);

                answersMap.put(key, ++count);
            } else {
                answersMap.put(key, 1);
            }
        }
    }

    void loadPieChart(final BarChart mChart, final ProgressBar mProgressBar, final SurveyQ surveyQ, final TextView textView) {


        final HashMap<String, Integer> answersMap = new HashMap<>();


        final String question = surveyQ.getQuestion();
        final String questionId = surveyQ.getQuestionId();


        // search user's answer
        DatabaseReference answersRef = FirebaseDatabase.getInstance().getReference()
                .child("UserQA");

        answersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnap : dataSnapshot.getChildren()) {
                    if (childSnap.hasChild(questionId)) {

                        String answer = childSnap.
                                child(questionId).
                                child("answer").getValue(String.class);

                        // is sc question
                        if (MyToolBox.isInteger(answer)) {
                            setSCMap(answer, answersMap);
                        } else {
                            // set up answer map default value is 0 else increment by 1

                            if (answersMap.containsKey(answer)) {
                                int count = answersMap.get(answer);
                                answersMap.put(answer, ++count);
                            } else {
                                answersMap.put(answer, 1);
                            }
                        }
                    }
                }

                Integer total = 0;
                for (String str : answersMap.keySet()) {
                    total += answersMap.get(str);

                }
                String user = answersMap.size() > 1 ? " users" : " user";
                String numText = "\n(" + total + user + " have answered this question)";

                CharSequence string = Html.fromHtml("<font color=\"#3c3e42\">" + numText + "</font>");
                String centerText = question + "\n" + string;
                textView.setText(centerText);


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


                List<BarEntry> entries = new ArrayList<>();
                ArrayList<String> xAxisStrings = new ArrayList<>();

                if (total > 0) {
                    BarEntry barEntry;
                    Integer count, index = 0;
                    Float result;
                    ArrayList<String> answerList = new ArrayList(answersMap.keySet());
                    Collections.sort(answerList);

                    for (String str : answerList) {
                        xAxisStrings.add(str);
                        count = answersMap.get(str);
                        Log.d("Result_Frag", " count is " + count);
                        result = (((float) count) / (float) total) * 100;
                        Log.d("Result_Frag", " result is " + result);

                        barEntry = new BarEntry((float) index, result);
                        index++;
                        entries.add(barEntry);


                    }
                }

                XAxis xAxis = mChart.getXAxis();

                xAxis.setValueFormatter(new MyXAxisValueFormatter(xAxisStrings));
                xAxis.setDrawAxisLine(true);
                xAxis.setDrawGridLines(false);
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setGranularity(1f);

                YAxis yAxis = mChart.getAxisLeft();

                yAxis.setValueFormatter(new PercentFormatter());
                yAxis.setDrawGridLines(false);
                BarDataSet set = new BarDataSet(entries, "BarDataSet");
                set.setColors(colors);
                set.setValueFormatter(new PercentFormatter());
                set.setValueTextSize(8f);
                BarData data = new BarData(set);
                data.setBarWidth(0.9f); // set custom bar width
                mChart.setData(data);
                mChart.setScaleEnabled(false);
                mChart.setDrawGridBackground(false);
                mChart.setDrawValueAboveBar(true);
                mChart.setFitBars(true); // make the x-axis fit exactly all bars
                mChart.getAxisRight().setEnabled(false);
                mChart.getDescription().setEnabled(false);
                mChart.getLegend().setEnabled(false);
                mChart.setTouchEnabled(false);
                xAxis.setLabelRotationAngle(-60f);
                mChart.animateXY(1000, 1000);
                mProgressBar.setVisibility(View.GONE);
                mChart.setVisibility(View.VISIBLE);
                mChart.invalidate(); // refresh

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    void bindView(final RecyclerView.ViewHolder holder, final String questionId) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("UserQA")
                .child(currentUser.getUid());

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean answered = dataSnapshot.hasChild(questionId);


                if (answered) {
                    UserQA userQA = dataSnapshot.child(questionId).getValue(UserQA.class);
                    String answer = userQA.getAnswer();
                    if (holder instanceof McViewHolder) {

                        ((McViewHolder) holder).mProgressBar.setVisibility(View.GONE);
                        ((McViewHolder) holder).skip.setVisibility(View.GONE);
                        ((McViewHolder) holder).confirm.setVisibility(View.GONE);
                        ((McViewHolder) holder).check.setVisibility(View.VISIBLE);
                        ((McViewHolder) holder).comment.setVisibility(View.VISIBLE);
                        ((McViewHolder) holder).pieChart.setVisibility(View.VISIBLE);
                        ((McViewHolder) holder).stamp.setVisibility(answer != null && answer.equals("skipped") ? View.VISIBLE : View.GONE);


                    } else if (holder instanceof ScViewHolder) {
                        ((ScViewHolder) holder).mProgressBar.setVisibility(View.GONE);
                        ((ScViewHolder) holder).skip.setVisibility(View.GONE);
                        ((ScViewHolder) holder).confirm.setVisibility(View.GONE);
                        ((ScViewHolder) holder).check.setVisibility(View.VISIBLE);
                        ((ScViewHolder) holder).comment.setVisibility(View.VISIBLE);
                        ((ScViewHolder) holder).pieChart.setVisibility(View.VISIBLE);
                        ((ScViewHolder) holder).stamp.setVisibility(answer != null && answer.equals("skipped") ? View.VISIBLE : View.GONE);

                    } else if (holder instanceof SpViewHolder) {
                        ((SpViewHolder) holder).mProgressBar.setVisibility(View.GONE);
                        ((SpViewHolder) holder).skip.setVisibility(View.GONE);
                        ((SpViewHolder) holder).confirm.setVisibility(View.GONE);
                        ((SpViewHolder) holder).check.setVisibility(View.VISIBLE);
                        ((SpViewHolder) holder).comment.setVisibility(View.VISIBLE);
                        ((SpViewHolder) holder).pieChart.setVisibility(View.VISIBLE);
                        ((SpViewHolder) holder).stamp.setVisibility(answer != null && answer.equals("skipped") ? View.VISIBLE : View.GONE);

                    }


                } else {
                    if (holder instanceof McViewHolder) {
                        ((McViewHolder) holder).mProgressBar.setVisibility(View.GONE);
                        ((McViewHolder) holder).skip.setVisibility(View.VISIBLE);
                        ((McViewHolder) holder).confirm.setVisibility(View.VISIBLE);
                        ((McViewHolder) holder).check.setVisibility(View.GONE);
                        ((McViewHolder) holder).comment.setVisibility(View.GONE);
                        ((McViewHolder) holder).pieChart.setVisibility(View.GONE);
                        ((McViewHolder) holder).stamp.setVisibility(View.GONE);
                    } else if (holder instanceof ScViewHolder) {
                        ((ScViewHolder) holder).mProgressBar.setVisibility(View.GONE);
                        ((ScViewHolder) holder).skip.setVisibility(View.VISIBLE);
                        ((ScViewHolder) holder).confirm.setVisibility(View.VISIBLE);
                        ((ScViewHolder) holder).check.setVisibility(View.GONE);
                        ((ScViewHolder) holder).comment.setVisibility(View.GONE);
                        ((ScViewHolder) holder).pieChart.setVisibility(View.GONE);
                        ((ScViewHolder) holder).stamp.setVisibility(View.GONE);
                    } else if (holder instanceof SpViewHolder) {
                        ((SpViewHolder) holder).mProgressBar.setVisibility(View.GONE);
                        ((SpViewHolder) holder).skip.setVisibility(View.VISIBLE);
                        ((SpViewHolder) holder).confirm.setVisibility(View.VISIBLE);
                        ((SpViewHolder) holder).check.setVisibility(View.GONE);
                        ((SpViewHolder) holder).comment.setVisibility(View.GONE);
                        ((SpViewHolder) holder).pieChart.setVisibility(View.GONE);
                        ((SpViewHolder) holder).stamp.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    void getTutorialSettings() {
        sharedPref = mContext.getSharedPreferences(
                "tutorial", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        tutorialComment = sharedPref.getBoolean("tutorialComment", true);
        tutorialPieChart = sharedPref.getBoolean("tutorialPieChart", true);
        tutorialConfirm = sharedPref.getBoolean("tutorialConfirm", true);
        tutorialSkip = sharedPref.getBoolean("tutorialSkip", true);
        tutorialWelcome = sharedPref.getBoolean("tutorialWelcome", true);
    }

    void showfirstLoginPrompt(final View confirm, final View skip) {

        // welcome message
        if (tutorialWelcome) {
            Log.d("surveyTab123", "tutorialWelcome");

            AlertDialog.Builder mBuilder = new AlertDialog.Builder(mActivity);
            View mView = mActivity.getLayoutInflater().inflate(R.layout.dialog_welcome,null);
            mBuilder.setView(mView);
            final AlertDialog dialog = mBuilder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            mView.findViewById(R.id.welcome_dialog_okay).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    editor.putBoolean("tutorialWelcome", false);
                    editor.commit();
                    tutorialWelcome = false;
                    //show  tutorial basic
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (tutorialConfirm && tutorialSkip) {
                                // show tutorial if necessary
                                new MaterialTapTargetSequence()
                                        .addPrompt(new MaterialTapTargetPrompt.Builder(mActivity)
                                                .setTarget(confirm)
                                                .setBackgroundColour(ContextCompat.getColor(mContext, R.color.colorPrimary))
                                                .setPrimaryText(mContext.getString(R.string.tutorial_confirm_questions_title))
                                                .setSecondaryText(mContext.getString(R.string.tutorial_confirm_questions_content))
                                                .create(), 8000)
                                        .addPrompt(new MaterialTapTargetPrompt.Builder(mActivity)
                                                .setTarget(skip)
                                                .setBackgroundColour(ContextCompat.getColor(mContext, R.color.colorPrimary))
                                                .setPrimaryText(mContext.getString(R.string.tutorial_skip_questions_title))
                                                .setSecondaryText(mContext.getString(R.string.tutorial_skip_questions_content))
                                                .setAnimationInterpolator(new LinearOutSlowInInterpolator())
                                                .create(), 8000)
                                        .show();

                                editor.putBoolean("tutorialConfirm", false);
                                editor.putBoolean("tutorialSkip", false);
                                editor.commit();
                                tutorialConfirm = false;
                                tutorialSkip = false;
                            }

                        }
                    }, 666);
                }
            });








        }


    }

    void showPieChartCommentPrompt(final View comment, final View pieChart) {
        if (tutorialComment && tutorialPieChart) {
            //show  tutorial basic
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // show tutorial if necessary
                    new MaterialTapTargetSequence()
                            .addPrompt(new MaterialTapTargetPrompt.Builder(mActivity)
                                    .setTarget(comment)
                                    .setBackgroundColour(ContextCompat.getColor(mContext, R.color.colorPrimary))
                                    .setPrimaryText(mContext.getString(R.string.tutorial_discuss_title))
                                    .setSecondaryText(mContext.getString(R.string.tutorial_discuss_content))
                                    .create(), 8000)
                            .addPrompt(new MaterialTapTargetPrompt.Builder(mActivity)
                                    .setTarget(pieChart)
                                    .setBackgroundColour(ContextCompat.getColor(mContext, R.color.colorPrimary))
                                    .setPrimaryText(mContext.getString(R.string.tutorial_result_title))
                                    .setSecondaryText(mContext.getString(R.string.tutorial_result_content))
                                    .setAnimationInterpolator(new LinearOutSlowInInterpolator())
                                    .create(), 8000)
                            .show();

                    editor.putBoolean("tutorialComment", false);
                    editor.putBoolean("tutorialPieChart", false);
                    editor.commit();
                    tutorialComment = false;
                    tutorialPieChart = false;


                }
            }, 666);
        }
    }


    public interface SubmitedListener {
        void onClick(SurveyQ surveyQ, String Answer);

    }

    private void showQuestionReportDialog(final SurveyQ surveyQ){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext,R.style.Icebr8kAlertDialogTheme);
       AlertDialog dialog =  builder.setTitle(R.string.question_report_dialog_title)
                .setPositiveButton(R.string.question_report_dialog_pos_btn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new EmailUtil(mContext).sendQuestionReport(FirebaseAuth.getInstance().getCurrentUser(),surveyQ);
                    }
                }).setNegativeButton(R.string.question_report_dialog_neg_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        }).create();
       dialog.setCanceledOnTouchOutside(true);
       dialog.show();
    }

    class McViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView question, confirm, skip, comment, pieChart;
        ImageView check, stamp,report;
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
            report = itemView.findViewById(R.id.survey_mc_report);

            mProgressBar.setVisibility(View.GONE);
            skip.setVisibility(View.VISIBLE);
            confirm.setVisibility(View.VISIBLE);
            check.setVisibility(View.GONE);
            comment.setVisibility(View.GONE);
            pieChart.setVisibility(View.GONE);
            stamp.setVisibility(View.GONE);


            // check to show tutorial or not
            Log.d("surveyTab123", "" + getAdapterPosition());
            showfirstLoginPrompt(confirm, skip);


            confirm.setOnClickListener(this);
            skip.setOnClickListener(this);
            report.setOnClickListener(this);
            pieChart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                        mSurveyQ = mQArrayList.get(getAdapterPosition());
                        showPieChart(mSurveyQ);
                    }

                }
            });
            Log.d("surveyTab123", "MC view ");


        }


        @Override
        public void onClick(View v) {

            if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                mSurveyQ = mQArrayList.get(getAdapterPosition());
            }
            if (mSurveyQ != null) {

                if (v == confirm) {
                    if (mRadioGroup.getCheckedRadioButtonId() == -1) {
                        Toast.makeText(getContext(), "Make a selection", Toast.LENGTH_SHORT).show();

                    } else if (mRadioGroup.getCheckedRadioButtonId() != -1) {
                        skip.setVisibility(View.GONE);
                        confirm.setVisibility(View.GONE);
                        mProgressBar.setVisibility(View.VISIBLE);
                        int id = mRadioGroup.getCheckedRadioButtonId();
                        View radioButton = mRadioGroup.findViewById(id);
                        int radioId = mRadioGroup.indexOfChild(radioButton);
                        RadioButton btn = (RadioButton) mRadioGroup.getChildAt(radioId);
                        final String selection = (String) btn.getText();


                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                uploadtoDatabase(mSurveyQ, skip, confirm, selection,
                                        mProgressBar, check,
                                        comment, pieChart, stamp);

                            }
                        }, 300);

                    }
                } else if (v == skip) {

                    skip.setVisibility(View.GONE);
                    confirm.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.VISIBLE);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            uploadtoDatabase(mSurveyQ, skip, confirm, "skipped",
                                    mProgressBar, check,
                                    comment, pieChart, stamp
                            );

                        }
                    }, 300);

                }else if(v == report){
                   showQuestionReportDialog(mSurveyQ);
                }
            }
        }


    }

    class ScViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView question, confirm, skip, pieChart;
        BubbleSeekBar mSeekBar;
        ProgressBar mProgressBar;
        TextView comment;
        ImageView check, stamp,report;
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
            report = itemView.findViewById(R.id.survey_sc_report);

            mProgressBar.setVisibility(View.GONE);
            skip.setVisibility(View.VISIBLE);
            confirm.setVisibility(View.VISIBLE);
            check.setVisibility(View.GONE);
            comment.setVisibility(View.GONE);
            pieChart.setVisibility(View.GONE);
            stamp.setVisibility(View.GONE);


            // check to show tutorial or not
            Log.d("surveyTab123", "" + getAdapterPosition());
            showfirstLoginPrompt(confirm, skip);


            confirm.setOnClickListener(this);
            report.setOnClickListener(this);
            skip.setOnClickListener(this);
            pieChart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                        mSurveyQ = mQArrayList.get(getAdapterPosition());
                        showPieChart(mSurveyQ);
                    }

                }
            });
            Log.d("SurveyAdapter123r", "SC view ");


        }

        @Override
        public void onClick(View v) {
            if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                mSurveyQ = mQArrayList.get(getAdapterPosition());
            }
            if (mSurveyQ != null) {
                if (v == confirm) {
                    skip.setVisibility(View.GONE);
                    confirm.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.VISIBLE);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            uploadtoDatabase(mSurveyQ, skip, confirm, "" + mSeekBar.getProgress(),
                                    mProgressBar, check,
                                    comment, pieChart, stamp);

                        }
                    }, 300);


                } else if (v == skip) {
                    skip.setVisibility(View.GONE);
                    confirm.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.VISIBLE);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            uploadtoDatabase(mSurveyQ, skip, confirm, "skipped",
                                    mProgressBar, check,
                                    comment, pieChart, stamp
                            );

                        }
                    }, 300);

                }else if(v == report){
                    showQuestionReportDialog(mSurveyQ);
                }
            }
        }
    }

    class SpViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView question, confirm, skip, pieChart;
        ProgressBar mProgressBar;
        TextView comment;
        ImageView check, stamp,report;
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
            report = itemView.findViewById(R.id.survey_sp_report);


            mProgressBar.setVisibility(View.GONE);
            skip.setVisibility(View.VISIBLE);
            confirm.setVisibility(View.VISIBLE);
            check.setVisibility(View.GONE);
            comment.setVisibility(View.GONE);
            pieChart.setVisibility(View.GONE);
            stamp.setVisibility(View.GONE);


            // check to show tutorial or not
            Log.d("surveyTab123", "" + getAdapterPosition());
            showfirstLoginPrompt(confirm, skip);


            confirm.setOnClickListener(this);
            report.setOnClickListener(this);
            skip.setOnClickListener(this);
            pieChart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                        mSurveyQ = mQArrayList.get(getAdapterPosition());
                        showPieChart(mSurveyQ);
                    }

                }
            });
            Log.d("surveyTab123", "SP view ");

        }

        @Override
        public void onClick(View v) {
            if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                mSurveyQ = mQArrayList.get(getAdapterPosition());
            }
            if (mSurveyQ != null) {

                if (v == confirm) {
                    skip.setVisibility(View.GONE);
                    confirm.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.VISIBLE);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            uploadtoDatabase(mSurveyQ, skip, confirm, mSpinner.getSelectedItem().toString(),
                                    mProgressBar, check,
                                    comment, pieChart, stamp);

                        }
                    }, 300);

                } else if (v == skip) {
                    skip.setVisibility(View.GONE);
                    confirm.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.VISIBLE);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            uploadtoDatabase(mSurveyQ, skip, confirm, "skipped",
                                    mProgressBar, check,
                                    comment, pieChart, stamp);

                        }
                    }, 300);

                }else if(v == report){
                    showQuestionReportDialog(mSurveyQ);
                }
            }
        }
    }




}
