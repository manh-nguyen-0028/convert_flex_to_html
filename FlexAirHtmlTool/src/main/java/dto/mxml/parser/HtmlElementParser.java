package dto.mxml.parser;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class HtmlElementParser {
    private String startTag;
    private String endStartTag;
    private String text;
    private String endTag;
    private List<AttributeParser> attributeParsers;
    private List<CssParser> cssParsers;
    private boolean isHadAttribute;
    private boolean isHadCss;
    private boolean isIgnoreElement;
}
