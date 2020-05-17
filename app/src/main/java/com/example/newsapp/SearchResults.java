package com.example.newsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.newsapp.adapter.HomePageAdapter;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;

public class SearchResults extends AppCompatActivity implements ClickListener{
    private static String TAG = "search", BOOKMARK = "bookmarks", KEYWORD = "query";
    boolean loading = false;
    JSONArray searchResults;
    List<NewsArticle> articles;
    HomePageAdapter adapter;
    private SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        ProgressBar spinner;
        spinner =  findViewById(R.id.progressBar3);
        TextView txt = findViewById(R.id.fetching_text);
        Intent intent = getIntent();
        refreshLayout = findViewById(R.id.refresh_search);
        String query = intent.getStringExtra(KEYWORD);
        Toolbar myToolBar = findViewById(R.id.toolbar_search);
        setSupportActionBar(myToolBar);
        loading = false;
        setTitle("Search Results for: " + query);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSearchResults(query);
        final Handler handler = new Handler();
        Runnable proceedsRunnable = new Runnable() {
            @Override
            public void run() {
                if(loading) {
                    spinner.setVisibility(GONE);
                    txt.setVisibility(GONE);
                    displayResults();
                }
                else {
                    handler.postDelayed(this, 100);
                }
            }
        };
        handler.post(proceedsRunnable);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loading = false;
                getSearchResults(query);
                final Handler handler = new Handler();
                Runnable proceedsRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if(loading) {
                            refreshLayout.setRefreshing(false);
                            displayResults();
                        } else {
                            handler.postDelayed(this, 100);
                        }
                    }
                };
                handler.post(proceedsRunnable);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    private void displayResults(){
        RecyclerView rv = findViewById(R.id.search_results);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        articles = new ArrayList<>();
        if(searchResults.length() == 0){
            findViewById(R.id.no_results).setVisibility(View.VISIBLE);
            return ;
        }
        for(int i=0; i < searchResults.length(); i++){
            try {
                JSONObject article = searchResults.getJSONObject(i);
                String title = article.getString("webTitle");
                String articleId = article.getString("id");
                String section = article.getString("sectionName");
                String imageUrl;
                try{
                    imageUrl = article.getJSONObject("blocks").getJSONObject("main")
                            .getJSONArray("elements").getJSONObject(0)
                            .getJSONArray("assets").getJSONObject(0)
                            .getString("file");
                }
                catch (Exception e){
                    imageUrl = Constants.DEFAULT_IMAGE;
                }
                String webUrl = article.getString("webUrl");
                ZonedDateTime publicationDate = ZonedDateTime.
                        parse(article.getString("webPublicationDate")).
                        withZoneSameLocal(ZoneId.of("GMT"));
                articles.add(new NewsArticle(title,section, imageUrl,
                        articleId, publicationDate, webUrl));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        adapter = new HomePageAdapter(articles, this);
        rv.setAdapter(adapter);
        adapter.setClickListener(this);
    }

    private void getSearchResults(final String query) {
        String url = Constants.SEARCH_ENDPOINT + query;
        RequestQueue que = Volley.newRequestQueue(getBaseContext());
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url,
                null, response -> {
                    try {
                        searchResults = response.getJSONObject("response")
                                .getJSONArray("results");
                        loading = true;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e(TAG, error.toString()));
        que.add(jsonRequest);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View view, int position) {
        Intent expandedArticleIntent = new Intent(this, ExpandedArticle.class);
        expandedArticleIntent.putExtra(Constants.DETAILED_ARTICLE_ID, articles.get(position).articleId);
        expandedArticleIntent.putExtra(Constants.DETAILED_ARTICLE_TITLE, articles.get(position).title);
        startActivity(expandedArticleIntent);
    }

    @Override
    public void onLongClick(View view, int position) {
        SharedPreferences pref = view.getContext().getSharedPreferences(BOOKMARK, 0);
        SharedPreferences.Editor addToBookmark = pref.edit();
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog);
        ImageView bookmarkIcon = dialog.findViewById(R.id.dialog_bookmark_icon);
        String articleId = articles.get(position).articleId;
        ImageView twitter = dialog.findViewById(R.id.twitter);
        ImageView img = dialog.findViewById(R.id.long_press_image);
        TextView dialogText = dialog.findViewById(R.id.dialog_text);
        dialogText.setText(articles.get(position).title);
        if(pref.contains(articles.get(position).articleId)){
            bookmarkIcon.setImageResource(R.drawable.ic_bookmarked);
        } else {
            bookmarkIcon.setImageResource(R.drawable.ic_bookmark);
        }
        twitter.setOnClickListener(v -> {
            String shareText = Constants.SHARE_TWITTER_TEXT
                    .replace("__link__", articles.get(position).webUrl);
            Intent twitterIntent = new Intent("android.intent.action.VIEW",
                    Uri.parse(Constants.TWITTER_INTENT.replace("__shareText__", shareText)));
            startActivity(twitterIntent);
        });
        bookmarkIcon.setOnClickListener(v -> {
            NewsArticle article = articles.get(position);
            String title = article.title;
            if(pref.getString(articleId,null) == null){
                bookmarkIcon.setImageResource(R.drawable.ic_bookmarked);
                addToBookmark.putString(articleId, article.toMyString());
                Toast.makeText(view.getContext(),
                        title+" added to Bookmarks", Toast.LENGTH_SHORT).show();
            } else {
                bookmarkIcon.setImageResource(R.drawable.ic_bookmark);
                addToBookmark.remove(articleId);
                Toast.makeText(view.getContext(),
                        title+" removed from Bookmarks", Toast.LENGTH_SHORT).show();
            }
            addToBookmark.apply();
            adapter.notifyDataSetChanged();
        });
        Picasso.with(this).load(articles.get(position).thumbnail).into(img);
        Picasso.with(this).load(Constants.TWITTER_IMAGE).into(twitter);
        dialog.show();
    }

    @Override
    public void bookmarkClick(View view, int position) {

    }
}
