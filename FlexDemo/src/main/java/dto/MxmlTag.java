package dto;

public class MxmlTag {
    private String name;
    private Boolean isHadAttribute;
    private String className;
    private String htmlTagStart;
    private String htmlTagStart2;
    private String htmlTagStart3;
    private String htmlTagEnd;
    private Boolean styleInFile;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHtmlTagStart() {
        return htmlTagStart;
    }

    public void setHtmlTagStart(String htmlTagStart) {
        this.htmlTagStart = htmlTagStart;
    }

    public String getHtmlTagEnd() {
        return htmlTagEnd;
    }

    public void setHtmlTagEnd(String htmlTagEnd) {
        this.htmlTagEnd = htmlTagEnd;
    }

    public String getHtmlTagStart2() {
        return htmlTagStart2;
    }

    public void setHtmlTagStart2(String htmlTagStart2) {
        this.htmlTagStart2 = htmlTagStart2;
    }

    public String getHtmlTagStart3() {
        return htmlTagStart3;
    }

    public void setHtmlTagStart3(String htmlTagStart3) {
        this.htmlTagStart3 = htmlTagStart3;
    }

    public Boolean getHadAttribute() {
        return isHadAttribute;
    }

    public void setHadAttribute(Boolean hadAttribute) {
        isHadAttribute = hadAttribute;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Boolean getStyleInFile() {
        return styleInFile;
    }

    public void setStyleInFile(Boolean styleInFile) {
        this.styleInFile = styleInFile;
    }
}