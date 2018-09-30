package app.jayang.icebr8k.Model;

public class TagModel {
  private  String tagtxt,tagId,authorId,questionId;
  private  Long timestamp;
  private  Integer likes,dislikes;
  private  String stats;


    public String getTagtxt() {
        return tagtxt;
    }

    public void setTagtxt(String tagtxt) {
        this.tagtxt = tagtxt;
    }

    public TagModel(String tagtxt, String tagId, String authorId, Long timestamp, String questionId,String stats) {
        this.tagtxt = tagtxt;
        this.tagId = tagId;
        this.authorId = authorId;
        this.timestamp = timestamp;
        this.questionId = questionId;
        this.stats = stats;
    }

    public String getStats() {
        return stats;
    }

    public void setStats(String stats) {
        this.stats = stats;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
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
}
