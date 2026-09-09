package com.animalloo.data.model;

public class Hospital {

    private final String id;
    private final String name;
    private final String address;
    private final String phone;
    private final String hours;
    private final double latitude;
    private final double longitude;
    private final double distanceKm;
    private final boolean openNow;
    private final String description;

    public Hospital(String id, String name, String address, String phone, String hours,
                    double latitude, double longitude, double distanceKm,
                    boolean openNow, String description) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.hours = hours;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distanceKm = distanceKm;
        this.openNow = openNow;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getHours() {
        return hours;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public boolean isOpenNow() {
        return openNow;
    }

    public String getDescription() {
        return description;
    }
}
