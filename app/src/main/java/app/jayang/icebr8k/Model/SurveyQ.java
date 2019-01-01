package app.jayang.icebr8k.Model;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by LoLJay on 10/22/2017.
 */

public class SurveyQ {
    private String type, question, questionId;
    private ArrayList<String> answer;
    private Integer likes, dislikes;

    public SurveyQ() {

    }

    public SurveyQ(String type, String question, String questionId, ArrayList<String> answer) {
        this.type = type;
        this.question = question;
        this.questionId = questionId;
        this.answer = answer;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public Integer getDislikes() {
        return dislikes;
    }

    public void setDislikes(Integer dislikes) {
        this.dislikes = dislikes;
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


    public void setAnswer(ArrayList<String> answer) {
        this.answer = answer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SurveyQ surveyQ = (SurveyQ) o;
        return Objects.equals(questionId, surveyQ.questionId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(questionId);
    }

    @Override
    public String toString() {
        return "SurveyQ{" +
                "\nquestion='" + question + '\'' +
                ",\nquestionId='" + questionId + '\'' +
                ",\nanswer=" + answer +
                '}';
    }
}
