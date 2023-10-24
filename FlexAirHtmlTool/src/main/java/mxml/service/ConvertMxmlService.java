package mxml.service;

import constants.Constants;
import mxml.dto.mapping.ComponentMap;
import mxml.dto.mapping.PropertyMap;
import mxml.dto.mapping.TransformerSpecialElement;
import mxml.dto.modify.CheckBoxReplace;
import mxml.dto.modify.ElementReplace;
import mxml.dto.modify.RadioGroupReplace;
import mxml.dto.parser.AttributeParser;
import mxml.dto.parser.CssParser;
import mxml.dto.parser.HtmlElementParser;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.Log;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ConvertMxmlService {
    private final Map<String, PropertyMap> hmAttributeMap;

    private final Map<String, ComponentMap> hmNodeMap;

    private final String xmlFileName;

    private final ElementReplace elementReplace;

    public ConvertMxmlService(Map<String, ComponentMap> hmNodeMap, Map<String, PropertyMap> hmAttributeMap, String xmlFileName, ElementReplace elementReplace) {
        this.hmNodeMap = hmNodeMap;
        this.hmAttributeMap = hmAttributeMap;
        this.xmlFileName = xmlFileName;
        this.elementReplace = elementReplace;
    }

    private void handleNodeAttributes(Node node, HtmlElementParser htmlElementParser) {
        if (node.hasAttributes()) {
            NamedNodeMap nodeAttMap = node.getAttributes();
            for (int i = 0; i < nodeAttMap.getLength(); i++) {
                Node nodeAtt = nodeAttMap.item(i);
                PropertyMap propertyMap = hmAttributeMap.get(nodeAtt.getNodeName());
                if (propertyMap != null) {
                    handleAttributeTag(htmlElementParser, propertyMap, nodeAtt);
                }
            }
        }
    }

    public void handleNodeXml(HtmlElementParser elementParser, boolean isFirstCanvas, NodeList nodeList, StringBuilder baseHtml, StringBuilder html) {
        for (int count = 0; count < nodeList.getLength(); count++) {
            Node nodeItem = nodeList.item(count);
            String nodeName = nodeItem.getNodeName();
            Log.log("Node Name =" + nodeName + " [OPEN]");
            if (nodeItem.getNodeType() == Node.ELEMENT_NODE) {
                ComponentMap componentMap = hmNodeMap.get(nodeName);
                if (componentMap != null) {
                    handleNodeMap(elementParser, componentMap, nodeItem, nodeName, baseHtml, isFirstCanvas, html);
                }
            }
        }
    }

    private void handleNodeMap(HtmlElementParser parentElement, ComponentMap componentMap, Node nodeItem, String currentNodeName, StringBuilder baseHtml, boolean isFirstCanvas, StringBuilder html) {
        String parentNodeName = parentElement.getNodeName();
        HtmlElementParser currentElement = new HtmlElementParser(currentNodeName, componentMap.getHtmlTagStart(), componentMap.getHtmlTagStart2(), componentMap.getHtmlTagEnd(), componentMap.getIsGenerateHtml());
        currentElement.setParentNodeName(parentElement.getNodeName());
        handleNodeAttributes(nodeItem, currentElement);
        transformerElementByAttribute(currentElement);
        handleFirstElement(currentElement, parentNodeName, currentNodeName);
        handleSpecialElement(currentElement);
        transformTagHaveAjax(currentElement);
        handleAttributeFromParent(currentElement, parentElement);
        if (nodeItem.hasChildNodes()) {
            // loop again if has child nodes
            addPropertyForChild(currentElement);
            handleNodeXml(currentElement, isFirstCanvas, nodeItem.getChildNodes(), baseHtml, html);
        }
        parentElement.getChildList().add(currentElement);

        Log.log("Node Name =" + currentNodeName + " [CLOSE]");
    }

    private void handleAttributeFromParent(HtmlElementParser currentElement, HtmlElementParser parentElement) {
        if (CollectionUtils.isNotEmpty(parentElement.getAttributeForChild())) {
            currentElement.getAttributeParsers().addAll(parentElement.getAttributeForChild());
        }
    }

    private void addPropertyForChild(HtmlElementParser elementParser) {
        Map<String, AttributeParser> hmAttribute = elementParser.getMapAttributeParser();
        if (Constants.MXML_MX_DATA_GRID_COLUMN.equals(elementParser.getNodeName())
                || Constants.MXML_MX_HEADER_RENDERER.equals(elementParser.getNodeName())
                || Constants.MXML_MX_COMPONENT.equals(elementParser.getNodeName())) {
            AttributeParser attributeForChild = hmAttribute.get("value");
            elementParser.getAttributeForChild().add(attributeForChild);
        }
    }

    private void handleSpecialElement(HtmlElementParser htmlElementParser) {
        StringBuilder attributeElement = createSyntaxAttributeHtml(htmlElementParser);
        StringBuilder cssElement = createCssElement(htmlElementParser.getCssParsers());
        StringBuilder elementParser = createHtmlElementParser(htmlElementParser, attributeElement, cssElement);
        elementParser.append(htmlElementParser.getEndTag());
        String nodeName = htmlElementParser.getNodeName();
        if (Constants.MXML_CONTROLS_ACC_RADIO_BUTTON.equals(nodeName)) {
            Optional<AttributeParser> groupNameOptional = htmlElementParser.getAttributeParsers().stream().filter(itemFilter -> "groupName".equals(itemFilter.getKey())).findFirst();
            if (groupNameOptional.isPresent()) {
                String groupId = groupNameOptional.get().getValue();
                Optional<RadioGroupReplace> radioGroupReplaceOptional = elementReplace.getRadioGroupReplaces()
                        .stream()
                        .filter(itemFilter -> groupId.equals(itemFilter.getGroupId())).findFirst();
                if (radioGroupReplaceOptional.isPresent()) {
                    RadioGroupReplace radioGroupReplace = radioGroupReplaceOptional.get();
                    List<String> selectItemList = radioGroupReplace.getSelectItemList();
                    selectItemList.add(elementParser.toString());
                    radioGroupReplace.setSelectItemList(selectItemList);
                } else {
                    RadioGroupReplace radioGroupReplace = new RadioGroupReplace(groupNameOptional.get().getValue());
                    radioGroupReplace.getSelectItemList().add(elementParser.toString());
                    elementReplace.getRadioGroupReplaces().add(radioGroupReplace);
                }
            }
        }
    }

    private static void transformerElementByAttribute(HtmlElementParser htmlElementParser) {
        List<TransformerSpecialElement> specialElementList = XmlService.getTransformerSpecialElement();
        List<AttributeParser> attributeList = htmlElementParser.getAttributeParsers();
        Optional<TransformerSpecialElement> specialElementOptional = specialElementList.stream().filter(itemFilter -> htmlElementParser.getNodeName().equals(itemFilter.getElementFrom())).findFirst();
        if (specialElementOptional.isPresent()) {
            Optional<AttributeParser> parserOptional = attributeList.stream().filter(itemFilter -> specialElementOptional.get().getAttribute().equals(itemFilter.getKey())).findFirst();
            if (parserOptional.isPresent()) {
                String value = specialElementOptional.get().getValue();
                if (StringUtils.isEmpty(value) || (StringUtils.isNotEmpty(value) && parserOptional.get().getValue().equals(value))) {
                    htmlElementParser.setStartTag(specialElementOptional.get().getHtmlTagStart());
                    htmlElementParser.setEndTag(specialElementOptional.get().getHtmlTagEnd());
                }
            }
        }
    }

    public void handleAttributeTag(HtmlElementParser htmlElementParser, PropertyMap propertyMap, Node nodeAtt) {
        String nodeValue = nodeAtt.getNodeValue();
        String attType = propertyMap.getType();

        switch (attType) {
            case "css":
                convertToCss(htmlElementParser, propertyMap, nodeValue);
                break;

            case "text":
                break;

            case "id":
                convertToAtt(htmlElementParser, propertyMap, nodeValue);
                htmlElementParser.setId(nodeValue);
                break;
            case "property":
                convertToAtt(htmlElementParser, propertyMap, nodeValue);
                break;

            case "js":
                convertToJs(htmlElementParser, propertyMap, nodeValue);
                break;

            default:
                // No action for other cases
        }
    }

    private HtmlElementParser convertToCss(HtmlElementParser elementParser, PropertyMap propertyMap, String nodeValue) {
        String startTag = propertyMap.getStartTag();
        if ("opacity".equals(startTag)) {
            if (Double.parseDouble(nodeValue) > 0) {
                elementParser.getCssParsers().add(new CssParser(propertyMap.getStartTag(), nodeValue));
            }
        } else {
            elementParser.getCssParsers().add(new CssParser(propertyMap.getStartTag(), nodeValue));
        }

        return elementParser;
    }

    private HtmlElementParser convertToAtt(HtmlElementParser elementParser, PropertyMap propertyMap, String nodeValue) {
        // handle when att = img
        String startTag = propertyMap.getStartTag();
        boolean compareTrueWithValue = propertyMap.isValueCompareTrue();
        AttributeParser attributeParser = new AttributeParser(startTag, nodeValue);
        if ("src".equals(startTag)) {
            nodeValue = nodeValue.replace("@Embed('", "").replace("')", "");
            CssParser cssParser = new CssParser("background-image", "url(#{resource['" + nodeValue + "']})");
            elementParser.getCssParsers().add(cssParser);
            attributeParser.setUse(false);
        }
        if (!compareTrueWithValue || (compareTrueWithValue && "true".equals(nodeValue))) {
            elementParser.getAttributeParsers().add(attributeParser);
        }
        return elementParser;
    }

    private HtmlElementParser convertToJs(HtmlElementParser elementParser, PropertyMap propertyMap, String nodeValue) {
        String startTag = propertyMap.getStartTag();
        if (nodeValue.contains("script.")) {
            nodeValue = nodeValue.replaceAll("script\\.(.*?)\\((.*?)\\)", "#{" + xmlFileName + Constants.CLASS_CONTROLLER + Constants.SYNTAX_DOT + "$1}");
        }
        elementParser.getAttributeParsers().add(new AttributeParser(startTag, nodeValue));
        return elementParser;
    }

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

    /**
     * @param elementParser
     * @return syntax attribute in html
     */
    public static StringBuilder createSyntaxAttributeHtml(HtmlElementParser elementParser) {
        StringBuilder attributeBuilder = new StringBuilder();
        // add attribute
        for (AttributeParser attribute : elementParser.getAttributeParsers()) {
            if (attribute.isUse()) {
                attributeBuilder
                        .append(Constants.SYNTAX_SPACE)
                        .append(attribute.getKey())
                        .append(Constants.SYNTAX_EQUAL)
                        .append(Constants.SYNTAX_DOUBLE_QUOTATION)
                        .append(attribute.getValue())
                        .append(Constants.SYNTAX_DOUBLE_QUOTATION);
            }
        }

        return attributeBuilder;
    }

    private StringBuilder createHtmlElementParser(HtmlElementParser elementParser, StringBuilder attributeElement, StringBuilder cssElement) {
        StringBuilder elementBuilder = new StringBuilder(elementParser.getStartTag());
        elementBuilder.append(Constants.SYNTAX_SPACE).append(attributeElement);
        if (StringUtils.isNotEmpty(cssElement)) {
            elementBuilder.append(Constants.SYNTAX_SPACE).append(Constants.ATTRIBUTE_STYLE).append(Constants.SYNTAX_EQUAL).append(Constants.SYNTAX_DOUBLE_QUOTATION)
                    .append(cssElement).append(Constants.SYNTAX_DOUBLE_QUOTATION);
        }
        elementBuilder.append(elementParser.getEndStartTag());
        return elementBuilder;
    }

    /**
     * Handle first element of mxml file
     * Example:<Containers:ACCCanvas
     *
     * @param elementChild
     * @param parentNodeName
     * @param currentNodeName
     */
    private void handleFirstElement(HtmlElementParser elementChild, String parentNodeName, String currentNodeName) {
        if ("First Node".equals(parentNodeName) && (currentNodeName.equals(Constants.MXML_CONTAINERS_ACC_CANVAS) || currentNodeName.equals(Constants.MXML_CONTAINERS_ACC_TITLE_WINDOW))) {
            elementChild.setGenerateHtml(false);
            Optional<AttributeParser> parserOptional = elementChild.getAttributeParsers().stream().filter(item -> "title".equals(item.getKey())).findFirst();
            if (parserOptional.isPresent()) {
                this.elementReplace.setTitle(parserOptional.get().getValue());
            }
            this.elementReplace.setCssCompositionFirstList(elementChild.getCssParsers());
            elementChild.setStartTag("<ui:composition");
            elementChild.setEndTag("</ui:composition");
        }
    }

    /**
     * @param elementParser Handle tag jsf may be have ajax tag
     *                      Example: checkbox, radio,...
     */
    private void transformTagHaveAjax(HtmlElementParser elementParser) {
        AttributeParser attributeParser = elementParser.getMapAttributeParser().get("change");
        if (attributeParser != null) {
            if (Constants.MXML_CONTROLS_ACC_CHECK_BOX.equals(elementParser.getNodeName())) {
                CheckBoxReplace checkBoxReplace = new CheckBoxReplace();
                checkBoxReplace.setId(elementParser.getId());
                checkBoxReplace.setEvent(attributeParser.getKey());
                this.elementReplace.getCheckBoxReplaces().add(checkBoxReplace);
            }
        }
    }
}
