package constants;

public class Constants {
    public static String TYPE_MODIFY_ADD = "add";
    public static String ATTRIBUTE_CLASS = "class";
    public static String ATTRIBUTE_STYLE = "style";
    public static final String EMPTY_STRING = "";
    public static final String ELLIPSIS_POINTS_STRING = "...";
    public static final String COMMA_STRING = ",";
    public static final String DOCUMENT_VAR = "document";
    public static final String DOCUMENT_VIEW = "document";
    public static final String EQUAL_OPERATOR = "=";
    public static final char COLON_CHAR = ':';
    public static final char CURLY_BRACE = '{';
    //Match with string: 'var variable_name:types
    public static final String VARIABLE_PATTERN = "(var|,)(.*?)([a-zA-Z_$][0-9a-zA-Z_$]*):([a-zA-Z_$][0-9a-zA-Z_$]*)";
    public static final String FUNC_VARIABLE_PATTERN = "((var|const)((\\s*[a-zA-Z_$*][0-9a-zA-Z_$.<>]*)\\s*)(:)(\\s*([a-zA-Z_$*][0-9a-zA-Z_$.<>]*)))";
    public static final String CATCH_PATTERN = "((catch\\()(\\w*)(:)(\\w*)(\\).*))";
    public static final String TRACE_INFO_PATTERN = "((trace)(\\(\\\"[^(Error|\\[Exception)])(.*)(\\)))";
    public static final String TRACE_ERROR_PATTERN = "((trace|ACCLog.errorLog)(\\(\\\"[(Error|\\[Exception)])(.*)(\\)))";
    public static final String TEXT_PP_PATTERN = "((.text)(\\s*)(=))";
    public static final String SCRIPT_TAG = "mx:Script";
    public static final String XML_TAG = "mx:XML";
    public static final String MXML_EXT = ".mxml";
    public static final String AS_EXT = ".as";
    public static final String XHTML_EXT = ".xhtml";
    public static final String CSS_EXT = ".css";
    public static final String JS_EXT = ".js";
    public static final String JAVA_EXT = ".java";

    // syntax
    public static final String SYNTAX_COLON = ":";
    public static final String SYNTAX_SEMICOLON = ";";
    public static final String SYNTAX_EQUAL = "=";
    public static final String SYNTAX_SPACE = " ";
    public static final String SYNTAX_DOUBLE_QUOTATION = "\"";
    public static final String SYNTAX_DOT = ".";

    // mxml tag
    public static final String MXML_CONTAINERS_ACC_CANVAS = "Containers:ACCCanvas";
    public static final String MXML_CONTAINERS_ACC_TITLE_WINDOW = "Containers:ACCTitleWindow";

    // java type
    public static final String CLASS_CONTROLLER = "Controller";
}
