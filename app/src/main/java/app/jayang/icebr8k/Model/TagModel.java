package app.jayang.icebr8k.Model;

public class TagModel {
  private  String tagtxt;
    //Todo add likes and dislikes number , the author and the timestamp

    public TagModel(String tagtxt) {
        this.tagtxt = tagtxt;
    }

    public String getTagtxt() {
        return tagtxt;
    }

    public void setTagtxt(String tagtxt) {
        this.tagtxt = tagtxt;
    }
}
