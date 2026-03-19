package com.example.travelmapindia;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
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

        holder.btnFavorite.setImageResource(place.isFavorite() ? R.drawable.ic_heart : R.drawable.ic_heart_border);

        String url = place.getImageUrl();
        Object imageSource = url;
        
        if (url != null && !url.isEmpty()) {
            if (!url.startsWith("http")) {
                int resourceId = context.getResources().getIdentifier(
                        url, "drawable", context.getPackageName());
                if (resourceId != 0) {
                    imageSource = resourceId;
                }
            }
        }

        // Show progress bar before loading
        holder.progressBar.setVisibility(View.VISIBLE);

        Glide.with(context)
                .load(imageSource)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .error(R.drawable.ic_launcher_foreground)
                .into(holder.image);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PlaceDetailActivity.class);
            intent.putExtra("place", place);
            context.startActivity(intent);
        });

        holder.btnFavorite.setOnClickListener(v -> {
            boolean newFavoriteStatus = !place.isFavorite();
            place.setFavorite(newFavoriteStatus);
            holder.btnFavorite.setImageResource(newFavoriteStatus ? R.drawable.ic_heart : R.drawable.ic_heart_border);

            new Thread(() -> {
                AppDatabase.getInstance(context).placeDao().insert(place);
            }).start();
        });
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    static class PlaceViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        ImageButton btnFavorite;
        ProgressBar progressBar;
        TextView name, city, rating, category;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.placeImage);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            progressBar = itemView.findViewById(R.id.imageProgressBar);
            name = itemView.findViewById(R.id.placeName);
            city = itemView.findViewById(R.id.placeCity);
            rating = itemView.findViewById(R.id.placeRating);
            category = itemView.findViewById(R.id.placeCategory);
        }
    }
}