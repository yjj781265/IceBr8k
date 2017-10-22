package app.jayang.icebr8k;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by LoLJay on 10/20/2017.
 */

public class Userstab_Fragment extends Fragment {
    View view;

    FirebaseDatabase mDatabase;
    DatabaseReference databaseReference;
    ArrayList<User> mUserArrayList;
    RecyclerView mRecyclerView;
    SwipeRefreshLayout refreshLayout;
    public Userstab_Fragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance();
        mUserArrayList=new ArrayList<>();
        populateUserList();

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.users_tab,container,false);
        mRecyclerView = view.findViewById(R.id.recyclerView_id);
        LinearLayoutManager manager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),1));
        refreshLayout = view.findViewById(R.id.swiperefresh);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populateUserList();
            }
        });

        return view;
    }

    public void populateUserList(){
        databaseReference = mDatabase.getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUserArrayList.clear();
                for(DataSnapshot userSnapshot : dataSnapshot.getChildren()){
                    User mUser = userSnapshot.getValue(User.class);
                    mUserArrayList.add(mUser);


                }
                mRecyclerView.setAdapter(new RecyclerAdapter(getContext(),mUserArrayList));
                refreshLayout.setRefreshing(false);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


}
