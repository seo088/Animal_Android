package com.animalloo.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.animalloo.R;
import com.animalloo.ui.main.MainActivity;

public final class NotificationHelper {

    public static final String CHANNEL_RESCUE = "animalloo_rescue";
    public static final String CHANNEL_LOST = "animalloo_lost";
    public static final String CHANNEL_GENERAL = "animalloo_general";

    public static final String EXTRA_OPEN_RESCUE_TAB = "extra_open_rescue_tab";
    public static final String EXTRA_OPEN_HOME = "extra_open_home";

    private static final int NOTIFICATION_ID_RESCUE = 1001;
    private static final int NOTIFICATION_ID_LOST = 1002;
    private static final int NOTIFICATION_ID_GENERAL = 1003;

    private NotificationHelper() {
    }

    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) {
            return;
        }

        android.app.NotificationChannel rescueChannel = new android.app.NotificationChannel(
                CHANNEL_RESCUE,
                context.getString(R.string.notification_channel_rescue),
                NotificationManager.IMPORTANCE_HIGH);
        rescueChannel.setDescription(context.getString(R.string.notification_channel_rescue_desc));

        android.app.NotificationChannel lostChannel = new android.app.NotificationChannel(
                CHANNEL_LOST,
                context.getString(R.string.notification_channel_lost),
                NotificationManager.IMPORTANCE_HIGH);
        lostChannel.setDescription(context.getString(R.string.notification_channel_lost_desc));

        android.app.NotificationChannel generalChannel = new android.app.NotificationChannel(
                CHANNEL_GENERAL,
                context.getString(R.string.notification_channel_general),
                NotificationManager.IMPORTANCE_DEFAULT);
        generalChannel.setDescription(context.getString(R.string.notification_channel_general_desc));

        manager.createNotificationChannel(rescueChannel);
        manager.createNotificationChannel(lostChannel);
        manager.createNotificationChannel(generalChannel);
    }

    public static void showRescueNotification(Context context, String title, String body) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(EXTRA_OPEN_RESCUE_TAB, 1);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID_RESCUE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_RESCUE)
                .setSmallIcon(R.drawable.ic_rescue)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID_RESCUE, builder.build());
        }
    }

    public static void showLostNotification(Context context, String title, String body) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(EXTRA_OPEN_RESCUE_TAB, 0);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID_LOST,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_LOST)
                .setSmallIcon(R.drawable.ic_rescue)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID_LOST, builder.build());
        }
    }

    public static void showGeneralNotification(Context context, String title, String body) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(EXTRA_OPEN_HOME, true);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID_GENERAL,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_GENERAL)
                .setSmallIcon(R.drawable.ic_home)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID_GENERAL, builder.build());
        }
    }
}
