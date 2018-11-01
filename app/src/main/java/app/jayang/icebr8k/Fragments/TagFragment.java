package app.jayang.icebr8k.Fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;

import app.jayang.icebr8k.Adapter.TagAdapter;
import app.jayang.icebr8k.Model.TagModel;
import app.jayang.icebr8k.R;


/**
 * Use the {@link TagFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TagFragment extends android.support.v4.app.Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBERS

    private static final String ARG_PARAM1 = "param1";
    public TagAdapter adapter;
    public ArrayList<TagModel> tagModels = new ArrayList<>();
    private String questionId;
    private RecyclerView recyclerView;
    private GridLayoutManager gridLayoutManager;
    private TextView tag;
    private View view;
    private CollectionReference mCollectionReference;
    private ListenerRegistration mRegistration;
    private final TagModel emptyTagModel = new TagModel(null,"1","1");

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
            mCollectionReference = FirebaseFirestore.getInstance().collection("Tags").document(questionId)
                    .collection(questionId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_tag, container, false);
        tag = view.findViewById(R.id.text_view);
        recyclerView = view.findViewById(R.id.tag_recyclerView);
        recyclerView.setItemAnimator(null);
        gridLayoutManager = new GridLayoutManager(getContext(), 2,
                GridLayoutManager.VERTICAL, false);
        adapter = new TagAdapter(tagModels, getActivity());
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
        getData();
        return view;
    }


    private void getData() {
        if (mCollectionReference != null) {
        mRegistration  = mCollectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            TagModel tagModel = document.toObject(TagModel.class);
                            if (!tagModels.contains(tagModel)) {
                                tagModels.add(tagModel);
                                adapter.notifyDataSetChanged();
                            }else{
                                tagModels.set(tagModels.indexOf(tagModel),tagModel);
                                adapter.notifyItemChanged(tagModels.indexOf(tagModel));
                            }
                        }
                        Collections.sort(tagModels);
                        if(!tagModels.contains(emptyTagModel)){
                            tagModels.add(emptyTagModel);
                        }else{
                            tagModels.remove(emptyTagModel);
                            tagModels.add(emptyTagModel);
                        }

                    }
                }
            });

        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            mRegistration.remove();
        } catch (NullPointerException e) {
            Log.d("TagFragment123", e.getMessage());
        }
    }
}
