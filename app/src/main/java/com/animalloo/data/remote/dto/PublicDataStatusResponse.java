package com.animalloo.data.remote.dto;

public class PublicDataStatusResponse {

    private int facilityCount;
    private int rescuedAnimalCount;
    private int publicLostReportCount;
    private String latestRescuedDate;
    private String latestLostDate;
    private boolean publicDataSyncConfigured;

    public int getFacilityCount() { return facilityCount; }
    public int getRescuedAnimalCount() { return rescuedAnimalCount; }
    public int getPublicLostReportCount() { return publicLostReportCount; }
    public String getLatestRescuedDate() { return latestRescuedDate; }
    public String getLatestLostDate() { return latestLostDate; }
    public boolean isPublicDataSyncConfigured() { return publicDataSyncConfigured; }
}
