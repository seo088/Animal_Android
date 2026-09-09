package com.animalloo;

import android.app.Application;
import android.util.Log;

import com.animalloo.notification.FcmTokenStore;
import com.animalloo.notification.NotificationHelper;
import com.animalloo.util.FirebaseAvailabilityChecker;
import com.animalloo.util.RepositoryProvider;
import com.google.firebase.messaging.FirebaseMessaging;

public class AnimalLooApplication extends Application {

    private static final String TAG = "AnimalLooApp";

    @Override
    public void onCreate() {
        super.onCreate();
        RepositoryProvider.init(this);
        NotificationHelper.createNotificationChannels(this);
        registerFcmTokenIfAvailable();
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
