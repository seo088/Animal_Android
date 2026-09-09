package com.animalloo.data.model;

public enum FacilityCategory {
    HOSPITAL("동물병원"),
    PHARMACY("약국"),
    SHELTER("보호소"),
    RESTAURANT("동반식당"),
    CAFE("동반카페"),
    HOTEL("동반숙소"),
    TOURISM("동반관광지");

    private final String displayName;

    FacilityCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
