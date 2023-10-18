package dto.mxml.parser;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class HtmlElementParser {
    private String nodeName;
    private String id;
    private String startTag;
    private String endStartTag;
    private String text;
    private String endTag;
    private List<AttributeParser> attributeParsers = new ArrayList<>();
    private List<CssParser> cssParsers = new ArrayList<>();
    private boolean isHadAttribute;
    private boolean isHadCss;
    private boolean isGenerateHtml;

    public HtmlElementParser(String nodeName, String startTag, String endStartTag, String endTag, boolean isGenerateHtml) {
        this.nodeName = nodeName;
        this.startTag = startTag;
        this.endStartTag = endStartTag;
        this.endTag = endTag;
        this.isGenerateHtml = isGenerateHtml;
    }
}
