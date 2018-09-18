package app.jayang.icebr8k.Model;

import app.jayang.icebr8k.Utility.Compatability;

public class UserComp {
    private Compatability mCompatability;
    private String userUid;

    public UserComp(Compatability compatability, String userUid) {
        mCompatability = compatability;
        this.userUid = userUid;
    }

    public Compatability getCompatability() {
        return mCompatability;
    }

    public void setCompatability(Compatability compatability) {
        mCompatability = compatability;
    }

    public String getUserUid() {
        return userUid;
    }
}
