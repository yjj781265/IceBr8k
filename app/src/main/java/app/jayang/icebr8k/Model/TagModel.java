package app.jayang.icebr8k.Model;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

public class TagModel implements Comparable<TagModel> {
    private String tagtxt = null, tagId, authorId, questionId;
    private Long timestamp;
    private Long likes = 0L, dislikes = 0L;
    private String stats;

    public TagModel() {

    }

    public TagModel(String tagtxt, String tagId, String authorId,
                    Long timestamp, String questionId, String stats) {
        this.tagtxt = tagtxt;
        this.tagId = tagId;
        this.authorId = authorId;
        this.timestamp = timestamp;
        this.questionId = questionId;
        this.stats = stats;
    }

    public TagModel(String tagtxt, String tagId, String questionId) {
        this.tagtxt = tagtxt;
        this.tagId = tagId;
        this.questionId = questionId;
    }

    public String getTagtxt() {
        return tagtxt;
    }

    public void setTagtxt(String tagtxt) {
        this.tagtxt = tagtxt;
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

    public Long getLikes() {
        return likes;
    }

    public void setLikes(Long likes) {
        this.likes = likes;
    }

    public Long getDislikes() {
        return dislikes;
    }

    public void setDislikes(Long dislikes) {
        this.dislikes = dislikes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagModel tagModel = (TagModel) o;
        return Objects.equals(tagId, tagModel.tagId) &&
                Objects.equals(questionId, tagModel.questionId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(tagId, questionId);
    }

    @Override
    public int compareTo(@NonNull TagModel tagModel) {
        int result;
        if(tagModel.getTagId().equals("6666") ||  tagId.equals("6666")){
            return 0;
        }
        likes = likes == null ? 0 : likes;
        dislikes = dislikes == null ? 0 : dislikes;
        if (tagModel.getLikes() == null) {
            tagModel.setLikes(0L);
        }
        if (tagModel.getDislikes() == null) {
            tagModel.setDislikes(0L);
        }
        Long diff = likes -dislikes;
        Long diff2 = tagModel.getLikes() - tagModel.getDislikes();
        result = diff2.compareTo(diff);
        if(result == 0){
            return  tagtxt.compareTo(tagModel.getTagtxt());
        }
        return result;
    }
}



































































