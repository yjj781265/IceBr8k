package app.jayang.icebr8k;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
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
import app.jayang.icebr8k.Fragments.Result_fragment;
import app.jayang.icebr8k.Fragments.TagFragment;
import app.jayang.icebr8k.Model.SurveyQ;
import app.jayang.icebr8k.Model.TagModel;
import app.jayang.icebr8k.Model.UserQA;
import app.jayang.icebr8k.Utility.MyToolBox;
import app.jayang.icebr8k.Utility.OnDoneListener;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class QuestionActivity extends SwipeBackActivity{
    private final int COMMENTS_TAB = 0,
            TAGS_TAB = 1, RESULTS_TAG = 2;
    private final long DAYS = 60 * 60 * 48 * 1000;  // 2 DAYS
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private final DatabaseReference userQARef = FirebaseDatabase.getInstance().getReference()
            .child("UserQA").child(currentUser.getUid());
    private TextView questionTV, subQuestion, confirmBtn, skipBtn;
    private TabLayout mLayout;
    private AppBarLayout mAppBarLayout;
    private ViewPager mViewPager;
    private Toolbar mToolbar;
    private ImageView stamp;
    private RadioGroup radioGroup;
    private CardView mCardView;
    private ProgressBar mProgressBar;
    private Spinner spinner;
    private MaterialDialog loadingDialog, submittedDialog;
    private String originalAnswer = null;
    private BubbleSeekBar mSeekBar;
    private Boolean firstTime = true;
    private String questionId;
    private TagFragment tagFragment;
    private FloatingActionButton mActionButton;
    private ValueEventListener skipListener;
    private DatabaseReference commentRef, question8Ref;
    private ValueEventListener commentRefListener, isScListener, isMcListener, isSpListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        confirmBtn = (TextView) findViewById(R.id.question_reset);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        skipBtn = (TextView) findViewById(R.id.question_skip);
        questionTV = (TextView) findViewById(R.id.question_id);
        subQuestion = (TextView) findViewById(R.id.sub_question_id);
        spinner = (Spinner) findViewById(R.id.spinner_id);
        mSeekBar = (BubbleSeekBar) findViewById(R.id.seekBar);
        stamp = (ImageView) findViewById(R.id.question_skip_stamp);
        mLayout = (TabLayout) findViewById(R.id.question_tablayout);
        mViewPager = (ViewPager) findViewById(R.id.question_viewpager);
        mToolbar = (Toolbar) findViewById(R.id.question_toolbar);
        mProgressBar = (ProgressBar) findViewById(R.id.question_progressBar);
        mCardView = (CardView) findViewById(R.id.cardView);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.question_appBar);
        mActionButton = (FloatingActionButton) findViewById(R.id.tag_add);
       


        loadingDialog = new MaterialDialog.Builder(this)
                .content("Submitting your answer....")
                .canceledOnTouchOutside(false)
                .build();

        submittedDialog = new MaterialDialog.Builder(QuestionActivity.this)
                .canceledOnTouchOutside(false)
                .content("Answer Submitted")
                .positiveText(R.string.ok)
                .build();
        getSwipeBackLayout().setEdgeSize(36);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        mActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInputDialog();
            }
        });


        final Handler handler = new Handler();


        questionId = getIntent().getExtras().getString("questionId", null);
        if (questionId != null) {
            question8Ref = FirebaseDatabase.getInstance().getReference("Questions_8")
                    .child(questionId);

            getCommentCounts();
            viewPagerAdapter.addFragment(Comment_Fragment.newInstance(questionId));
            tagFragment = TagFragment.newInstance(questionId);
            viewPagerAdapter.addFragment(tagFragment);
            viewPagerAdapter.addFragment(Result_fragment.newInstance(questionId));

            mViewPager.setAdapter(viewPagerAdapter);
            mViewPager.setOffscreenPageLimit(2);
            mLayout.setupWithViewPager(mViewPager);
            mLayout.getTabAt(COMMENTS_TAB).setText("Comments");
            mLayout.getTabAt(TAGS_TAB).setText("Tags");
            mLayout.getTabAt(RESULTS_TAG).setText("Results");


            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    switch (position) {
                        case RESULTS_TAG:
                            mAppBarLayout.setExpanded(false);
                            break;
                        default:
                            mAppBarLayout.setExpanded(true);
                    }
                }

                @Override
                public void onPageSelected(int position) {
                    if (position == TAGS_TAB) {
                        mActionButton.show();
                    } else {
                        mActionButton.hide();
                    }


                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }


        // Toast.makeText(this, questionId, Toast.LENGTH_SHORT).show();

        // extras for reply Page
        String topCommentId = getIntent().getExtras().getString("topCommentId");
        String commentId = getIntent().getExtras().getString("commentId");

        if (topCommentId != null) {
            Intent mIntent = new Intent(this, Reply.class);
            mIntent.putExtra("questionId", questionId);
            mIntent.putExtra("topCommentId", topCommentId);
            mIntent.putExtra("commentId", commentId);
            startActivity(mIntent);
        }


        //if user has answered question the btn will be reset , else will be confirm

        userQARef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (questionId != null && dataSnapshot.hasChild(questionId)) {
                    confirmBtn.setText("Reset");
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setUI(true);
                        }
                    }, 666);

                } else {
                    confirmBtn.setText("Confirm");
                    if (questionId != null) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setUI(false);
                            }
                        }, 666);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // skip stamp visibility

        skipListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserQA userQA = dataSnapshot.getValue(UserQA.class);
                if (userQA != null) {
                    // skip stamp visibility
                    stamp.setVisibility("skipped".equals(userQA.getAnswer()) ? View.VISIBLE : View.GONE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        userQARef.child(questionId).addValueEventListener(skipListener);


    }

    private void showInputDialog() {
        MaterialDialog materialDialog = new MaterialDialog.Builder(this)
                .title("Create your own tag")
                .inputRange(1, 15)
                .input("Enter your tag here...", null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if (MyToolBox.isOneWord(input.toString())) {
                           tagFragment.tagModels.add(new TagModel(input.toString()));
                           tagFragment.adapter.notifyDataSetChanged();
                        } else {
                            MyToolBox.showToast(getString(R.string.tag_dialog_one_word_error), QuestionActivity.this);
                            showInputDialog();

                        }

                    }
                }).build();
        materialDialog.show();
    }



    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    void getCommentCounts() {
        commentRef = FirebaseDatabase.getInstance().getReference()
                .child("Comments")
                .child(questionId);
        commentRefListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count = dataSnapshot.getChildrenCount();
                String str = count > 0 ? " (" + count + ")" : "";
                mLayout.getTabAt(0).setText("Comments" + str);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        commentRef.addValueEventListener(commentRefListener);
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

                if (answered) {
                    DatabaseReference questionRef = userQARef.child(questionId);
                    questionRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            UserQA userQA = dataSnapshot.getValue(UserQA.class);
                            skipBtn.setVisibility(userQA.getAnswer() != null && "skipped".equals(userQA.getAnswer()) ? View.GONE : View.VISIBLE);
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

                } else {
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
                            break;

                    }
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void isScale(final UserQA userQA) {

        mSeekBar.setVisibility(View.VISIBLE);
        subQuestion.setVisibility(View.VISIBLE);


        if (userQA != null) {
            originalAnswer = userQA.getAnswer();
        }


        //handle skipped answer
        mSeekBar.setProgress(userQA != null && !"skipped".equals(userQA.getAnswer()) ? Float.valueOf(userQA.getAnswer()) : 5f);

        // set question text


        isScListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SurveyQ surveyQ = dataSnapshot.getValue(SurveyQ.class);
                questionTV.setText(surveyQ.getQuestion());

                userQA.setQuestionId(surveyQ.getQuestionId());
                ;
                userQA.setQuestion(surveyQ.getQuestion());

//reset button click
                confirmBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // check question is resetable or not
                        DatabaseReference resetRef = userQARef
                                .child(userQA.getQuestionId())
                                .child("reset");

                        resetRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // if timestamp less than cuurent time question is resetable
                                if (dataSnapshot.getValue(Long.class) == null
                                        || new Date().getTime() > dataSnapshot.getValue(Long.class)) {
                                    userQA.setAnswer(String.valueOf(mSeekBar.getProgress()));
                                    showVerifyDialog(originalAnswer, String.valueOf(mSeekBar.getProgress()), userQA);

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

                // skip button click

                //skip button click
                skipBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        DatabaseReference resetRef = userQARef
                                .child(userQA.getQuestionId())
                                .child("reset");

                        resetRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                // if timestamp less than cuurent time question is resetable
                                if (dataSnapshot.getValue(Long.class) == null
                                        || new Date().getTime() > dataSnapshot.getValue(Long.class)) {

                                    userQA.setAnswer("skipped");
                                    showVerifyDialog(originalAnswer, "skipped", userQA);
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
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        question8Ref.addListenerForSingleValueEvent(isScListener);

        mCardView.setVisibility(View.VISIBLE);
        if (firstTime) {
            YoYo.with(Techniques.FadeIn).playOn(mCardView);
            firstTime = false;
        }

        mProgressBar.setVisibility(View.GONE);


    }


    private void isDropDown(final UserQA userQA) {
        spinner.setVisibility(View.VISIBLE);
        if (userQA != null) {
            originalAnswer = userQA.getAnswer();
        }

        isSpListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> answer = null;
                String type = dataSnapshot.child("type").getValue(String.class);
                String question = dataSnapshot.child("question").getValue(String.class);
                String question_id = dataSnapshot.child("questionId").getValue(String.class);
                if (dataSnapshot.hasChild("answer")) {
                    GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {
                    };
                    answer = dataSnapshot.child("answer").getValue(t);

                }
                SurveyQ surveyQ = new SurveyQ(type, question, question_id, answer);
                //Ui Stuff
                questionTV.setText(question);
                ArrayAdapter<String> adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, surveyQ.getAnswer());
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                int position = userQA != null ? surveyQ.getAnswer().indexOf(userQA.getAnswer()) : -1;
                spinner.setAdapter(adapter);
                // set answer
                if (position >= 0) {
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

                        resetRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // if timestamp less than cuurent time question is resetable
                                if (dataSnapshot.getValue(Long.class) == null
                                        || new Date().getTime() > dataSnapshot.getValue(Long.class)) {

                                    userQA.setAnswer(spinner.getSelectedItem().toString());
                                    showVerifyDialog(originalAnswer, spinner.getSelectedItem().toString(), userQA);

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

                        resetRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                // if timestamp less than cuurent time question is resetable
                                if (dataSnapshot.getValue(Long.class) == null
                                        || new Date().getTime() > dataSnapshot.getValue(Long.class)) {

                                    userQA.setAnswer("skipped");
                                    showVerifyDialog(originalAnswer, "skipped", userQA);
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
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        question8Ref.addListenerForSingleValueEvent(isSpListener);

        mCardView.setVisibility(View.VISIBLE);
        if (firstTime) {
            YoYo.with(Techniques.FadeIn).playOn(mCardView);
            firstTime = false;
        }

        mProgressBar.setVisibility(View.GONE);
    }

    private void isMultipleChoice(final UserQA userQA) {

        radioGroup.setVisibility(View.VISIBLE);
        if (userQA != null) {
            originalAnswer = userQA.getAnswer();
        }


        isMcListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> answer = null;
                String type = dataSnapshot.child("type").getValue(String.class);
                String question = dataSnapshot.child("question").getValue(String.class);
                String question_id = dataSnapshot.child("questionId").getValue(String.class);
                if (dataSnapshot.hasChild("answer")) {
                    GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {
                    };
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

                        resetRef.addListenerForSingleValueEvent(new ValueEventListener() {
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

                        resetRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                // if timestamp less than cuurent time question is resetable
                                if (dataSnapshot.getValue(Long.class) == null
                                        || new Date().getTime() > dataSnapshot.getValue(Long.class)) {

                                    userQA.setAnswer("skipped");
                                    showVerifyDialog(originalAnswer, "skipped", userQA);
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
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        question8Ref.addListenerForSingleValueEvent(isMcListener);
        mCardView.setVisibility(View.VISIBLE);
        if (firstTime) {
            YoYo.with(Techniques.FadeIn).playOn(mCardView);
            firstTime = false;
        }

        mProgressBar.setVisibility(View.GONE);
    }


    void showVerifyDialog(String originalAnswer, final String newAnswer, final UserQA userQA) {
        String content = originalAnswer != null ?
                "Are you sure to change answer from \"" + originalAnswer + "\" to \"" + newAnswer + "\"?"
                : "Are you sure you want to submit answer \"" + newAnswer + "\"?";
        new MaterialDialog.Builder(this)
                .content(content)
                .positiveText("Yes")
                .positiveColor(ContextCompat.getColor(this, R.color.colorAccent))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull final MaterialDialog dialog, @NonNull DialogAction which) {
                        loadingDialog.show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                new updateToDatabase().execute(userQA);
                            }
                        }, 666);


                    }
                })
                .negativeText("NO")
                .negativeColor(ContextCompat.getColor(this, R.color.holo_red_light))
                .show();

    }

    void setTimerUI(long mills) {

        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("You can't reset this question until after")
                .positiveText(R.string.ok)
                .show();
        new CountDownTimer(mills, 1000) {

            public void onTick(long millisUntilFinished) {
                long leftMills;
                long day = millisUntilFinished / (60 * 24 * 60 * 1000);
                leftMills = millisUntilFinished - day * 24 * 60 * 60 * 1000;
                long hour = leftMills / (60 * 60 * 1000);
                leftMills = leftMills - hour * 60 * 60 * 1000;
                long min = (leftMills) / (1000 * 60);
                leftMills = leftMills - (min * 60 * 1000);
                long sec = leftMills / 1000;

                dialog.setContent(day + "d " + hour + "h " + min + "m " + sec + "s");

            }

            public void onFinish() {
                dialog.setContent("done!");

            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        try {
            userQARef.child(questionId).removeEventListener(skipListener);
            commentRef.removeEventListener(commentRefListener);
            question8Ref.removeEventListener(isMcListener);
            question8Ref.removeEventListener(isSpListener);
            question8Ref.removeEventListener(isScListener);
        } catch (NullPointerException e) {
            Log.d("Question123", e.getMessage());
        }

        super.onDestroy();
    }

    public boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }



    public class updateToDatabase extends AsyncTask<UserQA, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(UserQA... userQAS) {
            UserQA userQA = userQAS[0];
            Log.d("question123", "is Main thread ?" + isMainThread());
            userQARef.child(userQA.getQuestionId()).setValue(userQA);
            userQARef.child(userQA.getQuestionId()).child("reset").setValue(new Date().getTime() +
                    (DAYS)).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("question123", "succ is Main thread ?" + isMainThread());

                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            loadingDialog.dismiss();
            submittedDialog.show();


        }
    }



}
