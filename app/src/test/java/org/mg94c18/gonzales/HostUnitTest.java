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

public class HostUnitTest {
    // for n in $(cat app/src/dijaspora/assets/numbers | grep -v abvgd) titles dates; do echo $n; cat app/src/dijaspora/assets/$n | /Applications/Android\ Studio.app//Contents/jbr/Contents/Home/bin/java -classpath . a3byka.Hijeroglif > app/src/dijaspora/assets/$n.cirilica; done
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

    // for n in $(cat app/src/gonzales/assets/numbers | grep -B 100 shakira | grep -vE "chatarra") ; do for p in bukvalno finalno; do echo $n; cat app/src/gonzales/assets/$n.$p | /Applications/Android\ Studio.app//Contents/jbr/Contents/Home/bin/java -cp . a3byka.Hijeroglif > app/src/gonzales/assets/$n.$p.cirilica; done; done
    @Test
    public void translationCyrillicIsUpToDate() throws Exception {
        String assetsDir = System.getProperty("user.dir") + "/src/gonzales/assets/";
        Scanner numbers = new Scanner(new FileInputStream(assetsDir + "numbers"));
        int checkedCount = 0;
        List<String> translations = List.of(".bukvalno", ".finalno");

        String number;
        String numberTranslation;
        while (numbers.hasNextLine()) {
            number = numbers.nextLine();
            for (String translation : translations) {
                numberTranslation = number + translation;
                if (fileExists(assetsDir + numberTranslation + AssetLoader.CYRILLIC_SUFFIX)) {
                    Assert.assertTrue(number, fileIsOlder(assetsDir + numberTranslation, assetsDir + numberTranslation + AssetLoader.CYRILLIC_SUFFIX));
                    checkedCount++;
                }
            }
        }
        numbers.close();
        Assert.assertTrue(checkedCount >= 62);
    }

    private static boolean fileExists(String path) {
        return new File(path).exists();
    }

    private static boolean firstLineMatches(String path1, String path2) throws FileNotFoundException {
        File file1 = new File(path1);
        File file2 = new File(path2);
        Scanner scanner1 = new Scanner(new FileInputStream(file1));
        Scanner scanner2 = new Scanner(new FileInputStream(file2));

        boolean matches = scanner1.hasNextLine() && scanner2.hasNextLine() && scanner1.nextLine().equals(scanner2.nextLine());

        scanner1.close();
        scanner2.close();

        return matches;
    }

    private static boolean fileIsOlder(String path1, String path2) {
        File file1 = new File(path1);
        File file2 = new File(path2);

        return file1.exists() && file2.exists() && file1.lastModified() < file2.lastModified();
    }
}
