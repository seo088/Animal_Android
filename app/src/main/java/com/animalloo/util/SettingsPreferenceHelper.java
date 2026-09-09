package com.animalloo.util;

import android.content.Context;
import android.content.SharedPreferences;

public final class SettingsPreferenceHelper {

    private static final String PREFS_NAME = "animalloo_settings";
    private static final String KEY_RESCUE_ALERT = "rescue_alert";
    private static final String KEY_LOST_ALERT = "lost_alert";
    private static final String KEY_LOCATION_ALERT = "location_alert";

    private SettingsPreferenceHelper() {
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isRescueAlertEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_RESCUE_ALERT, true);
    }

    public static void setRescueAlertEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_RESCUE_ALERT, enabled).apply();
    }

    public static boolean isLostAlertEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_LOST_ALERT, true);
    }

    public static void setLostAlertEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_LOST_ALERT, enabled).apply();
    }

    public static boolean isLocationAlertEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_LOCATION_ALERT, false);
    }

    public static void setLocationAlertEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_LOCATION_ALERT, enabled).apply();
    }
}
