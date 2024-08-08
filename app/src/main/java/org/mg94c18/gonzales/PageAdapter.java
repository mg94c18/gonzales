package org.mg94c18.gonzales;

import static org.mg94c18.gonzales.Logger.LOG_V;
import static org.mg94c18.gonzales.Logger.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class PageAdapter implements View.OnTouchListener, ScaleGestureDetector.OnScaleGestureListener, View.OnClickListener,
    MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    List<String> links;
    String episode;
    String localFilePath;
    Context context;
    Button button;
    private WebView webView;
    MyLoadTask loadTask;

    private static final ExecutorService cleanupService = Executors.newSingleThreadExecutor();

    private static final int SCALE_MAX_X_INT = 2;
    private static final int MINIMUM_ZOOM = 100;
    int originalZoom;
    private float beginScaleFactor;
    ScaleGestureDetector mScaleDetector;
    private boolean scaleInProgress = false;

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
    MediaPlayer mediaPlayer;
    State playerState;
    private static final String PLAY_START = "Play";

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.i(TAG, "onCompletion()");
        if (mediaPlayer == null) {
            Log.w(TAG, "Got a callback (soon?) after destroying or error");
            return;
        }
        playerState = State.PLAYBACK_COMPLETED;
        button.setText(PLAY_START);
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        Log.i(TAG, "onError()");
        playerState = State.ERROR;
        if (mediaPlayer == null) {
            Log.w(TAG, "Got a callback (soon?) after destroying");
            return false;
        }
        mediaPlayer.release();
        mediaPlayer = null;
        button.setEnabled(false);
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.i(TAG, "onPrepared()");
        if (mediaPlayer == null) {
            Log.w(TAG, "Got a callback (soon?) after destroying");
            return;
        }
        playerState = State.PREPARED;
        button.setEnabled(true);
    }

    PageAdapter(MainActivity activity, String episode) {
        if (BuildConfig.DEBUG) { LOG_V("PageAdapter(" + episode + ")"); }

        this.context = activity;
        this.episode = episode;
        links = AssetLoader.loadFromAssetOrUpdate(context, episode, MainActivity.syncIndex);

        mScaleDetector = new ScaleGestureDetector(context, this);

        webView = activity.findViewById(R.id.webview);
        button = activity.findViewById(R.id.button);
        button.setOnClickListener(this);
        button.setText("Play");
        button.setEnabled(false);

        updateScaleFromPrefs(context, webView);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.loadData(createHtml(links, true, false), "text/html", "UTF-8");
            }
        });

        ProgressBar progressBar = activity.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        webView.setTag(progressBar);
        webView.setOnTouchListener(this);

        loadTask = new MyLoadTask(links, this, DownloadAndSave.fileNameFromLink(links.get(0), episode, 0));
        loadTask.execute();
    }

    public void destroy() {
        if (BuildConfig.DEBUG) { LOG_V("destroy(" + episode + ")"); }
        loadTask.cancel(true);
        loadTask.parentRef.clear();
        if (mediaPlayer != null) {
            cleanupService.submit(new Runnable() {
                @Override
                public void run() {
                    mediaPlayer.release();
                }
            });
        }
        button.setEnabled(false);
    }

    private static String createHtml(List<String> links, boolean hints, boolean a3byka) {
        StringBuilder builder = new StringBuilder();

        builder.append("<html><head><title></title></head><body><p><br>");
        for (int i = 2; i < links.size(); i++) {
            builder.append(applyFilters(links.get(i), hints, a3byka)).append("<br>");
        }
        builder.append("</body></head></html>");
        return builder.toString();
    }

    private static final String hintsChars = "\\\\";
    private static final Pattern nuggetsPattern = Pattern.compile(hintsChars + "(r|ás|as|o|ó|go|ste|iendo)");
    private static final Pattern hintsPattern = Pattern.compile(hintsChars);
    private static String applyFilters(String line, boolean hints, boolean a3byka) {
        if (hints) {
            line = nuggetsPattern.matcher(line).replaceAll("<ins>$1</ins>");
            if (BuildConfig.DEBUG) {
                String oldLine = line;
                line = hintsPattern.matcher(oldLine).replaceAll("");
                if (oldLine.compareTo(line) != 0) {
                    Log.wtf(TAG, "There was a remaining hint: old=" + oldLine + ", " + "new=" + line);
                }
            }
        }
        line = hintsPattern.matcher(line).replaceAll("");
        return line;
    }

    private static int normalizeZoom(int currentZoom) {
        if (currentZoom < MINIMUM_ZOOM) {
            Log.wtf(TAG, "Invalid scale: " + currentZoom);
            return MINIMUM_ZOOM;
        } else if (currentZoom > MINIMUM_ZOOM * SCALE_MAX_X_INT) {
            Log.wtf(TAG, "Invalid scale: " + currentZoom);
            return MINIMUM_ZOOM * SCALE_MAX_X_INT;
        } else {
            return currentZoom;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        if (BuildConfig.DEBUG) { LOG_V("onTouch(" + action + ")"); }
        return mScaleDetector.onTouchEvent(motionEvent) && scaleInProgress;
    }

    @Override
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        return false;
    }

    private String getScaleKey() {
        // TODO: ovde dodati orijentaciju
        String orientation = "portrait";
        String key = orientation + ".zoom";
        return key;
    }

    private void updateScaleFromPrefs(Context context, @Nullable WebView webView) {
        if (webView == null) {
            return;
        }
        SharedPreferences preferences = MainActivity.getSharedPreferences(context);
        int scaleX = normalizeZoom(preferences.getInt(getScaleKey(), MINIMUM_ZOOM + 30));
        if (scaleX != webView.getSettings().getTextZoom()) {
            if (BuildConfig.DEBUG) { LOG_V("updateScaleFromPrefs(" + scaleX + ")"); }
            webView.getSettings().setTextZoom(scaleX);
            webView.invalidate();
        }
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        if (BuildConfig.DEBUG) { LOG_V("Scaling: onScaleBegin"); }
        beginScaleFactor = mScaleDetector.getScaleFactor();
        originalZoom = normalizeZoom(webView.getSettings().getTextZoom());
        scaleInProgress = true;
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
        if (BuildConfig.DEBUG) { LOG_V("Scaling: onScaleEnd"); }
        scaleInProgress = false;
        float endScaleFactor = mScaleDetector.getScaleFactor();

        final int newZoom;
        int comparison = Float.compare(endScaleFactor, beginScaleFactor);
        if (comparison == 0) {
            return;
        }
        if (comparison < 0) {
            newZoom = originalZoom - 10;
        } else {
            newZoom = originalZoom + 10;
        }

        SharedPreferences preferences = MainActivity.getSharedPreferences(context);
        preferences.edit()
                .putInt(getScaleKey(), newZoom)
                .apply();
        updateScaleFromPrefs(context, webView);
    }

    @Override
    public void onClick(View view) {
        Log.i(TAG, "onClick");
        if (localFilePath == null) {
            Log.wtf(TAG, "Should never happen");
            return;
        }

        if (playerState == State.STARTED) {
            mediaPlayer.pause();
            playerState = State.PAUSED;
            button.setText("Resume");
        } else if (playerState == State.PAUSED || playerState == State.PREPARED) {
            mediaPlayer.start();
            playerState = State.STARTED;
            button.setText("Pause");
        } else {
            Log.wtf(TAG, "Unexpected state: " + playerState);
        }
    }

    private static class MyLoadTask extends AsyncTask<Void, Void, String> {
        String imageFile;
        WeakReference<PageAdapter> parentRef;
        List<String> links;

        MyLoadTask(List<String> links, PageAdapter parent, String imageFile) {
            this.imageFile = imageFile;
            this.links = links;
            this.parentRef = new WeakReference<>(parent);
        }

        @Override
        protected String doInBackground(Void[] voids) {
            if (BuildConfig.DEBUG) { LOG_V("doInBackground(" + imageFile + ")"); }
            String savedBitmap = null;
            PageAdapter parent = parentRef.get();
            if (parent == null) {
                return null;
            }
            List<File> cacheDirs = new ArrayList<>();

            // TODO: ne znam zašto ovo već nisam imao.  Treba popraviti download ali zasad mogu da koristim ovo
            cacheDirs.add(parent.context.getCacheDir());
            for (File cacheDir : cacheDirs) {
                if (cacheDir == null) {
                    continue;
                }
                if (savedBitmap != null) {
                    break;
                }
                File savedImage = new File(cacheDir, imageFile);
                if (BuildConfig.DEBUG) { LOG_V("Trying " + savedImage.getAbsolutePath()); }
                if (savedImage.exists()) {
                    if (BuildConfig.DEBUG) { LOG_V("The file " + imageFile + " exists."); }
                    if (isCancelled()) {
                        return null;
                    } else {
                        if (BuildConfig.DEBUG) { LOG_V("Decoding image from " + imageFile); }
                        savedBitmap = mp3HelperVerifyFile(savedImage.getAbsolutePath());
                    }
                }
            }
            if (savedBitmap != null) {
                return savedBitmap;
            }

            if (MainActivity.internetNotAvailable(parent.context)) {
                return null;
            }

            File imageToDownload = new File(parent.context.getCacheDir(), imageFile);
            if (isCancelled()) {
                return null;
            } else {
                return DownloadAndSave.downloadAndSave(links.get(0), imageToDownload, 3);
            }
        }

        private String mp3HelperVerifyFile(String absolutePath) {
            // TODO: provera
            return absolutePath;
        }

        @Override
        protected void onPostExecute(String localFile) {
            PageAdapter parent = parentRef.get();
            if (parent == null) {
                return;
            }
            ProgressBar progressBar = (ProgressBar) parent.webView.getTag();
            try {
                if (BuildConfig.DEBUG) { LOG_V("onPostExecute(" + imageFile + "," + localFile + ")"); }
                if (localFile == null && !isCancelled()) {
                    return;
                }
                if (localFile == null) {
                    Log.e(TAG, "localFile=null, likely a download problem");
                    parent.button.setEnabled(false);
                    return;
                }
                parent.onDownloaded(localFile);
            } finally {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        }

        @Override
        protected void onCancelled(String b) {
            if (BuildConfig.DEBUG) { LOG_V("onCancelled(" + imageFile + ")"); }
            onPostExecute(null);
        }
    }

    private void onDownloaded(String localFile) {
        if (mediaPlayer != null) {
            Log.wtf(TAG, "Unexpected, we already have mediaPlayer");
            return;
        }
        localFilePath = localFile;
        mediaPlayer = new MediaPlayer();
        playerState = State.IDLE;
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        try {
            mediaPlayer.setDataSource(localFilePath);
            playerState = State.INITIALIZED;
            mediaPlayer.prepareAsync();
            playerState = State.PREPARING;
        } catch (IOException e) {
            Log.wtf(TAG, "Can't setDataSource(" + localFilePath + ")");
        }
    }
}
