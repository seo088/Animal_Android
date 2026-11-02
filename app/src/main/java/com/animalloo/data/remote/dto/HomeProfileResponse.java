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
    private String petSpecies, petGender, petNeutered, petBirthDate, petAdoptionDate, petPhotoUrl;
    private Double petWeightKg;

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
    public String getPetSpecies() { return petSpecies; }
    public String getPetGender() { return petGender; }
    public String getPetNeutered() { return petNeutered; }
    public Double getPetWeightKg() { return petWeightKg; }
    public String getPetBirthDate() { return petBirthDate; }
    public String getPetAdoptionDate() { return petAdoptionDate; }
    public String getPetPhotoUrl() { return petPhotoUrl; }
}
