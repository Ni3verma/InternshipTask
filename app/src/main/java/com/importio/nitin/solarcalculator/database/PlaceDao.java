package com.importio.nitin.solarcalculator.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface PlaceDao {
    @Query("select * from place")
    List<PlaceEntry> getAllPinnedPlaces();

    @Insert
    void addPlace(PlaceEntry place);

    @Query("delete from place where id=:id")
    void deletePlace(int id);

    @Query("select * from place where title=:title")
    PlaceEntry getPlaceByTitle(String title);
}
