package app.jayang.icebr8k.Utility;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.ArrayList;

public class MyXAxisValueFormatter implements IAxisValueFormatter {
    private ArrayList<String> mValues;

    public MyXAxisValueFormatter(ArrayList<String>values) {
        mValues = values;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {

        return mValues.get((int) value);
    }
}