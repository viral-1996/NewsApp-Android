package com.example.newsapp.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
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
import com.example.newsapp.bottomtabs.Bookmarks;
import com.squareup.picasso.Picasso;

import java.text.DateFormatSymbols;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookmarkPageAdapter extends RecyclerView.Adapter<BookmarkPageAdapter.ArticleViewHolder> {
    private List<NewsArticle> articles;
    private Context context;
    private ClickListener clickListener;
    private TextView no_bookmark;

    public BookmarkPageAdapter(List<NewsArticle> articles, Context context, TextView textView){
        this.articles = articles;
        this.context = context;
        this.no_bookmark = textView;
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public void setList(List<NewsArticle> list) {
        this.articles = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.bookmark_card_view, viewGroup, false);
        ArticleViewHolder avh = new ArticleViewHolder(v);
        return avh;
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public void onBindViewHolder(final ArticleViewHolder articleViewHolder, int i) {
        NewsArticle currentArticle = articles.get(i);
        articleViewHolder.title.setText(currentArticle.title);
        articleViewHolder.section.setText(currentArticle.section);
        articleViewHolder.articleDate.setText(getTodayDate(currentArticle.publicationDate));
        Picasso.with(context).load(currentArticle.thumbnail).into(articleViewHolder.newsCardImage);
        articleViewHolder.bookmarkIcon.setImageResource(R.drawable.ic_bookmarked);
    }

    private String getTodayDate(ZonedDateTime publicationDate) {
        ZoneId pdt = ZoneId.of(Constants.ZONEID_LA);
        publicationDate = publicationDate.withZoneSameInstant(pdt);
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();
        LocalDateTime dte =  publicationDate.toLocalDateTime();
        return dte.getDayOfMonth()+" "+ months[dte.getMonthValue()-1].substring(0, 3);
    }

    public class ArticleViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        CardView cv;
        TextView title, section, articleDate;
        ImageView newsCardImage;
        ImageView bookmarkIcon;

        ArticleViewHolder(View itemView) {
            super(itemView);
            cv = itemView.findViewById(R.id.bookmark_card);
            title = itemView.findViewById(R.id.bookmark_card_title);
            articleDate = itemView.findViewById(R.id.bookmark_card_date);
            section = itemView.findViewById(R.id.bookmark_card_section);
            newsCardImage = itemView.findViewById(R.id.bookmark_card_image);
            bookmarkIcon = itemView.findViewById(R.id.bookmark_card_bookmark_image);
            bookmarkIcon.setOnClickListener(this);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.bookmark_card_bookmark_image) {
                clickListener.bookmarkClick(view, getAdapterPosition());
                return;
            }
            clickListener.onClick(view, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            clickListener.onLongClick(view, getAdapterPosition());
            return true;
        }
    }
}