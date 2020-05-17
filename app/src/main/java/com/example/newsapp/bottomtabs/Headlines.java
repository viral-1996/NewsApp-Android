package com.example.newsapp.bottomtabs;


import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;
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
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class Headlines extends Fragment implements ClickListener{
    private static final String TAG = "Headlines", DEFAULT_TAB = "WORLD";
    private Adapter adapter;
    private boolean loading;
    private SwipeRefreshLayout refreshLayout;
    private JSONArray newsArticles;
    private String url, section;
    private List<NewsArticle> articles;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_headlines, container, false);
        final ViewPager viewPager = view.findViewById(R.id.viewpager);
        refreshLayout = view.findViewById(R.id.headlines_page);
        ProgressBar loadingSpinner;
        RecyclerView recyclerView = view.findViewById(R.id.section_articles);
        loadingSpinner =  view.findViewById(R.id.progressBar2);
        TextView loadingText = view.findViewById(R.id.fetching_text);
        setupViewPager(viewPager);
        TabLayout tabs = view.findViewById(R.id.result_tabs);
        tabs.setupWithViewPager(viewPager);
        TabLayout.Tab defaultTab = tabs.getTabAt(0);
        defaultTab.select();
        getSectionData(DEFAULT_TAB);
        loading = false;
        final Handler handler = new Handler();
        Runnable proceedsRunnable = new Runnable() {
            @Override
            public void run() {
                if(loading) {
                    loadingSpinner.setVisibility(View.GONE);
                    loadingText.setVisibility(View.GONE);
                    generateSectionNews(view);
                }
                else {
                    handler.postDelayed(this, 100);
                }
            }
        };
        handler.post(proceedsRunnable);
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                loadingSpinner.setVisibility(View.VISIBLE);
                loadingText.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                section = adapter.getPageTitle(position).toString();
                loading = false;
                getSectionData(section);
                final Handler handler = new Handler();
                Runnable proceedsRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if(loading) {
                            loadingSpinner.setVisibility(View.GONE);
                            loadingText.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            generateSectionNews(view);
                        }
                        else {
                            handler.postDelayed(this, 100);
                        }
                    }
                };
                handler.post(proceedsRunnable);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

        });

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                section = adapter.getPageTitle(tabs.getSelectedTabPosition()).toString();
                loading = false;
                getSectionData(section);
                final Handler handler = new Handler();
                Runnable proceedsRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if(loading) {
                            generateSectionNews(view);
                            refreshLayout.setRefreshing(false);
                        }
                        else {
                            handler.postDelayed(this, 100);
                        }
                    }
                };
                handler.post(proceedsRunnable);
            }
        });
        return view;
    }

    private void generateSectionNews(final View view) {
        RecyclerView rv = view.findViewById(R.id.section_articles);
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
                    thumbnail = article.getJSONObject("blocks").getJSONObject("main")
                            .getJSONArray("elements").getJSONObject(0)
                            .getJSONArray("assets").getJSONObject(0)
                            .getString("file");
                }
                catch (Exception e){
                    thumbnail = Constants.DEFAULT_IMAGE;
                }
                String articleId = article.getString("id");
                ZonedDateTime publicationDate = ZonedDateTime.parse(
                        article.getString("webPublicationDate"))
                        .withZoneSameLocal(ZoneId.of(Constants.ZONEID_GMT));
                String webUrl = article.getString("webUrl");
                articles.add(new NewsArticle(title,section, thumbnail, articleId,
                        publicationDate, webUrl));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        HomePageAdapter adapter = new HomePageAdapter(articles, getContext());
        rv.setAdapter(adapter);
        adapter.setClickListener(this);
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new Adapter(getChildFragmentManager());
        adapter.addFragment(new Section(), "WORLD");
        adapter.addFragment(new Section(), "BUSINESS");
        adapter.addFragment(new Section(), "POLITICS");
        adapter.addFragment(new Section(), "SPORTS");
        adapter.addFragment(new Section(), "TECHNOLOGY");
        adapter.addFragment(new Section(), "SCIENCE");
        viewPager.setAdapter(adapter);
    }

    private void getSectionData(String section) {
        // Fetching Weather News
        section = section.toLowerCase();
        section = section.equals("sports")?"sport":section;
        url = Constants.BACKEND_NEWS_ENDPOINT + section;
        RequestQueue que = Volley.newRequestQueue(getActivity().getApplicationContext());
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url,
                null, response -> {
                    try {
                        newsArticles = response.getJSONObject("response")
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
        Intent expandedArticleIntent = new Intent(getContext(), ExpandedArticle.class);
        expandedArticleIntent.putExtra(Constants.DETAILED_ARTICLE_ID, articles.get(position).articleId);
        expandedArticleIntent.putExtra(Constants.DETAILED_ARTICLE_TITLE, articles.get(position).title);
        startActivity(expandedArticleIntent);
    }

    @Override
    public void onLongClick(View vw, int position) {
        SharedPreferences pref = getContext().getSharedPreferences(Constants.BOOKMARK, 0);
        SharedPreferences.Editor addToBookmark = pref.edit();
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog);
        ImageView bookmarkIcon = dialog.findViewById(R.id.dialog_bookmark_icon);
        String articleId = articles.get(position).articleId;
        ImageView twitter = dialog.findViewById(R.id.twitter);
        ImageView articleImage = dialog.findViewById(R.id.long_press_image);
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
            Intent viewIntent = new Intent("android.intent.action.VIEW",
                    Uri.parse(Constants.TWITTER_INTENT.replace("__shareText__", shareText)));
            startActivity(viewIntent);
        });
        bookmarkIcon.setOnClickListener(v -> {
            NewsArticle article = articles.get(position);
            String title = article.title;
            if(pref.getString(articleId,null) == null){
                bookmarkIcon.setImageResource(R.drawable.ic_bookmarked);
                addToBookmark.putString(articleId, article.toMyString());
                Toast.makeText(getContext(),
                        title+" added to Bookmarks", Toast.LENGTH_SHORT).show();
            } else {
                bookmarkIcon.setImageResource(R.drawable.ic_bookmark);
                addToBookmark.remove(articleId);
                Toast.makeText(getContext(),
                        title+" removed from Bookmarks", Toast.LENGTH_SHORT).show();
            }
            RecyclerView recyclerView = view.findViewById(R.id.section_articles);
            recyclerView.getAdapter().notifyItemChanged(position);
            addToBookmark.apply();
        });
        Picasso.with(getContext()).load(articles.get(position).thumbnail).into(articleImage);
        Picasso.with(getContext()).load(Constants.TWITTER_IMAGE).into(twitter);
        dialog.show();
    }

    @Override
    public void bookmarkClick(View view, int position) {

    }
}

class Adapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    public Adapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFragment(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }
}