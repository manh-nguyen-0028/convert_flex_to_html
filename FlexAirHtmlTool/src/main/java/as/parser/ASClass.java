package as.parser;

import as.enums.ASKeyword;
import as.enums.ASPattern;
import as.types.ASArgument;
import as.types.ASFunction;
import as.types.ASMember;
import as.types.ASVariable;
import constants.Constants;
import constants.ReservedWords;
import constants.Templates;
import utils.StringUtils;

import java.util.*;

public class ASClass {
    private String packageName;
    private String className;
    private String orgClassName;
    private List<String> imports;
    private List<String> requires;
    private List<String> importWildcards;
    private List<String> importExtras;
    private List<String> interfaces;
    private String parent;
    private ASClass parentDefinition;
    private List<ASMember> members;
    private List<ASMember> staticMembers;
    private List<ASMember> getters;
    private List<ASMember> setters;
    private List<ASMember> staticGetters;
    private List<ASMember> staticSetters;
    private List<ASMember> importsPkg;
    private boolean isInterface;
    private List<ASMember> membersWithAssignments;
    private Map<String, ASMember> fieldMap;
    private Map<String, ASMember> staticFieldMap;
    private Map<String, ASClass> classMap;
    private Map<String, ASClass> classMapFiltered;
    private Map<String, ASClass> packageMap;

    // Options
    private boolean safeRequire;
    private boolean ignoreFlash;
    public ASClass(ParserOptions options) {
        options = options != null ? options : new ParserOptions();
        safeRequire = false;

        if (options.isSafeRequire()) {
            safeRequire = options.isSafeRequire();
        }
        if (options.isIgnoreFlash()) {
            ignoreFlash = options.isIgnoreFlash();
        }

        packageName = null;
        className = null;
        imports = new ArrayList<>();
        requires = new ArrayList<>();
        importWildcards = new ArrayList<>();
        importExtras = new ArrayList<>();
        interfaces = new ArrayList<>();
        parent = null;
        parentDefinition = null;
        members = new ArrayList<>();
        staticMembers = new ArrayList<>();
        getters = new ArrayList<>();
        setters = new ArrayList<>();
        staticGetters = new ArrayList<>();
        staticSetters = new ArrayList<>();
        membersWithAssignments = new ArrayList<>();
        isInterface = false;
        fieldMap = new HashMap<>();
        staticFieldMap = new HashMap<>();
        classMap = new HashMap<>();
        classMapFiltered = new HashMap<>();
        packageMap = new HashMap<>();
        importsPkg = new ArrayList<>();
    }

