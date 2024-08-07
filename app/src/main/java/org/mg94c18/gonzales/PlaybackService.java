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

public class PlaybackService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    public static final String ACTION_PLAY = "play";
    public static final String ACTION_PAUSE = "pause";
    public static final String ACTION_STOP = "stop";
    public static final String ACTION_NOTIFICATION = "notification";
    public static final String EXTRA_FILE = "file";
    private static final String CHANNEL_ID = "022f94de-8383-44e8-b52e-be67a2307044";
    private static final int NOTIFICATION_ID = 42;

    private Map<String, MediaPlayer> players;
    private ExecutorService executorService;
    private Set<Future<Boolean>> activeFutures;

    // https://developer.android.com/static/images/mediaplayer_state_diagram.gif
    enum State {
        IDLE,
        INITIALIZED,
        PREPARING,
        PREPARED,
        STARTED,
        PAUSED,
        STOPPED,
        PLAYBACK_COMPLETED,
        ERROR,
        END
    }
    // TODO: state za "svaki" MediaPlayer
    State state;

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
        executorService = Executors.newSingleThreadExecutor();
        activeFutures = new HashSet<>();
    }

    @Override
    public void onDestroy() {
        if (BuildConfig.DEBUG) { LOG_V("onDestroy()"); }
        for (Map.Entry<String, MediaPlayer> entry : players.entrySet()) {
            entry.getValue().release();
        }
        players.clear();
        myStopForeground(true);
        executorService.shutdown();
        for (Future<Boolean> future : activeFutures) {
            if (!future.isCancelled()) {
                future.cancel(true);
            }
        }
        // Abandoning the service instead of trying to wait for cancelation
        executorService = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            Log.wtf(TAG, "Null intent");
            return START_NOT_STICKY;
        }

        if (BuildConfig.DEBUG) { LOG_V("onStartCommand(" + flags + "," + startId + "," + intent.getAction() + ")"); }

        if (ACTION_PLAY.equals(intent.getAction())) {
            onPlayRequested(intent);
        } else if (ACTION_PAUSE.equals(intent.getAction())) {
            onPauseRequested(intent);
        } else if (ACTION_STOP.equals(intent.getAction())) {
            onStopRequested(intent);
        } else if (ACTION_NOTIFICATION.equals(intent.getAction())) {
            onNotification(intent);
        } else {
            Log.wtf(TAG, "Unknown action: " + intent.getAction());
        }
        return START_NOT_STICKY;
    }

    private void onNotification(Intent intent) {
        if (players.size() > 1) {
            Log.wtf(TAG, "Unexpected state: releasing all " + players.size() + " players");
            for (Map.Entry<String, MediaPlayer> entry : players.entrySet()) {
                entry.getValue().release();
            }
            players.clear();
        }
        if (players.size() == 0) {
            Log.wtf(TAG, "Unexpected state: no players");
            return;
        }

        Map.Entry<String, MediaPlayer> elvis = players.entrySet().iterator().next();
        if (state == State.STARTED) {
            elvis.getValue().pause();
            state = State.PAUSED;
            startForeground("Resume", true);
            myStopForeground(false);
        } else if (state == State.PAUSED) {
            elvis.getValue().start();
            state = State.STARTED;
            startForeground("Pause", false);
        } else {
            Log.wtf(TAG, "Unexpected state: " + state);
        }
    }

    private void onStopRequested(Intent intent) {
        activeFutures.add(executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                String file = intent.getStringExtra(EXTRA_FILE);
                MediaPlayer player = players.get(file);
                if (player == null) {
                    Log.wtf(TAG, "Can't find the player to stop");
                    return null;
                }
                return true;
            }
        }));
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
        state = State.IDLE;
        player.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        try {
            player.setDataSource(file);
            state = State.INITIALIZED;
            player.prepareAsync();
            state = State.PREPARING;
            players.put(file, player);
        } catch (IOException e) {
            Log.wtf(TAG, "Can't setDataSource(" + file + ")");
        }
    }

    private void startForeground(String actionText, boolean update) {
        Intent activityIntent = new Intent(this, MainActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        TaskStackBuilder builder = TaskStackBuilder.create(this);
        builder.addNextIntentWithParentStack(activityIntent); // even though there is no parent in this app
        PendingIntent activityPendingIntent = builder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent serviceIntent = new Intent(this, PlaybackService.class);
        serviceIntent.setAction(ACTION_NOTIFICATION);
        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Title")
                .setContentText("content text")
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .setContentIntent(activityPendingIntent)
                .addAction(R.drawable.ic_launcher, actionText, servicePendingIntent)
                .build();

        if (update) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.notify(NOTIFICATION_ID, notification);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    private void myStopForeground(boolean remove) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(remove ? STOP_FOREGROUND_REMOVE : STOP_FOREGROUND_DETACH);
        } else {
            stopForeground(remove);
        }
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
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.i(TAG, "onPrepared()");
        state = State.PREPARED;
        activeFutures.add(executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if (BuildConfig.DEBUG) { LOG_V("play()"); }
                mediaPlayer.start();
                state = State.STARTED;
                startForeground("Pause", false);
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
        state = State.PLAYBACK_COMPLETED;
        activeFutures.add(executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                releasePlayer(mediaPlayer);
                myStopForeground(true);
                return null;
            }
        }));
    }

    private void releasePlayer(MediaPlayer mediaPlayer) {
        if (BuildConfig.DEBUG) { LOG_V("release()"); }
        mediaPlayer.release();
        String file = findFileByPlayer(mediaPlayer);
        if (file == null) {
            Log.wtf(TAG, "Already released");
        } else {
            players.remove(file);
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        state = State.ERROR;
        activeFutures.add(executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                releasePlayer(mediaPlayer);
                myStopForeground(true);
                return null;
            }
        }));
        return false;
    }
}
