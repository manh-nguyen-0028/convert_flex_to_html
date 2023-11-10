package mxml.dto.mapping;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyMap {
    private String mxmlProperties;
    private String xhtmlConvertTo;
    private String xhtmlEndTag;
    private String xhtmlType;
    private boolean xhtmlValueCompareTrue;
    private boolean isGenerateHtml;
}
