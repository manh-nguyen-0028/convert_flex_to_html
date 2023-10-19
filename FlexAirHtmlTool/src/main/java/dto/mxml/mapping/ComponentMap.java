package dto.mxml.mapping;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComponentMap {
    private String name;
    private Boolean isHadAttribute;
    private String className;
    private String htmlTagStart;
    private String htmlTagStart2;
    private String htmlTagStart3;
    private String htmlTagEnd;
    private Boolean styleInFile;
    private Boolean isGenerateHtml;
}