package org.mg94c18.gonzales;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.os.Build;
import android.util.Log;
import android.util.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import static org.mg94c18.gonzales.Logger.LOG_V;
import static org.mg94c18.gonzales.Logger.TAG;
import static org.mg94c18.gonzales.MainActivity.syncIndex;

public final class AssetLoader {
    public static final String TITLES = "titles";
    public static final String NUMBERS = "numbers";
    public static final String DATES = "dates";
    public static final String HIDDEN_TITLES = "hiddenTitles";

    private static final boolean USE_COMPRESSION = false;

    // Pokriva jedan strip, a ako je *4 onda pokriva sve epizode
    private static final int CAPACITY = 128;

    public static final String ASSETS = "assets_";

    private static Set<String> currentAssets = Collections.emptySet();

    public static synchronized @NonNull Set<String> getCurrentAssets(AssetManager assetManager) {
        if (!currentAssets.isEmpty()) {
            return currentAssets;
        }

        String[] apkAssetArray;
        try {
            apkAssetArray = assetManager.list("");
        } catch (IOException ioe) {
            Log.wtf(TAG, "Can't list assets", ioe);
            return Collections.emptySet();
        }
        if (apkAssetArray == null || apkAssetArray.length == 0) {
            Log.wtf(TAG, "Unexpected list of assets");
            return Collections.emptySet();
        }
        currentAssets = new HashSet<>(Arrays.asList(apkAssetArray));
        return currentAssets;
    }

    private interface StreamCreator {
        InputStream createStream() throws IOException;
        void cleanup() throws IOException;
    }

