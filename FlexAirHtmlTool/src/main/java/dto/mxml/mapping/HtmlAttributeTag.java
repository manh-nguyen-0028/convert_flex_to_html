package dto.mxml.mapping;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
public class HtmlAttributeTag {
    private String id;
    private String className;
    private String text;
    private boolean ignoreElement;
    private HashMap<String, String> styles = new HashMap<>();
    private HashMap<String, String> attributes = new HashMap<>();
}
