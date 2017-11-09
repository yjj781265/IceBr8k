package app.jayang.icebr8k.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.victor.loading.newton.NewtonCradleLoading;

import java.util.ArrayList;
import java.util.Collections;

import app.jayang.icebr8k.Modle.User;
import app.jayang.icebr8k.R;
import app.jayang.icebr8k.RecyclerAdapter;
import app.jayang.icebr8k.UserProfilePage;
import app.jayang.icebr8k.login_page;

/**
 * Created by LoLJay on 10/20/2017.
 */

public class Userstab_Fragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener {
    View view;

    FirebaseDatabase mDatabase;
    DatabaseReference databaseReference;
    ArrayList<User> mUserArrayList;
    RecyclerView mRecyclerView;
    SwipeRefreshLayout refreshLayout;
    NewtonCradleLoading newtonCradleLoading;
    Toolbar toolbar;
    User currentUserDB;
    FirebaseUser currentUser;
    GoogleApiClient mGoogleApiClient;
    ImageView profileImg;


    public Userstab_Fragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance();
        mUserArrayList = new ArrayList<>();
        setHasOptionsMenu(true);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        getCurrentUserDB(currentUser);
        populateUserList();


    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.users_tab, container, false);
        mRecyclerView = view.findViewById(R.id.recyclerView_id);
        LinearLayoutManager manager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), 1));
        newtonCradleLoading = view.findViewById(R.id.newton_cradle_loading);
        newtonCradleLoading.setVisibility(view.VISIBLE);
        newtonCradleLoading.setLoadingColor(R.color.holo_red_light);
        newtonCradleLoading.start();
        refreshLayout = view.findViewById(R.id.swiperefresh);
        profileImg = view.findViewById(R.id.imageBtn);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // [END configure_signin]

        // [START build_client]
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(view.getContext())
                .enableAutoManage(getActivity() /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(true);
                populateUserList();
            }
        });
        toolbar = view.findViewById(R.id.users_toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(" ");

        profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Userstab_Fragment.this.getContext(), UserProfilePage.class);
                intent.putExtra("selfProfile", currentUserDB);
                startActivity(intent);
            }
        });
        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.log_out_item:
                Signout();
                getActivity().finish();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void populateUserList() {
        databaseReference = mDatabase.getReference("Users");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUserArrayList.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {

                    User mUser = userSnapshot.getValue(User.class);
                    if (!mUser.getUsername().equals(currentUserDB.getUsername())) {
                        mUserArrayList.add(mUser);

                    }


                }
                //Toast.makeText(getContext(),"Refreshed",Toast.LENGTH_SHORT).show();
                if (mUserArrayList != null && !mUserArrayList.isEmpty()) {
                    Collections.sort(mUserArrayList);
                    mRecyclerView.setHasFixedSize(false);
                    mRecyclerView.setAdapter(new RecyclerAdapter(getContext(), mUserArrayList));
                    refreshLayout.setRefreshing(false);
                    newtonCradleLoading.stop();
                    newtonCradleLoading.setVisibility(view.INVISIBLE);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                refreshLayout.setRefreshing(true);
            }
        });

    }

    public User getCurrentUserDB(FirebaseUser currentUser) {
        mDatabase.getReference("Users/" + currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUserDB = dataSnapshot.getValue(User.class);
                Glide.with(view.getContext()).load(currentUserDB.getPhotourl()).
                        apply(RequestOptions.circleCropTransform()).into(profileImg);
                Log.d("currentUserDB", currentUserDB.getUsername());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return currentUserDB;

    }


    public void Signout() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("Users/" + currentUser.getUid());
            mRef.child("onlineStats").setValue("0");
        }
        FirebaseAuth.getInstance().signOut();


        if (currentUser.getProviders().get(0).contains("google")) {

            // Google sign out
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {

                        @Override
                        public void onResult(@NonNull Status status) {

                        }
                    });
        }
        Intent intent = new Intent(view.getContext(), login_page.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


}
