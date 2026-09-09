package com.animalloo.notification;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.animalloo.R;
import com.animalloo.util.SettingsPreferenceHelper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class AnimalLooFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "AnimalLooFCM";

    public static final String DATA_KEY_TYPE = "type";
    public static final String DATA_KEY_TITLE = "title";
    public static final String DATA_KEY_BODY = "body";
    public static final String DATA_KEY_REGION = "region";

    public static final String TYPE_RESCUE = "rescue";
    public static final String TYPE_LOST = "lost";
    public static final String TYPE_LOCATION = "location";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.i(TAG, getString(R.string.fcm_token_updated));
        FcmTokenStore.saveToken(getApplicationContext(), token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        String type = message.getData().get(DATA_KEY_TYPE);
        String title = message.getData().get(DATA_KEY_TITLE);
        String body = message.getData().get(DATA_KEY_BODY);
        String region = message.getData().get(DATA_KEY_REGION);

        if (title == null || title.isEmpty()) {
            title = message.getNotification() != null
                    ? message.getNotification().getTitle()
                    : getString(R.string.app_name);
        }
        if (body == null || body.isEmpty()) {
            body = message.getNotification() != null
                    ? message.getNotification().getBody()
                    : getString(R.string.fcm_default_body);
        }
        if (region != null && !region.isEmpty()) {
            body = body + " (" + region + ")";
        }

        dispatchNotification(type, title, body);
    }

    private void dispatchNotification(String type, String title, String body) {
        Context context = getApplicationContext();

        if (TYPE_RESCUE.equals(type)) {
            if (SettingsPreferenceHelper.isRescueAlertEnabled(context)) {
                NotificationHelper.showRescueNotification(context, title, body);
            }
            return;
        }

        if (TYPE_LOST.equals(type)) {
            if (SettingsPreferenceHelper.isLostAlertEnabled(context)) {
                NotificationHelper.showLostNotification(context, title, body);
            }
            return;
        }

        if (TYPE_LOCATION.equals(type)) {
            if (SettingsPreferenceHelper.isLocationAlertEnabled(context)) {
                NotificationHelper.showGeneralNotification(context, title, body);
            }
            return;
        }

        NotificationHelper.showGeneralNotification(context, title, body);
    }
}
