package app.jayang.icebr8k.Fragments;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
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
import java.util.Random;
import java.util.UUID;

import app.jayang.icebr8k.Modle.SurveyQ;
import app.jayang.icebr8k.Modle.UserQA;
import app.jayang.icebr8k.R;


/**
 * Created by LoLJay on 10/22/2017.
 */

public class SurveyTab_Fragment extends Fragment {
    View mview;
    BubbleSeekBar mSeekBar;
    TextView mTextView,msubTextview;
    RadioGroup mRadioGroup;
    Button mSubmit;
    CardView mCardView;
    TextView skip;
    FloatingActionButton mActionButton;


    ArrayList<SurveyQ> surveyQArrayList,temp; // for unpdating UI
    ArrayList<String> userQlist;
    ArrayList<String> surveyQlist; // for comparing with userQArraylist

    ProgressBar mProgressBar,mProgressBar2;
    Spinner mSpinner;
   RelativeLayout mRelativeLayout;
    RelativeLayout mlayout;
    FirebaseUser currentUser;
    int index  ;




    public SurveyTab_Fragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




        currentUser = FirebaseAuth.getInstance().getCurrentUser();


        index =0;
       // new UploadQ().updataQdatabase(FirebaseDatabase.getInstance().getReference());

        ArrayList<String> answers = new ArrayList();
        answers.add("A");
        answers.add("B");
        SurveyQ q8 = new SurveyQ("mc","A or B?", UUID.randomUUID().toString(),answers);
       // mRef.child("Questions_8").child(q8.getQuestionId()).setValue(q8);





    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable  ViewGroup container, @Nullable final Bundle savedInstanceState) {

      mview = inflater.inflate(R.layout.survey_tab,container,false);
        mSubmit = mview.findViewById(R.id.submitBtn);
        mSeekBar = mview.findViewById(R.id.seekBar);
        mTextView = mview.findViewById(R.id.question_id);
        mRadioGroup =mview.findViewById(R.id.radioGroup);
        mProgressBar = mview.findViewById(R.id.survey_progressBar);
        mSpinner=mview.findViewById(R.id.spinner_id);
        msubTextview = mview.findViewById(R.id.sub_question_id);
        mCardView =mview.findViewById(R.id.cardView);
        mlayout = mview.findViewById(R.id.cardView_RLayout);
        mProgressBar2= mview.findViewById(R.id.progressBar2);
        mProgressBar2.setVisibility(View.VISIBLE);
        mRelativeLayout = mview.findViewById(R.id.cardView_RLayout);
        mActionButton= mview.findViewById(R.id.floatingActionButton);
        skip = mview.findViewById(R.id.skip_btn);
        skip.setVisibility(View.GONE);
        createInitQ();




        mActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActionButton.setClickable(false);
                mTextView.setVisibility(View.INVISIBLE);
                mProgressBar2.setVisibility(View.VISIBLE);

                createInitQ();// prevent user spam click the button ,may crash the program



            }
        });


        mSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {



               // Toast.makeText(mview.getContext(),String.valueOf(index),Toast.LENGTH_SHORT).show();
                //Toast.makeText(mview.getContext(),"SQ List "+surveyQArrayList.size(),Toast.LENGTH_SHORT).show();
                if(mRadioGroup.getCheckedRadioButtonId()==-1&& mRadioGroup.getVisibility()==View.VISIBLE){
                    Toast.makeText(getContext(),"Make a selection",Toast.LENGTH_SHORT).show();

                }else if(mRadioGroup.getCheckedRadioButtonId()!=-1&& mRadioGroup.getVisibility()==View.VISIBLE){
                    int id= mRadioGroup.getCheckedRadioButtonId();
                    View radioButton = mRadioGroup.findViewById(id);
                    int radioId = mRadioGroup.indexOfChild(radioButton);
                    RadioButton btn = (RadioButton) mRadioGroup.getChildAt(radioId);
                    String selection = (String) btn.getText();
                    pushUserQA(surveyQArrayList.get(index),selection);
                    updateCardView();

                }else if(mSpinner.getVisibility()==View.VISIBLE) {
                    String text = mSpinner.getSelectedItem().toString();
                    pushUserQA(surveyQArrayList.get(index),text);
                    updateCardView();


                }else if(mSeekBar.getVisibility()==View.VISIBLE){

                   String answer = String.valueOf(mSeekBar.getProgress());
                    pushUserQA(surveyQArrayList.get(index),answer);
                    updateCardView();


                }
            }
        });


        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(surveyQArrayList!=null || !surveyQArrayList.isEmpty()) {
                    pushUserQA(surveyQArrayList.get(index), "skipped");
                    updateCardView();
                }


            }
        });

        return  mview;
    }




    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();




    }


    // this method is triggered once the survey fragment is appeared to the user
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && getView()!=null) {
            if(mTextView.getText().toString().equals(getString(R.string.check_back_later))){
                createInitQ();
            }


        }

    }

    public void setBelowSeekbar(){
        RelativeLayout.LayoutParams params= new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, R.id.seekBar);
        params.setMargins(20,20,20,20);
        mSeekBar.setVisibility(View.VISIBLE);
        msubTextview.setVisibility(View.VISIBLE);

        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mRadioGroup.setVisibility(View.INVISIBLE);
        mSpinner.setVisibility(View.INVISIBLE);




        mSubmit.setLayoutParams(params);
        setSkipbtn();

    }

    public void setBelowRadioGroup(){
        RelativeLayout.LayoutParams params= new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, R.id.radioGroup);
        params.setMargins(15,15,15,15);
        mRadioGroup.setVisibility(View.VISIBLE);

        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mSeekBar.setVisibility(View.INVISIBLE);
        mSpinner.setVisibility(View.INVISIBLE);
        msubTextview.setVisibility(View.INVISIBLE);

        mSubmit.setLayoutParams(params);
        setSkipbtn();

    }

    public  void setSkipbtn(){
        RelativeLayout.LayoutParams params= new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, R.id.submitBtn);
        params.addRule(RelativeLayout.ALIGN_END,R.id.question_id);
        params.addRule(RelativeLayout.ALIGN_RIGHT,R.id.question_id);
        params.setMargins(0,0,5,10);
        skip.setLayoutParams(params);
        skip.setVisibility(View.VISIBLE);
    }


    public void setBelowSpinner(){
        RelativeLayout.LayoutParams params= new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, R.id.spinner_id);
        params.setMargins(15,15,15,15);
        mSpinner.setVisibility(View.VISIBLE);
        mRadioGroup.setVisibility(View.INVISIBLE);
        mSeekBar.setVisibility(View.INVISIBLE);
        msubTextview.setVisibility(View.INVISIBLE);

        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mSubmit.setLayoutParams(params);
        setSkipbtn();

    }

    public void updateUI(SurveyQ surveyQ){

        mRadioGroup.removeAllViews();
        mRadioGroup.clearCheck();
        if(index == surveyQArrayList.size()-1 ){
            mProgressBar.setProgress(100);
        }else {
            mProgressBar.incrementProgressBy( (100 / surveyQArrayList.size()));
        }

        if(surveyQ.getType().equals("mc")) {
            setBelowRadioGroup();

            mTextView.setText(surveyQ.getQuestion());
            for (int i = 0; i < surveyQ.getAnswer().size(); i++) {
                RadioButton button = new RadioButton(mview.getContext());
                button.setText(surveyQ.getAnswer().get(i).toString());
                mRadioGroup.addView(button);
            }
        }else if(surveyQ.getType().equals("sp")){
            setBelowSpinner();
            mTextView.setText(surveyQ.getQuestion());
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item,surveyQ.getAnswer());
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpinner.setAdapter(adapter);
            // String selected = sItems.getSelectedItem().toString();


        }else{
            setBelowSeekbar();
            mTextView.setText(surveyQ.getQuestion());

        }
    }
    public void updateCardView(){

        index = (index + 1);
        if(index >= surveyQArrayList.size()){
            mProgressBar.setProgress(0);
            hideCardViewComponent();

            mTextView.setText(getString(R.string.check_back_later));
            mActionButton.setClickable(true);
            YoYo.with(Techniques.FadeIn).duration(1000).repeat(0).playOn(mActionButton);
            mActionButton.setVisibility(View.VISIBLE);
            surveyQArrayList.clear();

        }else {
            YoYo.with(randomAnime())
                    .duration(500)
                    .repeat(0)
                    .playOn(mCardView);

            updateUI(surveyQArrayList.get(index));
        }


    }

    // create initial question;
