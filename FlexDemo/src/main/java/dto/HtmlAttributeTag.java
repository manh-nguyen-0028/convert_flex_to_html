package dto;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
public class HtmlAttributeTag {
    private String id;
    private String text;
    private HashMap<String, String> styles = new HashMap<>();
    private HashMap<String, String> attributes = new HashMap<>();
}
