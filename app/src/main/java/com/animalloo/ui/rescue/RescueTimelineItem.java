package com.animalloo.ui.rescue;

import androidx.annotation.Nullable;

import com.animalloo.data.model.AlertNotification;
import com.animalloo.data.model.LostAnimalReport;

public class RescueTimelineItem {

    private final RescueTimelineItemType type;
    @Nullable
    private final LostAnimalReport myReport;
    private final int matchCount;
    private final int highPriorityMatchCount;
    @Nullable
    private final AlertNotification alert;

    private RescueTimelineItem(RescueTimelineItemType type, @Nullable LostAnimalReport myReport,
                               int matchCount, int highPriorityMatchCount,
                               @Nullable AlertNotification alert) {
        this.type = type;
        this.myReport = myReport;
        this.matchCount = matchCount;
        this.highPriorityMatchCount = highPriorityMatchCount;
        this.alert = alert;
    }

    public static RescueTimelineItem myReport(LostAnimalReport report, int matchCount,
                                               int highPriorityMatchCount) {
        return new RescueTimelineItem(RescueTimelineItemType.MY_REPORT, report, matchCount,
                highPriorityMatchCount, null);
    }

    public static RescueTimelineItem timelineEvent(AlertNotification alert) {
        return new RescueTimelineItem(RescueTimelineItemType.TIMELINE_EVENT, null, 0, 0, alert);
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

    public int getHighPriorityMatchCount() {
        return highPriorityMatchCount;
    }

    @Nullable
    public AlertNotification getAlert() {
        return alert;
    }
}
