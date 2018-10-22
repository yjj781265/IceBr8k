package app.jayang.icebr8k.Model;

import java.util.Objects;
import java.util.Set;

public class TagModel {
    private String tagtxt, tagId, authorId, questionId;
    private Long timestamp;
    private Long likes, dislikes;
    private Set<String> liked, disliked;
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

    public String getTagtxt() {
        return tagtxt;
    }

    public void setTagtxt(String tagtxt) {
        this.tagtxt = tagtxt;
    }

    public Set<String> getLiked() {
        return liked;
    }

    public void setLiked(Set<String> liked) {
        this.liked = liked;
    }

    public Set<String> getDisliked() {
        return disliked;
    }

    public void setDisliked(Set<String> disliked) {
        this.disliked = disliked;
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
}



































































