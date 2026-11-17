package com.animalloo;

import android.app.Application;
import android.util.Log;

import com.animalloo.notification.FcmTokenStore;
import com.animalloo.notification.NotificationHelper;
import com.animalloo.util.FirebaseAvailabilityChecker;
import com.animalloo.util.MapsAvailabilityChecker;
import com.animalloo.util.RepositoryProvider;
import com.google.firebase.messaging.FirebaseMessaging;
import com.kakao.vectormap.KakaoMapSdk;

public class AnimalLooApplication extends Application {

    private static final String TAG = "AnimalLooApp";

    @Override
    public void onCreate() {
        super.onCreate();
        ensureKakaoMapSdkInitialized();
        RepositoryProvider.init(this);
        NotificationHelper.createNotificationChannels(this);
        registerFcmTokenIfAvailable();
    }

    /**
     * Rechecks the map SDK when a map screen is opened. This avoids keeping a
     * failed startup attempt as the permanent reason for showing the list UI.
     */
    public boolean ensureKakaoMapSdkInitialized() {
        if (MapsAvailabilityChecker.isMapsAvailable(this)) {
            return true;
        }
        if (!MapsAvailabilityChecker.isKeyConfigured(this)) {
            Log.i(TAG, "Kakao Map key is not configured. Set KAKAO_MAP_KEY in .env or environment variables.");
            return false;
        }
        try {
            KakaoMapSdk.init(this, BuildConfig.KAKAO_MAP_KEY);
            MapsAvailabilityChecker.setSdkInitialized(true);
            return true;
        } catch (UnsatisfiedLinkError error) {
            MapsAvailabilityChecker.setSdkInitialized(false);
            Log.w(TAG, "Kakao Map native library is unavailable; using the facility list.", error);
            return false;
        }
    }

    private void registerFcmTokenIfAvailable() {
        if (!FirebaseAvailabilityChecker.isFirebaseAvailable(this)) {
            Log.i(TAG, "Firebase is not configured. Add google-services.json locally for FCM.");
            return;
        }

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FcmTokenStore.saveToken(this, task.getResult());
                    }
                });
    }

    @Override
    public void onTerminate() {
        RepositoryProvider.shutdownIfInitialized();
        super.onTerminate();
    }
}
