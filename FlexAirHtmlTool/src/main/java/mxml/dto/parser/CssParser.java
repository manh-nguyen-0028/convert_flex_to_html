package mxml.dto.parser;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CssParser {
    private String key;
    private String value;
    private boolean isHadValue;

    public CssParser(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
