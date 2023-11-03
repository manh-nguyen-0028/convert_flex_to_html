package mxml.dto.parser;

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
    private String xhtmlTag;
    private String id;
    private String startTag;
    private String endStartTag;
    private String text;
    private String endTag;
    private List<PropertyParser> propertyParsers = new ArrayList<>();
    private List<PropertyParser> propertyForChild = new ArrayList<>();
    private Map<String, PropertyParser> mapPropertyParser = new HashMap<>();
    private List<CssParser> cssParsers = new ArrayList<>();
    private boolean isHadAttribute;
    private boolean isHadCss;
    private boolean isGenerateHtml;
    private boolean isUseAjaxTag;
    List<HtmlElementParser> childList = new ArrayList<>();

    public HtmlElementParser(String nodeName, String xhtmlTag, String startTag, String endStartTag, String endTag, boolean isGenerateHtml, boolean isUseAjaxTag) {
        this.nodeName = nodeName;
        this.xhtmlTag = xhtmlTag;
        this.startTag = startTag;
        this.endStartTag = endStartTag;
        this.endTag = endTag;
        this.isGenerateHtml = isGenerateHtml;
        this.isUseAjaxTag = isUseAjaxTag;
    }

    public Map<String, PropertyParser> getMapPropertyParser() {
        List<PropertyParser> propertyParserList = getPropertyParsers();
        if (CollectionUtils.isNotEmpty(propertyParserList)) {
            this.mapPropertyParser = propertyParserList.stream().collect(Collectors.toMap(PropertyParser::getKey, attribute -> attribute));
        }
        return mapPropertyParser;
    }

    public List<PropertyParser> getPropertyJsParser() {
        List<PropertyParser> propertyParserList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(this.propertyParsers)) {
            propertyParserList = this.propertyParsers.stream().filter(itemFilter -> "js".equals(itemFilter.getType())).collect(Collectors.toList());
        }
        return propertyParserList;
    }
}
