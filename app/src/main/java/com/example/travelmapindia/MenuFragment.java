package com.example.travelmapindia;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MenuFragment extends Fragment {

    private TextInputEditText editFrom, editTo;
    private MaterialButton btnGeneratePlan, btnAddCurrentPlan;
    private MaterialCardView planResultCard;
    private TextView txtDuration, txtDistance, txtStops, txtNoPlans, txtItineraryTitle;
    private RecyclerView rvCurrentPlans;
    private TripPlanAdapter adapter;
    private List<TripPlan> currentPlans = new ArrayList<>();
    private List<Place> allPlaces = new ArrayList<>();
    private int currentUserId;
    private TripPlan editingPlan = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        SharedPreferences prefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUserId = prefs.getInt("userId", -1);

        editFrom = view.findViewById(R.id.editFrom);
        editTo = view.findViewById(R.id.editTo);
        btnGeneratePlan = view.findViewById(R.id.btnGeneratePlan);
        btnAddCurrentPlan = view.findViewById(R.id.btnAddCurrentPlan);
        planResultCard = view.findViewById(R.id.planResultCard);
        txtDuration = view.findViewById(R.id.txtDuration);
        txtDistance = view.findViewById(R.id.txtDistance);
        txtStops = view.findViewById(R.id.txtStops);
        txtNoPlans = view.findViewById(R.id.txtNoPlans);
        txtItineraryTitle = view.findViewById(R.id.txtItineraryTitle);
        rvCurrentPlans = view.findViewById(R.id.rvCurrentPlans);

        rvCurrentPlans.setLayoutManager(new LinearLayoutManager(getContext()));
        loadPlacesAsset();
        loadPlans();

        btnGeneratePlan.setOnClickListener(v -> generateSmartPlan());

        btnAddCurrentPlan.setOnClickListener(v -> savePlan());

        return view;
    }

    private void loadPlacesAsset() {
        try {
            InputStream is = requireContext().getAssets().open("places.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Place>>() {}.getType();
            allPlaces = gson.fromJson(json, listType);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateSmartPlan() {
        String from = editFrom.getText().toString().trim();
        String to = editTo.getText().toString().trim();

        if (from.isEmpty() || to.isEmpty()) {
            Toast.makeText(getContext(), "Please enter both locations", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Try to find the coordinates of 'from' and 'to' cities in our database
        Place fromPlace = findPlaceByCity(from);
        Place toPlace = findPlaceByCity(to);

        if (fromPlace != null && toPlace != null) {
            calculateRealStats(fromPlace, toPlace);
        } else {
            // Fallback for cities not in our small JSON database
            txtDuration.setText("5 hrs 30 mins");
            txtDistance.setText("320 km");
            txtStops.setText("Nearby highway rest areas");
        }

        planResultCard.setVisibility(View.VISIBLE);
    }

    private Place findPlaceByCity(String cityName) {
        for (Place p : allPlaces) {
            if (p.getCity().equalsIgnoreCase(cityName) || p.getName().equalsIgnoreCase(cityName)) {
                return p;
            }
        }
        return null;
    }

    private void calculateRealStats(Place start, Place end) {
        double lat1 = start.getLatitude();
        double lon1 = start.getLongitude();
        double lat2 = end.getLatitude();
        double lon2 = end.getLongitude();

        // Haversine formula for distance
        double theta = lon1 - lon2;
        double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2))
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
        dist = Math.acos(dist);
        dist = Math.toDegrees(dist);
        dist = dist * 60 * 1.1515 * 1.609344; // Distance in KM
        
        // Add 30% for road winding
        int roadDist = (int) (dist * 1.3);
        txtDistance.setText(roadDist + " km");

        // Calculate time (avg 60km/h)
        int totalMinutes = (roadDist * 60) / 60;
        int hrs = totalMinutes / 60;
        int mins = totalMinutes % 60;
        txtDuration.setText(hrs + " hrs " + mins + " mins");

        // Find stops: Places that are between the start and end coordinates
        StringBuilder stopsBuilder = new StringBuilder();
        int stopCount = 0;
        for (Place p : allPlaces) {
            if (isPlaceBetween(p, start, end) && !p.getCity().equalsIgnoreCase(start.getCity()) && !p.getCity().equalsIgnoreCase(end.getCity())) {
                if (stopCount > 0) stopsBuilder.append(", ");
                stopsBuilder.append(p.getName());
                stopCount++;
                if (stopCount >= 3) break; // Limit to 3 major stops
            }
        }
        
        if (stopCount == 0) {
            txtStops.setText("Highway Rest Stops");
        } else {
            txtStops.setText(stopsBuilder.toString());
        }
    }

    private boolean isPlaceBetween(Place p, Place start, Place end) {
        double minLat = Math.min(start.getLatitude(), end.getLatitude()) - 0.5;
        double maxLat = Math.max(start.getLatitude(), end.getLatitude()) + 0.5;
        double minLon = Math.min(start.getLongitude(), end.getLongitude()) - 0.5;
        double maxLon = Math.max(start.getLongitude(), end.getLongitude()) + 0.5;

        return p.getLatitude() >= minLat && p.getLatitude() <= maxLat &&
               p.getLongitude() >= minLon && p.getLongitude() <= maxLon;
    }

    private void loadPlans() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            currentPlans = db.tripPlanDao().getPlansByUserId(currentUserId);
            requireActivity().runOnUiThread(() -> {
                if (currentPlans.isEmpty()) {
                    txtNoPlans.setVisibility(View.VISIBLE);
                    rvCurrentPlans.setVisibility(View.GONE);
                } else {
                    txtNoPlans.setVisibility(View.GONE);
                    rvCurrentPlans.setVisibility(View.VISIBLE);
                    adapter = new TripPlanAdapter(currentPlans, new TripPlanAdapter.OnPlanActionListener() {
                        @Override
                        public void onDelete(TripPlan plan) { deletePlan(plan); }
                        @Override
                        public void onEdit(TripPlan plan) { startEditing(plan); }
                        @Override
                        public void onClick(TripPlan plan) { openInGoogleMaps(plan); }
                    });
                    rvCurrentPlans.setAdapter(adapter);
                }
            });
        }).start();
    }

    private void openInGoogleMaps(TripPlan plan) {
        String uri = "http://maps.google.com/maps?saddr=" + plan.getFromLocation() + "&daddr=" + plan.getToLocation();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);
    }

    private void savePlan() {
        String from = editFrom.getText().toString().trim();
        String to = editTo.getText().toString().trim();
        String dur = txtDuration.getText().toString();
        String dist = txtDistance.getText().toString();
        String stops = txtStops.getText().toString();

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            if (editingPlan != null) {
                editingPlan.setFromLocation(from);
                editingPlan.setToLocation(to);
                editingPlan.setDuration(dur);
                editingPlan.setDistance(dist);
                editingPlan.setStops(stops);
                db.tripPlanDao().update(editingPlan);
                editingPlan = null;
            } else {
                TripPlan newPlan = new TripPlan(currentUserId, from, to, dur, dist, stops);
                db.tripPlanDao().insert(newPlan);
            }
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Plan saved!", Toast.LENGTH_SHORT).show();
                planResultCard.setVisibility(View.GONE);
                editFrom.setText("");
                editTo.setText("");
                txtItineraryTitle.setText("Create New Itinerary");
                loadPlans();
            });
        }).start();
    }

    private void deletePlan(TripPlan plan) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            db.tripPlanDao().delete(plan);
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Plan deleted", Toast.LENGTH_SHORT).show();
                loadPlans();
            });
        }).start();
    }

    private void startEditing(TripPlan plan) {
        editingPlan = plan;
        editFrom.setText(plan.getFromLocation());
        editTo.setText(plan.getToLocation());
        txtItineraryTitle.setText("Update Itinerary");
        generateSmartPlan();
    }
}