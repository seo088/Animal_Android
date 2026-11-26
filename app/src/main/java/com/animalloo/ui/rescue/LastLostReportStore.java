package com.animalloo.ui.rescue;

import android.content.Context;

/** Stores only the most recently submitted server report ID for a persistent re-entry point. */
final class LastLostReportStore {

    private static final String PREFS_NAME = "lost_report_state";
    private static final String KEY_REPORT_ID = "last_report_id";

    private LastLostReportStore() {
    }

    static void save(Context context, String reportId) {
        if (reportId == null || reportId.trim().isEmpty()) {
            return;
        }
        context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_REPORT_ID, reportId)
                .apply();
    }

    static String get(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_REPORT_ID, null);
    }

    static void clear(Context context) {
        context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_REPORT_ID)
                .apply();
    }
}
