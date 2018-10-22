package app.jayang.icebr8k.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import app.jayang.icebr8k.Adapter.SurveyQuestionAdapter;
import app.jayang.icebr8k.Model.SurveyQ;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.Utility.DimmedPromptBackground;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;


/**
 * Created by LoLJay on 10/22/2017.
 */

public class SurveyTab_Fragment extends Fragment implements SurveyQuestionAdapter.SubmitedListener,MaterialTapTargetPrompt.PromptStateChangeListener,View.OnClickListener{
    View mview;
    private RecyclerView mRecyclerView;
    private RelativeLayout noMoreQLayout;
    private ImageView congratsGif;
    private RelativeLayout loadingGif;
    private SurveyQuestionAdapter mAdapter;
    private LinearLayout mLinearLayout;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<SurveyQ> mList = new ArrayList<>();
    private ArrayList<String> answeredList = new ArrayList<>();
    private FirebaseUser currentUser = FirebaseAuth.getInstance()
            .getCurrentUser();
    final PagerSnapHelper snapHelper = new PagerSnapHelper();
    private final SurveyQ feedbackCard = new SurveyQ();
    private FloatingActionButton fab;
    private  SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private final Handler mHandler = new Handler();
    private SurveyQ currentSurveyQ;
    private HashMap<SurveyQ,String> indicatorMap =  new HashMap();
    private LinearLayout surveyNav;
    private Integer annsweredCounter = 0;


    private ImageView back,forward;
    private  int counter =0, currentPosition =0;
    private  YoYo.YoYoString fabAnimation;


