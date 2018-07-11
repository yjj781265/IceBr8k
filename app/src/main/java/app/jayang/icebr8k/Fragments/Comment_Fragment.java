package app.jayang.icebr8k.Fragments;


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import app.jayang.icebr8k.Adapter.CommentAdapter;
import app.jayang.icebr8k.Modle.Comment;
import app.jayang.icebr8k.Modle.MyEditText;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.Utility.OnLoadMoreListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Comment_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Comment_Fragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String QUESTION_ID = "param1";


    // TODO: Rename and change types of parameters
    private String questionId;
    private MyEditText editText;
    private ImageView send;
    private View mView;
    private RecyclerView mRecyclerView;
    private CommentAdapter mAdapter;
    private ArrayList<Comment> comments;
    private ProgressBar mProgressBar;
    private RecyclerView.LayoutManager mLayoutManager;
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private Comment loadingComment = new Comment("loading",null,null,"load",null,null);



    public Comment_Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     *
     * @return A new instance of fragment Comment_Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Comment_Fragment newInstance(String param1) {
        Comment_Fragment fragment = new Comment_Fragment();
        Bundle args = new Bundle();
        args.putString(QUESTION_ID, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            questionId = getArguments().getString(QUESTION_ID);
            Toast.makeText(getActivity(), questionId, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         mView =inflater.inflate(R.layout.fragment_comment, container, false);

        mRecyclerView = mView.findViewById(R.id.comment_list);
        mProgressBar = mView.findViewById(R.id.comment_progressBar);
        editText = (MyEditText) mView.findViewById(R.id.comment_input);
        send = mView.findViewById(R.id.comment_send);
        comments = new ArrayList<>();

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter
        mAdapter = new CommentAdapter(comments,mRecyclerView ,getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setLoaded();



        mAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (!comments.contains(loadingComment)) {
                   // comments.add(loadingComment);
                 //   mAdapter.notifyItemInserted(comments.size() - 1);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {


                        }
                    }, 2000);


                }
            }
        });


        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // if filed is empty send button is disabled
                if (charSequence.toString().trim().isEmpty()) {
                    send.setEnabled(false);
                } else {
                    send.setEnabled(true);

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {


            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkInternet()){
                    sendComment();
                }else{
                    Snackbar snackbar = Snackbar
                            .make(mRecyclerView, "No Internet Connection", Snackbar.LENGTH_LONG)
                            .setAction("Setting", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    startActivity(new Intent(Settings.ACTION_SETTINGS));
                                }
                            });

                    snackbar.show();
                }


            }
        });
        return mView;
    }

    private void sendComment() {
        Comment comment = new Comment(UUID.randomUUID().toString(),currentUser.getUid(), editText.getText().toString(),"text",new Date().getTime(),null);
        comments.add(0,comment);
        mAdapter.notifyItemInserted(0);


    }


    public boolean checkInternet() {
        ConnectivityManager cm = (ConnectivityManager) getActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            return true;
        } else {
            return false;


        }
    }


    void addData(){
        mProgressBar.setVisibility(View.VISIBLE);
        mAdapter.setLoaded();
        if (comments.contains(loadingComment)) {
            int index = comments.indexOf(loadingComment);
            comments.remove(index);
            mAdapter.notifyItemRemoved(index);

        }

        for(int i=0;i<10;i++){
            Comment comment = new Comment(UUID.randomUUID().toString(),"123","hello there","text",new Date().getTime()-1000000,i);
            comments.add(comment);
        }

        mProgressBar.setVisibility(View.GONE);
        mAdapter.notifyDataSetChanged();

    }




}
