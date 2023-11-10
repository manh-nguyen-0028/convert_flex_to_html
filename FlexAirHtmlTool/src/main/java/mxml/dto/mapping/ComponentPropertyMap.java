package mxml.dto.mapping;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComponentPropertyMap {
    private String mxmlComponentName;
    private String mxmlPropertyFrom;
    private String xhtmlPropertyTo;
}
