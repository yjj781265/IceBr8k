package app.jayang.icebr8k.CreateQuestionActivities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import app.jayang.icebr8k.R;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class CreateQuestionHomePage extends SwipeBackActivity {
    private Button mcBtn,scBtn;
    private Toolbar toolbar;
    public static final String MULTIPLE_CHOICE = "mc";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_question_home_page);
        toolbar = (Toolbar) findViewById(R.id.create_question_hp_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mcBtn = (Button) findViewById(R.id.create_question_hp_mc_btn);
        scBtn = (Button) findViewById(R.id.create_question_hp_sc_btn);
        mcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              toActivity(CreateQuestionInput.class,true);
            }
        });

        scBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                toActivity(CreateQuestionInput.class,false);
            }
        });
    }

    private void toActivity(Class clazz,boolean isMC){
        Intent intent = new Intent(this,clazz);
        intent.putExtra(MULTIPLE_CHOICE,isMC);
        startActivity(intent);
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
