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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.xw.repo.BubbleSeekBar;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by LoLJay on 10/22/2017.
 */

public class SurveyTab_Fragment extends Fragment {
    View mview;
    BubbleSeekBar mSeekBar;
    TextView mTextView;
    RadioGroup mRadioGroup;
    Button mSubmit;
    SurveyQ Q1,Q2,Q3;
    ArrayList<SurveyQ> surveyQArrayList;
    int index =0 ;




    public SurveyTab_Fragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        surveyQArrayList = new ArrayList<>();
        String[] arr = {"Yes","No"};
        Q1 = new SurveyQ("mc","Do you like sport ?","q1",arr);

        Q2 = new SurveyQ("mc","Do you like music ?","q2",arr);
        String[] arr2 ={"like","dislike"};
        Q3 = new SurveyQ("sc","How do you like music ? (scale 0~10 \n  0:dislike 10:like )","q3",arr2);
        surveyQArrayList.add(Q1);
        surveyQArrayList.add(Q2);
        surveyQArrayList.add(Q3);





    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable  ViewGroup container, @Nullable final Bundle savedInstanceState) {

      mview = inflater.inflate(R.layout.survey_tab,container,false);
        mSubmit = mview.findViewById(R.id.submitBtn);
        mSeekBar = mview.findViewById(R.id.seekBar);
        mTextView = mview.findViewById(R.id.question_id);
        mRadioGroup =mview.findViewById(R.id.radioGroup);
        updateUI(surveyQArrayList.get(index));



        mSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if(mRadioGroup.getCheckedRadioButtonId()==-1&& mRadioGroup.getVisibility()==View.VISIBLE){
                    Toast.makeText(getContext(),"Make a selection",Toast.LENGTH_SHORT).show();
                }else {

                    index = (index + 1) % surveyQArrayList.size();

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

        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mRadioGroup.setVisibility(View.INVISIBLE);

        mSubmit.setLayoutParams(params);
    }

    public void setBelowRadioGroup(){
        RelativeLayout.LayoutParams params= new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, R.id.radioGroup);
        params.setMargins(15,15,15,15);
        mRadioGroup.setVisibility(View.VISIBLE);

        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mSeekBar.setVisibility(View.INVISIBLE);

        mSubmit.setLayoutParams(params);
    }

    public void updateUI(SurveyQ surveyQ){
        mRadioGroup.removeAllViews();
        mRadioGroup.clearCheck();

        if(surveyQ.getType().equals("mc")){
            setBelowRadioGroup();

            mTextView.setText(surveyQ.getQuestion());
            for(int i=0; i< surveyQ.getAnswer().length;i++){
                RadioButton button = new RadioButton(getContext());
                button.setText(surveyQ.getAnswer()[i]);
                mRadioGroup.addView(button);
            }



        }else{
            setBelowSeekbar();
            mTextView.setText(surveyQ.getQuestion());

        }
    }




}
