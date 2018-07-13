package app.jayang.icebr8k;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.xw.repo.BubbleSeekBar;

import java.util.ArrayList;
import java.util.Date;

import app.jayang.icebr8k.Adapter.ViewPagerAdapter;
import app.jayang.icebr8k.Fragments.Comment_Fragment;
import app.jayang.icebr8k.Fragments.commonFrag;
import app.jayang.icebr8k.Fragments.diffFrag;
import app.jayang.icebr8k.Modle.SurveyQ;
import app.jayang.icebr8k.Modle.UserQA;

public class QuestionActivity extends AppCompatActivity {
    private TextView questionTV, subQuestion,confirmBtn,skipBtn ;
    private TabLayout mLayout;
    private ViewPager mViewPager;
    private Toolbar mToolbar;
    private ImageView stamp;
    private RadioGroup radioGroup;
    private Spinner spinner;
    private String originalAnswer =null;
    private BubbleSeekBar mSeekBar;
    private final long DAYS = 60*60*48*1000;  // 2 DAYS
    private String questionId;
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private final DatabaseReference userQARef = FirebaseDatabase.getInstance().getReference()
            .child("UserQA").child(currentUser.getUid());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        confirmBtn = findViewById(R.id.question_reset);
        radioGroup = findViewById(R.id.radioGroup);
        skipBtn = findViewById(R.id.question_skip);
        questionTV = findViewById(R.id.question_id);
        subQuestion = findViewById(R.id.sub_question_id);
        spinner = findViewById(R.id.spinner_id);
        mSeekBar = findViewById(R.id.seekBar);
        stamp = findViewById(R.id.question_skip_stamp);
        mLayout = findViewById(R.id.question_tablayout);
        mViewPager = findViewById(R.id.question_viewpager);
        mToolbar = findViewById(R.id.question_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        questionId = getIntent().getExtras().getString("questionId",null);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());


        viewPagerAdapter.addFragment(Comment_Fragment.newInstance(questionId));



        mViewPager.setAdapter(viewPagerAdapter);
        mLayout.setupWithViewPager(mViewPager);

        mLayout.getTabAt(0).setText("Comments");
        getCommentCounts();




        Toast.makeText(this, questionId, Toast.LENGTH_SHORT).show();

        //if user has answered question the btn will be reset , else will be confirm

