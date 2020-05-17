package com.example.newsapp.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.newsapp.ClickListener;
import com.example.newsapp.Constants;
import com.example.newsapp.NewsArticle;
import com.example.newsapp.R;
import com.squareup.picasso.Picasso;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class HomePageAdapter extends RecyclerView.Adapter<HomePageAdapter.ArticleViewHolder>{
    private static final String TAG = "Adapter";
    private List<NewsArticle> articles;
    private Context context;
    private ClickListener clickListener;

    public HomePageAdapter(List<NewsArticle> articles, Context context){
        this.articles = articles;
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.card_recycler_view, viewGroup, false);
        final ArticleViewHolder articleViewHolder = new ArticleViewHolder(v);
        return articleViewHolder;
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public void onBindViewHolder(ArticleViewHolder articleViewHolder, int i) {
        SharedPreferences pref = context.getSharedPreferences(Constants.BOOKMARK, 0);
        NewsArticle currentArticle = articles.get(i);
        articleViewHolder.title.setText(currentArticle.title);
        articleViewHolder.description.setText(currentArticle.section);
        articleViewHolder.timeDifference.setText(getDifference(currentArticle.publicationDate));
        Picasso.with(context).load(currentArticle.thumbnail).into(articleViewHolder.newsCardImage);
        articleViewHolder.bookmarkIcon.setImageResource(
                pref.getString(articles.get(i).articleId, null) == null ?
                        R.drawable.ic_bookmark : R.drawable.ic_bookmarked);
    }

    private String getDifference(final ZonedDateTime articleTimestamp) {
        ZoneId zoneId = ZoneId.of("GMT");
        ZonedDateTime articleTimeZoneAtPT = articleTimestamp.withZoneSameLocal(zoneId);
        ZonedDateTime now = ZonedDateTime.now((zoneId));
        long timeDifference = zonedDateTimeDifference(articleTimeZoneAtPT, now, ChronoUnit.SECONDS);
        if(timeDifference / Constants.TO_DAYS > 0) {
            return timeDifference/ Constants.TO_DAYS +"d ago";
        } else if(timeDifference / Constants.TO_HOURS > 0){
            return timeDifference / Constants.TO_HOURS +"h ago";
        } else if(timeDifference / Constants.TO_MINUTES > 0){
            return timeDifference / Constants.TO_MINUTES +"m ago";
        } else {
            return timeDifference+"s ago";
        }
    }

    private static long zonedDateTimeDifference(final ZonedDateTime d1, final ZonedDateTime d2,
                                                final ChronoUnit unit){
        return unit.between(d1, d2);
    }

    public class ArticleViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        CardView cv;
        TextView title;
        TextView description, timeDifference;
        ImageView newsCardImage;
        ImageView bookmarkIcon;
        String articleId;

        ArticleViewHolder(View itemView) {
            super(itemView);
            SharedPreferences pref = context.getSharedPreferences(Constants.BOOKMARK, 0);
            SharedPreferences.Editor addToBookmark = pref.edit();
            cv = itemView.findViewById(R.id.recycler_card);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            newsCardImage = itemView.findViewById(R.id.newscardImage);
            timeDifference = itemView.findViewById(R.id.time_difference);
            bookmarkIcon = itemView.findViewById(R.id.bookmark_card_recycler_view);
            bookmarkIcon.setOnClickListener(v -> {
                NewsArticle article = articles.get(getAdapterPosition());
                articleId = article.articleId;
                String title = article.title;
                if(pref.getString(articleId,null) == null){
                    bookmarkIcon.setImageResource(R.drawable.ic_bookmarked);
                    addToBookmark.putString(articleId, article.toMyString());
                    Toast.makeText(itemView.getContext(),
                            title+" added to Bookmarks",
                            Toast.LENGTH_SHORT).show();
                } else {
                    bookmarkIcon.setImageResource(R.drawable.ic_bookmark);
                    addToBookmark.remove(articleId);
                    Toast.makeText(itemView.getContext(),
                            title+" removed from Bookmarks",
                            Toast.LENGTH_SHORT).show();
                }
                addToBookmark.apply();
            });
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View view) {
            clickListener.onLongClick(view, getAdapterPosition());
            return true;
        }

        @Override
        public void onClick(View view) {
            clickListener.onClick(view, getAdapterPosition());
        }
    }
}