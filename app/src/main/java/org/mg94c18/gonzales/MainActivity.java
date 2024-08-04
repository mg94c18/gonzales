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
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
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
    private static final String INTERNET_PROBLEM = "Internet Problem";
    static final String INTERNAL_OFFLINE = "offline";
    private static final long BYTES_PER_MB = 1024 * 1024;

    MyPagerAdapter pagerAdapter;
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
    AlertDialog pagePickerDialog;
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

    private static int normalizePageIndex(int i, int max) {
        if (i < 0) {
            return 0;
        }

        if (max <= 2) {
            return -1;
        }

        if (i >= max) {
            return max - 1;
        }

        if (i == 1) {
            return 0;
        }

        if (i == 2) {
            i = 3;
        }

        return i - 2;
    }

    public SharedPreferences getSharedPreferences() {
        return getSharedPreferences(getApplicationContext());
    }

    public static SharedPreferences getSharedPreferences(@NonNull Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        if (!prefs.contains(MIGRATION_ID)) {
        }
        return prefs;
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
                startAssetLoadingThread(this);
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
        pagerAdapter.getItem(0);
        pagerAdapter.fragment.onCreate(savedInstanceState);
        pagerAdapter.fragment.onCreateView(this);
    }

    private static void startAssetLoadingThread(MainActivity mainActivity) {
        final WeakReference<MainActivity> mainActivityRef = new WeakReference<>(mainActivity);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<String> titles;
                final List<String> numbers;
                final List<String> dates;
                Context context = mainActivityRef.get();
                if (context == null) {
                    if (BuildConfig.DEBUG) { LOG_V("Not loading, context went away"); }
                    return;
                }
                if (BuildConfig.DEBUG) { LOG_V("Begin loading: " + System.currentTimeMillis()); }
                titles = AssetLoader.loadFromAssetOrUpdate(context, AssetLoader.TITLES, syncIndex);
                numbers = AssetLoader.loadFromAssetOrUpdate(context, AssetLoader.NUMBERS, syncIndex);
                dates = AssetLoader.loadFromAssetOrUpdate(context, AssetLoader.DATES, syncIndex);
                if (BuildConfig.DEBUG) { LOG_V("End loading: " + System.currentTimeMillis()); }

                int count = titles.size();
                if (numbers.size() != count || dates.size() != count) {
                    Log.wtf(TAG, "Episode list mismatch: titles=" + titles.size() + ", numbers=" + numbers.size() + ", dates=" + dates.size());
                    return;
                }

                final List<String> hiddenTitles = AssetLoader.loadFromAssetOrUpdate(context, AssetLoader.HIDDEN_TITLES, syncIndex);
                final List<String> hiddenNumbers = AssetLoader.loadFromAssetOrUpdate(context, AssetLoader.HIDDEN_NUMBERS, syncIndex);
                final List<String> hiddenDates = AssetLoader.loadFromAssetOrUpdate(context, AssetLoader.HIDDEN_MATCHES, syncIndex);
                count = hiddenTitles.size();
                if (hiddenNumbers.size() != count || hiddenDates.size() != count) {
                    Log.wtf(TAG, "Hidden list mismatch: titles=" + hiddenTitles.size() + ", numbers=" + hiddenNumbers.size() + ", dates=" + hiddenDates.size());
                    hiddenTitles.clear();
                    hiddenNumbers.clear();
                    hiddenDates.clear();
                }

                final MainActivity activity = mainActivityRef.get();
                if (activity == null) {
                    if (BuildConfig.DEBUG) { LOG_V("Not loading, activity went away"); }
                    return;
                }

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.updateAssets(titles, numbers, dates, hiddenTitles, hiddenNumbers, hiddenDates);
                    }
                });
            }
        }).start();
    }

    private void updateAssets(@NonNull List<String> titles, @NonNull List<String> numbers, @NonNull List<String> dates, List<String> hiddenTitles, List<String> hiddenNumbers, List<String> hiddenMatches) {
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
        if (pagerAdapter != null) {
            pagerAdapter.fragment.onDestroy();
            pagerAdapter = null;
        }
    }

    private void dismissAlertDialogs() {
        AlertDialog[] dialogs = {pagePickerDialog, configureDownloadDialog, quoteDialog};
        for (AlertDialog dialog : dialogs) {
            if (dialog != null) {
                dialog.cancel();
                dialog.dismiss();
            }
        }
        pagePickerDialog = null;
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
            case R.id.action_download:
                final String errorMessage;
                String storageState = Environment.getExternalStorageState();
                if (internetNotAvailable(this)) {
                    errorMessage = INTERNET_PROBLEM;
                } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(storageState)) {
                    errorMessage = "Memorijska kartica je ubačena, ali je read-only";
                } else if (!Environment.MEDIA_MOUNTED.equals(storageState)
                        || ExternalStorageHelper.getExternalCacheDir(this) == null) {
                    errorMessage = "Ne vidim memorijsku karticu";
                } else {
                    errorMessage = null;
                }
                if (errorMessage != null) {
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    return true;
                }
                if (BuildConfig.DEBUG) { LOG_V(">>configureDownload"); }
                configureDownload(pagerAdapter.episode, EpisodeDownloadTask.Destination.SD_CARD);
                if (BuildConfig.DEBUG) { LOG_V("<<configureDownload"); }
                return true;
            case R.id.action_download_internal:
                if (internetNotAvailable(this)) {
                    Toast.makeText(this, INTERNET_PROBLEM, Toast.LENGTH_LONG).show();
                    return true;
                }
                configureDownload(pagerAdapter.episode, EpisodeDownloadTask.Destination.INTERNAL_MEMORY);
                return true;
            case R.id.action_cancel_download:
                if (downloadTask == null) {
                    Log.wtf(TAG, "Nothing to cancel");
                } else {
                    downloadTask.cancel();
                    downloadTask = null;
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
        if (pagerAdapter != null) {
            pagerAdapter.fragment.onSaveInstanceState(instanceState);
        }
    }

    static boolean internetNotAvailable(@NonNull Context context) {
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
        if (pagerAdapter != null) {
            pagerAdapter.fragment.onDestroy();
            pagerAdapter = null;
        }

        pagerAdapter = new MyPagerAdapter(this, getSupportFragmentManager(), number);
        pagerAdapter.getItem(0);
        pagerAdapter.fragment.onCreate(null);
        pagerAdapter.fragment.onCreateView(this);
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

    public static class MyPagerAdapter {
        List<String> links;
        String episode;
        Context context;
        public MyFragment fragment;

        MyPagerAdapter(Context context, FragmentManager fm, String episode) {
            links = AssetLoader.loadFromAssetOrUpdate(context, episode, syncIndex);
            this.episode = episode;
            this.context = context;
        }

        // @Override
        public void getItem(int position) {
            MyFragment f = new MyFragment(links);
            Bundle args = new Bundle();
            args.putString(MyFragment.FILENAME, DownloadAndSave.fileNameFromLink(links.get(position), episode, position));
            args.putString(MyFragment.LINK, links.get(position));
            args.putString(MyFragment.EPISODE_ID, episode);
            args.putInt(MyFragment.PAGE_NUMBER, position);
            f.setArguments(args, context);
            fragment = f;
        }

        public static class MyFragment implements View.OnTouchListener, ScaleGestureDetector.OnScaleGestureListener {
            public static final String FILENAME = "filename";
            public static final String LINK = "link";
            public static final String EPISODE_ID = "episode";
            public static final String PAGE_NUMBER = "pageNumber";
            private static final String SCALE_X = "scale_x";
            private static final float DELTA = 0.02f;
            private static final float SCALE_MIN = 1.00f;
            private static final int SCALE_MAX_X_INT = 2;
            private static final float SCALE_MAX_X = SCALE_MAX_X_INT;
            private static final int MINIMUM_ZOOM = 100;
            private static final float SPAN_THRESHOLD = 100f;

            private Bundle arguments;
            private Context context;
            private WebView imageView;
            private float beginScaleFactor;

            String episodeId;
            String filename;
            String link;
            int pageNumber;
            MyLoadTask loadTask;
            ScaleGestureDetector mScaleDetector;
            int originalZoom;
            private boolean scaleInProgress = false;
            List<String> links;
            MediaPlayer mediaPlayer;

            MyFragment(List<String> links) {
                this.links = links;
            }

            // @Override
            public void onCreate(Bundle savedInstanceState) {
                mScaleDetector = new ScaleGestureDetector(getContext(), this);
                restore();
            }

            public void onDestroy() {
                if (BuildConfig.DEBUG) { LOG_V("onDestroy(" + filename + ")"); }
                if (loadTask != null) {
                    loadTask.cancel(true);
                }
            }

            private void restore() {
                Bundle args = getArguments();
                if (args != null) {
                    filename = args.getString(FILENAME);
                    link = args.getString(LINK);
                    episodeId = args.getString(EPISODE_ID);
                    pageNumber = args.getInt(PAGE_NUMBER);
                }
            }

            public void setArguments(Bundle arguments, Context context) {
                this.arguments = arguments;
                this.context = context;
            }

            private Bundle getArguments() {
                return arguments;
            }

            private Context getContext() {
                return context;
            }

            public void onSaveInstanceState(Bundle outState) {
                if (outState == null) {
                    return;
                }
                outState.putString(FILENAME, filename);
                outState.putString(LINK, link);
            }

            // @Override
            public void onCreateView(MainActivity activity) {
                if (BuildConfig.DEBUG) { LOG_V("onCreateView(" + filename + ")"); }

                imageView = activity.findViewById(R.id.webview);

                // TODO: ovo u zavisnosti od orijentacije i da li prikazuje bukvalno ili prevod
                // activity.findViewById(R.id.bukvalno).setVisibility(View.VISIBLE);
                // ((WebView) activity.findViewById(R.id.bukvalno)).loadData("<html><head><title></title></head><body><p>1234</p></body></html>", "text/html", "UTF-8");
                // activity.findViewById(R.id.prevod).setVisibility(View.GONE);

                updateScaleFromPrefs(getContext(), imageView);
                imageView.loadData("<html><head><title></title></head><body><p>...</p></body></html>", "text/html", "UTF-8");
                ProgressBar progressBar = activity.findViewById(R.id.progressBar);
                progressBar.setVisibility(View.VISIBLE);
                imageView.setTag(new Pair<ProgressBar, String>(progressBar, null));
                imageView.setOnTouchListener(this);

                loadTask = new MyLoadTask(links, activity, episodeId, link, filename, imageView);
                loadTask.execute();
                potentialClick = false;
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

            private boolean potentialClick;
            private long lastDownMs = 0;
            private long CLICK_SPEED_MS = 200;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                if (BuildConfig.DEBUG) { LOG_V("onTouch(" + action + ")"); }
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        potentialClick = true;
                        lastDownMs = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_UP: {
                        long currentMs = System.currentTimeMillis();
                        if (currentMs > lastDownMs && currentMs - lastDownMs < CLICK_SPEED_MS) {
                            // TODO: start the service
                            Intent intent = new Intent(context, PlaybackService.class);
                            intent.setAction(PlaybackService.ACTION_PLAY);
                            context.startService(intent);
                        }
                        break;
                    }

                }
                // TODO: ovde dodati da svira muziku!
                return mScaleDetector.onTouchEvent(motionEvent) && scaleInProgress;
            }

            @Override
            public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
                return false;
            }

            private String getScaleKey(String axis) {
                // TODO: ovde dodati orijentaciju
                String orientation = "portrait";
                String key = axis + ".zoom." + orientation;
                return key;
            }

            private void updateScaleFromPrefs(Context context, @Nullable WebView imageView) {
                if (imageView == null) {
                    return;
                }
                SharedPreferences preferences = getSharedPreferences(context);
                int scaleX = normalizeZoom(preferences.getInt(getScaleKey(SCALE_X), MINIMUM_ZOOM + 30));
                if (scaleX != imageView.getSettings().getTextZoom()) {
                    if (BuildConfig.DEBUG) { LOG_V("updateScaleFromPrefs(" + scaleX + ")"); }
                    imageView.getSettings().setTextZoom(scaleX);
                    imageView.invalidate();
                }
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
                if (BuildConfig.DEBUG) { LOG_V("Scaling: onScaleBegin"); }
                beginScaleFactor = mScaleDetector.getScaleFactor();
                originalZoom = normalizeZoom(imageView.getSettings().getTextZoom());
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

                Context context = getContext();
                SharedPreferences preferences = getSharedPreferences(context);
                preferences.edit()
                        .putInt(getScaleKey(SCALE_X), newZoom)
                        .apply();
                updateScaleFromPrefs(context, imageView);
            }

            private static class MyLoadTask extends AsyncTask<Void, Void, String> {
                String imageFile;
                WeakReference<WebView> imageView;
                String link;
                WeakReference<Context> contextRef;
                String episodeId;
                int destinationViewWidth;
                int destinationViewHeight;
                List<String> links;

                MyLoadTask(List<String> links, Context context, String episodeId, String link, String imageFile, WebView imageView) {
                    this.imageFile = imageFile;
                    this.link = link;
                    this.links = links;
                    this.contextRef = new WeakReference<>(context);
                    this.imageView = new WeakReference<>(imageView);
                    this.episodeId = episodeId;
                    this.destinationViewHeight = imageView.getHeight();
                    this.destinationViewWidth = imageView.getWidth();
                }

                private static void addDirAndEpisodeDir(List<File> cacheDirs, File dir, String subdirName, String episodeId) {
                    if (dir == null) {
                        return;
                    }
                    cacheDirs.add(dir);
                    File subdir = new File(dir, subdirName);
                    if (!subdir.exists()) {
                        return;
                    }

                    File episodeDir = new File(subdir, episodeId);
                    if (!episodeDir.exists()) {
                        return;
                    }

                    cacheDirs.add(episodeDir);
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
                    addDirAndEpisodeDir(cacheDirs, context.getCacheDir(), EpisodeDownloadTask.EPISODES_FOLDER, episodeId);

                    addDirAndEpisodeDir(cacheDirs, new File(context.getCacheDir(), INTERNAL_OFFLINE), EpisodeDownloadTask.EPISODES_FOLDER, episodeId);
                    addDirAndEpisodeDir(cacheDirs, ExternalStorageHelper.getExternalCacheDir(context), EpisodeDownloadTask.EPISODES_FOLDER, episodeId);
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
                            if (getImageView() == null || isCancelled()) {
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

                    View destinationView = imageView.get();
                    if (destinationView == null) {
                        return null;
                    }
                    if (internetNotAvailable(context)) {
                        return null;
                    }

                    File imageToDownload = new File(context.getCacheDir(), imageFile);
                    if (isCancelled()) {
                        return null;
                    } else {
                        deleteOldSavedFiles(imageToDownload.getParentFile(), MAX_DOWNLOADED_IMAGES_ONLINE);
                        return DownloadAndSave.downloadAndSave(link, imageToDownload, destinationViewWidth, destinationViewHeight, 3);
                    }
                }

                private String mp3HelperVerifyFile(String absolutePath) {
                    // TODO: provera
                    return absolutePath;
                }

                private synchronized WebView getImageView() {
                    return imageView.get();
                }

                @Override
                protected void onPostExecute(String bitmap) {
                    WebView view = getImageView();
                    Pair<ProgressBar, String> progressBar = view != null ? (Pair<ProgressBar, String>) view.getTag() : null;
                    try {
                        if (BuildConfig.DEBUG) { LOG_V("onPostExecute(" + imageFile + "," + bitmap + ")"); }
                        if (bitmap == null && !isCancelled()) {
                            Context context = contextRef.get();
                            if (view != null && context != null && internetNotAvailable(context)) {
                                view.loadData("<html><head></head><body><p>Internet Problem</p></body></html>", "text/html", "UTF-8");
                            }
                            return;
                        }
                        if (view != null) {
                            if (BuildConfig.DEBUG) { LOG_V("Loading into ImageView(" + imageFile + ")"); }
                            view.loadData(createHtml(links, true, false), "text/html", "UTF-8");
                        }
                        if (progressBar != null) {
                            view.setTag(new Pair<>(progressBar.first, bitmap));
                        }
                    } finally {
                        if (progressBar != null && progressBar.first != null) {
                            progressBar.first.setVisibility(View.GONE);
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
