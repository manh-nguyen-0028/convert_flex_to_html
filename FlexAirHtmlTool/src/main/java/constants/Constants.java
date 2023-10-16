package constants;

public class Constants {
    public static String TYPE_MODIFY_ADD = "add";
    public static String ATTRIBUTE_CLASS = "class";
    public static final String EMPTY_STRING = "";
    public static final String ELLIPSIS_POINTS_STRING = "...";
    public static final String COMMA_STRING = ",";
    public static final char COLON_CHAR = ':';
    public static final String PACKAGE = "package";
    public static final char CURLY_BRACE = '{';
    //Match with string: 'var variable_name:types
    public static final String VARIABLE_PATTERN = "(var|,)(.*?)([a-zA-Z_$][0-9a-zA-Z_$]*):([a-zA-Z_$][0-9a-zA-Z_$]*)";
}
