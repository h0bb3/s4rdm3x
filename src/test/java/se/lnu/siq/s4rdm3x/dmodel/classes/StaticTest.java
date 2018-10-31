package se.lnu.siq.s4rdm3x.dmodel.classes;

import java.util.HashMap;
import java.util.Map;

// this are parts from jabref 3.7 HTMLUnicodeConversionMaps
public class StaticTest {

    private static final int[] ints = new int[] {17}; // 11 byte code instructions according to JArchitect
    private static final String[] strings = new String[] {"17"}; // 10 byte code instructions according to JArchitect
    // if both are enable it is however 17

    private static final String[][] doubleStrings = new String[][] {{"17"}, {"19"}};

    static String getString(String a_arg) {
        return doubleStrings[0][0];
    }
    // if all three are enabled both are enable it is however 35

    /*private static final String[][] CONVERSION_LIST = new String[][]{{"160", "nbsp", "{~}"}, // no-break space = non-breaking space,
            //                                 U+00A0 ISOnum
            {"161", "iexcl", "{\\textexclamdown}"}, // inverted exclamation mark, U+00A1 ISOnum
            {"162", "cent", "{\\textcent}"}, // cent sign, U+00A2 ISOnum
            {"163", "pound", "{\\pounds}"}, // pound sign, U+00A3 ISOnum
            {"164", "curren", "{\\textcurrency}"}, // currency sign, U+00A4 ISOnum
    };*/

   /* private static final String[][] ACCENT_LIST = new String[][]{{"768", "`"}, // Grave
            {"769", "'"}, // Acute
            {"770", "^"}, // Circumflex
            {"771", "~"}, // Tilde
            {"772", "="}, // Macron
    };

    public static final Map<String, String> HTML_LATEX_CONVERSION_MAP = new HashMap<>();
    public static final Map<Integer, String> ESCAPED_ACCENTS = new HashMap<>();
    public static final Map<String, String> UNICODE_ESCAPED_ACCENTS = new HashMap<>();
    public static final Map<Integer, String> NUMERICAL_LATEX_CONVERSION_MAP = new HashMap<>();
    public static final Map<String, String> UNICODE_LATEX_CONVERSION_MAP = new HashMap<>();
    public static final Map<String, String> LATEX_HTML_CONVERSION_MAP = new HashMap<>();
    public static final Map<String, String> LATEX_UNICODE_CONVERSION_MAP = new HashMap<>();


    static {
        for (String[] aConversionList : CONVERSION_LIST) {
            if (!(aConversionList[2].isEmpty())) {
                String strippedLaTeX = cleanLaTeX(aConversionList[2]);
                if (!(aConversionList[1].isEmpty())) {
                    HTML_LATEX_CONVERSION_MAP.put("&" + aConversionList[1] + ";", aConversionList[2]);
                    if (!strippedLaTeX.isEmpty()) {
                        LATEX_HTML_CONVERSION_MAP.put(strippedLaTeX, "&" + aConversionList[1] + ";");
                    }
                } else if (!(aConversionList[0].isEmpty()) && !strippedLaTeX.isEmpty()) {
                    LATEX_HTML_CONVERSION_MAP.put(strippedLaTeX, "&#" + aConversionList[0] + ";");
                }
                if (!(aConversionList[0].isEmpty())) {
                    NUMERICAL_LATEX_CONVERSION_MAP.put(Integer.decode(aConversionList[0]), aConversionList[2]);
                    if (Integer.decode(aConversionList[0]) > 128) {
                        String unicodeSymbol = String.valueOf(Character.toChars(Integer.decode(aConversionList[0])));
                        UNICODE_LATEX_CONVERSION_MAP.put(unicodeSymbol, aConversionList[2]);
                        if (!strippedLaTeX.isEmpty()) {
                            LATEX_UNICODE_CONVERSION_MAP.put(strippedLaTeX, unicodeSymbol);
                        }
                    }
                }
            }
        }
        for (String[] anAccentList : ACCENT_LIST) {
            ESCAPED_ACCENTS.put(Integer.decode(anAccentList[0]), anAccentList[1]);
            UNICODE_ESCAPED_ACCENTS.put(anAccentList[1],
                    String.valueOf(Character.toChars(Integer.decode(anAccentList[0]))));
        }
        // Manually added values which are killed by cleanLaTeX
        LATEX_HTML_CONVERSION_MAP.put("$", "&dollar;");
        LATEX_UNICODE_CONVERSION_MAP.put("$", "$");

        // Manual corrections
        LATEX_HTML_CONVERSION_MAP.put("AA", "&Aring;"); // Overwritten by &angst; which is less supported
        LATEX_UNICODE_CONVERSION_MAP.put("AA", "Å"); // Overwritten by Ångstrom symbol

        // Manual additions
        // Support relax to the extent that it is simply removed
        LATEX_HTML_CONVERSION_MAP.put("relax", "");
        LATEX_UNICODE_CONVERSION_MAP.put("relax", "");

    }*/

   /* private static String cleanLaTeX(String escapedString) {
        // Get rid of \{}$ from the LaTeX-string
        return escapedString.replaceAll("[\\\\\\{\\}\\$]", "");
    }*/
}
