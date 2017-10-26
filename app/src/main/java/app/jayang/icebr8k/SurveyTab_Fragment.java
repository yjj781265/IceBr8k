package app.jayang.icebr8k;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.xw.repo.BubbleSeekBar;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;


/**
 * Created by LoLJay on 10/22/2017.
 */

public class SurveyTab_Fragment extends Fragment {
    View mview;
    BubbleSeekBar mSeekBar;
    TextView mTextView,msubTextview;
    RadioGroup mRadioGroup;
    Button mSubmit;

    ArrayList<SurveyQ> surveyQArrayList; // for unpdating UI
    ArrayList<String> userQlist;
    ArrayList<String> surveyQlist; // for comparing with userQArraylist
    ProgressBar mProgressBar,mProgressBar2;
    Spinner mSpinner;
   RelativeLayout mRelativeLayout;
    RelativeLayout mlayout;
    int index  ;




    public SurveyTab_Fragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        surveyQArrayList = new ArrayList<>();
        userQlist = new ArrayList<>();
        surveyQlist = new ArrayList<>();

        index =0;
      //  new UploadQ().updataQdatabase(FirebaseDatabase.getInstance().getReference());





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
        mlayout = mview.findViewById(R.id.cardView_RLayout);
        mProgressBar2= mview.findViewById(R.id.progressBar2);
        mProgressBar2.setVisibility(View.VISIBLE);
        mRelativeLayout = mview.findViewById(R.id.cardView_RLayout);

        mSubmit.setVisibility(View.INVISIBLE);












        mSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Toast.makeText(mview.getContext(),String.valueOf(index),Toast.LENGTH_SHORT).show();
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

        return  mview;
    }

    @Override
    public void onStart() {
        super.onStart();
        createInitQ();

    }

    @Override
    public void onPause() {
        super.onPause();

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
                RadioButton button = new RadioButton(getContext());
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
            mProgressBar2.setVisibility(View.INVISIBLE);
            mSubmit.setVisibility(View.INVISIBLE);
            mRadioGroup.setVisibility(View.GONE);
            mSpinner.setVisibility(View.INVISIBLE);
            mSeekBar.setVisibility(View.INVISIBLE);
            mTextView.setText("Check back later for more question");
            index =0;
            surveyQArrayList.clear();

        }else {
            updateUI(surveyQArrayList.get(index));
        }


    }

    // create initial 8 question;
public void createInitQ(){

    DatabaseReference mRef= FirebaseDatabase.getInstance().getReference("Questions_8");

    mRef.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            surveyQlist.clear();

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
public void pushUserQA(SurveyQ marr,String answer) {
    DatabaseReference mReference = FirebaseDatabase.getInstance().getReference("UserQA");
    HashMap<String,Object> map = new HashMap<>();
    map.put(marr.getQuestionId(),answer);
    mReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(map);

}

public void createUserQList(){
    userQlist.clear();
    DatabaseReference mref = FirebaseDatabase.getInstance().getReference
            ("UserQA/"+FirebaseAuth.getInstance().getCurrentUser().getUid());
    mref.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            for(DataSnapshot questionSnapchat: dataSnapshot.getChildren()){

                String q_id = questionSnapchat.getKey();
                if(!q_id.isEmpty()) {

                    userQlist.add(q_id);


                }
            }
            if(userQlist.isEmpty()){
                Snackbar.make(mview,"User hasn't answer any questions yet",Snackbar.LENGTH_LONG).show();
                new MaterialStyledDialog.Builder(getContext())
                        .setTitle("IceBr8k!")
                        .setDescription("Welcome to the IceBr8k, here are your first 8 questions")
                        .setStyle(Style.HEADER_WITH_TITLE)
                        .setPositiveText("Okay").onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                        .show();

            }
             // remove all the question user Answered from the list



            initQuestionPool(); // updateUI

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    });

}
    public void initQuestionPool(){
             surveyQlist.removeAll(userQlist);
        Log.d("debug","After Removal"+surveyQlist);


            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Questions_8");
            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        GenericTypeIndicator<ArrayList<String>> t = new GenericTypeIndicator<ArrayList<String>>() {};
                        ArrayList<String> answer = childSnapshot.child("answer").getValue(t);

                        String type = childSnapshot.child("type").getValue(String.class);
                        String question = childSnapshot.child("question").getValue(String.class);
                        String question_id = childSnapshot.child("questionId").getValue(String.class);

                        SurveyQ surveyQ = new SurveyQ(type, question, question_id, answer);
                        Log.d("debug",surveyQlist.toString());

                        if(surveyQlist.contains(surveyQ.getQuestionId())) {
                            surveyQArrayList.add(surveyQ);

                        }

                    }
                    Log.d("debug",String.valueOf(surveyQArrayList.size()));
                    if(!surveyQArrayList.isEmpty()) {
                        mProgressBar2.setVisibility(View.INVISIBLE);
                        mSubmit.setVisibility(View.VISIBLE);
                        updateUI(surveyQArrayList.get(index));
                    }else{ // no questions
                        mProgressBar2.setVisibility(View.INVISIBLE);
                        mSubmit.setVisibility(View.INVISIBLE);
                        mTextView.setText("Check back later for more questions");

                    }

                }


                @Override
                public void onCancelled(DatabaseError databaseError) {

                }

            });






        Log.d("size123",String.valueOf(surveyQArrayList.size()));





    }


}
