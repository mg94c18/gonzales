package org.mg94c18.gonzales;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

// Ovaj je nezavisan od flavor
public class HostUnitTest {
    // for n in $(cat app/src/dijaspora/assets/numbers | grep -v abvgd) titles dates; do echo $n; cat app/src/dijaspora/assets/$n | /Applications/Android\ Studio.app/Contents/jbr/Contents/Home/bin/java -classpath . a3byka.Hijeroglif > app/src/dijaspora/assets/$n.cirilica; done
    @Test
    public void originalCyrillicIsUpToDate() throws Exception {
        String assetsDir = System.getProperty("user.dir") + "/src/dijaspora/assets/";
        Scanner numbers = new Scanner(new FileInputStream(assetsDir + "numbers"));

        // Skip, and also verify it's not empty
        Assert.assertTrue(numbers.hasNextLine());
        Assert.assertEquals("abvgd", numbers.nextLine());

        String number;
        while (numbers.hasNextLine()) {
            number = numbers.nextLine();
            Assert.assertTrue(number, firstLineMatches(assetsDir + number, assetsDir + number + AssetLoader.CYRILLIC_SUFFIX));
            Assert.assertTrue(number, fileIsOlder(assetsDir + number, assetsDir + number + AssetLoader.CYRILLIC_SUFFIX));
        }

        List<String> otherAssets = List.of("titles", "dates");
        for (String a : otherAssets) {
            Assert.assertTrue(a, fileIsOlder(assetsDir + a, assetsDir + a + ".cirilica"));
        }
        numbers.close();
    }

    // for n in $(cat app/src/gonzales/assets/numbers | grep -B 100 nosdieron) ; do for p in bukvalno finalno; do echo $n; cat app/src/gonzales/assets/$n.$p | /Applications/Android\ Studio.app/Contents/jbr/Contents/Home/bin/java -cp . a3byka.Hijeroglif > app/src/gonzales/assets/$n.$p.cirilica; done; done
    @Test
    public void translationCyrillicIsUpToDate() throws Exception {
        String assetsDir = System.getProperty("user.dir") + "/src/gonzales/assets/";
        Scanner numbers = new Scanner(new FileInputStream(assetsDir + "numbers"));
        int checkedCount = 0;
        List<String> translations = List.of(".bukvalno", ".finalno");

        String number;
        String numberTranslationPath;
        while (numbers.hasNextLine()) {
            number = numbers.nextLine();
            for (String translation : translations) {
                numberTranslationPath = assetsDir + number + translation;
                if (fileExists(numberTranslationPath + AssetLoader.CYRILLIC_SUFFIX)) {
                    Assert.assertTrue(number, fileIsOlder(numberTranslationPath, numberTranslationPath + AssetLoader.CYRILLIC_SUFFIX));
                    checkedCount++;

                    // Možda ovo u neki drugi test, jer ovako su popravke testeraste
                    Assert.assertFalse(numberTranslationPath, fileContainsAnyOf(new File(numberTranslationPath), Set.of("¿", "¡")));
                }
            }
        }
        numbers.close();
        Assert.assertTrue(checkedCount >= 2 * COUNT_GONZALES_RELEASE);
    }

    private static int COUNT_GONZALES_RELEASE = 32;
    private static int COUNT_ENGLEZ_RELEASE = 40;

    @Test
    public void testNoLeftoverCharactersSanity() throws Exception {
        // Pišonja, Žuga...  Ne vredi baš da stavljam slova
        // testNoLeftoverCharacters("dijaspora", Set.of("đ", "ž", "ć", "č", "š", "Đ", "Ž", "Ć", "Č", "Š"), 82);
        testNoLeftoverCharacters("dijaspora", Set.of("ß"), 2 * COUNT_ENGLEZ_RELEASE); // Option+S umesto Cmd+S
        testNoLeftoverCharacters("gonzales", Set.of("¿", "¡", "ß", "y"), 2 * COUNT_GONZALES_RELEASE);
        testNoLeftoverCharacters("gonzales", Set.of("takođe", "]", "["), COUNT_GONZALES_RELEASE, List.of(".finalno"));
        testNoLeftoverCharacters("dijaspora", Set.of("]", "["), COUNT_ENGLEZ_RELEASE, List.of(".finalno"));
    }

    private void testNoLeftoverCharacters(String flavor, Set<String> nonGratas, int expectedCount) throws FileNotFoundException {
        testNoLeftoverCharacters(flavor, nonGratas, expectedCount, List.of(".bukvalno", ".finalno"));
    }

    private void testNoLeftoverCharacters(String flavor, Set<String> nonGratas, int expectedCount, List<String> translations) throws FileNotFoundException {
        String assetsDir = System.getProperty("user.dir") + "/src/" + flavor + "/assets/";
        Scanner numbers = new Scanner(new FileInputStream(assetsDir + "numbers"));
        int checkedCount = 0;

        String number;
        String numberTranslationPath;
        while (numbers.hasNextLine()) {
            number = numbers.nextLine();
            if (number.equals("abvgd")) {
                continue;
            }
            for (String translation : translations) {
                numberTranslationPath = assetsDir + number + translation;
                Assert.assertFalse(numberTranslationPath, fileContainsAnyOf(new File(numberTranslationPath), nonGratas));
                checkedCount++;
            }

            if (checkedCount >= expectedCount) {
                break;
            }
        }
        numbers.close();
        Assert.assertEquals(expectedCount, checkedCount);
    }

    private static boolean fileExists(String path) {
        return new File(path).exists();
    }

    private static boolean fileContainsAnyOf(File f, Set<String> nonGratas) throws FileNotFoundException {
        Scanner scanner = new Scanner(new FileInputStream(f));

        String line;
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            for (String nonGrata : nonGratas) {
                if (line.contains(nonGrata)) {
                    System.err.println("Found " + nonGrata);
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean firstLineMatches(String path1, String path2) throws FileNotFoundException {
        File file1 = new File(path1);
        File file2 = new File(path2);
        Scanner scanner1 = new Scanner(new FileInputStream(file1));
        Scanner scanner2 = new Scanner(new FileInputStream(file2));

        if (!scanner1.hasNextLine() || !scanner2.hasNextLine()) {
            return false;
        }

        String line1 = scanner1.nextLine();
        boolean matches = line1.equals(scanner2.nextLine());

        scanner1.close();
        scanner2.close();

        return matches && !line1.startsWith("http");
    }

    private static boolean fileIsOlder(String path1, String path2) {
        File file1 = new File(path1);
        File file2 = new File(path2);

        return file1.exists() && file2.exists() && file1.lastModified() < file2.lastModified();
    }
}
