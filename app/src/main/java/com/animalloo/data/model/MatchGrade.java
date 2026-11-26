package com.animalloo.data.model;

public enum MatchGrade {
    A("유사도 높음 (A)", "먼저 확인해 보세요"),
    B("유사도 보통 (B)", "신고 정보와 비교해 보세요"),
    C("일부 정보 일치 (C)", "사진과 특징을 더 확인해 보세요");

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
