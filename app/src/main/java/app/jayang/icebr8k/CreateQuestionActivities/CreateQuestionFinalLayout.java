package app.jayang.icebr8k.CreateQuestionActivities;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

import app.jayang.icebr8k.Adapter.SurveyQuestionAdapter;
import app.jayang.icebr8k.Model.SurveyQ;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.Utility.MyToolBox;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

import static app.jayang.icebr8k.CreateQuestionActivities.CreateQuestionInput.EXTRA_KEY;
import static app.jayang.icebr8k.CreateQuestionActivities.CreateQuestionMcAnswers.SURVEYQ_EXTRA_KEY;

public class CreateQuestionFinalLayout extends SwipeBackActivity {
    private static final String TAG = "CreateQuestionFinalot";
    private Toolbar toolbar;
    private RelativeLayout  questionFinalLayout;
    private TextView submitBtn;
    private Context context;
    private static final String SURVEY_DB_NODE = "Questions_8";
    private RadioGroup mRadioGroup;
    private final FirebaseDatabase mFB = FirebaseDatabase.getInstance();
    TextView question;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        setContentView(R.layout.activity_create_question_final_layout);
        toolbar = (Toolbar) findViewById(R.id.create_question_final_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        submitBtn = (TextView) findViewById(R.id.mc_answer_view_final_layout_btn);
        if(getIntent()!=null&& getIntent().getExtras().containsKey(SURVEYQ_EXTRA_KEY)){
            Log.d(TAG, "onCreate: "+ getIntent().getSerializableExtra(SURVEYQ_EXTRA_KEY));
            SurveyQ surveyQ = (SurveyQ) getIntent().getSerializableExtra(SURVEYQ_EXTRA_KEY);
             bindMcView(surveyQ);


        }

        if(getIntent()!=null&& getIntent().getExtras().containsKey(EXTRA_KEY)){
            SurveyQ surveyQ = new SurveyQ(SurveyQ.SCALE,getIntent().getExtras().getString(EXTRA_KEY)
            ,UUID.randomUUID().toString(),null);
            bindScView(surveyQ);
        }
    }




    private void bindMcView(final SurveyQ surveyQ){
        Log.d(TAG, "bindMcView: ");
        questionFinalLayout = (RelativeLayout) findViewById(R.id.create_question_final_mc_layout);
        questionFinalLayout.setVisibility(View.VISIBLE);

        question = (TextView) findViewById(R.id.survey_mc_question);
        mRadioGroup = (RadioGroup) findViewById(R.id.survey_mc_radioGroup);
        question.setText(surveyQ.getQuestion());
        //Ui Stuff add radio buttons
        for (int i = 0; i < surveyQ.getAnswer().size(); i++) {
            RadioButton button = new RadioButton(this);
            button.setText(surveyQ.getAnswer().get(i));
            mRadioGroup.addView(button);
        }
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(surveyQ!=null){
                    submitToDB(surveyQ);
                }
            }
        });
    }

    private void bindScView(final SurveyQ surveyQ){
        questionFinalLayout = (RelativeLayout) findViewById(R.id.create_question_final_sc_layout);
        questionFinalLayout.setVisibility(View.VISIBLE);
        question = (TextView) findViewById(R.id.survey_sc_question);
        question.setText(surveyQ.getQuestion());
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(surveyQ!=null){
                    submitToDB(surveyQ);
                }
            }
        });
    }

    private void submitToDB(SurveyQ surveyQ){
        DatabaseReference databaseReference = mFB.getReference(SURVEY_DB_NODE).
                child(surveyQ.getQuestionId());
        databaseReference.setValue(surveyQ).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                MyToolBox.showToast("Question Submitted ",getApplicationContext());
                Intent intent = new Intent(context,CreateQuestionHomePage.class);
                startActivity(intent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                MyToolBox.showToast("Failed, "+e,getApplicationContext());
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
