package service;

import constants.Constants;
import dto.mxml.mapping.*;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.*;

public class ConvertService {
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

    public void printNote(NodeList nodeList, StringBuilder html, String ownerNodeName, StringBuilder cssFileAdd, String fileName) throws IOException {
        xmlFileName = fileName;

        hmMappingTag = XmlService.getMappingTag();

        hmMappingAttribute = XmlService.getMappingAttribute();

        HtmlAttributeTag htmlAttTag = new HtmlAttributeTag();
        List<Double> fillAlphas = new ArrayList<>();
        List<String> fillColors = new ArrayList<>();

        boolean firstCanvas = true;
        for (int count = 0; count < nodeList.getLength(); count++) {

            Node tempNode = nodeList.item(count);
            System.out.println("Node Name =" + tempNode.getNodeName() + " [OPEN]");
            if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
                MappingAttribute mappingAttribute = hmMappingAttribute.get(tempNode.getNodeName());
                if (mappingAttribute != null) {
                    if (tempNode.hasAttributes()) {
                        // get attributes names and values
                        NamedNodeMap nodeAttMap = tempNode.getAttributes();
                        for (int i = 0; i < nodeAttMap.getLength(); i++) {
                            Node nodeAtt = nodeAttMap.item(i);
                            MappingTag mappingTag = hmMappingTag.get(nodeAtt.getNodeName());
                            if (mappingTag != null) {
                                handleAttributeTag(htmlAttTag, mappingTag, nodeAtt, tempNode, fillAlphas, fillColors);
                            }
                        }
                    }

                    // handle attribute fillAlphas and fillColors
                    handleAttFillColor(htmlAttTag, fillAlphas, fillColors);

                    // generic id if null
                    htmlAttTag.setId(StringUtils.isEmpty(htmlAttTag.getId()) ? "id_" + utils.StringUtils.generateRandomText(3) : htmlAttTag.getId());

                    // style to file
                    StringBuilder cssElement = createCssElement(htmlAttTag, ownerNodeName);
                    StringBuilder cssInHtmlAdd = new StringBuilder();
                    StringBuilder cssNeedAddFile = new StringBuilder();

                    if (StringUtils.isNotEmpty(cssElement) && StringUtils.isNotEmpty(htmlAttTag.getId())) {
                        cssNeedAddFile.append("#").append(htmlAttTag.getId()).append(" {").append(cssElement).append("} \n");
                        if ("Containers:ACCTitleWindow".equals(tempNode.getNodeName())) {
                            // String style add body
                            String cssAddBody = cssElement.toString().replace("position: absolute;", "");
                            cssNeedAddFile.append("body {").append(cssAddBody).append("} \n");
                            htmlAttTag.setClassName("acc-title-window");
                            firstCanvas = false;
                        }
                    } else if ((StringUtils.isNotEmpty(cssElement) && StringUtils.isEmpty(htmlAttTag.getId()))) {
                        cssInHtmlAdd = cssInHtmlAdd.append("style ='").append(cssElement).append("'");
                    }

                    // delete or replace element
                    String htmlNeedAdd = modifyHtml(mappingAttribute, htmlAttTag, cssElement, ownerNodeName);
                    ;

                    htmlNeedAdd = htmlNeedAdd.replace("{tab_button_replace}", "<button class=\"tab-button\" onclick=\"openTabPosition('0')\">Tab 1</button>\n" +
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

    /**
     * Delete or replace html element
     */
    private String modifyHtml(MappingAttribute mappingAttribute, HtmlAttributeTag htmlAttributeTag, StringBuilder cssElement, String parentNodeName) {
        HashMap<String, MappingElementModify> hmElementModify = XmlService.getMappingElementModify();

        String className = mappingAttribute.getClassName();
        // replace class
        String htmlNeedAdd = mappingAttribute.getHtmlTagStart().replace("{class_replace}", className);

        if (StringUtils.isNotEmpty(parentNodeName) && hmElementModify.get(parentNodeName) != null) {
            MappingElementModify elementModify = hmElementModify.get(parentNodeName);
            String typeModify = elementModify.getTypeModify();
            if (Constants.TYPE_MODIFY_ADD.equals(typeModify)) {
                String attNeedAdd = elementModify.getAttributeAdd();
                boolean checkAttContains = htmlNeedAdd.contains(attNeedAdd + "='");
                if (checkAttContains) {
                    htmlNeedAdd = htmlNeedAdd.replace(attNeedAdd + "='", attNeedAdd + "='" + elementModify.getHtmlInElement() + " ");
                }
            }
        }

        if (!mappingAttribute.getStyleInFile()) {
            htmlNeedAdd = htmlNeedAdd.replace("{style}", "style ='" + cssElement + "'");
        }
        if (StringUtils.isNotEmpty(htmlAttributeTag.getId())) {
            htmlNeedAdd = htmlNeedAdd + "id='" + htmlAttributeTag.getId() + "'";
        }

        htmlNeedAdd = htmlNeedAdd + mappingAttribute.getHtmlTagStart2();
        if (StringUtils.isNotEmpty(htmlAttributeTag.getText())) {
            htmlNeedAdd = htmlNeedAdd + htmlAttributeTag.getText();
        }

        if ("acc-tab-navigator".equals(className)) {
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

    public void handleAttributeTag(HtmlAttributeTag htmlAttTag, MappingTag mappingTag, Node nodeAtt, Node tempNode, List<Double> fillAlphas, List<String> fillColors) {
        String nodeValue = nodeAtt.getNodeValue();

        String startTag = mappingTag.getStartTag();
        String attType = mappingTag.getType();
        if (ATT_CSS.equals(attType)) {
            htmlAttTag = convertToCss(htmlAttTag, mappingTag, nodeValue);
        } else if (ATT_TEXT.equals(attType)) {
            if ("Controls:ACCRadioButton".equals(tempNode.getNodeName()) || "Controls:ACCCheckBox".equals(tempNode.getNodeName())) {
                htmlAttTag.setText("<label for=\"rdoOneReam1\">" + nodeValue + "</label>");
            } else {
                htmlAttTag.setText(nodeValue);
            }
        } else if ("id".equals(attType)) {
            htmlAttTag.setId(nodeValue);
        } else if (ATT_OTHER.equals(attType)) {
            if ("fillAlphas".equals(startTag)) {
                String[] aAlpha = nodeValue.split(",");
                for (String s : aAlpha) {
                    double alpha = Double.parseDouble(s.replace("[", "").replace("]", ""));
                    fillAlphas.add(alpha);
                }
            } else if ("fillColors".equals(startTag)) {
                String[] aColor = nodeValue.split(",");
                for (String s : aColor) {
                    fillColors.add(s.replace("[", "").replace("]", ""));
                }
            }
        } else if (ATT_ATTRIBUTE.equals(attType)) {
            htmlAttTag = convertToAtt(htmlAttTag, mappingTag, nodeValue);
        } else if (ATT_JS.equals(attType)) {
            htmlAttTag = convertToJs(htmlAttTag, mappingTag, nodeValue);
        } else {

        }
    }

    private HtmlAttributeTag convertToCss(HtmlAttributeTag htmlAttTag, MappingTag mappingTag, String nodeValue) {
        String startTag = mappingTag.getStartTag();
        String cssAdd = startTag + ":" + nodeValue + mappingTag.getEndTag() + ";";
        if ("opacity".equals(startTag)) {
            if (Double.parseDouble(nodeValue) > 0) {
                htmlAttTag.getStyles().put(startTag, nodeValue + mappingTag.getEndTag());
            }
        } else {
            htmlAttTag.getStyles().put(startTag, nodeValue + mappingTag.getEndTag());
        }

        return htmlAttTag;
    }

    private HtmlAttributeTag convertToAtt(HtmlAttributeTag htmlAttTag, MappingTag mappingTag, String nodeValue) {
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
        Iterator<Map.Entry<String, String>> iterator = cssMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
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
                cssElement = cssElement.replace(startIndex, startIndex + lengthOfStr, "");
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
            styleValue.substring(0, styleValue.length() - 2);
            styleValue.append(")");
            htmlAttTag.getStyles().put("background", styleValue.toString());
        }
    }
}
