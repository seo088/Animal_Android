package com.animalloo.util;

import android.content.Context;

import com.google.firebase.FirebaseApp;

public final class FirebaseAvailabilityChecker {

    private FirebaseAvailabilityChecker() {
    }

    public static boolean isFirebaseAvailable(Context context) {
        try {
            return !FirebaseApp.getApps(context).isEmpty();
        } catch (Exception exception) {
            return false;
        }
    }
}
