package app.jayang.icebr8k;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yjj781265 on 11/2/2017.
 */

public class UserQA implements Parcelable {
    private String questionId,answer,question;

    public UserQA(){}


    public UserQA(String questionId, String answer, String question) {
        this.questionId = questionId;
        this.answer = answer;
        this.question = question;
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

        if (questionId != null ? !questionId.equals(userQA.questionId) : userQA.questionId != null)
            return false;
        return answer != null ? answer.equals(userQA.answer) : userQA.answer == null;

    }

    @Override
    public String toString() {
        return "UserQA{" +  ", question='" + question + '\''+
                "answer='" + answer + '\'' + '}';
    }

    @Override
    public int hashCode() {
        int result = questionId != null ? questionId.hashCode() : 0;
        result = 31 * result + (answer != null ? answer.hashCode() : 0);
        return result;
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


}
