package org.mg94c18.gonzales;

import static org.mg94c18.gonzales.Logger.LOG_V;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

public class PlaybackService extends Service {
    public static final String ACTION_PLAY = "play";
    private static final String CHANNEL_ID = "022f94de-8383-44e8-b52e-be67a2307044";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        if (BuildConfig.DEBUG) { LOG_V("onCreate()"); }
    }

    @Override
    public void onDestroy() {
        if (BuildConfig.DEBUG) { LOG_V("onDestroy()"); }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (BuildConfig.DEBUG) { LOG_V("onStartCommand(" + flags + "," + startId + ")"); }
        Intent activityIntent = new Intent(this, PlaybackService.class);
        TaskStackBuilder builder = TaskStackBuilder.create(this);
        builder.addNextIntent(activityIntent);
        PendingIntent pendingIntent = builder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        createNotificationChannel();

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Title")
                .setContentText("content text")
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(false)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system. You can't change the importance
            // or other notification behaviors after this.
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
