package com.example.travelmapindia;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "places")
public class Place implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String city;
    private String state;
    private String imageUrl;
    private float rating;
    private String category;
    private boolean isFavorite;
    private double latitude;
    private double longitude;
    private String description;
    
    @Ignore
    private float distance; // Used for "Near Me" tab calculation

    public Place() {
    }

    public Place(String name, String city, String state, String imageUrl, float rating, String category, double latitude, double longitude, String description) {
        this.name = name;
        this.city = city;
        this.state = state;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.category = category;
        this.isFavorite = false;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public float getDistance() { return distance; }
    public void setDistance(float distance) { this.distance = distance; }
}