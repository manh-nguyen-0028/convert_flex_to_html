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
    private static String xmlFileName;

    public AttributeService(String xmlFileName) {
        this.xmlFileName = xmlFileName;
    }

    public void attributeParse(Node node, HtmlElementParser htmlElementParser) {
        Map<String, PropertyMap> hmAttributeMap = MappingService.getAttributeMap();

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

    public static void attributeParseDetail(HtmlElementParser elementParser, PropertyMap propertyMap, Node nodeAtt) {
        String nodeValue = nodeAtt.getNodeValue();
        String attType = propertyMap.getType();

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
        String startTag = propertyMap.getConvertTo();
        CssParser cssParser = null;
        if ("opacity".equals(startTag)) {
            if (Double.parseDouble(nodeValue) > 0) {
                cssParser = new CssParser(propertyMap.getConvertTo(), nodeValue + propertyMap.getEndTag());
//                elementParser.getCssParsers().add(new CssParser(propertyMap.getConvertTo(), nodeValue + propertyMap.getEndTag()));
            }
        } else {
            cssParser = new CssParser(propertyMap.getConvertTo(), nodeValue + propertyMap.getEndTag());
//            elementParser.getCssParsers().add(new CssParser(propertyMap.getConvertTo(), nodeValue + propertyMap.getEndTag()));
        }
        if (cssParser!=null) {
            String cssValue = cssParser.getValue();
            if (cssValue.contains("%px")) {
                cssValue = cssValue.replace("%px","%");
                cssParser.setValue(cssValue);
            }
            elementParser.getCssParsers().add(cssParser);
        }

        return elementParser;
    }

    private static HtmlElementParser propertyParser(HtmlElementParser elementParser, PropertyMap propertyMap, String nodeValue) {
        // handle when att = img
        String convertTo = propertyMap.getConvertTo();
        boolean compareTrueWithValue = propertyMap.isValueCompareTrue();
        PropertyParser propertyParser = new PropertyParser(convertTo, nodeValue);
        if ("src".equals(convertTo)) {
            nodeValue = nodeValue.replace("@Embed('", "").replace("')", "").replace("/",":");
            CssParser cssParser = new CssParser("background-image", "url(#{resource['" + nodeValue + "']})");
            elementParser.getCssParsers().add(cssParser);
            propertyParser.setUse(false);
        }
        if (!compareTrueWithValue || (compareTrueWithValue && "true".equals(nodeValue))) {
            elementParser.getPropertyParsers().add(propertyParser);
        }
        return elementParser;
    }

    private static HtmlElementParser javascriptParser(HtmlElementParser elementParser, PropertyMap propertyMap, String nodeValue) {
        String startTag = propertyMap.getConvertTo();
        if (nodeValue.contains("script.")) {
            nodeValue = nodeValue.replaceAll("script\\.(.*?)\\((.*?)\\)", "#{" + xmlFileName + Constants.CLASS_CONTROLLER + Constants.SYNTAX_DOT + "$1}");
        }
        elementParser.getPropertyParsers().add(new PropertyParser(startTag, nodeValue));
        return elementParser;
    }
}
