package app.jayang.icebr8k;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.Date;
import java.util.UUID;

import app.jayang.icebr8k.Adapter.ReplyAdapter;
import app.jayang.icebr8k.Modle.Comment;
import app.jayang.icebr8k.Modle.MyEditText;
import app.jayang.icebr8k.Modle.User;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.Utility.SendNotification;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class Reply extends SwipeBackActivity implements ReplyAdapter.replyClickedListener {
    private RecyclerView mRecyclerView;
    private ReplyAdapter mAdapter;
    private ProgressBar inputProgressBar;
    private Toolbar mToolbar;
    private MyEditText mEditText;
    private ArrayList<Comment> mComments;
    private String topCommentId;
    private ImageView send, avatar;
    private String commentAuthorId,commentId,firstCommentAuthorId;
    private String questionId, title;
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference topCommentRef;


    private final String VIEWTYPE_TEXT_STR = "text";
    private final String VIEWTYPE_LOAD_STR = "load";
    private final String VIEWTYPE_TOP_STR = "top";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);

        mComments = new ArrayList<>();
        mEditText = (MyEditText) findViewById(R.id.reply_input);
        send = (ImageView) findViewById(R.id.reply_send);
        send.setVisibility(View.GONE);
        avatar = (ImageView) findViewById(R.id.reply_avatar);
        mToolbar = (Toolbar) findViewById(R.id.reply_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        inputProgressBar = (ProgressBar) findViewById(R.id.reply_input_progressBar);


        mRecyclerView = (RecyclerView) findViewById(R.id.reply_recyclerView);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(null);

        // specify an adapter
        mAdapter = new ReplyAdapter(mComments, this, mRecyclerView);
        mRecyclerView.setAdapter(mAdapter);


        // get top original comment
        if (getIntent().getExtras() != null) {

            /******** required extras for this activity  ******/

            topCommentId = getIntent().getExtras().getString("topCommentId");
            questionId = getIntent().getExtras().getString("questionId");





            if (topCommentId != null && questionId != null ) {

                DatabaseReference titleRef = FirebaseDatabase.getInstance()
                        .getReference().child("Questions_8")
                        .child(questionId)
                        .child("question");
                titleRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        title = dataSnapshot.getValue(String.class);
                        getSupportActionBar().setTitle(title);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                topCommentRef = FirebaseDatabase.getInstance().getReference()
                        .child("Comments")
                        .child(questionId)
                        .child(topCommentId);


                // get top comment
                topCommentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final Comment comment = dataSnapshot.getValue(Comment.class);
                        comment.setType(VIEWTYPE_TOP_STR);
                        commentAuthorId = comment.getSenderId();
                        firstCommentAuthorId = commentAuthorId;

                        // get Num of replies
                        topCommentRef.child("replies").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                comment.setReply(dataSnapshot.getChildrenCount());

                                if (mComments.contains(comment)) {
                                    mComments.set(mComments.indexOf(comment), comment);
                                    mAdapter.notifyItemChanged(mComments.indexOf(comment));
                                } else {
                                    mComments.add(0, comment);
                                    mAdapter.notifyItemInserted(0);
                                }

                                mRecyclerView.scrollToPosition(0);


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

                //get its repies
                getReplies();
            }
        }
        //set up editext listeners
        setEditText();


    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    //set up editext listeners
    void setEditText() {
        final Handler handler = new Handler();

        // set avatar for input
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(currentUser.getUid());

        // set avatar a
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(Reply.this).load(user.getPhotourl()).apply(RequestOptions.circleCropTransform().placeholder(R.drawable.default_avatar3))
                        .into(avatar);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    send.setVisibility(View.GONE);
                    commentAuthorId = firstCommentAuthorId;
                } else {
                    send.setVisibility(View.VISIBLE);
                }
            }


            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkInternet()){

                    final String text = mEditText.getText().toString();
                    send.setVisibility(View.GONE);
                    mEditText.setText("");
                    mEditText.clearFocus();

                    inputProgressBar.setVisibility(View.VISIBLE);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            addReply(text);
                        }
                    }, 666);

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
    }

    public boolean checkInternet() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            return true;
        } else {
            return false;


        }
    }


    void getReplies() {

        DatabaseReference repliesRef = FirebaseDatabase.getInstance().getReference()
                .child("Comments")
                .child(questionId)
                .child(topCommentId)
                .child("replies");
        repliesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Comment comment = dataSnapshot.getValue(Comment.class);
                mComments.add(comment);
                Collections.sort(mComments);

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
        });

        repliesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                findViewById(R.id.reply_progressBar).setVisibility(View.GONE);
                if (mComments.size() > 1) {
                    Collections.sort(mComments.subList(1, mComments.size() - 1));
                    mAdapter.notifyDataSetChanged();
                }
                if(getIntent().getExtras().getString("commentId",null)!=null
                        && !getIntent().getExtras().getString("commentId").isEmpty()){

                    for(final Comment comment : mComments){
                        if(comment.getId().equals(getIntent().getExtras().getString("commentId"))){
                            mRecyclerView.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mRecyclerView. scrollToPosition(mComments.indexOf(comment));

                                }
                            },1000);

                        }
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    void addReply(final String text) {
        final Comment comment = new Comment();
        comment.setId(UUID.randomUUID().toString());
        comment.setType(VIEWTYPE_TEXT_STR);
        comment.setSenderId(currentUser.getUid());
        comment.setText(text);
        comment.setTimestamp(new Date().getTime());


        //get answer
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("UserQA")
                .child(currentUser.getUid())
                .child(questionId)
                .child("answer");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                comment.setAnswer(dataSnapshot.getValue(String.class));
                topCommentRef.child("replies").child(comment.getId()).setValue(comment)
                        //handle failure
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                inputProgressBar.setVisibility(View.GONE);


                                Toast.makeText(Reply.this, "Failed to add comment", Toast.LENGTH_SHORT).show();

                            }
                        })


                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                inputProgressBar.setVisibility(View.GONE);

                                String notificationText = currentUser.getDisplayName()+ " has replied to your comment \n" + text;


                                if(!commentAuthorId.equals(currentUser.getUid())){
                                    SendNotification.sendReplyNotification(commentAuthorId,currentUser.getDisplayName(),notificationText,questionId,topCommentId,comment.getId());
                                }


                                Toast.makeText(Reply.this, "Comment Added", Toast.LENGTH_SHORT).show();
                            }
                        });

                hideKeyboard();
                commentAuthorId = firstCommentAuthorId;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void hideKeyboard() {
        //hide keyboard
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showKeyboard() {
        //hide keyboard
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    @Override
    public void replyClicked(String text, String commentAuthorId,String commentId) {
        this.commentAuthorId = commentAuthorId;
        this.commentId = commentId;
        mEditText.requestFocus();
        showKeyboard();
        mEditText.setText(text);
        mEditText.setSelection(mEditText.getText().length());


    }
}



