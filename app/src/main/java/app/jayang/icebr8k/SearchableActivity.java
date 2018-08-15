package app.jayang.icebr8k;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.ArrayList;

import app.jayang.icebr8k.Adapter.QuestionAnsweredAdapter;
import app.jayang.icebr8k.Modle.UserDialog;
import app.jayang.icebr8k.Modle.UserQA;

public class SearchableActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private ArrayList<UserQA> mUserQAArrayList;
    private ArrayList<UserQA> filterdList;
    private LinearLayoutManager layoutManager;
    private QuestionAnsweredAdapter mQuestionAnsweredAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);
        mToolbar = findViewById(R.id.searchable_toolbar);
        filterdList = new ArrayList<>();
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mRecyclerView = findViewById(R.id.seachable_recyclerView);
         layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
         mRecyclerView.setLayoutManager(layoutManager);
         if(getIntent()!=null){
             mUserQAArrayList = getIntent().getExtras().getParcelableArrayList("questions");
             mQuestionAnsweredAdapter = new QuestionAnsweredAdapter(filterdList,this);
             mRecyclerView.setAdapter(mQuestionAnsweredAdapter);

         }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_context_menu, menu);

        MenuItem mSearch = menu.findItem(R.id.pdf_menu_search_item);
        SearchView searchView = (SearchView) mSearch.getActionView();
        searchView.setIconified(false);
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint("Search Question");
        searchView.setOnQueryTextListener(this);
        searchView.requestFocus();
        searchView.setMaxWidth(  Integer.MAX_VALUE);
        return true;

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }




    @Override
    public boolean onQueryTextSubmit(String query) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        filterdList.clear();
        for(UserQA dialog : mUserQAArrayList){
            if(dialog.getQuestion().toLowerCase().contains(newText)&& !newText.isEmpty()){
                filterdList.add(dialog);
            }
        }
        if(!filterdList.isEmpty()) {
            mQuestionAnsweredAdapter.notifyDataSetChanged();
            mRecyclerView.setVisibility(View.VISIBLE);
        }else{
            mRecyclerView.setVisibility(View.INVISIBLE);
        }
        return true;
    }
}
