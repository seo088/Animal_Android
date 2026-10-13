package com.animalloo.data.remote.dto;

import com.animalloo.data.model.LostAnimalReport;

public class LostReportMatchRequest {

    private final String species;
    private final String breed;
    private final String gender;
    private final String region;
    private final String lostDate;
    private final String features;

    public LostReportMatchRequest(LostAnimalReport report) {
        species = report.getSpecies();
        breed = report.getBreed();
        gender = report.getGender();
        region = report.getRegion();
        lostDate = report.getLostDate();
        features = report.getFeatures();
    }
}
