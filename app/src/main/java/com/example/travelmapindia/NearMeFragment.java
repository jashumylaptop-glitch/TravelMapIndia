package com.example.travelmapindia;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NearMeFragment extends Fragment {

    private RecyclerView recyclerView;
    private PlaceAdapter adapter;
    private ProgressBar progressBar;
    private FusedLocationProviderClient fusedLocationClient;
    
    private final ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (fineLocationGranted != null && fineLocationGranted) {
                    getCurrentLocation();
                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    getCurrentLocation();
                } else {
                    Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_near_me, container, false);

        recyclerView = view.findViewById(R.id.nearMeRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        progressBar = view.findViewById(R.id.nearMeLoading);
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        checkLocationPermissions();

        return view;
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                calculateDistancesAndShow(location);
            } else {
                Toast.makeText(getContext(), "Could not get location. Try opening Google Maps first.", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void calculateDistancesAndShow(Location userLocation) {
        new Thread(() -> {
            List<Place> allPlaces = loadPlacesFromAsset();
            AppDatabase db = AppDatabase.getInstance(requireContext());
            PlaceDao dao = db.placeDao();

            for (Place place : allPlaces) {
                // Calculate distance using Android's distanceBetween helper
                float[] results = new float[1];
                Location.distanceBetween(userLocation.getLatitude(), userLocation.getLongitude(),
                        place.getLatitude(), place.getLongitude(), results);
                
                // We store the distance temporarily in a custom way or just sort.
                // For a mini project, let's just sort the list based on this calculation.
                place.setDistance(results[0]); // We need to add this field to Place.java

                Place dbPlace = dao.getPlaceByName(place.getName());
                if (dbPlace != null) {
                    place.setFavorite(dbPlace.isFavorite());
                    place.setId(dbPlace.getId());
                }
            }

            // Sort by distance (closest first)
            Collections.sort(allPlaces, (p1, p2) -> Float.compare(p1.getDistance(), p2.getDistance()));

            new Handler(Looper.getMainLooper()).post(() -> {
                adapter = new PlaceAdapter(allPlaces);
                recyclerView.setAdapter(adapter);
                progressBar.setVisibility(View.GONE);
            });
        }).start();
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