package org.mg94c18.gonzales;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Build;
import android.provider.BaseColumns;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;

import static org.mg94c18.gonzales.Logger.LOG_V;
import static org.mg94c18.gonzales.Logger.TAG;

public class SearchProvider extends ContentProvider {
    private static final int MINIMUM_RESULTS = 16;
    public static List<String> TITLES = Collections.emptyList();
    public static List<String> NUMBERS = Collections.emptyList();
    public static List<String> DATES = Collections.emptyList();
    public static List<String> HIDDEN_TITLES = Collections.emptyList();

    public static final boolean ALLOW_MANUAL_SYNC = BuildConfig.DEBUG || "Amazon".equals(Build.MANUFACTURER);

    private static String[] MANDATORY_COLUMNS = {
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2,
            SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA
    };

    public static synchronized boolean deeperSearchReady() {
        return trie != null;
    }

    private static class Position {
        @Override
        public int hashCode() {
            return (title + episodeId).hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (!(o instanceof Position)) {
                return false;
            }
            Position that = (Position) o;
            return this.episodeId == that.episodeId && this.title.equals(that.title);
        }

        int episodeId;
        String title;
    }

    private static class Node {
        public Node() {
            children = new HashMap<>();
            results = new HashMap<>();
        }
        Map<String, Node> children;
        Map<String, Set<Position>> results;
    }

    private static Node trie = null;
    private static boolean threadKickedOff = false;
    private static Node lastNode = null;
    private static String lastQuery = "";

    public static final Pattern splitPattern = Pattern.compile("[\\[\\] .,!?\\|¡¿:;\"\\(\\)'\\-_\\{\\}]");
    public static final Pattern htmlTags = Pattern.compile("(<[^>]+>)|(\\{[^\\{\\}]+\\})");

