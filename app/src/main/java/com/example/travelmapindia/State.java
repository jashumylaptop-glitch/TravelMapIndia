package com.example.travelmapindia;

public class State {
    private String name;
    private String imageUrl;
    private int placeCount;

    public State(String name, String imageUrl, int placeCount) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.placeCount = placeCount;
    }

    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
    public int getPlaceCount() { return placeCount; }
}