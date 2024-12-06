package org.mg94c18.gonzales;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class HostUnitTest {
    @Test
    public void translationIsUpToDate() throws Exception {
        String assetsDir = System.getProperty("user.dir") + "/src/dijaspora/assets/";
        Scanner numbers = new Scanner(new FileInputStream(assetsDir + "numbers"));

        // Skip, and also verify it's not empty
        Assert.assertTrue(numbers.hasNextLine());
        Assert.assertEquals("abvgd", numbers.nextLine());

        String number;
        while (numbers.hasNextLine()) {
            number = numbers.nextLine();
            Assert.assertTrue(number, fileIsOlder(assetsDir + number, assetsDir + number + ".cirilica"));
        }

        List<String> otherAssets = List.of("titles", "dates");
        for (String a : otherAssets) {
            Assert.assertTrue(a, fileIsOlder(assetsDir + a, assetsDir + a + ".cirilica"));
        }
        numbers.close();
    }

    private static boolean fileIsOlder(String path1, String path2) {
        File file1 = new File(path1);
        File file2 = new File(path2);

        return file1.exists() && file2.exists() && file1.lastModified() < file2.lastModified();
    }
}
