package org.mg94c18.gonzales;

import android.content.Context;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;

import java.util.List;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class WordPatternTest {
    private static boolean surpiseCharacterExists(String word) {
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
                    Assert.assertFalse("Should have no surprise characters: '" + word + "' in " + number, surpiseCharacterExists(word));
                }
            }
        }
    }

    @Test
    public void DISABLED_testOneToOne() {
        // očajanja -> [of desperation] treba da bude 1:1
    }
}
