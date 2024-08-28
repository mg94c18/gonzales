import java.util.regex.Pattern;
import java.util.Scanner;

public class WordPatternTest {
    // From SearchProvider
    private static Pattern splitPattern = Pattern.compile("[\\[\\] .,!?\\|¡¿:;\"\\(\\)'\\-_\\{\\}]");
    private static Pattern htmlTags = Pattern.compile("(<[^>]+>)|(\\{[^\\{\\}]+\\})");

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

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String line;
        String[] words;
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            line = htmlTags.matcher(line).replaceAll("");
            words = splitPattern.split(line);
            for (String word : words) {
                if (nonAlphaExists(word)) {
                    System.err.println("Non-alphanumeric word found: " + word);
                }
            }
        }
    }
}
