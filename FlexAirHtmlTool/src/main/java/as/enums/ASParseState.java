package as.enums;

/**
 * Parsing state
 */
public class ASParseState {
    public static final String START = "start";
    public static final String PACKAGE_NAME = "packageName";
    public static final String PACKAGE = "package";
    public static final String CLASS_NAME = "className";
    public static final String CLASS = "class";
    public static final String CLASS_EXTENDS = "classExtends";
    public static final String CLASS_IMPLEMENTS = "classImplements";
    public static final String COMMENT_INLINE = "commentInline";
    public static final String COMMENT_MULTILINE = "commentMultiline";
    public static final String STRING_SINGLE_QUOTE = "stringSingleQuote";
    public static final String STRING_DOUBLE_QUOTE = "stringDoubleQuote";
    public static final String STRING_REGEX = "stringRegex";
    public static final String MEMBER_VARIABLE = "memberVariable";
    public static final String MEMBER_FUNCTION = "memberFunction";
    public static final String LOCAL_VARIABLE = "localVariable";
    public static final String LOCAL_FUNCTION = "localFunction";
    public static final String IMPORT_PACKAGE = "importPackage";
    public static final String REQUIRE_MODULE = "requireModule";
}
