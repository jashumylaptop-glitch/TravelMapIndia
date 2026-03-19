package com.example.travelmapindia;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface PlaceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Place place);

    @Update
    void update(Place place);

    @Delete
    void delete(Place place);

    @Query("SELECT * FROM places")
    List<Place> getAllPlaces();

    @Query("SELECT * FROM places WHERE isFavorite = 1")
    List<Place> getFavoritePlaces();
    
    @Query("SELECT * FROM places WHERE name = :name LIMIT 1")
    Place getPlaceByName(String name);
}