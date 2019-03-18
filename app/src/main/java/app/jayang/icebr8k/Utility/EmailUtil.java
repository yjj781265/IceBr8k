package app.jayang.icebr8k.Utility;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseUser;

import net.sargue.mailgun.Configuration;
import net.sargue.mailgun.Mail;
import net.sargue.mailgun.MailRequestCallback;
import net.sargue.mailgun.Response;

import app.jayang.icebr8k.Model.SurveyQ;
import app.jayang.icebr8k.Model.TagModel;
import app.jayang.icebr8k.R;

public class EmailUtil {
    public static final String TAG_REPORT_SUBJECT = "Tag Report";
    public static final String QUESTION_REPORT_SUBJECT = "Question Report";
    private static final String TAG = "Icebr8k EmailUtil";
    private static final String ICEBR8K_SUPPORT_EMAIL_ADDRESS = "icebr8ksup@gmail.com";
    private Context context;
    private Configuration configuration;
    private EmailUtilListener listener;


    public EmailUtil(Context context) {
        this.context = context;
        listener = (EmailUtilListener) context;
        // Build a new authorized API client service.
        configuration = new Configuration()
                .domain("sandboxdb4c40b7d5cf4c458e7689fda789fd97.mailgun.org")
                .apiKey(context.getString(R.string.mailgun_api_key))
                .from("postmaster@sandboxdb4c40b7d5cf4c458e7689fda789fd97.mailgun.org");
    }


    public void sendTagReport(FirebaseUser user, final TagModel tag) {
        Mail.using(configuration).to(ICEBR8K_SUPPORT_EMAIL_ADDRESS).subject(TAG_REPORT_SUBJECT)
                .text(createTagReportContent(user, tag)).build().sendAsync(new MailRequestCallback() {
            @Override
            public void completed(Response response) {
                Log.d(TAG, "completed: " + response.responseMessage());
                listener.onCompleted(tag);


            }

            @Override
            public void failed(Throwable throwable) {
                listener.onFailed();
            }
        });
    }

    public void sendQuestionReport(FirebaseUser user, final SurveyQ surveyQ) {
        Mail.using(configuration).to(ICEBR8K_SUPPORT_EMAIL_ADDRESS).subject(QUESTION_REPORT_SUBJECT)
                .text(createQuestionReportContent(user, surveyQ)).build().sendAsync(new MailRequestCallback() {
            @Override
            public void completed(Response response) {
                Log.d(TAG, "completed: " + response.responseMessage());
                listener.onCompleted(surveyQ);
            }

            @Override
            public void failed(Throwable throwable) {
                listener.onFailed();
            }
        });
    }

    private String createTagReportContent(@NonNull FirebaseUser user, @NonNull TagModel tagModel) {
        String content = "Reporter: " + user.getDisplayName() + " with UID: " + user.getUid() +
                "\n User Email : " + user.getEmail() + "\nTag Info :" + tagModel.toString();
        return content;
    }

    private String createQuestionReportContent(@NonNull FirebaseUser user, @NonNull SurveyQ surveyQ) {
        String content = "Reporter: " + user.getDisplayName() + " with UID: " + user.getUid() +
                "\n User Email : " + user.getEmail() + "\nQuestion Info :" + surveyQ.toString();
        return content;
    }


}
