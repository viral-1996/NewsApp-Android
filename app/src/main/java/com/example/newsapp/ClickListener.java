package com.example.newsapp;

import android.view.View;

public interface ClickListener {
    void onClick(View view, int position);
    void onLongClick(View view,int position);
    void bookmarkClick(View view, int position);
}
