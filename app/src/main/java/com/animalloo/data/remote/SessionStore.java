package com.animalloo.data.remote;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import com.animalloo.data.model.User;

/**
 * Persists the backend session (JWT + signed-in user) in SharedPreferences.
 */
public final class SessionStore {

    private static final String PREFS_NAME = "animalloo_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";

    private final SharedPreferences prefs;

    public SessionStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void save(String token, User user) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USER_ID, user.getId())
                .putString(KEY_USER_NAME, user.getDisplayName())
                .putString(KEY_USER_EMAIL, user.getEmail())
                .apply();
    }

    @Nullable
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    @Nullable
    public User getUser() {
        if (getToken() == null) {
            return null;
        }
        return new User(
                prefs.getString(KEY_USER_ID, ""),
                prefs.getString(KEY_USER_NAME, ""),
                prefs.getString(KEY_USER_EMAIL, ""));
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
