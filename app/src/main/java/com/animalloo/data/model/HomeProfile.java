package com.animalloo.data.model;

public class HomeProfile {

    private final String userDisplayName;
    private final String userPhotoUrl;
    private final int userPhotoFallbackResId;
    private final String petName;
    private final String petBreed;
    private final String petAge;
    private final String petSpecies;
    private final String petGender;
    private final String petNeutered;
    private final Double petWeightKg;
    private final String petBirthDate;
    private final String petAdoptionDate;
    private final String petPhotoUrl;

    public HomeProfile(String userDisplayName, String userPhotoUrl, int userPhotoFallbackResId,
                       String petName, String petBreed, String petAge) {
        this(userDisplayName, userPhotoUrl, userPhotoFallbackResId, petName, petBreed, petAge,
                null, null, null, null, null, null, null);
    }

    public HomeProfile(String userDisplayName, String userPhotoUrl, int userPhotoFallbackResId,
                       String petName, String petBreed, String petAge, String petSpecies,
                       String petGender, String petNeutered, Double petWeightKg,
                       String petBirthDate, String petAdoptionDate, String petPhotoUrl) {
        this.userDisplayName = userDisplayName;
        this.userPhotoUrl = userPhotoUrl;
        this.userPhotoFallbackResId = userPhotoFallbackResId;
        this.petName = petName;
        this.petBreed = petBreed;
        this.petAge = petAge;
        this.petSpecies = petSpecies;
        this.petGender = petGender;
        this.petNeutered = petNeutered;
        this.petWeightKg = petWeightKg;
        this.petBirthDate = petBirthDate;
        this.petAdoptionDate = petAdoptionDate;
        this.petPhotoUrl = petPhotoUrl;
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
    public String getPetSpecies() { return petSpecies; }
    public String getPetGender() { return petGender; }
    public String getPetNeutered() { return petNeutered; }
    public Double getPetWeightKg() { return petWeightKg; }
    public String getPetBirthDate() { return petBirthDate; }
    public String getPetAdoptionDate() { return petAdoptionDate; }
    public String getPetPhotoUrl() { return petPhotoUrl; }

    /** False when the signed-in user hasn't registered a pet yet (pet fields are null). */
    public boolean hasPet() {
        return petName != null && !petName.isEmpty();
    }
}
