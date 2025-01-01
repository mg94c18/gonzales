package org.mg94c18.gonzales;

import static org.mg94c18.gonzales.Logger.LOG_V;
import static org.mg94c18.gonzales.Logger.TAG;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlaybackService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener {
    public static final String ACTION_PLAY = "play";
    public static final String ACTION_PAUSE = "pause";
    public static final String ACTION_STOP = "stop";
    public static final String ACTION_RESUME = "resume";
    public static final String ACTION_ASSET_UPDATE = "cyrillic";
    public static final String EXTRA_IDS = "IDs";
    public static final String EXTRA_LAST_ID = "last_number";
    public static final String EXTRA_LAST_OFFSET = "last_offset";
    private static final String CHANNEL_ID = "8082e7d3-aa37-482c-8ce5-6004e2709cd7";
    private static final int NOTIFICATION_ID = 42;
    private static final int EXPECTED_MS_TO_SWITCH_SONGS = 5000;
    private static final String COMPONENT_ID = PlaybackService.class.toString();

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
    State state;
    MediaPlayer player;

    PowerManager.WakeLock wakeLock;

    int[] EMPTY_ARRAY = new int[0];
    int[] episodeIdsToPlay = EMPTY_ARRAY;
    int nextIndexToPlay;
    int nextOffset;

    int lastOffset;
    String lastNumber;

    List<String> numbers;
    List<String> titles;
    List<String> authors;

    private static class ReceiverInfo {
        private final IntentFilter filter;
        private final BroadcastReceiver receiver;
        private boolean registered;

        public ReceiverInfo(IntentFilter filter, BroadcastReceiver receiver) {
            this.filter = filter;
            this.receiver = receiver;
            this.registered = false;
        }

        @SuppressLint("UnspecifiedRegisterReceiverFlag") // OK because we supply flag on >= 34
        public void register(Context context) {
            if (registered) {
                return;
            }
            // Ovde ima crash na SDK=34; zaista, broadcast_actions.txt nema nijedan od mojih filtera, mada sam mislio da oni jesu "system broadcasts"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                context.registerReceiver(receiver, filter, RECEIVER_NOT_EXPORTED);
            } else {
                context.registerReceiver(receiver, filter);
            }
            registered = true;
        }

        public void unregister(Context context) {
            if (!registered) {
                return;
            }
            try {
                context.unregisterReceiver(receiver);
            } catch (IllegalArgumentException iae) {
                Log.wtf(TAG, "Unexpected, we were registered", iae);
            }
            registered = false;
        }
    }

    private final ReceiverInfo becomingNoisy = new ReceiverInfo(
            new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY),
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                        onPauseRequested();
                    }
                }
            });

    private final ReceiverInfo mediaButton = new ReceiverInfo(
            new IntentFilter(Intent.ACTION_MEDIA_BUTTON),
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    handleMediaButtonIntent(intent);
                }
            });

    private boolean handleMediaButtonIntent(Intent intent) {
        if (intent == null || intent.getExtras() == null) {
            Log.wtf(TAG, "Null intent or extras");
            return false;
        }

        if (!intent.hasExtra(Intent.EXTRA_KEY_EVENT)) {
            Log.e(TAG, "Doesn't have KeyEvent");
            return false;
        }

        KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
        if (keyEvent.getKeyCode() != KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
            Log.w(TAG, "Unexpected keyCode: " + keyEvent.getKeyCode());
            return false;
        }

        if (keyEvent.getAction() != KeyEvent.ACTION_UP) {
            Log.i(TAG, "KeyEvent received but not done yet");
            return true;
        }

        if ((keyEvent.getFlags() & KeyEvent.FLAG_CANCELED) != 0) {
            Log.i(TAG, "KeyEvent got canceled");
            return false;
        }

        if (state == State.STARTED) {
            onPauseRequested();
        } else if (state == State.PAUSED) {
            onResumeRequested();
        } else {
            Log.wtf(TAG, "Invalid state: " + state);
            return false;
        }

        return true;
    }

    MediaSession mediaSession;

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
        lastOffset = 0;
        nextOffset = 0;
        AssetLoader.handleAssetLoading(this);
        numbers = MainActivity.numbers;
        titles = MainActivity.titles;
        authors = MainActivity.dates;
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, COMPONENT_ID);
        wakeLock.setReferenceCounted(false);
        mediaSession = new MediaSession(this, COMPONENT_ID);
        mediaSession.setCallback(new MediaSession.Callback() {
            @Override
            public boolean onMediaButtonEvent(@NonNull Intent intent) {
                return handleMediaButtonIntent(intent);
            }
        });
    }

    @Override
    public void onDestroy() {
        if (BuildConfig.DEBUG) { LOG_V("onDestroy()"); }
        myDestroy();
        wakeLock.release();
        mediaSession.release();
    }

    void saveOrInvalidateState() {
        SharedPreferences preferences = MainActivity.getSharedPreferences(this);
        if (state == State.PAUSED) {
            preferences.edit()
                    .putString(MainActivity.PLAYLIST_TRACK, lastNumber)
                    .putInt(MainActivity.PLAYLIST_TRACK_OFFSET, player.getCurrentPosition())
                    .apply();
        } else {
            preferences.edit().remove(MainActivity.PLAYLIST_TRACK).apply();
        }
    }

    void myDestroy() {
        saveOrInvalidateState();
        myStopForeground(true);
        releasePlayerAsync(player);
        player = null;
        state = State.END;

        // Abandoning the service instead of trying to wait for cancelation
        if (cleanupService != null) {
            cleanupService.shutdown();
            cleanupService = null;
        }
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
            onPauseRequested();
        } else if (ACTION_STOP.equals(action)) {
            onStopRequested();
        } else if (ACTION_RESUME.equals(action)) {
            onResumeRequested();
        } else if (ACTION_ASSET_UPDATE.equals(action)) {
            onCyrillicChanged();
        } else {
            Log.wtf(TAG, "Unknown action: " + intent.getAction());
        }
        return START_NOT_STICKY;
    }

    private void releasePlayerAsync(MediaPlayer mediaPlayer) {
        if (mediaPlayer == null) {
            return;
        }
        if (cleanupService == null) {
            return;
        }
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
        nextIndexToPlay = intent.getIntExtra(EXTRA_LAST_ID, 0);
        nextOffset = intent.getIntExtra(EXTRA_LAST_OFFSET, 0);
        playNext();
    }

    private void onPauseRequested() {
        if (state != State.STARTED) {
            Log.wtf(TAG, "Unexpected state: " + state);
            return;
        }
        player.pause();
        state = State.PAUSED;
        myStartForeground(false, true);
        saveOrInvalidateState();
    }

    private static int[] episodeIdsFromNumbers(Set<String> numberSet, List<String> numbers) {
        int[] episodeIds = new int[numberSet.size()];
        int addedCount = 0;
        for (String number : numberSet) {
            int index = numbers.indexOf(number);
            if (index < 0) {
                Log.w(TAG, "Skipping over '" + number + "', not available anymore");
                continue;
            }
            episodeIds[addedCount++] = index;
        }
        int[] copy = Arrays.copyOf(episodeIds, addedCount);
        Arrays.sort(copy);
        return copy;
    }

    private void onResumeRequested() {
        if (state != State.PAUSED) {
            if (state != State.IDLE) {
                Log.wtf(TAG, "Unexpected state: " + state + ", will self-destruct");
                myDestroy();
                return;
            }

            Intent intent = new Intent();
            if (populateRecoveryIntent(intent, numbers, this)) {
                Log.i(TAG, "Successfully recovered the playback");
                // Defensively remove the state; one recovery attempt is enough
                MainActivity.getSharedPreferences(this).edit().remove(MainActivity.PLAYLIST_TRACK).apply();
                onPlayRequested(intent);
            } else {
                Log.wtf(TAG, "Asked to resume, but not playing and can't recover; will self-destruct");
                myDestroy();
            }
            return;
        }
        player.start();
        state = State.STARTED;
        myStartForeground(true, false); // TODO: argumenti su sad uvek suprotno jedan drugom, možda mi ne trabaju oba
    }

    private void onCyrillicChanged() {
        titles = MainActivity.titles;
        authors = MainActivity.dates;
        boolean playing = (state == State.STARTED);
        myStartForeground(playing, !playing);
    }

    private static boolean populateRecoveryIntent(Intent intent, List<String> numbers, Context context) {
        SharedPreferences preferences = MainActivity.getSharedPreferences(context);
        Set<String> playlist = preferences.getStringSet(MainActivity.PLAYLIST_EPISODES_SET, Collections.emptySet());
        Log.i(TAG, "Playlist to resume: " + playlist);
        int[] episodeIds = episodeIdsFromNumbers(playlist, numbers);

        intent.putExtra(EXTRA_IDS, episodeIds);
        String lastTrack = preferences.getString(MainActivity.PLAYLIST_TRACK, null);
        if (lastTrack == null) {
            Log.wtf(TAG, "Can't find last track");
            return false;
        }

        int lastId = -1;
        for (int i = 0; i < episodeIds.length; i++) {
            if (numbers.get(episodeIds[i]).equals(lastTrack)) {
                lastId = i;
                break;
            }
        }
        if (lastId == -1) {
            Log.wtf(TAG, "Last track got removed");
            return false;
        }

        intent.putExtra(EXTRA_LAST_ID, lastId);
        int savedOffset = preferences.getInt(MainActivity.PLAYLIST_TRACK_OFFSET, 0);
        intent.putExtra(EXTRA_LAST_OFFSET, savedOffset);
        Log.i(TAG, "lastId=" + lastId + ", savedOffset=" + savedOffset);
        return true;
    }

    private void onStopRequested() {
        episodeIdsToPlay = EMPTY_ARRAY;
        nextIndexToPlay = 0;
        if (state == State.STARTED || state == State.PAUSED) {
            player.stop();
            state = State.STOPPED;
        }
        releasePlayerAsync(player);
        player = new MediaPlayer();
        state = State.IDLE;
        myStopForeground(true);
        wakeLock.release();
    }

    private void playNext() {
        if (nextIndexToPlay >= episodeIdsToPlay.length) {
            onStopRequested();
            return;
        }

        wakeLock.acquire(EXPECTED_MS_TO_SWITCH_SONGS);
        Log.i(TAG, "wakeLock.acquire() called");
        releasePlayerAsync(player);
        player = new MediaPlayer();
        state = State.IDLE;
        // TODO: " W  See the documentation of setSound() for what to use instead with android.media.AudioAttributes to qualify your playback use case"
        player.setAudioAttributes(new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_MEDIA).build());
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        player.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
        lastNumber = numbers.get(episodeIdsToPlay[nextIndexToPlay]);
        String file = new File(ExternalStorageHelper.getMyCacheDir(this), DownloadAndSave.fileNameFromNumber(lastNumber)).getAbsolutePath();
        try {
            player.setDataSource(file);
            state = State.INITIALIZED;
            player.prepareAsync();
            state = State.PREPARING;
            nextIndexToPlay++;
            lastOffset = nextOffset;
            nextOffset = 0;
        } catch (IOException e) {
            Log.wtf(TAG, "Can't setDataSource(" + file + ")", e);
        }
    }

    PendingIntent myBuildServiceIntent(String action) {
        Intent serviceIntent = new Intent(this, PlaybackService.class);
        serviceIntent.setAction(action);
        // TODO: da li je requestCode uopšte bitan ovde?
        return PendingIntent.getService(this, 0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private void myStartForeground(boolean currentlyPlaying, boolean update) {
        Intent activityIntent = new Intent(this, MainActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        int actualIndex = episodeIdsToPlay[nextIndexToPlay - 1];
        activityIntent.setAction(MainActivity.MY_ACTION_VIEW);
        MainActivity.updateIntentWithEpisode(activityIntent, actualIndex, titles.get(actualIndex), authors.get(actualIndex), numbers.get(actualIndex));

        TaskStackBuilder builder = TaskStackBuilder.create(this);
        builder.addNextIntentWithParentStack(activityIntent); // even though there is no parent in this app
        PendingIntent activityPendingIntent = builder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        int iconId = currentlyPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play;
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getCurrentTitle())
                .setContentText("" + nextIndexToPlay + "/" + episodeIdsToPlay.length)
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(false)
                .setOnlyAlertOnce(false) // izgleda da ako je ovo true onda se nekad ne vidi u top bar mada je i dalje tu ako se izvuče
                .setContentIntent(activityPendingIntent)
                .setDeleteIntent(myBuildServiceIntent(ACTION_STOP))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
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

        mediaButton.register(this);
        mediaSession.setActive(true);
        if (currentlyPlaying) {
            becomingNoisy.register(this);
        } else {
            myStopForeground(false);
        }
    }

    private String getCurrentTitle() {
        int i = nextIndexToPlay - 1;
        if (i >= 0 && i < episodeIdsToPlay.length) {
            return titles.get(episodeIdsToPlay[i]);
        } else {
            Log.wtf(TAG, "Can't figure out the title: " + nextIndexToPlay + "/" + episodeIdsToPlay.length);
            return "Title";
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
        becomingNoisy.unregister(this);
        mediaButton.unregister(this);
        mediaSession.setActive(false);
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
            wakeLock.release();
            return;
        }
        state = State.PREPARED;
        if (lastOffset > 0) {
            player.setOnSeekCompleteListener(this);
            player.seekTo(lastOffset);
        } else {
            onSeekComplete(player);
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        if (mediaPlayer != player) {
            Log.wtf(TAG, "Invalid player seeked, probably terminating");
            wakeLock.release();
            return;
        }

        player.start();
        state = State.STARTED;
        myStartForeground(true, false);
        wakeLock.release();
    }

    @Override
    public void onCompletion(final MediaPlayer mediaPlayer) {
        Log.i(TAG, "onCompletion");
        if (mediaPlayer != player) {
            // TODO: ovde verovatno dođe ako u onError vratim false, probati ovo
            Log.wtf(TAG, "Mismatched players, ignoring");
            wakeLock.release();
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
            wakeLock.release();
            return false;
        }
        state = State.ERROR;
        onStopRequested();
        return false;
    }
}
