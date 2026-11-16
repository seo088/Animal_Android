package com.animalloo;

import android.app.Application;
import android.os.Build;
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
        initKakaoMapSdk();
        RepositoryProvider.init(this);
        NotificationHelper.createNotificationChannels(this);
        registerFcmTokenIfAvailable();
    }

    private void initKakaoMapSdk() {
        if (!isKakaoMapSupportedAbi()) {
            Log.w(TAG, "Kakao Map is unavailable on this device ABI: "
                    + String.join(", ", Build.SUPPORTED_ABIS));
            return;
        }
        if (!MapsAvailabilityChecker.isKeyConfigured(this)) {
            Log.i(TAG, "Kakao Map key is not configured. Set KAKAO_MAP_KEY in .env or environment variables.");
            return;
        }
        try {
            KakaoMapSdk.init(this, BuildConfig.KAKAO_MAP_KEY);
            MapsAvailabilityChecker.setSdkInitialized(true);
        } catch (UnsatisfiedLinkError error) {
            Log.w(TAG, "Kakao Map native library is unavailable; using the facility list.", error);
        }
    }

    private boolean isKakaoMapSupportedAbi() {
        if (Build.SUPPORTED_ABIS == null || Build.SUPPORTED_ABIS.length == 0) {
            return false;
        }
        // Some x86_64 emulators provide Android's native bridge and advertise an
        // ARM ABI as a secondary ABI. Kakao Map can then load its ARM library, so
        // do not discard that usable configuration only because x86_64 is first.
        for (String abi : Build.SUPPORTED_ABIS) {
            if ("arm64-v8a".equals(abi) || "armeabi-v7a".equals(abi)) {
                return true;
            }
        }
        return false;
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
