package com.example.travelmapindia;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "trip_plans")
public class TripPlan {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int userId;
    private String fromLocation;
    private String toLocation;
    private String duration;
    private String distance;
    private String stops;

    public TripPlan(int userId, String fromLocation, String toLocation, String duration, String distance, String stops) {
        this.userId = userId;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.duration = duration;
        this.distance = distance;
        this.stops = stops;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getFromLocation() { return fromLocation; }
    public void setFromLocation(String fromLocation) { this.fromLocation = fromLocation; }
    public String getToLocation() { return toLocation; }
    public void setToLocation(String toLocation) { this.toLocation = toLocation; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public String getDistance() { return distance; }
    public void setDistance(String distance) { this.distance = distance; }
    public String getStops() { return stops; }
    public void setStops(String stops) { this.stops = stops; }
}