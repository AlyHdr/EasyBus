package com.example.alihaidar.phoneverification;

import com.google.android.gms.maps.model.LatLng;

import java.util.Arrays;

/**
 * Created by Ahmad Alibrahim on 5/26/2018.
 */

public class School {
    LatLng latLng;
    String name;
    String [] offDays;

    public School(String name,LatLng latLng) {
        this.latLng = latLng;
        this.name = name;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public String getName() {
        return name;
    }
}
