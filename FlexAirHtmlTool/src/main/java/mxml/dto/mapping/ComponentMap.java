package mxml.dto.mapping;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComponentMap {
    private String name;
    private String htmlTagStart;
    private String htmlEndStartTag;
    private String htmlTagEnd;
    private Boolean isGenerateHtml;

    public ComponentMap() {
    }

    public ComponentMap(String name, String htmlTagStart, String htmlEndStartTag, String htmlTagEnd) {
        this.name = name;
        this.htmlTagStart = htmlTagStart;
        this.htmlEndStartTag = htmlEndStartTag;
        this.htmlTagEnd = htmlTagEnd;
        this.isGenerateHtml = true;
    }
}