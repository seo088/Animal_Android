package com.animalloo.data.model;

public class Facility {

    private final String id;
    private final String name;
    private final FacilityCategory category;
    private final String address;
    private final String phone;
    private final String hours;
    private final double latitude;
    private final double longitude;
    private final double distanceKm;
    private final String imageUrl;
    private final String description;
    private final boolean petFriendly;
    private final String allowedPetType;
    private final String sizeLimit;
    private final boolean indoorAllowed;
    private final boolean carrierRequired;
    private final boolean leashRequired;

    public Facility(String id, String name, FacilityCategory category, String address,
                    String phone, String hours, double latitude, double longitude,
                    double distanceKm, String imageUrl, String description,
                    boolean petFriendly, String allowedPetType, String sizeLimit,
                    boolean indoorAllowed, boolean carrierRequired, boolean leashRequired) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.address = address;
        this.phone = phone;
        this.hours = hours;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distanceKm = distanceKm;
        this.imageUrl = imageUrl;
        this.description = description;
        this.petFriendly = petFriendly;
        this.allowedPetType = allowedPetType;
        this.sizeLimit = sizeLimit;
        this.indoorAllowed = indoorAllowed;
        this.carrierRequired = carrierRequired;
        this.leashRequired = leashRequired;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public FacilityCategory getCategory() {
        return category;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPetFriendly() {
        return petFriendly;
    }

    public String getAllowedPetType() {
        return allowedPetType;
    }

    public String getSizeLimit() {
        return sizeLimit;
    }

    public boolean isIndoorAllowed() {
        return indoorAllowed;
    }

    public boolean isCarrierRequired() {
        return carrierRequired;
    }

    public boolean isLeashRequired() {
        return leashRequired;
    }
}
