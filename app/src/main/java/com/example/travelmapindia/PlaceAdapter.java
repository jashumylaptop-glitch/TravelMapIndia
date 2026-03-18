package com.example.travelmapindia;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {

    private List<Place> placeList;

    public PlaceAdapter(List<Place> placeList) {
        this.placeList = placeList;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Place place = placeList.get(position);
        Context context = holder.itemView.getContext();
        
        holder.name.setText(place.getName());
        holder.city.setText(place.getCity());
        holder.rating.setText(String.valueOf(place.getRating()));
        holder.category.setText(place.getCategory());

        String url = place.getImageUrl();
        Object imageSource = url;
        
        // Null check and local resource check
        if (url != null && !url.isEmpty()) {
            if (!url.startsWith("http")) {
                int resourceId = context.getResources().getIdentifier(
                        url, "drawable", context.getPackageName());
                if (resourceId != 0) {
                    imageSource = resourceId;
                }
            }
        }

        Glide.with(context)
                .load(imageSource)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_foreground)
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    static class PlaceViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, city, rating, category;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.placeImage);
            name = itemView.findViewById(R.id.placeName);
            city = itemView.findViewById(R.id.placeCity);
            rating = itemView.findViewById(R.id.placeRating);
            category = itemView.findViewById(R.id.placeCategory);
        }
    }
}