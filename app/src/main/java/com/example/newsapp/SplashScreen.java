package com.example.newsapp;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import gr.net.maroulis.library.EasySplashScreen;

public class SplashScreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EasySplashScreen config = new EasySplashScreen(SplashScreen.this)
                .withTargetActivity(MainActivity.class).withSplashTimeOut(2000)
                .withBackgroundColor(Color.parseColor("#ffffff"))
                .withLogo(R.drawable.ic_headline);
        View view = config.create();
        setContentView(view);
    }
}
