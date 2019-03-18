package app.jayang.icebr8k.CreateQuestionActivities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.UUID;

import app.jayang.icebr8k.Model.SurveyQ;
import app.jayang.icebr8k.R;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class CreateQuestionMcAnswers extends SwipeBackActivity {
    LinearLayout mcAnswersList ;
    android.support.v7.widget.Toolbar toolbar;
    ImageView add;
    TextView viewFinalLayoutBtn;
    private static final String TAG = "CreateQuestionMcAnswers";
    public static final String SURVEYQ_EXTRA_KEY = "answers";
    private String question;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_question_mc_answers);
        if(getIntent()!=null && getIntent().getExtras().containsKey(CreateQuestionInput.EXTRA_KEY)){
            question = getIntent().getExtras().getString(CreateQuestionInput.EXTRA_KEY);
        }
        Log.d(TAG, "onCreate: getIntent "+ question);

        mcAnswersList = (LinearLayout) findViewById(R.id.mc_answer_container);
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.mc_answer_toolbar);
        viewFinalLayoutBtn = (TextView) findViewById(R.id.mc_answer_view_final_layout_btn);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        add = (ImageView) findViewById(R.id.mc_answer_add_fab);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mcAnswersList.addView(bindItemView());
                updateLayout();
                handleAddBtnVisibility();
            }
        });
        viewFinalLayoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleViewFinalLayout();
            }
        });
        addFirstTwoItems();
    }


    void addFirstTwoItems(){
        if(mcAnswersList!=null){
          mcAnswersList.addView(bindItemView());
          mcAnswersList.addView(bindItemView());
          updateLayout();
        }
    }

    private void handleAddBtnVisibility(){
       add.setVisibility(mcAnswersList.getChildCount()>3 ?View.GONE :View.VISIBLE);
    }


    private View bindItemView(){
        final View mcItem = LayoutInflater.from(this).inflate(R.layout.item_mc_answer,mcAnswersList,false);
        ImageView delete = mcItem.findViewById(R.id.mc_answer_item_delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    mcAnswersList.removeView(mcItem);
                    updateLayout();
                    handleAddBtnVisibility();

                }catch (Exception e){
                    Log.e(TAG, "onClick: ",e );
                }

            }
        });

       return  mcItem;
    }

    private void updateLayout(){
        for(int i =0; i<mcAnswersList.getChildCount(); i++){
            View view = mcAnswersList.getChildAt(i);
            TextView textView =  view.findViewById(R.id.mc_answer_item_number);
            ImageView delete =  view.findViewById(R.id.mc_answer_item_delete);
            delete.setVisibility(i>1 ? View.VISIBLE : View.GONE);
            textView.setText(i+1+".");
            Log.d(TAG, "updateLayout: "+ i);
        }
    }

    private void handleViewFinalLayout(){

        if(question!=null){
            ArrayList<String>answers = new ArrayList<>();
            SurveyQ surveyQ = new SurveyQ();
            surveyQ.setType(SurveyQ.MULTIPLE_CHOICE);
            surveyQ.setQuestion(question);
            surveyQ.setQuestionId(UUID.randomUUID().toString());
            for(int i =0; i<mcAnswersList.getChildCount(); i++){
                View view = mcAnswersList.getChildAt(i);
                EditText editText =  view.findViewById(R.id.mc_answer_item_et);
                if(!editText.getText().toString().isEmpty()){
                    answers.add(editText.getText().toString().trim());
                    Log.d(TAG, "handleAddQuestionToDB: adding answer "+editText.getText());
                }

            }
            if(answers.size()<2){
                Toast.makeText(this, "At Least Two Answers", Toast.LENGTH_SHORT).show();
            }else{
                surveyQ.setAnswer(answers);
                Intent intent = new Intent(this,CreateQuestionFinalLayout.class);
                intent.putExtra(SURVEYQ_EXTRA_KEY,surveyQ);;
                startActivity(intent);
            }

        }else{
            Log.d(TAG, "handleAddQuestionToDB: unable to get question from intent");
        }


    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
