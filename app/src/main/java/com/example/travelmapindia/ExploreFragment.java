package com.example.travelmapindia;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
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
    private List<Place> allPlacesList = new ArrayList<>();
    private List<Place> displayList = new ArrayList<>();
    private ChipGroup categoryChipGroup;
    private TextInputEditText editSearch;
    private ImageButton btnMenu, btnPlanTrip;
    private TextView badgeTripCount;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        recyclerView = view.findViewById(R.id.exploreRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        categoryChipGroup = view.findViewById(R.id.categoryChipGroup);
        editSearch = view.findViewById(R.id.editSearch);
        btnMenu = view.findViewById(R.id.btn_menu);
        btnPlanTrip = view.findViewById(R.id.btn_plan_trip);
        badgeTripCount = view.findViewById(R.id.badge_trip_count);

        btnMenu.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openMenu();
            }
        });

        btnPlanTrip.setOnClickListener(v -> showCurrentPlansDialog());

        setupSearch();
        setupFilters();
        syncPlacesWithDatabase();
        updateTripBadge();

        return view;
    }

    private void showCurrentPlansDialog() {
        SharedPreferences prefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1);

        if (userId == -1) return;

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            List<TripPlan> plans = db.tripPlanDao().getPlansByUserId(userId);

            requireActivity().runOnUiThread(() -> {
                if (plans.isEmpty()) {
                    Toast.makeText(getContext(), "No active plans. Create one in the Trip Planner!", Toast.LENGTH_SHORT).show();
                    navigateToTripPlan();
                    return;
                }

                View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_current_plans, null);
                RecyclerView rv = dialogView.findViewById(R.id.rvPopupPlans);
                rv.setLayoutManager(new LinearLayoutManager(getContext()));
                
                final AlertDialog[] dialogHolder = new AlertDialog[1];
                
                TripPlanAdapter popupAdapter = new TripPlanAdapter(plans, new TripPlanAdapter.OnPlanActionListener() {
                    @Override
                    public void onClick(TripPlan plan) {
                        if (dialogHolder[0] != null) dialogHolder[0].dismiss();
                        navigateToTripPlan();
                    }
                });
                popupAdapter.setMini(true);
                rv.setAdapter(popupAdapter);

                dialogHolder[0] = new MaterialAlertDialogBuilder(requireContext(), R.style.RoundedAlertDialog)
                        .setView(dialogView)
                        .create();

                // Close button logic
                dialogView.findViewById(R.id.btnCloseDialog).setOnClickListener(v -> {
                    dialogHolder[0].dismiss();
                });

                dialogView.findViewById(R.id.btnGoToPlanner).setOnClickListener(v -> {
                    dialogHolder[0].dismiss();
                    navigateToTripPlan();
                });

                dialogHolder[0].show();
            });
        }).start();
    }

    private void navigateToTripPlan() {
        if (getActivity() instanceof MainActivity) {
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.navigation_trip_plan);
            }
        }
    }

    private void updateTripBadge() {
        SharedPreferences prefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1);

        if (userId != -1) {
            new Thread(() -> {
                int count = AppDatabase.getInstance(requireContext()).tripPlanDao().getPlanCountByUserId(userId);
                requireActivity().runOnUiThread(() -> {
                    if (count > 0) {
                        badgeTripCount.setText(String.valueOf(count));
                        badgeTripCount.setVisibility(View.VISIBLE);
                    } else {
                        badgeTripCount.setVisibility(View.GONE);
                    }
                });
            }).start();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTripBadge();
    }

    private void setupSearch() {
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> filterList(s.toString(), getSelectedCategory());
                searchHandler.postDelayed(searchRunnable, 150);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilters() {
        categoryChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            filterList(editSearch.getText().toString(), getSelectedCategory());
        });
    }

    private String getSelectedCategory() {
        int checkedId = categoryChipGroup.getCheckedChipId();
        if (checkedId == R.id.chipHeritage) return "Heritage";
        if (checkedId == R.id.chipNature) return "Nature";
        if (checkedId == R.id.chipSpiritual) return "Spiritual";
        if (checkedId == R.id.chipBeach) return "Beach";
        if (checkedId == R.id.chipAdventure) return "Adventure";
        return "All";
    }

    private void filterList(String query, String category) {
        displayList.clear();
        for (Place place : allPlacesList) {
            boolean matchesQuery = place.getName().toLowerCase().contains(query.toLowerCase()) ||
                    place.getCity().toLowerCase().contains(query.toLowerCase());
            boolean matchesCategory = category.equals("All") || place.getCategory().equalsIgnoreCase(category);

            if (matchesQuery && matchesCategory) {
                displayList.add(place);
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void syncPlacesWithDatabase() {
        new Thread(() -> {
            allPlacesList = loadPlacesFromAsset();
            AppDatabase db = AppDatabase.getInstance(requireContext());
            PlaceDao dao = db.placeDao();

            for (Place jsonPlace : allPlacesList) {
                Place dbPlace = dao.getPlaceByName(jsonPlace.getName());
                if (dbPlace != null) {
                    jsonPlace.setFavorite(dbPlace.isFavorite());
                    jsonPlace.setId(dbPlace.getId());
                    
                    dbPlace.setImageUrl(jsonPlace.getImageUrl());
                    dbPlace.setLatitude(jsonPlace.getLatitude());
                    dbPlace.setLongitude(jsonPlace.getLongitude());
                    dbPlace.setCategory(jsonPlace.getCategory());
                    dao.update(dbPlace);
                } else {
                    dao.insert(jsonPlace);
                }
            }

            displayList.clear();
            displayList.addAll(allPlacesList);

            new Handler(Looper.getMainLooper()).post(() -> {
                adapter = new PlaceAdapter(new ArrayList<>(displayList));
                recyclerView.setAdapter(adapter);
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