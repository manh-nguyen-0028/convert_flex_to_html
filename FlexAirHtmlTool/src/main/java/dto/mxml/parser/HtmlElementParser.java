package dto.mxml.parser;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Setter
@Getter
public class HtmlElementParser {
    private String parentNodeName;
    private String nodeName;
    private String id;
    private String startTag;
    private String endStartTag;
    private String text;
    private String endTag;
    private List<AttributeParser> attributeParsers = new ArrayList<>();
    private Map<String, AttributeParser> mapAttributeParser = new HashMap();
    private List<CssParser> cssParsers = new ArrayList<>();
    private boolean isHadAttribute;
    private boolean isHadCss;
    private boolean isGenerateHtml;
    List<HtmlElementParser> childList = new ArrayList<>();

    public HtmlElementParser(String nodeName, String startTag, String endStartTag, String endTag, boolean isGenerateHtml) {
        this.nodeName = nodeName;
        this.startTag = startTag;
        this.endStartTag = endStartTag;
        this.endTag = endTag;
        this.isGenerateHtml = isGenerateHtml;
    }

    public Map<String, AttributeParser> getMapAttributeParser() {
        if (CollectionUtils.isNotEmpty(this.attributeParsers)) {
            this.mapAttributeParser = this.attributeParsers.stream().collect(Collectors.toMap(AttributeParser::getKey, attribute -> attribute));
        }
        return mapAttributeParser;
    }
}
