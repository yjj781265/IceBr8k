package app.jayang.icebr8k;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;

import app.jayang.icebr8k.Adapter.UserLocationDialogAdapter;
import app.jayang.icebr8k.Model.UserLocationDialog;

public class SearchPeopleNearby extends AppCompatActivity implements SearchView.OnQueryTextListener {

    android.support.v7.widget.Toolbar mToolbar;

    private ArrayList<UserLocationDialog> mList;
    private ArrayList<UserLocationDialog> filterdList;
    private UserLocationDialogAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private  SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_people_nearby);
        mToolbar = findViewById(R.id.search_peopleNearby_toolbar);
        mRecyclerView =  findViewById(R.id.search_peopleNearby_recyclerVIew);
        LinearLayoutManager manager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        mRecyclerView.setLayoutManager(manager);
        filterdList =new ArrayList<>();


        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        if(getIntent()!=null){

            mList = getIntent().getExtras().getParcelableArrayList ("peopleNearbyList");
        }

        if(mList!=null) {

            mAdapter = new UserLocationDialogAdapter(filterdList);
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


    }

    @Override
    public boolean onSupportNavigateUp() {
        //hide keyboard
        if(searchView.hasFocus()) {
            hideKeyboard();

        }

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
        for(UserLocationDialog dialog : mList){
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


        return true;
    }
}
