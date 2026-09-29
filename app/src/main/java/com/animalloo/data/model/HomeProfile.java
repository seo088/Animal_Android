package com.animalloo.data.model;

public class HomeProfile {

    private final String userDisplayName;
    private final String userPhotoUrl;
    private final int userPhotoFallbackResId;
    private final String petName;
    private final String petBreed;
    private final String petAge;

    public HomeProfile(String userDisplayName, String userPhotoUrl, int userPhotoFallbackResId,
                       String petName, String petBreed, String petAge) {
        this.userDisplayName = userDisplayName;
        this.userPhotoUrl = userPhotoUrl;
        this.userPhotoFallbackResId = userPhotoFallbackResId;
        this.petName = petName;
        this.petBreed = petBreed;
        this.petAge = petAge;
    }

    public String getUserDisplayName() {
        return userDisplayName;
    }

    public String getUserPhotoUrl() {
        return userPhotoUrl;
    }

    public int getUserPhotoFallbackResId() {
        return userPhotoFallbackResId;
    }

    public String getPetName() {
        return petName;
    }

    public String getPetBreed() {
        return petBreed;
    }

    public String getPetAge() {
        return petAge;
    }
}
