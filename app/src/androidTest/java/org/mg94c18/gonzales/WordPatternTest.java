package org.mg94c18.gonzales;

import static org.mg94c18.gonzales.MainActivity.syncIndex;

import android.content.Context;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class WordPatternTest {
    private static boolean surpiseCharacterExists(String number, String word) {
        String rest = word;
        while (!rest.isEmpty()) {
            String first = rest.substring(0, 1);
            if (!Character.isAlphabetic(first.codePointAt(0))) {
                return true;
            }
            if (allowedNonPlainKeys.contains(first) && !number.equals("abvgd")) {
                return true;
            }
            rest = rest.substring(1);
        }
        return false;
    }

    @Test
    public void testNoSurpriseCharacters() {
        Context context = ApplicationProvider.getApplicationContext();
        Assert.assertNotNull(context);

        List<String> numbers = AssetLoader.loadFromAssetOrUpdate(context, AssetLoader.NUMBERS, MainActivity.syncIndex);
        for (String number : numbers) {
            List<String> lines = AssetLoader.loadFromAssetOrUpdate(context, number, MainActivity.syncIndex);
            Assert.assertNotNull(context);
            Assert.assertFalse(lines.isEmpty());

            String line;
            String[] words;
            for (int i = 2; i < lines.size(); i++) {
                line = lines.get(i);
                line = SearchProvider.htmlTags.matcher(line).replaceAll("");
                words = SearchProvider.splitPattern.split(line);
                for (String word : words) {
                    Assert.assertFalse("Should have no surprise characters: '" + word + "' in " + number, surpiseCharacterExists(number, word));
                }
            }
        }
    }

    @Test
    public void testOneToOne() {
        // očajanja -> [of desperation] treba da bude 1:1
        Context context = ApplicationProvider.getApplicationContext();
        Assert.assertNotNull(context);

        List<String> numbers = AssetLoader.loadFromAssetOrUpdate(context, AssetLoader.NUMBERS, MainActivity.syncIndex);
        for (String number : numbers) {
            List<String> lines = AssetLoader.loadFromAssetOrUpdate(context, number, MainActivity.syncIndex);
            List<String> bukvalno = AssetLoader.loadFromAssetOrUpdate(context, number + ".bukvalno", MainActivity.syncIndex);
            List<String> finalno = AssetLoader.loadFromAssetOrUpdate(context, number + ".finalno", MainActivity.syncIndex);

            Assert.assertFalse(lines.isEmpty());
            Assert.assertFalse(bukvalno.isEmpty());
            Assert.assertFalse(finalno.isEmpty());

            Assert.assertTrue(number,lines.size() == bukvalno.size() || bukvalno.size() == 3);
            Assert.assertTrue(number,lines.size() == finalno.size() || finalno.size() == 3);

            Assert.assertTrue(number, finalno.get(0).isEmpty());
            Assert.assertTrue(number, finalno.get(1).isEmpty());
            Assert.assertTrue(number, bukvalno.get(0).isEmpty());
            Assert.assertTrue(number, bukvalno.get(1).isEmpty());

            if (bukvalno.size() > 3) {
                for (int i = 2; i < bukvalno.size(); i++) {
                    Assert.assertTrue(lines.get(i) + "->" + bukvalno.get(i), wordGroupingsMatch(lines.get(i), bukvalno.get(i)));
                }
            }
            if (finalno.size() > 3) {
                for (int i = 2; i < finalno.size(); i++) {
                    String message = "'" + lines.get(i) + "' -> '" + finalno.get(i) + "'";
                    if (lines.get(i).isBlank()) {
                        Assert.assertTrue(message, finalno.get(i).isBlank());
                    } else {
                        Assert.assertFalse(message, finalno.get(i).isBlank());
                    }
                }
            }
        }
    }

    private static final Pattern groupingPattern = Pattern.compile("\\[[^\\]]+\\]"); // [je rekao]
    private static final Pattern translationHintPattern = Pattern.compile("\\([^\\)]+\\)"); // he (the smile)
    private static final Pattern noTranslationPattern = Pattern.compile("[_¿]"); // ¿ alone tonight?
    private static final Pattern insideWordSeparator = Pattern.compile("['\\-]"); // self's, passers-by, WC-a

    private static boolean wordGroupingsMatch(String tekst, String prevod) {
        tekst = groupingPattern.matcher(tekst).replaceAll("group");
        tekst = PageAdapter.hintsPattern.matcher(tekst).replaceAll("");
        tekst = noTranslationPattern.matcher(tekst).replaceAll("noot");
        tekst = insideWordSeparator.matcher(tekst).replaceAll("");
        prevod = noTranslationPattern.matcher(prevod).replaceAll("noot");
        prevod = groupingPattern.matcher(prevod).replaceAll("group");
        prevod = PageAdapter.hintsPattern.matcher(prevod).replaceAll("");
        prevod = translationHintPattern.matcher(prevod).replaceAll("");
        prevod = insideWordSeparator.matcher(prevod).replaceAll("");

        tekst = SearchProvider.htmlTags.matcher(tekst).replaceAll("");
        prevod = SearchProvider.htmlTags.matcher(prevod).replaceAll("");
        String[] wordsInText = SearchProvider.splitPattern.split(tekst);
        String[] wordsInTranslation = SearchProvider.splitPattern.split(prevod);

        List<String> inText = new ArrayList<>(Arrays.asList(wordsInText));
        List<String> inTranslation = new ArrayList(Arrays.asList(wordsInTranslation));

        while (inText.remove(""));
        while (inTranslation.remove(""));

        if (inTranslation.size() == inText.size()) {
            return true;
        } else {
            // For the breakpoint :)
            return false;
        }
    }

    private static Set<String> allowedNonPlainKeys = Set.of("ǆ", "ǉ", "ǌ");
    @Test
    public void testTrieKeysArePlain() {
        // č -> da se nalazi pod 'c'
        Context context = ApplicationProvider.getApplicationContext();
        Assert.assertNotNull(context);

        List<String> numbers = AssetLoader.loadFromAssetOrUpdate(context, AssetLoader.NUMBERS, MainActivity.syncIndex);
        List<String> titles = AssetLoader.loadFromAssetOrUpdate(context, AssetLoader.TITLES, syncIndex);
        SearchProvider.populateTrieBlocking(context, numbers, titles);

        Set<String> nonPlainKeys = SearchProvider.nonPlainKeys();
        Assert.assertNotNull(nonPlainKeys);

        nonPlainKeys.removeAll(allowedNonPlainKeys);
        Assert.assertTrue(nonPlainKeys.toString(), nonPlainKeys.isEmpty());

        int wc = SearchProvider.wordCount();
        Assert.assertTrue("" + wc, wc > (context.getPackageName().endsWith("dijaspora") ? 3236 : 1799));
    }

    @Test
    public void testLinesMatch() {
        Context context = ApplicationProvider.getApplicationContext();
        Assert.assertNotNull(context);

        List<String> numbers = AssetLoader.loadFromAssetOrUpdate(context, AssetLoader.NUMBERS, MainActivity.syncIndex);
        for (String number : numbers) {
            List<String> lines = AssetLoader.loadFromAssetOrUpdate(context, number, MainActivity.syncIndex);
            List<String> bukvalno = AssetLoader.loadFromAssetOrUpdate(context, number + ".bukvalno", MainActivity.syncIndex);
            List<String> finalno = AssetLoader.loadFromAssetOrUpdate(context, number + ".finalno", MainActivity.syncIndex);

            Assert.assertFalse(lines.isEmpty());
            Assert.assertFalse(bukvalno.isEmpty());
            Assert.assertFalse(finalno.isEmpty());

            Assert.assertTrue(number,lines.size() == bukvalno.size() || bukvalno.size() == 3);
            Assert.assertTrue(number,lines.size() == finalno.size() || finalno.size() == 3);
        }
    }
}
