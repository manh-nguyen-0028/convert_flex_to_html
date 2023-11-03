package as.parser;

import as.enums.ASKeyword;
import as.types.ASArgument;
import as.types.ASFunction;
import as.types.ASMember;
import as.types.ASVariable;
import constants.Constants;
import constants.ReservedWords;
import constants.Templates;
import utils.CommonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ASClass {
    private String packageName;
    private String className;
    private String orgClassName;
    private List<String> imports;
    private String parent;
    private ASClass parentDefinition;
    private List<ASMember> members;
    private List<ASMember> importsPkg;
    private boolean isInterface;
    private List<ASMember> membersWithAssignments;
    private Map<String, ASMember> fieldMap;
    private Map<String, ASMember> staticFieldMap;
    private Map<String, ASClass> classMap;
    private Map<String, ASClass> classMapFiltered;
    private Map<String, ASClass> packageMap;

    public ASClass() {
        packageName = null;
        className = null;
        imports = new ArrayList<>();
        parent = null;
        parentDefinition = null;
        members = new ArrayList<>();
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
        List<ASVariable> lstProperties = new ArrayList<>();
        //Process require package
        // Deal with static member assignments
        if (this.members.size() > 0) {
            //Place defaults first
            for (ASMember m : this.members) {
                if (m instanceof ASFunction) {
                    //Ignore setForm/clearForm
                    //if (StringUtils.isMatched(Constants.SET_CLEAR_FORM_PATTERN, m.getName())) continue;
                    // buffer.append(stringifyFunc(m, true));
                } else {
                    ASVariable currentVar = (ASVariable) m;
                    String type;
                    String value = m.getValue();
                    //Ignore service uri string
                    if (CommonUtils.isMatched(Constants.SERVICE_URI_PATTERN, value)) {
                        continue;
                    }
                    type = CommonUtils.convertType(currentVar.getType());
                    if (ReservedWords.INT.equals(type)) {
                        if (!CommonUtils.isNumeric(currentVar.getValue())) {
                            value = "0";
                        }
                    }
                    // Remove call service variable

                    buffer.append(currentVar.getComment().replaceAll("^;", ""));
                    String tmpVar = "";
                    if (currentVar.isStatic()) {
                        tmpVar = Templates.VARIABLE_STATIC;
                        tmpVar = tmpVar.replace("{type}", type)
                                .replace("{name}", m.getName())
                                .replace("{value}", value);
                    } else {
                        if (m.getValue() == null) {
                            tmpVar = Templates.VARIABLE;
                            tmpVar = tmpVar.replace("{type}", type)
                                    .replace("{name}", m.getName());
                        } else {
                            tmpVar = Templates.VARIABLE_ASSIGN;
                            tmpVar = tmpVar.replace("{type}", type)
                                    .replace("{name}", m.getName())
                                    .replace("{value}", value);
                        }
                    }
                    tmpVar = tmpVar.replace("{const}", m.isConst() ? " final" : "");
                    tmpVar = tmpVar.replace("{encap}", m.getEncapsulation());

                    buffer.append(tmpVar);
                    buffer.append(";");
                    //Add getter/setter for bindable property
                    if (currentVar.isBindable()) {
                        lstProperties.add(currentVar);
                    }
                }
                buffer.append("\n");
            }

            // getterとsetterの生成
            StringBuilder propBuilder = new StringBuilder();
            StringBuilder propGetSetBuilder = new StringBuilder();
            StringBuilder[] getSetPPs;
            //XML object
            if (xmlObjectInline != null && xmlObjectInline.size() > 0) {
                for (String key : xmlObjectInline.keySet()) {
                    List<String> properties = xmlObjectInline.get(key);
                    for (String pp : properties) {
                        getSetPPs = generateProperty(pp, ReservedWords.STRING);
                        if (getSetPPs != null && getSetPPs.length > 0) {
                            propBuilder.append(getSetPPs[0]);
                            propGetSetBuilder.append(getSetPPs[1]);
                        }
                    }
                }
            }
            //[Bindable] properties
            for (ASVariable var : lstProperties) {
                getSetPPs = generateProperty(var.getName(), CommonUtils.convertType(var.getType()));
                if (getSetPPs != null && getSetPPs.length > 0) {
                    //propBuilder.append(getSetPPs[0]);
                    propGetSetBuilder.append(getSetPPs[1]);
                }
            }
            if (propBuilder != null && propBuilder.length() > 0) {
                buffer.append(propBuilder);
                buffer.append("\n");
            }
            if (propGetSetBuilder != null && propGetSetBuilder.length() > 0) {
                buffer.append(propGetSetBuilder);
            }
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
                    ASVariable currentVar = (ASVariable) m;
                    String type;
                    String value = currentVar.getValue();
                    type = CommonUtils.convertType(currentVar.getType());
                    if (ReservedWords.INT.equals(type)) {
                        if (!CommonUtils.isNumeric(currentVar.getValue())) {
                            value = "0";
                        }
                    } else if (ReservedWords.SIMPLEDATEFORMAT.equals(type)) {
                        if (!CommonUtils.isNullOrEmpty(value)) {
                            value = value.replaceAll(Constants.NEW_DATETIME_PATTERN, "$2" + ReservedWords.SIMPLEDATEFORMAT + "$4");
                        }
                    }
                    buffer.append(currentVar.getComment());
                    String tmpVar = "";
                    if (currentVar.isStatic()) {
                        tmpVar = Templates.VARIABLE_STATIC;
                        tmpVar = tmpVar.replace("{type}", type)
                                .replace("{name}", m.getName())
                                .replace("{value}", value);
                    } else if (m.getValue() == null) {
                        tmpVar = Templates.VARIABLE;
                        tmpVar = tmpVar.replace("{type}", type)
                                .replace("{name}", m.getName());
                    } else {
                        tmpVar = Templates.VARIABLE_ASSIGN;
                        tmpVar = tmpVar.replace("{type}", type)
                                .replace("{name}", m.getName())
                                .replace("{value}", value);
                    }
                    tmpVar = tmpVar.replace("{const}", m.isConst() ? " final" : "");
                    tmpVar = tmpVar.replace("{encap}", m.getEncapsulation());
                    buffer.append(tmpVar);
                }
            }
        }
        buffer.append("\n}");
        String result = buffer.toString();
        result = result.replaceAll("\n\t\t", "\n\t");
        return result;
    }

    public String stringifyFunc(ASMember fn, boolean isModel) {
        StringBuilder buffer = new StringBuilder();
        if (fn instanceof ASFunction) {
            ASFunction function = (ASFunction) fn;
            String fncStr = function.getComment();
            String fncName = function.getName();
            if (function.isStatic()) {
                // Static functions
                fncStr += Templates.FUNCTION_STATIC;
            } else if (getOrgClassName().equals(function.getName())) {
                // Constructor
                fncStr += Templates.FUNCTION_CTOR;
                fncName = getClassName() + (isModel ? ReservedWords.MODEL : ReservedWords.CONTROLLER);
            } else if (ASKeyword.SET.equals(function.getSubType())) {
                fncName = ASKeyword.SET + CommonUtils.capitalize(fncName);
                // Set functions
                fncStr += Templates.FUNCTION;
            } else if (ASKeyword.GET.equals(function.getSubType())) {
                fncName = ASKeyword.GET + CommonUtils.capitalize(fncName);
                // Set functions
                fncStr += Templates.FUNCTION;
            } else {
                // Normal functions
                fncStr += Templates.FUNCTION;
            }
            // Encapsulation
            fncStr = fncStr.replace("{encap}", function.getEncapsulation());
            // Type
            fncStr = fncStr.replace("{type}", function.getType());
            // Name
            fncStr = fncStr.replace("{name}", fncName);
            // Params
            List<String> tmpArr = new ArrayList<>();
            for (ASVariable arg : ((ASFunction) fn).getArgList()) {
                if (!((ASArgument) arg).isRestParam()) {
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

    public void modelProcess() {
        ASClass self = this;
        ASParser parser = new ASParser();
        int i;

        for (i = 0; i < members.size(); i++) {
//            if (members.get(i) instanceof ASFunction) {
//                //Parse function
//                ASFunction func = (ASFunction)members.get(i);
//                func.setValue(parser.parseFunc(self, func.getValue(), func.buildLocalVariableStack())[0]);
//            }
            if (members.get(i) instanceof ASVariable) {
                ASVariable tmpVar = (ASVariable) members.get(i);
                if (tmpVar.getValue() != null
                        && retrieveField(tmpVar.getValue().replaceFirst("^([a-zA-Z_$][0-9a-zA-Z_$]*)(.*?)$", "$1"), true) != null) {
                    tmpVar.setValue(className + '.' + members.get(i).getValue());
                }
            }
        }
    }

    public void process() {
        ASClass self = this;
        ASParser parser = new ASParser();
        int i;

        for (i = 0; i < members.size(); i++) {
            if (members.get(i) instanceof ASFunction) {
                //Parse function
                ASFunction func = (ASFunction) members.get(i);
                func.setValue(parser.parseFunc(self, func.getValue(), func.buildLocalVariableStack())[0]);
            }
            if (members.get(i) instanceof ASVariable) {
                ASVariable tmpVar = (ASVariable) members.get(i);
                if (tmpVar.getValue() != null
                        && retrieveField(tmpVar.getValue().replaceFirst("^([a-zA-Z_$][0-9a-zA-Z_$]*)(.*?)$", "$1"), true) != null) {
                    tmpVar.setValue(className + '.' + members.get(i).getValue());
                }
            }
        }
    }

    private StringBuilder[] generateProperty(String ppName, String ppType) {
        StringBuilder propBuilder = new StringBuilder();
        StringBuilder propGetSetBuilder = new StringBuilder();
        String getSetStr;
        propBuilder.append(Templates.VARIABLE_GET_SET
                .replace("{type}", ppType)
                .replace("{name}", ppName));
        propBuilder.append("\n");

        if (ReservedWords.BOOLEAN.equals(ppType)) {
            getSetStr = Templates.FUNCTION_GET_BOOLEAN.replace("{type}", ppType)
                    .replace("{pp}", ppName).replace("{Pp}", CommonUtils.capitalize(ppName));
        } else {
            getSetStr = Templates.FUNCTION_GET.replace("{type}", ppType)
                    .replace("{pp}", ppName).replace("{Pp}", CommonUtils.capitalize(ppName));
        }

        propGetSetBuilder.append("\n");
        propGetSetBuilder.append(getSetStr);
        getSetStr = Templates.FUNCTION_SET.replace("{type}", ppType)
                .replace("{pp}", ppName).replace("{Pp}", CommonUtils.capitalize(ppName));
        propGetSetBuilder.append(getSetStr);
        propGetSetBuilder.append("\n");
        return new StringBuilder[]{propBuilder, propGetSetBuilder};
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

}
