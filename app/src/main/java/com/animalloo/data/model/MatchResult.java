package com.animalloo.data.model;

public class MatchResult {

    private final String rescuedAnimalId;
    private final String breed;
    private final String gender;
    private final String foundRegion;
    private final String foundDate;
    private final String imageUrl;
    private final MatchGrade grade;
    private final int similarityPercent;

    public MatchResult(String rescuedAnimalId, String breed, String gender,
                       String foundRegion, String foundDate, String imageUrl,
                       MatchGrade grade, int similarityPercent) {
        this.rescuedAnimalId = rescuedAnimalId;
        this.breed = breed;
        this.gender = gender;
        this.foundRegion = foundRegion;
        this.foundDate = foundDate;
        this.imageUrl = imageUrl;
        this.grade = grade;
        this.similarityPercent = similarityPercent;
    }

    public String getRescuedAnimalId() {
        return rescuedAnimalId;
    }

    public String getBreed() {
        return breed;
    }

    public String getGender() {
        return gender;
    }

    public String getFoundRegion() {
        return foundRegion;
    }

    public String getFoundDate() {
        return foundDate;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public MatchGrade getGrade() {
        return grade;
    }

    public int getSimilarityPercent() {
        return similarityPercent;
    }
}
