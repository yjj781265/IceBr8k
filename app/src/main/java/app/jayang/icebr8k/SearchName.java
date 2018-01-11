package app.jayang.icebr8k;

import android.content.Context;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toolbar;

import java.util.ArrayList;

import app.jayang.icebr8k.Modle.UserDialog;

public class SearchName extends AppCompatActivity implements SearchView.OnQueryTextListener {
  android.support.v7.widget.Toolbar mToolbar;

  private ArrayList<UserDialog> friendList;
  private ArrayList<UserDialog> filterdList;
  private RecyclerAdapter mAdapter;
  private RecyclerView mRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_name);
        mToolbar = findViewById(R.id.searchname_toolbar);
        mRecyclerView = findViewById(R.id.searchname_recyclerView);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(manager);
        filterdList =new ArrayList<>();

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        friendList = getIntent().getExtras().getParcelableArrayList("friendList");
        if(friendList!=null) {
            Log.d("SearchName123",String.valueOf(friendList.size())+ friendList.get(0));
            mAdapter = new RecyclerAdapter(getApplicationContext(), filterdList);
            mRecyclerView.setAdapter(mAdapter);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_context_menu,menu);
        final MenuItem searchItem = menu.findItem(R.id.pdf_menu_search_item);
        SearchView searchView =(SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setIconified(false);
        searchView.setQueryHint(getString(R.string.hint1));
        searchView.setOnQueryTextListener(this);
        searchView.requestFocus();
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        //hide keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        finish();
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return  false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        filterdList.clear();
        for(UserDialog dialog : friendList){
            if(dialog.getName().toLowerCase().contains(newText)&& !newText.isEmpty()){
                filterdList.add(dialog);
            }
        }
        mAdapter.notifyDataSetChanged();
        Log.d("SearchName123","Changed");

        return true;
    }
}
