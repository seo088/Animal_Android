package com.animalloo.data.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DetailUiModel {

    private final DetailType type;
    private final String title;
    private final String subtitle;
    private final String imageUrl;
    private final String description;
    private final List<DetailField> fields;
    private final DetailType relatedItemType;
    private final String relatedItemId;

    public DetailUiModel(DetailType type, String title, String subtitle, String imageUrl,
                         String description, List<DetailField> fields,
                         DetailType relatedItemType, String relatedItemId) {
        this.type = type;
        this.title = title;
        this.subtitle = subtitle;
        this.imageUrl = imageUrl;
        this.description = description;
        this.fields = fields == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(fields));
        this.relatedItemType = relatedItemType;
        this.relatedItemId = relatedItemId;
    }

    public DetailType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public List<DetailField> getFields() {
        return fields;
    }

    public DetailType getRelatedItemType() {
        return relatedItemType;
    }

    public String getRelatedItemId() {
        return relatedItemId;
    }

    public boolean hasRelatedItem() {
        return relatedItemType != null && relatedItemId != null && !relatedItemId.isEmpty();
    }
}
