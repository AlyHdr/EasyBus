package com.example.alihaidar.phoneverification;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Ahmad Alibrahim on 5/22/2018.
 */

public class Driver {
    int id;
    private String name;
    private String phoneNb;
    private LatLng latLng;
    private String address;
    private String trackType;
    private int rate;

    public LatLng getSchoolLatLng() {
        return schoolLatLng;
    }

    public void setSchoolLatLng(LatLng schoolLatLng) {

        this.schoolLatLng = schoolLatLng;
    }

    private LatLng schoolLatLng;

    public Driver(int id, String name, String phoneNb, LatLng latLng,String trackType) {
        this.id = id;
        this.name = name;
        this.phoneNb = phoneNb;
        this.latLng=latLng;
        this.trackType=trackType;
    }

    public Driver(int id, String name, String phoneNb, LatLng latLng) {
        this.id = id;
        this.name = name;
        this.phoneNb = phoneNb;
        this.latLng = latLng;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNb() {
        return phoneNb;
    }

    public LatLng getLatLng()
    {
        return latLng;
    }

    public String getAddress() {
        return address;
    }

    public int getRate() {
        return rate;
    }

    public String getTrackType() {
        return trackType;
    }

    public void setTrackType(String trackType) {
        this.trackType = trackType;
    }
}