    /**
     * Register field
     */
    public void registerField(String name, ASMember value) {
        // Return if register field is null
        if (value == null) {
            return;
        }
        // Static field
        if (value.isStatic()) {
            staticFieldMap.put(name, staticFieldMap.getOrDefault(name, value));
        } else {
            // Normal field
            fieldMap.put(name, fieldMap.getOrDefault(name, value));
        }
    }
    public String generateModelString(Map<String, List<String>> xmlObjectInline) {
        StringBuilder buffer = new StringBuilder();
        //Process require package
        // Deal with static member assignments
        if (this.members.size() > 0) {
            //Place defaults first
            for (ASMember m : this.members) {
                if (m instanceof ASFunction) {
                    //Ignore setForm/clearForm
                    if (StringUtils.isMatched(Constants.SET_CLEAR_FORM_PATTERN, m.getName())) continue;
                    buffer.append(stringifyFunc(m, true));
                } else {
                    ASVariable currentVar = (ASVariable) m;
                    String type = ASKeyword.STRING;
                    String value = m.getValue();
                    //Ignore service uri string
                    if (StringUtils.isMatched(Constants.SERVICE_URI_PATTERN, value)) {
                        continue;
                    }
                    if (ASKeyword.NUMBER.equals(currentVar.getType())
                            || ASKeyword.INT.equals(currentVar.getType())
                            || ASKeyword.UINT.equals(currentVar.getType())) {
                        type = ReservedWords.INT;
                        if (!StringUtils.isNumeric(currentVar.getValue())) {
                            value = "0";
                        }
                    } else if (ASKeyword.BOOLEAN.equals(currentVar.getType())) {
                        type = ReservedWords.BOOLEAN;
                    } else if (ASKeyword.STRING.equals(currentVar.getType())) {
                        type = ReservedWords.STRING;
                    } else if (ASKeyword.DATEFORMATTER.equals(currentVar.getType())) {
                        type = ReservedWords.SIMPLEDATEFORMAT;
                    }
//                    else if (getClassName().equals(currentVar.getType())) {
//                        type = getClassName() + ReservedWords.MODEL;
//                    }
                    else {
                        type = currentVar.getType();
                    }
                    // Remove call service variable

                    buffer.append(currentVar.getComment());
                    String tmpVar = "";
                    if (currentVar.isStatic()) {
                        tmpVar = Templates.VARIABLE_STATIC;
                    } else if (m.getValue() == null){
                        tmpVar = Templates.VARIABLE;
                        tmpVar = tmpVar.replace("{type}", type)
                                .replace("{name}", m.getName());
                    } else {
                        tmpVar = Templates.VARIABLE_ASSIGN;
                        tmpVar = tmpVar.replace("{type}", type)
                                .replace("{name}", m.getName())
                                .replace("{value}", value);
                    }
                    buffer.append(tmpVar);
                    buffer.append(";");
                }
                buffer.append("\n");
            }
            if (xmlObjectInline != null && xmlObjectInline.size() > 0) {
                for (String key : xmlObjectInline.keySet()) {
                    buffer.append("\n");
                    List<String> properties = xmlObjectInline.get(key);
                    StringBuilder propBuilder = new StringBuilder();
                    StringBuilder propGetSetBuilder = new StringBuilder();
                    String getSetStr = "";
                    for (String pp : properties) {
                        propBuilder.append(Templates.VARIABLE_GET_SET.replace("{name}", pp));
                        propBuilder.append("\n");
                        //getterとsetterの生成
                        getSetStr = Templates.FUNCTION_GET.replace("{pp}", pp).replace("{Pp}", StringUtils.capitalize(pp));
                        propGetSetBuilder.append(getSetStr);
                        getSetStr = Templates.FUNCTION_SET.replace("{pp}", pp).replace("{Pp}", StringUtils.capitalize(pp));
                        propGetSetBuilder.append(getSetStr);
                    }
                    buffer.append(propBuilder);
                    buffer.append("\n");
                    buffer.append(propGetSetBuilder);
                }
            }
            buffer.append("\n");
        }
        String result = buffer.toString();
        result = result.replaceAll("^;", "");
        // Remove duplicate new line
        result = result.replaceAll("\n\n", "\n");
        // Replace 4 spaces to tab
        result = result.replaceAll("    ", "\t");
        //Remove 2 tabs identify
        result = result.replaceAll("\n\t\t\t", "\n\t");
        return result;
    }
    public String generateString() {
        StringBuilder buffer = new StringBuilder();
        //Process require package
        // Import

        String[] tmpArr = null;
        // Add Class source
        String strClass = Templates.CLASS_TEMPLATE.replace("{className}", getClassName() + ReservedWords.CONTROLLER)
                .replace("{parentType}", ReservedWords.EXTENDS)
                .replace("{parentName}", getParent());
        buffer.append(strClass);
        buffer.append("\n{");
        // Deal with static member assignments
        if (this.members.size() > 0) {
            //Place defaults first
            for (ASMember m : this.members) {
                if (m instanceof ASFunction) {
                    buffer.append(stringifyFunc(m, false));
                } else {
                    ASVariable currentVar = (ASVariable)m;
                    String type = ASKeyword.STRING;
                    String value = currentVar.getValue();
                    if (ASKeyword.NUMBER.equals(currentVar.getType())
                            || ASKeyword.INT.equals(currentVar.getType())
                            || ASKeyword.UINT.equals(currentVar.getType())) {
                        type = ReservedWords.INT;
                        if (!StringUtils.isNumeric(currentVar.getValue())) {
                            value = "0";
                        }
                    } else if (ASKeyword.BOOLEAN.equals(currentVar.getType())) {
                        type = ReservedWords.BOOLEAN;
                    } else if (ASKeyword.STRING.equals(currentVar.getType())) {
                        type = ReservedWords.STRING;
                    }
                    else if (ASKeyword.DATEFORMATTER.equals(currentVar.getType())) {
                        type = ReservedWords.SIMPLEDATEFORMAT;
                    }
//                    else if (getClassName().equals(currentVar.getType())) {
//                        type = getClassName() + ReservedWords.MODEL;
//                    }
                    else {
                        type = currentVar.getType();
                    }
                    buffer.append(currentVar.getComment());
                    String tmpVar = "";
                   if (currentVar.isStatic()) {
                       tmpVar = Templates.VARIABLE_STATIC;
                       tmpVar = tmpVar.replace("{type}", type)
                               .replace("{name}", m.getName())
                               .replace("{value}", value);
                   } else if (m.getValue() == null){
                       tmpVar = Templates.VARIABLE;
                       tmpVar = tmpVar.replace("{type}", type)
                               .replace("{name}", m.getName());
                   } else {
                       tmpVar = Templates.VARIABLE_ASSIGN;
                       tmpVar = tmpVar.replace("{type}", type)
                               .replace("{name}", m.getName())
                               .replace("{value}", value);
                   }
                    buffer.append(tmpVar);
                }
            }
            buffer.append("\n");
        }
        buffer.append("\n}");
        String result = buffer.toString();
        result = result.replaceAll("\n\t\t", "\n\t");
        return result;
    }
    public String stringifyFunc(ASMember fn, boolean isModel) {
        StringBuilder buffer = new StringBuilder();
        boolean isConstructor = false;
        if (fn instanceof ASFunction) {
            ASFunction function = (ASFunction)fn;
            String fncStr = function.getComment();
            if (function.isStatic()) {
                // Static functions
                fncStr += Templates.FUNCTION_STATIC;
            } else if (getOrgClassName().equals(function.getName())) {
                // Constructor
                fncStr += Templates.FUNCTION_CTOR;
                isConstructor = true;
            }
            else {
                // Normal functions
                fncStr += Templates.FUNCTION;
            }
            // Encapsulation
            fncStr = fncStr.replace("{encap}", function.getEncapsulation());
            // Type
            fncStr = fncStr.replace("{type}", function.getType());
            // Name
            if (isConstructor) {
                fncStr = fncStr.replace("{name}", getClassName() + (isModel? ReservedWords.MODEL : ReservedWords.CONTROLLER) );
            } else  {
                fncStr = fncStr.replace("{name}", function.getName());
            }
            // Params
            List<String> tmpArr = new ArrayList<>();
            for (ASVariable arg : ((ASFunction)fn).getArgList()) {
                if (!((ASArgument)arg).isRestParam()) {
                    String paramVar = "";
                    paramVar = Templates.VARIABLE_PARAM.replace("{type}", arg.getType())
                                                        .replace("{name}", arg.getName());
                    tmpArr.add(paramVar);
                }
            }
            fncStr = fncStr.replace("{params}", String.join(", ", tmpArr));
            if (isModel) {
                fncStr = fncStr.replace("\n\t\t\t", "\n\t");
            }
            buffer.append(fncStr);
            //buffer.append(cleanup(fn.getValue())).append("\n");
            buffer.append(fn.getValue()).append("\n");
        }
        return buffer.toString();
    }
    public String cleanup(String text) {
        String type;
        String[] params;
        String val;
        String[] matches = StringUtils.matches(ASPattern.VECTOR[0],text);
        //For each Vector.<>() found in the text
        for (int i = 0; i < matches.length; i++) {
            //Strip the type and provided params
            type = matches[i].replace(ASPattern.VECTOR[0], "$1").trim();
            params = matches[i].replace(ASPattern.VECTOR[0], "$2").split(",");
            //Set the default based on var type
            if (type.equals("int") || type.equals("uint") || type.equals("Number")) {
                val = "0";
            } else if (type.equals("Boolean")) {
                val = "false";
            } else {
                val = "null";
            }
            //Replace accordingly
            if (params.length > 0 && params[0].trim() != "") {
                text = text.replace(ASPattern.VECTOR[1], "Utils.createArray(" + params[0] + ", " + val + ")");
            } else {
                text = text.replace(ASPattern.VECTOR[1], "[]");
            }
        }
        matches = StringUtils.matches(ASPattern.ARRAY[0], text);
        //For each Array() found in the text
        for (int i = 0; i < matches.length; i++) {
            //Strip the provided params
            params = null;
            params = new String[]{matches[i].replace(ASPattern.ARRAY[0], "$1").trim()};
            //Replace accordingly
            if (params.length > 0 && params[0].trim() != "") {
                text = text.replace(ASPattern.ARRAY[1], "Utils.createArray(" + params[0] + ", null)");
            } else {
                text = text.replace(ASPattern.ARRAY[1], "[]");
            }
        }

        matches = StringUtils.matches(ASPattern.DICTIONARY[0], text);
        //For each instantiated Dictionary found in the text
        for (int i = 0; i < matches.length; i++) {
            // Replace with empty object
            text = text.replace(ASPattern.DICTIONARY[0], "{}");
        }

        return text;
    }
    public ASMember retrieveField(String name, boolean isStatic) {
        if (isStatic) {
            if (staticFieldMap.containsKey(name)) {
                return staticFieldMap.get(name);
            } else if (parentDefinition != null) {
                return parentDefinition.retrieveField(name, isStatic);
            } else {
                return null;
            }
        } else {
            if (fieldMap.containsKey(name)) {
                return fieldMap.get(name);
            } else if (parentDefinition != null) {
                return parentDefinition.retrieveField(name, isStatic);
            } else {
                return null;
            }
        }
    }

