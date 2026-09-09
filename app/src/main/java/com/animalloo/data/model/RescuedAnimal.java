package com.animalloo.data.model;

public class RescuedAnimal {

    private final String id;
    private final String name;
    private final String species;
    private final String breed;
    private final String gender;
    private final String region;
    private final String rescuedDate;
    private final String protectionStatus;
    private final String imageUrl;
    private final String features;
    private final double latitude;
    private final double longitude;

    public RescuedAnimal(String id, String name, String species, String breed, String gender,
                         String region, String rescuedDate, String protectionStatus,
                         String imageUrl, String features, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.gender = gender;
        this.region = region;
        this.rescuedDate = rescuedDate;
        this.protectionStatus = protectionStatus;
        this.imageUrl = imageUrl;
        this.features = features;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSpecies() {
        return species;
    }

    public String getBreed() {
        return breed;
    }

    public String getGender() {
        return gender;
    }

    public String getRegion() {
        return region;
    }

    public String getRescuedDate() {
        return rescuedDate;
    }

    public String getProtectionStatus() {
        return protectionStatus;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getFeatures() {
        return features;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
