package com.animalloo.notification;

import android.content.Context;
import android.content.SharedPreferences;

public final class FcmTokenStore {

    private static final String PREFS_NAME = "animalloo_fcm";
    private static final String KEY_TOKEN = "fcm_token";

    private FcmTokenStore() {
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void saveToken(Context context, String token) {
        getPrefs(context).edit().putString(KEY_TOKEN, token).apply();
    }

    public static String getToken(Context context) {
        return getPrefs(context).getString(KEY_TOKEN, null);
    }
}
