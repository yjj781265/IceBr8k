package app.jayang.icebr8k.Fragments;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.dd.processbutton.FlatButton;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.xw.repo.BubbleSeekBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import app.jayang.icebr8k.Homepage;
import app.jayang.icebr8k.Modle.CommentBottomSheetFrag;
import app.jayang.icebr8k.Modle.SurveyQ;
import app.jayang.icebr8k.Modle.UserQA;
import app.jayang.icebr8k.PeopleNearby;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.Utility.Compatability;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;


/**
 * Created by LoLJay on 10/22/2017.
 */

public class SurveyTab_Fragment extends Fragment implements OnSuccessListener {
    View mview;
    private BubbleSeekBar mSeekBar;
    private TextView mTextView, msubTextview, progressText;

    private RadioGroup mRadioGroup;
    private Homepage mHomepage;
    private ImageView  thumbUp, thumbDown;
    private RelativeLayout loadingGif;
    private BootstrapButton mSubmit;
    private ImageView backArrow;
    private UserQA  currentUserQA;
    private TextView skip;
    private int index =0, ChilderenCounter =0 , Counter=0;
    private MaterialDialog loadingDialog;
    private String TAG = "surveyFrag";
    private CardView cardView;
    private ConstraintLayout feedbackPannel;
    private FlatButton connect, feedback,moreQuestion;
    private DatabaseReference userQARef;
    private final String MC_TYPE = "mc";
    private final String SC_TYPE = "sc";
    private final String SP_TYPE = "sp";


    private  ArrayList<SurveyQ> surveyList; // for unpdating UI
    private ArrayList<String> userQlist;

    private HashMap<SurveyQ, UserQA> userQAHashMap; // for comparing with userQArraylist

    private ProgressBar mProgressBar;
    private  Spinner mSpinner;
    private  FirebaseUser currentUser;
    public SurveyTab_Fragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userQlist = new ArrayList<>();
        userQAHashMap = new HashMap<>();
        surveyList =  new ArrayList<>();

        userQARef = FirebaseDatabase.getInstance().getReference().child("UserQA").child(currentUser.getUid());

