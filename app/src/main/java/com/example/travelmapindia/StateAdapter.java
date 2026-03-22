package com.example.travelmapindia;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class StateAdapter extends RecyclerView.Adapter<StateAdapter.StateViewHolder> {

    private List<State> stateList;

    public StateAdapter(List<State> stateList) {
        this.stateList = stateList;
    }

    @NonNull
    @Override
    public StateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_state, parent, false);
        return new StateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StateViewHolder holder, int position) {
        State state = stateList.get(position);
        Context context = holder.itemView.getContext();

        holder.name.setText(state.getName());
        holder.count.setText(state.getPlaceCount() + " Places");

        int resourceId = context.getResources().getIdentifier(
                state.getImageUrl(), "drawable", context.getPackageName());

        Glide.with(context)
                .load(resourceId != 0 ? resourceId : R.drawable.ic_launcher_background)
                .centerCrop()
                .into(holder.image);

        // Click to open places for this state
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, StatePlacesActivity.class);
            intent.putExtra("stateName", state.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return stateList.size();
    }

    static class StateViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, count;

        public StateViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.stateImage);
            name = itemView.findViewById(R.id.stateName);
            count = itemView.findViewById(R.id.statePlaceCount);
        }
    }
}