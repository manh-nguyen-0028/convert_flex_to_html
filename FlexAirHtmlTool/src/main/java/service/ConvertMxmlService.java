package service;

import constants.Constants;
import dto.mxml.mapping.ComponentMap;
import dto.mxml.mapping.PropertyMap;
import dto.mxml.mapping.TransformerSpecialElement;
import dto.mxml.modify.CheckBoxReplace;
import dto.mxml.modify.ElementReplace;
import dto.mxml.modify.RadioGroupReplace;
import dto.mxml.parser.AttributeParser;
import dto.mxml.parser.CssParser;
import dto.mxml.parser.HtmlElementParser;
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

    private void handleNodeMap(HtmlElementParser elementParser, ComponentMap componentMap, Node nodeItem, String currentNodeName, StringBuilder baseHtml, boolean isFirstCanvas, StringBuilder html) {
        String parentNodeName = elementParser.getNodeName();
        HtmlElementParser elementChild = new HtmlElementParser(currentNodeName, componentMap.getHtmlTagStart(), componentMap.getHtmlTagStart2(), componentMap.getHtmlTagEnd(), componentMap.getIsGenerateHtml());
        elementChild.setParentNodeName(elementParser.getNodeName());
        handleNodeAttributes(nodeItem, elementChild);
        transformerElementByAttribute(elementChild);
        handleFirstElement(elementChild, parentNodeName, currentNodeName);
        handleSpecialElement(elementChild);
        transformTagHaveAjax(elementChild);
        if (nodeItem.hasChildNodes()) {
            // loop again if has child nodes
            handleNodeXml(elementChild, isFirstCanvas, nodeItem.getChildNodes(), baseHtml, html);
        }
        elementParser.getChildList().add(elementChild);

        Log.log("Node Name =" + currentNodeName + " [CLOSE]");
    }

    private void handleSpecialElement(HtmlElementParser htmlElementParser) {
        StringBuilder attributeElement = createAttributeElement(htmlElementParser);
        StringBuilder cssElement = createCssElement(htmlElementParser.getCssParsers());
        StringBuilder elementParser = createHtmlElementParser(htmlElementParser, attributeElement, cssElement);
        elementParser.append(htmlElementParser.getEndTag());
        String nodeName = htmlElementParser.getNodeName();
        if ("Controls:ACCRadioButton".equals(nodeName)) {
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
        if ("src".equals(startTag)) {
            nodeValue = nodeValue.replace("@Embed('", "").replace("')", "");
        }
        if (compareTrueWithValue) {
            if ("true".equals(nodeValue)) {
                elementParser.getAttributeParsers().add(new AttributeParser(startTag, "true"));
            }
        } else {
            elementParser.getAttributeParsers().add(new AttributeParser(startTag, nodeValue));
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

    public static StringBuilder createCssElement(List<CssParser> cssParsers) {
        StringBuilder cssBuilder = new StringBuilder();
        // css
        for (CssParser css : cssParsers) {
            cssBuilder.append(css.getKey()).append(Constants.SYNTAX_COLON).append(css.getValue()).append(Constants.SYNTAX_SEMICOLON);
        }
        return cssBuilder;
    }

    public static StringBuilder createCssElementInline(List<CssParser> cssParsers) {
        StringBuilder cssElement = createCssElement(cssParsers);
        StringBuilder cssElementInline = new StringBuilder(Constants.ATTRIBUTE_STYLE + Constants.SYNTAX_EQUAL + Constants.SYNTAX_DOUBLE_QUOTATION);
        cssElementInline.append(cssElement).append(Constants.SYNTAX_DOUBLE_QUOTATION);
        return cssElementInline;
    }

    public static StringBuilder createCssElementInline(StringBuilder cssElement) {
        StringBuilder cssElementInline = new StringBuilder(Constants.ATTRIBUTE_STYLE + Constants.SYNTAX_EQUAL + Constants.SYNTAX_DOUBLE_QUOTATION);
        cssElementInline.append(cssElement).append(Constants.SYNTAX_DOUBLE_QUOTATION);
        return cssElementInline;
    }

    public static StringBuilder createAttributeElement(HtmlElementParser elementParser) {
        StringBuilder attributeBuilder = new StringBuilder();
        // add attribute
        for (AttributeParser attribute : elementParser.getAttributeParsers()) {
            attributeBuilder
                    .append(Constants.SYNTAX_SPACE)
                    .append(attribute.getKey())
                    .append(Constants.SYNTAX_EQUAL)
                    .append(Constants.SYNTAX_DOUBLE_QUOTATION)
                    .append(attribute.getValue())
                    .append(Constants.SYNTAX_DOUBLE_QUOTATION);
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

    private void handleFirstElement(HtmlElementParser elementChild, String parentNodeName, String currentNodeName) {
        if ("First Node".equals(parentNodeName) && (currentNodeName.equals(Constants.MXML_CONTAINERS_ACC_CANVAS) || currentNodeName.equals(Constants.MXML_CONTAINERS_ACC_TITLE_WINDOW))) {
            elementChild.setGenerateHtml(false);
            Optional<AttributeParser> parserOptional = elementChild.getAttributeParsers().stream().filter(item -> "title".equals(item.getKey())).findFirst();
            if (parserOptional.isPresent()) {
                this.elementReplace.setTitle(parserOptional.get().getValue());
            }
            this.elementReplace.setCssCompositionFirstList(elementChild.getCssParsers());
        }
    }

    /**
     * @param elementParser Handle tag jsf may be have ajax tag
     *                      Example: checkbox, radio,...
     */
    private void transformTagHaveAjax(HtmlElementParser elementParser) {
        AttributeParser attributeParser = elementParser.getMapAttributeParser().get("change");
        if (attributeParser != null) {
            if ("Controls:ACCCheckBox".equals(elementParser.getNodeName())) {
                CheckBoxReplace checkBoxReplace = new CheckBoxReplace();
                checkBoxReplace.setId(elementParser.getId());
//                checkBoxReplace.setListener(elementParser.get());
                checkBoxReplace.setEvent(attributeParser.getKey());
                this.elementReplace.getCheckBoxReplaces().add(checkBoxReplace);
            }
        }
    }
}
