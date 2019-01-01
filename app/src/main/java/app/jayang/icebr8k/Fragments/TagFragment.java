package app.jayang.icebr8k.Fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
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
    private TextView no_tag;
    private View view;
    private boolean firstTime = true;
    private CollectionReference mCollectionReference;
    private ListenerRegistration mRegistration;
    private final TagModel emptyTagModel = new TagModel(null,"6666","1");

    public TagFragment() {
        // Required empty public constructor
    }

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
        no_tag = view.findViewById(R.id.tag_no_tag_text);
        recyclerView = view.findViewById(R.id.tag_recyclerView);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        gridLayoutManager = new GridLayoutManager(getContext(), 2,
                GridLayoutManager.VERTICAL, false);
        adapter = new TagAdapter(tagModels, getActivity());
        adapter.setHasStableIds(true);
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
                                tagModels.add(0,tagModel);
                                if(!firstTime){
                                    Collections.sort(tagModels);
                                    adapter.notifyDataSetChanged();
                                }
                            }else{
                                tagModels.set(tagModels.indexOf(tagModel),tagModel);
                                Collections.sort(tagModels);
                                adapter.notifyDataSetChanged();
                            }
                        }
                        if(firstTime){
                            Collections.sort(tagModels);
                            tagModels.add(tagModels.size(),emptyTagModel);
                            adapter.notifyDataSetChanged();
                            firstTime = false;
                        }



                    }
                    no_tag.setVisibility(tagModels.contains(emptyTagModel) && tagModels.size() ==1 ? View.VISIBLE :View.GONE);

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
