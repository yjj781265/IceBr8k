package app.jayang.icebr8k;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * Created by yjj781265 on 10/25/2017.
 */

public class UploadQ {

    public  UploadQ(){

    }

    public  void updataQdatabase(DatabaseReference mRef){


        // create 8 questions
        ArrayList answer1 = new ArrayList();
        answer1.add("Yes");
        answer1.add("No");
        SurveyQ q1 = new SurveyQ("mc","Do you like sport ?", UUID.randomUUID().toString(),answer1);


        ArrayList answer2 = new ArrayList();
        answer2.add("Pop");
        answer2.add("Rock");
        answer2.add("Classical");
        answer2.add("Country");
        answer2.add("Blues");
        answer2.add("Hip-Hop/Rap");
        answer2.add("Electronic");
        answer2.add("Jazz");
        answer2.add("R&B");
        answer2.add("Other");

        SurveyQ q2 = new SurveyQ("sp","What is your favorite kind of music?", UUID.randomUUID().toString(),answer2);

        ArrayList answer3 = new ArrayList();
        answer3.add("Action");
        answer3.add("Comedy");
        answer3.add("Adventure");
        answer3.add("Crime");
        answer3.add("Drama");
        answer3.add("Fantasy");
        answer3.add("Historical");
        answer3.add("Science Fiction");
        answer3.add("Other");

        SurveyQ q3 = new SurveyQ("sp","What is your favorite kind of movie?", UUID.randomUUID().toString(),answer3);

        ArrayList answer4 = new ArrayList();
        answer4.add("Coke");
        answer4.add("Pepsi");
        SurveyQ q4 = new SurveyQ("mc","Coke or Pepsi ?", UUID.randomUUID().toString(),answer4);

        ArrayList answer5 = new ArrayList();
        answer5.add("Playstation");
        answer5.add("Xbox");
        answer5.add("Wii");
        answer5.add("PC");
        answer5.add("Nintendo");
        answer5.add("Other");
        SurveyQ q5 = new SurveyQ("mc","Most favorite console ?", UUID.randomUUID().toString(),answer5);

        ArrayList answer6 = new ArrayList();
        answer6.add("Coffee");
        answer6.add("Tea");
        SurveyQ q6 = new SurveyQ("mc","Coffee or Tea ?", UUID.randomUUID().toString(),answer6);

        ArrayList answer7 = new ArrayList();
        answer7.add("Mac");
        answer7.add("PC");
        SurveyQ q7 = new SurveyQ("mc","Mac or PC ?", UUID.randomUUID().toString(),answer7);

        ArrayList answer8 = new ArrayList();
        answer8.add("Android");
        answer8.add("IOS");
        SurveyQ q8 = new SurveyQ("mc","Android or IOS ?", UUID.randomUUID().toString(),answer8);





        ArrayList<SurveyQ> initial8 = new ArrayList<>();

           SurveyQ[] arr = {q1,q2,q3,q4,q5,q6,q7,q8};
        for(SurveyQ surveyQ : arr){
            initial8.add(surveyQ);
            mRef.child("Questions_8").child(surveyQ.getQuestionId()).setValue(surveyQ);
            Log.d("Qtable",surveyQ.getQuestion());

        }




    }

}
