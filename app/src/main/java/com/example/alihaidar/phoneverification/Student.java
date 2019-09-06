package com.example.alihaidar.phoneverification;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by Ahmad Alibrahim on 5/10/2018.
 */

public class Student {
    int id;
    String name;
    String driverPhone;
    LatLng latLng;
    String phNumber;
    String trackType;
    int morning;
    int afternoon;
    int fenceRaduis;
    ArrayList<Driver> myDrivers;

    public int getFenceRaduis() {
        return fenceRaduis;
    }

    public int getAfternoon() {
        return afternoon;
    }

    public int getMorning() {

        return morning;
    }

    public Student(int id, String name, String phNumber,LatLng latLng) {
        this.id=id;
        this.latLng=latLng;
        this.name = name;
        this.phNumber = phNumber;
    }

    public Student(int id, String name, String driverPhone, LatLng latLng, String phNumber,int fenceRaduis) {
        this.id=id;
        this.name = name;
        this.driverPhone = driverPhone;
        this.latLng = latLng;
        this.phNumber = phNumber;
        this.fenceRaduis=fenceRaduis;
    }
    public Student(int id, String name, String driverPhone, LatLng latLng, String phNumber, int morning,int afternoon,String trackType,int fenceRaduis) {
        this.id = id;
        this.name = name;
        this.driverPhone = driverPhone;
        this.latLng = latLng;
        this.phNumber = phNumber;
        this.morning=morning;
        this.afternoon=afternoon;
        this.trackType=trackType;
        this.fenceRaduis=fenceRaduis;
    }

    public Student(int id, String name, String driverPhone, LatLng latLng, String phNumber, int morning,int afternoon,int fenceRaduis) {
        this.id = id;
        this.name = name;
        this.driverPhone = driverPhone;
        this.latLng = latLng;
        this.morning=morning;
        this.afternoon=afternoon;
        this.phNumber = phNumber;
        this.fenceRaduis=fenceRaduis;
    }

    public Student(int id, String name, String driverPhone, LatLng latLng, String phNumber, int morning,int afternoon) {
        this.id = id;
        this.name = name;
        this.driverPhone = driverPhone;
        this.latLng = latLng;
        this.phNumber = phNumber;
        this.morning=morning;
        this.afternoon=afternoon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDriverPhone() {
        return driverPhone;
    }

    public void setDriverPhone(String driverPhone) {
        this.driverPhone = driverPhone;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getPhNumber() {
        return phNumber;
    }

    public void setPhNumber(String phNumber) {
        this.phNumber = phNumber;
    }
    public int getId() {
        return id;
    }

    public void setRadius(int radius) {
        this.fenceRaduis = radius;
    }
}
