package org.mg94c18.gonzales;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import static org.mg94c18.gonzales.Logger.LOG_V;
import static org.mg94c18.gonzales.Logger.TAG;

import java.io.File;
import java.io.FileFilter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final long MAX_DOWNLOADED_IMAGES_ONLINE = 20;
    private static final String SHARED_PREFS_NAME = "config";
    private static final String EPISODE_TITLE = "episode_title";
    private static final String EPISODE_NUMBER = "episode_number";
    private static final String EPISODE_INDEX = "episode";
    private static final String MIGRATION_ID = "migration_id";
    private static final String DRAWER = "drawer";
    private static final String NIGHT_MODE = "night_mode";
    private static final String CONTACT_EMAIL = "yckopo@gmail.com";
    private static final String MY_ACTION_VIEW = BuildConfig.APPLICATION_ID + ".VIEW";
    static final String INTERNAL_OFFLINE = "offline";
    private static final long BYTES_PER_MB = 1024 * 1024;

    PageAdapter pageAdapter;
    DrawerLayout drawerLayout;
    ListView drawerList;
    List<String> titles;
    List<String> numbers;
    List<String> dates;
    List<String> numberAndTitle;
    boolean assetsLoaded = false;
    int selectedEpisode = 0;
    String selectedEpisodeTitle;
    String selectedEpisodeNumber;
    EpisodeDownloadTask downloadTask;
    AlertDialog configureDownloadDialog;
    AlertDialog quoteDialog;
    static final long syncIndex = -1;
    ActionBarDrawerToggle drawerToggle;
    Toast warningToast;
    private static final String DOWNLOAD_DIALOG_TITLE = "Download";
    private boolean sdCardReady;
    private String previousProgressString;
    private String progressString;
    private static boolean nightModeAllowed = Build.VERSION.SDK_INT >= 29;

    public SharedPreferences getSharedPreferences() {
        return getSharedPreferences(getApplicationContext());
    }

    public static SharedPreferences getSharedPreferences(@NonNull Context context) {
        return context.getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
    }

    /**
     * Callback with download progress.
     * @param downloadedPageCount: value 0 through allEpisodesPageCount, or -1 for canceled
     * @param allEpisodesPageCount total number of pages, or -1 for canceled
     */
    public void onDownloadProgress(int downloadedPageCount, int allEpisodesPageCount) {
        if (downloadedPageCount == -1 || allEpisodesPageCount == -1 || allEpisodesPageCount == 0 || downloadedPageCount >= allEpisodesPageCount) {
            progressString = null;
        } else {
            progressString = String.format(Locale.US," (%d%%)", downloadedPageCount * 100 / allEpisodesPageCount);
        }
        ActionBar actionBar = getMyActionBar();
        String titleWithNoProgress = "" + actionBar.getTitle();
        if (previousProgressString != null && titleWithNoProgress.endsWith(previousProgressString)) {
            titleWithNoProgress = titleWithNoProgress.substring(0, titleWithNoProgress.indexOf(previousProgressString));
        }
        previousProgressString = progressString;
        mySetActionBarTitle(actionBar, titleWithNoProgress);
    }

    private class MyArrayAdapter extends ArrayAdapter<String> {
        MyArrayAdapter(@NonNull Context context, int resource) {
            super(context, resource, titles);
        }

        @Override
        public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false);
            }
            TextView textView = (TextView) convertView;
            String title = numberAndTitle.get(position);
            textView.setText(title);
            if (position == selectedEpisode) {
                textView.setTypeface(null, Typeface.BOLD);
            } else {
                textView.setTypeface(null, Typeface.NORMAL);
            }

            return textView;
        }

        @Override
        public int getCount() {
            return titles.size();
        }
    }

    private void showQuoteDialog(int episode) {
        if (episode <0 || episode >= SearchProvider.HIDDEN_TITLES.size()) {
            Log.wtf(TAG, "Invalid episode: " + episode);
            return;
        }

        dismissAlertDialogs();
        String number = SearchProvider.HIDDEN_NUMBERS.get(episode);
        List<String> quotes = AssetLoader.loadFromAssetOrUpdate(this, number, syncIndex);
        quoteDialog = new AlertDialog.Builder(this)
                .setTitle(SearchProvider.HIDDEN_TITLES.get(episode))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, null)
                .setItems(quotes.toArray(new String[0]), null)
                .create();
        quoteDialog.setCanceledOnTouchOutside(false);
        quoteDialog.show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleNewIntent(intent);
    }

    private boolean handleNewIntent(Intent intent) {
        if (intent == null) {
            if (BuildConfig.DEBUG) { LOG_V("Null intent"); }
            return false;
        }

        if (!MY_ACTION_VIEW.equals(intent.getAction())) {
            return false;
        }

        String epizodeStr = intent.getStringExtra(SearchManager.EXTRA_DATA_KEY);
        if (epizodeStr == null) {
            if (BuildConfig.DEBUG) { LOG_V("Can't get epizodeStr"); }
            return false;
        }

        int episode;
        try {
            episode = Integer.parseInt(epizodeStr);
        } catch (NumberFormatException nfe) {
            Log.wtf(TAG, "Can't convert the episode ID", nfe);
            return false;
        }

        if (episode < 0) {
            showQuoteDialog(-1 * (episode + 1));
        } else {
            selectEpisode(episode);
            drawerList.setSelection(episode);
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (nightModeAllowed) {
            updateNightMode();
        }

        if (BuildConfig.DEBUG) { LOG_V("onCreate"); }
        super.onCreate(savedInstanceState);
        downloadTask = null;
        setContentView(R.layout.activity_main);

        titles = Collections.emptyList();
        numbers = Collections.emptyList();
        dates = Collections.emptyList();
        numberAndTitle = Collections.emptyList();

        drawerLayout = findViewById(R.id.drawer_layout);

        drawerList = findViewById(R.id.navigation);
        drawerList.setAdapter(new MyArrayAdapter(this, android.R.layout.simple_list_item_1));
        drawerList.setOnItemClickListener(this);

        if (!handleNewIntent(getIntent())) {
            EpisodeInfo episodeInfo = findSavedEpisode(savedInstanceState);

            if (episodeInfo.migration) {
                selectedEpisode = episodeInfo.index;
                updateAssets(
                        AssetLoader.loadFromAssetOrUpdate(this, AssetLoader.TITLES, syncIndex),
                        AssetLoader.loadFromAssetOrUpdate(this, AssetLoader.NUMBERS, syncIndex),
                        AssetLoader.loadFromAssetOrUpdate(this, AssetLoader.DATES, syncIndex),
                        AssetLoader.loadFromAssetOrUpdate(this, AssetLoader.HIDDEN_TITLES, syncIndex),
                        AssetLoader.loadFromAssetOrUpdate(this, AssetLoader.HIDDEN_NUMBERS, syncIndex),
                        AssetLoader.loadFromAssetOrUpdate(this, AssetLoader.HIDDEN_MATCHES, syncIndex));
                selectEpisode(episodeInfo.index);
            } else {
                AssetLoader.startAssetLoadingThread(this);
                selectEpisode(episodeInfo.index, episodeInfo.title, episodeInfo.number);
            }
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(DRAWER)) {
            Parcelable drawerState = savedInstanceState.getParcelable(DRAWER);
            if (drawerState != null) {
                if (BuildConfig.DEBUG) { LOG_V("Restoring drawer instance state"); }
                drawerList.onRestoreInstanceState(drawerState);
            } else {
                if (BuildConfig.DEBUG) { LOG_V("Can't restore drawer instance state"); }
            }
        }
    }

    public void updateAssets(@NonNull List<String> titles, @NonNull List<String> numbers, @NonNull List<String> dates, List<String> hiddenTitles, List<String> hiddenNumbers, List<String> hiddenMatches) {
        SearchProvider.TITLES = this.titles = titles;
        SearchProvider.NUMBERS = this.numbers = numbers;
        SearchProvider.DATES = this.dates = dates;
        SearchProvider.HIDDEN_TITLES = hiddenTitles;
        SearchProvider.HIDDEN_NUMBERS = hiddenNumbers;
        SearchProvider.HIDDEN_MATCHES = hiddenMatches;

        numberAndTitle = new ArrayList<>(numbers.size());
        for (int i = 0; i < numbers.size(); i++) {
            numberAndTitle.add(numbers.get(i) + ". " + titles.get(i));
        }

        assetsLoaded = true;

        drawerList.setAdapter(new MyArrayAdapter(this, android.R.layout.simple_list_item_1));
        drawerList.setSelection(selectedEpisode);
        invalidateOptionsMenu();
    }

    @Override
    public void onStop() {
        if (BuildConfig.DEBUG) { LOG_V("onStop"); }
        super.onStop();
        if (downloadTask != null) {
            downloadTask.cancel();
            downloadTask = null;
        }
        dismissAlertDialogs();
        destroyPageAdapter();
    }

    private void destroyPageAdapter() {
        if (pageAdapter != null) {
            pageAdapter.destroy();
            pageAdapter = null;
        }
    }

    private void dismissAlertDialogs() {
        AlertDialog[] dialogs = {configureDownloadDialog, quoteDialog};
        for (AlertDialog dialog : dialogs) {
            if (dialog != null) {
                dialog.cancel();
                dialog.dismiss();
            }
        }
        configureDownloadDialog = null;
        quoteDialog = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (BuildConfig.DEBUG) { LOG_V("onPrepareOptionsMenu"); }

        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.glavni_meni, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        if (searchManager != null) {
            searchView.setSearchableInfo(
                    searchManager.getSearchableInfo(getComponentName()));
        }

        sdCardReady = (ExternalStorageHelper.getExternalCacheDir(this) != null);
        updateDownloadButtons(menu);
        updateDarkModeButtons(menu);

        return super.onCreateOptionsMenu(menu);
    }

    private void updateDownloadButtons(Menu menu) {
        if (sdCardReady) {
            menu.findItem(R.id.action_download).setVisible(true);
        } else {
            menu.findItem(R.id.action_download_internal).setVisible(true);
        }
        menu.findItem(R.id.action_cancel_download).setVisible(false);
    }

    private void updateDarkModeButtons(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_dark_mode);
        if (nightModeAllowed) {
            item.setVisible(true);
        } else {
            item.setVisible(false);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (BuildConfig.DEBUG) { LOG_V("onPrepareOptionsMenu"); }

        final ActionBar actionBar = getSupportActionBar();
        if (assetsLoaded && actionBar != null && drawerToggle == null) {
            menu.findItem(R.id.search).setVisible(true);

            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open_accessibility, R.string.drawer_close_accessibility) {
                @Override
                public void onDrawerClosed(View view) {
                    super.onDrawerClosed(view);
                    mySetActionBarTitle(actionBar, selectedEpisodeTitle);
                }

                @Override
                public void onDrawerOpened(View view) {
                    super.onDrawerOpened(view);
                    String actionBarTitle = "Epizode";
                    if (numbers != null && numbers.size() > 0) {
                        actionBarTitle = actionBarTitle + " " + numbers.get(0) + "-" + numbers.get(numbers.size() - 1);
                    }
                    mySetActionBarTitle(actionBar, actionBarTitle);
                }
            };
            drawerLayout.addDrawerListener(drawerToggle);
            drawerToggle.syncState();
        }

        if (downloadTask != null && downloadTask.completed()) {
            downloadTask = null;
        }

        if (downloadTask != null) {
            menu.findItem(R.id.action_download).setVisible(false);
            menu.findItem(R.id.action_download_internal).setVisible(false);
            menu.findItem(R.id.action_cancel_download).setVisible(true);
        } else {
            menu.findItem(R.id.action_cancel_download).setVisible(false);
            updateDownloadButtons(menu);
            progressString = null;
        }

        return super.onPrepareOptionsMenu(menu);
    }

    private void mySetActionBarTitle(@NonNull ActionBar actionBar, @NonNull String title) {
        if (progressString != null) {
            title = title + progressString;
        }
        actionBar.setTitle(title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle != null && drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_email:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:"));
                String[] emails = {CONTACT_EMAIL};
                emailIntent.putExtra(Intent.EXTRA_EMAIL, emails);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name) + " App");
                if (emailIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(emailIntent);
                }
                return true;
            case R.id.action_review:
                Intent intent = PlayIntentMaker.createPlayIntent(this);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                return true;
            case R.id.search:
                if (BuildConfig.DEBUG) { LOG_V("Search requested"); }
                onSearchRequested();
                return true;
            case R.id.action_dark_mode:
                boolean newNightMode = !getNightModeFromSharedPrefs();
                getSharedPreferences().edit().putBoolean(NIGHT_MODE, newNightMode).apply();
                recreate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean getNightModeFromSharedPrefs() {
        return getSharedPreferences().getBoolean(NIGHT_MODE, false);
    }

    private void updateNightMode() {
        boolean nightMode = getNightModeFromSharedPrefs();
        int mode = nightMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        if (BuildConfig.DEBUG) { LOG_V("setDefaultNightMode(" + mode + ")"); }
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    private void configureDownload(String episode, final EpisodeDownloadTask.Destination destination) {
        dismissAlertDialogs();
        final MainActivity activity = this;
        final File destinationDir;
        final int maxEpisodesToOffer;
        final long spaceBufferMb;
        final List<String> episodesToDelete = new ArrayList<>();

        if (destination == EpisodeDownloadTask.Destination.INTERNAL_MEMORY) {
            destinationDir = ExternalStorageHelper.getInternalOfflineDir(activity);
            maxEpisodesToOffer = 10;
            spaceBufferMb = 350;
        } else {
            destinationDir = ExternalStorageHelper.getExternalCacheDir(activity);
            maxEpisodesToOffer = 25;
            spaceBufferMb = 0;
        }
        if (destinationDir == null) {
            Log.wtf(TAG, "Destination is null");
            return;
        }

        final LinkedHashMap<String, Long> completelyDownloadedEpisodes = EpisodeDownloadTask.getCompletelyDownloadedEpisodes(destinationDir);
        final List<Integer> episodesToDownload = new ArrayList<>();
        final List<String> namesToShow = new ArrayList<>();
        final List<Integer> indexesOfNamesToShow = new ArrayList<>();
        int indexToScrollTo = -1;
        for (int i = 0; i < numbers.size(); i++) {
            if (numbers.get(i).equals(episode)) {
                indexToScrollTo = indexesOfNamesToShow.size();
            }
            if (completelyDownloadedEpisodes.containsKey(numbers.get(i))) {
                continue;
            }
            namesToShow.add(numberAndTitle.get(i));
            indexesOfNamesToShow.add(i);
        }
        long freeSpaceAtDir = ExternalStorageHelper.getFreeSpaceAtDir(destinationDir);
        final long freeSpaceMb = (freeSpaceAtDir != -1 ? freeSpaceAtDir : Long.MAX_VALUE) / BYTES_PER_MB;
        final long averageMbPerEpisode = getResources().getInteger(R.integer.average_episode_size_mb);
        warningToast = null;
        // boolean[] checkedItems = new boolean[namesToShow.size()];
        // for (int i = 0; i < checkedItems.length; i++) {
        //     checkedItems[i] = true;
        //     episodesToDownload.add(indexesOfNamesToShow.get(i));
        // }
        boolean[] checkedItems = null;
        configureDownloadDialog = new AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle(DOWNLOAD_DIALOG_TITLE)
                .setMultiChoiceItems(namesToShow.toArray(new String[0]), checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean checked) {
                        Integer actualIndex = indexesOfNamesToShow.get(i);
                        if (checked) {
                            episodesToDownload.add(actualIndex);
                        } else {
                            episodesToDownload.remove(actualIndex);
                        }
                        Button positiveButton = configureDownloadDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                        if (episodesToDownload.size() > maxEpisodesToOffer) {
                            positiveButton.setEnabled(false);
                        } else {
                            long downloadMb = episodesToDownload.size() * averageMbPerEpisode;
                            if (downloadMb + spaceBufferMb > freeSpaceMb) {
                                if ((episodesToDownload.size() - completelyDownloadedEpisodes.size()) * averageMbPerEpisode + spaceBufferMb < freeSpaceMb) {
                                    if (BuildConfig.DEBUG) { LOG_V("Will delete old episodes"); }
                                    if (warningToast != null) {
                                        warningToast.cancel();
                                    }
                                    warningToast = Toast.makeText(activity,"Stare epizode će biti obrisane", Toast.LENGTH_SHORT);
                                    warningToast.show();
                                    positiveButton.setEnabled(true);
                                    updateDownloadDialogTitle(configureDownloadDialog, downloadMb);
                                } else {
                                    if (BuildConfig.DEBUG) { LOG_V("Too much to download"); }
                                    positiveButton.setEnabled(false);
                                }
                            } else {
                                if (BuildConfig.DEBUG) { LOG_V("We can fit the download"); }
                                if (warningToast != null) {
                                    warningToast.cancel();
                                    warningToast = null;
                                }
                                positiveButton.setEnabled(true);
                                updateDownloadDialogTitle(configureDownloadDialog, downloadMb);
                            }
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        if (episodesToDownload.isEmpty()) {
                            return;
                        }
                        if (episodesToDownload.size() * averageMbPerEpisode + spaceBufferMb > freeSpaceMb) {
                            for (Map.Entry<String, Long> entry : completelyDownloadedEpisodes.entrySet()) {
                                episodesToDelete.add(entry.getKey());
                                if ((episodesToDownload.size() - episodesToDelete.size()) * averageMbPerEpisode + spaceBufferMb < freeSpaceMb) {
                                    break;
                                }
                            }
                        }
                        downloadTask = new EpisodeDownloadTask(episodesToDelete, activity, episodesToDownload, destinationDir);
                        onDownloadProgress(-1, -1);
                        downloadTask.execute();
                    }
                })
                .create();
        configureDownloadDialog.show();
        ListView listView = configureDownloadDialog.getListView();
        if (listView != null && indexToScrollTo != -1) {
            listView.setSelection(indexToScrollTo);
        }
    }

    private static void updateDownloadDialogTitle(AlertDialog configureDownloadDialog, long downloadMb) {
        String title = DOWNLOAD_DIALOG_TITLE;
        if (downloadMb > 0) {
            title = title + String.format(Locale.US, " (oko %d MB)", downloadMb);
        }
        configureDownloadDialog.setTitle(title);
    }

    @Override
    protected void onSaveInstanceState(Bundle instanceState) {
        if (BuildConfig.DEBUG) { LOG_V("onSaveInstanceState"); }
        if (BuildConfig.DEBUG) { LOG_V("Saving selectedEpisode=" + selectedEpisode); }
        instanceState.putInt(EPISODE_INDEX, selectedEpisode);
        instanceState.putString(EPISODE_TITLE, selectedEpisodeTitle);
        instanceState.putString(EPISODE_NUMBER, selectedEpisodeNumber);
        instanceState.putParcelable(DRAWER, drawerList.onSaveInstanceState());

        super.onSaveInstanceState(instanceState);
    }

    public static boolean internetNotAvailable(@NonNull Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            if (BuildConfig.DEBUG) { LOG_V("Can't get connectivityManager"); }
            return true;
        }
        if (BuildConfig.DEBUG) { LOG_V("Got connectivityManager"); }

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            if (BuildConfig.DEBUG) { LOG_V("getActiveNetworkInfo returned null"); }
            return true;
        }

        boolean connected = networkInfo.isConnected();
        if (BuildConfig.DEBUG) { LOG_V("Connected=" + connected); }
        return !connected;
    }

    static class EpisodeInfo {
        String title;
        String number;
        int index;
        boolean migration; // whether we're in migration from older release where just index was available but no title or number

        @Override
        public String toString() {
            return "{" + index + ": '" + title + "' (" + number + ")}";
        }
    }

    private @NonNull EpisodeInfo findSavedEpisode(Bundle state) {
        EpisodeInfo episodeInfo = new EpisodeInfo();

        if (state != null && state.containsKey(EPISODE_INDEX) && state.containsKey(EPISODE_TITLE) && state.containsKey(EPISODE_NUMBER)) {
            episodeInfo.index = state.getInt(EPISODE_INDEX);
            episodeInfo.title = state.getString(EPISODE_TITLE);
            episodeInfo.number = state.getString(EPISODE_NUMBER);
            if (BuildConfig.DEBUG) { LOG_V("Loaded episode from bundle: " + episodeInfo); }
        } else {
            SharedPreferences preferences = getSharedPreferences();
            episodeInfo.migration = false;
            String defaultTitle = getResources().getString(R.string.default_episode_title);
            String defaultNumber = getResources().getString(R.string.default_episode_number);
            if (preferences.contains(EPISODE_TITLE) && preferences.contains(EPISODE_NUMBER)) {
                episodeInfo.title = preferences.getString(EPISODE_TITLE, defaultTitle);
                episodeInfo.number = preferences.getString(EPISODE_NUMBER, defaultNumber);
            } else {
                if (preferences.contains(EPISODE_INDEX)) {
                    episodeInfo.migration = true;
                } else {
                    episodeInfo.title = defaultTitle;
                    episodeInfo.number = defaultNumber;
                }
            }
            episodeInfo.index = preferences.getInt(EPISODE_INDEX, getResources().getInteger(R.integer.default_episode_index));
            if (BuildConfig.DEBUG) { LOG_V("Loaded episode from shared prefs: " + episodeInfo); }
        }
        return episodeInfo;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (BuildConfig.DEBUG) { LOG_V("onItemClick: " + position); }
        drawerList.setItemChecked(position, !drawerList.isItemChecked(position));
        drawerLayout.closeDrawer(drawerList);
        selectEpisode(position);
    }

    private void selectEpisode(int position) {
        selectEpisode(position, titles.get(position), numbers.get(position));
    }

    private @NonNull ActionBar getMyActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            throw new Error("Unexpected");
        }
        return actionBar;
    }

    private void selectEpisode(int position, String title, String number) {
        if (position >= 0) {
            selectedEpisodeTitle = title;
            selectedEpisodeNumber = number;
        }
        mySetActionBarTitle(getMyActionBar(), title);
        destroyPageAdapter();
        pageAdapter = new PageAdapter(this, number);
        if (position >= 0) {
            selectedEpisode = position;
            if (BuildConfig.DEBUG) { LOG_V("Saving episode " + selectedEpisode); }
            getSharedPreferences().edit()
                    .putInt(EPISODE_INDEX, selectedEpisode)
                    .putString(EPISODE_TITLE, title)
                    .putString(EPISODE_NUMBER, number)
                    .apply();
        }
    }


    public static synchronized void deleteOldSavedFiles(@Nullable File dir, long maxImages) {
        if (dir == null) {
            return;
        }
        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return !file.isDirectory();
            }
        });
        if (files != null && files.length > maxImages) {
            if (BuildConfig.DEBUG) { LOG_V("Found " + files.length + " cached images"); }
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    long diff = o1.lastModified() - o2.lastModified();
                    if (diff < 0) return -1;
                    else if (diff > 0) return 1;
                    else return 0;
                }
            });

            for (int i = 0; i < files.length - maxImages; i++) {
                deleteFile(files[i]);
            }
        }
    }

    public static void deleteFile(File file) {
        if (BuildConfig.DEBUG) { LOG_V("deleteFile(" + file.getAbsolutePath() + ")"); }
        if (file.delete()) {
            if (BuildConfig.DEBUG) { LOG_V("Deleted " + file); }
        } else {
            Log.wtf(TAG, "Can't delete " + file);
        }
    }
}
