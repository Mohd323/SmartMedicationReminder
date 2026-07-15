package de.hwr.smartmedicationreminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String name = intent.getStringExtra("name");
        String channelId = "medication_alarm"; // Die Kanal-ID ist ein eindeutiger Name

        // Alarmton verwenden
        Uri sound = RingtoneManager.getDefaultUri(
                RingtoneManager.TYPE_ALARM
        );

        // Benachrichtigungskanal ab Android 8
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
// wie Android den Ton behandeln soll
            AudioAttributes audio = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();

            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Medikamentenalarm",
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setSound(sound, audio);
            channel.enableVibration(true);

            NotificationManager manager =
                    context.getSystemService(NotificationManager.class); //Android-Systemdienst, Er verwaltet alle Benachrichtigungen.

            manager.createNotificationChannel(channel); // Hier wird der Kanal bei Android angemeldet.
        }

        // Benachrichtigung erstellen
        NotificationCompat.Builder notification =
                new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("Medikament einnehmen")
                        .setContentText(name + " ist jetzt fällig.")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context)
                    .notify(name.hashCode(), notification.build());
        } catch (SecurityException ignored) {
            // Keine Benachrichtigung, falls die Berechtigung fehlt
        }
    }
}
