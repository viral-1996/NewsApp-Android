package com.example.newsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormatSymbols;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.view.View.GONE;

public class ExpandedArticle extends AppCompatActivity {
    private static final String TAG = "ExpandedArticle";
    private boolean loading;
    private ZonedDateTime articleDate;
    private String id, title, section, imageUrl, description, articleUrl;
    private JSONObject articleDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent articleDetails = getIntent();
        description = "";
        id = articleDetails.getStringExtra(Constants.DETAILED_ARTICLE_ID);
        setContentView(R.layout.activity_expanded_article);
        Toolbar myToolBar = findViewById(R.id.my_toolbar);
        myToolBar.setTitle(articleDetails.getStringExtra(Constants.DETAILED_ARTICLE_TITLE));
        setSupportActionBar(myToolBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        ProgressBar loadingSpinner = findViewById(R.id.progress_bar_expanded_article);
        CardView expandedArticleCard = findViewById(R.id.expanded_card);
        TextView loadingText = findViewById(R.id.fetching_text);
        loading = false;
        getArticleData(id);
        final Handler handler = new Handler();
        Runnable proceedsRunnable = new Runnable() {
            @Override
            public void run() {
                if(loading) {
                    loadingSpinner.setVisibility(GONE);
                    loadingText.setVisibility(GONE);
                    expandedArticleCard.setVisibility(View.VISIBLE);
                    displayExpandedArticle();
                }
                else {
                    handler.postDelayed(this, 100);
                }
            }
        };
        handler.post(proceedsRunnable);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        SharedPreferences pref = this.getSharedPreferences(Constants.BOOKMARK, 0);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.bookmark_titlebar).setIcon(pref.getString(id, null) == null?
                R.drawable.ic_bookmark:R.drawable.ic_bookmarked);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.bookmark_titlebar:
                SharedPreferences pref = this.getSharedPreferences(Constants.BOOKMARK, 0);
                SharedPreferences.Editor addToBookmark = pref.edit();
                NewsArticle articleDetails = new NewsArticle(title, section, imageUrl,
                        id, articleDate, articleUrl);
                if(pref.getString(id,null) == null){
                    item.setIcon(R.drawable.ic_bookmarked);
                    addToBookmark.putString(id, articleDetails.toMyString());
                    Toast.makeText(this,
                            title+" added to Bookmarks", Toast.LENGTH_SHORT).show();
                } else {
                    item.setIcon(R.drawable.ic_bookmark);
                    addToBookmark.remove(id);
                    Toast.makeText(this,
                            title+" removed from Bookmarks", Toast.LENGTH_SHORT).show();
                }
                addToBookmark.apply();
                break;
            case R.id.twitter:
                String shareText = Constants.SHARE_TWITTER_TEXT
                        .replace("__link__", articleUrl);
                Intent viewIntent = new Intent("android.intent.action.VIEW",
                        Uri.parse(Constants.TWITTER_INTENT.replace("__shareText__", shareText)));
                startActivity(viewIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayExpandedArticle() {
        ZoneId la = ZoneId.of(Constants.ZONEID_LA);
        TextView titleTextView = findViewById(R.id.article_title);
        ImageView imageView = findViewById(R.id.expanded_image);
        TextView descriptionTextView = findViewById(R.id.description);
        TextView sectionTextView = findViewById(R.id.section_details);
        TextView dateOfArticle = findViewById(R.id.expanded_date);
        TextView hyperLink = findViewById(R.id.full_article_link);
        try {
            title = articleDetails.getString("webTitle");
            section = articleDetails.getString("sectionName");
            articleDate = ZonedDateTime.parse(articleDetails.getString("webPublicationDate"))
                    .withZoneSameLocal(ZoneId.of(Constants.ZONEID_GMT));
            try {
                imageUrl = articleDetails.getJSONObject("blocks").getJSONObject("main").
                        getJSONArray("elements").getJSONObject(0).
                        getJSONArray("assets").getJSONObject(0).getString("file");
            } catch (JSONException e) {
                imageUrl = Constants.DEFAULT_IMAGE;
            }
            articleUrl = articleDetails.getString("webUrl");
            JSONArray body = articleDetails.getJSONObject("blocks").getJSONArray("body");
            for (int i = 0; i < body.length(); i++) {
                description += body.getJSONObject(i).getString("bodyHtml") + "/n";
            }
            Spanned descriptionHTML = Html.fromHtml(description);
            titleTextView.setText(title);
            descriptionTextView.setText(descriptionHTML);
            sectionTextView.setText(section);
            articleDate = articleDate.withZoneSameInstant(la);
            String[] months = new DateFormatSymbols().getMonths();
            LocalDateTime dte = articleDate.toLocalDateTime();
            String articleDate = dte.getDayOfMonth() + " " +
                    months[dte.getMonthValue() - 1] + " " + dte.getYear();
            dateOfArticle.setText(articleDate);
            Picasso.with(this).load(imageUrl).into(imageView);
            Spanned linkToArticle = Html.fromHtml(
                    Constants.LINK_TO_MAIN_ARTICLE.replace("__articleUrl__", articleUrl));
            hyperLink.setMovementMethod(LinkMovementMethod.getInstance());
            hyperLink.setText(linkToArticle);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getArticleData(final String id) {
        String url = Constants.EXPANDED_ARTICLE_ENDPOINT+id;
        RequestQueue que = Volley.newRequestQueue(getBaseContext());
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url,
                null, response -> {
                    try {
                        articleDetails = response.getJSONObject("response").getJSONObject("content");
                        loading = true;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e(TAG, error.toString()));
        que.add(jsonRequest);
    }
}
