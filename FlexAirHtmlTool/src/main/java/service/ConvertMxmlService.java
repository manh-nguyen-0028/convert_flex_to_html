package service;

import constants.Constants;
import dto.mxml.mapping.ComponentMap;
import dto.mxml.mapping.PropertyMap;
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

    public void printNote(boolean isFirstCanvas, NodeList nodeList, StringBuilder baseHtml, StringBuilder html) {
        for (int count = 0; count < nodeList.getLength(); count++) {
            Node nodeItem = nodeList.item(count);
            String nodeName = nodeItem.getNodeName();
            Log.log("Node Name =" + nodeName + " [OPEN]");
            if (nodeItem.getNodeType() == Node.ELEMENT_NODE) {
                ComponentMap componentMap = hmNodeMap.get(nodeName);
                if (componentMap != null) {
                    handleNodeMap(componentMap, nodeItem, nodeName, baseHtml, isFirstCanvas, html);
                }
            }
        }
    }

    private void handleNodeMap(ComponentMap componentMap, Node nodeItem, String nodeName, StringBuilder baseHtml, boolean isFirstCanvas, StringBuilder html) {
        HtmlElementParser htmlElementParser = new HtmlElementParser(nodeName, componentMap.getHtmlTagStart(), componentMap.getHtmlTagStart2(), componentMap.getHtmlTagEnd(), componentMap.getIsGenerateHtml());

        handleNodeAttributes(nodeItem, htmlElementParser);

        StringBuilder cssElement = createCssElement(htmlElementParser);

        StringBuilder attributeElement = createAttributeElement(htmlElementParser);

        isFirstCanvas = handleFirstElement(nodeName, baseHtml, cssElement, isFirstCanvas, htmlElementParser);

        handleSpecialAttribute(htmlElementParser);

        if (htmlElementParser.isGenerateHtml()) {
            html.append(createHtmlElementParser(htmlElementParser, attributeElement, cssElement));
        }

        if (nodeItem.hasChildNodes()) {
            // loop again if has child nodes
            printNote(isFirstCanvas, nodeItem.getChildNodes(), baseHtml, html);
        }

        if (htmlElementParser.isGenerateHtml()) {
            html.append(htmlElementParser.getEndTag());
        }

        handleSpecialElement(htmlElementParser, attributeElement, cssElement);

        Log.log(htmlElementParser.getText());
        Log.log("Node Name =" + nodeName + " [CLOSE]");
    }

    private void handleSpecialElement(HtmlElementParser htmlElementParser, StringBuilder attributeElement, StringBuilder cssElement) {
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

    private void handleSpecialAttribute(HtmlElementParser htmlElementParser) {
        String id = htmlElementParser.getId();
        if ("ttPwd".equals(id)) {
            htmlElementParser.setStartTag("<p:password ");
            htmlElementParser.setEndTag("</p:password>");
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
        if ("script.".equals(nodeValue)) {
            nodeValue = nodeValue.replaceAll("script\\.(.*?)\\(\\)", "#{" + xmlFileName + Constants.CLASS_CONTROLLER + Constants.SYNTAX_DOT + "$1}");
        }
        elementParser.getAttributeParsers().add(new AttributeParser(startTag, nodeValue));
        return elementParser;
    }

    private StringBuilder createCssElement(HtmlElementParser elementParser) {
        StringBuilder cssBuilder = new StringBuilder();
        // css
        for (CssParser css : elementParser.getCssParsers()) {
            cssBuilder.append(css.getKey()).append(Constants.SYNTAX_COLON).append(css.getValue()).append(Constants.SYNTAX_SEMICOLON);
        }
        return cssBuilder;
    }

    private StringBuilder createAttributeElement(HtmlElementParser elementParser) {
        StringBuilder attributeBuilder = new StringBuilder();
        // add attribute
        for (AttributeParser attribute : elementParser.getAttributeParsers()) {
            attributeBuilder.append(attribute.getKey()).append(Constants.SYNTAX_EQUAL).
                    append(Constants.SYNTAX_DOUBLE_QUOTATION).append(attribute.getValue()).append(Constants.SYNTAX_DOUBLE_QUOTATION);
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

    private boolean handleFirstElement(String nodeName, StringBuilder baseHtml, StringBuilder cssElement, boolean isFirstCanvas, HtmlElementParser elementParser) {
        if (isFirstCanvas && (nodeName.equals(Constants.MXML_CONTAINERS_ACC_CANVAS) || nodeName.equals(Constants.MXML_CONTAINERS_ACC_TITLE_WINDOW))) {
            Optional<AttributeParser> parserOptional = elementParser.getAttributeParsers().stream().filter(item -> "title".equals(item.getKey())).findFirst();
            if (parserOptional.isPresent()) {
                this.elementReplace.setTitle(parserOptional.get().getValue());
            }
            utils.StringUtils.replaceInStringBuilder(baseHtml, "{style_composition_first}", cssElement.toString());
            isFirstCanvas = false;
            elementParser.setGenerateHtml(true);
        }
        return isFirstCanvas;
    }
}
