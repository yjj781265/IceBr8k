package app.jayang.icebr8k.Modle;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Created by yjj781265 on 11/2/2017.
 */

public class UserQA implements Parcelable,Comparable<UserQA> {
    private String questionId,answer,question;
    private Boolean favorite;

    public UserQA(){}


    public UserQA(String questionId, String answer, String question, Boolean favorite) {
        this.questionId = questionId;
        this.answer = answer;
        this.question = question;
        this.favorite = favorite;
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

        if (!questionId.equals(userQA.questionId)) return false;
        return (answer.equals(userQA.answer) && !answer.equals("skipped")
                && !userQA.answer.equals("skipped")) ;
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

    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.questionId);
        parcel.writeString(this.answer);
        parcel.writeString(this.question);

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
        return question.compareTo(userQA.getQuestion());
    }
}
