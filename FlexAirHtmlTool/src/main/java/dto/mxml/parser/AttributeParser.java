package dto.mxml.parser;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AttributeParser {
    private String key;
    private String value;
    private boolean isHadValue;

    public AttributeParser(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
