package mxml.enums;

public class MXMLPattern {
    private MXMLPattern() {
    }

    public static final String TOOL_TIP = "<h:outputText(?:\\s+(id=\"(\\w+)\")?|\\s+(value=\"(#[^\"]+)\")?|\\s+(showDataTips=\"(true|false)\")?|\\s+(style=\"([^\"]*)\")?)*></h:outputText>(.*?<\\/p:column>)";
}
