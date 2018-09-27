package app.jayang.icebr8k.Fragments;


import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import app.jayang.icebr8k.Adapter.TagAdapter;
import app.jayang.icebr8k.Model.TagModel;
import app.jayang.icebr8k.R;


/**
 * Use the {@link TagFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TagFragment extends android.support.v4.app.Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private String questionId;
    private RecyclerView recyclerView;
    private TagAdapter adapter;
    private GridLayoutManager gridLayoutManager;
    private ArrayList<TagModel> tagModels = new ArrayList<>();
    private TextView tag;
    private View view;


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

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_tag, container, false);
        tag = view.findViewById(R.id.text_view);
        recyclerView = view.findViewById(R.id.tag_recyclerView);
        gridLayoutManager = new GridLayoutManager(getContext(), 2,
                GridLayoutManager.VERTICAL, false);
        adapter = new TagAdapter(tagModels, getActivity());
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
        getData();

        return view;
    }

    private void getData() {
        tagModels.add(new TagModel("abc"));
        tagModels.add(new TagModel("123abc"));
        tagModels.add(new TagModel("ab123123c"));
        tagModels.add(new TagModel("ab342424c"));
        tagModels.add(new TagModel("abrwerc"));
        tagModels.add(new TagModel("abdsffsdfc"));
        tagModels.add(new TagModel("ab1c"));

        tagModels.add(new TagModel("abc"));
        tagModels.add(new TagModel("123abc"));
        tagModels.add(new TagModel("ab123123c"));
        tagModels.add(new TagModel("ab342424c"));
        tagModels.add(new TagModel("abrwerc"));
        tagModels.add(new TagModel("abdsffsdfc"));
        tagModels.add(new TagModel("ab1c"));
        tagModels.add(new TagModel("abc"));
        tagModels.add(new TagModel("123abc"));
        tagModels.add(new TagModel("ab123123c"));
        tagModels.add(new TagModel("ab342424c"));
       // tagModels.add(new TagModel("abrwerc"));
        tagModels.add(new TagModel(null));

        adapter.notifyDataSetChanged();

    }

}
