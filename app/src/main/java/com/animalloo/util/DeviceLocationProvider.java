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

    public interface CurrentLocationCallback {
        void onLocation(Double latitude, Double longitude);
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

    /** 실제 국내 위치만 돌려준다. 지도 화면에서는 서울 대체 좌표를 사용하지 않는다. */
    @SuppressLint("MissingPermission")
    public static void getCurrentKoreanLocation(Context context, CurrentLocationCallback callback) {
        Context appContext = context.getApplicationContext();
        Handler mainHandler = new Handler(Looper.getMainLooper());
        AtomicBoolean delivered = new AtomicBoolean(false);
        Runnable fallback = () -> deliverCurrent(delivered, callback, null);
        if (!PermissionHelper.hasLocationPermission(appContext)) {
            mainHandler.post(fallback);
            return;
        }
        mainHandler.postDelayed(fallback, TIMEOUT_MS);
        try {
            FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(appContext);
            client.getLastLocation().addOnCompleteListener(task -> {
                Location location = task.isSuccessful() ? task.getResult() : null;
                if (location != null) {
                    mainHandler.removeCallbacks(fallback);
                    deliverCurrent(delivered, callback, location);
                    return;
                }
                client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                        .addOnCompleteListener(current -> {
                            mainHandler.removeCallbacks(fallback);
                            deliverCurrent(delivered, callback,
                                    current.isSuccessful() ? current.getResult() : null);
                        });
            });
        } catch (RuntimeException exception) {
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

    private static void deliverCurrent(AtomicBoolean delivered, CurrentLocationCallback callback,
                                       Location location) {
        if (!delivered.compareAndSet(false, true)) return;
        if (location != null && isInKorea(location.getLatitude(), location.getLongitude())) {
            callback.onLocation(location.getLatitude(), location.getLongitude());
        } else {
            callback.onLocation(null, null);
        }
    }
}
