package dto.mxml.parser;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ElementNodeParser {
    private String parentNodeName;
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
    private List<ElementNodeParser> nodeParserList = new ArrayList<>();
}
