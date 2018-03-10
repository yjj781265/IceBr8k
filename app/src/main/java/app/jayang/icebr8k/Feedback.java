package app.jayang.icebr8k;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.beardedhen.androidbootstrap.BootstrapButton;

public class Feedback extends AppCompatActivity {
    private EditText email, subject,message;
    private BootstrapButton send;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        subject = findViewById(R.id.feedback_subject);
        message = findViewById(R.id.feedback_message);
        send = findViewById(R.id.feedback_button);
        toolbar = findViewById(R.id.feedback_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        send.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                //Get and Set editText value in String.
                String to = "icebr8kdev@gmail.com";
                String subjectmsg =  subject.getText().toString();
                String messagemsg = message.getText().toString();

                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{ to});
                email.putExtra(Intent.EXTRA_SUBJECT, subjectmsg);
                email.putExtra(Intent.EXTRA_TEXT, messagemsg);

                //This will show prompts of email intent
                email.setType("message/rfc822");
                startActivity(Intent.createChooser(email, "Choose an Email sender :"));
            }

        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return  true;
    }
}
