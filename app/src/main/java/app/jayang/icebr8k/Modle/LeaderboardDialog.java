package app.jayang.icebr8k.Modle;

import android.support.annotation.NonNull;

import java.util.Objects;

public class LeaderboardDialog implements Comparable<LeaderboardDialog>{
    private User user;
    private Long questionSum;
    private String id,rank;

    public LeaderboardDialog() {
    }

    public LeaderboardDialog(User user, Long questionSum, String id, String rank) {
        this.user = user;
        this.questionSum = questionSum;
        this.id = id;
        this.rank = rank;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getQuestionSum() {
        return questionSum;
    }

    public void setQuestionSum(Long questionSum) {
        this.questionSum = questionSum;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LeaderboardDialog that = (LeaderboardDialog) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "LeaderboardDialog{" +
                "user=" + user +
                ", questionSum=" + questionSum +
                ", id='" + id + '\'' +
                ", rank='" + rank + '\'' +
                '}';
    }

    @Override
    public int compareTo(@NonNull LeaderboardDialog o) {
        if(o.getQuestionSum().compareTo(this.getQuestionSum()) ==0){
            this.setRank(o.getRank());
            return  this.getUser().getDisplayname().compareTo(o.getUser().getDisplayname());
        }else{
            return o.getQuestionSum().compareTo(this.getQuestionSum());
        }

    }
}
