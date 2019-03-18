package app.jayang.icebr8k.CreateQuestionActivities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import app.jayang.icebr8k.Model.BadWordException;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.Utility.Icebr8kLanguageFilter;
import app.jayang.icebr8k.Utility.MyToolBox;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class CreateQuestionInput extends SwipeBackActivity implements TextWatcher {
    private EditText mEditText;
    private TextView counter,toAnswersBtn;
    private android.support.v7.widget.Toolbar toolbar;
    private final String CHAR_LIMT = "75";
    private FrameLayout frameLayout;
    public static final String EXTRA_KEY = "survey_question";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_question_input);
        frameLayout = (FrameLayout) findViewById(R.id.input_question_container);

        mEditText = (EditText) findViewById(R.id.input_question_et);
        mEditText.requestFocus();
        MyToolBox.showSoftKeyboard(mEditText);
        counter = (TextView) findViewById(R.id.input_question_counter);
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.input_question_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mEditText.addTextChangedListener(this);
        String counterStr = mEditText.getText().length() + "/" + CHAR_LIMT;
        counter.setText(counterStr );
        toAnswersBtn = (TextView) findViewById(R.id.mc_question_btn);
        frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditText.requestFocus();
                MyToolBox.showSoftKeyboard(mEditText);
            }
        });

        if(getIntent()!=null && getIntent().getExtras().containsKey(CreateQuestionHomePage.MULTIPLE_CHOICE)){
            if(getIntent().getExtras().getBoolean(CreateQuestionHomePage.MULTIPLE_CHOICE)){
                toAnswersBtn.setText(R.string.go_to_answers);
                toAnswersBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(isValidQuestion()){
                            toActivity(CreateQuestionMcAnswers.class);
                        }

                    }
                });
            }else{
                toAnswersBtn.setText(R.string.view_final_layout);
                toAnswersBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(isValidQuestion()){
                            toActivity(CreateQuestionFinalLayout.class);
                        }

                    }
                });
            }
        }




    }


    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        // do nothing
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        String counterStr = mEditText.getText().length() + "/" + CHAR_LIMT;
        counter.setText(counterStr );
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    private void toActivity(Class clazz){
        Intent intent = new Intent(this,clazz);
        intent.putExtra(EXTRA_KEY,mEditText.getText().toString().trim());
        startActivity(intent);
    }

    private boolean isValidQuestion(){
        boolean isValid = false;
        try {
            if(mEditText.getText().toString().isEmpty()){
                Toast.makeText(this, "Question can't be empty.", Toast.LENGTH_SHORT).show();
            }
            isValid =  !mEditText.getText().toString().isEmpty() && !Icebr8kLanguageFilter.containBadWord(mEditText.getText().toString());
        } catch (BadWordException e) {
            Toast.makeText(this, "Question contains censored word.", Toast.LENGTH_SHORT).show();
        }
        return  isValid;
    }
}
