package org.mg94c18.gonzales;

import static org.mg94c18.gonzales.Logger.LOG_V;
import static org.mg94c18.gonzales.Logger.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import android.os.Build;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class PageAdapter implements View.OnTouchListener, ScaleGestureDetector.OnScaleGestureListener, View.OnClickListener,
    MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    List<String> links;
    List<String> bukvalno;
    List<String> finalno;
    List<String> zaPrikaz;
    String episode;
    String author;
    String searchedWord;
    String localFilePath;
    Context context;
    Button button;
    private WebView webView;
    private boolean inLandscape;
    private int currentWidth;
    MyLoadTask loadTask;

    private static final ExecutorService cleanupService = Executors.newSingleThreadExecutor();

    private String PREF_BUKVALNO = "bukvalno";
    private static final int MINIMUM_ZOOM_P = 4;
    private static final int MAXIMUM_ZOOM_P = 8;
    private static final int MINIMUM_ZOOM_L = 2;
    private static final int MAXIMUM_ZOOM_L = 5;
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
        if (!PlaybackService.inForeground) {
            button.setEnabled(true);
        }
        if (inLandscape) {
            button.setVisibility(View.GONE);
        }
    }

    PageAdapter(MainActivity activity, String episode, String author, String searchedWord) {
        if (BuildConfig.DEBUG) { LOG_V("PageAdapter(" + episode + ")"); }

        if (explicits == null) {
            explicits = new HashMap<>();
            explicits.put(Pattern.compile("((f)uck)", Pattern.CASE_INSENSITIVE), "***");
            explicits.put(Pattern.compile("((d)ick)", Pattern.CASE_INSENSITIVE), "***");
            explicits.put(Pattern.compile("((c)unt)", Pattern.CASE_INSENSITIVE), "***");
            // explicits.put(Pattern.compile("(([Ss])hit)"), "***");
        }

        this.context = activity;
        this.episode = episode;
        this.author = author;
        this.searchedWord = searchedWord;
        SharedPreferences preferences = MainActivity.getSharedPreferences(context);
        links = AssetLoader.loadFromAssetOrUpdate(context, episode, MainActivity.syncIndex);
        bukvalno = AssetLoader.loadFromAssetOrUpdate(context, episode + ".bukvalno", MainActivity.syncIndex);
        finalno = AssetLoader.loadFromAssetOrUpdate(context, episode + ".finalno", MainActivity.syncIndex);
        zaPrikaz = preferences.getBoolean(PREF_BUKVALNO, true) ? bukvalno : finalno;

        mScaleDetector = new ScaleGestureDetector(context, this);

        webView = activity.findViewById(R.id.webview);
        webView.getSettings().setAllowFileAccess(true);
        inLandscape = activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        button = activity.findViewById(R.id.button);
        if (inLandscape) {
            button.setVisibility(View.GONE);
        } else {
            button.setOnClickListener(this);
            button.setText("Play");
            button.setEnabled(false);
            // Google Play ne voli da ima dugme.  Za sad ovako, a posle da obrišem kompletno.
            button.setVisibility(View.GONE);
        }

        if (inLandscape) {
            currentWidth = 3;
        } else {
            currentWidth = 5;
        }
        updateScaleFromPrefs(context, webView);
        refreshWebView();

        ProgressBar progressBar = activity.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        webView.setTag(progressBar);
        webView.setOnTouchListener(this);

        loadTask = new MyLoadTask(links, this, DownloadAndSave.fileNameFromNumber(episode));
        loadTask.execute();
    }

    private void refreshWebView() {
        refreshWebView(false);
    }

    private void refreshWebView(boolean restoreScroll) {
        // TODO (manji prioritet): ako nema oba prevoda, ne treba da radi dugme...
        // TODO (veći prioritet): iskoristiti ActionBar kao deo lekcije, a da ime pesme bude unutar HTML
        if (restoreScroll && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setRestoreScrollListener();
        }
        webView.loadDataWithBaseURL("file:///android_asset/", createHtml(links, zaPrikaz, zaPrikaz == finalno, author, false, inLandscape, searchedWord, currentWidth), "text/html", "UTF-8", null);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setRestoreScrollListener() {
        int oldHeight = webView.getContentHeight();
        webView.setOnScrollChangeListener((view, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            int newHeight = webView.getContentHeight();
            int newScrollY = (int) Math.floor(oldScrollY * newHeight * 1.0 / oldHeight);
            Log.i(TAG, "oldHeight=" + oldHeight + ",oldScrollY=" + oldScrollY + "," + "newHeight=" + newHeight + ",newY=" + newScrollY);
            if (newScrollY > 0 && newScrollY < 1234567) {
                webView.setScrollY(newScrollY);
            }
            webView.post(() -> webView.setOnScrollChangeListener(null));
        });
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

    public void toggle() {
        if (zaPrikaz == bukvalno) {
            MainActivity.getSharedPreferences(context).edit().putBoolean(PREF_BUKVALNO, false).apply();
            zaPrikaz = finalno;
        } else {
            MainActivity.getSharedPreferences(context).edit().putBoolean(PREF_BUKVALNO, true).apply();
            zaPrikaz = bukvalno;
        }
        refreshWebView(true);
    }

    private static String createHtml(List<String> tekst, List<String> prevod, boolean removeGroupings, String author, boolean a3byka, boolean inLandscape, String searchedWord, int width) {
        StringBuilder builder = new StringBuilder();
        Pattern searchedWordPattern = null;
        if (!searchedWord.isEmpty()) {
            searchedWordPattern = Pattern.compile("\\b(" + searchedWord + ")\\b", Pattern.CASE_INSENSITIVE);
        }

        // TODO: Probati kao na iOS: <style>p { font-size: 5vw; }</style>
        builder.append("<html><head><meta http-equiv=\"content-type\" value=\"UTF-8\"><title></title><style>* { font-size: ").append(width).append("vw; }</style></head><body>");
        if (inLandscape && !prevod.isEmpty()) {
            builder.append("<table width=\"100%\">");
            int i = 2;
            for (; i < tekst.size(); i++) {
                builder.append("<tr><td width=\"50%\">");
                if (tekst.get(i).isEmpty()) {
                    builder.append("&nbsp;");
                } else {
                    builder.append(applyFilters(tekst.get(i), true, a3byka, removeGroupings, searchedWordPattern));
                }
                builder.append("</td><td width=\"50%\">");
                if (i < prevod.size()) {
                    builder.append(applyFilters(prevod.get(i), true, a3byka, false, searchedWordPattern));
                }
                builder.append("</td></tr>");
            }
            builder.append("</table>");
        } else {
            if (!author.isEmpty()) {
                builder.append("<p>(").append(author).append(")<br>");
                if (tekst.size() > 1 && !tekst.get(1).isEmpty()) {
                    builder.append(tekst.get(1)).append("<br>");
                }
                builder.append("<br></p>");
            }
            builder.append("<p>");
            for (int i = 2; i < tekst.size(); i++) {
                builder.append(applyFilters(tekst.get(i), false, a3byka, true, searchedWordPattern)).append("<br>");
            }
            builder.append("</p>");
        }
        builder.append("</body></head></html>");
        return builder.toString();
    }

    private static final String hintsChars = "([\\\\|])";
    private static final Pattern groupingPattern = Pattern.compile("[\\[\\]]");
    public static final Pattern hintsPattern = Pattern.compile(hintsChars);
    private static final Pattern wordEmphasisPattern = Pattern.compile("\\|([^ \n\\],]+)");
    private static Map<Pattern, String> explicits = null;
    private static String applyFilters(String line, boolean hints, boolean a3byka, boolean removeGroupings, Pattern searchedWordPattern) {
        if (hints) {
            // TODO: ako ovo menjam, treba da promenim i ručne ins u fajlovima...
            // TODO: možda je napadno da sve ovo bude označeno na glavnoj strani, možda samo tokom analize (položeno)
            line = wordEmphasisPattern.matcher(line).replaceAll("<em>$1</em>");
            if (BuildConfig.DEBUG) {
                String oldLine = line;
                line = hintsPattern.matcher(oldLine).replaceAll("");
                if (oldLine.compareTo(line) != 0) {
                    Log.wtf(TAG, "There was a remaining hint: old=" + oldLine + ", " + "new=" + line);
                }
            }
        }
        line = hintsPattern.matcher(line).replaceAll("");
        for (Map.Entry<Pattern, String> fuck : explicits.entrySet()) {
            line = fuck.getKey().matcher(line).replaceAll("$2" + fuck.getValue());
        }
        if (removeGroupings) {
            line = groupingPattern.matcher(line).replaceAll("");
        }
        if (searchedWordPattern != null) {
            line = searchedWordPattern.matcher(line).replaceAll("<strong>$1</strong>");
        }
        return line;
    }

    private static int normalizeZoom(int currentZoom, boolean inLandscape) {
        final int min, max;
        if (inLandscape) {
            min = MINIMUM_ZOOM_L;
            max = MAXIMUM_ZOOM_L;
        } else {
            min = MINIMUM_ZOOM_P;
            max = MAXIMUM_ZOOM_P;
        }
        if (currentZoom < min) {
            Log.wtf(TAG, "Invalid scale: " + currentZoom);
            return min;
        } else if (currentZoom > max) {
            Log.wtf(TAG, "Invalid scale: " + currentZoom);
            return max;
        } else {
            return currentZoom;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        // if (BuildConfig.DEBUG) { LOG_V("onTouch(" + action + ")"); }
        return mScaleDetector.onTouchEvent(motionEvent) && scaleInProgress;
    }

    @Override
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        return false;
    }

    private String getScaleKey() {
        String orientation = inLandscape ? "landscape" : "portrait";
        return orientation + ".zoom";
    }

    private boolean updateScaleFromPrefs(Context context, @Nullable WebView webView) {
        if (webView == null) {
            return false;
        }
        SharedPreferences preferences = MainActivity.getSharedPreferences(context);
        int scaleX = normalizeZoom(preferences.getInt(getScaleKey(), currentWidth), inLandscape);
        if (scaleX != currentWidth) {
            currentWidth = scaleX;
            if (BuildConfig.DEBUG) { LOG_V("updateScaleFromPrefs(" + scaleX + ")"); }
            return true;
        }
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        if (BuildConfig.DEBUG) { LOG_V("Scaling: onScaleBegin"); }
        beginScaleFactor = mScaleDetector.getScaleFactor();
        originalZoom = currentWidth;
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
            newZoom = originalZoom - 1;
        } else {
            newZoom = originalZoom + 1;
        }

        SharedPreferences preferences = MainActivity.getSharedPreferences(context);
        preferences.edit()
                .putInt(getScaleKey(), newZoom)
                .apply();
        if (updateScaleFromPrefs(context, webView)) {
            refreshWebView();
        }
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
        } else if (playerState == State.PAUSED || playerState == State.PREPARED || playerState == State.PLAYBACK_COMPLETED) {
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

        private Context getParentContext() {
            PageAdapter parent = parentRef.get();
            if (parent == null) {
                return null;
            }
            return parent.context;
        }

        @Override
        protected String doInBackground(Void[] voids) {
            if (BuildConfig.DEBUG) { LOG_V("doInBackground(" + imageFile + ")"); }
            String savedBitmap = null;
            Context context = getParentContext();
            if (context == null) {
                return null;
            }
            List<File> cacheDirs = new ArrayList<>();

            // TODO: ne znam zašto ovo već nisam imao.  Treba popraviti download ali zasad mogu da koristim ovo
            cacheDirs.add(ExternalStorageHelper.getMyCacheDir(context));
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

            if (MainActivity.internetNotAvailable(context)) {
                return null;
            }

            File imageToDownload = new File(ExternalStorageHelper.getMyCacheDir(context), imageFile);
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
