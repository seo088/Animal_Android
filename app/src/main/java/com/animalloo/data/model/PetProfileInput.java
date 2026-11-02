package com.animalloo.data.model;

public class PetProfileInput {
    public final String name, breed, age, species, gender, neutered, birthDate, adoptionDate;
    public final Double weightKg;
    public PetProfileInput(String name, String breed, String age, String species, String gender, String neutered, Double weightKg, String birthDate, String adoptionDate) {
        this.name=name; this.breed=breed; this.age=age; this.species=species; this.gender=gender; this.neutered=neutered; this.weightKg=weightKg; this.birthDate=birthDate; this.adoptionDate=adoptionDate;
    }
}
