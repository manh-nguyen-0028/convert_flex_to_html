package dto.mxml.mapping;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MappingElementModify {
    private String parentXmlName;
    private String typeModify;
    private String attributeAdd;
    private String cssBefore;
    private String cssAfter;
    private String htmlStart;
    private String htmlInElement;
    private String htmlEnd;
}