    public void process() {
        ASClass self = this;
        int i;

        for (i = 0; i < members.size(); i++) {
            if (members.get(i) instanceof ASFunction) {
                //Pars function
                ASFunction func = (ASFunction)members.get(i);
                func.setValue(ASParser.parseFunc(self, func.getValue(), func.buildLocalVariableStack(), func.isStatic())[0]);
            }
            if (members.get(i) instanceof ASVariable) {
                ASVariable tmpVar = (ASVariable) members.get(i);
                if (tmpVar.getValue() != null && retrieveField(tmpVar.getValue().replaceFirst("^([a-zA-Z_$][0-9a-zA-Z_$]*)(.*?)$", "$1"), true) != null) {
                    tmpVar.setValue(className + '.' + members.get(i).getValue());
                }
            }
        }
    }
    //----------------------------------------------
    // getterとsetter生成
    //----------------------------------------------


    public List<ASMember> getImportsPkg() {
        return importsPkg;
    }

    public void setImportsPkg(List<ASMember> importsPkg) {
        this.importsPkg = importsPkg;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getOrgClassName() {
        return orgClassName;
    }

    public void setOrgClassName(String orgClassName) {
        this.orgClassName = orgClassName;
    }

    public List<String> getImports() {
        return imports;
    }

    public void setImports(List<String> imports) {
        this.imports = imports;
    }

    public List<String> getRequires() {
        return requires;
    }

    public void setRequires(List<String> requires) {
        this.requires = requires;
    }

    public List<String> getImportWildcards() {
        return importWildcards;
    }

    public void setImportWildcards(List<String> importWildcards) {
        this.importWildcards = importWildcards;
    }

    public List<String> getImportExtras() {
        return importExtras;
    }

    public void setImportExtras(List<String> importExtras) {
        this.importExtras = importExtras;
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<String> interfaces) {
        this.interfaces = interfaces;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public ASClass getParentDefinition() {
        return parentDefinition;
    }

    public void setParentDefinition(ASClass parentDefinition) {
        this.parentDefinition = parentDefinition;
    }

    public List<ASMember> getMembers() {
        return members;
    }

    public void setMembers(List<ASMember> members) {
        this.members = members;
    }

    public List<ASMember> getStaticMembers() {
        return staticMembers;
    }

    public void setStaticMembers(List<ASMember> staticMembers) {
        this.staticMembers = staticMembers;
    }

    public List<ASMember> getGetters() {
        return getters;
    }

    public void setGetters(List<ASMember> getters) {
        this.getters = getters;
    }

    public List<ASMember> getSetters() {
        return setters;
    }

    public void setSetters(List<ASMember> setters) {
        this.setters = setters;
    }

    public List<ASMember> getStaticGetters() {
        return staticGetters;
    }

    public void setStaticGetters(List<ASMember> staticGetters) {
        this.staticGetters = staticGetters;
    }

    public List<ASMember> getStaticSetters() {
        return staticSetters;
    }

    public void setStaticSetters(List<ASMember> staticSetters) {
        this.staticSetters = staticSetters;
    }

    public boolean isInterface() {
        return isInterface;
    }

    public void setInterface(boolean anInterface) {
        isInterface = anInterface;
    }

    public List<ASMember> getMembersWithAssignments() {
        return membersWithAssignments;
    }

    public void setMembersWithAssignments(List<ASMember> membersWithAssignments) {
        this.membersWithAssignments = membersWithAssignments;
    }

    public Map<String, ASMember> getFieldMap() {
        return fieldMap;
    }

    public void setFieldMap(Map<String, ASMember> fieldMap) {
        this.fieldMap = fieldMap;
    }

    public Map<String, ASMember> getStaticFieldMap() {
        return staticFieldMap;
    }

    public void setStaticFieldMap(Map<String, ASMember> staticFieldMap) {
        this.staticFieldMap = staticFieldMap;
    }

    public Map<String, ASClass> getClassMap() {
        return classMap;
    }

    public void setClassMap(Map<String, ASClass> classMap) {
        this.classMap = classMap;
    }

    public Map<String, ASClass> getClassMapFiltered() {
        return classMapFiltered;
    }

    public void setClassMapFiltered(Map<String, ASClass> classMapFiltered) {
        this.classMapFiltered = classMapFiltered;
    }

    public Map<String, ASClass> getPackageMap() {
        return packageMap;
    }

    public void setPackageMap(Map<String, ASClass> packageMap) {
        this.packageMap = packageMap;
    }

    public boolean isSafeRequire() {
        return safeRequire;
    }

    public void setSafeRequire(boolean safeRequire) {
        this.safeRequire = safeRequire;
    }

    public boolean isIgnoreFlash() {
        return ignoreFlash;
    }

    public void setIgnoreFlash(boolean ignoreFlash) {
        this.ignoreFlash = ignoreFlash;
    }

}
