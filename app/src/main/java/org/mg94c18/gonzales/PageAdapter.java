package org.mg94c18.gonzales;

import static org.mg94c18.gonzales.Logger.LOG_V;
import static org.mg94c18.gonzales.Logger.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PageAdapter implements View.OnTouchListener, ScaleGestureDetector.OnScaleGestureListener, View.OnClickListener {
    List<String> links;
    String episode;
    String filename;

    Context context;
    Button button;
    private WebView webView;

    private static final int SCALE_MAX_X_INT = 2;
    private static final int MINIMUM_ZOOM = 100;
    int originalZoom;
    private float beginScaleFactor;
    ScaleGestureDetector mScaleDetector;
    private boolean scaleInProgress = false;

    MediaPlayer mediaPlayer;
    MyLoadTask loadTask;

    PageAdapter(MainActivity activity, String episode) {
        if (BuildConfig.DEBUG) { LOG_V("PageAdapter(" + episode + ")"); }

        this.context = activity;
        this.episode = episode;
        links = AssetLoader.loadFromAssetOrUpdate(context, episode, MainActivity.syncIndex);
        this.filename = DownloadAndSave.fileNameFromLink(links.get(0), episode, 0);

        mScaleDetector = new ScaleGestureDetector(context, this);

        webView = activity.findViewById(R.id.webview);
        button = activity.findViewById(R.id.button);
        button.setOnClickListener(this);

        updateScaleFromPrefs(context, webView);

        // TODO: možda ovo ugasi ProgressBar da se ne vidi?
        //webView.loadData("<html><head><title></title></head><body><p>...</p></body></html>", "text/html", "UTF-8");
        ProgressBar progressBar = activity.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        webView.setTag(progressBar);
        webView.setOnTouchListener(this);

        loadTask = new MyLoadTask(links, context, episode, links.get(0), filename, webView);
        loadTask.execute();
    }

    public void destroy() {
        if (BuildConfig.DEBUG) { LOG_V("destroy(" + filename + ")"); }
        loadTask.cancel(true);
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

    private boolean previouslyClicked = false;
    @Override
    public void onClick(View view) {
        Log.i(TAG, "onClick");
        if (!previouslyClicked) {
            Intent intent = new Intent(context, PlaybackService.class);
            intent.setAction(PlaybackService.ACTION_PLAY);
            intent.putExtra(PlaybackService.EXTRA_FILE, "TODO:file");
            context.startService(intent);
            previouslyClicked = true;
        } else {
            Intent intent = new Intent(context, PlaybackService.class);
            intent.setAction(PlaybackService.ACTION_NOTIFICATION);
            context.startService(intent);
        }
    }

    private static class MyLoadTask extends AsyncTask<Void, Void, String> {
        String imageFile;
        WeakReference<WebView> webView;
        String link;
        WeakReference<Context> contextRef;
        String episodeId;
        int destinationViewWidth;
        int destinationViewHeight;
        List<String> links;

        MyLoadTask(List<String> links, Context context, String episodeId, String link, String imageFile, WebView webView) {
            this.imageFile = imageFile;
            this.link = link;
            this.links = links;
            this.contextRef = new WeakReference<>(context);
            this.webView = new WeakReference<>(webView);
            this.episodeId = episodeId;
            this.destinationViewHeight = webView.getHeight();
            this.destinationViewWidth = webView.getWidth();
        }

        @Override
        protected String doInBackground(Void[] voids) {
            if (BuildConfig.DEBUG) { LOG_V("doInBackground(" + imageFile + ")"); }
            String savedBitmap = null;
            Context context = contextRef.get();
            if (context == null) {
                return null;
            }
            List<File> cacheDirs = new ArrayList<>();

            // TODO: ne znam zašto ovo već nisam imao.  Treba popraviti download ali zasad mogu da koristim ovo
            cacheDirs.add(context.getCacheDir());
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
                    if (getWebView() == null || isCancelled()) {
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

            View destinationView = webView.get();
            if (destinationView == null) {
                return null;
            }
            if (MainActivity.internetNotAvailable(context)) {
                return null;
            }

            File imageToDownload = new File(context.getCacheDir(), imageFile);
            if (isCancelled()) {
                return null;
            } else {
                return DownloadAndSave.downloadAndSave(link, imageToDownload, destinationViewWidth, destinationViewHeight, 3);
            }
        }

        private String mp3HelperVerifyFile(String absolutePath) {
            // TODO: provera
            return absolutePath;
        }

        private synchronized WebView getWebView() {
            return webView.get();
        }

        @Override
        protected void onPostExecute(String bitmap) {
            WebView view = getWebView();
            ProgressBar progressBar = view != null ? (ProgressBar) view.getTag() : null;
            try {
                if (BuildConfig.DEBUG) { LOG_V("onPostExecute(" + imageFile + "," + bitmap + ")"); }
                if (bitmap == null && !isCancelled()) {
                    Context context = contextRef.get();
                    if (view != null && context != null && MainActivity.internetNotAvailable(context)) {
                        view.loadData("<html><head></head><body><p>Internet Problem</p></body></html>", "text/html", "UTF-8");
                    }
                    return;
                }
                if (view != null) {
                    if (BuildConfig.DEBUG) { LOG_V("Loading into WebView(" + imageFile + ")"); }
                    view.loadData(createHtml(links, true, false), "text/html", "UTF-8");
                }
            } finally {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            }
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

        @Override
        protected void onCancelled(String b) {
            if (BuildConfig.DEBUG) { LOG_V("onCancelled(" + imageFile + ")"); }
            onPostExecute(null);
        }
    }
}
