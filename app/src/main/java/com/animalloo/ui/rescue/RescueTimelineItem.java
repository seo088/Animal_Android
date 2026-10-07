package com.animalloo.ui.rescue;

import androidx.annotation.Nullable;

import com.animalloo.data.model.AlertNotification;
import com.animalloo.data.model.LostAnimalReport;

public class RescueTimelineItem {

    private final RescueTimelineItemType type;
    @Nullable
    private final LostAnimalReport myReport;
    private final int matchCount;
    @Nullable
    private final AlertNotification alert;

    private RescueTimelineItem(RescueTimelineItemType type, @Nullable LostAnimalReport myReport,
                               int matchCount, @Nullable AlertNotification alert) {
        this.type = type;
        this.myReport = myReport;
        this.matchCount = matchCount;
        this.alert = alert;
    }

    public static RescueTimelineItem myReport(LostAnimalReport report, int matchCount) {
        return new RescueTimelineItem(RescueTimelineItemType.MY_REPORT, report, matchCount, null);
    }

    public static RescueTimelineItem timelineEvent(AlertNotification alert) {
        return new RescueTimelineItem(RescueTimelineItemType.TIMELINE_EVENT, null, 0, alert);
    }

    public RescueTimelineItemType getType() {
        return type;
    }

    @Nullable
    public LostAnimalReport getMyReport() {
        return myReport;
    }

    public int getMatchCount() {
        return matchCount;
    }

    @Nullable
    public AlertNotification getAlert() {
        return alert;
    }
}
