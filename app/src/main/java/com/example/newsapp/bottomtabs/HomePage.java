package com.example.newsapp.bottomtabs;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.newsapp.ClickListener;
import com.example.newsapp.Constants;
import com.example.newsapp.ExpandedArticle;
import com.example.newsapp.NewsArticle;
import com.example.newsapp.R;
import com.example.newsapp.adapter.HomePageAdapter;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomePage extends Fragment implements ClickListener {

    private static final String TAG = "HomePage", BOOKMARKS = "bookmarks", SECTION = "home";
    private String city, state, currentWeatherType;
    private JSONArray newsArticles;
    private int temperature;
    private boolean dataReceived;
    private SwipeRefreshLayout refreshLayout;
    private List<NewsArticle> articles;
    private HomePageAdapter adapter;


    public HomePage() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view  = inflater.inflate(R.layout.fragment_homepage, container, false);
        refreshLayout = view.findViewById(R.id.refresh_homepage);
        ProgressBar spinner;
        spinner =  view.findViewById(R.id.progressBar1);
        Bundle bundle = getArguments();
        TextView txt =view.findViewById(R.id.fetching_text);
        city = bundle.getString("city");
        state = bundle.getString("state");
        dataReceived = false;
        getData();
        final Handler handler = new Handler();
        Runnable proceedsRunnable = new Runnable() {
            @Override
            public void run() {
                if(dataReceived) {
                    spinner.setVisibility(View.GONE);
                    txt.setVisibility(View.GONE);
                    generateWeatherCard(view);
                    getHomePageNews(view);
                } else {
                    handler.postDelayed(this, 100);
                }
            }
        };
        proceedsRunnable.run();
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                dataReceived = false;
                getData();
                final Handler handler = new Handler();
                Runnable proceedsRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if(dataReceived) {
                            refreshLayout.setRefreshing(false);
                            generateWeatherCard(view);
                            getHomePageNews(view);
                        } else {
                            handler.postDelayed(this, 100);
                        }
                    }
                };
                handler.post(proceedsRunnable);
            }
        });
        return view;
    }

    private void getHomePageNews(final View view) {
        RecyclerView rv = view.findViewById(R.id.news_articles);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);
        articles = new ArrayList<>();
        for(int i = 0; i < newsArticles.length(); i ++){
            try {
                JSONObject article = newsArticles.getJSONObject(i);
                String title = article.getString("webTitle");
                String section = article.getString("sectionName");
                String thumbnail;
                try {
                    thumbnail = article.getJSONObject("fields").getString("thumbnail");
                } catch (Exception e){
                    thumbnail = Constants.DEFAULT_IMAGE;
                }
                String articleId = article.getString("id");
                ZonedDateTime publicationDate = ZonedDateTime.parse(
                        article.getString("webPublicationDate"))
                        .withZoneSameLocal(ZoneId.of(Constants.ZONEID_GMT));
                String webUrl = article.getString("webUrl");
                articles.add(new NewsArticle(title,section, thumbnail,
                        articleId, publicationDate, webUrl));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        adapter = new HomePageAdapter(articles, getContext());
        rv.setAdapter(adapter);
        adapter.setClickListener(this);
    }

    private void generateWeatherCard(final View view) {
        view.findViewById(R.id.card_view).setVisibility(View.VISIBLE);
        TextView cityTextView = view.findViewById(R.id.cityTextView);
        TextView stateTextView = view.findViewById(R.id.stateTextView);
        TextView temperatureTextView = view.findViewById(R.id.temperatureTextView);
        TextView weatherTextView = view.findViewById(R.id.weatherTextView);
        cityTextView.setText(city);
        stateTextView.setText(state);
        temperatureTextView.setText(temperature+"°C");
        weatherTextView.setText(currentWeatherType);
        String weatherCardImageUrl;
        ImageView weatherCardImage = view.findViewById(R.id.backgroundImage);
        switch (currentWeatherType) {
            case "Clouds":
                weatherCardImageUrl = "https://csci571.com/hw/hw9/images/android/cloudy_weather.jpg";
                break;
            case "Clear":
                weatherCardImageUrl = "https://csci571.com/hw/hw9/images/android/clear_weather.jpg";
                break;
            case "Snow":
                weatherCardImageUrl = "https://csci571.com/hw/hw9/images/android/snow_weather.jpg";
                break;
            case "Rain":
                weatherCardImageUrl = "https://csci571.com/hw/hw9/images/android/rainy_weather.jpg";
                break;
            case "Drizzle":
                weatherCardImageUrl = "https://csci571.com/hw/hw9/images/android/rainy_weather.jpg";
                break;
            case "Thunderstorm":
                weatherCardImageUrl = "https://csci571.com/hw/hw9/images/android/thunder_weather.jpg";
                break;
            default:
                weatherCardImageUrl = "https://csci571.com/hw/hw9/images/android/sunny_weather.jpg";
                break;
        }
        Picasso.with(getContext()).load(weatherCardImageUrl).into(weatherCardImage);
    }

    private void getData() {
        String url = Constants.WEATHER_API.replace("__city__", city);
        RequestQueue que = Volley.newRequestQueue(getContext());
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url,
                null, response -> {
                    try {
                        JSONObject weatherData =
                                (JSONObject) response.getJSONArray("weather").get(0);
                        currentWeatherType = weatherData.getString("main");
                        temperature = response.getJSONObject("main").getInt("temp");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Log.e(TAG, error.toString()));
        que.add(jsonRequest);
        url = Constants.BACKEND_NEWS_ENDPOINT+SECTION;
        jsonRequest = new JsonObjectRequest(Request.Method.GET, url,
                null, response -> {
                    try {
                        newsArticles = response.getJSONObject("response")
                                .getJSONArray("results");
                        dataReceived = true;
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
        Intent expandedArticleIntent = new Intent(getContext(), ExpandedArticle.class);
        expandedArticleIntent.putExtra(Constants.DETAILED_ARTICLE_ID, articles.get(position).articleId);
        expandedArticleIntent.putExtra(Constants.DETAILED_ARTICLE_TITLE, articles.get(position).title);
        startActivity(expandedArticleIntent);
    }

    @Override
    public void onLongClick(View view, int position) {
        SharedPreferences pref = view.getContext().getSharedPreferences(BOOKMARKS, 0);
        SharedPreferences.Editor addToBookmark = pref.edit();
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog);
        ImageView bookmarkIcon = dialog.findViewById(R.id.dialog_bookmark_icon);
        String articleId = articles.get(position).articleId;
        ImageView twitter = dialog.findViewById(R.id.twitter);
        ImageView img = dialog.findViewById(R.id.long_press_image);
        TextView dialogText = dialog.findViewById(R.id.dialog_text);
        dialogText.setText(articles.get(position).title);
        if(pref.contains(articleId)){
            bookmarkIcon.setImageResource(R.drawable.ic_bookmarked);
        } else{
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
            if(pref.getString(articleId,null) == null) {
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
        Picasso.with(getContext()).load(articles.get(position).thumbnail).into(img);
        Picasso.with(getContext()).load(Constants.TWITTER_IMAGE).into(twitter);
        dialog.show();
    }

    @Override
    public void bookmarkClick(View view, int position) {

    }
}
