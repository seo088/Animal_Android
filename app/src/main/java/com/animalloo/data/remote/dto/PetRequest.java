package com.animalloo.data.remote.dto;

/** Request body for the existing PUT /home/pet endpoint. */
public class PetRequest {

    private final String name;
    private final String breed;
    private final String age;
    private final String species, gender, neutered, birthDate, adoptionDate;
    private final Double weightKg;

    public PetRequest(String name, String breed, String age) {
        this(name, breed, age, null, null, null, null, null, null);
    }

    public PetRequest(String name, String breed, String age, String species, String gender,
                      String neutered, Double weightKg, String birthDate, String adoptionDate) {
        this.name = name;
        this.breed = breed;
        this.age = age;
        this.species = species;
        this.gender = gender;
        this.neutered = neutered;
        this.weightKg = weightKg;
        this.birthDate = birthDate;
        this.adoptionDate = adoptionDate;
    }
}
