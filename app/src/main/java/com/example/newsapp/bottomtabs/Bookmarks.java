package com.example.newsapp.bottomtabs;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.newsapp.ClickListener;
import com.example.newsapp.Constants;
import com.example.newsapp.ExpandedArticle;
import com.example.newsapp.NewsArticle;
import com.example.newsapp.R;
import com.example.newsapp.adapter.BookmarkPageAdapter;
import com.squareup.picasso.Picasso;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class Bookmarks extends Fragment implements ClickListener {
    private static String TAG = "Bookmarks";
    private List<NewsArticle> articles;
    private BookmarkPageAdapter adapter;
    private View view;
    public Bookmarks() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_bookmarks, container, false);
        articles = new ArrayList<>();
        SharedPreferences pref = getContext().getSharedPreferences(Constants.BOOKMARK, 0);
        Map<String,?> keys = pref.getAll();
        for(Map.Entry<String,?> entry : keys.entrySet()) {
            try {
                articles.add(NewsArticle.toNewsArticle(entry.getValue().toString()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if(checkEmptyList()) {
            return view;
        }
        displayArticles(view);
        return view;
    }

    private boolean checkEmptyList() {
        if (articles.size() == 0) {
            view.findViewById(R.id.no_bookmarks).setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }

    private void displayArticles(final View view) {
        RecyclerView rv = view.findViewById(R.id.bookmark_recycler_view);
        GridLayoutManager glm = new GridLayoutManager(getContext(), 2);
        rv.setLayoutManager(glm);
        DividerItemDecoration divide = new DividerItemDecoration(getContext(), glm.getOrientation());
        rv.addItemDecoration(divide);
        adapter = new BookmarkPageAdapter(articles, getContext(), view.findViewById(R.id.no_bookmarks));
        rv.setAdapter(adapter);
        adapter.setClickListener(this);
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
        SharedPreferences pref = view.getContext().getSharedPreferences(Constants.BOOKMARK, 0);
        SharedPreferences.Editor addToBookmark = pref.edit();
        Dialog dialog = new Dialog(getContext());
        articles.forEach(a -> System.out.println(a.title));
        dialog.setContentView(R.layout.dialog);
        ImageView bookmarkIcon = dialog.findViewById(R.id.dialog_bookmark_icon);
        String articleId = articles.get(position).articleId;
        ImageView twitter = dialog.findViewById(R.id.twitter);
        ImageView img = dialog.findViewById(R.id.long_press_image);
        TextView dialogText = dialog.findViewById(R.id.dialog_text);
        dialogText.setText(articles.get(position).title);
        if(pref.contains(articleId)){
            bookmarkIcon.setImageResource(R.drawable.ic_bookmarked);
        } else {
            bookmarkIcon.setImageResource(R.drawable.ic_bookmark);
        }
        twitter.setOnClickListener(v -> {
            String shareText = "Check out this Link : "+articles.get(position).webUrl;
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
            articles.remove(position);
            adapter.notifyDataSetChanged();
            dialog.dismiss();
            onResume();
        });
        Picasso.with(getContext()).load(articles.get(position).thumbnail).into(img);
        Picasso.with(getContext()).load(Constants.TWITTER_IMAGE).into(twitter);
        dialog.show();
    }

    @Override
    public void bookmarkClick(View view, int position) {
        SharedPreferences pref = view.getContext().getSharedPreferences(Constants.BOOKMARK, 0);
        SharedPreferences.Editor addToBookmark = pref.edit();
        NewsArticle article = articles.get(position);
        String articleId = article.articleId;
        String title = article.title;
        addToBookmark.remove(articleId);
        addToBookmark.apply();
        articles.remove(position);
        Toast.makeText(view.getContext(),
                title + " removed from bookmark", Toast.LENGTH_SHORT).show();
        checkEmptyList();
        adapter.setList(articles);
    }

    @Override
    public void onResume() {
        super.onResume();
        articles = new ArrayList<>();
        SharedPreferences pref = getContext().getSharedPreferences(Constants.BOOKMARK, 0);
        Map<String,?> articleList = pref.getAll();
        for(Map.Entry<String,?> entry : articleList.entrySet()) {
            try {
                articles.add(NewsArticle.toNewsArticle(entry.getValue().toString()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (adapter != null) {
            adapter.setList(articles);
            adapter.notifyDataSetChanged();
        }
        checkEmptyList();
    }
}
