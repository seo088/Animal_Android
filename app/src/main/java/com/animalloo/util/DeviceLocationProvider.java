package com.animalloo.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * One-shot device location for "nearby" queries. Falls back to Seoul City Hall
 * (the map's default center) when permission is missing or no fix arrives in time.
 */
public final class DeviceLocationProvider {

    public static final double DEFAULT_LATITUDE = 37.5665;
    public static final double DEFAULT_LONGITUDE = 126.9780;
    private static final long TIMEOUT_MS = 5000L;

    public interface Callback {
        /** Always called exactly once, on the main thread. */
        void onLocation(double latitude, double longitude);
    }

    private DeviceLocationProvider() {
    }

    @SuppressLint("MissingPermission") // checked via PermissionHelper
    public static void getLocation(Context context, Callback callback) {
        Context appContext = context.getApplicationContext();
        Handler mainHandler = new Handler(Looper.getMainLooper());
        AtomicBoolean delivered = new AtomicBoolean(false);
        Runnable fallback = () -> deliver(delivered, callback, null);

        if (!PermissionHelper.hasLocationPermission(appContext)) {
            mainHandler.post(fallback);
            return;
        }
        mainHandler.postDelayed(fallback, TIMEOUT_MS);

        try {
            FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(appContext);
            client.getLastLocation().addOnCompleteListener(lastTask -> {
                Location last = lastTask.isSuccessful() ? lastTask.getResult() : null;
                if (last != null) {
                    mainHandler.removeCallbacks(fallback);
                    deliver(delivered, callback, last);
                    return;
                }
                client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                        .addOnCompleteListener(currentTask -> {
                            mainHandler.removeCallbacks(fallback);
                            deliver(delivered, callback,
                                    currentTask.isSuccessful() ? currentTask.getResult() : null);
                        });
            });
        } catch (RuntimeException exception) {
            // Play services unavailable or permission revoked mid-call.
            mainHandler.removeCallbacks(fallback);
            mainHandler.post(fallback);
        }
    }

    private static void deliver(AtomicBoolean delivered, Callback callback, Location location) {
        if (!delivered.compareAndSet(false, true)) {
            return;
        }
        // All data is domestic; an out-of-Korea fix (e.g. the emulator's default
        // Mountain View location) would only ever produce empty results.
        if (location != null && isInKorea(location.getLatitude(), location.getLongitude())) {
            callback.onLocation(location.getLatitude(), location.getLongitude());
        } else {
            callback.onLocation(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
        }
    }

    private static boolean isInKorea(double latitude, double longitude) {
        return latitude >= 33.0 && latitude <= 38.7 && longitude >= 124.5 && longitude <= 131.0;
    }
}
