package app.jayang.icebr8k.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import app.jayang.icebr8k.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TagFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TagFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private String questionId;
    private TextView tag;
    private View view;



    public TagFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment TagFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TagFragment newInstance(String param1) {
        TagFragment fragment = new TagFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            questionId = getArguments().getString(ARG_PARAM1);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
       view =inflater.inflate(R.layout.fragment_tag, container, false);
       tag = view.findViewById(R.id.text_view);
       tag.setOnTouchListener(new View.OnTouchListener() {
           @Override
           public boolean onTouch(View view, MotionEvent motionEvent) {
               if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                   tag.setSelected(true);
               }else{
                   tag.setSelected(false);
               }

               return false;
           }
       });
        return view;
    }

}
