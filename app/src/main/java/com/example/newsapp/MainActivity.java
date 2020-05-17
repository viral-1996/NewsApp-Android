package com.example.newsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.appcompat.widget.SearchView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.newsapp.bottomtabs.Bookmarks;
import com.example.newsapp.bottomtabs.Headlines;
import com.example.newsapp.bottomtabs.HomePage;
import com.example.newsapp.bottomtabs.Trending;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements LocationListener {
    public static final String TAG = "MainActivity";
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 1, minTime = 0,
            minDistance = 5, maxResults = 1;
    private LocationManager locationManager;
    private String city, stateName;
    private JSONArray suggestionsData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkLocationPermission();
        Toolbar myToolbar = findViewById(R.id.main_activity_toolbar);
        setSupportActionBar(myToolbar);
        BottomNavigationView bottomNavigationView =  findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment fragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    fragment = new HomePage();
                    break;
                case R.id.navigation_headline:
                    fragment = new Headlines();
                    break;
                case R.id.navigation_trending:
                    fragment = new Trending();
                    break;
                case R.id.navigation_bookmark:
                    fragment = new Bookmarks();
                    break;
            }
            if (fragment != null) {
                openFragment(fragment);
            }
            return true;
        });
    }

    public void openFragment(Fragment fragment) {
        Bundle bundle = new Bundle();
        bundle.putString("city", city);
        bundle.putString("state", stateName);
        fragment.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, minTime, minDistance, this);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);
        SearchManager searchManager = (SearchManager)
                getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchMenuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        final SearchView.SearchAutoComplete suggestions =
                searchView.findViewById(R.id.search_src_text);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        suggestions.setOnItemClickListener((parent, view, position, id) -> {
            String queryString = (String)parent.getItemAtPosition(position);
            searchView.setQuery(queryString, false);
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent searchIntent = new Intent(searchView.getContext(), SearchResults.class);
                searchIntent.putExtra("query", query);
                startActivity(searchIntent);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.length() < 3)
                    return false;
                // TODO - test & add during final demo
                getSuggestionList(newText, suggestions);
                return true;
            }
        });
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d(TAG, requestCode + Arrays.toString(permissions));
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    if (locationManager != null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER, minTime, minDistance, this);
                    }
                }
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, maxResults);
        } catch (IOException e) {
            e.printStackTrace();
        }
        city = addresses.get(0).getLocality();
        stateName = addresses.get(0).getAdminArea();
        openFragment(new HomePage());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
//        Log.d(TAG, "enabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
//        Log.d(TAG, "disabled");
    }

    private void getSuggestionList(String query, SearchView.SearchAutoComplete suggestions) {
        String url = Constants.AUTOSUGGEST_API + query;
        RequestQueue que = Volley.newRequestQueue(getBaseContext());
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url,
            null, response -> {
                try {
                    String[] suggested = new String[5];
                    suggestionsData = response.getJSONArray("suggestionGroups")
                            .getJSONObject(0).getJSONArray("searchSuggestions");
                    for(int i=0; i < Math.min(suggestionsData.length(), 5);i++){
                        try {
                            suggested[i] = suggestionsData.getJSONObject(i)
                                    .getString("displayText");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    ArrayAdapter<String> newsAdapter = new ArrayAdapter<>(MainActivity.this,
                            android.R.layout.simple_dropdown_item_1line, suggested);
                    suggestions.setAdapter(newsAdapter);
                    suggestions.showDropDown();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> {
                Log.e(TAG, error.toString());
            } ) {
            @Override
            public Map<String, String> getHeaders() {
                Map <String, String> apiKey = new HashMap<>();
                apiKey.put("Ocp-Apim-Subscription-Key", Constants.AUTOSUGGEST_KEY);
                return apiKey;
            }
        };
        que.add(jsonRequest);
    }
}
