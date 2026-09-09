package com.animalloo.data.model;

public class AlertNotification {

    private final String id;
    private final AlertType type;
    private final String animalType;
    private final String region;
    private final String occurredAt;
    private final String status;
    private final String imageUrl;
    private final String relatedItemId;

    public AlertNotification(String id, AlertType type, String animalType, String region,
                             String occurredAt, String status, String imageUrl,
                             String relatedItemId) {
        this.id = id;
        this.type = type;
        this.animalType = animalType;
        this.region = region;
        this.occurredAt = occurredAt;
        this.status = status;
        this.imageUrl = imageUrl;
        this.relatedItemId = relatedItemId;
    }

    public String getId() {
        return id;
    }

    public AlertType getType() {
        return type;
    }

    public String getAnimalType() {
        return animalType;
    }

    public String getRegion() {
        return region;
    }

    public String getOccurredAt() {
        return occurredAt;
    }

    public String getStatus() {
        return status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getRelatedItemId() {
        return relatedItemId;
    }
}
