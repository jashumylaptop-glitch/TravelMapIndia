package com.example.travelmapindia;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

public class ExploreFragment extends Fragment {

    private RecyclerView recyclerView;
    private PlaceAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        recyclerView = view.findViewById(R.id.exploreRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<Place> places = loadPlacesFromAsset();
        adapter = new PlaceAdapter(places);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private List<Place> loadPlacesFromAsset() {
        List<Place> places = new ArrayList<>();
        try {
            InputStream is = requireContext().getAssets().open("places.json");
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