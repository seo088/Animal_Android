package com.animalloo.ui.rescue;

import android.content.Context;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Stores report IDs created on this device so more than one report remains reachable. */
final class LastLostReportStore {

    private static final String PREFS_NAME = "lost_report_state";
    private static final String KEY_REPORT_ID = "last_report_id";
    private static final String KEY_REPORT_IDS = "report_ids";

    private LastLostReportStore() {
    }

    static void save(Context context, String reportId) {
        if (reportId == null || reportId.trim().isEmpty()) {
            return;
        }
        Set<String> ids = new LinkedHashSet<>(getAll(context));
        ids.remove(reportId);
        ids.add(reportId);
        context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                .putString(KEY_REPORT_ID, reportId).putStringSet(KEY_REPORT_IDS, ids).apply();
    }

    static String get(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_REPORT_ID, null);
    }

    static List<String> getAll(Context context) {
        Set<String> saved = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getStringSet(KEY_REPORT_IDS, null);
        LinkedHashSet<String> ids = saved == null ? new LinkedHashSet<>() : new LinkedHashSet<>(saved);
        String legacyId = get(context);
        if (legacyId != null && !legacyId.trim().isEmpty()) ids.add(legacyId);
        return new ArrayList<>(ids);
    }

    static void clear(Context context) {
        context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_REPORT_ID)
                .remove(KEY_REPORT_IDS)
                .apply();
    }
}
