package mxml.dto.parser;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PropertyParser {
    private String key;
    private String value;
    private String type;
    private boolean isHadValue;
    private boolean isGenerateHtml;
    private boolean isUse = true;

    public PropertyParser(String key, String value, String type) {
        this.key = key;
        this.value = value;
        this.type = type;
    }
}
