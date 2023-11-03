package constants;

public class Constants {
    public static String TYPE_MODIFY_ADD = "add";
    public static String ATTRIBUTE_CLASS = "class";
    public static String ATTRIBUTE_STYLE = "style";
    public static final String ELLIPSIS_POINTS_STRING = "...";
    public static final String COMMA_STRING = ",";
    public static final String EQUAL_OPERATOR = "=";
    public static final String BINDABLE_TEXT = "[Bindable]";
    public static final char COLON_CHAR = ':';
    public static final char DOT_CHAR = '.';
    public static final char CURLY_BRACE = '{';
    public static final char SEMICOLON_CHAR = ';';
    public static final char BINDABLE_CHAR_OPEN = '[';
    public static final char BINDABLE_CHAR_CLOSE = ']';
    //Match with string: 'var variable_name:types
    public static final String VARIABLE_PATTERN = "(var|,)(.*?)([a-zA-Z_$][0-9a-zA-Z_$]*):([a-zA-Z_$][0-9a-zA-Z_$]*)";
    public static final String MODEL_VARIABLE_PATTERN = ".*\\s*as\\s*.*";
    public static final String FUNC_VARIABLE_PATTERN = "((var|const)((\\s*[a-zA-Z_$*][0-9a-zA-Z_$.<>]*)\\s*)(:)(\\s*([a-zA-Z_$*][0-9a-zA-Z_$.<>]*)))";
    public static final String CATCH_PATTERN = "((catch\\s*\\()(\\w*)(:)(\\w*)(\\).*))";
    public static final String TRACE_INFO_PATTERN = "((trace)(\\(.*)(\\)))";
    public static final String TRACE_ERROR_PATTERN = "((trace|ACCLog.errorLog)(\\((\\\"Error|\\\"stack|\\\"<< 例外発生|\\\"\\[Exception|error))(.*)(\\)))";
    public static final String ACC_LOG_ERROR_PATTERN = "((ACCLog.errorLog)(\\())";
    public static final String SERVICE_URI_PATTERN = ".?\\/acc_web\\/services\\/.*";
    public static final String SET_CLEAR_FORM_PATTERN = "clearFrame|setFrame|clearForm|setForm";
    public static final String NARROW_FUNCTION_PATTERN = "((function)(\\(\\))(:void))";
    public static final String INIT_FOR_PATTERN = "((for\\s*\\()(reTryCnt\\d*?)(;))";
    public static final String PARSE_INT_PATTERN = "((int)(\\()(.*)(\\);))";
    public static final String PARSE_FNC_PATTERN = "((parseInt)(\\())";
    public static final String TEXT_PROP_PATTERN = "((.text)(\\s*)(=))";
    public static final String TEXT_PROP_END_PATTERN = "((.text))";
    public static final String STATEMENT_END_PATTERN = "[;\\r\\n]";
    public static final String NEW_DATETIME_PATTERN = "((new\\s+)(DateFormatter)(\\(\\)))";
    public static final String CLASS_NAME_PATTERN = "(MG\\d{7}_\\d{2}_\\d{3})";
    public static final String BOOLEAN_TYPE_PATTERN = "(([\\t\\s])(Boolean)(\\s?))";
    public static final String FOR_INIT_PATTERN = "((for\\()(byte|short|int|long)(\\s+)([a-zA-Z_$<>.*][0-9a-zA-Z_$<>.]*)(;))";

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
    public static final String MXML_CONTROLS_ACC_CHECK_BOX = "Controls:ACCCheckBox";
    public static final String MXML_MX_DATA_GRID_COLUMN = "mx:DataGridColumn";
    public static final String MXML_MX_HEADER_RENDERER = "mx:headerRenderer";
    public static final String MXML_MX_COMPONENT = "mx:Component";
    public static final String MXML_CONTROLS_ACC_RADIO_BUTTON = "Controls:ACCRadioButton";
    public static final String MXML_CONTROLS_ACC_RADIO_BUTTON_GROUP = "Controls:ACCRadioButtonGroup";
    public static final String MXML_CONTAINERS_ACC_TAB_NAVIGATOR = "Containers:ACCTabNavigator";
    public static final String MXML_ROOT_NODE = "RootNode";

    // xhtml tag
    public static final String XHTML_P_TAB = "p:tab";
    public static final String XHTML_H_OUTPUT_TEXT = "h:outputText";

    // java type
    public static final String CLASS_CONTROLLER = "Controller";
}
