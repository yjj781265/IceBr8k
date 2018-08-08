package app.jayang.icebr8k.Modle;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Objects;

public class ResultItem implements Serializable,Comparable<ResultItem> {

    String questionId, quesiton,answer1,answer2,comment,user2Id;


    public ResultItem() {
    }

    public ResultItem(String questionId, String quesiton, String answer1, String answer2, String comment, String user2Id) {
        this.questionId = questionId;
        this.quesiton = quesiton;
        this.answer1 = answer1;
        this.answer2 = answer2;
        this.comment = comment;
        this.user2Id = user2Id;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getQuesiton() {
        return quesiton;
    }

    public void setQuesiton(String quesiton) {
        this.quesiton = quesiton;
    }

    public String getAnswer1() {
        return answer1;
    }

    public void setAnswer1(String answer1) {
        this.answer1 = answer1;
    }

    public String getAnswer2() {
        return answer2;
    }

    public void setAnswer2(String answer2) {
        this.answer2 = answer2;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUser2Id() {
        return user2Id;
    }

    public void setUser2Id(String user2Id) {
        this.user2Id = user2Id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResultItem that = (ResultItem) o;
        return Objects.equals(questionId, that.questionId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(questionId);
    }

    @Override
    public int compareTo(@NonNull ResultItem o) {
        if(o!=null &&o.getQuesiton()!=null && quesiton!=null){
            return quesiton.trim().compareTo(o.getQuesiton().trim());
        }else{
            return 0;
        }

    }
}
