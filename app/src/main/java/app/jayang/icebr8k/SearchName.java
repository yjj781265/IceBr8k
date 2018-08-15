package app.jayang.icebr8k;

import android.content.Context;
import android.support.v4.view.MenuItemCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;

import app.jayang.icebr8k.Adapter.RecyclerAdapter;
import app.jayang.icebr8k.Modle.UserDialog;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.app.SwipeBackActivity;

public class SearchName extends AppCompatActivity implements SearchView.OnQueryTextListener {
    android.support.v7.widget.Toolbar mToolbar;

    private ArrayList<UserDialog> friendList;
    private ArrayList<UserDialog> filterdList;
    private RecyclerAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private  SearchView searchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_name);
        mToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.searchname_toolbar);
        mRecyclerView = (RecyclerView) findViewById(R.id.searchname_recyclerView);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(manager);
        filterdList =new ArrayList<>();


        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        if(getIntent()!=null){
            friendList = (ArrayList<UserDialog>) getIntent().getExtras().getSerializable("friendList");
        }

        if(friendList!=null) {

            mAdapter = new RecyclerAdapter(this, filterdList);
            mRecyclerView.setAdapter(mAdapter);
        }

    }


    private  void hideKeyboard(){
        //hide keyboard
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_context_menu,menu);
        final MenuItem searchItem = menu.findItem(R.id.pdf_menu_search_item);
        searchView =(SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setIconified(false);
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint(getString(R.string.hint1));
        searchView.setOnQueryTextListener(this);
        searchView.requestFocus();
        searchView.setMaxWidth(  Integer.MAX_VALUE);

        return true;
    }

    @Override
    public void onBackPressed() {
        hideKeyboard();

        finish();
        overridePendingTransition(0,0);

    }

    @Override
    public boolean onSupportNavigateUp() {
        //hide keyboard
        if(searchView.hasFocus()) {
            hideKeyboard();

        }

        finish();
        overridePendingTransition(0,0);

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
            if(dialog.getUser().getDisplayname().toLowerCase().contains(newText)&& !newText.isEmpty()){
                filterdList.add(dialog);
            }
        }
        if(!filterdList.isEmpty()) {
            mAdapter.notifyDataSetChanged();
            mRecyclerView.setVisibility(View.VISIBLE);
        }else{
            mRecyclerView.setVisibility(View.INVISIBLE);
        }
        Log.d("SearchName123","Changed");

        return true;
    }
}