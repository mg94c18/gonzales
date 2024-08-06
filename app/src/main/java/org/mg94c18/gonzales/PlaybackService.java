package org.mg94c18.gonzales;

import static org.mg94c18.gonzales.Logger.LOG_V;
import static org.mg94c18.gonzales.Logger.TAG;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PlaybackService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    public static final String ACTION_PLAY = "play";
    public static final String ACTION_PAUSE = "pause";
    public static final String ACTION_STOP = "stop";
    public static final String EXTRA_FILE = "file";
    private static final String CHANNEL_ID = "022f94de-8383-44e8-b52e-be67a2307044";

    private Map<String, MediaPlayer> players;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Set<Future<Boolean>> activeFutures = new HashSet<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        if (BuildConfig.DEBUG) { LOG_V("onCreate()"); }
        createNotificationChannel();
        players = new HashMap<>();
    }

    @Override
    public void onDestroy() {
        if (BuildConfig.DEBUG) { LOG_V("onDestroy()"); }
        for (Map.Entry<String, MediaPlayer> entry : players.entrySet()) {
            entry.getValue().release();
        }
        players.clear();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            Log.wtf(TAG, "Null intent");
            return START_NOT_STICKY;
        }

        if (BuildConfig.DEBUG) { LOG_V("onStartCommand(" + flags + "," + startId + ")"); }

        if (ACTION_PLAY.equals(intent.getAction())) {
            onPlayRequested(intent);
            startForeground();
        } else if (ACTION_PAUSE.equals(intent.getAction())) {
            onPauseRequested(intent);
        } else if (ACTION_STOP.equals(intent.getAction())) {
            onStopRequested(intent);
        }
        return START_NOT_STICKY;
    }

    private void onStopRequested(Intent intent) {
        String file = intent.getStringExtra(EXTRA_FILE);
        MediaPlayer player = players.get(file);
        if (player == null) {
            Log.wtf(TAG, "Can't find the player to stop");
        }
    }

    private void onPauseRequested(Intent intent) {
        String file = intent.getStringExtra(EXTRA_FILE);
        MediaPlayer player = players.get(file);
        if (player == null) {
            Log.wtf(TAG, "Can't find the player to stop");
        }
    }

    private void onPlayRequested(Intent intent) {
        final String file = intent.getStringExtra(EXTRA_FILE);
        MediaPlayer player = players.get(file);
        if (player != null) {
            Log.wtf(TAG, "Already playing");
            // TODO: handle this
            return;
        }
        player = new MediaPlayer();
        player.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        try {
            player.setDataSource(file);
            player.prepareAsync();
        } catch (IOException e) {
            Log.wtf(TAG, "Can't setDataSource(" + file + ")");
            return;
        }
        players.put(file, player);
    }

    private void startForeground() {
        Intent activityIntent = new Intent(this, PlaybackService.class);
        TaskStackBuilder builder = TaskStackBuilder.create(this);
        builder.addNextIntent(activityIntent);
        PendingIntent pendingIntent = builder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Title")
                .setContentText("content text")
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(false)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
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

    @Override
    public void onPrepared(final MediaPlayer mediaPlayer) {
        Log.i(TAG, "onPrepared()");
        activeFutures.add(executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if (BuildConfig.DEBUG) { LOG_V("play()"); }
                mediaPlayer.start();
                return null;
            }
        }));
    }

    String findFileByPlayer(MediaPlayer player) {
        for (Map.Entry<String, MediaPlayer> entry : players.entrySet()) {
            if (entry.getValue() == player) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    public void onCompletion(final MediaPlayer mediaPlayer) {
        Log.i(TAG, "onCompletion");
        activeFutures.add(executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if (BuildConfig.DEBUG) { LOG_V("release()"); }
                mediaPlayer.release();
                String file = findFileByPlayer(mediaPlayer);
                if (file == null) {
                    Log.wtf(TAG, "Already removed");
                } else {
                    players.remove(file);
                }
                return null;
            }
        }));
    }
}
