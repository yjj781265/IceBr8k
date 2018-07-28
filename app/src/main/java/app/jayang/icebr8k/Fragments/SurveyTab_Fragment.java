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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import app.jayang.icebr8k.Adapter.SurveyQuestionAdapter;
import app.jayang.icebr8k.Modle.SurveyQ;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.Utility.DimmedPromptBackground;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;


/**
 * Created by LoLJay on 10/22/2017.
 */

public class SurveyTab_Fragment extends Fragment implements SurveyQuestionAdapter.SubmitedListener,MaterialTapTargetPrompt.PromptStateChangeListener{
    View mview;
    private RecyclerView mRecyclerView;
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
   ;

    private ImageView back,forward;
    private  int counter =0, currentPosition =0;
    private TextView mTextView;





    public SurveyTab_Fragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable  ViewGroup container, @Nullable final Bundle savedInstanceState) {

      mview = inflater.inflate(R.layout.survey_tab,container,false);
        mRecyclerView = mview.findViewById(R.id.survey_recyclerView);
        back =  mview.findViewById(R.id.backward);
        forward = mview.findViewById(R.id.forward);
        mTextView = mview.findViewById(R.id.survey_text);
        mTextView.setText("");
        fab =  mview.findViewById(R.id.survey_fab);
        mLinearLayout = mview.findViewById(R.id.dot);
        loadingGif = mview.findViewById(R.id.survey_loading);
        loadingGif.setVisibility(View.VISIBLE);

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
                btnAction(currentPosition,mList.size());

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
                btnAction(currentPosition,mList.size());

            }
        });




        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loadingGif.setVisibility(View.VISIBLE);
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
        fab.hide();
        currentPosition =0;
        counter =0;
        mRecyclerView.scrollToPosition(currentPosition);
        mLinearLayout.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
        mTextView.setText("");
        back.setVisibility(View.GONE);
        forward.setVisibility(View.GONE);
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
                            }
                            if(mList.size()>7){
                                break;
                            }
                        }


                        Log.d("SurvetFrag",mList.toString());
                        mLinearLayout.removeAllViews();
                        for (int i = 0; i < mList.size(); i++) {
                            ImageView imageView = new ImageView(getContext());
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            lp.setMargins(2, 0, 2, 0);
                            imageView.setLayoutParams(lp);
                            mLinearLayout.addView(imageView, i);
                            imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.indicator_default));
                        }
                        mLinearLayout.setVisibility(View.VISIBLE);
                       btnAction(currentPosition,mList.size());

                        mAdapter.notifyDataSetChanged();
                        mTextView.setText("Answered 0/"+(mList.size()) );
                        loadingGif.setVisibility(View.GONE);
                        mRecyclerView.setVisibility(View.VISIBLE);
                        back.setVisibility(View.VISIBLE);
                        forward.setVisibility(View.VISIBLE);
                        fab.show();



                        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                            @Override
                            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                                super.onScrollStateChanged(recyclerView, newState);
                                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                                    View centerView = snapHelper.findSnapView(mLayoutManager);
                                    currentPosition = mLayoutManager.getPosition(centerView);
                                    btnAction(currentPosition,mList.size());

                                }

                            }
                        });


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
    private void btnAction(int position, int bannerListSize) {
        for (int i = 0; i < bannerListSize; i++) {
            ImageView imageView2 = (ImageView) mLinearLayout.getChildAt(i);
            if (i == position) {
                imageView2.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.indicator_selected));
            } else {
                imageView2.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.indicator_default));
            }
        }
    }

    @Override
    public void onClick() {
        // answered questions counter
        counter++;
        mTextView.setText("Answered " + counter +"/"+ (mList.size()));
    }


    @Override
    public void onPromptStateChanged(@NonNull MaterialTapTargetPrompt prompt, int state) {
        if (state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED)
        {
            // User has pressed the prompt target
            prompt.dismiss();
        }
    }
}
