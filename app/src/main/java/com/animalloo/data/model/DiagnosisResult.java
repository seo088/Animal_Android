package com.animalloo.data.model;

import java.util.List;

public class DiagnosisResult {

    private final String id;
    private final String diseaseName;
    private final int probabilityPercent;
    private final List<String> relatedSymptoms;
    private final String description;
    private final String caution;

    public DiagnosisResult(String id, String diseaseName, int probabilityPercent,
                           List<String> relatedSymptoms, String description, String caution) {
        this.id = id;
        this.diseaseName = diseaseName;
        this.probabilityPercent = probabilityPercent;
        this.relatedSymptoms = relatedSymptoms;
        this.description = description;
        this.caution = caution;
    }

    public String getId() {
        return id;
    }

    public String getDiseaseName() {
        return diseaseName;
    }

    public int getProbabilityPercent() {
        return probabilityPercent;
    }

    public List<String> getRelatedSymptoms() {
        return relatedSymptoms;
    }

    public String getDescription() {
        return description;
    }

    public String getCaution() {
        return caution;
    }
}