    public SurveyTab_Fragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable  ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {

      mview = inflater.inflate(R.layout.survey_tab,container,false);
      surveyNav = mview.findViewById(R.id.survey_nav);
        mRecyclerView = mview.findViewById(R.id.survey_recyclerView);
        back =  mview.findViewById(R.id.back);
        forward = mview.findViewById(R.id.forward);
        fab =  mview.findViewById(R.id.survey_fab);
        mLinearLayout = mview.findViewById(R.id.dot);
        loadingGif = mview.findViewById(R.id.survey_loading);
        loadingGif.setVisibility(View.VISIBLE);
        noMoreQLayout = mview.findViewById(R.id.noMoreQGif);
        noMoreQLayout.setVisibility(View.GONE);
        congratsGif = mview.findViewById(R.id.congratsGif);
        Glide.with(this).load(R.drawable.congrats_gif).into(congratsGif);

        feedbackCard.setQuestionId(UUID.randomUUID().toString());
        feedbackCard.setType("fb");
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        // specify an adapter (see also next example)
        mAdapter = new SurveyQuestionAdapter(mList, getActivity(),this,getActivity());
        mRecyclerView.setAdapter(mAdapter);
        // add pager behavior

        snapHelper.attachToRecyclerView(mRecyclerView);


        // init sharePref
        sharedPref = getActivity().getSharedPreferences("tutorial", Context.MODE_PRIVATE);
         editor = sharedPref.edit();



        getSurveyQ();
        fabflag();

        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 if(++currentPosition >mList.size()-1){
                     currentPosition =0;
                 }
                if(currentPosition ==0){
                    mRecyclerView.scrollToPosition(currentPosition);
                }else{
                    mRecyclerView.smoothScrollToPosition(currentPosition);
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(--currentPosition <0){
                    currentPosition =mList.size()-1;
                }
                if(currentPosition ==mList.size()-1){
                    mRecyclerView.scrollToPosition(currentPosition);
                }else{
                    mRecyclerView.smoothScrollToPosition(currentPosition);

                }

            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingGif.setVisibility(View.VISIBLE);
                noMoreQLayout.setVisibility(View.GONE);

                hideView();
               new Handler().postDelayed(new Runnable() {
                   @Override
                   public void run() {
                      getSurveyQ();
                   }
               },666);

            }
        });

        // change all mp question with more than 4 answers  to sp

        final DatabaseReference ref= FirebaseDatabase.getInstance().getReference()
                .child("Questions_8");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot childSnap : dataSnapshot.getChildren()){
                    SurveyQ surveyQ = childSnap.getValue(SurveyQ.class);
                    if(surveyQ.getType().equals("mc") && surveyQ.getAnswer().size()>4){
                        surveyQ.setType("sp");
                        ref.child(surveyQ.getQuestionId()).setValue(surveyQ);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return  mview;
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(getView()!=null){
           boolean show = sharedPref.getBoolean("fabTutorial",false);
            if(isVisibleToUser){
                if(show){
                   mHandler.postDelayed(new Runnable() {
                       @Override
                       public void run() {
                           fab.show();
                       }
                   },300);
                }
            }else{
                fab.hide();
            }


        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    void hideView(){
        if(fabAnimation!=null){
            fabAnimation.stop();
        }
        fab.hide();
        currentPosition =0;
        counter =0;
        mRecyclerView.scrollToPosition(currentPosition);
        surveyNav.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);

    }
    // if has more than 8 questions answered show fab all the time, show tutorial after first 8 questions answered
    void fabflag(){
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("UserQA")
                .child(currentUser.getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Long count = dataSnapshot.getChildrenCount();
                // true user has already done the tutorial for fab
                boolean flag = sharedPref.getBoolean("fabTutorial",false);

                if(count>=8 ){
                    fab.show();
                    if(!flag) {
                        // show tutorial for the first time
                        new MaterialTapTargetPrompt.Builder(getActivity())
                                .setTarget(fab)
                                .setPromptStateChangeListener(SurveyTab_Fragment.this)
                                .setPromptBackground(new DimmedPromptBackground())
                                .setBackgroundColour(ContextCompat.getColor(getActivity(), R.color.colorPrimary))
                                .setSecondaryText("When you finish answering this set of 8 questions, click here to load 8 new questions")
                                .show();
                        editor.putBoolean("fabTutorial", true);
                        editor.apply();
                        ref.removeEventListener(this);
                    }

                }else{
                    fab.hide();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    void getSurveyQ(){
        mRecyclerView.setVisibility(View.GONE);
        answeredList.clear();;
        annsweredCounter =0;
        mList.clear();
       // get all the questions user answered
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("UserQA").child(currentUser.getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot child : dataSnapshot.getChildren()){
                    if(!answeredList.contains(child.getKey())){
                        answeredList.add(child.getKey());
                    }
                }
                // get first 8 questions
                DatabaseReference questionRef = FirebaseDatabase.getInstance().getReference()
                        .child("Questions_8");
                questionRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        ArrayList<SurveyQ > temptList = new ArrayList<>();

                        for(DataSnapshot childSnap : dataSnapshot.getChildren()) {
                            SurveyQ surveyQ = childSnap.getValue(SurveyQ.class);
                            temptList.add(surveyQ);

                        }

                        // randomize the questions
                        Collections.shuffle(temptList);
                        for(SurveyQ surveyQ : temptList){
                            if(!answeredList.contains(surveyQ.getQuestionId())){
                                mList.add(surveyQ);
                                indicatorMap.put(surveyQ,"default");

                            }
                            if(mList.size()>7){
                                break;
                            }
                        }

                        if(mList.isEmpty()){
                            noMoreQLayout.setVisibility(View.VISIBLE);
                            loadingGif.setVisibility(View.GONE);
                            hideView();
                            fab.show();
                        }else{
                            Log.d("SurvetFrag",mList.toString());
                            mLinearLayout.removeAllViews();
                            currentSurveyQ = mList.get(0);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            params.gravity = Gravity.CENTER_VERTICAL;
                            mLinearLayout.setLayoutParams(params);


                            // add indicators
                            for (int i = 0; i < mList.size(); i++) {
                                ImageView imageView = new ImageView(getContext());
                                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                lp.setMargins(8, 0, 8, 0);
                                imageView.setLayoutParams(lp);
                                mLinearLayout.addView(imageView, i);
                                imageView.setTag(i);
                                imageView.setOnClickListener(SurveyTab_Fragment.this);
                                imageView.requestLayout();
                                imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.indicator_default));
                                imageView.getLayoutParams().height = (int) getResources().getDimension(R.dimen.imageview_height);
                                imageView.getLayoutParams().width = (int) getResources().getDimension(R.dimen.imageview_width);

                            }
                            btnAction(currentSurveyQ);

                            mAdapter.notifyDataSetChanged();
                            loadingGif.setVisibility(View.GONE);
                            mRecyclerView.setVisibility(View.VISIBLE);
                            surveyNav.setVisibility(View.VISIBLE);
                            fab.show();
                          /*  mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                                @Override
                                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                                    super.onScrollStateChanged(recyclerView, newState);
                                    if(newState == RecyclerView.SCROLL_STATE_IDLE){
                                        View centerView = snapHelper.findSnapView(mLayoutManager);
                                        currentPosition = mLayoutManager.getPosition(centerView);
                                        currentSurveyQ = mList.get(currentPosition);
                                        btnAction(currentSurveyQ);
                                    }

                                }
                            });*/

                          if(mRecyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                                final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView
                                        .getLayoutManager();

                                mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                                    @Override
                                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                                        super.onScrolled(recyclerView, dx, dy);
                                        Log.d("Survey123", "first visable"+
                                                String.valueOf(linearLayoutManager.findFirstVisibleItemPosition())
                                                +"last Visiable"+ linearLayoutManager.findLastVisibleItemPosition());
                                        currentPosition = (linearLayoutManager.findFirstVisibleItemPosition()
                                                + linearLayoutManager.findLastVisibleItemPosition())/2;
                                        currentSurveyQ = mList.get(currentPosition);
                                        btnAction(currentSurveyQ);
                                    }
                                });

                            }

                        }


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
    private void btnAction(SurveyQ surveyQ) {
        for(int i = 0; i < mList.size(); i++){
            ImageView imageView2 = (ImageView) mLinearLayout.getChildAt(i);
            String answertemp;
            answertemp = indicatorMap.get(mList.get(i));
            int id ;
            if(answertemp!=null && answertemp.equals("skipped")){
                id = R.drawable.indicator_skip;
            }else if(answertemp==null || answertemp.equals("default")){
                id = R.drawable.indicator_default;
            }else{
                id = R.drawable.indicator_answered;
            }

            if(mList.get(i).equals(surveyQ) ){
               if(id == R.drawable.indicator_default){
                   id = R.drawable.indicator_selected;
               }
                imageView2.getLayoutParams().height = (int) getResources().getDimension(R.dimen.imageview_height_selected);
                imageView2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.imageview_width_selected);

            }else{
                imageView2.getLayoutParams().height = (int) getResources().getDimension(R.dimen.imageview_height);
                imageView2.getLayoutParams().width = (int) getResources().getDimension(R.dimen.imageview_width);
            }
            imageView2.requestLayout();
            imageView2.setImageDrawable(ContextCompat.getDrawable(getActivity(), id));

        }
    }




    @Override
    public void onClick(SurveyQ surveyQ, String answer) {
        // answered questions counter
        indicatorMap.put(surveyQ,answer);
        btnAction(surveyQ);
        annsweredCounter ++;
        if(annsweredCounter == mList.size()){
            fabAnimation = YoYo.with(Techniques.Pulse).repeat(8).playOn(fab);
        }else{
            if(fabAnimation!=null){
                fabAnimation.stop();
            }

        }

    }


    @Override
    public void onPromptStateChanged(@NonNull MaterialTapTargetPrompt prompt, int state) {
        if (state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED)
        {
            // User has pressed the prompt target
            prompt.dismiss();
        }
    }

// when user click on the indicator
    @Override
    public void onClick(View v) {
      mRecyclerView.scrollToPosition((int)v.getTag());
      btnAction(mList.get((int)v.getTag()));

    }
}
