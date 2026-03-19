package com.example.travelmapindia;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reviews")
public class Review {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int userId;
    private String userName;
    private String placeName;
    private String reviewText;
    private float rating;
    private long timestamp;

    public Review(int userId, String userName, String placeName, String reviewText, float rating, long timestamp) {
        this.userId = userId;
        this.userName = userName;
        this.placeName = placeName;
        this.reviewText = reviewText;
        this.rating = rating;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getPlaceName() { return placeName; }
    public void setPlaceName(String placeName) { this.placeName = placeName; }
    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}