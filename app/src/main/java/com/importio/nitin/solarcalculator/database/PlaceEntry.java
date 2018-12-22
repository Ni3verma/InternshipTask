package com.importio.nitin.solarcalculator.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "place")
public class PlaceEntry {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private double lat;
    private double lng;

    @Ignore

    public PlaceEntry(String title, double lat, double lng) {
        this.title = title;
        this.lat = lat;
        this.lng = lng;
    }

    public PlaceEntry(int id, String title, double lat, double lng) {

        this.id = id;
        this.title = title;
        this.lat = lat;
        this.lng = lng;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
