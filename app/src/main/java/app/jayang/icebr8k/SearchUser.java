package app.jayang.icebr8k;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.daimajia.androidanimations.library.flippers.FlipInXAnimator;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import app.jayang.icebr8k.Modle.User;

public class SearchUser extends AppCompatActivity implements android.widget.SearchView.OnQueryTextListener {
    private Toolbar searchToolbar;
    private TextView username,notfound;
    private LinearLayout mLinearLayout;
    private  MaterialDialog searchingDialog;
    private android.widget.SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);
        searchToolbar = findViewById(R.id.search_toolbar);
        setSupportActionBar(searchToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        username = findViewById(R.id.search_username);
        mLinearLayout = findViewById(R.id.search_item);
        notfound =findViewById(R.id.search_notfound);

        searchingDialog  = new MaterialDialog.Builder(this)
                .content("Searching...")
                .progress(true, 0).build();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
         searchView =
                (android.widget.SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("Username");
        searchView.setOnQueryTextListener(this);
         searchView.setIconified(false);

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
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
        if(s.isEmpty()){
            mLinearLayout.setVisibility(View.GONE);
        }else{
            mLinearLayout.setVisibility(View.VISIBLE);

            username.setText(s);
        }

        return false;
    }

    private void searchUser(String usernameStr){

        DatabaseReference usernameRef = FirebaseDatabase.getInstance().getReference().
                child("Usernames").child(usernameStr);

        usernameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userId = dataSnapshot.getValue(String.class);
                if(userId!=null) {
                    getUserInfo(userId);


                }else{
                    searchingDialog.dismiss();
                    mLinearLayout.setVisibility(View.GONE);
                    notfound.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void getUserInfo(final String uid){
        final DatabaseReference userInfoRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(uid);
        userInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Intent i = new Intent(getBaseContext(), UserProfilePage.class);
                i.putExtra("userInfo",user);
                i.putExtra("userUid",uid);
                searchingDialog.dismiss();
                startActivity(i);
                overridePendingTransition(R.anim.slide_from_right,R.anim.slide_to_left);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}