       showLog("onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable  ViewGroup container, @Nullable final Bundle savedInstanceState) {

      mview = inflater.inflate(R.layout.survey_tab,container,false);


        thumbUp = mview.findViewById(R.id.survey_thumbup);
        thumbDown = mview.findViewById(R.id.survey_thumbdown);

        mSubmit = mview.findViewById(R.id.submitBtn);
        mSubmit.setVisibility(View.GONE);
        backArrow = mview.findViewById(R.id.backarrow_survey);
        backArrow.setVisibility(View.GONE);
        mProgressBar = mview.findViewById(R.id.loading_progressbar);
        mProgressBar.setVisibility(View.INVISIBLE);


        mSeekBar = mview.findViewById(R.id.seekBar);
        mSeekBar.setVisibility(View.INVISIBLE);
        mTextView = mview.findViewById(R.id.question_id);
        mTextView.setVisibility(View.INVISIBLE);
        mRadioGroup =mview.findViewById(R.id.radioGroup);
        mRadioGroup.setVisibility(View.GONE);
        mSpinner=mview.findViewById(R.id.spinner_id);
        mSpinner.setVisibility(View.GONE);
        msubTextview = mview.findViewById(R.id.sub_question_id);
        msubTextview.setVisibility(View.GONE);
        progressText = mview.findViewById(R.id.progress_text);
        progressText.setVisibility(View.INVISIBLE);



        skip = mview.findViewById(R.id.skip_btn);
        skip.setVisibility(View.GONE);
        cardView = mview.findViewById(R.id.cardView);
        cardView.setVisibility(View.GONE);
        loadingGif = mview.findViewById(R.id.survey_loading);
        loadingGif.setVisibility(View.VISIBLE);
        feedbackPannel = mview.findViewById(R.id.survey_feedbackPannel);
        moreQuestion = mview.findViewById(R.id.survey_moreq);
        feedback = mview.findViewById(R.id.survey_feedback);
        connect = mview.findViewById(R.id.survey_connect);

        loadingDialog = new MaterialDialog.Builder(getContext())

                .content("Updating your compatibility ")
                .progress(true, 0).build();


       thumbUp.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Toast.makeText(getActivity(), ""+currentUserQA.getQuestion(), Toast.LENGTH_SHORT).show();
               if(thumbUp.isSelected()){
                   thumbUp.setSelected(false);
                   Snackbar.make(mview, "Removed like for this question",
                           Snackbar.LENGTH_SHORT)
                           .show();

               }else{
                   thumbUp.setSelected(true);
                   thumbDown.setSelected(false);
                   YoYo.with(Techniques.Tada).playOn(thumbUp);

                   Snackbar.make(mview, "You liked this question",
                           Snackbar.LENGTH_SHORT)
                           .show();
               }
           }
       });

        thumbDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(thumbDown.isSelected()){
                    thumbDown.setSelected(false);
                    Snackbar.make(mview, "Removed dislike for this question",
                            Snackbar.LENGTH_SHORT)
                            .show();
                }else{
                    thumbDown.setSelected(true);
                    thumbUp.setSelected(false);
                    YoYo.with(Techniques.Bounce).playOn(thumbDown);
                    Snackbar.make(mview, "You disliked this question",
                            Snackbar.LENGTH_SHORT)
                            .show();
                }
                CommentBottomSheetFrag mCommentBottomSheetFrag = new CommentBottomSheetFrag();
                mCommentBottomSheetFrag.show(getFragmentManager(),"bottomsheet");
            }



        });

        moreQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextView.setVisibility(View.INVISIBLE);
                initQuestions();
                if(surveyList.isEmpty()){
                    Toast.makeText(getActivity(), "You have answered all the questions", Toast.LENGTH_SHORT).show();
                }
            }
        });

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if share my postion is on
                if("public".equals(getPrivacySharedPreference())){
                    showSingleChoiceDialog();
                }else {
                    new MaterialDialog.Builder(getActivity())
                            .content("\"Share My Location\" is disabled, you can go to Me tab to enable it.")
                            .positiveText(R.string.ok).onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            mHomepage = (Homepage)getActivity();
                            if(mHomepage!=null) {
                                mHomepage.getViewPager().setCurrentItem(4,true);
                            }

                        }
                    })
                            .show();
                }
            }
        });

        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("market://details?id=" + getActivity().getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + getActivity().getPackageName())));
                }
            }
        });




        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(index>1 && !surveyList.isEmpty()){
                    updateUI_QA(surveyList.get(index-2));

                }
            }
        });


        mSeekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
              @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                // Toast.makeText(getContext(), "changed "+progress + progressFloat, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                showLog(String.valueOf(progressFloat));
                mSeekBar.setProgress(progressFloat);
            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                //Toast.makeText(getContext(), "finally "+progress + progressFloat, Toast.LENGTH_SHORT).show();
            }
        });


        initQuestions();









        showLog("onCreateView");
        return  mview;
    }




    @Override
    public void onStart() {
        super.onStart();
        showLog("onStart");
    }

    @Override
    public void onPause() {
        super.onPause();
        showLog("onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        showLog("onResume");





    }










    public void initQuestions(){
        feedbackPannel.setVisibility(View.GONE);
        cardView.setVisibility(View.VISIBLE);
    userQlist = new ArrayList<>();
    index =0;
    mProgressBar.setIndeterminate(true);
    mProgressBar.setVisibility(View.VISIBLE);

    // get all the questions user answered
    DatabaseReference mref = FirebaseDatabase.getInstance().getReference
            ("UserQA/"+currentUser.getUid());
    mref.addChildEventListener(new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            showLog(dataSnapshot.toString());
            userQlist.add(dataSnapshot.getKey());

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    });

    mref.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(userQlist.isEmpty()){
                new MaterialStyledDialog.Builder(getContext())
                        .setIcon(R.mipmap.ic_launcher)
                        .setHeaderColor(R.color.lightBlue)
                        .withDialogAnimation(true)
                        .setDescription(getString(R.string.first_login_message))
                        .setStyle(Style.HEADER_WITH_ICON)
                        .setPositiveText(getString(R.string.ok))
                        .show();
            }

            pullDatabaseQuesteions();

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    });


}

    // create initial question;
    public void pullDatabaseQuesteions(){
        surveyList .clear();
        DatabaseReference mRef= FirebaseDatabase.getInstance().getReference("Questions_8");
        mRef.keepSynced(true);
        mRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(!userQlist.contains(dataSnapshot.getKey())){
                    ArrayList<String> answer = null;


                    String type = dataSnapshot.child("type").getValue(String.class);
                    String question = dataSnapshot.child("question").getValue(String.class);
                    String question_id = dataSnapshot.child("questionId").getValue(String.class);
                    if(dataSnapshot.hasChild("answer") ) {
                        GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>(){};
                        answer = dataSnapshot.child("answer").getValue(t);

                    }

                    SurveyQ surveyQ = new SurveyQ(type, question, question_id, answer);
                    if( surveyList.size()<8){
                        surveyList.add(surveyQ);
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!surveyList.isEmpty()){
                    Collections.shuffle(surveyList);
                    updateUI(surveyList.get(0));
                    cardView.setVisibility(View.VISIBLE);






                    // no more questions in database
                }else if(surveyList.isEmpty()){
                    hideCardViewComponent();


                }
                mProgressBar.setVisibility(View.GONE);
                loadingGif.setVisibility(View.GONE);



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateUI(SurveyQ surveyQ) {

        if(surveyList.indexOf(surveyQ) ==0){
            backArrow.setVisibility(View.GONE);
        }else{
            backArrow.setVisibility(View.VISIBLE);
        }
        if(userQAHashMap.get(surveyQ)!=null){
            updateUI_QA(surveyQ);
        }else{
            mTextView.setTypeface(Typeface.DEFAULT_BOLD);
            String type = surveyQ.getType();
            switch(type){
                case  MC_TYPE:
                    isMultipleChoic(surveyQ);
                    break;
                case  SC_TYPE:
                    isScaleQuestion(surveyQ);
                    break;
                case  SP_TYPE:
                    isSpinnerQuestion(surveyQ);
                    break;
                default: isMultipleChoic(surveyQ);
                    break;
            }
        }
        compareWithFriends();


    }

    private void updateUI_QA(SurveyQ surveyQ) {
        if(surveyList.indexOf(surveyQ) ==0){
            backArrow.setVisibility(View.GONE);
        }else{
            backArrow.setVisibility(View.VISIBLE);
        }
        mTextView.setTypeface(Typeface.DEFAULT_BOLD);
        String type = surveyQ.getType();
        switch(type){
            case  MC_TYPE:
                isMultipleChoicQA(surveyQ);
                break;
            case  SC_TYPE:
                isScaleQuestionQA(surveyQ);
                break;
            case  SP_TYPE:
                isSpinnerQuestionQA(surveyQ);
                break;
            default: isMultipleChoic(surveyQ);
                break;
        }


    }






    private void isSpinnerQuestionQA(final SurveyQ surveyQ) {
        if(surveyQ.getType().equals(SP_TYPE) && surveyList.contains(surveyQ)){
            index = surveyList.indexOf(surveyQ)+1;
            mTextView.setVisibility(View.VISIBLE);
            mTextView.setText(surveyQ.getQuestion());
            progressText .setVisibility(View.VISIBLE);
            progressText.setText(index +"/"+surveyList.size());
            skip.setVisibility(View.VISIBLE);

            mSeekBar.setVisibility(View.GONE);
            mSpinner.setVisibility(View.VISIBLE);
            mRadioGroup.setVisibility(View.GONE);


            mSubmit.setVisibility(View.VISIBLE);
            msubTextview.setVisibility(View.GONE);
        }
        String answer = userQAHashMap.get(surveyQ).getAnswer();

        if(!surveyQ.getAnswer().isEmpty()) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, surveyQ.getAnswer());
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpinner.setAdapter(adapter);
          if(answer!=null&&!"skipped".equals(answer)){
              for(int i =0 ; i<surveyQ.getAnswer().size();i++){
                  if(surveyQ.getAnswer().get(i).equals(answer)){
                      mSpinner.setSelection(i);
                  }
              }

          }




            final UserQA userQA = new UserQA();
            userQA.setQuestion(surveyQ.getQuestion());
            userQA.setQuestionId(surveyQ.getQuestionId());
            currentUserQA = userQA;
            //default is false;
            userQA.setFavorite(userQAHashMap.get(surveyQ).getFavorite());



            skip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    userQA.setAnswer("skipped");
                    userQA.setFavorite(false);
                    userQARef.child(userQA.getQuestionId()).setValue(userQA).addOnSuccessListener(SurveyTab_Fragment.this);
                    if(index<surveyList.size() && index!=0){
                        updateUI(surveyList.get(index));
                        userQAHashMap.put(surveyQ,userQA);
                    }else if(index == surveyList.size()){
                        hideCardViewComponent();
                    }
                }
            });

            mSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String text =  mSpinner.getSelectedItem().toString();
                    userQA.setAnswer(text);
                    userQARef.child(userQA.getQuestionId()).setValue(userQA).addOnSuccessListener(SurveyTab_Fragment.this);

                    if(index<surveyList.size()&& index!=0){
                        updateUI(surveyList.get(index));
                        userQAHashMap.put(surveyQ,userQA);
                    }else if(index ==surveyList.size()){
                        hideCardViewComponent();

                    }
                }
            });
        }

    }

    private void isScaleQuestionQA(final SurveyQ surveyQ) {

        if(surveyQ.getType().equals(SC_TYPE) && surveyList.contains(surveyQ)){
            index = surveyList.indexOf(surveyQ)+1;
            mTextView.setVisibility(View.VISIBLE);
            mTextView.setText(surveyQ.getQuestion());
            progressText .setVisibility(View.VISIBLE);
            progressText.setText(index +"/"+surveyList.size());
            skip.setVisibility(View.VISIBLE);

            mSeekBar.setVisibility(View.VISIBLE);
            mSpinner.setVisibility(View.GONE);
            mRadioGroup.setVisibility(View.GONE);

            mSubmit.setVisibility(View.VISIBLE);
            msubTextview.setVisibility(View.VISIBLE);
            String str = userQAHashMap.get(surveyQ).getAnswer();
            if(str!=null && !"skipped".equals(str)) {
                Float answer = Float.valueOf(userQAHashMap.get(surveyQ).getAnswer());
                mSeekBar.setProgress(answer);
            }else{
                mSeekBar.setProgress(5f);

            }







            final UserQA userQA = new UserQA();
            userQA.setQuestion(surveyQ.getQuestion());
            userQA.setQuestionId(surveyQ.getQuestionId());
            currentUserQA = userQA;
            //default is false;
            userQA.setFavorite(userQAHashMap.get(surveyQ).getFavorite());


            skip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    userQA.setFavorite(false);
                    userQA.setAnswer("skipped");
                    userQARef.child(userQA.getQuestionId()).setValue(userQA).addOnSuccessListener(SurveyTab_Fragment.this);
                    if(index<surveyList.size()&& index!=0){
                        updateUI(surveyList.get(index));
                        userQAHashMap.put(surveyQ,userQA);
                    }else if(index == surveyList.size()){
                        hideCardViewComponent();
                    }
                }
            });

            mSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String answer = String.valueOf( mSeekBar.getProgress());
                    userQA.setAnswer(answer);
                    userQARef.child(userQA.getQuestionId()).setValue(userQA).addOnSuccessListener(SurveyTab_Fragment.this);
                    if(index<surveyList.size() && index!=0){
                        updateUI(surveyList.get(index));
                        userQAHashMap.put(surveyQ,userQA);
                    }else if(index ==surveyList.size()){
                        hideCardViewComponent();
                    }
                }
            });



        }
    }

    private void isMultipleChoicQA(final SurveyQ surveyQ) {
        if(surveyQ.getType().equals(MC_TYPE) && surveyList.contains(surveyQ)) {
            index = surveyList.indexOf(surveyQ)+1;
            mTextView.setVisibility(View.VISIBLE);
            mTextView.setText(surveyQ.getQuestion());
            progressText .setVisibility(View.VISIBLE);
            progressText.setText(index +"/"+surveyList.size());
            skip.setVisibility(View.VISIBLE);

            mSubmit.setVisibility(View.VISIBLE);
            mSeekBar.setVisibility(View.GONE);
            mSpinner.setVisibility(View.GONE);

            msubTextview.setVisibility(View.GONE);

            mRadioGroup.removeAllViews();
            mRadioGroup.clearCheck();
            mRadioGroup.setVisibility(View.VISIBLE);

            String answer =null;
            if(userQAHashMap.get(surveyQ)!=null) {
                answer =userQAHashMap.get(surveyQ).getAnswer();

            }

            //Ui Stuff
            for (int i = 0; i < surveyQ.getAnswer().size(); i++) {
                RadioButton button = new RadioButton(mview.getContext());
                button.setText(surveyQ.getAnswer().get(i).toString());
                mRadioGroup.addView(button);
                if(answer!=null&&!"skipped".equals(answer)  && surveyQ.getAnswer().get(i).toString().equals(answer)) {
                    ((RadioButton)mRadioGroup.getChildAt(i)).setChecked(true);
                }
            }


            final UserQA userQA = new UserQA();
            userQA.setQuestion(surveyQ.getQuestion());
            userQA.setQuestionId(surveyQ.getQuestionId());
            currentUserQA = userQA;
            //default is false;
            userQA.setFavorite(userQAHashMap.get(surveyQ).getFavorite());


            skip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    userQA.setFavorite(false);
                    userQA.setAnswer("skipped");
                    userQARef.child(userQA.getQuestionId()).setValue(userQA).addOnSuccessListener(SurveyTab_Fragment.this);
                    if(index<surveyList.size() && index!=0){
                        updateUI(surveyList.get(index));
                        userQAHashMap.put(surveyQ,userQA);
                    }else if(index == surveyList.size()){
                        hideCardViewComponent();
                    }
                }
            });

            mSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mRadioGroup.getCheckedRadioButtonId()==-1&& mRadioGroup.getVisibility()==View.VISIBLE){
                        Toast.makeText(getContext(),"Make a selection",Toast.LENGTH_SHORT).show();

                    }else if(mRadioGroup.getCheckedRadioButtonId()!=-1&& mRadioGroup.getVisibility()==View.VISIBLE){
                        int id= mRadioGroup.getCheckedRadioButtonId();
                        View radioButton = mRadioGroup.findViewById(id);
                        int radioId = mRadioGroup.indexOfChild(radioButton);
                        RadioButton btn = (RadioButton) mRadioGroup.getChildAt(radioId);
                        String selection = (String) btn.getText();
                        userQA.setAnswer(selection);
                        userQARef.child(userQA.getQuestionId()).setValue(userQA).addOnSuccessListener(SurveyTab_Fragment.this);
                        if(index<surveyList.size() && index!=0){
                            updateUI(surveyList.get(index));
                            userQAHashMap.put(surveyQ,userQA);
                        }else if(index == surveyList.size()){
                            hideCardViewComponent();
                        }

                    }
                }
            });


        }

    }



    private void isMultipleChoic(final SurveyQ surveyQ) {
        if(surveyQ.getType().equals(MC_TYPE) && surveyList.contains(surveyQ)) {
            index = surveyList.indexOf(surveyQ)+1;
            mTextView.setVisibility(View.VISIBLE);
            mTextView.setText(surveyQ.getQuestion());
            progressText .setVisibility(View.VISIBLE);
            progressText.setText(index +"/"+surveyList.size());
            skip.setVisibility(View.VISIBLE);

            mSubmit.setVisibility(View.VISIBLE);
            mSeekBar.setVisibility(View.GONE);
            mSpinner.setVisibility(View.GONE);

            msubTextview.setVisibility(View.GONE);

            mRadioGroup.removeAllViews();
            mRadioGroup.clearCheck();
            mRadioGroup.setVisibility(View.VISIBLE);



          //Ui Stuff
            for (int i = 0; i < surveyQ.getAnswer().size(); i++) {
                RadioButton button = new RadioButton(mview.getContext());
                button.setText(surveyQ.getAnswer().get(i));
                mRadioGroup.addView(button);
            }
            final UserQA userQA = new UserQA();
            userQA.setQuestion(surveyQ.getQuestion());
            userQA.setQuestionId(surveyQ.getQuestionId());
            currentUserQA = userQA;
            //default is false;
            userQA.setFavorite(false);

            skip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    userQA.setFavorite(false);
                    userQA.setAnswer("skipped");
                    userQARef.child(userQA.getQuestionId()).setValue(userQA).addOnSuccessListener(SurveyTab_Fragment.this);
                    if(index<surveyList.size() && index!=0){
                        updateUI(surveyList.get(index));
                        userQAHashMap.put(surveyQ,userQA);
                    }else if(index == surveyList.size()){
                        hideCardViewComponent();
                    }
                }
            });

            mSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mRadioGroup.getCheckedRadioButtonId()==-1&& mRadioGroup.getVisibility()==View.VISIBLE){
                        Toast.makeText(getContext(),"Make a selection",Toast.LENGTH_SHORT).show();

                    }else if(mRadioGroup.getCheckedRadioButtonId()!=-1&& mRadioGroup.getVisibility()==View.VISIBLE){
                        int id= mRadioGroup.getCheckedRadioButtonId();
                        View radioButton = mRadioGroup.findViewById(id);
                        int radioId = mRadioGroup.indexOfChild(radioButton);
                        RadioButton btn = (RadioButton) mRadioGroup.getChildAt(radioId);
                        String selection = (String) btn.getText();
                        userQA.setAnswer(selection);
                        userQARef.child(userQA.getQuestionId()).setValue(userQA).addOnSuccessListener(SurveyTab_Fragment.this);
                        if(index<surveyList.size() && index!=0){
                            updateUI(surveyList.get(index));
                            userQAHashMap.put(surveyQ,userQA);
                        }else if(index == surveyList.size()){
                            hideCardViewComponent();
                        }

                    }
                }
            });


        }


    }
    private  void isScaleQuestion(final SurveyQ surveyQ){
        if(surveyQ.getType().equals(SC_TYPE) && surveyList.contains(surveyQ)){
            index = surveyList.indexOf(surveyQ)+1;
            mTextView.setVisibility(View.VISIBLE);
            mTextView.setText(surveyQ.getQuestion());
            progressText .setVisibility(View.VISIBLE);
            progressText.setText(index +"/"+surveyList.size());
            skip.setVisibility(View.VISIBLE);

            mSeekBar.setVisibility(View.VISIBLE);
            mSeekBar.setProgress(5f);
            mSpinner.setVisibility(View.GONE);
            mRadioGroup.setVisibility(View.GONE);



            mSubmit.setVisibility(View.VISIBLE);
            msubTextview.setVisibility(View.VISIBLE);




            final UserQA userQA = new UserQA();
            userQA.setQuestion(surveyQ.getQuestion());
            userQA.setQuestionId(surveyQ.getQuestionId());
            currentUserQA = userQA;
            //default is false;
            userQA.setFavorite(false);


            skip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    userQA.setFavorite(false);
                    userQA.setAnswer("skipped");
                    userQARef.child(userQA.getQuestionId()).setValue(userQA).addOnSuccessListener(SurveyTab_Fragment.this);
                    if(index<surveyList.size()&& index!=0){
                        updateUI(surveyList.get(index));
                        userQAHashMap.put(surveyQ,userQA);
                    }else if(index == surveyList.size()){
                        hideCardViewComponent();
                    }
                }
            });

            mSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String answer = String.valueOf( mSeekBar.getProgress());
                    userQA.setAnswer(answer);
                    userQARef.child(userQA.getQuestionId()).setValue(userQA).addOnSuccessListener(SurveyTab_Fragment.this);
                    if(index<surveyList.size() && index!=0){
                        updateUI(surveyList.get(index));
                        userQAHashMap.put(surveyQ,userQA);
                    }else if(index ==surveyList.size()){
                        hideCardViewComponent();
                    }
                }
            });



        }

    }
    private  void isSpinnerQuestion(final SurveyQ surveyQ){
        if(surveyQ.getType().equals(SP_TYPE) && surveyList.contains(surveyQ)){
            index = surveyList.indexOf(surveyQ)+1;
            mTextView.setVisibility(View.VISIBLE);
            mTextView.setText(surveyQ.getQuestion());
            progressText .setVisibility(View.VISIBLE);
            progressText.setText(index +"/"+surveyList.size());
            skip.setVisibility(View.VISIBLE);

            mSeekBar.setVisibility(View.GONE);
            mSpinner.setVisibility(View.VISIBLE);
            mRadioGroup.setVisibility(View.GONE);


            mSubmit.setVisibility(View.VISIBLE);
            msubTextview.setVisibility(View.GONE);
        }

        if(!surveyQ.getAnswer().isEmpty()) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, surveyQ.getAnswer());
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpinner.setAdapter(adapter);


            final UserQA userQA = new UserQA();
            userQA.setQuestion(surveyQ.getQuestion());
            userQA.setQuestionId(surveyQ.getQuestionId());
            currentUserQA = userQA;
            //default is false;
            userQA.setFavorite(false);



            skip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    userQA.setAnswer("skipped");
                    userQA.setFavorite(false);
                    userQARef.child(userQA.getQuestionId()).setValue(userQA).addOnSuccessListener(SurveyTab_Fragment.this);
                    if(index<surveyList.size() && index!=0){
                        updateUI(surveyList.get(index));
                        userQAHashMap.put(surveyQ,userQA);
                    }else if(index == surveyList.size()){
                        hideCardViewComponent();
                    }
                }
            });

            mSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String text =  mSpinner.getSelectedItem().toString();
                    userQA.setAnswer(text);
                    userQARef.child(userQA.getQuestionId()).setValue(userQA).addOnSuccessListener(SurveyTab_Fragment.this);

                    if(index<surveyList.size()&& index!=0){
                        updateUI(surveyList.get(index));
                        userQAHashMap.put(surveyQ,userQA);
                    }else if(index ==surveyList.size()){
                        hideCardViewComponent();
                    }
                }
            });
        }


    }





    private void hideCardViewComponent(){
        feedbackPannel.setVisibility(View.VISIBLE);
        cardView.setVisibility(View.GONE);
       mTextView.setVisibility(View.GONE);
        progressText.setVisibility(View.GONE);
        mSpinner.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.INVISIBLE);
        mRadioGroup.setVisibility(View.GONE);
        mSubmit.setVisibility(View.GONE);
        msubTextview.setVisibility(View.GONE);
        mSeekBar.setVisibility(View.INVISIBLE);
        skip.setVisibility(View.GONE);
        backArrow.setVisibility(View.GONE);
        progressText.setVisibility(View.GONE);

        //reset tutorial
        //MaterialShowcaseView.resetSingleUse(getActivity(), "survey_tutorial");

        // sequence example
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view


        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), "survey_tutorial");

        sequence.setConfig(config);

        sequence.addSequenceItem(moreQuestion,
                "Click here to load more questions", "GOT IT");

        sequence.addSequenceItem(connect,
                "Click here to connect with people nearby", "GOT IT");

        sequence.addSequenceItem(feedback,
                "Click here to give us feedback on Google Play Store , we want to make this app better for everyone", "GOT IT");

        sequence.start();










    }

    public void showLog(String str){
        Log.d(TAG,str);
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

    public void setScoreNode(String user2Uid,String score){
        DatabaseReference scoreRef = FirebaseDatabase.getInstance().getReference()
                .child("UserFriends")
                .child(currentUser.getUid())
                .child(user2Uid)
                .child("score");
        scoreRef.setValue(score);

        DatabaseReference scoreRef2 = FirebaseDatabase.getInstance().getReference()
                .child("UserFriends")
                .child(user2Uid)
                .child(currentUser.getUid())
                .child("score");

        scoreRef2.setValue(score).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                Log.d(TAG,""+ ChilderenCounter + " " + Counter);

            }
        });



    }


    @Override
    public void onSuccess(Object o) {

    }

    private String getPrivacySharedPreference(){
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String defaultValue = "private";
        String privacy = sharedPref.getString(currentUser.getUid()+"privacy", defaultValue);
        return privacy;
    }

    private void showSingleChoiceDialog() {

        new MaterialDialog.Builder(getContext())
                .title(R.string.radius_title)
                .items(R.array.radius)
                .itemsCallbackSingleChoice(index, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        index =which;
                        String radius = String.valueOf(text);
                        Intent intent = new Intent(getActivity(),PeopleNearby.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        intent.putExtra("radius",radius);
                        intent.putExtra("index",index);
                        startActivity(intent);
                        return true;
                    }
                })
                .positiveText(R.string.ok).show();
    }








}
