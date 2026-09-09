package com.animalloo.data.model;

public enum AlertType {
    RESCUE("구조"),
    LOST("분실");

    private final String displayName;

    AlertType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
