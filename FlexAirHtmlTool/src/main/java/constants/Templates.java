package constants;

public class Templates {
    public static final String CLASS_TEMPLATE = "public class {className} {parentType} {parentName}";
    public static final String VARIABLE_STATIC = "private static final {type} {name} = {value}";
    public static final String VARIABLE_ASSIGN = "private {type} {name} = {value}";
    public static final String VARIABLE = "private {type} {name}";
    public static final String VARIABLE_GET_SET = "\tprotected String {name};";
    public static final String VARIABLE_PARAM = "{type} {name}";
    public static final String FUNCTION = "{encap} {type} {name}({params})";
    public static final String FUNCTION_STATIC = "{encap} static {type} {name}({params})";
    public static final String FUNCTION_CTOR = "public {name}({params})";
    public static final String FUNCTION_GET = "\n\t/**"
            + "\n\t * @return {pp}"
            + "\n\t */"
            + "\n\tpublic String get{Pp}() {"
            + "\n\t\treturn {pp};"
            + "\n\t}";
    public static final String FUNCTION_SET = "\n\t/**"
            + "\n\t * @param {pp} セットする {pp}"
            + "\n\t */"
            + "\n\tpublic void set{Pp}(String {pp}) {"
            + "\n\t\tthis.{pp} = {pp};"
            + "\n\t}";
}
