package com.example.travelmapindia;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Place.class, User.class, Review.class}, version = 5)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract PlaceDao placeDao();
    public abstract UserDao userDao();
    public abstract ReviewDao reviewDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "travel_map_db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}