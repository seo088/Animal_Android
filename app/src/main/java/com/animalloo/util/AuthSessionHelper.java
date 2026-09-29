package com.animalloo.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.animalloo.data.model.User;

import java.util.Locale;

/**
 * Persists mock auth session and registered accounts in SharedPreferences.
 */
public final class AuthSessionHelper {

    private static final String PREFS_NAME = "animalloo_auth";
    private static final String KEY_SESSION_USER_ID = "session_user_id";
    private static final String KEY_REGISTERED_EMAILS = "registered_emails";

    private static final String KEY_USER_ID_PREFIX = "user_id_";
    private static final String KEY_USER_NAME_PREFIX = "user_name_";
    private static final String KEY_USER_PASSWORD_PREFIX = "user_password_";

    public static final String DEMO_EMAIL = "admin";
    public static final String DEMO_PASSWORD = "1234";
    private static final String LEGACY_DEMO_EMAIL = "demo@animalloo.app";
    public static final String DEMO_DISPLAY_NAME = "데모 사용자";
    public static final String DEMO_USER_ID = "user_demo";

    private AuthSessionHelper() {
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void ensureDemoAccount(Context context) {
        removeAccount(context, LEGACY_DEMO_EMAIL);
        saveAccount(context, DEMO_USER_ID, DEMO_DISPLAY_NAME, DEMO_EMAIL, DEMO_PASSWORD);
    }

    public static void saveAccount(Context context, String userId, String displayName,
                                   String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        SharedPreferences prefs = getPrefs(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USER_ID_PREFIX + normalizedEmail, userId);
        editor.putString(KEY_USER_NAME_PREFIX + normalizedEmail, displayName);
        editor.putString(KEY_USER_PASSWORD_PREFIX + normalizedEmail, password);

        String registered = prefs.getString(KEY_REGISTERED_EMAILS, "");
        if (!isEmailRegisteredInList(registered, normalizedEmail)) {
            String updated = registered.isEmpty()
                    ? normalizedEmail
                    : registered + "," + normalizedEmail;
            editor.putString(KEY_REGISTERED_EMAILS, updated);
        }
        editor.apply();
    }

    public static boolean isEmailRegistered(Context context, String email) {
        return getPrefs(context).contains(KEY_USER_PASSWORD_PREFIX + normalizeEmail(email));
    }

    public static User authenticate(Context context, String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        SharedPreferences prefs = getPrefs(context);
        String storedPassword = prefs.getString(KEY_USER_PASSWORD_PREFIX + normalizedEmail, null);
        if (storedPassword == null || !storedPassword.equals(password)) {
            return null;
        }
        String userId = prefs.getString(KEY_USER_ID_PREFIX + normalizedEmail, "");
        String displayName = prefs.getString(KEY_USER_NAME_PREFIX + normalizedEmail, "");
        return new User(userId, displayName, normalizedEmail);
    }

    public static void saveSession(Context context, User user) {
        getPrefs(context).edit()
                .putString(KEY_SESSION_USER_ID, user.getId())
                .putString(KEY_USER_ID_PREFIX + user.getEmail(), user.getId())
                .putString(KEY_USER_NAME_PREFIX + user.getEmail(), user.getDisplayName())
                .apply();
    }

    public static User getSessionUser(Context context) {
        SharedPreferences prefs = getPrefs(context);
        String sessionUserId = prefs.getString(KEY_SESSION_USER_ID, null);
        if (sessionUserId == null || sessionUserId.isEmpty()) {
            return null;
        }

        String registered = prefs.getString(KEY_REGISTERED_EMAILS, "");
        if (registered.isEmpty()) {
            return null;
        }

        for (String email : registered.split(",")) {
            String userId = prefs.getString(KEY_USER_ID_PREFIX + email, null);
            if (sessionUserId.equals(userId)) {
                String displayName = prefs.getString(KEY_USER_NAME_PREFIX + email, "");
                return new User(userId, displayName, email);
            }
        }
        return null;
    }

    public static void clearSession(Context context) {
        getPrefs(context).edit().remove(KEY_SESSION_USER_ID).apply();
    }

    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean isValidLoginId(String loginId) {
        if (loginId == null || loginId.trim().isEmpty()) {
            return false;
        }
        String normalized = normalizeEmail(loginId);
        return DEMO_EMAIL.equals(normalized)
                || android.util.Patterns.EMAIL_ADDRESS.matcher(loginId.trim()).matches();
    }

    private static void removeAccount(Context context, String email) {
        String normalizedEmail = normalizeEmail(email);
        SharedPreferences prefs = getPrefs(context);
        if (!prefs.contains(KEY_USER_PASSWORD_PREFIX + normalizedEmail)) {
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_USER_ID_PREFIX + normalizedEmail);
        editor.remove(KEY_USER_NAME_PREFIX + normalizedEmail);
        editor.remove(KEY_USER_PASSWORD_PREFIX + normalizedEmail);

        String registered = prefs.getString(KEY_REGISTERED_EMAILS, "");
        if (registered != null && !registered.isEmpty()) {
            StringBuilder updated = new StringBuilder();
            for (String item : registered.split(",")) {
                if (!item.equals(normalizedEmail)) {
                    if (updated.length() > 0) {
                        updated.append(',');
                    }
                    updated.append(item);
                }
            }
            editor.putString(KEY_REGISTERED_EMAILS, updated.toString());
        }
        editor.apply();
    }

    private static boolean isEmailRegisteredInList(String registeredCsv, String normalizedEmail) {
        if (registeredCsv == null || registeredCsv.isEmpty()) {
            return false;
        }
        for (String email : registeredCsv.split(",")) {
            if (email.equals(normalizedEmail)) {
                return true;
            }
        }
        return false;
    }
}