        userQARef .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(questionId!=null  && dataSnapshot.hasChild(questionId)){
                    confirmBtn.setText("Reset");
                    setUI(true);
                }else{
                    confirmBtn.setText("Confirm");
                    if(questionId!=null){
                        setUI(false);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // skip stamp visibility
        userQARef.child(questionId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserQA userQA = dataSnapshot.getValue(UserQA.class);
                // skip stamp visibility
              stamp.setVisibility("skipped".equals(userQA.getAnswer()) ? View.VISIBLE :View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });







    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    void getCommentCounts(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("Comments")
                .child(questionId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                long count = dataSnapshot.getChildrenCount();
                String str = count>0 ? "("+count+")" :"";
                mLayout.getTabAt(0).setText("Comments" +str );
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    void setUI(final boolean answered) {

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("Questions_8")
                .child(questionId)
                .child("type");

        //get questionType
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String type = dataSnapshot.getValue(String.class);

                if(answered){
                    DatabaseReference questionRef = userQARef.child(questionId);
                    questionRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            UserQA userQA = dataSnapshot.getValue(UserQA.class);
                            switch (type) {
                                case "mc":
                                    isMultipleChoice(userQA);
                                    break;

                                case "sp":
                                    isDropDown(userQA);
                                    break;

                                case "sc":
                                    isScale(userQA);
                                    break;

                                default:
                                    return;

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }else{
                    switch (type) {
                        case "mc":
                            isMultipleChoice(null);
                            break;

                        case "sp":
                            isDropDown(null);
                            break;

                        case "sc":
                            isScale(null);
                            break;

                        default:
                            return;

                    }
                }






            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void isScale(final UserQA userQA){

        mSeekBar.setVisibility(View.VISIBLE);
        subQuestion.setVisibility(View.VISIBLE);

        if(userQA!=null){
            originalAnswer= userQA.getAnswer();
        }


        //handle skipped answer
        mSeekBar.setProgress(  userQA!=null && !"skipped".equals(userQA.getAnswer()) ? Float.valueOf(userQA.getAnswer()) :5f);

        // set question text
        DatabaseReference mRef= FirebaseDatabase.getInstance().getReference("Questions_8")
                .child(questionId);
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SurveyQ surveyQ = dataSnapshot.getValue(SurveyQ.class);
                questionTV.setText(surveyQ.getQuestion());

                userQA.setQuestionId(surveyQ.getQuestionId());;
                userQA.setQuestion(surveyQ.getQuestion());

//reset button click
                confirmBtn.setOnClickListener(new View.OnClickListener() {
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

                // skip button click

                //skip button click
                skipBtn.setOnClickListener(new View.OnClickListener() {
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

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });





    }


    private void isDropDown(final UserQA userQA){
        spinner.setVisibility(View.VISIBLE);
        if(userQA!=null){
            originalAnswer= userQA.getAnswer();
        }

        DatabaseReference mRef= FirebaseDatabase.getInstance().getReference("Questions_8")
                .child(questionId);

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
                questionTV.setText(question);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, surveyQ.getAnswer());
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                int position = userQA!=null ?  surveyQ.getAnswer().indexOf(userQA.getAnswer()) : -1;
                spinner.setAdapter(adapter);
                // set answer
                if(position>=0){
                    spinner.setSelection(position);
                }

                // if user hasn't answered this question, create new userQA
                userQA.setQuestionId(questionId);
                userQA.setAnswer(null);
                userQA.setQuestion(question);
                userQA.setFavorite(false);



                // reset click listener
                confirmBtn.setOnClickListener(new View.OnClickListener() {
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
                                if (dataSnapshot.getValue(Long.class) == null
                                        || new Date().getTime() > dataSnapshot.getValue(Long.class)) {

                                    userQA.setAnswer(spinner.getSelectedItem().toString());
                                    showVerifyDialog(originalAnswer,spinner.getSelectedItem().toString(),userQA);

                                } else {
                                    setTimerUI(dataSnapshot.getValue(Long.class) - new Date().getTime());
                                }
                            }


                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                });



                //skip button click
                skipBtn.setOnClickListener(new View.OnClickListener() {
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

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void isMultipleChoice(final UserQA userQA) {

        radioGroup.setVisibility(View.VISIBLE);
        if(userQA!=null){
            originalAnswer= userQA.getAnswer();
        }


        DatabaseReference mRef= FirebaseDatabase.getInstance().getReference("Questions_8")
                .child(questionId);

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
                questionTV.setText(question);
                radioGroup.removeAllViews();
                radioGroup.clearCheck();
                for (int i = 0; i < surveyQ.getAnswer().size(); i++) {
                    RadioButton button = new RadioButton(QuestionActivity.this);
                    button.setText(surveyQ.getAnswer().get(i));
                    radioGroup.addView(button);
                    if (userQA != null && surveyQ.getAnswer().get(i).toString().equals(userQA.getAnswer())) {
                        ((RadioButton) radioGroup.getChildAt(i)).setChecked(true);
                    }
                }
                // if user hasn't answered this question, create new userQA
                    userQA.setQuestionId(questionId);
                    userQA.setAnswer(null);
                    userQA.setQuestion(question);
                    userQA.setFavorite(false);



                // reset click listener
               confirmBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // check question is resetable or not
                        DatabaseReference resetRef = userQARef
                                .child(userQA.getQuestionId())
                                .child("reset");

                        resetRef .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // if timestamp less than current time question is resetable
                                if (dataSnapshot.getValue(Long.class) == null
                                        || new Date().getTime() > dataSnapshot.getValue(Long.class)) {

                                    if (radioGroup.getCheckedRadioButtonId() == -1 && radioGroup.getVisibility() == View.VISIBLE) {
                                        Toast.makeText(getApplicationContext(), "Make a selection", Toast.LENGTH_SHORT).show();

                                    } else if (radioGroup.getCheckedRadioButtonId() != -1 && radioGroup.getVisibility() == View.VISIBLE) {
                                        int id = radioGroup.getCheckedRadioButtonId();
                                        View radioButton = radioGroup.findViewById(id);
                                        int radioId = radioGroup.indexOfChild(radioButton);
                                        RadioButton btn = (RadioButton) radioGroup.getChildAt(radioId);
                                        String selection = (String) btn.getText();
                                        userQA.setAnswer(selection);
                                        showVerifyDialog(originalAnswer, selection, userQA);
                                    }


                                } else {
                                    setTimerUI(dataSnapshot.getValue(Long.class) - new Date().getTime());
                                }
                            }


                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                });



                //skip button click
                skipBtn.setOnClickListener(new View.OnClickListener() {
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

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    void showVerifyDialog (String originalAnswer, final String newAnswer, final UserQA userQA){
        String content = originalAnswer!=null ?
                "Are you sure to change answer from "+ originalAnswer +" to "+ newAnswer+"?"
                : "Are you sure you want to submit answer "+ newAnswer;
        new MaterialDialog.Builder(this)
                .content(content)
                .positiveText("Yes")
                .positiveColor(ContextCompat.getColor(this, R.color.colorAccent))
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
                                final MaterialDialog resetDialog =  new MaterialDialog.Builder(QuestionActivity.this)
                                        .content("Submitting your answer...")
                                        .positiveText(R.string.ok)
                                        .show();

                                resetDialog.setContent("Answer Submitted");
                                resetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface mdialog) {
                                      stamp.setVisibility("skipped".equals(newAnswer)? View.VISIBLE :View.GONE);
                                    }
                                });



                            }
                        });
                    }
                })

                .negativeText("NO")
                .negativeColor(ContextCompat.getColor(this, R.color.holo_red_light))
                .show();

    }

    void setTimerUI (long mills){

        final MaterialDialog dialog =  new MaterialDialog.Builder(this)
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
}