public void createInitQ(){

    mProgressBar.setProgress(0);

    surveyQlist = new ArrayList<>();

    DatabaseReference mRef= FirebaseDatabase.getInstance().getReference("Questions_8");
    mRef.keepSynced(true);

    mRef.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            surveyQlist.clear();
// pull all the questions from questions table just hte questions id
            for (DataSnapshot surveySnapshot : dataSnapshot.getChildren()) {


               /* GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {};
                ArrayList<String> answer = surveySnapshot.child("answer").getValue(t);*/
                String q_id = surveySnapshot.child("questionId").getValue(String.class);

                surveyQlist.add(q_id);
            }

            createUserQList();



        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    });



}
// push the user survy data to the userQA doc in firebase
public void pushUserQA(SurveyQ surveyQ,String answer) {
    DatabaseReference mReference = FirebaseDatabase.getInstance().getReference("UserQA");
    mReference.keepSynced(true);
    UserQA userQA = new UserQA(surveyQ.getQuestionId(),answer,surveyQ.getQuestion());
    mReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(userQA.getQuestionId()).setValue(userQA);

}

public void createUserQList(){
    userQlist = new ArrayList<>();
    // get all the questions user answered
    DatabaseReference mref = FirebaseDatabase.getInstance().getReference
            ("UserQA/"+FirebaseAuth.getInstance().getCurrentUser().getUid());
    mref.keepSynced(true);
    mref.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            userQlist.clear();
            for(DataSnapshot questionSnapchat: dataSnapshot.getChildren()){

               UserQA  userQA = questionSnapchat.getValue(UserQA.class);
                if(!userQA.getQuestionId().isEmpty()) {

                    userQlist.add(userQA.getQuestionId());


                }
            }
            if(userQlist.isEmpty()){
                skip.setVisibility(View.GONE);
                new MaterialStyledDialog.Builder(getContext())
                        .setTitle("IceBr8k!")
                        .setDescription("Welcome to the IceBr8k, please answer some simple questions to get this wonderful journey started")
                        .setStyle(Style.HEADER_WITH_TITLE)
                        .setPositiveText("Okay").onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        dialog.dismiss();
                    }
                })
                        .show();

            }




            initQuestionPool(); // updateUI

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    });

}
    public void initQuestionPool(){
        surveyQArrayList = new ArrayList<>();
        surveyQlist.removeAll(userQlist);
        // remove all the question user Answered from the list

        Log.d("debug","After Removal"+surveyQlist);



            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Questions_8");
            mRef.keepSynced(true);
            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    temp = new ArrayList<>();
                    for(DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {};
                        ArrayList<String> answer = childSnapshot.child("answer").getValue(t);

                        String type = childSnapshot.child("type").getValue(String.class);
                        String question = childSnapshot.child("question").getValue(String.class);
                        String question_id = childSnapshot.child("questionId").getValue(String.class);

                        SurveyQ surveyQ = new SurveyQ(type, question, question_id, answer);
                        Log.d("debug2",surveyQlist.toString());


                        if(surveyQlist.contains(surveyQ.getQuestionId())) {
                            temp.add(surveyQ);

                        }


                    }

                   if(temp.size()>=8){
                        Collections.shuffle(temp);
                        for(int i=0; i<8;i++){
                            surveyQArrayList.add(temp.get(i));
                        }
                    }else{
                        surveyQArrayList =temp;
                    }
                    Log.d("debug",String.valueOf(surveyQArrayList.size()));
                   //user has some question didint answer
                    if(!surveyQArrayList.isEmpty() && surveyQArrayList!=null) {
                        mTextView.setVisibility(View.VISIBLE);
                        index =0;
                        Collections.shuffle(surveyQArrayList);
                        hideCardViewComponent();
                        mProgressBar.setVisibility(View.VISIBLE);
                        mActionButton.setVisibility(View.GONE);
                        mSubmit.setVisibility(View.VISIBLE);
                        updateUI(surveyQArrayList.get(index));
                    }else{ // no questions
                        hideCardViewComponent();
                        mTextView.setVisibility(View.VISIBLE);
                        mActionButton.setVisibility(View.VISIBLE);
                        mTextView.setText(getString(R.string.check_back_later));
                        mActionButton.setClickable(true);

                    }

                }


                @Override
                public void onCancelled(DatabaseError databaseError) {

                }

            });






        Log.d("size123",String.valueOf(surveyQArrayList.size()));





    }

    public  static Techniques randomAnime(){
       ArrayList<Techniques> techniquesArrayList = new ArrayList<>();
        techniquesArrayList.add(Techniques.FadeIn);
        techniquesArrayList.add(Techniques.Bounce);
         int index = new Random().nextInt(techniquesArrayList.size()-1);

        return techniquesArrayList.get(index);

    }

    private void hideCardViewComponent(){
        mSpinner.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mProgressBar2.setVisibility(View.GONE);
        mRadioGroup.setVisibility(View.GONE);
        mSubmit.setVisibility(View.GONE);
        msubTextview.setVisibility(View.GONE);
        mSeekBar.setVisibility(View.INVISIBLE);
        skip.setVisibility(View.GONE);

    }




}
