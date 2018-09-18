package app.jayang.icebr8k.Fragments;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import app.jayang.icebr8k.Adapter.CommentAdapter;
import app.jayang.icebr8k.Model.Comment;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.Utility.OnLoadMoreListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Comment_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Comment_Fragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String QUESTION_ID = "param1";




    private String questionId;
    private TextView noComments;
    private View mView;
    private RecyclerView mRecyclerView;
    private Comparator<Comment> mComparator;
    private CommentAdapter mAdapter;
    private HashMap<DatabaseReference,ValueEventListener> repliesMap = new HashMap<>();
    private ArrayList<Comment> comments;
    private ProgressBar mProgressBar;
    private RecyclerView.LayoutManager mLayoutManager;
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private final Comment loadingComment = new Comment("loading",null,null,"load",null,null,null);
    private final Comment inputComment = new Comment("input",null,null,"input",null,null,null);
    private DatabaseReference commentRef;
    private ChildEventListener commentRefChildListener;


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

    public static Comment_Fragment newInstance(String param1) {
        Comment_Fragment fragment = new Comment_Fragment();
        Bundle args = new Bundle();
        args.putString(QUESTION_ID, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            questionId = getArguments().getString(QUESTION_ID);

        }

        mComparator =new Comparator<Comment>() {
            @Override
            public int compare(Comment o1, Comment o2) {
                if(o1!=null && o1.getTimestamp()!=null &&o2!=null && o2.getTimestamp()!=null){
                    return o2.getTimestamp().compareTo(o1.getTimestamp());
                }
                return 0;
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         mView =inflater.inflate(R.layout.fragment_comment, container, false);

        mRecyclerView = mView.findViewById(R.id.comment_list);
        mProgressBar = mView.findViewById(R.id.comment_progressBar);
        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.VISIBLE);
        
        comments = new ArrayList<>();
        // add input text box as the first item
        comments.add(inputComment);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter
        mAdapter = new CommentAdapter(comments,questionId, mRecyclerView ,getActivity(),getFragmentManager());
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

        getComments();

        return mView;
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


    void getComments(){
        mProgressBar.setVisibility(View.VISIBLE);
         commentRef = FirebaseDatabase.getInstance().getReference()
                .child("Comments")
                .child(questionId);
         commentRefChildListener = new ChildEventListener() {
             @Override
             public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                 final Comment comment = dataSnapshot.getValue(Comment.class);

                 DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                         .child("Comments")
                         .child(questionId).
                                 child(comment.getId())
                         .child("replies");
                 ValueEventListener repliesListener = new ValueEventListener() {
                     @Override
                     public void onDataChange(DataSnapshot dataSnapshot) {
                         long count = dataSnapshot.getChildrenCount();
                         comment.setReply(count);
                         if(comments.contains(comment)){
                             comments.set(comments.indexOf(comment),comment);
                             mAdapter.notifyItemChanged(comments.indexOf(comment));
                         }else{
                             comments.add(comment);
                             Collections.sort(comments,mComparator);
                             mAdapter.notifyDataSetChanged();
                         }
                     }

                     @Override
                     public void onCancelled(DatabaseError databaseError) {

                     }
                 };
                 ref.addValueEventListener(repliesListener);
                 repliesMap.put(ref,repliesListener);

             }

             @Override
             public void onChildChanged(DataSnapshot dataSnapshot, String s) {

             }

             @Override
             public void onChildRemoved(DataSnapshot dataSnapshot) {

             }

             @Override
             public void onChildMoved(DataSnapshot dataSnapshot, String s) {

             }

             @Override
             public void onCancelled(DatabaseError databaseError) {

             }
         };
        commentRef.addChildEventListener(commentRefChildListener);

        commentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mProgressBar.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    @Override
    public void onDestroy() {
        try{
            commentRef.removeEventListener(commentRefChildListener);
            for(DatabaseReference ref : repliesMap.keySet()){
                ref.removeEventListener(repliesMap.get(ref));
            }
        }catch (NullPointerException e){
            Log.d("CommentFrag123",e.getMessage());
        }


        super.onDestroy();
    }
}
