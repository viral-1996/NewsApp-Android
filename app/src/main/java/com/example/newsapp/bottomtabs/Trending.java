package com.example.newsapp.bottomtabs;


import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.newsapp.Constants;
import com.example.newsapp.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class Trending extends Fragment {
    private static final String TAG = "Trending";
    private JSONArray trendingData;
    private boolean dataReceived;
    private String keyword = "CoronaVirus";
    public Trending() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)  {
        View view =  inflater.inflate(R.layout.fragment_trending, container, false);
        dataReceived = false;
        getTrendingData(keyword);
        final Handler handler = new Handler();
        Runnable proceedsRunnable = new Runnable() {
            @Override
            public void run() {
                if (dataReceived) {
                    generateChart(view);
                } else {
                    handler.postDelayed(this, 100);
                }
            }
        };
        handler.post(proceedsRunnable);
        EditText keywordInput = view.findViewById(R.id.trending_input);
        keywordInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEND ||
                        event.getAction() == KeyEvent.ACTION_DOWN){
                    keyword = v.getText().toString();
                    dataReceived = false;
                    getTrendingData(keyword);
                    final Handler handler = new Handler();
                    Runnable proceedsRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if (dataReceived) {
                                generateChart(view);
                            } else {
                                handler.postDelayed(this, 100);
                            }
                        }
                    };
                    handler.post(proceedsRunnable);
                    return true;
                }
                return false;
            }
        });
        return view;
    }

    private void generateChart(final View view) {
        LineChart trendingChart = view.findViewById(R.id.trending_chart);
        List<Entry> data = new ArrayList<Entry>();
        for(int i=0; i<trendingData.length(); i++){
            try {
                int count = trendingData.getJSONObject(i).
                        getJSONArray("value").getInt(0);
                data.add(new Entry((float) i, (float) count));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        trendingChart = styleTrendingChart(trendingChart, data);
        trendingChart.setVisibility(View.VISIBLE);
        trendingChart.invalidate();
    }

    private LineChart styleTrendingChart(final LineChart trendingChart,final List<Entry> data){
        LineDataSet chartData = new LineDataSet(data, "Trending chart for "+keyword);
        chartData.setColor(Color.parseColor(Constants.CHARTCOLOR));
        chartData.setDrawCircleHole(false);
        chartData.setCircleColor(Color.parseColor(Constants.CHARTCOLOR));
        chartData.setValueTextColor(Color.parseColor(Constants.CHARTCOLOR));
        LineData chart = new LineData(chartData);
        trendingChart.setData(chart);
        trendingChart.setDrawGridBackground(false);
        trendingChart.getAxisLeft().setDrawGridLines(false);
        trendingChart.getAxisRight().setDrawGridLines(false);
        trendingChart.getXAxis().setDrawGridLines(false);
        Legend legend = trendingChart.getLegend();
        legend.setTextColor(Color.BLACK);
        legend.setTextSize(15);
        return trendingChart;
    }

    private void getTrendingData(final String keyword) {
        String url = Constants.TRENDING_CHART_ENDPOINT + keyword;
        RequestQueue que = Volley.newRequestQueue(getContext());
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url,
                null, response -> {
                    try {
                        trendingData = response.getJSONObject("default").
                                getJSONArray("timelineData");
                        dataReceived = true;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> Log.d(TAG, error.toString()));
        que.add(jsonRequest);
    }
}
