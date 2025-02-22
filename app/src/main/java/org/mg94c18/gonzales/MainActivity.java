package org.mg94c18.gonzales;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDelegate;
import android.util.Log;
import android.util.Pair;
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

import static org.mg94c18.gonzales.Logger.LOG_V;
import static org.mg94c18.gonzales.Logger.TAG;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String SHARED_PREFS_NAME = "config";
    static final String EPISODE_TITLE = "episode_title";
    static final String EPISODE_NUMBER = "episode_number";
    static final String EPISODE_AUTHOR = "episode_author";
    private static final String EPISODE_INDEX = "episode";
    static final String PLAYLIST_EPISODES_SET = "playlist_episodes_set";
    static final String PLAYLIST_TRACK = "playlist_track";
    static final String PLAYLIST_TRACK_OFFSET = "playlist_track_offset";
    private static final String DRAWER = "drawer";
    private static final String NIGHT_MODE = "night_mode";
    public static final String CYRILLIC_MODE = "cyrillic_mode";
    private static final String CONTACT_EMAIL = "yckopo@gmail.com";
    static final String MY_ACTION_VIEW = BuildConfig.APPLICATION_ID + ".VIEW";
    static final String INTERNAL_OFFLINE = "offline";

    PageAdapter pageAdapter;
    DrawerLayout drawerLayout;
    ListView drawerList;

    static List<String> titles = Collections.emptyList();
    static List<String> numbers = Collections.emptyList();
    static List<String> dates = Collections.emptyList();
    static List<String> numberAndTitle = Collections.emptyList();
    static boolean assetsLoaded = false;

    int selectedEpisode = 0;
    String selectedEpisodeTitle;
    String selectedEpisodeNumber;
    String selectedEpisodeAuthor;
    ActivityResultLauncher<String> requestPermissionLauncher;
    Intent playbackServiceIntent;
    AlertDialog configureDownloadDialog;
    AlertDialog quoteDialog;
    static final long syncIndex = -1;
    ActionBarDrawerToggle drawerToggle;
    private String downloadDialogTitle;
    private String previousProgressString;
    private String progressString;
    private static boolean nightModeAllowed = Build.VERSION.SDK_INT >= 29;
    private static String lastSearchedWord = "";
    private static String lastSearchedWordEpisode = "";

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
        String[] quotes = {SearchProvider.HIDDEN_TITLES.get(episode)};
        quoteDialog = new AlertDialog.Builder(this)
                .setTitle(SearchProvider.HIDDEN_TITLES.get(episode))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!SearchProvider.HIDDEN_TITLES.get(episode).equals("a3byka")) {
                            return;
                        }
                        SharedPreferences preferences = getSharedPreferences();
                        boolean cyrillic = preferences.getBoolean(CYRILLIC_MODE, false);
                        // 12-05 18:19:27.541 29825 30102 E dijaspora: Nijedna zora ne svane != Ниједна зора не сване
                        // Potiče iz SearchProvider-a, mada tamo koristim Log.wtf(); nema odakle drugo
                        // Zato probam u ovom slučaju commit(), da taj drugi thread
                        preferences.edit().putBoolean(CYRILLIC_MODE, !cyrillic).commit();
                        MainActivity.assetsLoaded = false;
                        SearchProvider.invalidateAssets();
                        recreate();
                    }
                })
                .setItems(quotes, null)
                .create();
        quoteDialog.setCanceledOnTouchOutside(false);
        quoteDialog.show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
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
        String searchedWord = "";
        try {
            int separatorIndex = epizodeStr.indexOf(SearchProvider.WORD_EPISODE_SEPARATOR);
            if (separatorIndex > 0) {
                searchedWord = epizodeStr.substring(0, separatorIndex);
                epizodeStr = epizodeStr.substring(separatorIndex + 1);
            }
            episode = Integer.parseInt(epizodeStr);
        } catch (NumberFormatException nfe) {
            Log.wtf(TAG, "Can't convert the episode ID", nfe);
            return false;
        }

        if (episode < 0) {
            showQuoteDialog(-1 * (episode + 1));
        } else {
            if (intent.hasExtra(EPISODE_TITLE) && intent.hasExtra(EPISODE_NUMBER) && intent.hasExtra(EPISODE_AUTHOR)) {
                boolean refresh = !titles.isEmpty();
                AssetLoader.handleAssetLoading(this);
                String title = refresh && titles.size() > episode ? titles.get(episode) : intent.getStringExtra(EPISODE_TITLE);
                String author = refresh && dates.size() > episode ? dates.get(episode) : intent.getStringExtra(EPISODE_AUTHOR);
                selectEpisode(searchedWord, episode, title, intent.getStringExtra(EPISODE_NUMBER), author);
                updateDrawer();
            } else {
                selectEpisode(searchedWord, episode);
            }
            drawerList.setSelection(episode);
        }

        if (BuildConfig.DEBUG) { LOG_V("handleNewIntent() returning true"); }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                startPlaybackService(this, playbackServiceIntent);
            }
        });

        if (BuildConfig.DEBUG) { LOG_V("onCreate"); }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);

        drawerList = findViewById(R.id.navigation);
        drawerList.setAdapter(new MyArrayAdapter(this, android.R.layout.simple_list_item_1));
        drawerList.setOnItemClickListener(this);

        if (!handleNewIntent(getIntent())) {
            if (BuildConfig.DEBUG) { LOG_V("handleNewIntent() returned false"); }
            EpisodeInfo episodeInfo = findSavedEpisode(savedInstanceState);
            boolean refresh = !titles.isEmpty();
            AssetLoader.handleAssetLoading(this);
            String title = refresh && titles.size() > episodeInfo.index ? titles.get(episodeInfo.index) : episodeInfo.title;
            String author = refresh && dates.size() > episodeInfo.index ? dates.get(episodeInfo.index) : episodeInfo.author;
            selectEpisode(episodeInfo.index, title, episodeInfo.number, author);

            updateDrawer();
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

    @Override
    public void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart");
        if (selectedEpisode >= 0) {
            selectEpisode(selectedEpisode);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onPause() {
        super.onPause();
        setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
    }

    public static void updateAssets(@NonNull List<String> titles, @NonNull List<String> numbers, @NonNull List<String> dates, List<String> hiddenTitles) {
        SearchProvider.TITLES = MainActivity.titles = titles;
        SearchProvider.NUMBERS = MainActivity.numbers = numbers;
        SearchProvider.DATES = MainActivity.dates = dates;
        SearchProvider.HIDDEN_TITLES = hiddenTitles;

        numberAndTitle = new ArrayList<>(numbers.size());
        for (int i = 0; i < numbers.size(); i++) {
            numberAndTitle.add("" + (i + 1) + ". " + titles.get(i));
        }

        assetsLoaded = true;
    }

    public void updateDrawer() {
        drawerList.setAdapter(new MyArrayAdapter(this, android.R.layout.simple_list_item_1));
        drawerList.setSelection(selectedEpisode);
        invalidateOptionsMenu();
    }

    @Override
    public void onStop() {
        if (BuildConfig.DEBUG) { LOG_V("onStop"); }
        super.onStop();
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
        if (BuildConfig.DEBUG) { LOG_V("onCreateOptionsMenu"); }

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

        updateDownloadButtons(menu);
        updateDarkModeButtons(menu);

        return super.onCreateOptionsMenu(menu);
    }

    private void updateDownloadButtons(Menu menu) {
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
                    String actionBarTitle = getResources().getString(R.string.tracks);
                    if (numbers != null && numbers.size() > 0) {
                        actionBarTitle = actionBarTitle + " " + 1 + "-" + numbers.size();
                    }
                    mySetActionBarTitle(actionBar, actionBarTitle);
                }
            };
            drawerLayout.addDrawerListener(drawerToggle);
            drawerToggle.syncState();
        }

        if (PlaybackService.inForeground) {
            menu.findItem(R.id.action_download_internal).setVisible(false);
            menu.findItem(R.id.action_cancel_download).setVisible(true);
        } else {
            menu.findItem(R.id.action_download_internal).setVisible(true);
            menu.findItem(R.id.action_cancel_download).setVisible(false);
            updateDownloadButtons(menu);
            progressString = null;
        }

        if (getPackageName().endsWith("antifon")) {
            menu.findItem(R.id.search).setVisible(false);
        } else {
            if (SearchProvider.deeperSearchReady()) {
                menu.findItem(R.id.search).setTitle(R.string.na_i_2);
            } else {
                menu.findItem(R.id.search).setTitle(R.string.na_i);
            }
        }

        boolean inLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        if (inLandscape && (!pageAdapter.bukvalno.isEmpty() || !pageAdapter.finalno.isEmpty())) {
            menu.findItem(R.id.action_toggle).setVisible(true);
        } else {
            menu.findItem(R.id.action_toggle).setVisible(false);
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
        int itemId = item.getItemId();
        if (itemId == R.id.action_email) {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:"));
            String[] emails = {CONTACT_EMAIL};
            emailIntent.putExtra(Intent.EXTRA_EMAIL, emails);
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name) + " App");
            if (emailIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(emailIntent);
            }
            return true;
        } else if (itemId == R.id.action_review) {
            Intent intent = PlayIntentMaker.createPlayIntent(this);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
            return true;
        } else if (itemId == R.id.search) {
            if (BuildConfig.DEBUG) { LOG_V("Search requested"); }
            onSearchRequested();
            return true;
        } else if (itemId == R.id.action_download_internal) {
            configureDownload(selectedEpisodeNumber);
            return true;
        } else if (itemId == R.id.action_cancel_download) {
            Intent stopIntent = new Intent(this, PlaybackService.class);
            stopIntent.setAction(PlaybackService.ACTION_STOP);
            startService(stopIntent);
            findViewById(R.id.button).setEnabled(true);
            return true;
        } else if (itemId == R.id.action_dark_mode) {
            boolean newNightMode = !getNightModeFromSharedPrefs(this);
            getSharedPreferences().edit().putBoolean(NIGHT_MODE, newNightMode).apply();
            possiblyUpdateForNightMode(this);
            return true;
        } else if (itemId == R.id.action_toggle) {
            pageAdapter.toggle();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private static boolean getNightModeFromSharedPrefs(Context context) {
        return getSharedPreferences(context).getBoolean(NIGHT_MODE, false);
    }

    public static void possiblyUpdateForNightMode(Context context) {
        if (!nightModeAllowed) {
            return;
        }
        boolean nightMode = getNightModeFromSharedPrefs(context);
        int mode = nightMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        if (BuildConfig.DEBUG) { LOG_V("setDefaultNightMode(" + mode + ")"); }

        int currentMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentMode == Configuration.UI_MODE_NIGHT_NO && mode == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(mode);
        } else if (currentMode == Configuration.UI_MODE_NIGHT_YES && mode == AppCompatDelegate.MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(mode);
        }
    }

    private void configureDownload(String episode) {
        dismissAlertDialogs();
        final MainActivity activity = this;
        final File destinationDir = ExternalStorageHelper.getMyCacheDir(this);
        final List<String> episodesToDelete = new ArrayList<>();

        if (destinationDir == null) {
            Log.wtf(TAG, "Destination is null");
            return;
        }

        final Set<String> completelyDownloadedEpisodes = getCompletelyDownloadedEpisodes(destinationDir, numbers);
        final Set<Integer> episodesToDownload = new HashSet<>();
        final List<String> namesToShow = new ArrayList<>();
        final List<Integer> indexesOfNamesToShow = new ArrayList<>();
        int indexToScrollTo = -1;
        for (int i = 0; i < numbers.size(); i++) {
            if (numbers.get(i).equals(episode)) {
                indexToScrollTo = indexesOfNamesToShow.size();
            }
            if (!completelyDownloadedEpisodes.contains(numbers.get(i))) {
                continue;
            }
            namesToShow.add(numberAndTitle.get(i));
            indexesOfNamesToShow.add(i);
        }
        final long averageMbPerEpisode = getResources().getInteger(R.integer.average_episode_size_mb);
        boolean[] checkedItems = new boolean[namesToShow.size()];
        SharedPreferences preferences = getSharedPreferences();
        Set<String> previousPlaylist = getSharedPreferences().getStringSet(PLAYLIST_EPISODES_SET, Collections.emptySet());
        if (BuildConfig.DEBUG) { LOG_V("Previous list: " + previousPlaylist); }
        for (int i = 0; i < checkedItems.length; i++) {
            int episodeId = indexesOfNamesToShow.get(i);
            checkedItems[i] = previousPlaylist.contains(numbers.get(episodeId));
            if (checkedItems[i]) {
                episodesToDownload.add(episodeId);
            }
        }
        if (BuildConfig.DEBUG) { LOG_V("episodesToDownload: " + episodesToDownload); }
        if (downloadDialogTitle == null) {
            downloadDialogTitle = getResources().getString(R.string.play);
        }
        configureDownloadDialog = new AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle(downloadDialogTitle)
                .setMultiChoiceItems(namesToShow.toArray(new String[0]), checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean checked) {
                        Integer actualIndex = indexesOfNamesToShow.get(i);
                        if (checked) {
                            episodesToDownload.add(actualIndex);
                            if (BuildConfig.DEBUG) { LOG_V("Added " + actualIndex); }
                        } else {
                            episodesToDownload.remove(actualIndex);
                            if (BuildConfig.DEBUG) { LOG_V("Removed " + actualIndex); }
                        }
                        Button positiveButton = configureDownloadDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                        long downloadMb = episodesToDownload.size() * averageMbPerEpisode;
                        if (episodesToDownload.size() > 0) {
                            positiveButton.setEnabled(true);
                        } else {
                            positiveButton.setEnabled(false);
                        }
                        updateDownloadDialogTitle(configureDownloadDialog, downloadDialogTitle, downloadMb);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(downloadDialogTitle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        if (episodesToDownload.isEmpty()) {
                            return;
                        }
                        if (BuildConfig.DEBUG) { LOG_V("episodesToDownload: " + episodesToDownload); }
                        Pair<int[], Set<String>> collectionPair = getIdCollections(episodesToDownload);
                        playbackServiceIntent = new Intent(MainActivity.this, PlaybackService.class);
                        playbackServiceIntent.setAction(PlaybackService.ACTION_PLAY);
                        playbackServiceIntent.putExtra(PlaybackService.EXTRA_IDS, collectionPair.first);
                        if (BuildConfig.DEBUG) { LOG_V("Saving the list: " + collectionPair.second); }
                        preferences.edit().putStringSet(PLAYLIST_EPISODES_SET, collectionPair.second).apply();
                        // https://developer.android.com/training/permissions/requesting
                        if (!canShowNotifications()) {
                            return;
                        }
                        startPlaybackService(MainActivity.this, playbackServiceIntent);
                        findViewById(R.id.button).setEnabled(false);
                    }
                })
                .create();
        configureDownloadDialog.show();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (episodesToDownload.isEmpty()) {
                    configureDownloadDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                }
            }
        });
        ListView listView = configureDownloadDialog.getListView();
        if (listView != null && indexToScrollTo != -1) {
            listView.setSelection(indexToScrollTo);
        }
    }

    private static void startPlaybackService(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    private boolean canShowNotifications() {
        String permission = "android.permission.POST_NOTIFICATIONS";
        // https://developer.android.com/training/permissions/requesting
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            quoteDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.rationale_title)
                    .setMessage(R.string.rationale)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            requestPermissionLauncher.launch(permission);
                        }
                    }).create();
            quoteDialog.setCanceledOnTouchOutside(false);
            quoteDialog.show();
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestPermissionLauncher.launch(permission);
        }
        return false;
    }

    static Pair<int[], Set<String>> getIdCollections(Set<Integer> ids) {
        Set<String> episodeNumbers = new HashSet<>();
        int[] episodeIds = new int[ids.size()];
        int i = 0;
        for (Integer episodeToDownload : ids) {
            episodeIds[i] = episodeToDownload;
            episodeNumbers.add(numbers.get(episodeIds[i]));
            i++;
        }
        Arrays.sort(episodeIds); // onClick može da promeni redosled
        return Pair.create(episodeIds, episodeNumbers);
    }

    private static Set<String> getCompletelyDownloadedEpisodes(File cacheDir, List<String> numbers) {
        Map<String, String> mp3FileForNumber = new HashMap<>();
        for (int i = 0; i < numbers.size(); i++) {
            mp3FileForNumber.put(DownloadAndSave.fileNameFromNumber(numbers.get(i)), numbers.get(i));
        }
        File[] files = cacheDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s != null && s.endsWith(".mp3");
            }
        });
        if (files == null) {
            return Collections.emptySet();
        }
        Set<String> downloadedEpisodes = new HashSet<>();
        for (File file : files) {
            if (file == null) {
                continue;
            }
            String number = mp3FileForNumber.get(file.getName());
            if (number == null) {
                continue;
            }
            downloadedEpisodes.add(number);
        }
        return downloadedEpisodes;
    }

    private static void updateDownloadDialogTitle(AlertDialog configureDownloadDialog, String plainTitle, long downloadMb) {
        String title = plainTitle;
        if (downloadMb > 0) {
            // TODO: ovde prebaciti da daje približno trajanje
            // title = title + String.format(Locale.US, " (oko %d MB)", downloadMb);
        }
        configureDownloadDialog.setTitle(title);
    }

    @Override
    protected void onSaveInstanceState(Bundle instanceState) {
        // TODO: zašto se WebView ne vraća na stari scroll ako ugasim pa upalim ekran?

        if (BuildConfig.DEBUG) { LOG_V("onSaveInstanceState"); }
        if (BuildConfig.DEBUG) { LOG_V("Saving selectedEpisode=" + selectedEpisode); }
        instanceState.putInt(EPISODE_INDEX, selectedEpisode);
        instanceState.putString(EPISODE_TITLE, selectedEpisodeTitle);
        instanceState.putString(EPISODE_NUMBER, selectedEpisodeNumber);
        instanceState.putString(EPISODE_AUTHOR, selectedEpisodeAuthor);
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
        String author;
        int index;

        @Override
        public String toString() {
            return "{" + index + ": '" + title + "' (" + number + ")}";
        }
    }

    private @NonNull EpisodeInfo findSavedEpisode(Bundle state) {
        EpisodeInfo episodeInfo = new EpisodeInfo();

        if (state != null && state.containsKey(EPISODE_INDEX) && state.containsKey(EPISODE_TITLE) && state.containsKey(EPISODE_NUMBER) && state.containsKey(EPISODE_AUTHOR)) {
            episodeInfo.index = state.getInt(EPISODE_INDEX);
            episodeInfo.title = state.getString(EPISODE_TITLE);
            episodeInfo.number = state.getString(EPISODE_NUMBER);
            episodeInfo.author = state.getString(EPISODE_AUTHOR);
            if (BuildConfig.DEBUG) { LOG_V("Loaded episode from bundle: " + episodeInfo); }
        } else {
            SharedPreferences preferences = getSharedPreferences();
            String defaultTitle = getResources().getString(R.string.default_episode_title);
            String defaultNumber = getResources().getString(R.string.default_episode_number);
            String defaultAuthor = getResources().getString(R.string.default_episode_author);
            if (preferences.contains(EPISODE_TITLE) && preferences.contains(EPISODE_NUMBER) && preferences.contains(EPISODE_AUTHOR) && numberExists(preferences.getString(EPISODE_NUMBER, null))) {
                episodeInfo.title = preferences.getString(EPISODE_TITLE, defaultTitle);
                episodeInfo.number = preferences.getString(EPISODE_NUMBER, defaultNumber);
                episodeInfo.author = preferences.getString(EPISODE_AUTHOR, defaultAuthor);
            } else {
                episodeInfo.title = defaultTitle;
                episodeInfo.number = defaultNumber;
                episodeInfo.author = defaultAuthor;
            }
            episodeInfo.index = preferences.getInt(EPISODE_INDEX, getResources().getInteger(R.integer.default_episode_index));
            if (BuildConfig.DEBUG) { LOG_V("Loaded episode from shared prefs: " + episodeInfo); }
        }
        return episodeInfo;
    }

    private boolean numberExists(String number) {
        if (number == null) {
            return false;
        }
        if (number.equals("1") || number.equals("36")) {
            return false;
        }
        // TODO: dodati ovde obrisane brojeve pa da ga baci na default
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (BuildConfig.DEBUG) { LOG_V("onItemClick: " + position); }
        drawerList.setItemChecked(position, !drawerList.isItemChecked(position));
        drawerLayout.closeDrawer(drawerList);
        selectEpisode(position);
    }

    private void selectEpisode(int position) {
        selectEpisode("", position);
    }

    private void selectEpisode(String searchedWord, int position) {
        selectEpisode(searchedWord, position, titles.get(position), numbers.get(position), dates.get(position));
    }

    private @NonNull ActionBar getMyActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            throw new Error("Unexpected");
        }
        return actionBar;
    }

    private void selectEpisode(int position, String title, String number, String author) {
        selectEpisode("", position, title, number, author);
    }

    private void selectEpisode(String searchedWord, int position, String title, String number, String author) {
        if (lastSearchedWordEpisode.equals(number) && !lastSearchedWord.isEmpty() && searchedWord.isEmpty()) {
            searchedWord = lastSearchedWord;
        }
        lastSearchedWord = searchedWord;
        lastSearchedWordEpisode = number;
        if (position >= 0) {
            selectedEpisodeTitle = title;
            selectedEpisodeNumber = number;
            selectedEpisodeAuthor = author;
        }
        mySetActionBarTitle(getMyActionBar(), title);
        destroyPageAdapter();
        pageAdapter = new PageAdapter(this, number, author, searchedWord);
        if (position >= 0) {
            selectedEpisode = position;
            if (BuildConfig.DEBUG) { LOG_V("Saving episode " + selectedEpisode); }
            getSharedPreferences().edit()
                    .putInt(EPISODE_INDEX, selectedEpisode)
                    .putString(EPISODE_TITLE, title)
                    .putString(EPISODE_AUTHOR, author)
                    .putString(EPISODE_NUMBER, number)
                    .apply();

            Intent intent = getIntent();
            if (intent != null) {
                if (BuildConfig.DEBUG) { LOG_V("Saving episode " + selectedEpisode + " in getIntent()"); }
                updateIntentWithEpisode(intent, selectedEpisode, title, author, number);
            }
        }
    }

    static void updateIntentWithEpisode(Intent activityIntent, int episodeId, String title, String author, String number) {
        activityIntent.putExtra(SearchManager.EXTRA_DATA_KEY, Integer.toString(episodeId));
        activityIntent.putExtra(MainActivity.EPISODE_TITLE, title);
        activityIntent.putExtra(MainActivity.EPISODE_AUTHOR, author);
        activityIntent.putExtra(MainActivity.EPISODE_NUMBER, number);
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
