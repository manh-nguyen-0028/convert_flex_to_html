package utils;

import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();
    private static Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
    private static final String YYYYMMDD_FORMAT = "yyyy/MM/dd";

    public static String generateRandomText(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(randomIndex);
            sb.append(randomChar);
        }
        return sb.toString();
    }

    //    public static String[] matches(String pattern, String value) {
//        return Pattern.compile(pattern).matcher(value).results().map(MatchResult::group).toArray(String[]::new);
//    }

    /**
     * Matching text by pattern
     */
    public static boolean isMatched(String pattern, String value) {
        return !isNullOrEmpty(value) && Pattern.compile(pattern).matcher(value).find();
    }

    /**
     * Get all group matching by patter
     */
    public static String[] matches(String regexPattern, String input) {
        Pattern pattern = Pattern.compile(regexPattern);
        List<String> list = new ArrayList<>();
        Matcher m = pattern.matcher(input);
        while (m.find()) {
            list.add(m.group());
        }
        if (list.size() == 0) {
            return new String[]{};
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * Verify number of input string
     */
    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }

    /**
     * Check string is null or empty
     */
    public static boolean isNullOrEmpty(String value) {
        return value == null || value.length() == 0;
    }

    public static String getDefaultValueString(String value, String fallback) {
        return value == null ? fallback : value;
    }
    public static String getDateYYYYMMDD() {
        DateFormat df = new SimpleDateFormat(YYYYMMDD_FORMAT);
        return df.format(new Date());
    }

    public static void replaceInStringBuilder(StringBuilder builder, String target, String replacement) {
        int index = builder.indexOf(target);
        while (index != -1) {
            builder.replace(index, index + target.length(), replacement);
            index = builder.indexOf(target, index + replacement.length());
        }
    }

    /**
     * Capitalize the first character of each word in a string
     * Ex:  "corpCd" -> "CorpCd"
     */
    public static String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }
}
