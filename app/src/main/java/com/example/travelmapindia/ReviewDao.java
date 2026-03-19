package com.example.travelmapindia;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ReviewDao {
    @Insert
    void insert(Review review);

    @Query("SELECT * FROM reviews WHERE placeName = :placeName ORDER BY timestamp DESC")
    List<Review> getReviewsForPlace(String placeName);

    @Query("SELECT * FROM reviews WHERE userId = :userId ORDER BY timestamp DESC")
    List<Review> getReviewsByUser(int userId);
}