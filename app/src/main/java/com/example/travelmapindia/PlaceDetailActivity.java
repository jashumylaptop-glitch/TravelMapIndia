package com.example.travelmapindia;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;

public class PlaceDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);

        Place place = (Place) getIntent().getSerializableExtra("place");

        if (place != null) {
            setupUI(place);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupUI(Place place) {
        ImageView imageView = findViewById(R.id.detailImage);
        TextView nameView = findViewById(R.id.detailName);
        TextView cityView = findViewById(R.id.detailCity);
        TextView ratingView = findViewById(R.id.detailRating);
        TextView descView = findViewById(R.id.detailDescription);
        Button btnNavigate = findViewById(R.id.btnNavigate);
        Button btnSubmit = findViewById(R.id.btnSubmitReview);
        RatingBar ratingBar = findViewById(R.id.ratingBar);
        EditText editReview = findViewById(R.id.editReview);

        nameView.setText(place.getName());
        cityView.setText(place.getCity());
        ratingView.setText(String.valueOf(place.getRating()));
        descView.setText(place.getDescription());

        int resourceId = getResources().getIdentifier(
                place.getImageUrl(), "drawable", getPackageName());

        Glide.with(this)
                .load(resourceId != 0 ? resourceId : R.drawable.ic_launcher_background)
                .into(imageView);

        btnNavigate.setOnClickListener(v -> {
            String uri = "google.navigation:q=" + place.getLatitude() + "," + place.getLongitude();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Uri mapUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + place.getLatitude() + "," + place.getLongitude());
                startActivity(new Intent(Intent.ACTION_VIEW, mapUri));
            }
        });

        btnSubmit.setOnClickListener(v -> {
            String reviewText = editReview.getText().toString().trim();
            float userRating = ratingBar.getRating();

            if (reviewText.isEmpty()) {
                Toast.makeText(this, "Please write a review", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
            int userId = prefs.getInt("userId", -1);
            String userName = prefs.getString("userName", "Anonymous");

            if (userId == -1) {
                Toast.makeText(this, "Please login to review", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                Review review = new Review(
                        userId,
                        userName,
                        place.getName(),
                        reviewText,
                        userRating,
                        System.currentTimeMillis()
                );
                AppDatabase.getInstance(this).reviewDao().insert(review);
                
                runOnUiThread(() -> {
                    Toast.makeText(this, "Review submitted successfully!", Toast.LENGTH_SHORT).show();
                    editReview.setText("");
                    ratingBar.setRating(0);
                });
            }).start();
        });
    }
}