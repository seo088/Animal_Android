package com.animalloo.data.remote.dto;

/**
 * Server shape of HomeProfile; the app model additionally carries a local fallback drawable.
 */
public class HomeProfileResponse {

    private String userDisplayName;
    private String userPhotoUrl;
    private String petName;
    private String petBreed;
    private String petAge;

    public String getUserDisplayName() {
        return userDisplayName;
    }

    public String getUserPhotoUrl() {
        return userPhotoUrl;
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
