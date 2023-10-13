package as.enums;

/**
 * Parsing pattern
 */
public class ASPattern {
    public static final String[] IDENTIFIER = { "\\w", "\\w" };
    public static final String[] OBJECT = { "[\\w\\.]", "[\\w(\\w(\\.\\w)+)]" };
    public static final String[] IMPORT = { "[0-9a-zA-Z_$.*]", "[a-zA-Z_$][0-9a-zA-Z_$]([.][a-zA-Z_$][0-9a-zA-Z_$])*\\*?" };
    public static final String[] REQUIRE = { ".", "['\"](.*?)['\"]" };
    public static final String[] CURLY_BRACE = { "[\\{|\\}]", "[\\{|\\}]" };
    public static final String[] VARIABLE = { "[0-9a-zA-Z_$]", "[a-zA-Z_$][0-9a-zA-Z_$]*" };
    public static final String[] VARIABLE_TYPE = { "[a-zA-Z_$<>.*][0-9a-zA-Z_$<>.]*", "[a-zA-Z_$<>.*][0-9a-zA-Z_$<>.]*" };
    public static final String[] VARIABLE_DECLARATION = { "[0-9a-zA-Z_$:<>.*]", "[a-zA-Z_$][0-9a-zA-Z_$]*\\s*:\\s*([a-zA-Z_$<>.\\*][0-9a-zA-Z_$<>.]*)"};
    public static final String[] ASSIGN_START = { "[=\\r\\n]", "[=\\r\\n]" };
    public static final String[] ASSIGN_UPTO = { "[^;\\r\\n]", "(.*?)" };
    public static final String[] VECTOR = { "new[\\s\\t]+Vector\\.<(.*?)>\\((.*?)\\)", "new[\\s\\t]+Vector\\.<(.*?)>\\((.*?)\\)" };
    public static final String[] ARRAY = { "new[\\s\\t]+Array\\((.*?)\\)", "new[\\s\\t]+Array\\((.*?)\\)" };
    public static final String[] DICTIONARY = { "new[\\s\\t]+Dictionary\\((.*?)\\)" };
    public static final String[] REST_ARG = { "\\.{3}[a-zA-Z_$][0-9a-zA-Z_$]*", "\\.{3}[a-zA-Z_$][0-9a-zA-Z_$]*" };
}
