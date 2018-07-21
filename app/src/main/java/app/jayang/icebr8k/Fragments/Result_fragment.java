package app.jayang.icebr8k.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import app.jayang.icebr8k.Modle.SurveyQ;
import app.jayang.icebr8k.QuestionActivity;
import app.jayang.icebr8k.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the

 * to handle interaction events.
 * Use the {@link Result_fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Result_fragment extends Fragment {

    private static final String QUESTIONID = "questionId";
    private HashMap<String,Integer> answersMap= new HashMap<>();



    private String questionId,question;
    private ProgressBar mProgressBar;
    private resultFragDisplayedListener mListener;
    private PieChart mPieChart;
    private QuestionActivity mQuestionActivity;
    private View mView;
    private boolean firstTime = true;

    public Result_fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     *
     */
    // TODO: Rename and change types and number of parameters
    public static Result_fragment newInstance(String param1) {
        Result_fragment fragment = new Result_fragment();
        Bundle args = new Bundle();
        args.putString(QUESTIONID, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mQuestionActivity = (QuestionActivity) getActivity();

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            questionId = getArguments().getString("questionId");



        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_result_fragment, container, false);
        mPieChart = mView.findViewById(R.id.chart);
        mProgressBar =mView.findViewById(R.id.result_progressBar);
        mPieChart.setUsePercentValues(true);
        mPieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        mPieChart.setUsePercentValues(true);
        mPieChart.getDescription().setEnabled(false);
        mPieChart.setExtraOffsets(5, 10, 5, 5);

        mPieChart.setDragDecelerationFrictionCoef(0.95f);



        mPieChart.setDrawHoleEnabled(true);
        mPieChart.setHoleColor(Color.WHITE);

        mPieChart.setTransparentCircleColor(Color.WHITE);
        mPieChart.setTransparentCircleAlpha(110);

        mPieChart.setHoleRadius(58f);
        mPieChart.setTransparentCircleRadius(51f);

        mPieChart.setDrawCenterText(true);

        mPieChart.setRotationAngle(0);

        mPieChart.setRotationEnabled(false);
        mPieChart.setHighlightPerTapEnabled(false);

        Legend l = mPieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setWordWrapEnabled(true);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(1f);
        l.setYOffset(8f);

        // entry label styling
        mPieChart.setEntryLabelColor(ContextCompat.getColor(getActivity(),R.color.dark_gray));
        mPieChart.setEntryLabelTextSize(12f);



        return mView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser && getView()!=null && firstTime){
            if(mQuestionActivity!=null){
                mQuestionActivity.getAppBarLayout().setExpanded(false,true);
            }
            mProgressBar.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getDataFromDatabase();
                }
            },666);

            firstTime =false;



        }else if (isVisibleToUser && !firstTime){
            if(mQuestionActivity!=null){
                mQuestionActivity.getAppBarLayout().setExpanded(false,true);
            }

        }else{
            if(mQuestionActivity!=null){
                mQuestionActivity.getAppBarLayout().setExpanded(true,true);
            }
        }

    }

    void getDataFromDatabase(){


        // get the question for this  questionId
        DatabaseReference titleRef = FirebaseDatabase .getInstance().getReference()
                .child("Questions_8")
                .child(questionId);
        titleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                question = dataSnapshot.child("question").getValue(String.class);



                // search user's answer
                DatabaseReference answersRef = FirebaseDatabase .getInstance().getReference()
                        .child("UserQA");

                answersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                     for(DataSnapshot childSnap : dataSnapshot.getChildren()){
                         if(childSnap.hasChild(questionId)){

                            String answer = childSnap.
                                    child(questionId).
                                    child("answer").getValue(String.class);

                            // set up answer map default value is 0 else increment by 1

                            if(answersMap.containsKey(answer)){
                                int count = answersMap.get(answer);
                                answersMap.put(answer, ++count );
                            }else{
                                answersMap.put(answer, 1 );
                            }
                         }
                     }
                        Log.d("Result_Frag",answersMap.toString());
                     setPieChart(answersMap);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    void setPieChart(HashMap<String,Integer> map){


        Integer total =0;
        for(String str : map.keySet()){
            total += map.get(str);

        }
        String user = map.size()>1 ?" users" :" user";
        String numText = "\n(" + total + user+" have answered this question)";

        CharSequence string  = Html.fromHtml("<font color=\"#3c3e42\">" + numText+ "</font>");
        String centerText = question +"\n"+ string;
        mPieChart.setCenterText(centerText);


        List<PieEntry> entries = new ArrayList<>();
        if(total>0){
            PieEntry pieEntry;
            Integer count ;
            Float result;
            for(String str: map.keySet()){
                count = map.get(str);
                Log.d("Result_Frag"," count is "+ count);
                result = ((float)count)/(float)total;
                Log.d("Result_Frag"," result is "+ result);

                pieEntry = new PieEntry(result,str);
                entries.add(pieEntry);

            }
        }


/*
        entries.add(new PieEntry(18.5f, "Hey"));
        entries.add(new PieEntry(26.7f, "123"));
        entries.add(new PieEntry(24.0f, "21313"));
        entries.add(new PieEntry(30.8f, "12313"));*/
        Log.d("Result_Frag",""+entries.size());

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<Integer>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);
        colors.add(ColorTemplate.getHoloBlue());


        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setDrawIcons(false);

        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);



        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);
        data.setValueTextSize(11f);
        mProgressBar.setVisibility(View.GONE);

        mPieChart.setData(data);
        mPieChart.setVisibility(View.VISIBLE);
        mPieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        mPieChart.invalidate(); // refresh

    }


    interface  resultFragDisplayedListener {

        void onDisplay();
    }





}
