package service;

import constants.Constants;
import dto.mxml.mapping.*;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConvertMxmlService {
    public static HashMap<String, MappingTag> hmMappingTag;

    public static HashMap<String, MappingAttribute> hmMappingAttribute;

    public static String xmlFileName;

    private static String ATT_CSS = "css";
    private static String ATT_JS = "js";
    private static String ATT_TEXT = "text";
    private static String ATT_OTHER = "other";
    private static String ATT_ATTRIBUTE = "attribute";

    public static String initHtml(String title, String cssName) {
        String htmlStart = "<html><head>" + "<meta charset=\"UTF-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<title>{p_title}</title>" +
                "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js\"></script>" +
                "<link rel=\"stylesheet\" href=\"css\\common.css\">" +
                "<script src=\"js\\common.js\"></script>" +
                "<link rel=\"stylesheet\" href=\"css\\control.css\">" +
                "<link rel=\"stylesheet\" href=\"css\\{p_css}.css\">" +
                "</head><body id=\"" + cssName + "_body\" >";
        htmlStart = htmlStart.replace("{p_css}", cssName).replace("{p_title}", title);
        return htmlStart;
    }

    private static void handleAttributes(Node tempNode, HtmlAttributeTag htmlAttTag, List<Double> fillAlphas, List<String> fillColors) {
        if (tempNode.hasAttributes()) {
            NamedNodeMap nodeAttMap = tempNode.getAttributes();
            for (int i = 0; i < nodeAttMap.getLength(); i++) {
                Node nodeAtt = nodeAttMap.item(i);
                MappingTag mappingTag = hmMappingTag.get(nodeAtt.getNodeName());
                if (mappingTag != null) {
                    handleAttributeTag(htmlAttTag, mappingTag, nodeAtt, tempNode, fillAlphas, fillColors);
                }
            }
        }
    }

    public static void printNote(NodeList nodeList, StringBuilder html, String ownerNodeName, StringBuilder cssFileAdd, String fileName) throws IOException {
        xmlFileName = fileName;

        hmMappingTag = XmlService.getMappingTag();

        hmMappingAttribute = XmlService.getMappingAttribute();

        HtmlAttributeTag htmlAttTag = new HtmlAttributeTag();
        List<Double> fillAlphas = new ArrayList<>();
        List<String> fillColors = new ArrayList<>();

        for (int count = 0; count < nodeList.getLength(); count++) {

            Node tempNode = nodeList.item(count);
            System.out.println("Node Name =" + tempNode.getNodeName() + " [OPEN]");
            if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
                MappingAttribute mappingAttribute = hmMappingAttribute.get(tempNode.getNodeName());
                if (mappingAttribute != null) {
                    handleAttributes(tempNode, htmlAttTag, fillAlphas, fillColors);

                    handleAttFillColor(htmlAttTag, fillAlphas, fillColors);

                    htmlAttTag.setId(StringUtils.defaultIfBlank(htmlAttTag.getId(), "id_" + utils.StringUtils.generateRandomText(3)));

                    StringBuilder cssElement = createCssElement(htmlAttTag, ownerNodeName);

                    StringBuilder cssNeedAddFile = new StringBuilder();
                    StringBuilder cssInHtmlAdd = new StringBuilder();

                    if (StringUtils.isNotBlank(cssElement)) {
                        if (StringUtils.isNotBlank(htmlAttTag.getId())) {
                            cssNeedAddFile.append("#").append(htmlAttTag.getId()).append(" {").append(cssElement).append("} \n");
                            if ("Containers:ACCTitleWindow".equals(tempNode.getNodeName())) {
                                // String style add body
                                String cssAddBody = cssElement.toString().replace("position: absolute;", "");
                                cssNeedAddFile.append("body {").append(cssAddBody).append("} \n");
                                htmlAttTag.setClassName("acc-title-window");
                            }
                        } else {
                            cssInHtmlAdd.append("style ='").append(cssElement).append("'");
                        }
                    }

                    // delete or replace element
                    StringBuilder htmlNeedAdd = modifyHtml(mappingAttribute, htmlAttTag, cssElement, ownerNodeName);

                    // replace tab element
                    utils.StringUtils.replaceInStringBuilder(htmlNeedAdd, "{tab_button_replace}", "<button class=\"tab-button\" onclick=\"openTabPosition('0')\">Tab 1</button>\n" +
                            "  <button class=\"tab-button\" onclick=\"openTabPosition('1')\">Tab 2</button>");

                    // Append for html
                    html.append(htmlNeedAdd);

                    if (mappingAttribute.getStyleInFile()) {
                        // remove position: absolute;
                        cssFileAdd.append(cssNeedAddFile);
                    }

                    htmlAttTag = new HtmlAttributeTag();
                    fillAlphas = new ArrayList<>();
                    fillColors = new ArrayList<>();

                    if (tempNode.hasChildNodes()) {
                        // loop again if has child nodes
                        printNote(tempNode.getChildNodes(), html, tempNode.getNodeName(), cssFileAdd, fileName);
                    }

                    html.append(mappingAttribute.getHtmlTagEnd());
                    System.out.println("Node Name =" + tempNode.getNodeName() + " [CLOSE]");
                }
            }
        }
    }

    private static StringBuilder buildHtmlStartTag(MappingAttribute mappingAttribute, HtmlAttributeTag htmlAttributeTag, StringBuilder cssElement, String parentNodeName) {
        HashMap<String, MappingElementModify> hmElementModify = XmlService.getMappingElementModify();
        StringBuilder htmlNeedAdd = new StringBuilder(mappingAttribute.getHtmlTagStart());
        // replace class
        utils.StringUtils.replaceInStringBuilder(htmlNeedAdd, "{class_replace}", mappingAttribute.getClassName());

        if (StringUtils.isNotEmpty(parentNodeName) && hmElementModify.get(parentNodeName) != null) {
            MappingElementModify elementModify = hmElementModify.get(parentNodeName);
            String typeModify = elementModify.getTypeModify();
            if (Constants.TYPE_MODIFY_ADD.equals(typeModify)) {
                String attNeedAdd = elementModify.getAttributeAdd();
                int attIndex = htmlNeedAdd.indexOf(attNeedAdd + "='");
                if (attIndex != -1) {
                    htmlNeedAdd.replace(attIndex, attIndex + attNeedAdd.length() + 2, attNeedAdd + "='" + elementModify.getHtmlInElement() + " ");
                }
            }
        }

        if (!mappingAttribute.getStyleInFile()) {
            utils.StringUtils.replaceInStringBuilder(htmlNeedAdd, "{style}", "style ='" + cssElement + "'");
        }
        if (StringUtils.isNotEmpty(htmlAttributeTag.getId())) {
            appendAttribute(htmlNeedAdd, "id", htmlAttributeTag.getId());
        }
        return htmlNeedAdd;
    }

    private static void appendAttribute(StringBuilder html, String attributeName, String attributeValue) {
        if (StringUtils.isNotEmpty(attributeValue)) {
            html.append(attributeName).append("='").append(attributeValue).append("'");
        }
    }

    /**
     * Delete or replace html element
     */
    private static StringBuilder modifyHtml(MappingAttribute mappingAttribute, HtmlAttributeTag htmlAttributeTag, StringBuilder cssElement, String parentNodeName) {
        StringBuilder htmlNeedAdd = buildHtmlStartTag(mappingAttribute, htmlAttributeTag, cssElement, parentNodeName);

        htmlNeedAdd.append(mappingAttribute.getHtmlTagStart2());
        if (StringUtils.isNotEmpty(htmlAttributeTag.getText())) {
            htmlNeedAdd.append(htmlAttributeTag.getText());
        }

        if ("acc-tab-navigator".equals(mappingAttribute.getClassName())) {
            String id = htmlAttributeTag.getId();
            TabConfig tabConfig = XmlService.getTabConfigByFileName(xmlFileName, id);
            StringBuilder buttonGeneric = new StringBuilder();

            for (int i = 0; i < tabConfig.getTabNameList().size(); i++) {

            }
            for (String item : tabConfig.getTabNameList()) {
                buttonGeneric.append("<button");
            }
        }
        return htmlNeedAdd;
    }

    public static void handleAttributeTag(HtmlAttributeTag htmlAttTag, MappingTag mappingTag, Node nodeAtt, Node tempNode, List<Double> fillAlphas, List<String> fillColors) {
        String nodeValue = nodeAtt.getNodeValue();
        String startTag = mappingTag.getStartTag();
        String attType = mappingTag.getType();

        switch (attType) {
            case "css":
                convertToCss(htmlAttTag, mappingTag, nodeValue);
                break;

            case "text":
                handleTextAttribute(htmlAttTag, tempNode, nodeValue);
                break;

            case "id":
                htmlAttTag.setId(nodeValue);
                break;

            case "other":
                handleOtherAttribute(startTag, nodeValue, fillAlphas, fillColors);
                break;

            case "attribute":
                convertToAtt(htmlAttTag, mappingTag, nodeValue);
                break;

            case "js":
                convertToJs(htmlAttTag, mappingTag, nodeValue);
                break;

            default:
                // No action for other cases
        }
    }

    private static void handleTextAttribute(HtmlAttributeTag htmlAttTag, Node tempNode, String nodeValue) {
        if ("Controls:ACCRadioButton".equals(tempNode.getNodeName()) || "Controls:ACCCheckBox".equals(tempNode.getNodeName())) {
            htmlAttTag.setText("<label for=\"rdoOneReam1\">" + nodeValue + "</label>");
        } else {
            htmlAttTag.setText(nodeValue);
        }
    }

    private static void handleOtherAttribute(String startTag, String nodeValue, List<Double> fillAlphas, List<String> fillColors) {
        if ("fillAlphas".equals(startTag)) {
            String[] aAlpha = nodeValue.split(",");
            for (String s : aAlpha) {
                if (!s.isEmpty()) {
                    double alpha = Double.parseDouble(s.replace("[", "").replace("]", ""));
                    fillAlphas.add(alpha);
                }
            }
        } else if ("fillColors".equals(startTag)) {
            String[] aColor = nodeValue.split(",");
            for (String s : aColor) {
                if (!s.isEmpty()) {
                    fillColors.add(s.replace("[", "").replace("]", ""));
                }
            }
        }
    }

    private static HtmlAttributeTag convertToCss(HtmlAttributeTag htmlAttTag, MappingTag mappingTag, String nodeValue) {
        String startTag = mappingTag.getStartTag();
        if ("opacity".equals(startTag)) {
            if (Double.parseDouble(nodeValue) > 0) {
                htmlAttTag.getStyles().put(startTag, nodeValue + mappingTag.getEndTag());
            }
        } else {
            htmlAttTag.getStyles().put(startTag, nodeValue + mappingTag.getEndTag());
        }

        return htmlAttTag;
    }

    private static HtmlAttributeTag convertToAtt(HtmlAttributeTag htmlAttTag, MappingTag mappingTag, String nodeValue) {
        // handle when att = img
        String startTag = mappingTag.getStartTag();
        boolean compareTrueWithValue = mappingTag.isValueCompareTrue();
        if ("src".equals(startTag)) {
            nodeValue = nodeValue.replace("@Embed('", "").replace("')", "");
        }
        if (compareTrueWithValue) {
            if ("true".equals(nodeValue)) {
                htmlAttTag.getAttributes().put(startTag, "true");
            }
        } else {
            htmlAttTag.getAttributes().put(startTag, nodeValue);
        }

        return htmlAttTag;
    }

    private static HtmlAttributeTag convertToJs(HtmlAttributeTag htmlAttTag, MappingTag mappingTag, String nodeValue) {
        String startTag = mappingTag.getStartTag();
        htmlAttTag.getAttributes().put(startTag, nodeValue);
        return htmlAttTag;
    }

    private static StringBuilder createCssElement(HtmlAttributeTag htmlAttTag, String ownerNodeName) {
        StringBuilder cssElement = new StringBuilder();
        HashMap<String, String> cssMap = htmlAttTag.getStyles();

        for (Map.Entry<String, String> entry : cssMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            System.out.println("Key: " + key + ", Value: " + value);
            cssElement.append(key).append(": ").append(value).append(";");
        }

        if ("Containers:ACCHBox".equals(ownerNodeName) || "Containers:ACCVBox".equals(ownerNodeName)) {
            String strNeedReplace = "position: absolute;";
            int startIndex = cssElement.indexOf(strNeedReplace);
            int lengthOfStr = strNeedReplace.length();
            if (startIndex >= 0) {
                cssElement.replace(startIndex, startIndex + lengthOfStr, "");
            }
        }

        return cssElement;
    }

    private static void handleAttFillColor(HtmlAttributeTag htmlAttTag, List<Double> fillAlphas, List<String> fillColors) {
        if (fillColors.size() > 0) {
            StringBuilder styleValue = new StringBuilder("linear-gradient(");
            for (int i = 0; i < fillColors.size(); i++) {
                double alpha = fillAlphas.get(i);
                int percentageValue = (int) alpha * 100;
                styleValue.append(fillColors.get(i)).append(" ").append(percentageValue).append("%, ");
            }
            styleValue.setLength(styleValue.length() - 2);  // Fix: Use setLength to modify the existing StringBuilder
            styleValue.append(")");
            htmlAttTag.getStyles().put("background", styleValue.toString());
        }
    }
}
