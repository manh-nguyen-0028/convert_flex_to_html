package as.parser;

import as.types.ASMember;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ASClass {
    private String packageName;
    private String className;
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
        if (value != null) {
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
