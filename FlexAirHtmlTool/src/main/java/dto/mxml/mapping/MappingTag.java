package dto.mxml.mapping;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MappingTag {
    private String name;
    private String startTag;
    private String endTag;
    private String type;
    private boolean valueCompareTrue;
}
