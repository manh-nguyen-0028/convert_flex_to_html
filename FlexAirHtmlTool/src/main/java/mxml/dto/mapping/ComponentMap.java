package mxml.dto.mapping;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComponentMap {
    private String mxmlTag;
    private String xhtmlTag;
    private String xhtmlTagStart;
    private String xhtmlEndStartTag;
    private String xhtmlTagEnd;
    private Boolean isGenerateXHTML;
    private boolean isUseAjaxTag;

    public ComponentMap() {
    }

    public ComponentMap(String mxmlTag, String xhtmlTag, String xhtmlTagStart, String xhtmlEndStartTag, String xhtmlTagEnd) {
        this.mxmlTag = mxmlTag;
        this.xhtmlTag = xhtmlTag;
        this.xhtmlTagStart = xhtmlTagStart;
        this.xhtmlEndStartTag = xhtmlEndStartTag;
        this.xhtmlTagEnd = xhtmlTagEnd;
        this.isGenerateXHTML = true;
    }
}