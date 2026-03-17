package com.example.travelmapindia;

public class Place {
    private String name;
    private String city;
    private String imageUrl;
    private float rating;
    private String category;

    public Place(String name, String city, String imageUrl, float rating, String category) {
        this.name = name;
        this.city = city;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.category = category;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}