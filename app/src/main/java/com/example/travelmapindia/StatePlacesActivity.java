package com.example.travelmapindia;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class StatePlacesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PlaceAdapter adapter;
    private TextView noPlacesText;
    private String stateName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_state_places);

        stateName = getIntent().getStringExtra("stateName");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(stateName);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerView = findViewById(R.id.statePlacesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        noPlacesText = findViewById(R.id.noPlacesText);

        syncPlacesForState();
    }

    private void syncPlacesForState() {
        new Thread(() -> {
            List<Place> allPlaces = loadPlacesFromAsset();
            List<Place> filteredPlaces = new ArrayList<>();
            
            AppDatabase db = AppDatabase.getInstance(this);
            PlaceDao dao = db.placeDao();

            for (Place place : allPlaces) {
                // Filter by State Name
                if (place.getState().equalsIgnoreCase(stateName)) {
                    Place dbPlace = dao.getPlaceByName(place.getName());
                    if (dbPlace != null) {
                        place.setFavorite(dbPlace.isFavorite());
                        place.setId(dbPlace.getId());
                    }
                    filteredPlaces.add(place);
                }
            }

            new Handler(Looper.getMainLooper()).post(() -> {
                if (filteredPlaces.isEmpty()) {
                    noPlacesText.setVisibility(View.VISIBLE);
                } else {
                    noPlacesText.setVisibility(View.GONE);
                    adapter = new PlaceAdapter(filteredPlaces);
                    recyclerView.setAdapter(adapter);
                }
            });
        }).start();
    }

    private List<Place> loadPlacesFromAsset() {
        List<Place> places = new ArrayList<>();
        try {
            InputStream is = getAssets().open("places.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Place>>() {}.getType();
            places = gson.fromJson(json, listType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return places;
    }
}