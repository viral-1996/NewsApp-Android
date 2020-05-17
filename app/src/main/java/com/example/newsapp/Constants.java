package com.example.newsapp;

public final class Constants {
    private Constants(){

    }

    public static final String WEATHER_API_KEY = "35603fe37fa46ee8c4d0f36e1c4c6a6e";
    public static final String AUTOSUGGEST_KEY = "d7b4cfaca825421790b6e0c208b11173";
    public static final String BACKEND_NEWS_ENDPOINT =
            "http://newsapp-android-backend.us-east-1.elasticbeanstalk.com/section/";
    public static final String TRENDING_CHART_ENDPOINT =
            "http://newsapp-android-backend.us-east-1.elasticbeanstalk.com/trending?keyword=";
    public static final String SEARCH_ENDPOINT =
            "http://newsapp-android-backend.us-east-1.elasticbeanstalk.com/search?q=";
    public static final String EXPANDED_ARTICLE_ENDPOINT =
            "http://newsapp-android-backend.us-east-1.elasticbeanstalk.com/post?id=";
    public static final String AUTOSUGGEST_API =
            "https://kevin-daftary.cognitiveservices.azure.com/bing/v7.0/suggestions?q=";
    public static final String ZONEID_LA = "America/Los_Angeles", ZONEID_GMT = "GMT";
    public static final String BOOKMARK = "bookmarks", CHARTCOLOR = "#6200EE";
    public static final String TWITTER_IMAGE =
            "https://csci571.com/hw/hw9/images/android/bluetwitter.png";
    public static final String DEFAULT_IMAGE =
            "https://assets.guim.co.uk/images/eada8aa27c12fe2d5afa3a89d3fbae0d/fallback-logo.png";
    public static final int TO_DAYS = 86400, TO_HOURS = 3600, TO_MINUTES = 60;
    public static final String TWITTER_INTENT =
            "https://twitter.com/intent/tweet?text=__shareText__&hashtags=CSCI571NewsApp";
    public static final String WEATHER_API = "https://api.openweathermap.org/data/2.5/weather?q=" +
            "__city__&units=metric&appid="+ Constants.WEATHER_API_KEY;
    public static final String SHARE_TWITTER_TEXT = "Check out this Link : __link__";
    public static final String DETAILED_ARTICLE_ID = "id", DETAILED_ARTICLE_TITLE = "title";
    public static final String LINK_TO_MAIN_ARTICLE = "<a href=__articleUrl__ " +
            "style=\"text-decoration:none, text-color:#aaaaaa\"> View Full Article</a>";
}
