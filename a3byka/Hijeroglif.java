package a3byka;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

// /Applications/Android\ Studio.app/Contents/jbr/Contents/Home/bin/javac a3byka/Hijeroglif.java
public class Hijeroglif {
    private static Pattern youtubeReferences = Pattern.compile("^orig: [a-zA-Z0-9_\\-]{11},.*");
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String line;
        String prevod;

        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            if (line.startsWith("https") || line.startsWith("artistic") || youtubeReferences.matcher(line).matches()) {
                prevod = line;
            } else {
                prevod = naCirilicu(line);
                prevod = paSeVratimDaVratim(prevod);
            }
            System.out.println(prevod);
        }
    }

    private static Map<String, String> cirilica;
    private static Set<String> dvoslovni;
    private static Map<Pattern, String> strancizmi;

    static {
        cirilica = new HashMap<>();
        cirilica.put("a", "а");
        cirilica.put("b", "б");
        cirilica.put("v", "в");
        cirilica.put("g", "г");
        cirilica.put("d", "д");
        cirilica.put("đ", "ђ");
        cirilica.put("e", "е");
        cirilica.put("ž", "ж");
        cirilica.put("z", "з");
        cirilica.put("i", "и");
        cirilica.put("j", "ј");
        cirilica.put("k", "к");
        cirilica.put("l", "л");
        cirilica.put("lj", "љ");
        cirilica.put("m", "м");
        cirilica.put("n", "н");
        cirilica.put("nj", "њ");
        cirilica.put("o", "о");
        cirilica.put("p", "п");
        cirilica.put("r", "р");
        cirilica.put("s", "с");
        cirilica.put("t", "т");
        cirilica.put("ć", "ћ");
        cirilica.put("u", "у");
        cirilica.put("f", "ф");
        cirilica.put("h", "х");
        cirilica.put("c", "ц");
        cirilica.put("č", "ч");
        cirilica.put("dž", "џ");
        cirilica.put("š", "ш");
        cirilica.put("A", "А");
        cirilica.put("B", "Б");
        cirilica.put("V", "В");
        cirilica.put("G", "Г");
        cirilica.put("D", "Д");
        cirilica.put("Đ", "Ђ");
        cirilica.put("E", "Е");
        cirilica.put("Ž", "Ж");
        cirilica.put("Z", "З");
        cirilica.put("I", "И");
        cirilica.put("J", "Ј");
        cirilica.put("K", "К");
        cirilica.put("L", "Л");
        cirilica.put("Lj", "Љ");
        cirilica.put("M", "М");
        cirilica.put("N", "Н");
        cirilica.put("Nj", "Њ");
        cirilica.put("O", "О");
        cirilica.put("P", "П");
        cirilica.put("R", "Р");
        cirilica.put("S", "С");
        cirilica.put("T", "Т");
        cirilica.put("Ć", "Ћ");
        cirilica.put("U", "У");
        cirilica.put("F", "Ф");
        cirilica.put("H", "Х");
        cirilica.put("C", "Ц");
        cirilica.put("Č", "Ч");
        cirilica.put("Dž", "Џ");
        cirilica.put("Š", "Ш");
        // Ostala slova
        cirilica.put("Y", "J"); // Yoda, YouTube
        cirilica.put("y", "у"); // user friendly
        cirilica.put("x", "*"); // x8
        cirilica.put("W", "В"); // WC
        // Naglasci
        cirilica.put("Ȁ", "А");
        cirilica.put("Â", "А");
        cirilica.put("À", "А");
        cirilica.put("Á", "А");
        cirilica.put("Ȅ", "Е");
        cirilica.put("Ê", "Е");
        cirilica.put("È", "Е");
        cirilica.put("É", "Е");
        cirilica.put("Ȉ", "И");
        cirilica.put("Î", "И");
        cirilica.put("Ì", "И");
        cirilica.put("Í", "И");
        cirilica.put("Ȍ", "О");
        cirilica.put("Ô", "О");
        cirilica.put("Ò", "О");
        cirilica.put("Ó", "О");
        cirilica.put("Ȕ", "У");
        cirilica.put("Û", "У");
        cirilica.put("Ù", "У");
        cirilica.put("Ú", "У");
        cirilica.put("ȁ", "а");
        cirilica.put("â", "а");
        cirilica.put("à", "а");
        cirilica.put("á", "а");
        cirilica.put("ȅ", "е");
        cirilica.put("ê", "е");
        cirilica.put("è", "е");
        cirilica.put("é", "е");
        cirilica.put("ȉ", "и");
        cirilica.put("î", "и");
        cirilica.put("ì", "и");
        cirilica.put("í", "и");
        cirilica.put("ȍ", "о");
        cirilica.put("ô", "о");
        cirilica.put("ò", "о");
        cirilica.put("ó", "о");
        cirilica.put("ȕ", "у");
        cirilica.put("û", "у");
        cirilica.put("ù", "у");
        cirilica.put("ú", "у");

        dvoslovni = new HashSet<>();
        for (Map.Entry<String, String> entries : cirilica.entrySet()) {
            if (entries.getKey().length() == 2) {
                dvoslovni.add(entries.getKey());
            }
        }

        strancizmi = new HashMap<>();
        strancizmi.put(Pattern.compile("усер фриендлу"), "user friendly");
        strancizmi.put(Pattern.compile("ЈоуТубе"), "YouTube");
        strancizmi.put(Pattern.compile("цонвениенце"), "convenience");
        strancizmi.put(Pattern.compile("онлине"), "online");
        strancizmi.put(Pattern.compile("маинстреам"), "mainstream");
    }

    private static String naCirilicu(String line) {
        StringBuilder builder = new StringBuilder();
        while (!line.isEmpty()) {
            int uzmi = 1;
            for (String dvoslovan : dvoslovni) {
                if (line.startsWith(dvoslovan)) {
                    uzmi = 2;
                    break;
                }
            }

            String prvoSlovo = line.substring(0, uzmi);
            String ostatak = line.substring(uzmi);
            String prevod = cirilica.get(prvoSlovo);
            if (prevod == null) {
                // TODO: dodati da dopušta nepoznata slova za ono što mi treba Yoda, WC, x8 i slično
                if (Character.isLetter(prvoSlovo.codePointAt(0))) {
                    System.err.println("Nepoznato slovo: " + prvoSlovo);
                    System.exit(-1);
                } else {
                    prevod = prvoSlovo;
                }
            } else if (prvoSlovo.equals("nj") && ostatak.startsWith("ug")) { // Tanjug
                prevod = "нј";
            }
            builder.append(prevod);
            line = ostatak;
        }
        return builder.toString();
    }

    private static String paSeVratimDaVratim(String prevod) {
        for (Map.Entry<Pattern, String> entry : strancizmi.entrySet()) {
            prevod = entry.getKey().matcher(prevod).replaceAll(entry.getValue());
        }
        return prevod;
    }
}
