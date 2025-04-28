package com.example.mobile_phone_subscription;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

/**
 * Előfizetés emlékeztető osztály.
 * Emlékeztetőt állít be a felhasználó számára, hogy ne felejtse el megújítani az előfizetését.
 */
public class SubscriptionReminder extends BroadcastReceiver {
    private static final String TAG = "SubscriptionReminder";
    public static final String EXTRA_PLAN_NAME = "PLAN_NAME";

    /*
     * Emlékeztető értesítés küldése
     * A BroadcastReceiver osztály onReceive() metódusában hívjuk meg
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String planName = intent.getStringExtra(EXTRA_PLAN_NAME);

        if (planName != null) {
            Log.d(TAG, "Emlékeztető értesítés: " + planName);
            NotificationHelper.sendReminderNotification(context, planName);
        }
    }

    /**
     * Emlékeztető beállítása
     * 24 órával későbbre állítja be az emlékeztetőt
     * @param context A kontextus, amelyből az emlékeztetőt beállítjuk
     * @param planName A csomag neve, amelyhez az emlékeztető tartozik
     */
    public static void setReminderAlarm(Context context, String planName) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Ellenőrizzük a jogosultságot Android 13+ (API 33+) esetén
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !alarmManager.canScheduleExactAlarms()) {
            Log.e(TAG, "Exact alarm permission not granted");
            // Ha a context egy Activity, megnyithatjuk a beállításokat
            if (context instanceof android.app.Activity) {
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(android.net.Uri.fromParts("package", context.getPackageName(), null));
                context.startActivity(intent);
            }
            return;
        }

        Intent intent = new Intent(context, SubscriptionReminder.class);
        intent.putExtra(EXTRA_PLAN_NAME, planName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                planName.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            // 24 órával későbbre állítjuk be az emlékeztetőt
            long triggerTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                Log.d(TAG, "Emlékeztető beállítva 24 órára: " + planName);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                Log.d(TAG, "Emlékeztető beállítva 24 órára: " + planName);
            }
        }
    }

    /**
     * Törli az emlékeztetőt, ha már nem szükséges
     * @param context A kontextus, amelyből a törlést végrehajtjuk
     * @param planName A csomag neve, amelyhez az emlékeztető tartozik
     */
    public static void cancelReminderAlarm(Context context, String planName) {
        Intent intent = new Intent(context, SubscriptionReminder.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                planName.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Log.d(TAG, "Emlékeztető törölve: " + planName);
        }
    }
}