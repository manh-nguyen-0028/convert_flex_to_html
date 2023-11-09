package mxml.dto.mapping;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyMap {
    private String name;
    private String convertTo;
    private String endTag;
    private String type;
    private boolean valueCompareTrue;
    private boolean isGenerateHtml;
}
