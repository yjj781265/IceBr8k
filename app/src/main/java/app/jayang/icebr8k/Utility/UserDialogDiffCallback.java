package app.jayang.icebr8k.Utility;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import java.util.ArrayList;

import app.jayang.icebr8k.Modle.UserDialog;

public class UserDialogDiffCallback extends DiffUtil.Callback {
    private final ArrayList<UserDialog> oldList;
    private final ArrayList<UserDialog> newList;

    public UserDialogDiffCallback(ArrayList<UserDialog> oldList, ArrayList<UserDialog> newList) {
        this.oldList = oldList;
        this.newList = newList;
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
        return oldList.get(oldItemPosition).
                equals(newList.get(newItemPosition));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
         return oldList.get(oldItemPosition).getScore().
                equals(newList.get(newItemPosition).getScore())
                 ||oldList.get(oldItemPosition).getOnlinestats().
                 equals(newList.get(newItemPosition).getOnlinestats())
                 ||oldList.get(oldItemPosition).getPhotoUrl().
                 equals(newList.get(newItemPosition).getPhotoUrl())
                         ||oldList.get(oldItemPosition).getName().
                         equals(newList.get(newItemPosition).getName())
                                 ||oldList.get(oldItemPosition).getLastseen().
                                 equals(newList.get(newItemPosition).getLastseen());
    }


}
