package app.jayang.icebr8k.Utility;

import java.util.HashMap;
import java.util.Map;

import app.jayang.icebr8k.Model.TagModel;

public interface EmailUtilListener {
    void onCompleted(Object object);
    void onFailed();
}
