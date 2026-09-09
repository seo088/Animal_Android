package com.animalloo.data.model;

public class LostAnimalReport {

    private final String id;
    private final String name;
    private final String species;
    private final String breed;
    private final String gender;
    private final String region;
    private final String lostDate;
    private final String features;
    private final String contactInfo;
    private final String photoPath;
    private final long reportedAt;

    public LostAnimalReport(String id, String name, String species, String breed,
                            String gender, String region, String lostDate,
                            String features, String contactInfo, String photoPath,
                            long reportedAt) {
        this.id = id;
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.gender = gender;
        this.region = region;
        this.lostDate = lostDate;
        this.features = features;
        this.contactInfo = contactInfo;
        this.photoPath = photoPath;
        this.reportedAt = reportedAt;
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

    public String getLostDate() {
        return lostDate;
    }

    public String getFeatures() {
        return features;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public long getReportedAt() {
        return reportedAt;
    }
}