    public static void populateTrie(Context context, List<String> numbers, List<String> titles) {
        synchronized (SearchProvider.class) {
            if (SearchProvider.trie != null) {
                Log.wtf(TAG, "Already called");
                return;
            }
            if (SearchProvider.threadKickedOff) {
                Log.wtf(TAG, "Already called");
                return;
            }
            SearchProvider.threadKickedOff = true;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (numbers.size() != titles.size()) {
                    Log.wtf(TAG, "Unexpected: " + numbers.size() + "!=" + titles.size());
                    return;
                }
                Log.i(TAG, "populateTrie: begin");
                Node newTrie = new Node();
                String number;
                List<String> lines;
                for (int i = 0; i < numbers.size(); i++) {
                    number = numbers.get(i);
                    lines = AssetLoader.loadFromAssetOrUpdate(context, number, MainActivity.syncIndex);
                    for (int j = 2; j < lines.size(); j++) {
                        String line = htmlTags.matcher(lines.get(j)).replaceAll("").toLowerCase();
                        line = PageAdapter.hintsPattern.matcher(line).replaceAll("");
                        String[] words = splitPattern.split(line);
                        if (words == null) {
                            continue;
                        }

                        for (String word : words) {
                            Position position = new Position();
                            position.episodeId = i;
                            position.title = titles.get(i);
                            //if (BuildConfig.DEBUG) { LOG_V("insertWord(" + word + "," + position.number + ")"); }
                            if (word.isBlank()) {
                                continue;
                            }
                            insertWord(newTrie, word, word, position);
                        }
                    }
                }
                synchronized (SearchProvider.class) {
                    SearchProvider.trie = newTrie;
                }
                Log.i(TAG, "populateTrie: end");
            }
        }).start();
    }

    private static void insertWord(Node node, String finalWord, String remaining, Position position) {
        if (remaining.isEmpty()) {
            Set<Position> results = node.results.get(finalWord);
            if (results == null) {
                results = new HashSet<>();
                // if (BuildConfig.DEBUG) { LOG_V("Inserting: " + finalWord); }
                node.results.put(finalWord, results);
            }
            // if (BuildConfig.DEBUG) { LOG_V("Adding position '" + position.title + "' to '" + finalWord + "'");}
            results.add(position);
            return;
        }
        String first = remaining.substring(0, 1);
        String rest = remaining.substring(1);

        Node child = node.children.get(first);
        if (child == null) {
            child = new Node();
            // if (BuildConfig.DEBUG) { LOG_V("Growing trie for: " + first); }
            node.children.put(first, child);
        }
        insertWord(child, finalWord, rest, position);
    }

    private static int trieQuery(String query, Node node, MatrixCursor cursor, Set<Position> positionsAdded) {
        // if (BuildConfig.DEBUG) { LOG_V("trieQuery(" + query + ")"); }
        int resultCount = 0;
        if (node == null) {
            // if (BuildConfig.DEBUG) { LOG_V("Nothing here"); }
            return resultCount;
        }
        if (query.isEmpty()) {
            lastNode = node;
            resultCount += addThisNode(node, cursor, positionsAdded);
        } else {
            String first = query.substring(0, 1);
            String rest = query.substring(1);
            Node child = node.children.get(first);
            // if (BuildConfig.DEBUG) { LOG_V("Searching for '" + rest + "' under '" + first + "'"); }
            resultCount += trieQuery(rest, child, cursor, positionsAdded);
        }
        Queue<Node> fillUpNodes = new ArrayDeque<>();
        fillUpNodes.add(node);
        while (!fillUpNodes.isEmpty() && resultCount < MINIMUM_RESULTS) {
            Node fillUpNode = fillUpNodes.poll();
            for (Map.Entry<String, Node> entry : fillUpNode.children.entrySet()) {
                fillUpNodes.add(entry.getValue());
                resultCount += addThisNode(entry.getValue(), cursor, positionsAdded);
            }
        }
        return resultCount;
    }

    private static int addThisNode(Node node, MatrixCursor cursor, Set<Position> positionsAdded) {
        int resultCount = 0;
        for (Map.Entry<String, Set<Position>> entry : node.results.entrySet()) {
            String finalWord = entry.getKey();
            Set<Position> positions = entry.getValue();

            for (Position position : positions) {
                if (positionsAdded.contains(position)) {
                    continue;
                }

                // if (BuildConfig.DEBUG) { LOG_V("Adding result: " + position.title + "," + position.episodeId + " for " + finalWord); }
                MatrixCursor.RowBuilder builder = cursor.newRow();
                builder.add(resultCount++); // BaseColumns._ID
                builder.add(finalWord); // SearchManager.SUGGEST_COLUMN_TEXT_1
                // TODO: ovo sad otrkiva interno preslikavanje ali će i tako da se promeni kad promenim search
                builder.add(position.title); // SearchManager.SUGGEST_COLUMN_TEXT_2
                builder.add(position.episodeId); // SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA

                positionsAdded.add(position);
            }
        }
        return resultCount;
    }

    private int stdQuery(String query, MatrixCursor cursor) {
        int resultCount = 0;
        Set<String> querySet = new HashSet<>();
        querySet.add(query);
        querySet.add(query.replace('c', 'ć'));
        querySet.add(query.replace('s', 'š'));
        querySet.add(query.replace('c', 'č'));
        querySet.add(query.replace('z', 'ž'));
        querySet.add(query.replace("dj", "đ"));
        querySet.add(query.replace("e", "je"));
        querySet.add(query.replace("e", "ije"));
        querySet.add(query.replace("je", "e"));
        querySet.add(query.replace("ije", "e"));

        List<String> titlesLowercase = new ArrayList<>(TITLES.size());
        for (String title : TITLES) {
            titlesLowercase.add(title.toLowerCase());
        }

        for (int i = 0; i < TITLES.size(); i++) {
            boolean addThis = false;
            for (String q : querySet) {
                if (titlesLowercase.get(i).contains(q)) {
                    addThis = true;
                    break;
                }
            }

            if (addThis) {
                resultCount++;
                MatrixCursor.RowBuilder builder = cursor.newRow();
                builder.add(i); // BaseColumns._ID
                builder.add(TITLES.get(i)); // SearchManager.SUGGEST_COLUMN_TEXT_1
                // TODO: ovo sad otrkiva interno preslikavanje ali će i tako da se promeni kad promenim search
                builder.add("broj " + NUMBERS.get(i) + ", " + DATES.get(i)); // SearchManager.SUGGEST_COLUMN_TEXT_2
                builder.add(Integer.toString(i)); // SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA
            }
        }

        if (ALLOW_MANUAL_SYNC) {
            if (resultCount == 0 && query.equals("sync")) {
                MatrixCursor.RowBuilder builder = cursor.newRow();
                builder.add(0); // BaseColumns._ID
                builder.add("Osveži spisak epizoda"); // SearchManager.SUGGEST_COLUMN_TEXT_1
                builder.add("Za troubleshooting"); // SearchManager.SUGGEST_COLUMN_TEXT_2
                builder.add(query); // SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA
            }
        }
        return resultCount;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    // Ovde dodati da ima drvo gde su grane označene "karakterima", zapravo stringovima.
    // I onda dodavanje treba da ima dodaj(ostatak reči, krajnja reč, pozicija) a da za zapravo doda svaku od kombinacija sa jekavskim, takođe ako neko traži c da nađe i ćč i slično
    // ili u španskom ako neko traži a da takođe nađe á
    // A? a... i a nisu iste reči, možda koristiti naglaske takođe
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] unused1, @Nullable String unused2, @Nullable String[] unused3, @Nullable String unused4) {
        MatrixCursor cursor = new MatrixCursor(MANDATORY_COLUMNS);
        if (uri.getLastPathSegment() == null) {
            return cursor;
        }

        String query = uri.getLastPathSegment().toLowerCase();
        if (query.compareTo(SearchManager.SUGGEST_URI_PATH_QUERY) == 0) {
            return cursor;
        }

        if (BuildConfig.DEBUG) { LOG_V("query(" + query + ")"); }

        Node searchFrom;
        String searchWhat;
        synchronized (SearchProvider.class) {
            if (trie == null) {
                searchFrom = null;
                searchWhat = query;
            } else {
                // TODO: dodati i za backspace, da može da se vrati na prethodno
                if (query.startsWith(lastQuery) && lastNode != null) {
                    if (BuildConfig.DEBUG) { LOG_V("Using previous subtree"); }
                    searchWhat = query.substring(lastQuery.length());
                    searchFrom = lastNode;
                } else {
                    searchWhat = query;
                    searchFrom = trie;
                    lastNode = null;
                }
            }
            lastQuery = query;
        }
        int resultCount = 0;
        if (searchFrom != null) {
            resultCount = trieQuery(searchWhat, searchFrom, cursor, new HashSet<>());
        } else {
            resultCount = stdQuery(searchWhat, cursor);
        }

        if (resultCount == 0) {
            tryAddingHiddenResults(Set.of(query), cursor);
        }

        return cursor;
    }

    private void tryAddingHiddenResults(Set<String> querySet, MatrixCursor cursor) {
        Context context = getContext();
        if (context == null) {
            return;
        }

        for (int i = 0; i < HIDDEN_TITLES.size(); i++) {
            boolean addThis = false;
            for (String query : querySet) {
                if (HIDDEN_TITLES.get(i).equals(query)) {
                    addThis = true;
                    break;
                }
            }
            if (addThis) {
                MatrixCursor.RowBuilder builder = cursor.newRow();
                builder.add(i); // BaseColumns._ID
                builder.add(HIDDEN_TITLES.get(i)); // SearchManager.SUGGEST_COLUMN_TEXT_1
                builder.add(""); // SearchManager.SUGGEST_COLUMN_TEXT_2
                builder.add(Integer.toString(-1 * (i + 1))); // SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA
            }
        }
    }

    private static boolean nonAlphaExists(String word) {
        String rest = word;
        while (!rest.isEmpty()) {
            String first = rest.substring(0, 1);
            if (!Character.isAlphabetic(first.codePointAt(0))) {
                return true;
            }
            rest = rest.substring(1);
        }
        return false;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
