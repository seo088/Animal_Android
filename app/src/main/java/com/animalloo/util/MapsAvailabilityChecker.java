package com.animalloo.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

/**
 * Google Maps API Key 설정 여부를 확인합니다.
 * Key가 없어도 앱의 다른 기능은 정상 동작해야 합니다.
 */
public final class MapsAvailabilityChecker {

    private static final String PLACEHOLDER_KEY = "YOUR_MAPS_API_KEY_HERE";
    private static final String META_DATA_KEY = "com.google.android.geo.API_KEY";

    private MapsAvailabilityChecker() {
    }

    public static boolean isMapsAvailable(Context context) {
        try {
            ApplicationInfo applicationInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (applicationInfo.metaData == null) {
                return false;
            }
            String apiKey = applicationInfo.metaData.getString(META_DATA_KEY);
            return apiKey != null
                    && !apiKey.trim().isEmpty()
                    && !PLACEHOLDER_KEY.equals(apiKey.trim());
        } catch (PackageManager.NameNotFoundException exception) {
            return false;
        }
    }
}
