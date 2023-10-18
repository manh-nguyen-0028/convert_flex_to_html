package constants;

public class Templates {
    public static final String CLASS_TEMPLATE = "public class {className} {parentType} {parentName}";
    public static final String VARIABLE_STATIC = "private static {type} {name} = {value}";
    public static final String VARIABLE = "private {type} {name} = {value}";
    public static final String VARIABLE_PARAM = "{type} {name}";
    public static final String FUNCTION = "{encap} {type} {name}({params})";
    public static final String FUNCTION_STATIC = "{encap} static {type} {name}({params})";
    public static final String FUNCTION_CTOR = "public {name}({params})";
}
