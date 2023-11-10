package mxml.service;

import constants.Constants;
import mxml.dto.mapping.PropertyMap;
import mxml.dto.parser.CssParser;
import mxml.dto.parser.HtmlElementParser;
import mxml.dto.parser.PropertyParser;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.Map;

public class AttributeService {
    private final String xmlFileName;
    private final Map<String, Object> retrievedMap;

    public AttributeService(String xmlFileName, Map<String, Object> retrievedMap) {
        this.xmlFileName = xmlFileName;
        this.retrievedMap = retrievedMap;
    }

    public void attributeParse(Node node, HtmlElementParser htmlElementParser) {
        Map<String, PropertyMap> hmAttributeMap = (Map<String, PropertyMap>) retrievedMap.get("attributeMap");

        if (node.hasAttributes()) {
            NamedNodeMap nodeAttMap = node.getAttributes();
            for (int i = 0; i < nodeAttMap.getLength(); i++) {
                Node nodeAtt = nodeAttMap.item(i);
                PropertyMap propertyMap = hmAttributeMap.get(nodeAtt.getNodeName());
                if (propertyMap != null) {
                    attributeParseDetail(htmlElementParser, propertyMap, nodeAtt);
                }
            }
        }
    }

    public void attributeParseDetail(HtmlElementParser elementParser, PropertyMap propertyMap, Node nodeAtt) {
        String nodeValue = nodeAtt.getNodeValue();
        String attType = propertyMap.getXhtmlType();

        switch (attType) {
            case "css":
                cssParser(elementParser, propertyMap, nodeValue);
                break;

            case "text":
                break;

            case "id":
                propertyParser(elementParser, propertyMap, nodeValue);
                elementParser.setId(nodeValue);
                break;
            case "property":
                propertyParser(elementParser, propertyMap, nodeValue);
                break;

            case "js":
                javascriptParser(elementParser, propertyMap, nodeValue);
                break;

            default:
                // No action for other cases
        }
    }

    private static HtmlElementParser cssParser(HtmlElementParser elementParser, PropertyMap propertyMap, String nodeValue) {
        String startTag = propertyMap.getXhtmlConvertTo();
        CssParser cssParser = null;
        if ("opacity".equals(startTag)) {
            if (Double.parseDouble(nodeValue) > 0) {
                cssParser = new CssParser(propertyMap.getXhtmlConvertTo(), nodeValue + propertyMap.getXhtmlEndTag());
            }
        } else {
            cssParser = new CssParser(propertyMap.getXhtmlConvertTo(), nodeValue + propertyMap.getXhtmlEndTag());
        }
        if (cssParser != null) {
            String cssValue = cssParser.getValue();
            if (cssValue.contains("%px")) {
                cssValue = cssValue.replace("%px", "%");
                cssParser.setValue(cssValue);
            }
            elementParser.getCssParsers().add(cssParser);
        }

        return elementParser;
    }

    private static HtmlElementParser propertyParser(HtmlElementParser elementParser, PropertyMap propertyMap, String nodeValue) {
        // handle when att = img
        String convertTo = propertyMap.getXhtmlConvertTo();
        boolean compareTrueWithValue = propertyMap.isXhtmlValueCompareTrue();
        PropertyParser propertyParser = new PropertyParser(convertTo, nodeValue, propertyMap.getXhtmlType(), propertyMap.isGenerateHtml());
        if ("src".equals(convertTo)) {
            nodeValue = nodeValue.replace("@Embed('", "").replace("')", "").replace("/", ":");
            CssParser cssParser = new CssParser("background-image", "url(#{resource['" + nodeValue + "']})");
            elementParser.getCssParsers().add(cssParser);
            propertyParser.setGenerateHtml(false);
        }
        if (!compareTrueWithValue || (compareTrueWithValue && "true".equals(nodeValue))) {
            elementParser.getPropertyParsers().add(propertyParser);
        }
        return elementParser;
    }

    private HtmlElementParser javascriptParser(HtmlElementParser elementParser, PropertyMap propertyMap, String nodeValue) {
        String startTag = propertyMap.getXhtmlConvertTo();
        if (nodeValue.contains("script.")) {
            nodeValue = nodeValue.replaceAll("script\\.(.*?)\\((.*?)\\)", "#{" + xmlFileName + Constants.CLASS_CONTROLLER + Constants.DOT_CHAR + "$1}");
        }
        elementParser.getPropertyParsers().add(new PropertyParser(startTag, nodeValue, propertyMap.getXhtmlType()));
        return elementParser;
    }
}
