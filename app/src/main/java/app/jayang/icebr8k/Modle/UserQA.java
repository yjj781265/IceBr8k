package app.jayang.icebr8k.Modle;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by yjj781265 on 11/2/2017.
 */

public class UserQA implements Parcelable,Comparable<UserQA>,Serializable {
    private String questionId,answer,question;
    private Boolean like,favorite;
    private String type;
    private String numComments;
    private Long reset;


    public UserQA(){}


    public UserQA(String questionId, String answer, String question, Boolean like) {
        this.questionId = questionId;
        this.answer = answer;
        this.question = question;
        this.like = like;

    }

    public UserQA(String questionId, String answer, String question, String type) {
        this.questionId = questionId;
        this.answer = answer;
        this.question = question;
        this.type = type;
    }

    public Long getReset() {
        return reset;
    }

    public void setReset(Long reset) {
        this.reset = reset;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNumComments() {
        return numComments;
    }

    public void setNumComments(String numComments) {
        this.numComments = numComments;
    }

    public Boolean getLike() {
        return like;
    }

    public void setLike(Boolean like) {
        this.like = like;
    }

    public Boolean getFavorite() {
        return favorite;
    }

    public void setFavorite(Boolean favorite) {
        this.favorite = favorite;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserQA userQA = (UserQA) o;

        if(questionId.equals(userQA.questionId) && isInteger(answer) && isInteger(userQA.answer) ){
            return Math.abs((Integer.valueOf(answer) -  Integer.valueOf(userQA.getAnswer()))) <=1;
        }else{
            return questionId.equals(userQA.getQuestionId()) && questionId!=null && userQA!=null
                    && answer.equals(userQA.answer);
        }
    }

    @Override
    public int hashCode() {
        int result = questionId.hashCode();
        result = 31 * result + answer.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "UserQA{" +  ", question='" + question + '\''+
                "answer='" + answer + '\'' + '}';
    }



    @Override
    public int describeContents() {
        return 0;
    }

    public UserQA(Parcel in){
      this.questionId = in.readString();
        this.answer = in.readString();
        this.question = in.readString();
        this.numComments = in.readString();

    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.questionId);
        parcel.writeString(this.answer);
        parcel.writeString(this.question);
        parcel.writeString(this.numComments);

    }

  public static final Parcelable.Creator CREATOR = new Parcelable.Creator(){
      @Override
      public UserQA createFromParcel(Parcel parcel) {
          return  new UserQA(parcel);
      }

      @Override
      public UserQA[] newArray(int i) {
          return new UserQA[i];
      }
  };


    @Override
    public int compareTo(@NonNull UserQA userQA) {
        // remove leading space
        return question.trim().compareTo(userQA.getQuestion().trim());
    }


    public static boolean isInteger(String s) {
        return isInteger(s,10);
    }

    public static boolean isInteger(String s, int radix) {
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i),radix) < 0) return false;
        }
        return true;
    }
}
