package app.jayang.icebr8k.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import app.jayang.icebr8k.QuestionActivity;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.Utility.MyToolBox;
import app.jayang.icebr8k.Utility.MyXAxisValueFormatter;

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
    private TextView centerText;
    private  DatabaseReference titleRef;
    private BarChart mChart;
    private QuestionActivity mQuestionActivity;
    private View mView;
    private boolean firstTime = true;
    private ValueEventListener piechatListener;

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
             titleRef = FirebaseDatabase .getInstance().getReference()
                    .child("Questions_8")
                    .child(questionId);


        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_result_fragment, container, false);
        mChart = mView.findViewById(R.id.chart);
        mProgressBar =mView.findViewById(R.id.result_progressBar);
        centerText = mView.findViewById(R.id.centerText);







        return mView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && getView() != null && firstTime) {

            mProgressBar.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getDataFromDatabase();
                }
            }, 666);
            firstTime = false;


        }
    }
    void getDataFromDatabase(){


        // get the question for this  questionId
        piechatListener = new ValueEventListener() {
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

                                // is sc question
                                if(MyToolBox.isInteger(answer)){
                                    setSCMap(answer);
                                }else{
                                    // set up answer map default value is 0 else increment by 1

                                    if(answersMap.containsKey(answer)){
                                        int count = answersMap.get(answer);
                                        answersMap.put(answer, ++count );
                                    }else{
                                        answersMap.put(answer, 1 );
                                    }
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
        };
        titleRef.addListenerForSingleValueEvent(piechatListener);
    }

    // if is SC question we will group them
    public void setSCMap(String answer){
        if(Integer.valueOf(answer)>=1 &&Integer.valueOf(answer)<=2){
            String key = "(1-2)";
            if(answersMap.containsKey(answer)){
                int count = answersMap.get(answer);

                answersMap.put(key, ++count );
            }else{
                answersMap.put(key, 1 );
            }
        }else if(Integer.valueOf(answer)>=3 &&Integer.valueOf(answer)<=5){
            String key = "(3-5)";
            if(answersMap.containsKey(answer)){
                int count = answersMap.get(answer);

                answersMap.put(key, ++count );
            }else{
                answersMap.put(key, 1 );
            }
        }else if(Integer.valueOf(answer)>=6 &&Integer.valueOf(answer)<=8){
            String key = "(6-8)";
            if(answersMap.containsKey(answer)){
                int count = answersMap.get(answer);

                answersMap.put(key, ++count );
            }else{
                answersMap.put(key, 1 );
            }
        }else if(Integer.valueOf(answer)>=9 &&Integer.valueOf(answer)<=10){
            String key = "(9-10)";
            if(answersMap.containsKey(answer)){
                int count = answersMap.get(answer);

                answersMap.put(key, ++count );
            }else{
                answersMap.put(key, 1 );
            }
        }
    }


    void setPieChart(HashMap<String,Integer> map){


        Integer total =0;
        for(String str : map.keySet()){
            total += map.get(str);

        }
        String user = map.size()>1 ?" users" :" user";
        String numText = "\n(" + total + user+" have answered this question)";

        CharSequence string  = Html.fromHtml("<font color=\"#3c3e42\">" + numText+ "</font>");
        String text = question +"\n"+ string;
        centerText.setText(text);




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




        List<BarEntry> entries = new ArrayList<>();
        ArrayList<String> xAxisStrings = new ArrayList<>();

        if(total>0){
            BarEntry barEntry;
            Integer count,index =0 ;
            Float result;
            ArrayList<String> answerList=new ArrayList(map.keySet());
            Collections.sort(answerList);

            for(String str: answerList){
                xAxisStrings.add(str);
                count = map.get(str);
                Log.d("Result_Frag"," count is "+ count);
                result = (((float)count)/(float)total)*100;
                Log.d("Result_Frag"," result is "+ result);

                barEntry = new BarEntry((float)index,result);
                index++;
                entries.add(barEntry);


            }
        }
        /*entries.add(new BarEntry(0f, 30f));
        entries.add(new BarEntry(1f, 80f));
        entries.add(new BarEntry(2f, 60f));*/
        XAxis xAxis = mChart.getXAxis();
        xAxis.setLabelRotationAngle(-60f);

        xAxis.setValueFormatter(new MyXAxisValueFormatter(xAxisStrings));
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);

        YAxis yAxis = mChart.getAxisLeft();

        yAxis.setValueFormatter(new PercentFormatter());
        yAxis.setDrawGridLines(false);
        BarDataSet set = new BarDataSet(entries, "BarDataSet");
        set.setColors(colors);
        set.setValueFormatter(new PercentFormatter());
        set.setValueTextSize(8f);
        BarData data = new BarData(set);
        data.setBarWidth(0.9f); // set custom bar width
        mChart.setData(data);
        mChart.setScaleEnabled(false);
        mChart.setDrawGridBackground(false);
        mChart.setDrawValueAboveBar(true);
        mChart.setFitBars(true); // make the x-axis fit exactly all bars
        mChart.getAxisRight().setEnabled(false);
       mChart.getDescription().setEnabled(false);
       mChart.getLegend().setEnabled(false);
       mChart.setTouchEnabled(false);
       mChart.setHighlightPerTapEnabled(false);
       mChart.animateXY(3000, 3000);
       mProgressBar.setVisibility(View.GONE);
       mChart.setVisibility(View.VISIBLE);

        mChart.invalidate(); // refresh


    }

    @Override
    public void onDestroy() {
        try{
           titleRef.removeEventListener(piechatListener);
        }catch (NullPointerException e){
            Log.d("Result_Fragment",e.getMessage());
        }
        super.onDestroy();
    }
}
