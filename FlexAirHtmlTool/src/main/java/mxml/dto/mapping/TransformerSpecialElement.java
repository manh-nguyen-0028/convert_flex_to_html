package mxml.dto.mapping;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TransformerSpecialElement {
    private String elementFrom;
    private String attribute;
    private String value;
    private String elementTo;
    private String htmlTagStart;
    private String htmlTagStart2;
    private String htmlTagStart3;
    private String htmlTagEnd;
}
