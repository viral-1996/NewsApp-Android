package com.example.newsapp;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class NewsArticle {
    public String title;
    public String section;
    public String thumbnail;
    public String articleId;
    public ZonedDateTime publicationDate;
    public String webUrl;

    public NewsArticle(String title, String section, String thumbnail,
                       String articleId, ZonedDateTime publicationDate, String webUrl){
        this.title = title;
        this.section = section;
        this.thumbnail = thumbnail;
        this.articleId = articleId;
        this.publicationDate = publicationDate;
        this.webUrl = webUrl;
    }

    public String toMyString() {
        return "{ \"title\": \"" + title + "\", \"section\": \"" + section + "\", \"thumbnail\": \""
                + thumbnail + "\", \"articleId\": \""+articleId+"\", \"publicationDate\": \""
                + publicationDate + "\", \"webUrl\": \""+webUrl+"\" }";
    }

    public static NewsArticle toNewsArticle(String article) throws JSONException {
        JSONObject artcle = new JSONObject(article);
        String title = artcle.getString("title");
        String section = artcle.getString("section");
        String thumbnail = artcle.getString("thumbnail");
        String articleId = artcle.getString("articleId");
        ZonedDateTime publicationDate = ZonedDateTime
                .parse(artcle.getString("publicationDate"));
        String webUrl = artcle.getString("webUrl");
        return new NewsArticle(title, section, thumbnail, articleId, publicationDate, webUrl);
    }
}
