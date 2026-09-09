package com.animalloo.data.model;

public enum MatchGrade {
    A("A 등급", "매우 유사"),
    B("B 등급", "유사"),
    C("C 등급", "일부 일치");

    private final String gradeLabel;
    private final String description;

    MatchGrade(String gradeLabel, String description) {
        this.gradeLabel = gradeLabel;
        this.description = description;
    }

    public String getGradeLabel() {
        return gradeLabel;
    }

    public String getDescription() {
        return description;
    }
}
