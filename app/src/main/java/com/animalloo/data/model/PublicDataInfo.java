package com.animalloo.data.model;

public class PublicDataInfo {

    private final String sourceName;
    private final String title;
    private final String summary;
    private final long fetchedAtMillis;

    public PublicDataInfo(String sourceName, String title, String summary, long fetchedAtMillis) {
        this.sourceName = sourceName;
        this.title = title;
        this.summary = summary;
        this.fetchedAtMillis = fetchedAtMillis;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public long getFetchedAtMillis() {
        return fetchedAtMillis;
    }
}
