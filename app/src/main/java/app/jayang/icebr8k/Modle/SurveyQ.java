package app.jayang.icebr8k.Modle;

import java.util.ArrayList;

/**
 * Created by LoLJay on 10/22/2017.
 */

public class SurveyQ {
    private String type, question,questionId;
    private ArrayList<String> answer;

    public SurveyQ() {

    }

    public SurveyQ(String type, String question, String questionId, ArrayList<String> answer) {
        this.type = type;
        this.question = question;
        this.questionId = questionId;
        this.answer = answer;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public ArrayList<String> getAnswer() {
        return answer;
    }

    public void setAnswer(ArrayList answer) {
        this.answer = answer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SurveyQ surveyQ = (SurveyQ) o;

        if (type != null ? !type.equals(surveyQ.type) : surveyQ.type != null) return false;
        return questionId.equals(surveyQ.questionId);
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + questionId.hashCode();
        return result;
    }
}
