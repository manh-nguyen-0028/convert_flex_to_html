package mxml.dto.mapping;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TransformerSpecialElement {
    private String mxmlElementFrom;
    private String mxmlAttribute;
    private String xhtmlValue;
    private String xhtmlElementTo;
    private String htmlTagStart;
    private String htmlTagStart2;
    private String htmlTagStart3;
    private String htmlTagEnd;
    private boolean isUseAjaxTag;
}
