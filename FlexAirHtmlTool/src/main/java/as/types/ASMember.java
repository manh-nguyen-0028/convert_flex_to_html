package as.types;

/**
 * Wrapper class for member
 */
public class ASMember {
    private String name;
    private String type;
    private String subType;
    private String value;
    private String encapsulation;
    private boolean isStatic;
    private String comment;
    private boolean isConst;
    public ASMember() {
        name = null;
        type = "*";
        subType = null;
        value = null;
        encapsulation = "public";
        isStatic = false;
    }

    public ASVariable createVariable() {
        ASVariable obj = new ASVariable();
        obj.setName(name);
        obj.setType(type);
        obj.setSubType(subType);
        obj.setValue(value);
        obj.setEncapsulation(encapsulation);
        obj.setStatic(isStatic);
        obj.setConst(isConst);
        obj.setComment(comment);
        return obj;
    }

    public ASFunction createFunction() {
        ASFunction obj = new ASFunction();
        obj.setName(name);
        obj.setType(type);
        obj.setSubType(subType);
        obj.setValue(value);
        obj.setEncapsulation(encapsulation);
        obj.setStatic(isStatic);
        obj.setComment(comment);
        return obj;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getEncapsulation() {
        return encapsulation;
    }

    public void setEncapsulation(String encapsulation) {
        this.encapsulation = encapsulation;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public void setStatic(boolean aStatic) {
        isStatic = aStatic;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isConst() {
        return isConst;
    }

    public void setConst(boolean aConst) {
        isConst = aConst;
    }
}
