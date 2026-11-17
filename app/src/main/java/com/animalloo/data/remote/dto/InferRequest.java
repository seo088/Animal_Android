package com.animalloo.data.remote.dto;

import java.util.List;

public class InferRequest {

    private final String species;
    private final List<String> symptomIds;

    public InferRequest(String species, List<String> symptomIds) {
        this.species = species;
        this.symptomIds = symptomIds;
    }
}
