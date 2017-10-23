package app.jayang.icebr8k;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutCompat;
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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
    SurveyQ Q1,Q2,Q3,Q4;
    ArrayList<SurveyQ> surveyQArrayList;
    ProgressBar mProgressBar;
    Spinner mSpinner;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;
    int index =0 ;
    String ID;




    public SurveyTab_Fragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        surveyQArrayList = new ArrayList<>();
        ID = UUID.randomUUID().toString();

        ArrayList answer = new ArrayList();
        answer.add("Yes");
        answer.add("No");

        Q1 = new SurveyQ("mc","Do you like music ?",ID,answer);
        Q2 = new SurveyQ("mc","Do you like sport ?",ID,answer);

        ArrayList answer2 = new ArrayList();
        answer2.add("Action");
        answer2.add("Comedy");
        answer2.add("Adventure");
        answer2.add("Crime");
        answer2.add("Drama");
        answer2.add("Fantasy");
        answer2.add("Historical");
        answer2.add("Science Fiction");




        Q3 = new SurveyQ("sp"," What kind of movie do you like the most ?",ID,answer2);
        Q4 = new SurveyQ("sc"," Scale 1 to 10, how do you like soccer ?",ID,null);
        surveyQArrayList.add(Q1);
        surveyQArrayList.add(Q2);
        surveyQArrayList.add(Q3);
        surveyQArrayList.add(Q4);


       /* mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference();
        HashMap hashMap = new HashMap();
        hashMap.put(Q1.getQuestionId(),Q1);
        mRef.child("Questions").updateChildren(hashMap);
*/




    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable  ViewGroup container, @Nullable final Bundle savedInstanceState) {

      mview = inflater.inflate(R.layout.survey_tab,container,false);
        mSubmit = mview.findViewById(R.id.submitBtn);
        mSeekBar = mview.findViewById(R.id.seekBar);
        mTextView = mview.findViewById(R.id.question_id);
        mRadioGroup =mview.findViewById(R.id.radioGroup);
        mProgressBar = mview.findViewById(R.id.survey_progressBar);
        mSpinner=mview.findViewById(R.id.spinner_id);
        msubTextview = mview.findViewById(R.id.sub_question_id);

        updateUI(surveyQArrayList.get(index));



        mSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if(mRadioGroup.getCheckedRadioButtonId()==-1&& mRadioGroup.getVisibility()==View.VISIBLE){
                    Toast.makeText(getContext(),"Make a selection",Toast.LENGTH_SHORT).show();
                }else {

                    index = (index + 1) % surveyQArrayList.size();
                    if(index == 0){
                        mProgressBar.setProgress(0);
                    }
                    updateUI(surveyQArrayList.get(index));

                }
            }
        });

        return  mview;
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
        if(index == surveyQArrayList.size()-1){
            mProgressBar.setProgress(100);
        }else {
            mProgressBar.incrementProgressBy((int) Math.ceil(100 / surveyQArrayList.size()));
        }

        if(surveyQ.getType().equals("mc")) {
            setBelowRadioGroup();

            mTextView.setText(surveyQ.getQuestion());
            for (int i = 0; i < surveyQ.getAnswer().size(); i++) {
                RadioButton button = new RadioButton(getContext());
                button.setText(surveyQ.getAnswer().get(i));
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




}
