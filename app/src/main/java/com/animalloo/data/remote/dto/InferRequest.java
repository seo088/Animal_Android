package com.animalloo.data.remote.dto;

import java.util.List;

public class InferRequest {

    private final List<String> symptomIds;

    public InferRequest(List<String> symptomIds) {
        this.symptomIds = symptomIds;
    }
}
