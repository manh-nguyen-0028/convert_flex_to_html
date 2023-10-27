package mxml.service;

import constants.Constants;
import mxml.dto.parser.CssParser;

import java.util.List;

public class CssService {
    /**
     * @param cssParsers
     * @return create syntax css
     */
    public static StringBuilder createCssElement(List<CssParser> cssParsers) {
        StringBuilder cssBuilder = new StringBuilder();
        // css
        for (CssParser css : cssParsers) {
            cssBuilder.append(css.getKey()).append(Constants.SYNTAX_COLON).append(css.getValue()).append(Constants.SYNTAX_SEMICOLON);
        }
        return cssBuilder;
    }

    /**
     * @param cssParsers
     * @return create syntax css inline html
     */
    public static StringBuilder createSyntaxCssInline(List<CssParser> cssParsers) {
        StringBuilder cssElement = createCssElement(cssParsers);
        return createSyntaxCssInline(cssElement);
    }

    /**
     * @param cssElement
     * @return create syntax css inline html
     */
    public static StringBuilder createSyntaxCssInline(StringBuilder cssElement) {
        StringBuilder cssElementInline = new StringBuilder(Constants.SYNTAX_SPACE + Constants.ATTRIBUTE_STYLE + Constants.SYNTAX_EQUAL + Constants.SYNTAX_DOUBLE_QUOTATION);
        cssElementInline.append(cssElement).append(Constants.SYNTAX_DOUBLE_QUOTATION);
        return cssElementInline;
    }
}
