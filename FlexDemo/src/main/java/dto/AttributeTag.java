package dto;

public class AttributeTag {
    private String name;
    private String startTag;
    private String endTag;
    private String type;
    private boolean valueCompareTrue;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartTag() {
        return startTag;
    }

    public void setStartTag(String startTag) {
        this.startTag = startTag;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEndTag() {
        return endTag;
    }

    public void setEndTag(String endTag) {
        this.endTag = endTag;
    }

    public boolean isValueCompareTrue() {
        return valueCompareTrue;
    }

    public void setValueCompareTrue(boolean valueCompareTrue) {
        this.valueCompareTrue = valueCompareTrue;
    }
}
