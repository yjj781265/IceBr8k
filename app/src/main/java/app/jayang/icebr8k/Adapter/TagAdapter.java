package app.jayang.icebr8k.Adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import app.jayang.icebr8k.Model.TagModel;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.Utility.MyToolBox;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class TagAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    final int EMPTY_VH = 0, TAG_VH = 1;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String COLLECTION_PARENT_NODE = "Tags";
    private final String SUB_COLLECTION_TAG_LIKED = "TagLiked";
    ArrayList<TagModel> tagModels;
    Activity activity;

    public TagAdapter(ArrayList<TagModel> tagModels, Activity activity) {
        this.tagModels = tagModels;
        this.activity = activity;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == EMPTY_VH) {
            view = LayoutInflater.from(activity).inflate(R.layout.item_empty_placeholder, parent, false);
            return new EmptyViewHolder(view);
        } else {
            view = LayoutInflater.from(activity).inflate(R.layout.item_tag, parent, false);
            return new TagViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        TagModel tagModel = tagModels.get(position);
        return tagModel.getTagtxt() == null ?
                EMPTY_VH :
                TAG_VH;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TagModel tagModel = tagModels.get(position);

        if (holder instanceof TagViewHolder) {
            ((TagViewHolder) holder).bindView(tagModel);
            FrameLayout container = ((TagViewHolder) holder).container;
            container.setTag(tagModel.getTagId());
            Long likes = tagModel.getLikes() == null ? 0 : tagModel.getLikes();
            Long dislikes = tagModel.getDislikes() == null ? 0 : tagModel.getDislikes();
            Long total = likes + dislikes;
            double percentage = 0;
            if (total > 0) {
                percentage = ((double) likes / (double) total) * 100;
            }

            if (container.getTag().equals(tagModel.getTagId())) {
                if (percentage > 80) {
                    // container.getBackground().setColorFilter(ContextCompat.getColor(activity,R.color.tag_very_positive),PorterDuff.Mode.SRC_ATOP);
                    Toast.makeText(activity, tagModel.getTagId() + " dark green", Toast.LENGTH_SHORT).show();
                } else if (percentage > 69 && percentage < 79) {
                    //container.getBackground().setColorFilter(ContextCompat.getColor(activity,R.color.tag_little_positive),PorterDuff.Mode.SRC_ATOP);
                    Toast.makeText(activity, tagModel.getTagId() + " light green", Toast.LENGTH_SHORT).show();
                } else if (percentage > 40 && percentage < 59) {
                    //container.getBackground().setColorFilter(ContextCompat.getColor(activity,R.color.tag_neutral),PorterDuff.Mode.SRC_ATOP);
                    Toast.makeText(activity, tagModel.getTagId() + " blue", Toast.LENGTH_SHORT).show();
                } else if (percentage > 25 && percentage < 39) {
                    //container.getBackground().setColorFilter(ContextCompat.getColor(activity,R.color.tag_light_red),PorterDuff.Mode.SRC_ATOP);
                    Toast.makeText(activity, tagModel.getTagId() + " red", Toast.LENGTH_SHORT).show();
                } else if (percentage < 25 && percentage >= 0) {
                    //container.getBackground().setColorFilter(ContextCompat.getColor(activity,R.color.tag_dark_red),PorterDuff.Mode.SRC_ATOP);
                    Toast.makeText(activity, tagModel.getTagId() + " dark red", Toast.LENGTH_SHORT).show();
                }

            }


        }
    }

    @Override
    public int getItemCount() {
        return tagModels.size();
    }

    class TagViewHolder extends RecyclerView.ViewHolder {
        TextView tagName;
        FrameLayout container;
        PopupWindow mPopupWindow;
        View customView;
        ImageView thumbUp;

        ImageView thumbDown;
        TextView likes;
        TextView dislikes;


        public TagViewHolder(View itemView) {
            super(itemView);

            tagName = itemView.findViewById(R.id.tag_txt);
            container = itemView.findViewById(R.id.tag_container);
            // Initialize a new instance of LayoutInflater service

            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(LAYOUT_INFLATER_SERVICE);

            // Inflate the custom layout/view
            customView = inflater.inflate(R.layout.tag_popup, null);
            thumbUp = customView.findViewById(R.id.tag_thumbup);
            thumbDown = customView.findViewById(R.id.tag_thumbdown);
            likes = customView.findViewById(R.id.tag_likes);
            dislikes = customView.findViewById(R.id.tag_dislikes);

            mPopupWindow = new PopupWindow(
                    customView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                        TagModel tagModel = tagModels.get(getAdapterPosition());
                        showPopup(view, tagModel);
                    }
                }
            });

        }

        private void bindView(TagModel tagModel) {
            tagName.setText(tagModel.getTagtxt());
            final DocumentReference sublikedDocRef = db.collection(COLLECTION_PARENT_NODE).document(tagModel.getQuestionId()).collection(tagModel.getQuestionId())
                    .document(tagModel.getTagId()).collection(SUB_COLLECTION_TAG_LIKED).document(FirebaseAuth.getInstance().getCurrentUser().getUid());
            sublikedDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            boolean selected = documentSnapshot.getBoolean("liked");
                            thumbUp.setSelected(selected);
                            thumbDown.setSelected(!selected);
                        }
                    }
                }
            });

        }


        private void showPopup(View parentView, final TagModel tagModel) {
            mPopupWindow.setOutsideTouchable(true);
            likes.setText(String.valueOf(tagModel.getLikes() == null ? 0 : tagModel.getLikes()));
            dislikes.setText(String.valueOf(tagModel.getDislikes() == null ? 0 : tagModel.getDislikes()));
            thumbUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Integer likedCount = Integer.valueOf(likes.getText().toString());
                    Integer dislikedCount = Integer.valueOf(dislikes.getText().toString());

                    // check thumbDown is selected or not
                    if (thumbDown.isSelected()) {
                        thumbDown.setSelected(false);
                        disliked(tagModel, false);
                        dislikes.setText(String.valueOf(--dislikedCount < 0 ? 0 : dislikedCount));
                    }

                    if (thumbUp.isSelected()) {
                        thumbUp.setSelected(false);
                        likes.setText(String.valueOf(--likedCount < 0 ? 0 : likedCount));
                    } else {
                        thumbUp.setSelected(true);
                        likes.setText(String.valueOf(++likedCount));

                    }


                    liked(tagModel, view.isSelected());
                }
            });
            thumbDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Integer likedCount = Integer.valueOf(likes.getText().toString());
                    Integer dislikedCount = Integer.valueOf(dislikes.getText().toString());
                    // check thumbUp is selected or not
                    if (thumbUp.isSelected()) {
                        thumbUp.setSelected(false);
                        likes.setText(String.valueOf(--likedCount < 0 ? 0 : likedCount));
                        liked(tagModel, false);
                    }

                    if (thumbDown.isSelected()) {
                        thumbDown.setSelected(false);
                        dislikes.setText(String.valueOf(--dislikedCount < 0 ? 0 : dislikedCount));
                    } else {
                        thumbDown.setSelected(true);
                        dislikes.setText(String.valueOf(++dislikedCount));

                    }
                    disliked(tagModel, view.isSelected());
                }
            });

            mPopupWindow.showAsDropDown(parentView,
                    MyToolBox.convertDptoPixel(16f, parentView.getContext()), 0);

        }

        public void liked(TagModel tagModel, final Boolean selected) {
            final DocumentReference likedDocRef = db.collection(COLLECTION_PARENT_NODE).document(tagModel.getQuestionId()).collection(tagModel.getQuestionId())
                    .document(tagModel.getTagId());
            final CollectionReference likedColRef = db.collection(COLLECTION_PARENT_NODE).document(tagModel.getQuestionId()).collection(tagModel.getQuestionId())
                    .document(tagModel.getTagId()).collection(SUB_COLLECTION_TAG_LIKED);
            final Map<String, Boolean> map = new HashMap<>();
            map.put("liked", true);
            db.runTransaction(new Transaction.Function<Void>() {
                @Nullable
                @Override
                public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                    DocumentSnapshot snapshot = transaction.get(likedDocRef);
                    Long count = snapshot.getLong("likes") == null ? 0 : snapshot.getLong("likes");
                    if (selected) {
                        count++;
                    } else {
                        count = (count - 1) < 0 ? 0 : snapshot.getLong("likes") - 1;
                    }
                    transaction.update(likedDocRef, "likes", count);
                    return null;
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if (selected) {
                        Toast.makeText(activity, "Liked", Toast.LENGTH_SHORT).show();
                    }


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(activity, "Failed" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            if (selected) {
                likedColRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).set(map);
            } else {
                likedColRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).delete();
            }
        }

        public void disliked(TagModel tagModel, final Boolean selected) {
            final DocumentReference sfDocRef = db.collection(COLLECTION_PARENT_NODE).document(tagModel.getQuestionId()).collection(tagModel.getQuestionId())
                    .document(tagModel.getTagId());
            final CollectionReference disliiedColRef = db.collection(COLLECTION_PARENT_NODE).document(tagModel.getQuestionId()).collection(tagModel.getQuestionId())
                    .document(tagModel.getTagId()).collection(SUB_COLLECTION_TAG_LIKED);
            final Map<String, Boolean> map = new HashMap<>();
            map.put("liked", false);
            db.runTransaction(new Transaction.Function<Void>() {
                @Nullable
                @Override
                public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                    DocumentSnapshot snapshot = transaction.get(sfDocRef);
                    Long count = snapshot.getLong("dislikes") == null ? 0 : snapshot.getLong("dislikes");
                    if (selected) {
                        count++;
                    } else {
                        count = (count - 1) < 0 ? 0 : snapshot.getLong("dislikes") - 1;

                    }

                    transaction.update(sfDocRef, "dislikes", count);
                    return null;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(activity, "Failed" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if (selected) {
                        Toast.makeText(activity, "Disliked", Toast.LENGTH_SHORT).show();
                    }

                }
            });

            if (selected) {
                disliiedColRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).set(map);
            } else {
                disliiedColRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).delete();
            }
        }


    }


}
