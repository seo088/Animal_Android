package com.animalloo.data.model;

public class HomeStats {

    private final int protectedCount;
    private final int rescuedTodayCount;
    private final int lostReportCount;
    private final int facilityCount;

    public HomeStats(int protectedCount, int rescuedTodayCount,
                     int lostReportCount, int facilityCount) {
        this.protectedCount = protectedCount;
        this.rescuedTodayCount = rescuedTodayCount;
        this.lostReportCount = lostReportCount;
        this.facilityCount = facilityCount;
    }

    public int getProtectedCount() {
        return protectedCount;
    }

    public int getRescuedTodayCount() {
        return rescuedTodayCount;
    }

    public int getLostReportCount() {
        return lostReportCount;
    }

    public int getFacilityCount() {
        return facilityCount;
    }
}
