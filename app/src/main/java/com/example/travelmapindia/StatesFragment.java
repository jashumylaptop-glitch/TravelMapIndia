package com.example.travelmapindia;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class StatesFragment extends Fragment {

    private RecyclerView recyclerView;
    private StateAdapter adapter;
    private TextInputEditText searchEdit;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_states, container, false);

        recyclerView = view.findViewById(R.id.statesRecyclerView);
        searchEdit = view.findViewById(R.id.editStateSearch);
        
        // Using a GridLayout with 2 columns
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        List<State> states = loadStatesFromAsset();
        adapter = new StateAdapter(states);
        recyclerView.setAdapter(adapter);

        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private List<State> loadStatesFromAsset() {
        List<State> states = new ArrayList<>();
        try {
            InputStream is = requireContext().getAssets().open("states.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            Gson gson = new Gson();
            Type listType = new TypeToken<List<State>>() {}.getType();
            states = gson.fromJson(json, listType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return states;
    }
}