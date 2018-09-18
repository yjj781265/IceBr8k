package app.jayang.icebr8k.Fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.UUID;

import app.jayang.icebr8k.Model.Comment;

import app.jayang.icebr8k.Model.MyEditText;
import app.jayang.icebr8k.Model.User;
import app.jayang.icebr8k.R;

public class CommentBottomSheetFrag extends BottomSheetDialogFragment {

    private final String VIEWTYPE_TEXT_STR = "text";
    private final String VIEWTYPE_LOAD_STR = "load";



    private MyEditText mEditText;
    private Handler handler = new Handler();
    private ImageView send;
    private String  questionId;
    private ProgressBar mProgressBar;
    private ImageView avatar;
    private DatabaseReference commentRef;
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();



    public static CommentBottomSheetFrag newInstance(String param1) {
        CommentBottomSheetFrag fragment = new CommentBottomSheetFrag();
        Bundle args = new Bundle();
        args.putString("questionId", param1);
        fragment.setArguments(args);
        return fragment;
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            questionId = getArguments().getString("questionId");
            commentRef = FirebaseDatabase.getInstance().getReference()
                    .child("Comments")
                    .child(questionId);

        }
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetStyle);


    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        bottomSheetDialog.setContentView(R.layout.comment_bottomsheet);
        // disable bottomsheet drag;
        try {
            Field mBehaviorField = bottomSheetDialog.getClass().getDeclaredField("mBehavior");
            mBehaviorField.setAccessible(true);
            final BottomSheetBehavior behavior = (BottomSheetBehavior) mBehaviorField.get(bottomSheetDialog);
            behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                }
            });
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        mProgressBar = bottomSheetDialog.findViewById(R.id.comment_input_progressBar);
        avatar = bottomSheetDialog.findViewById(R.id.comment_avatar);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(currentUser.getUid())
                ;

        // set avatar
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(getActivity()).load(user.getPhotourl()).apply(RequestOptions.circleCropTransform().placeholder(R.drawable.default_avatar3))
                        .into(avatar);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        send = bottomSheetDialog.findViewById(R.id.comment_send);
        send.setVisibility(View.GONE);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String text = mEditText.getText().toString();
                mEditText.setText("");
                mProgressBar.setIndeterminate(true);
                mProgressBar.setVisibility(View.VISIBLE);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(questionId!=null){
                            sendComment(text);
                        }

                    }
                },666);


            }
        });


        mEditText= bottomSheetDialog.findViewById(R.id.comment_input);
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().isEmpty()){
                    send.setVisibility(View.GONE);
                }else{
                    send.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return bottomSheetDialog;
    }


    void sendComment(String text){
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
                commentRef.child(comment.getId()).setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mProgressBar.setVisibility(View.GONE);
                        dismiss();
                        Toast.makeText(getActivity(), "Comment Added", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }








}
