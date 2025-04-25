package com.example.mobile_phone_subscription;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class NotificationHelper {
    private static final String PURCHASE_CHANNEL_ID = "purchase_channel";
    private static final String REMINDER_CHANNEL_ID = "reminder_channel";
    private static final String CANCEL_CHANNEL_ID = "cancel_channel";


    // Notification IDs
    public static final int PURCHASE_NOTIFICATION_ID = 1;
    public static final int REMINDER_NOTIFICATION_ID = 2;
    public static final int CANCEL_NOTIFICATION_ID = 3;

    // Notification csatornák létrehozása
    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // Vásárlási csatorna
            NotificationChannel purchaseChannel = new NotificationChannel(
                    PURCHASE_CHANNEL_ID,
                    "Vásárlási értesítések",
                    NotificationManager.IMPORTANCE_DEFAULT);

            // Emlékeztető csatorna
            NotificationChannel reminderChannel = new NotificationChannel(
                    REMINDER_CHANNEL_ID,
                    "Emlékeztető értesítések",
                    NotificationManager.IMPORTANCE_HIGH);

            // Lemondási csatorna
            NotificationChannel cancelChannel = new NotificationChannel(
                    CANCEL_CHANNEL_ID,
                    "Lemondási értesítések",
                    NotificationManager.IMPORTANCE_DEFAULT);


            notificationManager.createNotificationChannel(purchaseChannel);
            notificationManager.createNotificationChannel(reminderChannel);
            notificationManager.createNotificationChannel(cancelChannel);
        }
    }

    public static void sendPurchaseNotification(Context context, String planName) {
        // Intent létrehozása a Profile aktivitáshoz
        Intent intent = new Intent(context, Profile.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // PendingIntent létrehozása
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                PURCHASE_NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, PURCHASE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_shop)
                .setContentTitle("Sikeres előfizetés")
                .setContentText("Sikeresen előfizettél a következő csomagra: " + planName)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)  // PendingIntent beállítása
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(PURCHASE_NOTIFICATION_ID, builder.build());
    }

    public static void sendReminderNotification(Context context, String planName) {
        // Intent létrehozása a Profile aktivitáshoz
        Intent intent = new Intent(context, Profile.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // PendingIntent létrehozása
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                REMINDER_NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_shop)
                .setContentTitle("Üdv a csomagban!")
                .setContentText("Köszönjük, hogy előfizettél erre a csomagra: " + planName)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)  // PendingIntent beállítása
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(REMINDER_NOTIFICATION_ID, builder.build());
    }

    /**
     * Értesítés küldése előfizetés lemondásakor
     */
    public static void sendCancellationNotification(Context context, String planName) {
        // Intent létrehozása a Shopping aktivitáshoz
        Intent intent = new Intent(context, Shopping.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // PendingIntent létrehozása
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                CANCEL_NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CANCEL_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_shop)
                .setContentTitle("Előfizetés lemondva")
                .setContentText("Sajnáljuk, hogy lemondtad az előfizetésed. \nReméljük, hogy hamarosan előfizetsz egy újra :)")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(CANCEL_NOTIFICATION_ID, builder.build());
    }

    // Értesítési engedély ellenőrzése (Android 13+)
    public static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }
}