    private static long getAssetUpdateTime(String assetName, @NonNull File assetDir) {
        final String assetUpdatePrefix = assetName + "_";
        String[] assetFiles = assetDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s != null && s.startsWith(assetUpdatePrefix);
            }
        });
        if (assetFiles == null || assetFiles.length != 1 || assetFiles[0] == null) {
            return -1;
        }
        final long assetUpdateTimestamp;
        try {
            assetUpdateTimestamp = Long.parseLong(assetFiles[0].substring(assetUpdatePrefix.length()));
        } catch (NumberFormatException nfe) {
            Log.wtf(TAG, "Can't convert a number", nfe);
            return -1;
        }
        return assetUpdateTimestamp;
    }

    // TODO: obrisati ovo
    public static long getApkAssetTime(Context context) {
        try {
            return Long.parseLong(context.getResources().getString(R.string.apk_assets_time));
        } catch (NumberFormatException nfe) {
            Log.wtf(TAG, "Can't convert number", nfe);
            return -1;
        }
    }

    public static final String CYRILLIC_SUFFIX = ".cirilica";
    public @NonNull static List<String> loadFromAssetOrUpdateOrCyrillic(final Context context, final String assetName, final long syncIndex) {
        boolean isCyrillicMode = MainActivity.getSharedPreferences(context).getBoolean(MainActivity.CYRILLIC_MODE, false);
        final String suffix = isCyrillicMode ? CYRILLIC_SUFFIX : "";
        List<String> result = loadFromAssetOrUpdate(context, assetName + suffix, syncIndex);
        if (result.isEmpty() && !suffix.isEmpty()) {
            // Fallback
            result = loadFromAssetOrUpdate(context, assetName, syncIndex);
        }
        return result;
    }

    public @NonNull static List<String> loadFromAssetOrUpdate(final Context context, final String assetName, final long syncIndex) {
        List<String> fromAsset = loadFromAsset(assetName, context.getAssets());
        if (syncIndex < 0) {
            return fromAsset;
        }
        File assetDir = new File (context.getFilesDir(), ASSETS + syncIndex);
        if (!assetDir.exists()) {
            if (BuildConfig.DEBUG) { LOG_V("Asset dir doesn't exist: " + assetDir); }
            return fromAsset;
        }
        long assetTimestamp = getApkAssetTime(context);
        if (assetTimestamp == -1) {
            if (BuildConfig.DEBUG) { LOG_V("Can't get APK install time"); }
            return fromAsset;
        }
        long assetUpdateTimestamp = getAssetUpdateTime(assetName, assetDir);
        if (assetUpdateTimestamp == -1) {
            if (BuildConfig.DEBUG) { LOG_V("No update for asset " + assetName); }
            return fromAsset;
        }
        if (assetUpdateTimestamp <= assetTimestamp) {
            if (BuildConfig.DEBUG) { LOG_V("Asset " + assetName + " is already up to date"); }
            return fromAsset;
        }

        List<String> fromUpdate = loadFromFile(new File(assetDir, assetName + "_" + assetUpdateTimestamp));
        if (fromUpdate == null || fromUpdate.size() < fromAsset.size()) {
            if (BuildConfig.DEBUG) { LOG_V("Can't load updates from update file"); }
            return fromAsset;
        }

        if (fromAsset.isEmpty()) {
            if (BuildConfig.DEBUG) { LOG_V("Assets from APK are empty"); }
            return fromUpdate;
        }

        for (int i = 0; i < fromUpdate.size(); i++) {
            String updatedLine = fromUpdate.get(i);
            if (i >= fromAsset.size()) {
                fromAsset.add(updatedLine);
            } else if (!updatedLine.isEmpty()) {
                fromAsset.set(i, updatedLine);
            }
        }
        return fromAsset;
    }

    public @NonNull static List<String> loadFromAsset(final String name, final AssetManager assetManager) {
        if (!getCurrentAssets(assetManager).contains(name)) {
            if (BuildConfig.DEBUG) { LOG_V("Asset " + name + " not present in APK"); }
            return Collections.emptyList();
        }

        List<String> list = load(new StreamCreator() {
            @Override
            public InputStream createStream() throws IOException {
                return assetManager.open(name);
            }

            @Override
            public void cleanup() {
            }
        });
        if (list == null) {
            return Collections.emptyList();
        }
        return list;
    }

    @Nullable static List<String> loadFromUrl(final String url) {
        return load(new StreamCreator() {
            private HttpURLConnection connection;

            @Override
            public InputStream createStream() throws IOException {
                Pair<HttpURLConnection, InputStream> readInfo = DownloadAndSave.readUrlWithRedirect(url);
                if (readInfo == null) {
                    connection = null;
                    return null;
                }
                connection = readInfo.first;
                return readInfo.second;
            }

            @Override
            public void cleanup() {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    public @Nullable static List<String> loadFromFile(final File file) {
        if (!file.exists()) {
            return null;
        }
        return load(new StreamCreator() {
            @Override
            public InputStream createStream() throws IOException {
                return new FileInputStream(file);
            }

            @Override
            public void cleanup() {
            }
        });
    }

    private @Nullable static List<String> load(StreamCreator streamCreator) {
        InputStream inputStream = null;
        GZIPInputStream gzipInputStream = null;
        Scanner scanner = null;
        try {
            ArrayList<String> list = new ArrayList<>(CAPACITY);
            inputStream = streamCreator.createStream();

            if (USE_COMPRESSION) {
                gzipInputStream = new GZIPInputStream(inputStream);
                scanner = new Scanner(gzipInputStream);
            } else {
                scanner = new Scanner(inputStream);
            }

            while (scanner.hasNextLine()) {
                list.add(scanner.nextLine());
            }

            scanner.close();
            scanner = null;
            if (USE_COMPRESSION) {
                gzipInputStream.close();
            }
            inputStream.close();

            return list;
        } catch (IOException e) {
            Log.wtf(TAG, "Can't read asset", e);
        } finally {
            try {
                if (scanner != null) scanner.close();
                IOUtils.closeQuietly(gzipInputStream);
                IOUtils.closeQuietly(inputStream);
                streamCreator.cleanup();
            } catch (IOException e) {
                Log.wtf(TAG, "Can't close", e);
            }
        }
        return null;
    }

    // No longer starts a thread, no need to load from disk and causes a problem during recreation/rotation
    // TODO: move the assets to this class instead of MainActivity
    public static void handleAssetLoading(Context context) {
        if (MainActivity.assetsLoaded) {
            return;
        }
        final List<String> titles;
        final List<String> numbers;
        final List<String> dates;
        if (BuildConfig.DEBUG) { LOG_V("Begin loading: " + System.currentTimeMillis()); }
        titles = AssetLoader.loadFromAssetOrUpdateOrCyrillic(context, AssetLoader.TITLES, syncIndex);
        numbers = AssetLoader.loadFromAssetOrUpdate(context, AssetLoader.NUMBERS, syncIndex);
        dates = AssetLoader.loadFromAssetOrUpdateOrCyrillic(context, AssetLoader.DATES, syncIndex);
        if (BuildConfig.DEBUG) { LOG_V("End loading: " + System.currentTimeMillis()); }

        int count = titles.size();
        if (numbers.size() != count || dates.size() != count) {
            Log.wtf(TAG, "Episode list mismatch: titles=" + titles.size() + ", numbers=" + numbers.size() + ", dates=" + dates.size());
            return;
        }

        final List<String> hiddenTitles = AssetLoader.loadFromAssetOrUpdate(context, AssetLoader.HIDDEN_TITLES, syncIndex);

        MainActivity.updateAssets(titles, numbers, dates, hiddenTitles);
        SearchProvider.populateTrie(context, numbers, titles);

        if (PlaybackService.inForeground) {
            Intent intent = new Intent(context, PlaybackService.class);
            intent.setAction(PlaybackService.ACTION_ASSET_UPDATE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        }
    }
}
