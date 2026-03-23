package com.example.travelmapindia;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TripPlanAdapter extends RecyclerView.Adapter<TripPlanAdapter.ViewHolder> {

    private List<TripPlan> plans;
    private OnPlanActionListener listener;
    private boolean isMini = false;

    public interface OnPlanActionListener {
        default void onDelete(TripPlan plan) {}
        default void onEdit(TripPlan plan) {}
        default void onClick(TripPlan plan) {}
    }

    public TripPlanAdapter(List<TripPlan> plans, OnPlanActionListener listener) {
        this.plans = plans;
        this.listener = listener;
    }

    public void setMini(boolean mini) {
        isMini = mini;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip_plan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int viewType) {
        TripPlan plan = plans.get(holder.getAdapterPosition());
        holder.txtRoute.setText(plan.getFromLocation() + " to " + plan.getToLocation());
        holder.txtDetails.setText(plan.getDuration() + " • " + plan.getDistance());
        holder.txtStopsList.setText("Stops: " + plan.getStops());

        if (isMini) {
            holder.btnDelete.setVisibility(View.GONE);
            holder.btnEdit.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(v -> listener.onClick(plan));
        } else {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> listener.onDelete(plan));
            holder.btnEdit.setOnClickListener(v -> listener.onEdit(plan));
        }
    }

    @Override
    public int getItemCount() {
        return plans.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtRoute, txtDetails, txtStopsList, btnEdit;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtRoute = itemView.findViewById(R.id.txtRoute);
            txtDetails = itemView.findViewById(R.id.txtDetails);
            txtStopsList = itemView.findViewById(R.id.txtStopsList);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}