package app.jayang.icebr8k;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import app.jayang.icebr8k.Model.User;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class SearchUser extends SwipeBackActivity implements SearchView.OnQueryTextListener {
    private static final String TAG = "SearchUser";
    private Toolbar searchToolbar;
    private RecyclerView mRecyclerView;
    private TextView searchTitle;
    private RecyclerView.LayoutManager mLayoutManager;
    private TextView username, notfound;
    private LinearLayout mLinearLayout, searchListContainer;
    private MaterialDialog searchingDialog;
    private SearchView searchView;
    private SwipeBackLayout mSwipeBackLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);
        Log.d(TAG, "onCreate: ");
        searchToolbar = (Toolbar) findViewById(R.id.search_toolbar);
        setSupportActionBar(searchToolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mSwipeBackLayout = getSwipeBackLayout();
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        username = (TextView) findViewById(R.id.search_username);
        mLinearLayout = (LinearLayout) findViewById(R.id.search_item);
        searchListContainer = (LinearLayout) findViewById(R.id.search_user_list_view_container);
        searchTitle = (TextView) findViewById(R.id.search_user_list_view_title);
        notfound = (TextView) findViewById(R.id.search_notfound);
        mRecyclerView = (RecyclerView) findViewById(R.id.search_user_list_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        searchingDialog = new MaterialDialog.Builder(this)
                .content("Searching...")
                .progress(true, 0).build();
        mSwipeBackLayout.addSwipeListener(new SwipeBackLayout.SwipeListener() {
            @Override
            public void onScrollStateChange(int state, float scrollPercent) {

            }

            @Override
            public void onEdgeTouch(int edgeFlag) {
                if (edgeFlag == SwipeBackLayout.EDGE_LEFT && searchView.hasFocus()) {
                    hideKeyboard();
                }
            }

            @Override
            public void onScrollOverThreshold() {

            }
        });
    }


    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_context_menu, menu);

        searchView = (SearchView) menu.findItem(R.id.pdf_menu_search_item).getActionView();
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint("Search Username/Name");
        searchView.setOnQueryTextListener(this);
        searchView.setElevation(0f);
        searchView.requestFocus();
        searchView.setMaxWidth(Integer.MAX_VALUE);


        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (searchView.hasFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

        }
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void userSearchOnClick(View view) {
        searchingDialog.show();
        searchUser(username.getText().toString());
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        searchingDialog.show();
        searchUser(username.getText().toString());

        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        notfound.setVisibility(View.GONE);
        if (s.isEmpty()) {
            mLinearLayout.setVisibility(View.GONE);
        } else {
            mLinearLayout.setVisibility(View.VISIBLE);

            username.setText(s);
        }

        return false;
    }

    private void searchUser(final String usernameStr) {
        final String string = usernameStr.trim();
        if (!checkString(string)) {
            searchingDialog.dismiss();
            mLinearLayout.setVisibility(View.GONE);
            notfound.setVisibility(View.VISIBLE);
            return;
        }
        DatabaseReference usernameRef = FirebaseDatabase.getInstance().getReference().
                child("Usernames").child(string);

        usernameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userId = dataSnapshot.getValue(String.class);
                if (userId != null) {
                    getUserInfo(userId);
                } else {
                    searchByName(usernameStr);
                    Log.d(TAG, "search UserName failed, start searching by Name");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getUserInfo(final String uid) {
        final DatabaseReference userInfoRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(uid);
        userInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Intent i = new Intent(getBaseContext(), UserProfilePage.class);
                searchingDialog.dismiss();
                toUserInfoPage(user);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void toUserInfoPage(User user) {
        Intent i = new Intent(getBaseContext(), UserProfilePage.class);
        i.putExtra("userInfo", user);
        i.putExtra("userUid", user.getId());
        startActivity(i);
        finish();
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    private void searchByName(String textInput) {
        DatabaseReference nameRef = FirebaseDatabase.getInstance().getReference()
                .child("Users");
        nameRef.orderByChild("displayname").equalTo(textInput).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot);
                ArrayList<User> users = new ArrayList<>();
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        users.add(user);
                        Log.d(TAG, "User Found: " + user.getUsername());
                    }

                }
                updateListView(users);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void updateListView(ArrayList<User> users) {

        RecyclerView.Adapter mAdapter = new SearchUserAdapter(users);
        mRecyclerView.setAdapter(mAdapter);
        if (mAdapter.getItemCount() > 0) {
            searchListContainer.setVisibility(View.VISIBLE);
            searchTitle.setText("Found following users with name " + username.getText().toString());
        } else {
            searchListContainer.setVisibility(View.GONE);
            notfound.setVisibility(View.VISIBLE);
        }
        searchingDialog.dismiss();
        mLinearLayout.setVisibility(View.GONE);
        searchView.clearFocus();

    }

    private boolean checkString(String str) {
        if (str.contains(".") || str.contains("#") || str.contains("$") || str.contains("{")
                || str.contains("}")) {
            return false;
        } else {
            return true;
        }
    }




    public class SearchUserAdapter extends RecyclerView.Adapter<SearchUserAdapter.SearchUserViewHolder> {
        private ArrayList<User> users;

        // Provide a suitable constructor (depends on the kind of dataset)
        public SearchUserAdapter(ArrayList<User> users) {
            this.users = users;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public SearchUserAdapter.SearchUserViewHolder onCreateViewHolder(ViewGroup parent,
                                                                         int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_search_user, parent, false);

            SearchUserViewHolder vh = new SearchUserViewHolder(v);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(SearchUserViewHolder holder, int position) {
            try {
                holder.name.setText(users.get(position).getDisplayname());
                holder.username.setText(users.get(position).getUsername());
                Glide.with(SearchUser.this)
                        .load(users.get(position).getPhotourl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(holder.avatar);
            } catch (NullPointerException e) {
                Log.w(TAG, "onBindViewHolder: ", e);
            }


        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return users.size();
        }

        public class SearchUserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private TextView name, username;
            private ImageView avatar;

            public SearchUserViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                name = itemView.findViewById(R.id.name);
                username = itemView.findViewById(R.id.username);
                avatar = itemView.findViewById(R.id.search_user_avatar);
            }

            @Override
            public void onClick(View view) {
                User user = users.get(getAdapterPosition());
                try {
                    toUserInfoPage(user);
                } catch (NullPointerException e) {
                    Log.e(TAG, "onClick: ", e);
                }
            }
        }
    }


}




