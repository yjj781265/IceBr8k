package app.jayang.icebr8k.Modle;

import android.support.v7.util.DiffUtil;

import java.util.ArrayList;

import app.jayang.icebr8k.Modle.UserDialog;

/**
 * Created by yjj781265 on 1/8/2018.
 */

public class UserDialogsChange extends DiffUtil.Callback {
    private ArrayList<UserDialog> mNewList;
    private ArrayList<UserDialog> mOldList;

    public UserDialogsChange(ArrayList<UserDialog> oldList, ArrayList<UserDialog>newList) {
        mNewList = newList;
        mOldList = oldList;
    }

    @Override
    public int getOldListSize() {
        return mOldList.size();
    }

    @Override
    public int getNewListSize() {
        return mNewList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldList.get(oldItemPosition).getId().equals(mNewList.get(newItemPosition).getId()) ;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        UserDialog newDialog = mNewList.get(newItemPosition);
        UserDialog oldDialog = mOldList.get(oldItemPosition);
        if (oldDialog.getOnlineStats().equals(newDialog.getOnlineStats())&&
                oldDialog.getScore().equals(newDialog.getScore())) {
            return true;
        }else{
            return false;
        }
    }
}
