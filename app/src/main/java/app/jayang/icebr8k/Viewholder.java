package app.jayang.icebr8k;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by LoLJay on 10/20/2017.
 */

public class Viewholder extends RecyclerView.ViewHolder {
    public ImageView image;
    public TextView displayname;
    public TextView username;
    public LinearLayout linearLayout;

    public Viewholder(View itemView) {
        super(itemView);
        image = itemView.findViewById(R.id.imageview_id);
        displayname =itemView.findViewById(R.id.displayname_textview);
        username=itemView.findViewById(R.id.username_textview);
        linearLayout =itemView.findViewById(R.id.recycleritem_id);
    }

}