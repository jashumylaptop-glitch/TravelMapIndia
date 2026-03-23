package com.example.travelmapindia;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface TripPlanDao {
    @Insert
    void insert(TripPlan plan);

    @Update
    void update(TripPlan plan);

    @Delete
    void delete(TripPlan plan);

    @Query("SELECT * FROM trip_plans WHERE userId = :userId")
    List<TripPlan> getPlansByUserId(int userId);

    @Query("SELECT COUNT(*) FROM trip_plans WHERE userId = :userId")
    int getPlanCountByUserId(int userId);
}