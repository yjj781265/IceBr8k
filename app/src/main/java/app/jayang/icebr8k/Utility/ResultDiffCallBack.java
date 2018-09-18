package app.jayang.icebr8k.Utility;

import android.support.v7.util.DiffUtil;

import java.util.ArrayList;

import app.jayang.icebr8k.Model.ResultItem;

public class ResultDiffCallBack extends DiffUtil.Callback {

    ArrayList<ResultItem> newList;
    ArrayList<ResultItem> oldList;

    public ResultDiffCallBack(ArrayList<ResultItem> newList, ArrayList<ResultItem> oldList) {
        this.newList = newList;
        this.oldList = oldList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getQuestionId().equals(newList.get(newItemPosition).getQuestionId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getAnswer1().equals(newList.get(newItemPosition).getAnswer1())
                && oldList.get(oldItemPosition).getAnswer2().equals(newList.get(newItemPosition).getAnswer2())
                && oldList.get(oldItemPosition).getQuesiton().equals(newList.get(newItemPosition).getQuesiton());
    }
}
