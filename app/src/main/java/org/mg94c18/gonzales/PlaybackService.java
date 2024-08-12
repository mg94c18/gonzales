package org.mg94c18.gonzales;

import static org.mg94c18.gonzales.Logger.LOG_V;
import static org.mg94c18.gonzales.Logger.TAG;
import static org.mg94c18.gonzales.MainActivity.syncIndex;

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
    public static final String ACTION_RESUME = "resume";
    public static final String EXTRA_IDS = "IDs";
    private static final String CHANNEL_ID = "8082e7d3-aa37-482c-8ce5-6004e2709cd7";
    private static final int NOTIFICATION_ID = 42;

    MediaPlayer player;
    private ExecutorService cleanupService;

    public static boolean inForeground = false;

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

    int[] EMPTY_ARRAY = new int[0];
    int[] episodeIdsToPlay = EMPTY_ARRAY;
    int nextIndexToPlay;
    List<String> mp3Links;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        if (BuildConfig.DEBUG) { LOG_V("onCreate()"); }
        createNotificationChannel();
        cleanupService = Executors.newSingleThreadExecutor();
        player = new MediaPlayer();
        state = State.IDLE;
        episodeIdsToPlay = EMPTY_ARRAY;
        nextIndexToPlay = 0;
        mp3Links = AssetLoader.loadFromAssetOrUpdate(this, AssetLoader.MP3LINKS, syncIndex);
    }

    @Override
    public void onDestroy() {
        if (BuildConfig.DEBUG) { LOG_V("onDestroy()"); }
        myStopForeground(true);
        releasePlayerAsync(player);
        player = null;
        state = State.END;

        // Abandoning the service instead of trying to wait for cancelation
        cleanupService.shutdown();
        cleanupService = null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            Log.wtf(TAG, "Null intent");
            return START_NOT_STICKY;
        }

        String action = intent.getAction();
        if (BuildConfig.DEBUG) { LOG_V("onStartCommand(" + flags + "," + startId + "," + action + ")"); }

        if (ACTION_PLAY.equals(action)) {
            onPlayRequested(intent);
        } else if (ACTION_PAUSE.equals(action)) {
            onPauseRequested(intent);
        } else if (ACTION_STOP.equals(action)) {
            onStopRequested();
        } else if (ACTION_RESUME.equals(action)) {
            onResumeRequested(intent);
        } else {
            Log.wtf(TAG, "Unknown action: " + intent.getAction());
        }
        return START_NOT_STICKY;
    }

    private void releasePlayerAsync(MediaPlayer mediaPlayer) {
        cleanupService.submit(new Runnable() {
            @Override
            public void run() {
                mediaPlayer.release();
            }
        });
    }

    private void onPlayRequested(Intent intent) {
        int[] episodeIds = intent.getIntArrayExtra(EXTRA_IDS);
        if (episodeIds == null) {
            Log.wtf(TAG, "Can't find IDs to play");
            return;
        }
        episodeIdsToPlay = episodeIds;
        nextIndexToPlay = 0;
        playNext();
    }

    private void onPauseRequested(Intent intent) {
        if (state != State.STARTED) {
            Log.wtf(TAG, "Unexpected state: " + state);
            return;
        }
        player.pause();
        state = State.PAUSED;
        myStartForeground(false, true);
    }

    private void onResumeRequested(Intent intent) {
        if (state != State.PAUSED) {
            Log.wtf(TAG, "Unexpected state: " + state);
            // TODO: nešto defensive ovde
            return;
        }
        player.start();
        state = State.STARTED;
        myStartForeground(true, false); // TODO: argumenti su sad uvek suprotno jedan drugom, možda mi ne trabaju oba
    }

    private void onStopRequested() {
        episodeIdsToPlay = EMPTY_ARRAY;
        nextIndexToPlay = 0;
        if (state == State.STARTED || state == State.PAUSED) {
            player.stop();
        }
        releasePlayerAsync(player);
        player = new MediaPlayer();
        state = State.IDLE;
        myStopForeground(true);
    }

    private void playNext() {
        if (nextIndexToPlay >= episodeIdsToPlay.length) {
            onStopRequested();
            return;
        }

        releasePlayerAsync(player);
        player = new MediaPlayer();
        state = State.IDLE;
        // TODO: " W  See the documentation of setSound() for what to use instead with android.media.AudioAttributes to qualify your playback use case"
        player.setAudioAttributes(new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_MEDIA).build());
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        String file = new File(getCacheDir(), DownloadAndSave.fileNameFromLink(mp3Links.get(episodeIdsToPlay[nextIndexToPlay]))).getAbsolutePath();
        try {
            player.setDataSource(file);
            state = State.INITIALIZED;
            player.prepareAsync();
            state = State.PREPARING;
            nextIndexToPlay++;
        } catch (IOException e) {
            Log.wtf(TAG, "Can't setDataSource(" + file + ")");
        }
    }

    PendingIntent myBuildServiceIntent(String action) {
        Intent serviceIntent = new Intent(this, PlaybackService.class);
        serviceIntent.setAction(action);
        // TODO: da li je requestCode uopšte bitan ovde?
        return PendingIntent.getService(this, 0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void myStartForeground(boolean currentlyPlaying, boolean update) {
        Intent activityIntent = new Intent(this, MainActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        TaskStackBuilder builder = TaskStackBuilder.create(this);
        builder.addNextIntentWithParentStack(activityIntent); // even though there is no parent in this app
        PendingIntent activityPendingIntent = builder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        int iconId = currentlyPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play;
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Title")
                .setContentText("content text")
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(false)
                .setOnlyAlertOnce(false) // izgleda da ako je ovo true onda se nekad ne vidi u top bar mada je i dalje tu ako se izvuče
                .setContentIntent(activityPendingIntent)
                .setDeleteIntent(myBuildServiceIntent(ACTION_STOP))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0)
                        .setShowCancelButton(false))
                .addAction(new NotificationCompat.Action(
                        iconId,
                        currentlyPlaying ? "Pause" : "Resume",
                        myBuildServiceIntent(currentlyPlaying ? ACTION_PAUSE : ACTION_RESUME)))
                .build();

        if (update) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.notify(NOTIFICATION_ID, notification);
        } else {
            startForeground(NOTIFICATION_ID, notification);
            inForeground = true;
        }

        if (!currentlyPlaying) {
            myStopForeground(false);
        }
    }

    private void myStopForeground(boolean remove) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(remove ? STOP_FOREGROUND_REMOVE : STOP_FOREGROUND_DETACH);
        } else {
            stopForeground(remove);
        }
        if (remove) {
            inForeground = false;
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
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
        if (mediaPlayer != player) {
            Log.wtf(TAG, "Mismatched players, ignoring");
            return;
        }
        state = State.PREPARED;
        player.start();
        state = State.STARTED;
        myStartForeground(true, false);
    }

    @Override
    public void onCompletion(final MediaPlayer mediaPlayer) {
        Log.i(TAG, "onCompletion");
        if (mediaPlayer != player) {
            Log.wtf(TAG, "Mismatched players, ignoring");
            return;
        }
        state = State.PLAYBACK_COMPLETED;
        playNext();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i1, int i2) {
        Log.i(TAG, "onError(" + i1 + "," + i2 + ")");
        if (mediaPlayer != player) {
            Log.wtf(TAG, "Mismatched players, ignoring");
            return false;
        }
        state = State.ERROR;
        onStopRequested();
        return false;
    }
}
